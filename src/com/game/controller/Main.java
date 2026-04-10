package com.game.controller;

import javax.swing.JFrame;
import java.awt.CardLayout;
import javax.swing.JPanel;
import com.game.view.MenuPanel;
import com.game.view.GamePanel;
import com.game.view.LevelPanel;

public class Main extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private MenuPanel menuPanel;
    private GamePanel gamePanel;
    private LevelPanel levelPanel;

    public Main() {
        setTitle("Scream Runner");
        
        // Kích thước chuẩn để giữ GamePanel ở mức 950x600
        setSize(965, 639); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setResizable(false); 

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // Khởi tạo các màn hình và truyền Main vào
        gamePanel = new GamePanel(this); 
        menuPanel = new MenuPanel(this);
        levelPanel = new LevelPanel(this); 

        // Gắn vào khung chứa
        mainContainer.add(menuPanel, "Menu");
        mainContainer.add(levelPanel, "Level"); 
        mainContainer.add(gamePanel, "Game");

        add(mainContainer);
        
        // Khởi động vào thẳng Menu
        showMenu(); 
    }

    // ==========================================
    // CÁC HÀM ĐIỀU HƯỚNG MÀN HÌNH
    // ==========================================
    public void showLevelSelection() {
        cardLayout.show(mainContainer, "Level");
    }

    public void showMenu() {
        SoundManager.stopBGM(); 
        SoundManager.playBGM("assets/sounds/menu_music.wav"); 
        cardLayout.show(mainContainer, "Menu"); 
    }

    public void startGame(int themeId) {
        SoundManager.stopBGM(); 
        gamePanel.setTheme(themeId); // Đặt chủ đề (Đồng cỏ, Sa mạc, Rừng đêm)
        gamePanel.resetToVoiceTest(); 
        cardLayout.show(mainContainer, "Game"); 
        gamePanel.requestFocusInWindow(); // Yêu cầu bàn phím tập trung vào Game
    }

    // ==========================================
    // CÁC HÀM XỬ LÝ DỮ LIỆU
    // ==========================================
    public void updatePlayerSkin(String color) {
        if (gamePanel != null) {
            gamePanel.setPlayerSkin(color);
        }
    }

    public static void main(String[] args) {
        new Main().setVisible(true);
    }
}