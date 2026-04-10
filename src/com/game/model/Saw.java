package com.game.model;

import java.awt.Color;
import java.awt.Graphics2D; // Dùng Graphics2D để xoay ảnh
import java.awt.Image;
import java.awt.Rectangle;
import java.io.File;
import javax.imageio.ImageIO;

public class Saw {
    public int x, y, width, height;
    private Image myImg;
    private int angle = 0; // Biến góc xoay

    private static Image[] themeImgs = new Image[3];
    static {
        themeImgs[0] = loadImg("assets/images/objects/plain/saw.png"); // Lưỡi cưa
        themeImgs[1] = loadImg("assets/images/objects/desert/cactus_trap.png"); // Bẫy xương rồng
        themeImgs[2] = loadImg("assets/images/objects/forest/spike_trap.png"); // Bẫy gai
    }

    private static Image loadImg(String path) {
        try { return ImageIO.read(new File(path)); } catch (Exception e) { return null; }
    }

    public Saw(int x, int y, int width, int height, int themeId) {
        this.x = x; this.y = y; this.width = width; this.height = height;
        this.myImg = themeImgs[themeId] != null ? themeImgs[themeId] : themeImgs[0];
    }

    public void update(int scrollSpeed) {
        this.x -= scrollSpeed;
        this.angle -= 15; // Mỗi frame xoay 15 độ
        if (this.angle <= -360) this.angle = 0;
    }

    public void draw(Graphics2D g2d) {
        if (myImg != null) {
            // Logic xoay tròn ảnh mượt mà quanh tâm
            g2d.rotate(Math.toRadians(angle), x + width / 2, y + height / 2);
            g2d.drawImage(myImg, x, y, width, height, null);
            g2d.rotate(-Math.toRadians(angle), x + width / 2, y + height / 2); // Xoay lại để không ảnh hưởng ảnh khác
        } else {
            g2d.setColor(Color.GRAY); g2d.fillOval(x, y, width, height);
        }
    }

    public Rectangle getHitbox() { return new Rectangle(x + 5, y + 5, width - 10, height - 10); }
}