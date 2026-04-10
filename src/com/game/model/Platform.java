package com.game.model;

import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Platform {
    public int x, y, width, height;
    private static final int BLOCK_SIZE = 50;
    private static final int SCREEN_HEIGHT = 600;

    // --- HÌNH ẢNH ĐỊA HÌNH ---
    private static Image imgTopLeft, imgTopCenter, imgTopRight;
    private static Image imgBodyLeft, imgBodyCenter, imgBodyRight;

    // --- MẢNG LƯU TRỮ ĐỒ TRANG TRÍ THEO THEME ---
    private static List<Image> plainDecors = new ArrayList<>();
    private static List<Image> desertDecors = new ArrayList<>();
    private static List<Image> forestDecors = new ArrayList<>();
    
    private static List<Image> plainGrass = new ArrayList<>();
    private static List<Image> desertGrass = new ArrayList<>(); 
    private static List<Image> forestGrass = new ArrayList<>(); 

    private static Image imgFence, imgFenceBroken;
    private static final Random rand = new Random();

    static {
        try {
            imgTopLeft   = ImageIO.read(new File("assets/images/terrain/plain/terrain_grass_block_top_left.png"));
            imgTopCenter = ImageIO.read(new File("assets/images/terrain/plain/terrain_grass_block_top.png"));
            imgTopRight  = ImageIO.read(new File("assets/images/terrain/plain/terrain_grass_block_top_right.png"));
            imgBodyLeft  = ImageIO.read(new File("assets/images/terrain/plain/terrain_grass_block_left.png"));
            imgBodyCenter= ImageIO.read(new File("assets/images/terrain/plain/terrain_grass_block_center.png"));
            imgBodyRight = ImageIO.read(new File("assets/images/terrain/plain/terrain_grass_block_right.png"));

            imgFence      = ImageIO.read(new File("assets/images/terrain/plain/fence.png"));
            imgFenceBroken= ImageIO.read(new File("assets/images/terrain/desert/fence_broken.png"));

            addIfExist(plainDecors, "assets/images/terrain/plain/tree34.png");
            addIfExist(plainDecors, "assets/images/terrain/plain/tree27.png");
            addIfExist(plainGrass, "assets/images/terrain/plain/grass4.png");
            addIfExist(plainGrass, "assets/images/terrain/plain/grass2.png");
            addIfExist(plainGrass, "assets/images/terrain/plain/grass.png");

            addIfExist(desertDecors, "assets/images/terrain/desert/cactus.png");
        } catch (Exception e) {
            System.out.println("🚨 Lỗi tải ảnh trong Platform: " + e.getMessage());
        }
    }

    private static void addIfExist(List<Image> list, String path) {
        try {
            File f = new File(path);
            if (f.exists()) list.add(ImageIO.read(f));
        } catch (Exception e) {}
    }

    public Mouse mouse;
    public Saw saw;
    public List<Coin> coins = new ArrayList<>();

    public static class Decoration {
        Image img;
        int relX, relY, w, h;
        public Decoration(Image img, int relX, int relY, int w, int h) {
            this.img = img; this.relX = relX; this.relY = relY; this.w = w; this.h = h;
        }
    }

    public List<Decoration> grassList     = new ArrayList<>();
    public List<Decoration> tallDecorList = new ArrayList<>();
    public List<Decoration> fenceList     = new ArrayList<>();

    // ĐÃ THÊM BIẾN themeId VÀO HÀM KHỞI TẠO Ở ĐÂY ĐỂ TRÁNH LỖI UNDEFINED
    public Platform(int x, int y, int width, int height,
                    boolean hasMouse, boolean hasSaw, boolean hasAdvancedObstacle, int themeId) {
        this.x      = x;
        this.y      = y;
        this.width  = Math.max(150, (width / BLOCK_SIZE) * BLOCK_SIZE);
        this.height = height;

        List<Rectangle> occupiedSpaces = new ArrayList<>();
        int decorPadding = 15;

        // CHỌN BỘ ẢNH THEO THEME
        List<Image> currentDecors = plainDecors;
        List<Image> currentGrass = plainGrass;
        
        if (themeId == 1) { 
            currentDecors = desertDecors.isEmpty() ? plainDecors : desertDecors; 
            currentGrass = desertGrass.isEmpty() ? plainGrass : desertGrass;
        } else if (themeId == 2) { 
            currentDecors = forestDecors.isEmpty() ? plainDecors : forestDecors;
            currentGrass = forestGrass.isEmpty() ? plainGrass : forestGrass;
        }

        // SINH HÀNG RÀO
        if (imgFence != null && rand.nextInt(100) < 60) {
            Image fImg = (themeId == 1) ? imgFenceBroken : (rand.nextBoolean() ? imgFence : imgFenceBroken);
            if (fImg == null) fImg = imgFence;
            
            int rx = 10 + rand.nextInt(20);
            int fw = 50, fh = 40;
            fenceList.add(new Decoration(fImg, rx, -35, fw, fh));
            occupiedSpaces.add(new Rectangle(rx - decorPadding, -35, fw + decorPadding * 2, fh));
        }

        // SINH ĐỒ TRANG TRÍ CAO
        if (!currentDecors.isEmpty()) {
            int numTallDecors = 1 + rand.nextInt(2);
            for (int i = 0; i < numTallDecors; i++) {
                Image decorImg = currentDecors.get(rand.nextInt(currentDecors.size()));
                int tW = 40 + rand.nextInt(30); 
                int tH = 70 + rand.nextInt(30);

                for (int attempt = 0; attempt < 5; attempt++) {
                    int rx = rand.nextInt(Math.max(1, this.width - tW));
                    Rectangle proposed = new Rectangle(rx - decorPadding, -tH, tW + decorPadding * 2, tH);

                    boolean collides = false;
                    for (Rectangle occ : occupiedSpaces) {
                        if (proposed.intersects(occ)) { collides = true; break; }
                    }

                    if (!collides) {
                        tallDecorList.add(new Decoration(decorImg, rx, -tH + 10, tW, tH));
                        occupiedSpaces.add(proposed);
                        break;
                    }
                }
            }
        }

        // SINH CỎ
        if (!currentGrass.isEmpty()) {
            int numGrass = 1 + rand.nextInt(3);
            for (int i = 0; i < numGrass; i++) {
                Image grassImg = currentGrass.get(rand.nextInt(currentGrass.size()));
                int gW = 25 + rand.nextInt(15);
                int gH = (int)(gW * 0.8);

                for (int attempt = 0; attempt < 5; attempt++) {
                    int rx = rand.nextInt(Math.max(1, this.width - gW));
                    Rectangle proposed = new Rectangle(rx - decorPadding / 2, -gH, gW + decorPadding, gH);

                    boolean collides = false;
                    for (Rectangle occ : occupiedSpaces) {
                        if (proposed.intersects(occ)) { collides = true; break; }
                    }

                    if (!collides) {
                        grassList.add(new Decoration(grassImg, rx, -gH + 5, gW, gH));
                        occupiedSpaces.add(proposed);
                        break;
                    }
                }
            }
        }

        int obsX = this.x + (this.width / 2) - 20;
        if (hasMouse) {
            this.mouse = new Mouse(obsX, y - 30, 40, 30, themeId);
        } else if (hasSaw) {
            this.saw = new Saw(obsX, y - 45, 45, 45, themeId);
        } else if (hasAdvancedObstacle) {
            this.saw = new Saw(this.x + this.width - 70, y - 45, 45, 45, themeId);
        }

        if (!hasMouse && !hasSaw && !hasAdvancedObstacle && this.width >= 150) {
            int numCoins   = rand.nextInt(3) + 1;
            int spacing    = 45;
            int startCoinX = this.x + (this.width / 2) - ((numCoins * spacing) / 2);
            for (int i = 0; i < numCoins; i++) {
                coins.add(new Coin(startCoinX + (i * spacing), y - 150, themeId));
            }
        }
    }

    public void update(int scrollSpeed) {
        this.x -= scrollSpeed;
        if (mouse != null) { mouse.y = this.y - mouse.height + 5; mouse.update(scrollSpeed, this.x, this.width); }
        if (saw != null) { saw.y = this.y - saw.height + 10; saw.update(scrollSpeed); }
        for (Coin c : coins) c.update(scrollSpeed, this.y - 150);
    }

    public void draw(Graphics2D g2d) {
        int numCols = width / BLOCK_SIZE;
        int numRows = (SCREEN_HEIGHT - y) / BLOCK_SIZE + 1;

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                int drawX = x + (c * BLOCK_SIZE);
                int drawY = y + (r * BLOCK_SIZE);
                Image blockImg = imgBodyCenter;
                if (r == 0) {
                    if (c == 0) blockImg = imgTopLeft;
                    else if (c == numCols - 1) blockImg = imgTopRight;
                    else blockImg = imgTopCenter;
                } else {
                    if (c == 0) blockImg = imgBodyLeft;
                    else if (c == numCols - 1) blockImg = imgBodyRight;
                }
                if (blockImg != null) g2d.drawImage(blockImg, drawX, drawY, BLOCK_SIZE, BLOCK_SIZE, null);
            }
        }

        for (Decoration d : fenceList)     g2d.drawImage(d.img, x + d.relX, y + d.relY, d.w, d.h, null);
        for (Decoration d : tallDecorList) g2d.drawImage(d.img, x + d.relX, y + d.relY, d.w, d.h, null);
        for (Decoration d : grassList)     g2d.drawImage(d.img, x + d.relX, y + d.relY, d.w, d.h, null);

        if (mouse != null) mouse.draw(g2d);
        if (saw   != null) saw.draw(g2d);
        for (Coin c : coins) c.draw(g2d);
    }

    public Rectangle getMouseHitbox() { return mouse != null ? mouse.getMouseHitbox() : null; }
    public Rectangle getSawHitbox()   { return saw   != null ? saw.getHitbox()        : null; }
}