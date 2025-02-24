package com.example.vibecut.Adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMuxer;
import android.net.Uri;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MediaCodecConverter {

    private final String TAG = "MediaCodecConverter";
    private final int FRAME_RATE = 40; // Частота кадров
    private final int BIT_RATE = 1000000; // Битрейт
    private final float VIDEO_DURATION_SECONDS = 3.0f; // Длительность видео
    private int VIDEO_WIDTH;
    private int VIDEO_HEIGHT;

    // Поиск поддерживаемого формата цвета
    private int findSupportedColorFormat(MediaCodecInfo codecInfo, String mimeType) {
        MediaCodecInfo.CodecCapabilities capabilities = codecInfo.getCapabilitiesForType(mimeType);
        for (int format : capabilities.colorFormats) {
            if (format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar ||
                    format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar ||
                    format == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible) {
                return format;
            }
        }
        return -1; // Неподдерживаемый формат
    }

    // Выбор кодека
    private MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) continue;
            for (String type : codecInfo.getSupportedTypes()) {
                if (type.equalsIgnoreCase(mimeType)) return codecInfo;
            }
        }
        return null;
    }

    // Поворот изображения, если требуется
    private Bitmap rotateBitmapIfRequired(Bitmap bitmap, Uri imageUri) {
        try {
            ExifInterface ei = new ExifInterface(imageUri.getPath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF: " + e.getMessage());
            return bitmap;
        }
    }

    // Декодирование изображения
    private Bitmap decodeSampledBitmapFromUri(Uri uri) {
        Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
        return bitmap != null ? rotateBitmapIfRequired(bitmap, uri) : null;
    }

    // Расчет размеров видео
    private void calculateSize(Bitmap bitmap, int targetWidth, int targetHeight) {
        int outputWidth = bitmap.getWidth();
        int outputHeight = bitmap.getHeight();
        float aspectRatio = (float) outputWidth / outputHeight;

        if (outputWidth > targetWidth) {
            outputWidth = targetWidth;
            outputHeight = (int) (targetWidth / aspectRatio);
        }
        if (outputHeight > targetHeight) {
            outputHeight = targetHeight;
            outputWidth = (int) (targetHeight * aspectRatio);
        }

        VIDEO_WIDTH = (outputWidth + 15) / 16 * 16;
        VIDEO_HEIGHT = (outputHeight + 15) / 16 * 16;
    }

    // Конвертация Bitmap в YUV
    private byte[] convertBitmapToYUV(Bitmap bitmap, int width, int height) {
        if (width > bitmap.getWidth() || height > bitmap.getHeight()) {
            Log.e(TAG, "Target dimensions exceed bitmap dimensions");
            return null;
        }

        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        byte[] yuv = new byte[width * height * 3 / 2]; // YUV420
        int yIndex = 0;
        int uvIndex = width * height;

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int p = pixels[j * width + i];
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                yuv[yIndex++] = (byte) ((0.299 * r + 0.587 * g + 0.114 * b)); // Y
                if (i % 2 == 0 && j % 2 == 0) {
                    yuv[uvIndex++] = (byte) ((-0.14713 * r - 0.28886 * g + 0.436 * b) + 128); // U
                    yuv[uvIndex++] = (byte) ((0.615 * r - 0.51499 * g - 0.10001 * b) + 128); // V
                }
            }
        }
        return yuv;
    }

    // Основной метод конвертации
    public Paths convertImageToVideoMediaCodec(Uri originFileUri, Uri outputFileUri) {
        String stringOutPutPath = outputFileUri.getPath();

        Bitmap bitmap = decodeSampledBitmapFromUri(originFileUri);
        if (bitmap == null) {
            Log.e(TAG, "Failed to decode bitmap from " + originFileUri);
            return null;
        }

        calculateSize(bitmap, 1080, 1920);
        bitmap = Bitmap.createScaledBitmap(bitmap, VIDEO_WIDTH, VIDEO_HEIGHT, true);

        File outputFile = new File(stringOutPutPath);
        if (outputFile.exists() && !outputFile.delete()) {
            Log.e(TAG, "Failed to delete existing output file.");
            return null;
        }
        stringOutPutPath = outputFileUri.getPath().substring(0, outputFileUri.getPath().lastIndexOf('.')) + ".mp4";

        try {
            MediaCodec encoder = setupEncoder();
            MediaMuxer muxer = new MediaMuxer(stringOutPutPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            byte[] yuv = convertBitmapToYUV(bitmap, VIDEO_WIDTH, VIDEO_HEIGHT);
            if (yuv == null) return null;

            int videoTrackIndex = -1;
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            long presentationTimeUs = 0;
            int totalFrames = (int) (VIDEO_DURATION_SECONDS * FRAME_RATE);
            long frameDurationUs = 1000000L / FRAME_RATE;
            int outputBufferIndex = 0;
            for (int frameIndex = 0; frameIndex < totalFrames; frameIndex++) {
                int inputBufferIndex = encoder.dequeueInputBuffer(10000);
                if (inputBufferIndex >= 0) {
                    ByteBuffer inputBuffer = encoder.getInputBuffer(inputBufferIndex);
                    inputBuffer.clear();
                    inputBuffer.put(yuv);
                    encoder.queueInputBuffer(inputBufferIndex, 0, yuv.length, presentationTimeUs, 0);
                    presentationTimeUs += frameDurationUs;
                }


                while ((outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000)) >= 0) {
                    ByteBuffer outputBuffer = encoder.getOutputBuffer(outputBufferIndex);
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        if (videoTrackIndex == -1) {
                            videoTrackIndex = muxer.addTrack(encoder.getOutputFormat());
                            muxer.start();
                        }
                    } else if (videoTrackIndex != -1) {
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                        muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo);
                    }
                    encoder.releaseOutputBuffer(outputBufferIndex, false);
                }
            }

            encoder.queueInputBuffer(encoder.dequeueInputBuffer(10000), 0, 0, presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            while (encoder.dequeueOutputBuffer(bufferInfo, 10000) >= 0) {
                encoder.releaseOutputBuffer(outputBufferIndex, false);
            }

            Log.i(TAG, "MediaCodec conversion successful: " + stringOutPutPath);
        } catch (IOException e) {
            Log.e(TAG, "MediaCodec conversion failed: " + e.getMessage());
            return null;
        }

        return new Paths(originFileUri.getPath(), stringOutPutPath);
    }

    // Настройка кодека
    private MediaCodec setupEncoder() throws IOException {
        MediaCodecInfo codecInfo = selectCodec("video/avc");
        if (codecInfo == null) throw new IOException("No codec found");

        int colorFormat = findSupportedColorFormat(codecInfo, "video/avc");
        if (colorFormat == -1) throw new IOException("No supported color format");

        MediaFormat format = MediaFormat.createVideoFormat("video/avc", VIDEO_WIDTH, VIDEO_HEIGHT);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        format.setLong(MediaFormat.KEY_DURATION, (long) (VIDEO_DURATION_SECONDS * 1000000L));

        MediaCodec encoder = MediaCodec.createEncoderByType("video/avc");
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoder.start();
        return encoder;
    }

    // Внутренний класс для хранения путей
    public static class Paths {
        public final String originPath;
        public final String outputPath;

        public Paths(String originPath, String outputPath) {
            this.originPath = originPath;
            this.outputPath = outputPath;
        }
    }
}