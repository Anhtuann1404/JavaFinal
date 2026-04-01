package com.game.view;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import javax.imageio.ImageIO;

import com.game.controller.Main;

public class MenuPanel extends JPanel implements MouseListener {
    private Image menuBg;
    private Main mainFrame; 
    
    private final int WIDTH = 950;
    private final int HEIGHT = 600;

    // --- CÁC BIẾN CHO GIAO DIỆN SKIN ---
    private boolean isSkinMenu = false; 
    private String[] characterColors = {"yellow", "pink", "purple", "green"};
    private Image[] charMenuImages = new Image[4]; 
    private Rectangle[] charSelectionRects = new Rectangle[4]; 
    private Rectangle backRect; 
    private String currentSkin = "yellow"; 

    // --- KHAI BÁO 3 NÚT LÀ BIẾN TOÀN CỤC ĐỂ DỄ ẨN/HIỆN ---
    private RoundButton playBtn;
    private RoundButton guideBtn;
    private RoundButton skinsBtn;

    // ==========================================
    // CLASS TỰ TẠO NÚT HÌNH TRÒN (Tránh đè Hitbox)
    // ==========================================
    class RoundButton extends JButton {
        public RoundButton() {
            super();
            setContentAreaFilled(false); 
            setBorderPainted(false); 
            setFocusPainted(false); 
        }

