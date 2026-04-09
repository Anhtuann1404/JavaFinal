package com.game.view;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.game.controller.Main;
import com.game.controller.SoundManager;

public class LevelPanel extends JPanel implements MouseListener {
    private Main mainFrame; 
    private final int WIDTH = 950;
    private final int HEIGHT = 600;

    // Các khu vực click chuột cho từng màn
    private Rectangle[] levelRects = new Rectangle[3];
    private String[] levelNames = {"ĐỒNG CỎ", "SA MẠC", "RỪNG ĐÊM"};
    private Color[] levelColors = {new Color(50, 205, 50), new Color(218, 165, 32), new Color(75, 0, 130)};
    private Rectangle backRect; 

    public LevelPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setLayout(null); 
        this.addMouseListener(this); 

        // Khởi tạo tọa độ cho 3 thẻ màn chơi (nằm giữa màn hình)
        int startX = 175; 
        for (int i = 0; i < 3; i++) {
            levelRects[i] = new Rectangle(startX + (i * 220), HEIGHT/2 - 100, 160, 200);
        }
        
        // Tọa độ nút Quay lại
        backRect = new Rectangle(WIDTH/2 - 100, HEIGHT - 80, 200, 50);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ nền tối
        g2d.setColor(new Color(30, 30, 40)); 
        g2d.fillRect(0, 0, getWidth(), getHeight()); 
        
        // Vẽ Tiêu đề
        g2d.setFont(new Font("Monospaced", Font.BOLD, 60));
        g2d.setColor(Color.WHITE);
        g2d.drawString("CHỌN MÀN CHƠI", WIDTH/2 - 230, 100);
        
        // Vẽ 3 thẻ màn chơi
        for (int i = 0; i < 3; i++) {
            Rectangle r = levelRects[i];
            
            // Vẽ nền thẻ
            g2d.setColor(levelColors[i]);
            g2d.fillRoundRect(r.x, r.y, r.width, r.height, 20, 20);
            
            // Vẽ viền thẻ
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new java.awt.BasicStroke(3));
            g2d.drawRoundRect(r.x, r.y, r.width, r.height, 20, 20);
            
            // Vẽ Tên màn
            g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
            g2d.setColor(Color.WHITE);
            // Căn giữa chữ
            int textX = r.x + (r.width - g2d.getFontMetrics().stringWidth(levelNames[i])) / 2;
            g2d.drawString(levelNames[i], textX, r.y + r.height/2 + 10);
        }
        
        // Vẽ nút Quay lại
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
<<<<<<< HEAD
        g2d.setColor(Color.YELLOW);
=======
g2d.setColor(Color.YELLOW);
>>>>>>> e1b303dbbe9d1b3ecff920bc842441ff279cbb8b
        g2d.drawString("[ QUAY LẠI MENU ]", backRect.x + 10, backRect.y + 30);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        
        // Kiểm tra xem user bấm vào màn nào
        for (int i = 0; i < 3; i++) {
            if (levelRects[i].contains(mx, my)) {
                SoundManager.playSound("assets/sounds/jump.wav"); 
                // Truyền ID của màn (0, 1, 2) sang GamePanel
                mainFrame.startGame(i); 
                return;
            }
        }
        
        // Bấm nút quay lại
        if (backRect.contains(mx, my)) {
            SoundManager.playSound("assets/sounds/jump.wav"); 
            mainFrame.showMenu();
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}