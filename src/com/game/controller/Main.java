package com.game.controller;

import javax.swing.JFrame;
import java.awt.CardLayout;
import javax.swing.JPanel;
import com.game.view.MenuPanel;
import com.game.view.GamePanel;
import com.game.view.LevelPanel; // Đừng quên import cái này nhé

public class Main extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private MenuPanel menuPanel;
    private GamePanel gamePanel;
    private LevelPanel levelPanel;

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

        // Khởi tạo 3 màn hình (Nhớ truyền 'this' vào để chúng có thể gọi lại Main)
        gamePanel = new GamePanel(this); // Sửa: Truyền 'this' vào GamePanel
        menuPanel = new MenuPanel(this);
        levelPanel = new LevelPanel(this); // Thêm LevelPanel

        // Gắn vào khung chứa
        mainContainer.add(menuPanel, "Menu");
        mainContainer.add(levelPanel, "Level"); // Thêm vào CardLayout
        mainContainer.add(gamePanel, "Game");

        add(mainContainer);
        
        // --- XỬ LÝ KHỞI ĐỘNG ---
        // Hiển thị màn hình Menu và bật nhạc nền thông qua hàm showMenu()
        showMenu(); 
    }

    // ==========================================
    // CÁC HÀM ĐIỀU HƯỚNG MÀN HÌNH (ROUTER)
    // ==========================================

    /**
     * Gọi từ MenuPanel (Nút Play): Chuyển sang màn hình chọn Màn chơi
     */
    public void showLevelSelection() {
        cardLayout.show(mainContainer, "Level");
    }

    /**
     * Gọi từ GamePanel (Nút Thoát) hoặc LevelPanel (Nút Back): Về lại Menu chính
     */
    public void showMenu() {
        SoundManager.stopBGM(); // Tắt mọi nhạc đang phát
        SoundManager.playBGM("assets/sounds/menu_music.wav"); // Bật lại nhạc menu
        cardLayout.show(mainContainer, "Menu"); // Lật sang màn hình Menu
    }

    /**
     * Gọi từ LevelPanel (Khi bấm vào 1 thẻ màn chơi): Bắt đầu vào game
     * @param themeId ID của màn chơi (0: Đồng cỏ, 1: Sa mạc, 2: Rừng đêm)
     */
    public void startGame(int themeId) {
        SoundManager.stopBGM(); // Tắt nhạc menu
        gamePanel.setTheme(themeId); // Báo cho GamePanel biết người chơi chọn màn nào
        gamePanel.resetToVoiceTest(); // Đưa Game về trạng thái Test Mic
        cardLayout.show(mainContainer, "Game"); // Lật sang màn hình Game
        gamePanel.requestFocusInWindow(); // Lấy tiêu điểm bàn phím cho GamePanel
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ DỮ LIỆU
    // ==========================================

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