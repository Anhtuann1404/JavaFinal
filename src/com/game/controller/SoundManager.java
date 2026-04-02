package com.game.controller;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {
    
    // Biến lưu trữ riêng cho nhạc nền để dễ dàng dừng/đổi bài
    private static Clip bgmClip; 

    // Hàm này giữ lại để không bị lỗi với lệnh gọi bên GamePanel
    public static void loadAllSounds() {
        System.out.println("🎵 Hệ thống âm thanh đã sẵn sàng!");
    }

    // ==========================================
    // 1. PHẦN XỬ LÝ ÂM THANH NGẮN (SFX)
    // Dùng cho: Tiếng nhảy (jump.wav), đi bộ (walk.wav), Game Over (die.wav)...
    // ==========================================
    public static void playSound(String fileName) {
        try {
            File soundFile = new File(fileName);
            if (!soundFile.exists()) {
                System.out.println("🚨 Không tìm thấy file SFX: " + fileName);
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start(); // Phát một lần rồi tự tắt
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("🚨 Lỗi khi phát SFX: " + fileName);
        }
    }

    // ==========================================
    // 2. PHẦN XỬ LÝ NHẠC NỀN (BGM)
    // Dùng cho: Nhạc Menu, Nhạc trong trận (Lặp vô tận)
    // ==========================================
    public static void playBGM(String fileName) {
        try {
            // Nếu đang có nhạc nền khác chạy thì tắt đi để bật bài mới
            if (bgmClip != null && bgmClip.isRunning()) {
                bgmClip.stop();
                bgmClip.close();
            }

            File soundFile = new File(fileName);
            if (!soundFile.exists()) {
                System.out.println("🚨 Không tìm thấy file Nhạc nền BGM: " + fileName);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioIn);
            
            // Cài đặt lặp lại liên tục vô tận
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("🚨 Lỗi khi phát nhạc nền: " + fileName);
            e.printStackTrace();
        }
    }

    // ==========================================
    // 3. CÁC HÀM ĐIỀU KHIỂN NHẠC NỀN
    // ==========================================
    
    // Dừng hẳn nhạc nền (Dùng khi Game Over)
    public static void stopBGM() {
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
        }
    }

    // Chỉnh âm lượng nhạc nền (volume từ 0.0f đến 1.0f)
    // Ví dụ: 1.0f là 100%, 0.5f là 50%
    public static void setBGMVolume(float volume) { 
        if (bgmClip != null && bgmClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
            
            // Ép giới hạn an toàn từ 0.0f đến 1.0f
            volume = Math.max(0.0f, Math.min(volume, 1.0f)); 
            
            // Công thức chuyển đổi tỷ lệ phần trăm sang Decibel (dB)
            float range = gainControl.getMaximum() - gainControl.getMinimum();
            float gain = (range * volume) + gainControl.getMinimum();
            
            gainControl.setValue(gain);
        }
    }
}