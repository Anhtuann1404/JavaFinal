package com.game.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import javax.imageio.ImageIO;

public class Bee {
    public int x, y;
    public final int width = 45;
    public final int height = 40;
    
    private Image myWingUp, myWingDown; 
    private boolean movingLeft = true;
    
    private int startX, patrolRange, moveSpeed = 3, animationFrame = 0; 

    private static Image[][] themeImgs = new Image[3][2];
    static {
        // Theme 0: Đồng Cỏ (Ong)
        themeImgs[0][0] = loadImg("assets/images/objects/plain/bee_a.png");
        themeImgs[0][1] = loadImg("assets/images/objects/plain/bee_b.png");
        // Theme 1: Sa Mạc (Dơi sa mạc)
        themeImgs[1][0] = loadImg("assets/images/objects/desert/bat_a.png");
        themeImgs[1][1] = loadImg("assets/images/objects/desert/bat_b.png");
        // Theme 2: Rừng Đêm (Bóng ma)
        themeImgs[2][0] = loadImg("assets/images/objects/forest/ghost_a.png");
        themeImgs[2][1] = loadImg("assets/images/objects/forest/ghost_b.png");
    }

    private static Image loadImg(String path) {
        try { return ImageIO.read(new File(path)); } catch (Exception e) { return null; }
    }

    // NÂNG CẤP: Thêm themeId vào hàm khởi tạo
    public Bee(int x, int y, int patrolRange, int themeId) {
        this.x = x; this.y = y; this.startX = x; this.patrolRange = patrolRange;
        this.myWingUp = themeImgs[themeId][0] != null ? themeImgs[themeId][0] : themeImgs[0][0];
        this.myWingDown = themeImgs[themeId][1] != null ? themeImgs[themeId][1] : themeImgs[0][1];
    }

    public void update(int scrollSpeed) {
        this.startX -= scrollSpeed; this.x -= scrollSpeed;
        if (movingLeft) { this.x -= moveSpeed; if (this.x < startX - patrolRange) movingLeft = false; } 
        else { this.x += moveSpeed; if (this.x > startX + patrolRange) movingLeft = true; }
        animationFrame++; 
    }

    public void draw(Graphics g) {
        int hoverOffset = (int)(Math.sin(animationFrame * 0.2) * 5); 
        int drawY = y + hoverOffset;

        if (myWingUp != null && myWingDown != null) {
            Image currentImg = ((animationFrame / 8) % 2 == 0) ? myWingUp : myWingDown;
            if (movingLeft) g.drawImage(currentImg, x, drawY, width, height, null);
            else g.drawImage(currentImg, x + width, drawY, -width, height, null);
        } else {
            g.setColor(Color.YELLOW); g.fillOval(x, drawY, width, height);
        }
    }

    public Rectangle getHitbox() { return new Rectangle(x + 5, y + 5, width - 10, height - 10); }
}