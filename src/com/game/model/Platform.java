package com.game.model;

import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Platform {
    public int x, y, width, height;

    // --- HÌNH ẢNH ĐỊA HÌNH CỎ (6 tấm ảnh bạn vừa gửi) ---
    private static Image imgTopLeft, imgTopCenter, imgTopRight;
    private static Image imgBodyLeft, imgBodyCenter, imgBodyRight;

    // --- HÌNH ẢNH TRANG TRÍ ---
    private static Image imgFence, imgFenceBroken, imgSignRight;
    private Image decorImage;

    // Kích thước chuẩn mỗi khối gạch là 50x50
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

        // Ép chiều rộng chia hết cho 50 để ghép gạch luôn khít, không bị đứt nửa
        this.width = Math.max(150, (width / blockSize) * blockSize);
        this.height = height;
        this.isMoving = isMoving;

        // TẢI ẢNH ĐỊA HÌNH (Tải 1 lần duy nhất để tối ưu)
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
        } catch (Exception e) {
            System.out.println("🚨 Lỗi tải ảnh terrain! Kiểm tra lại tên 6 file ảnh mới nhé.");
        }

        Random rand = new Random();

        // CHỌN ĐỒ TRANG TRÍ NGẪU NHIÊN
        int decorChance = rand.nextInt(100);
        if (decorChance < 20) decorImage = imgFence;
        else if (decorChance < 40) decorImage = imgFenceBroken;
        else if (decorChance < 60) decorImage = imgSignRight;
        else decorImage = null;

        // ĐẶT VẬT CẢN (Canh ngay giữa bục)
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
        // Tính số hàng cần vẽ để lấp đầy phần thân xuống chạm đáy (600px là chiều cao game)
        int numRows = (600 - y) / blockSize + 1;

        // --- VẼ ĐỊA HÌNH CỎ (LÁT GẠCH THÔNG MINH) ---
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                int drawX = x + (c * blockSize);
                int drawY = y + (r * blockSize);
                Image blockImg = null;

                if (r == 0) {
                    // HÀNG 0: BỀ MẶT CỎ TRÊN CÙNG
                    if (c == 0) blockImg = imgTopLeft;                 // Góc trái
                    else if (c == numCols - 1) blockImg = imgTopRight; // Góc phải
                    else blockImg = imgTopCenter;                      // Giữa
                } else {
                    // HÀNG 1 TRỞ XUỐNG: THÂN ĐẤT LÒNG ĐẤT
                    if (c == 0) blockImg = imgBodyLeft;                 // Mép trái
                    else if (c == numCols - 1) blockImg = imgBodyRight; // Mép phải
                    else blockImg = imgBodyCenter;                      // Giữa
                }

                if (blockImg != null) {
                    g.drawImage(blockImg, drawX, drawY, blockSize, blockSize, null);
                } else {
                    // Màu nâu dự phòng nếu ảnh lỗi
                    g.setColor(new Color(139, 69, 19));
                    g.fillRect(drawX, drawY, blockSize, blockSize);
                }
            }
        }

        // --- VẼ ĐỒ TRANG TRÍ (Hàng rào, biển báo) ---
        if (decorImage != null) {
            int dSize = 50;
            g.drawImage(decorImage, x + 20, y - dSize + 5, dSize, dSize, null);
        }

        // --- VẼ VẬT CẢN VÀ XU ---
        if (mouse != null) mouse.draw(g);
        if (saw != null) saw.draw(g);
        for (Coin c : coins) c.draw(g);
    }

    public Rectangle getMouseHitbox() { return mouse != null ? mouse.getMouseHitbox() : null; }
    public Rectangle getSawHitbox() { return saw != null ? saw.getHitbox() : null; }
}