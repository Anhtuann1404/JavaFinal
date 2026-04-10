package com.game.model;

import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;

public class Coin {
    public int x, y;
    public final int width = 30, height = 30;
    
    private Image myImgA, myImgB;
    private int frameCount = 0;
    private boolean isFrameA = true;
    public boolean isCollected = false;

    private static Image[][] themeImgs = new Image[3][2];
    static {
        // Có thể custom xu vàng/xu bạc/ngọc đỏ theo theme tùy ý
        themeImgs[0][0] = loadImg("assets/images/objects/plain/coin_a.png");
        themeImgs[0][1] = loadImg("assets/images/objects/plain/coin_b.png");
        themeImgs[1][0] = loadImg("assets/images/objects/desert/coin_a.png");
        themeImgs[1][1] = loadImg("assets/images/objects/desert/coin_b.png");
        themeImgs[2][0] = loadImg("assets/images/objects/forest/coin_a.png");
        themeImgs[2][1] = loadImg("assets/images/objects/forest/coin_b.png");
    }

    private static Image loadImg(String path) {
        try { return ImageIO.read(new File(path)); } catch (Exception e) { return null; }
    }

    public Coin(int x, int y, int themeId) {
        this.x = x; this.y = y;
        this.myImgA = themeImgs[themeId][0] != null ? themeImgs[themeId][0] : themeImgs[0][0];
        this.myImgB = themeImgs[themeId][1] != null ? themeImgs[themeId][1] : themeImgs[0][1];
    }

    public void update(int scrollSpeed, int platformY) {
        this.x -= scrollSpeed;
        this.y = platformY - height - 15;
        frameCount++;
        if (frameCount >= 12) { isFrameA = !isFrameA; frameCount = 0; }
    }

    public void draw(Graphics g) {
        if (isCollected) return;
        Image current = isFrameA ? myImgA : myImgB;
        if (current != null) g.drawImage(current, x, y, width, height, null);
        else { g.setColor(Color.YELLOW); g.fillOval(x, y, width, height); }
    }

    public Rectangle getHitbox() { return new Rectangle(x, y, width, height); }
}