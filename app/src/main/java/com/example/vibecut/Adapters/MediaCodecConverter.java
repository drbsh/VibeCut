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
    private final int FRAME_RATE = 24; // 24 кадра в секунду
    private float VIDEO_DURATION_SECONDS;
    private final int BIT_RATE = 1000000; // Bitrate in bits per second (adjust as needed)
    private int VIDEO_WIDTH;
    private int VIDEO_HEIGHT;

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
    private MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    // Helper method to decode the image with inSampleSize for scaling.
    private  Bitmap rotateBitmapIfRequired(Bitmap bitmap, Uri imageUri) {
        try {
            ExifInterface ei = new ExifInterface(imageUri.getPath());
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(bitmap, 270);
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    return flipImageHorizontal(bitmap);
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    return flipImageVertical(bitmap);
                default:
                    return bitmap;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading EXIF: " + e.getMessage());
            return bitmap; // Вернуть исходное изображение в случае ошибки
        }
    }

    private  Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private  Bitmap flipImageHorizontal(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f); // Горизонтальное отражение
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private  Bitmap flipImageVertical(Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f); // Вертикальное отражение
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    // Измените decodeSampledBitmapFromUri:
    private  Bitmap decodeSampledBitmapFromUri(Uri uri) {
        Bitmap bitmap = BitmapFactory.decodeFile(uri.getPath());
        if (bitmap != null) {
            bitmap = rotateBitmapIfRequired(bitmap, uri); // Поворачиваем изображение после декодирования
        }
        return bitmap;
    }
    private  void calculateSize(Bitmap bitmap, int targetWidth, int targetHeight){
        int outputWidth = bitmap.getWidth();
        int outputHeight = bitmap.getHeight();

        if (outputWidth > targetWidth || outputHeight > targetHeight) {
            float aspectRatio = (float) outputWidth / (float) outputHeight;

            if (outputWidth > outputHeight) { // Ориентируемся по ширине
                outputWidth = targetWidth;
                outputHeight = (int) (targetWidth / aspectRatio);
            } else { // Ориентируемся по высоте
                outputHeight = targetHeight;
                outputWidth = (int) (targetHeight * aspectRatio);
            }
            if (outputWidth > targetWidth) { // Проверяем, если ширина все еще превышает targetWidth
                outputWidth = targetWidth;
                outputHeight = (int) (targetWidth / aspectRatio);
            }
            if (outputHeight > targetHeight) { // Проверяем, если высота превышает targetHeight
                outputHeight = targetHeight;
                outputWidth = (int) (targetHeight * aspectRatio);
            }
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, outputWidth, outputHeight, true);
            bitmap = scaledBitmap;
        } else if (outputWidth < targetWidth) { //  если ширина меньше 1080, то делаем ее 1080
            float aspectRatio = (float) outputWidth / (float) outputHeight;

            outputWidth = targetWidth;
            outputHeight = (int) (targetWidth / aspectRatio);

            if (outputHeight > targetHeight) { // Если высота больше targetHeight, то уменьшаем до 2340, пересчитывая ширину
                outputHeight = targetHeight;
                outputWidth = (int) (targetHeight * aspectRatio);
            }

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, outputWidth, outputHeight, true);
            bitmap = scaledBitmap;
        } else if (outputHeight < targetHeight) { // Если высота меньше 2340, то делаем ее 2340
            float aspectRatio = (float) outputWidth / (float) outputHeight;

            outputHeight = targetHeight;
            outputWidth = (int) (targetHeight * aspectRatio);

            if (outputWidth > targetWidth) { // Если ширина больше targetWidth, то уменьшаем до 1080, пересчитывая высоту
                outputWidth = targetWidth;
                outputHeight = (int) (targetWidth / aspectRatio);
            }
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, outputWidth, outputHeight, true);
            bitmap = scaledBitmap;
        }
        // If width is larger than targetWidth, scale to targetWidth, keeping aspect ratio
        if (outputWidth > targetWidth) {
            float aspectRatio = (float) outputWidth / (float) outputHeight;
            outputWidth = targetWidth;
            outputHeight = (int) (targetWidth / aspectRatio);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, outputWidth, outputHeight, true);
            bitmap = scaledBitmap;
        }

        // If height is larger than targetHeight, scale to targetHeight, keeping aspect ratio
        if (outputHeight > targetHeight) {
            float aspectRatio = (float) outputWidth / (float) outputHeight;
            outputHeight = targetHeight;
            outputWidth = (int) (targetHeight * aspectRatio);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, outputWidth, outputHeight, true);
            bitmap = scaledBitmap;
        }

        VIDEO_WIDTH = outputWidth;
        VIDEO_HEIGHT = outputHeight;

    }
    public class Paths{
        public String originPath;
        public String outputPath;
        public Paths(String originPath, String outputPath){
            this.originPath = originPath;
            this.outputPath = outputPath;
        }
    }
    // Основной метод
    public Paths convertImageToVideoMediaCodec(Uri originFileUri, Uri outputFileUri, float time) {
        // 1. Determine the desired video dimensions (scale down if necessary).
        VIDEO_DURATION_SECONDS = time;
        int targetWidth = 1080; // Or whatever dimensions are compatible with your device.
        int targetHeight = 1920;
        String stringOutPutPath = outputFileUri.getPath();

        // 2.  Load and scale the bitmap using decodeSampledBitmapFromUri
        Bitmap bitmap = decodeSampledBitmapFromUri(originFileUri);

        if (bitmap == null) {
            Log.e(TAG, "Failed to decode bitmap from " + originFileUri);
            return null;
        }

        // 3. Scale bitmap to fit 1080x2340, keeping aspect ratio and avoiding cropping.
        calculateSize(bitmap, targetWidth, targetHeight);


        File outputFile = new File(stringOutPutPath);
        if (outputFile.exists()) {
            if (!outputFile.delete()) {
                Log.e(TAG, "Failed to delete existing output file.");
                return null;
            }
        }
        stringOutPutPath = outputFileUri.getPath().substring(0, outputFileUri.getPath().lastIndexOf('.')) + ".mp4";

        MediaCodec encoder = null;
        MediaMuxer muxer = null;
        int videoTrackIndex = -1;

        try {
            String mimeType = "video/avc";
            MediaCodecInfo codecInfo = selectCodec(mimeType);
            if (codecInfo == null) {
                Log.e(TAG, "No codec found for " + mimeType);
                return null;
            }

            int colorFormat = findSupportedColorFormat(codecInfo, mimeType);
            if (colorFormat == -1) {
                Log.e(TAG, "No supported color format found.");
                return null;
            }

            MediaFormat format = MediaFormat.createVideoFormat(mimeType, VIDEO_WIDTH, VIDEO_HEIGHT);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
            format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
            long dur = (long)(VIDEO_DURATION_SECONDS * 1000000L);
            format.setLong(MediaFormat.KEY_DURATION, dur);

            encoder = MediaCodec.createEncoderByType(mimeType);
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            encoder.start();

            muxer = new MediaMuxer(stringOutPutPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            byte[] yuv = convertBitmapToYUV(bitmap, VIDEO_WIDTH, VIDEO_HEIGHT);
            if (yuv == null) {
                Log.e(TAG, "Failed to convert bitmap to YUV.");
                return null;
            }

            ByteBuffer[] inputBuffers = encoder.getInputBuffers();
            ByteBuffer[] outputBuffers = encoder.getOutputBuffers();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            long presentationTimeUs = 0;

            for (int frameIndex = 0; frameIndex < VIDEO_DURATION_SECONDS * FRAME_RATE; frameIndex++) {
                int inputBufferIndex = encoder.dequeueInputBuffer(10000);
                if (inputBufferIndex >= 0) {
                    ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                    inputBuffer.clear();
                    inputBuffer.put(yuv);
                    encoder.queueInputBuffer(inputBufferIndex, 0, yuv.length, presentationTimeUs, 0);
                    presentationTimeUs += 1000000L / FRAME_RATE;
                    presentationTimeUs += 1000000L / FRAME_RATE;
                }

                int outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000);
                while (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = encoder.getOutputBuffer(outputBufferIndex);
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        if (videoTrackIndex == -1) {
                            videoTrackIndex = muxer.addTrack(encoder.getOutputFormat());
                            muxer.start();
                        }
                    } else {
                        if (videoTrackIndex != -1) {
                            outputBuffer.position(bufferInfo.offset);
                            outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                            muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo);
                        }
                    }
                    encoder.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000);
                }
            }

            int inputBufferIndex = encoder.dequeueInputBuffer(10000);
            if (inputBufferIndex >= 0) {
                encoder.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            }

            int outputBufferIndex;
            do {
                outputBufferIndex = encoder.dequeueOutputBuffer(bufferInfo, 10000);
                if (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = encoder.getOutputBuffer(outputBufferIndex);
                    if (videoTrackIndex != -1) {
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                        muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo);
                    }
                    encoder.releaseOutputBuffer(outputBufferIndex, false);
                }
            } while (outputBufferIndex != MediaCodec.INFO_TRY_AGAIN_LATER);

            Log.i(TAG, "MediaCodec conversion successful: " + stringOutPutPath);
        } catch (IOException e) {
            Log.e(TAG, "MediaCodec conversion failed: " + e.getMessage());
            return null;
        } finally {
            if (encoder != null) {
                encoder.stop();
                encoder.release();
            }
            if (muxer != null) {
                try {
                    if (videoTrackIndex != -1) {
                        muxer.stop(); // Останавливаем только если muxer был запущен
                    }
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Failed to stop muxer: " + e.getMessage());
                } finally {
                    muxer.release();
                }
            }
        }
        File OriginFile = new File(originFileUri.getPath());
        try{
            OriginFile.delete();
        } catch (Exception e) {
            Log.e("ErrorDelete", "Error deleting " + originFileUri + "\nError: " + e);
        }
        originFileUri = Uri.parse(originFileUri.getPath().substring(0, originFileUri.getPath().lastIndexOf(".")) + ".mp4");
        OriginFile = new File(originFileUri.getPath());
        try{
            OriginFile.createNewFile();
        } catch (IOException e) {
            Log.e("Error create file", "Error: ", e);
        }
        outputFile = new File(stringOutPutPath);
        try {
            FillingMediaFile.copyFile(outputFile, OriginFile);
        } catch (IOException e) {
            Log.e("CopyError", "Error Copy: " + outputFileUri + "\nto " + originFileUri);
        }
        Paths paths = new Paths(originFileUri.getPath(), stringOutPutPath);
        return paths;
    }

    // Helper Method: Convert Bitmap to YUV (Simplified, may need optimization)
    private byte[] convertBitmapToYUV(Bitmap bitmap, int width, int height) {
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

                // Y
                yuv[yIndex++] = (byte) ((0.299 * r + 0.587 * g + 0.114 * b));

                // U and V (every other pixel)
                if (i % 2 == 0 && j % 2 == 0) {
                    yuv[uvIndex++] = (byte) ((-0.16874 * r - 0.33126 * g + 0.5 * b) + 128); // U
                    yuv[uvIndex++] = (byte) ((0.5 * r - 0.41869 * g - 0.08131 * b) + 128); // V
                }
            }
        }
        return yuv;
    }

}