package com.game.model;

import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.List;

public class Player {
    private int x, y;
    private final int WIDTH = 45, HEIGHT = 50; 
    
    // VẬT LÝ & DI CHUYỂN
    private double velY = 0;
    private final double GRAVITY = 0.8; 
    private final double JUMP_FORCE = -17; 
    private boolean isGrounded = false;
    
    // --- QUẢN LÝ ẢNH NHÂN VẬT ĐỘNG ---
    private Image imgWalkA, imgWalkB, imgJump;
    
    // ANIMATION
    private int animTimer = 0;
    private boolean useWalkA = true;
    private String currentColor = "yellow"; // Mặc định là màu Yellow

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        // Load nhân vật mặc định ban đầu
        loadCharacterImages("yellow");
    }

    // --- HÀM TẢI ẢNH THEO MÀU SẮC ĐƯỢC CHỌN ---
    public void loadCharacterImages(String color) {
        this.currentColor = color;
        
        String pathJump = "character_" + color + "_jump.png";
        String pathWalkA = "character_" + color + "_walk_a.png";
        String pathWalkB = "character_" + color + "_walk_b.png";
        
        try {
            File fJump = new File(pathJump);
            if (fJump.exists()) imgJump = ImageIO.read(fJump);

            File fWalkA = new File(pathWalkA);
            if (fWalkA.exists()) imgWalkA = ImageIO.read(fWalkA);
            else {
                File fWalkBTmp = new File(pathWalkB);
                if (fWalkBTmp.exists()) imgWalkA = ImageIO.read(fWalkBTmp);
            }
            
            File fWalkB = new File(pathWalkB);
            if (fWalkB.exists()) imgWalkB = ImageIO.read(fWalkB);

        } catch (Exception e) {
            System.err.println("🚨 Lỗi load ảnh nhân vật màu: " + color);
        }
    }

    public void jump(double volume) {
        if (isGrounded) {
            velY = JUMP_FORCE - (volume * 0.1); 
            isGrounded = false;
        }
    }

    public void update(List<Platform> platforms) {
        velY += GRAVITY;
        if (velY > 20) velY = 20;
        y += velY;

        animTimer++;
        if (animTimer > 20) { 
            useWalkA = !useWalkA;
            animTimer = 0;
        }

        // --- XỬ LÝ VA CHẠM ---
        isGrounded = false; 
        Rectangle pHitbox = getHitbox();

        for (Platform p : platforms) {
            Rectangle pRect = new Rectangle(p.x, p.y, p.width, p.height);
            
            if (pHitbox.intersects(pRect)) {
                if (velY > 0 && playerFeetIsAbovePlatform(p.y)) {
                    y = p.y - HEIGHT + 2; 
                    velY = 0;
                    isGrounded = true;
                    return; 
                }
            }
        }
    }
    
    private boolean playerFeetIsAbovePlatform(int platformTopY) {
        int feetY = y + HEIGHT;
        return feetY < platformTopY + 25; 
    }

    public void draw(Graphics2D g2d) {
        Image currentImg;
        if (!isGrounded) {
            currentImg = imgJump;
        } else {
            currentImg = useWalkA ? imgWalkA : imgWalkB;
        }

        if (currentImg != null) {
            g2d.drawImage(currentImg, x, y, WIDTH, HEIGHT, null);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect(x, y, WIDTH, HEIGHT);
        }
    }

    public Rectangle getHitbox() {
        return new Rectangle(x + 5, y + 2, WIDTH - 10, HEIGHT - 4);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public boolean isGrounded() { return isGrounded; }
    public String getCurrentColor() { return currentColor; }
}