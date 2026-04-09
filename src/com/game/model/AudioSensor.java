package com.game.model;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioSensor implements Runnable {
    private TargetDataLine line;
    private double currentVolume = 0;
    private boolean isCalibrated = false;
    private double backgroundNoiseLevel = 0;
    private final int CALIBRATION_FRAMES = 100;

    // =============================================
    // CẢI TIẾN 2: SMOOTHING BUFFER
    // Thay vì dùng raw volume trực tiếp (dễ bị spike),
    // lấy trung bình 5 frame gần nhất → loại bỏ tiếng động
    // ngẫu nhiên như gõ bàn, tiếng ồn xung quanh
    // =============================================
    private static final int BUFFER_SIZE = 5;
    private double[] volumeBuffer = new double[BUFFER_SIZE];
    private int bufferIndex = 0;
    private boolean bufferFull = false; // Đánh dấu buffer đã có đủ dữ liệu chưa

    public AudioSensor() {
        setupAudio();
    }

    private void setupAudio() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("🚨 Microphone không hỗ trợ định dạng này!");
                return;
            }
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();
        } catch (LineUnavailableException e) {
            System.err.println("🚨 Không thể truy cập Microphone: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        byte[] buffer = new byte[2048];
        int    framesCount = 0;
        double noiseSum    = 0;

        while (true) {
            int bytesRead = line.read(buffer, 0, buffer.length);
            if (bytesRead > 0) {
                short[] samples = new short[bytesRead / 2];
                ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
                          .asShortBuffer().get(samples);

                double sum = 0;
                for (short s : samples) sum += s * s;
                double rms    = Math.sqrt(sum / samples.length);
                double volume = Math.min(100, rms / 500);

                if (!isCalibrated) {
                    noiseSum += volume;
                    framesCount++;
                    if (framesCount >= CALIBRATION_FRAMES) {
                        backgroundNoiseLevel = noiseSum / CALIBRATION_FRAMES;
                        isCalibrated = true;
                        System.out.println("✅ Đã đo xong tiếng ồn nền: " + backgroundNoiseLevel);
                    }
                } else {
                    // Trừ noise nền trước
                    double cleanVolume = Math.max(0, volume - backgroundNoiseLevel);

                    // =============================================
// CẢI TIẾN 2 ÁP DỤNG Ở ĐÂY:
                    // Đẩy giá trị vào buffer vòng (circular buffer)
                    // rồi tính trung bình → currentVolume mượt hơn
                    // =============================================
                    volumeBuffer[bufferIndex] = cleanVolume;
                    bufferIndex = (bufferIndex + 1) % BUFFER_SIZE;
                    if (bufferIndex == 0) bufferFull = true;

                    currentVolume = getSmoothedVolume();
                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    // Tính trung bình các giá trị trong buffer
    private double getSmoothedVolume() {
        int count = bufferFull ? BUFFER_SIZE : bufferIndex;
        if (count == 0) return 0;
        double sum = 0;
        for (int i = 0; i < count; i++) sum += volumeBuffer[i];
        return sum / count;
    }

    public double  getCurrentVolume() { return currentVolume; }
    public boolean isCalibrated()     { return isCalibrated; }
}