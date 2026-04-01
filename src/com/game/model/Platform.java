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

    // --- HÌNH ẢNH TRANG TRÍ ---
    private static Image imgTree34, imgTree27, imgGrass4, imgGrass2;
    private static Image imgFence, imgFenceBroken;
    
    // --- CHƯỚNG NGẠI VẬT & COIN ---
    public Mouse mouse;
    public Saw saw;
    public List<Coin> coins = new ArrayList<>();

    // Lớp nội bộ để quản lý đồ trang trí
    public static class Decoration {
        Image img;
        int relX, relY, w, h;
        Rectangle hitBox; // Dùng để kiểm tra va chạm lúc sinh đồ

        public Decoration(Image img, int relX, int relY, int w, int h) {
            this.img = img; 
            this.relX = relX; 
            this.relY = relY; 
            this.w = w; 
            this.h = h;
            // Tạo hitbox tương đối để kiểm tra chồng lấp
            this.hitBox = new Rectangle(relX, relY, w, h);
        }
    }
    
    // Danh sách đồ trang trí hiển thị
    public List<Decoration> grassList = new ArrayList<>();
    public List<Decoration> treeList = new ArrayList<>();
    public List<Decoration> fenceList = new ArrayList<>();

    public Platform(int x, int y, int width, int height, boolean hasMouse, boolean hasSaw, boolean hasAdvancedObstacle) {
        this.x = x;
        this.y = y;
        // Đảm bảo width chia hết cho blockSize
        this.width = Math.max(150, (width / blockSize) * blockSize);
        this.height = height;

        // --- NẠP ẢNH (Chỉ nạp 1 lần nhờ static) ---
        loadImages();

        Random rand = new Random();

        // --- DANH SÁCH TẠM ĐỂ KIỂM TRA VA CHẠM ĐỒ TRANG TRÍ ---
        List<Rectangle> occupiedSpaces = new ArrayList<>();
        int decorPadding = 15; // Khoảng cách tối thiểu giữa các món đồ (pixel)

        // ==========================================
        // --- SINH ĐỒ TRANG TRÍ (MỚI: KHÔNG CHỒNG HÌNH) ---
        // ==========================================
        
        // 1. Sinh Hàng rào (Ưu tiên sinh trước vì nó nằm sau cây/cỏ)
        if (imgFence != null && rand.nextInt(100) < 60) { // 60% có hàng rào
            Image fImg = rand.nextBoolean() ? imgFence : imgFenceBroken;
            // Hàng rào thường đặt cố định gần mép trái bục cho đẹp
            int rx = 10 + rand.nextInt(20); 
            int fw = 50, fh = 40;
            
            Decoration fence = new Decoration(fImg, rx, -35, fw, fh);
            fenceList.add(fence);
            
            // Đánh dấu vùng hàng rào đã chiếm (cộng thêm padding)
            occupiedSpaces.add(new Rectangle(rx - decorPadding, -35, fw + decorPadding * 2, fh));
        }

        // 2. Sinh Cây (1 hoặc 2 cây)
        int numTrees = 1 + rand.nextInt(2); 
        for (int i = 0; i < numTrees; i++) {
            Image treeImg = rand.nextBoolean() ? imgTree34 : imgTree27;
            if (treeImg == null) continue;

            int tW = treeImg == imgTree34 ? (60 + rand.nextInt(15)) : (40 + rand.nextInt(10));
            int tH = treeImg == imgTree34 ? (80 + rand.nextInt(20)) : (90 + rand.nextInt(20));
            
            // Thử tìm chỗ trống để đặt cây (tối đa 5 lần thử)
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
                    // Chỗ trống! Đặt cây và đánh dấu vùng
                    treeList.add(new Decoration(treeImg, rx, -tH + 10, tW, tH));
                    occupiedSpaces.add(proposedSpace);
                    break; // Thoát vòng lặp thử
                }
            }
        }

        // 3. Sinh Cỏ (lác đác 1 đến 2 bụi)
        int numGrass = 1 + rand.nextInt(2);
        for (int i = 0; i < numGrass; i++) {
            Image grassImg = rand.nextBoolean() ? imgGrass4 : imgGrass2;
            if (grassImg == null) continue;

            int gW = 25 + rand.nextInt(10);
            int gH = (int)(gW * 0.8);

            // Thử tìm chỗ trống để đặt cỏ (tối đa 5 lần thử)
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
                    grassList.add(new Decoration(grassImg, rx, -gH + 5, gW, gH));
                    occupiedSpaces.add(proposedSpace);
                    break;
                }
            }
        }

        // --- SINH CHƯỚNG NGẠI VẬT TRÊN BỤC (Giữ nguyên) ---
        int obsX = this.x + (this.width / 2) - 20;
        if (hasMouse) {
            this.mouse = new Mouse(obsX, y - 30, 40, 30);
        } else if (hasSaw) {
            this.saw = new Saw(obsX, y - 45, 45, 45);
        } else if (hasAdvancedObstacle) { 
            this.saw = new Saw(this.x + this.width - 70, y - 45, 45, 45);
        }

        // --- SINH VÀNG (Giữ nguyên) ---
        if (!hasMouse && !hasSaw && this.width >= 150) {
            int numCoins = rand.nextInt(3) + 1;
            int spacing = 45;
            int startCoinX = this.x + (this.width / 2) - ((numCoins * spacing) / 2);
            for (int i = 0; i < numCoins; i++) {
                coins.add(new Coin(startCoinX + (i * spacing), y - 150));
            }
        }
    }

    // --- HÀM NẠP ẢNH STATIC ---
    private static void loadImages() {
        try {
            if (imgTopLeft == null) imgTopLeft = ImageIO.read(new File("terrain_grass_block_top_left.png"));
            if (imgTopCenter == null) imgTopCenter = ImageIO.read(new File("terrain_grass_block_top.png"));
            if (imgTopRight == null) imgTopRight = ImageIO.read(new File("terrain_grass_block_top_right.png"));
            if (imgBodyLeft == null) imgBodyLeft = ImageIO.read(new File("terrain_grass_block_left.png"));
            if (imgBodyCenter == null) imgBodyCenter = ImageIO.read(new File("terrain_grass_block_center.png"));
            if (imgBodyRight == null) imgBodyRight = ImageIO.read(new File("terrain_grass_block_right.png"));
            if (imgTree34 == null) imgTree34 = ImageIO.read(new File("tree34.png"));
            if (imgTree27 == null) imgTree27 = ImageIO.read(new File("tree27.png"));
            if (imgGrass4 == null) imgGrass4 = ImageIO.read(new File("grass4.png"));
            if (imgGrass2 == null) imgGrass2 = ImageIO.read(new File("grass2.png"));
            if (imgFence == null) imgFence = ImageIO.read(new File("fence.png"));
            if (imgFenceBroken == null) imgFenceBroken = ImageIO.read(new File("fence_broken.png"));
        } catch (Exception e) {
            System.out.println("🚨 Lỗi tải ảnh trong Platform!");
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

        // --- 1. VẼ ĐỊA HÌNH ---
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

        // --- 2. VẼ TRANG TRÍ (MỚI: Thứ tự vẽ để tạo chiều sâu) ---
        // Vẽ Hàng rào trước (nằm sau cây/cỏ)
        for (Decoration d : fenceList) g2d.drawImage(d.img, x + d.relX, y + d.relY, d.w, d.h, null);
        // Vẽ Cây
        for (Decoration d : treeList) g2d.drawImage(d.img, x + d.relX, y + d.relY, d.w, d.h, null);
        // Vẽ Cỏ (nằm trên cùng)
        for (Decoration d : grassList) g2d.drawImage(d.img, x + d.relX, y + d.relY, d.w, d.h, null);

        // --- 3. VẼ CHƯỚNG NGẠI VẬT VÀ XU ---
        if (mouse != null) mouse.draw(g2d);
        if (saw != null) saw.draw(g2d);
        for (Coin c : coins) c.draw(g2d);
    }

    public Rectangle getMouseHitbox() { return mouse != null ? mouse.getMouseHitbox() : null; }
    public Rectangle getSawHitbox() { return saw != null ? saw.getHitbox() : null; }
}