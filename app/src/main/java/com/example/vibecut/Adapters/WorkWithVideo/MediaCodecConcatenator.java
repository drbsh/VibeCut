package com.example.vibecut.Adapters.WorkWithVideo;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class MediaCodecConcatenator {
    public static void concatenateVideos(List<String> inputPaths, String outputPath) throws IOException {
        MediaMuxer muxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        int videoTrackIndex = -1;
        int audioTrackIndex = -1;
        long totalDuration = 0;

        for (String inputPath : inputPaths) {
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(inputPath);

            int numTracks = extractor.getTrackCount();
            for (int i = 0; i < numTracks; i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);

                if (mime.startsWith("video/") && videoTrackIndex == -1) {
                    videoTrackIndex = muxer.addTrack(format);
                } else if (mime.startsWith("audio/") && audioTrackIndex == -1) {
                    audioTrackIndex = muxer.addTrack(format);
                }
            }

            extractor.release();
        }

        muxer.start();

        for (String inputPath : inputPaths) {
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(inputPath);

            int numTracks = extractor.getTrackCount();
            for (int i = 0; i < numTracks; i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);

                if ((mime.startsWith("video/") && videoTrackIndex != -1) ||
                        (mime.startsWith("audio/") && audioTrackIndex != -1)) {
                    extractor.selectTrack(i);

                    int trackIndex = mime.startsWith("video/") ? videoTrackIndex : audioTrackIndex;
                    ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

                    while (true) {
                        int sampleSize = extractor.readSampleData(buffer, 0);
                        if (sampleSize < 0) {
                            break;
                        }

                        bufferInfo.size = sampleSize;
                        bufferInfo.presentationTimeUs = extractor.getSampleTime() + totalDuration;
                        bufferInfo.flags = convertSampleFlag(extractor);

                        muxer.writeSampleData(trackIndex, buffer, bufferInfo);
                        extractor.advance();
                    }
                }
            }

            totalDuration += extractor.getTrackFormat(0).getLong(MediaFormat.KEY_DURATION);
            extractor.release();
        }

        muxer.stop();
        muxer.release();
    }

    private static int convertSampleFlag(MediaExtractor mediaExtractor){
        int extractorFlags = mediaExtractor.getSampleFlags();

// Преобразуем флаги в формат, понятный MediaCodec
        int codecFlags = 0;
        if ((extractorFlags & MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
            codecFlags |= MediaCodec.BUFFER_FLAG_KEY_FRAME; // Ключевой кадр
        }
        if ((extractorFlags & MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME) != 0) {
            codecFlags |= MediaCodec.BUFFER_FLAG_PARTIAL_FRAME; // Частичный кадр
        }
        return codecFlags;
    }
}
