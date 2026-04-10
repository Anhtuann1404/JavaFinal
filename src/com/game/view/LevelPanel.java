package com.game.view;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;

import com.game.controller.Main;
import com.game.controller.SoundManager;

// NÂNG CẤP: Thêm MouseMotionListener để bắt sự kiện di chuyển chuột
public class LevelPanel extends JPanel implements MouseListener, MouseMotionListener {
    private Main mainFrame; 
    private final int WIDTH = 950, HEIGHT = 600;
    
    private Rectangle[] levelRects = new Rectangle[3];
    private String[] levelNames = {"Đồng Cỏ", "Sa Mạc", "Núi Tuyết"};
    private Color[] defaultColors = {new Color(50, 205, 50), new Color(218, 165, 32), new Color(75, 0, 130)};
    private Image[] levelImages = new Image[3];
    private Rectangle backRect; 

    // BIẾN MỚI: Lưu lại vị trí ô đang được chuột trỏ vào (-1 nghĩa là không trỏ vào đâu)
    private int hoveredIndex = -1;
    private boolean isBackHovered = false;

    public LevelPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setLayout(null); 
        this.addMouseListener(this); 
        this.addMouseMotionListener(this); // Lắng nghe di chuyển chuột

        try {
            File fPlain = new File("assets/images/ui/level_plain.png");
            if (fPlain.exists()) levelImages[0] = ImageIO.read(fPlain);

            File fDesert = new File("assets/images/ui/level_desert.png");
            if (fDesert.exists()) levelImages[1] = ImageIO.read(fDesert);

            File fForest = new File("assets/images/ui/level_forest.png");
            if (fForest.exists()) levelImages[2] = ImageIO.read(fForest);
        } catch (Exception e) {
            System.out.println("🚨 Lỗi tải ảnh Thumbnail trong LevelPanel!");
        }

        int startX = 175; 
        for (int i = 0; i < 3; i++) {
            levelRects[i] = new Rectangle(startX + (i * 220), HEIGHT/2 - 100, 160, 200);
        }
        backRect = new Rectangle(WIDTH/2 - 100, HEIGHT - 80, 200, 50);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.setColor(new Color(30, 30, 40)); 
        g2d.fillRect(0, 0, getWidth(), getHeight()); 
        
        g2d.setFont(new Font("Monospaced", Font.BOLD, 60));
        g2d.setColor(Color.WHITE);
        g2d.drawString("CHỌN MÀN CHƠI", WIDTH/2 - 230, 100);
        
        for (int i = 0; i < 3; i++) {
            Rectangle r = levelRects[i];
            
            if (levelImages[i] != null) {
                g2d.setClip(new java.awt.geom.RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 20, 20));
                g2d.drawImage(levelImages[i], r.x, r.y, r.width, r.height, null);
                g2d.setClip(null); 
            } else {
                g2d.setColor(defaultColors[i]);
                g2d.fillRoundRect(r.x, r.y, r.width, r.height, 20, 20);
            }
            
            // NÂNG CẤP: Chỉ vẽ viền trắng và làm ô to lên một xíu nếu con chuột đang nằm trên nó
            if (i == hoveredIndex) {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new java.awt.BasicStroke(4));
                // Vẽ viền lùi ra ngoài 2 pixel để tạo cảm giác hiệu ứng phóng to
                g2d.drawRoundRect(r.x - 2, r.y - 2, r.width + 4, r.height + 4, 22, 22);
            } else {
                // Viền mờ tịt khi không đưa chuột vào để khung hình có chiều sâu
                g2d.setColor(new Color(255, 255, 255, 50)); 
                g2d.setStroke(new java.awt.BasicStroke(2));
                g2d.drawRoundRect(r.x, r.y, r.width, r.height, 20, 20);
            }
            
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(r.x, r.y + r.height - 40, r.width, 40); 
            
            g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
            // Đổi màu chữ thành Vàng nếu đang Hover
            g2d.setColor(i == hoveredIndex ? Color.YELLOW : Color.WHITE);
            int textX = r.x + (r.width - g2d.getFontMetrics().stringWidth(levelNames[i])) / 2;
            g2d.drawString(levelNames[i], textX, r.y + r.height - 12);
        }
        
        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        // Đổi màu nút Back nếu đang Hover
        g2d.setColor(isBackHovered ? Color.WHITE : Color.GRAY);
        g2d.drawString("[ QUAY LẠI MENU ]", backRect.x + 10, backRect.y + 30);
    }

    // --- CÁC HÀM LẮNG NGHE CHUỘT DÀNH CHO HIỆU ỨNG HOVER ---
    @Override
    public void mouseMoved(MouseEvent e) {
        int oldHover = hoveredIndex;
        boolean oldBackHover = isBackHovered;
        
        hoveredIndex = -1;
        isBackHovered = false;
        boolean isHandCursor = false;

        // Quét xem chuột có nằm trên 1 trong 3 ô màn chơi không
        for (int i = 0; i < 3; i++) {
            if (levelRects[i].contains(e.getPoint())) {
                hoveredIndex = i;
                isHandCursor = true;
                break;
            }
        }
        
        // Quét xem chuột có nằm trên nút Back không
        if (backRect.contains(e.getPoint())) {
            isBackHovered = true;
            isHandCursor = true;
        }

        // Đổi con trỏ thành hình bàn tay nếu đang đè lên nút
        setCursor(new Cursor(isHandCursor ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));

        // CHỈ VẼ LẠI MÀN HÌNH NẾU TRẠNG THÁI CHUỘT BỊ THAY ĐỔI (Tối ưu hiệu năng)
        if (oldHover != hoveredIndex || oldBackHover != isBackHovered) {
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        for (int i = 0; i < 3; i++) {
            if (levelRects[i].contains(e.getPoint())) {
                SoundManager.playSound("assets/sounds/jump.wav"); 
                // Khi chuyển màn, nhớ reset chuột về mũi tên bình thường
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); 
                mainFrame.startGame(i); 
                return;
            }
        }
        if (backRect.contains(e.getPoint())) {
            SoundManager.playSound("assets/sounds/jump.wav"); 
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); 
            mainFrame.showMenu();
        }
    }
    
    // Các hàm không dùng đến nhưng bắt buộc phải có của Interface
    @Override public void mouseDragged(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {
        // Khi kéo chuột hẳn ra ngoài cửa sổ game thì tắt hết Hover
        hoveredIndex = -1;
        isBackHovered = false;
        repaint();
    }
}