        @Override
        public boolean contains(int x, int y) {
            // Tính toán tọa độ tâm và bán kính của nút
            int radius = Math.min(getWidth(), getHeight()) / 2;
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            // Dùng công thức hình học để kiểm tra xem điểm click có nằm TRONG hình tròn không
            double distance = Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2);
            return distance <= Math.pow(radius, 2);
        }
    }

    public MenuPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setLayout(null); 
        this.addMouseListener(this); // Lắng nghe click chuột cho Skin menu

        // 1. TẢI ẢNH NỀN & ẢNH SKIN
        try {
            menuBg = ImageIO.read(new File("menu_bg.png"));
            for (int i = 0; i < 4; i++) {
                File f = new File("character_" + characterColors[i] + "_jump.png");
                if (f.exists()) charMenuImages[i] = ImageIO.read(f);
            }
        } catch (Exception e) {
            System.out.println("🚨 Lỗi: Không tìm thấy file ảnh nền hoặc ảnh nhân vật!");
        }

        // --- Tọa độ 4 ô chọn Skin và nút Back ---
        int startX = 245; 
        for (int i = 0; i < 4; i++) {
            charSelectionRects[i] = new Rectangle(startX + (i * 120), HEIGHT/2 - 60, 100, 120);
        }
        backRect = new Rectangle(WIDTH/2 - 350, HEIGHT - 80, 700, 50);

        // ==========================================
        // KHỞI TẠO 3 NÚT (GIỮ NGUYÊN THÔNG SỐ CỦA BẠN)
        // ==========================================
        int btnW = 360; 
        int btnH = 160;

        // 2. NÚT PLAY
        playBtn = new RoundButton();
        createRoundButtonImage(playBtn, "btn_play_round.png", btnW, btnH);
        playBtn.setBounds(305, 345, btnW, btnH); 
        playBtn.addActionListener(e -> {
            System.out.println("---> Clicked: PLAY");
            mainFrame.startGame(); 
        });
        this.add(playBtn);


        // 3. NÚT GUIDE
        guideBtn = new RoundButton();
        createRoundButtonImage(guideBtn, "btn_guide_round.png", btnW, btnH);
        guideBtn.setBounds(170, 330, btnW, btnH); 
        guideBtn.addActionListener(e -> {
            System.out.println("---> Clicked: GUIDE");
            showGuide();
        });
        this.add(guideBtn);


        // 4. NÚT SKINS 
        skinsBtn = new RoundButton();
        createRoundButtonImage(skinsBtn, "btn_skins_round.png", btnW, btnH);
        skinsBtn.setBounds(445, 330, btnW, btnH); 
        skinsBtn.addActionListener(e -> {
            System.out.println("---> Clicked: SKINS");
            // Mở giao diện Skin và Ẩn 3 nút đi
            isSkinMenu = true;
            playBtn.setVisible(false);
            guideBtn.setVisible(false);
            skinsBtn.setVisible(false);
            repaint();
        });
        this.add(skinsBtn);
    }

    // Hàm hỗ trợ để load ảnh vào nút
    private void createRoundButtonImage(RoundButton button, String fileName, int width, int height) {
        try {
            Image img = ImageIO.read(new File(fileName));
            Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            System.out.println("🚨 Lỗi: Không tìm thấy file ảnh nút: " + fileName);
            button.setText(fileName);
        }
    }

    private void showGuide() {
        String msg = "WELCOME TO SCREAM RUNNER!\n\n" +
                     "- Im lặng: Nhân vật đứng yên.\n" +
                     "- Nói nhỏ: Đi bộ.\n" +
                     "- Hét to: Nhảy lên!\n\n" +
                     "Hét thật to để vượt qua chướng ngại vật nhé!";
        JOptionPane.showMessageDialog(this, msg, "Hướng dẫn chơi", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ Nền
        if (menuBg != null) {
            g2d.drawImage(menuBg, 0, 0, WIDTH, HEIGHT, null);
        }

        // ==========================================
        // VẼ GIAO DIỆN CHỌN SKIN (CHỈ HIỆN KHI BẤM NÚT SKINS)
        // ==========================================
        if (isSkinMenu) {
            g2d.setColor(new Color(0, 0, 0, 180)); 
            g2d.fillRect(0, 0, WIDTH, HEIGHT);
            
            g2d.setFont(new Font("Monospaced", Font.BOLD, 60));
            g2d.setColor(Color.WHITE);
            g2d.drawString("LỰA CHỌN NHÂN VẬT", WIDTH/2 - 300, 100);
            
            for (int i = 0; i < 4; i++) {
                Rectangle r = charSelectionRects[i];
                if (currentSkin.equals(characterColors[i])) {
                    g2d.setColor(new Color(0, 255, 255, 100)); 
                    g2d.fillRoundRect(r.x - 5, r.y - 5, r.width + 10, r.height + 10, 15, 15);
                    g2d.setColor(Color.CYAN);
                    g2d.drawRoundRect(r.x - 5, r.y - 5, r.width + 10, r.height + 10, 15, 15);
                } else {
                    g2d.setColor(new Color(255, 255, 255, 50));
                    g2d.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
                }
                if (charMenuImages[i] != null) {
                    g2d.drawImage(charMenuImages[i], r.x + 10, r.y + 10, r.width - 20, r.height - 40, null);
                }
                g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
                g2d.setColor(Color.WHITE);
                g2d.drawString(characterColors[i].toUpperCase(), r.x + 15, r.y + r.height - 10);
            }
            
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("[ NHẤP CHUỘT CHỌN NHÂN VẬT - BẤM VÀO ĐÂY ĐỂ QUAY LẠI ]", WIDTH/2 - 320, HEIGHT - 50);
        }
    }

    // ==========================================
    // XỬ LÝ CLICK CHUỘT CHO BẢNG CHỌN SKIN
    // ==========================================
    @Override
    public void mouseClicked(MouseEvent e) {
        if (isSkinMenu) {
            int mx = e.getX();
            int my = e.getY();
            
            // Kiểm tra click vào 4 ô nhân vật
            for (int i = 0; i < 4; i++) {
                if (charSelectionRects[i].contains(mx, my)) {
                    currentSkin = characterColors[i];
                    // Báo cho Main biết để update Skin cho GamePanel
                    mainFrame.updatePlayerSkin(currentSkin); 
                    repaint(); 
                    return;
                }
            }
            
            // Kiểm tra click vào nút Quay Lại (Dòng chữ màu vàng ở dưới cùng)
            if (backRect.contains(mx, my)) {
                isSkinMenu = false; // Tắt bảng Skin
                // Hiện lại 3 nút Play, Guide, Skins
                playBtn.setVisible(true);
                guideBtn.setVisible(true);
                skinsBtn.setVisible(true);
                repaint();
            }
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}