package com.game.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import javax.imageio.ImageIO;

public class FallingObject {
    public int x, y;
    public int width = 50, height = 50;
    
    private Image myImgA, myImgB; 
    private int fallSpeed, animationFrame = 0; 

    private static Image[][] themeImgs = new Image[3][2];
    static {
        // Theme 0: Đồng cỏ (Thiên thạch)
        themeImgs[0][0] = loadImg("assets/images/objects/plain/meteor_a.png");
        themeImgs[0][1] = loadImg("assets/images/objects/plain/meteor_b.png");
        // Theme 1: Sa mạc (Đá lăn)
        themeImgs[1][0] = loadImg("assets/images/objects/desert/rock_a.png");
        themeImgs[1][1] = loadImg("assets/images/objects/desert/rock_b.png");
        // Theme 2: Rừng đêm (Bóng lửa độc)
        themeImgs[2][0] = loadImg("assets/images/objects/forest/fireball_a.png");
        themeImgs[2][1] = loadImg("assets/images/objects/forest/fireball_b.png");
    }

    private static Image loadImg(String path) {
        try { return ImageIO.read(new File(path)); } catch (Exception e) { return null; }
    }

    public FallingObject(int x, int y, int fallSpeed, int themeId) {
        this.x = x; this.y = y; this.fallSpeed = fallSpeed;
        this.myImgA = themeImgs[themeId][0] != null ? themeImgs[themeId][0] : themeImgs[0][0];
        this.myImgB = themeImgs[themeId][1] != null ? themeImgs[themeId][1] : themeImgs[0][1];
    }

    public void update(int scrollSpeed) {
        this.x -= scrollSpeed; this.y += fallSpeed; animationFrame++; 
    }

    public void draw(Graphics g) {
        Image currentImg = ((animationFrame / 5) % 2 == 0) ? myImgA : myImgB;
        if (currentImg != null) {
            g.drawImage(currentImg, x, y, width, height, null);
        } else {
            g.setColor(Color.RED); g.fillOval(x, y + 10, width, height - 10);
        }
    }

    public Rectangle getHitbox() { return new Rectangle(x + 5, y + 15, width - 10, height - 20); }
}