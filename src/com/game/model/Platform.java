package com.game.model;

import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Platform {
    public int x, y, width, height;
    private int blockSize = 50; 

    // --- HÌNH ẢNH ĐỊA HÌNH CỎ (LÁT GẠCH) ---
    private static Image imgTopLeft, imgTopCenter, imgTopRight;
    private static Image imgBodyLeft, imgBodyCenter, imgBodyRight;

    // --- HÌNH ẢNH TRANG TRÍ (GỘP CẢ CŨ VÀ MỚI) ---
    private static Image imgTree34, imgTree27, imgCactus;
    private static Image imgGrass4, imgGrass2, imgNewGrass;
    private static Image imgFence, imgFenceBroken;
    
    // --- CHƯỚNG NGẠI VẬT & COIN ---
    public Mouse mouse;
    public Saw saw;
    public List<Coin> coins = new ArrayList<>();

    // Lớp nội bộ để quản lý đồ trang trí
    public static class Decoration {
        Image img;
        int relX, relY, w, h;
        Rectangle hitBox; 

        public Decoration(Image img, int relX, int relY, int w, int h) {
            this.img = img; 
            this.relX = relX; 
            this.relY = relY; 
            this.w = w; 
            this.h = h;
            this.hitBox = new Rectangle(relX, relY, w, h);
        }
    }
    
    // Danh sách đồ trang trí hiển thị
    public List<Decoration> grassList = new ArrayList<>();
    public List<Decoration> tallDecorList = new ArrayList<>(); 
    public List<Decoration> fenceList = new ArrayList<>();

    public Platform(int x, int y, int width, int height, boolean hasMouse, boolean hasSaw, boolean hasAdvancedObstacle) {
        this.x = x;
        this.y = y;
        this.width = Math.max(150, (width / blockSize) * blockSize);
        this.height = height;

        // --- NẠP ẢNH ---
        loadImages();

        Random rand = new Random();

        // --- DANH SÁCH TẠM ĐỂ KIỂM TRA VA CHẠM ĐỒ TRANG TRÍ ---
        List<Rectangle> occupiedSpaces = new ArrayList<>();
        int decorPadding = 15; 

        // ==========================================
        // --- SINH ĐỒ TRANG TRÍ ---
        // ==========================================
        
        // 1. Sinh Hàng rào 
        if (imgFence != null && rand.nextInt(100) < 60) { 
            Image fImg = rand.nextBoolean() ? imgFence : imgFenceBroken;
            int rx = 10 + rand.nextInt(20); 
            int fw = 50, fh = 40;
            
            Decoration fence = new Decoration(fImg, rx, -35, fw, fh);
            fenceList.add(fence);
            occupiedSpaces.add(new Rectangle(rx - decorPadding, -35, fw + decorPadding * 2, fh));
        }

        // 2. Sinh Đồ trang trí cao (Trộn ngẫu nhiên Cây cũ và Xương rồng mới)
        int numTallDecors = 1 + rand.nextInt(2); 
        for (int i = 0; i < numTallDecors; i++) {
            Image decorImg = null;
            int tW = 0, tH = 0;
            
            // Random chọn loại cây (0: Cây 34, 1: Cây 27, 2: Xương rồng)
            int type = rand.nextInt(3);
            if (type == 0 && imgTree34 != null) {
                decorImg = imgTree34;
                tW = 60 + rand.nextInt(15);
                tH = 80 + rand.nextInt(20);
            } else if (type == 1 && imgTree27 != null) {
                decorImg = imgTree27;
                tW = 40 + rand.nextInt(10);
                tH = 90 + rand.nextInt(20);
            } else if (type == 2 && imgCactus != null) {
                decorImg = imgCactus;
                tW = 40 + rand.nextInt(15);
                tH = 65 + rand.nextInt(20);
            }
            
            if (decorImg == null) continue;

            for (int attempt = 0; attempt < 5; attempt++) {
                int rx = rand.nextInt(Math.max(1, this.width - tW));
                Rectangle proposedSpace = new Rectangle(rx - decorPadding, -tH, tW + decorPadding * 2, tH);
                
                boolean collides = false;
                for (Rectangle occupied : occupiedSpaces) {
                    if (proposedSpace.intersects(occupied)) {
                        collides = true;
                        break;
                    }
                }
                
                if (!collides) {
                    // Căn chỉnh y một chút để vật thể cắm xuống đất tự nhiên hơn
                    int yOffset = (decorImg == imgCactus) ? 12 : 10; 
                    tallDecorList.add(new Decoration(decorImg, rx, -tH + yOffset, tW, tH));
                    occupiedSpaces.add(proposedSpace);
                    break; 
                }
            }
        }

        // 3. Sinh Cỏ (Trộn ngẫu nhiên 2 loại cỏ cũ và 1 loại cỏ mới)
        int numGrass = 1 + rand.nextInt(3); 
        for (int i = 0; i < numGrass; i++) {
            Image grassImg = null;
            int gW = 0, gH = 0;

            int type = rand.nextInt(3);
            if (type == 0 && imgGrass4 != null) {
                grassImg = imgGrass4;
                gW = 25 + rand.nextInt(10);
                gH = (int)(gW * 0.8);
            } else if (type == 1 && imgGrass2 != null) {
                grassImg = imgGrass2;
                gW = 25 + rand.nextInt(10);
                gH = (int)(gW * 0.8);
            } else if (type == 2 && imgNewGrass != null) {
                grassImg = imgNewGrass;
                gW = 35 + rand.nextInt(10);
                gH = (int)(gW * 0.9);
            }

            if (grassImg == null) continue;

            for (int attempt = 0; attempt < 5; attempt++) {
                int rx = rand.nextInt(Math.max(1, this.width - gW));
                Rectangle proposedSpace = new Rectangle(rx - decorPadding/2, -gH, gW + decorPadding, gH);
                
                boolean collides = false;
                for (Rectangle occupied : occupiedSpaces) {
                    if (proposedSpace.intersects(occupied)) {
                        collides = true;
                        break;
                    }
                }
                
                if (!collides) {
                    int yOffset = (grassImg == imgNewGrass) ? 8 : 5;
                    grassList.add(new Decoration(grassImg, rx, -gH + yOffset, gW, gH));
                    occupiedSpaces.add(proposedSpace);
                    break;
                }
            }
        }

        // --- SINH CHƯỚNG NGẠI VẬT TRÊN BỤC ---
        int obsX = this.x + (this.width / 2) - 20;
        if (hasMouse) {
            this.mouse = new Mouse(obsX, y - 30, 40, 30);
        } else if (hasSaw) {
            this.saw = new Saw(obsX, y - 45, 45, 45);
        } else if (hasAdvancedObstacle) { 
            this.saw = new Saw(this.x + this.width - 70, y - 45, 45, 45);
        }

        // --- SINH VÀNG ---
        if (!hasMouse && !hasSaw && this.width >= 150) {
            int numCoins = rand.nextInt(3) + 1;
            int spacing = 45;
            int startCoinX = this.x + (this.width / 2) - ((numCoins * spacing) / 2);
            for (int i = 0; i < numCoins; i++) {
                coins.add(new Coin(startCoinX + (i * spacing), y - 150));
            }
        }
    }

    // --- HÀM NẠP ẢNH ĐÃ SỬA ĐƯỜNG DẪN ---
    private static void loadImages() {
        try {
            if (imgTopLeft == null) imgTopLeft = ImageIO.read(new File("assets/images/terrain_grass_block_top_left.png"));
            if (imgTopCenter == null) imgTopCenter = ImageIO.read(new File("assets/images/terrain_grass_block_top.png"));
            if (imgTopRight == null) imgTopRight = ImageIO.read(new File("assets/images/terrain_grass_block_top_right.png"));
            if (imgBodyLeft == null) imgBodyLeft = ImageIO.read(new File("assets/images/terrain_grass_block_left.png"));
            if (imgBodyCenter == null) imgBodyCenter = ImageIO.read(new File("assets/images/terrain_grass_block_center.png"));
            if (imgBodyRight == null) imgBodyRight = ImageIO.read(new File("assets/images/terrain_grass_block_right.png"));
            
            if (imgTree34 == null) imgTree34 = ImageIO.read(new File("assets/images/tree34.png"));
            if (imgTree27 == null) imgTree27 = ImageIO.read(new File("assets/images/tree27.png"));
            if (imgGrass4 == null) imgGrass4 = ImageIO.read(new File("assets/images/grass4.png"));
            if (imgGrass2 == null) imgGrass2 = ImageIO.read(new File("assets/images/grass2.png"));
            
            if (imgCactus == null) imgCactus = ImageIO.read(new File("assets/images/cactus.png"));
            if (imgNewGrass == null) imgNewGrass = ImageIO.read(new File("assets/images/grass.png"));
            
            if (imgFence == null) imgFence = ImageIO.read(new File("assets/images/fence.png"));
            if (imgFenceBroken == null) imgFenceBroken = ImageIO.read(new File("assets/images/fence_broken.png"));
        } catch (Exception e) {
            System.out.println("🚨 Lỗi tải ảnh trong Platform: " + e.getMessage());
        }
    }

    public void update(int scrollSpeed) {
        this.x -= scrollSpeed;
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

    public void draw(Graphics2D g2d) {
        int numCols = width / blockSize;
        int numRows = (600 - y) / blockSize + 1;

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
                if (blockImg != null) g2d.drawImage(blockImg, drawX, drawY, blockSize, blockSize, null);
            }
        }

        for (Decoration d : fenceList) g2d.drawImage(d.img, x + d.relX, y + d.relY, d.w, d.h, null);
        for (Decoration d : tallDecorList) g2d.drawImage(d.img, x + d.relX, y + d.relY, d.w, d.h, null);
        for (Decoration d : grassList) g2d.drawImage(d.img, x + d.relX, y + d.relY, d.w, d.h, null);

        if (mouse != null) mouse.draw(g2d);
        if (saw != null) saw.draw(g2d);
        for (Coin c : coins) c.draw(g2d);
    }

    public Rectangle getMouseHitbox() { return mouse != null ? mouse.getMouseHitbox() : null; }
    public Rectangle getSawHitbox() { return saw != null ? saw.getHitbox() : null; }
}