package com.game.controller;

import javax.swing.JFrame;
import java.awt.CardLayout;
import javax.swing.JPanel;
import com.game.view.MenuPanel;
import com.game.view.GamePanel;

public class Main extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private MenuPanel menuPanel;
    private GamePanel gamePanel;

    public Main() {
        setTitle("Scream Runner");
        
        // Kích thước này bao gồm cả viền cửa sổ (Window borders)
        // Để GamePanel và MenuPanel bên trong giữ đúng tỷ lệ 950x600
        setSize(965, 639); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Giữa màn hình
        setResizable(false); // Khóa phóng to thu nhỏ để tránh vỡ giao diện

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Khởi tạo 2 màn hình
        gamePanel = new GamePanel();
        menuPanel = new MenuPanel(this);

        // Gắn vào khung chứa
        mainContainer.add(menuPanel, "Menu");
        mainContainer.add(gamePanel, "Game");

        add(mainContainer);
        
        // --- XỬ LÝ KHỞI ĐỘNG ---
        // 1. Hiển thị màn hình Menu đầu tiên
        cardLayout.show(mainContainer, "Menu");
        
        // 2. BẬT NHẠC NỀN MENU TỪ THƯ MỤC MỚI (Đã sửa đường dẫn)
        SoundManager.playBGM("assets/sounds/menu_music.wav");
    }

    // Hàm gọi khi nhấn nút PLAY ở MenuPanel
    public void startGame() {
        SoundManager.stopBGM(); // Tắt nhạc menu
        gamePanel.resetToVoiceTest(); // Đưa Game về trạng thái Test Mic
        cardLayout.show(mainContainer, "Game"); // Lật sang màn hình Game
        gamePanel.requestFocusInWindow(); // Lấy tiêu điểm bàn phím cho GamePanel
    }

    // Hàm gọi khi chọn nhân vật ở bảng Skin trong MenuPanel
    public void updatePlayerSkin(String color) {
        if (gamePanel != null) {
            gamePanel.setPlayerSkin(color);
        }
    }

    public static void main(String[] args) {
        new Main().setVisible(true);
    }
}