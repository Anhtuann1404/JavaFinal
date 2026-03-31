package com.game.model;

import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Platform {
    public int x, y, width, height;

    // --- HÌNH ẢNH ĐỊA HÌNH CỎ ---
    private static Image imgTopLeft, imgTopCenter, imgTopRight;
    private static Image imgBodyLeft, imgBodyCenter, imgBodyRight;

    // --- HÌNH ẢNH TRANG TRÍ (CŨ + 4 ẢNH MỚI) ---
    private static Image imgFence, imgFenceBroken, imgSignRight;
    private static Image imgTree34, imgTree27, imgGrass4, imgGrass2;
    
    private Image decorImage;
    
    // Kích thước động để Cây cao hơn, Cỏ thấp hơn
    private int decorW = 50, decorH = 50, decorOffsetY = 0;

    private int blockSize = 50; 

    // --- LOGIC DI CHUYỂN ---
    public boolean isMoving;
    private int startY, moveDir = 1, moveSpeed = 2;

    // --- VẬT CẢN VÀ VÀNG ---
    public Mouse mouse;
    public Saw saw;
    public List<Coin> coins = new ArrayList<>();

    public Platform(int x, int y, int width, int height, boolean hasMouse, boolean hasSaw, boolean isMoving) {
        this.x = x;
        this.y = y;
        this.startY = y;

        this.width = Math.max(150, (width / blockSize) * blockSize);
        this.height = height;
        this.isMoving = isMoving;

        // TẢI ẢNH ĐỊA HÌNH VÀ TRANG TRÍ
        try {
            if (imgTopLeft == null) imgTopLeft = ImageIO.read(new File("terrain_grass_block_top_left.png"));
            if (imgTopCenter == null) imgTopCenter = ImageIO.read(new File("terrain_grass_block_top.png"));
            if (imgTopRight == null) imgTopRight = ImageIO.read(new File("terrain_grass_block_top_right.png"));
            
            if (imgBodyLeft == null) imgBodyLeft = ImageIO.read(new File("terrain_grass_block_left.png"));
            if (imgBodyCenter == null) imgBodyCenter = ImageIO.read(new File("terrain_grass_block_center.png"));
            if (imgBodyRight == null) imgBodyRight = ImageIO.read(new File("terrain_grass_block_right.png"));
            
            if (imgFence == null) imgFence = ImageIO.read(new File("fence.png"));
            if (imgFenceBroken == null) imgFenceBroken = ImageIO.read(new File("fence_broken.png"));
            if (imgSignRight == null) imgSignRight = ImageIO.read(new File("sign_right.png"));
            
            // --- TẢI 4 ẢNH TRANG TRÍ MỚI ---
            if (imgTree34 == null) imgTree34 = ImageIO.read(new File("tree34.png"));
            if (imgTree27 == null) imgTree27 = ImageIO.read(new File("tree27.png"));
            if (imgGrass4 == null) imgGrass4 = ImageIO.read(new File("grass4.png"));
            if (imgGrass2 == null) imgGrass2 = ImageIO.read(new File("grass2.png"));
            
        } catch (Exception e) {
            System.out.println("🚨 Lỗi tải ảnh terrain hoặc decor! Kiểm tra lại tên file.");
        }

        Random rand = new Random();

        // --- CHỌN ĐỒ TRANG TRÍ NGẪU NHIÊN KÈM KÍCH THƯỚC ---
        int decorChance = rand.nextInt(100);
        if (decorChance < 70) { // 70% bục sẽ mọc cây/cỏ/hàng rào
            int type = rand.nextInt(7); // Random 1 trong 7 món đồ
            switch (type) {
                case 0: decorImage = imgFence;       decorW = 50; decorH = 50; decorOffsetY = 5; break;
                case 1: decorImage = imgFenceBroken; decorW = 50; decorH = 50; decorOffsetY = 5; break;
                case 2: decorImage = imgSignRight;   decorW = 50; decorH = 50; decorOffsetY = 5; break;
                case 3: decorImage = imgTree34;      decorW = 70; decorH = 100; decorOffsetY = 5; break; // Cây to
                case 4: decorImage = imgTree27;      decorW = 40; decorH = 110; decorOffsetY = 5; break; // Cây cao ốm
                case 5: decorImage = imgGrass2;      decorW = 30; decorH = 30; decorOffsetY = 2; break;  // Bụi cỏ nhỏ
                case 6: decorImage = imgGrass4;      decorW = 40; decorH = 35; decorOffsetY = 2; break;  // Bụi cỏ to
            }
        } else {
            decorImage = null; // 30% bục trống trải
        }

        // ĐẶT VẬT CẢN (Canh giữa bục)
        int obsX = this.x + (this.width / 2) - 22;
        if (hasMouse) {
            this.mouse = new Mouse(obsX, y - 25, 40, 30);
        } else if (hasSaw) {
            this.saw = new Saw(obsX, y - 35, 45, 45);
        }

        // SINH VÀNG TRÊN CAO
        if (!hasMouse && !hasSaw && this.width >= 150) {
            int numCoins = rand.nextInt(3) + 1;
            int spacing = 45;
            int startCoinX = this.x + (this.width / 2) - ((numCoins * spacing) / 2);
            for (int i = 0; i < numCoins; i++) {
                coins.add(new Coin(startCoinX + (i * spacing), y - 150));
            }
        }
    }

    public void update(int scrollSpeed) {
        this.x -= scrollSpeed;
        if (isMoving) {
            this.y += (moveDir * moveSpeed);
            if (Math.abs(this.y - startY) > 80) moveDir *= -1;
        }
        if (mouse != null) {
            mouse.y = this.y - mouse.height + 5;
            mouse.update(scrollSpeed, this.x, this.width);
        }
        if (saw != null) {
            saw.y = this.y - saw.height + 10;
            saw.update(scrollSpeed);
        }
        for (Coin c : coins) {
            c.update(scrollSpeed, this.y - 150);
        }
    }

    public void draw(Graphics g) {
        int numCols = width / blockSize;
        int numRows = (600 - y) / blockSize + 1;

        // --- VẼ ĐỊA HÌNH CỎ (LÁT GẠCH) ---
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                int drawX = x + (c * blockSize);
                int drawY = y + (r * blockSize);
                Image blockImg = null;

                if (r == 0) {
                    if (c == 0) blockImg = imgTopLeft;                 
                    else if (c == numCols - 1) blockImg = imgTopRight; 
                    else blockImg = imgTopCenter;                      
                } else {
                    if (c == 0) blockImg = imgBodyLeft;                 
                    else if (c == numCols - 1) blockImg = imgBodyRight; 
                    else blockImg = imgBodyCenter;                      
                }

                if (blockImg != null) {
                    g.drawImage(blockImg, drawX, drawY, blockSize, blockSize, null);
                } else {
                    g.setColor(new Color(139, 69, 19));
                    g.fillRect(drawX, drawY, blockSize, blockSize);
                }
            }
        }

        // --- VẼ ĐỒ TRANG TRÍ LÊN BỤC ---
        if (decorImage != null) {
            // Canh lề lệch sang trái một chút để chừa chỗ cho vật cản ở giữa bục
            int dx = x + 15; 
            int dy = y - decorH + decorOffsetY; // Trừ đi chiều cao (decorH) để mọc cắm xuống đất
            g.drawImage(decorImage, dx, dy, decorW, decorH, null);
        }

        // --- VẼ VẬT CẢN VÀ XU ---
        if (mouse != null) mouse.draw(g);
        if (saw != null) saw.draw(g);
        for (Coin c : coins) c.draw(g);
    }

    public Rectangle getMouseHitbox() { return mouse != null ? mouse.getMouseHitbox() : null; }
    public Rectangle getSawHitbox() { return saw != null ? saw.getHitbox() : null; }
}