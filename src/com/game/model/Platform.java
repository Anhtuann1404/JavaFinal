package com.game.model;

import java.awt.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Platform {
    public int x, y, width, height;
    private static final int BLOCK_SIZE = 60;
    private static final int SCREEN_HEIGHT = 600;

    // Biến lưu lại ID của Map hiện tại để hàm draw() biết đường vẽ
    private int currentThemeId;

    // --- MẢNG LƯU TRỮ GẠCH THEO THEME [3 Themes][6 Vị trí] ---
    private static Image[][] tileImgs = new Image[3][6];

    // --- MẢNG LƯU TRỮ HÀNG RÀO THEO THEME [3 Themes] ---
    private static Image[] fenceImgs = new Image[3];

    // --- DANH SÁCH LƯU TRỮ ĐỒ TRANG TRÍ THEO THEME ---
    private static List<Image> plainDecors = new ArrayList<>();
    private static List<Image> desertDecors = new ArrayList<>();
    private static List<Image> forestDecors = new ArrayList<>();
    
    private static List<Image> plainGrass = new ArrayList<>();
    private static List<Image> desertGrass = new ArrayList<>(); 
    private static List<Image> forestGrass = new ArrayList<>(); 

    private static final Random rand = new Random();

    // KHỐI NẠP ẢNH TĨNH (Chỉ nạp 1 lần khi game chạy)
    static {
        try {
            // 1. THEME 0: ĐỒNG CỎ
            tileImgs[0][0] = loadImg("assets/images/terrain/plain/terrain_grass_block_top_left.png");
            tileImgs[0][1] = loadImg("assets/images/terrain/plain/terrain_grass_block_top.png");
            tileImgs[0][2] = loadImg("assets/images/terrain/plain/terrain_grass_block_top_right.png");
            tileImgs[0][3] = loadImg("assets/images/terrain/plain/terrain_grass_block_left.png");
            tileImgs[0][4] = loadImg("assets/images/terrain/plain/terrain_grass_block_center.png");
            tileImgs[0][5] = loadImg("assets/images/terrain/plain/terrain_grass_block_right.png");

            fenceImgs[0] = loadImg("assets/images/terrain/plain/fence.png");

            addIfExist(plainDecors, "assets/images/terrain/plain/sign_right.png");
            addIfExist(plainDecors, "assets/images/terrain/plain/tree5.png");
            addIfExist(plainGrass, "assets/images/terrain/plain/grass5.png");
            addIfExist(plainGrass, "assets/images/terrain/plain/grass2.png");
            addIfExist(plainGrass, "assets/images/terrain/plain/grass.png");

            // 2. THEME 1: SA MẠC
            tileImgs[1][0] = loadImg("assets/images/terrain/desert/terrain_sand_block_top_left.png");
            tileImgs[1][1] = loadImg("assets/images/terrain/desert/terrain_sand_block_top.png");
            tileImgs[1][2] = loadImg("assets/images/terrain/desert/terrain_sand_block_top_right.png");
            tileImgs[1][3] = loadImg("assets/images/terrain/desert/terrain_sand_block_left.png");
            tileImgs[1][4] = loadImg("assets/images/terrain/desert/terrain_sand_block_center.png");
            tileImgs[1][5] = loadImg("assets/images/terrain/desert/terrain_sand_block_right.png");

            fenceImgs[1] = loadImg("assets/images/terrain/desert/fence_broken.png");

            addIfExist(desertDecors, "assets/images/terrain/desert/cactus.png");
            // Gợi ý: Nếu có cỏ khô sa mạc thì thêm dòng dưới
             addIfExist(desertGrass, "assets/images/terrain/desert/dry_grass.png"); 

            // 3. THEME 2: RỪNG ĐÊM
            tileImgs[2][0] = loadImg("assets/images/terrain/forest/terrain_wood_block_top_left.png");
            tileImgs[2][1] = loadImg("assets/images/terrain/forest/terrain_wood_block_top.png");
            tileImgs[2][2] = loadImg("assets/images/terrain/forest/terrain_wood_block_top_right.png");
            tileImgs[2][3] = loadImg("assets/images/terrain/forest/terrain_wood_block_left.png");
            tileImgs[2][4] = loadImg("assets/images/terrain/forest/terrain_wood_block_center.png");
            tileImgs[2][5] = loadImg("assets/images/terrain/forest/terrain_wood_block_right.png");

            // Gợi ý Hàng rào và cây cối rừng đêm:
            fenceImgs[2] = loadImg("assets/images/terrain/forest/fence_wood (2).png");
             addIfExist(forestDecors, "assets/images/terrain/forest/mushroom.png");
             addIfExist(forestGrass, "assets/images/terrain/forest/dark_grass.png");

            // --- LOGIC "CHỐNG MÙ" (Dùng Tạm Ảnh) ---
            for(int t = 1; t <= 2; t++) {
                // Nếu thiếu gạch, mượn gạch Đồng Cỏ
                for(int i = 0; i < 6; i++) {
                    if(tileImgs[t][i] == null) tileImgs[t][i] = tileImgs[0][i];
                }
                // Nếu thiếu hàng rào, mượn hàng rào Đồng Cỏ
                if(fenceImgs[t] == null) fenceImgs[t] = fenceImgs[0];
            }

        } catch (Exception e) {
            System.out.println("🚨 Lỗi tải ảnh trong Platform: " + e.getMessage());
        }
    }

    private static Image loadImg(String path) {
        try { return ImageIO.read(new File(path)); } catch (Exception e) { return null; }
    }

    private static void addIfExist(List<Image> list, String path) {
        try {
            File f = new File(path);
            if (f.exists()) list.add(ImageIO.read(f));
        } catch (Exception e) {}
    }

    // Các thành phần chướng ngại vật
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

    // CONSTRUCTOR
    public Platform(int x, int y, int width, int height,
                    boolean hasMouse, boolean hasSaw, boolean hasAdvancedObstacle, int themeId) {
        this.x      = x;
        this.y      = y;
        this.width  = Math.max(150, (width / BLOCK_SIZE) * BLOCK_SIZE);
        this.height = height;
        
        // Lưu Theme ID để hàm Draw biết
        this.currentThemeId = themeId;

        List<Rectangle> occupiedSpaces = new ArrayList<>();
        int decorPadding = 15;

        // --- BỘ LỌC TÀI NGUYÊN THEO THEME ---
        List<Image> currentDecors = plainDecors;
        List<Image> currentGrass = plainGrass;
        Image currentFence = fenceImgs[0];
        
        if (themeId == 1) { 
            currentDecors = desertDecors.isEmpty() ? plainDecors : desertDecors; 
            currentGrass = desertGrass.isEmpty() ? plainGrass : desertGrass;
            currentFence = fenceImgs[1];
        } else if (themeId == 2) { 
            currentDecors = forestDecors.isEmpty() ? plainDecors : forestDecors;
            currentGrass = forestGrass.isEmpty() ? plainGrass : forestGrass;
            currentFence = fenceImgs[2];
        }

        // 1. SINH HÀNG RÀO THEO THEME
        if (currentFence != null && rand.nextInt(100) < 60) {
            int rx = 10 + rand.nextInt(20);
            int fw = 50, fh = 40;
            fenceList.add(new Decoration(currentFence, rx, -35, fw, fh));
            occupiedSpaces.add(new Rectangle(rx - decorPadding, -35, fw + decorPadding * 2, fh));
        }

        // 2. SINH CÂY CỐI/XƯƠNG RỒNG THEO THEME
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

        // 3. SINH CỎ THEO THEME
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

        // Sinh Chướng ngại vật và Xu
        int obsX = this.x + (this.width / 2) - 20;
        if (hasMouse) {
            this.mouse = new Mouse(obsX, y - 30, 40, 30, themeId); // Chuột cũng tự đổi theo theme
        } else if (hasSaw) {
            this.saw = new Saw(obsX, y - 45, 45, 45, themeId);     // Lưỡi cưa cũng tự đổi
        } else if (hasAdvancedObstacle) {
            this.saw = new Saw(this.x + this.width - 70, y - 45, 45, 45, themeId);
        }

        if (!hasMouse && !hasSaw && !hasAdvancedObstacle && this.width >= 150) {
            int numCoins   = rand.nextInt(3) + 1;
            int spacing    = 45;
            int startCoinX = this.x + (this.width / 2) - ((numCoins * spacing) / 2);
            for (int i = 0; i < numCoins; i++) {
                coins.add(new Coin(startCoinX + (i * spacing), y - 150, themeId)); // Tiền cũng đổi theo theme
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

        // VẼ GẠCH ĐÚNG THEO THEME
        Image[] currentTiles = tileImgs[currentThemeId];

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                int drawX = x + (c * BLOCK_SIZE);
                int drawY = y + (r * BLOCK_SIZE);
                
                Image blockImg = currentTiles[4]; // Mặc định khối thân giữa
                
                if (r == 0) {
                    if (c == 0) blockImg = currentTiles[0];
                    else if (c == numCols - 1) blockImg = currentTiles[2];
                    else blockImg = currentTiles[1];
                } else {
                    if (c == 0) blockImg = currentTiles[3];
                    else if (c == numCols - 1) blockImg = currentTiles[5];
                }
                
                if (blockImg != null) g2d.drawImage(blockImg, drawX, drawY, BLOCK_SIZE, BLOCK_SIZE, null);
            }
        }

        // Vẽ đồ trang trí
        for (Decoration d : fenceList)     g2d.drawImage(d.img, x + d.relX, y + d.relY, d.w, d.h, null);
        for (Decoration d : tallDecorList) g2d.drawImage(d.img, x + d.relX, y + d.relY, d.w, d.h, null);
        for (Decoration d : grassList)     g2d.drawImage(d.img, x + d.relX, y + d.relY, d.w, d.h, null);

        // Vẽ chướng ngại vật
        if (mouse != null) mouse.draw(g2d);
        if (saw   != null) saw.draw(g2d);
        for (Coin c : coins) c.draw(g2d);
    }

    public Rectangle getMouseHitbox() { return mouse != null ? mouse.getMouseHitbox() : null; }
    public Rectangle getSawHitbox()   { return saw   != null ? saw.getHitbox()        : null; }
}