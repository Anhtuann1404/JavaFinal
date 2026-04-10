package com.game.model;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import javax.imageio.ImageIO;

public class Mouse {
    public int x, y, width, height;
    
    // Ảnh riêng cho từng con quái (Tùy theo Theme)
    private Image myImgA; 
    private Image myImgB; 

    private int frameCount = 0;      
    private boolean isWalkA = true;  
    private int speed = 3;       
    private int direction = -1;  

    // Mảng lưu toàn bộ ảnh: [3 Themes][2 Frames]
    private static Image[][] themeImgs = new Image[3][2];

    static {
        // Theme 0: Đồng Cỏ (Chuột)
        themeImgs[0][0] = loadImg("assets/images/objects/plain/mouse_walk_a.png");
        themeImgs[0][1] = loadImg("assets/images/objects/plain/mouse_walk_b.png");
        // Theme 1: Sa Mạc (Ví dụ: Bọ cạp)
        themeImgs[1][0] = loadImg("assets/images/objects/desert/scorpion_a.png");
        themeImgs[1][1] = loadImg("assets/images/objects/desert/scorpion_b.png");
        // Theme 2: Rừng Đêm (Ví dụ: Nhền nhện)
        themeImgs[2][0] = loadImg("assets/images/objects/forest/spider_a.png");
        themeImgs[2][1] = loadImg("assets/images/objects/forest/spider_b.png");
    }

    private static Image loadImg(String path) {
        try { return ImageIO.read(new File(path)); } catch (Exception e) { return null; }
    }

    // NÂNG CẤP: Thêm themeId vào hàm khởi tạo
    public Mouse(int x, int y, int width, int height, int themeId) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        // Lấy đúng ảnh của theme đó. Nếu chưa có file ảnh thì xài tạm ảnh Theme 0 (Chuột)
        this.myImgA = themeImgs[themeId][0] != null ? themeImgs[themeId][0] : themeImgs[0][0];
        this.myImgB = themeImgs[themeId][1] != null ? themeImgs[themeId][1] : themeImgs[0][1];
    }

    public void update(int scrollSpeed, int platX, int platWidth) {
        this.x -= scrollSpeed; 
        this.x += (speed * direction);

        int leftEdge = platX;
        int rightEdge = platX + platWidth - this.width;

        if (this.x <= leftEdge) { this.x = leftEdge; direction = 1; } 
        else if (this.x >= rightEdge) { this.x = rightEdge; direction = -1; }

        frameCount++;
        if (frameCount >= 8) { isWalkA = !isWalkA; frameCount = 0; }
    }

    public void draw(Graphics g) {
        Image currentImg = isWalkA ? myImgA : myImgB;
        if (currentImg != null) {
            if (direction == -1) g.drawImage(currentImg, x, y, width, height, null);
            else g.drawImage(currentImg, x + width, y, -width, height, null);
        } else {
            g.setColor(java.awt.Color.BLACK); g.fillRect(x, y, width, height);
        }
    }

    public Rectangle getMouseHitbox() { return new Rectangle(x + 5, y + 5, width - 10, height - 10); }
}