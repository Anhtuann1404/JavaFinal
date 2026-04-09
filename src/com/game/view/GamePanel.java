package com.game.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.game.model.*;
import com.game.controller.Main;
import com.game.controller.SoundManager;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 950, HEIGHT = 600;
    
    private enum State { VOICE_TEST, FADING_OUT, FADING_IN, PLAYING, GAMEOVER }
    private State currentState = State.VOICE_TEST; 
    
    // --- THÊM BIẾN MAIN ĐỂ KẾT NỐI VỚI BỘ ĐIỀU KHIỂN ---
    private Main mainFrame;
    
    // --- THÊM BIẾN ĐỂ LƯU CHỦ ĐỀ (THEME) CỦA MÀN CHƠI ---
    private int currentTheme = 0; 
    
    private Player player;
    private AudioSensor audioSensor;
    private Timer gameTimer;
    
    // --- BIẾN CHO HIỆU ỨNG CHUYỂN CẢNH ---
    private float fadeAlpha = 0f;
    private Timer fadeTimer;
    
    private Image bgPlain, bgHills, bgPiramids, bgForest; 
    private float plainX = 0, hillsX = 0, piramidsX = 0, forestX = 0;
    private Image[] cloudImages = new Image[3]; 
    private String[] cloudFileNames = { 
        "assets/images/cloud4.png", 
        "assets/images/cloud5.png", 
        "assets/images/cloud6.png" 
    }; 
    private List<Cloud> clouds = new ArrayList<>();
    
    private Image flagImgA, flagImgB;
    private int flagAnimTimer = 0;
    private boolean isFlagA = true;
    
    private List<Platform> platforms = new ArrayList<>();
    private List<Bee> bees = new ArrayList<>(); 
    private List<FallingObject> fallingObjects = new ArrayList<>(); 
    private List<Particle> particles = new ArrayList<>(); 
    
    private int score = 0;
    private int highScore = 0; 
    private int difficultyLevel = 1;
    private Random random = new Random();
    private int meteorSpawnTimer = 0; 
    private int walkSoundTimer = 0;
    private int startDelay = 0; 

    private double walkThreshold = 1.5;  
    private double jumpThreshold = 7.0; 

    private final int HUD_Y = 15;
    private final int HUD_HEIGHT = 50;

    private class Cloud {
        float x, windSpeed;
        int y, width, height;
        Image img;
        public Cloud(float x, int y, int width, int height, float windSpeed, Image img) {
            this.x = x; this.y = y; this.width = width; this.height = height;
            this.windSpeed = windSpeed; this.img = img;
        }
        public void update(int baseSpeed) {
            this.x -= (baseSpeed * 0.1f) + this.windSpeed;
            if (this.x + this.width < -50) {
                this.x = WIDTH + random.nextInt(150);
                this.y = random.nextInt(200); 
                this.img = cloudImages[random.nextInt(3)]; 
                this.width = 80 + random.nextInt(100);
                this.height = (int)(this.width * 0.6); 
this.windSpeed = 0.2f + random.nextFloat() * 0.8f; 
            }
        }
        public void draw(Graphics2D g) {
            if (img != null) g.drawImage(img, (int)x, y, width, height, null);
        }
    }

    // ==========================================
    // HÀM KHỞI TẠO ĐÃ ĐƯỢC CẬP NHẬT (THÊM MAIN)
    // ==========================================
    public GamePanel(Main mainFrame) {
        this.mainFrame = mainFrame; // Nhận kết nối từ Main
        
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this); 
        
        SoundManager.loadAllSounds();
        loadHighScore(); 
        
        try { 
            File fPlain = new File("assets/images/bg/plain/uncolored_plain.png");
            if(fPlain.exists()) bgPlain = ImageIO.read(fPlain);
            
            File fHills = new File("assets/images/bg/plain/uncolored_hills.png");
            if(fHills.exists()) bgHills = ImageIO.read(fHills);
            
            File fPiramids = new File("assets/images/bg/plain/uncolored_piramids.png");
            if(fPiramids.exists()) bgPiramids = ImageIO.read(fPiramids);
            
            File fForest = new File("assets/images/bg/plain/uncolored_forest.png");
            if(fForest.exists()) bgForest = ImageIO.read(fForest);
            
            File fFlagA = new File("assets/images/flag_blue_a.png");
            if(fFlagA.exists()) flagImgA = ImageIO.read(fFlagA);
            
            File fFlagB = new File("assets/images/flag_blue_b.png");
            if(fFlagB.exists()) flagImgB = ImageIO.read(fFlagB);
            
            for (int i = 0; i < 3; i++) {
                File fc = new File(cloudFileNames[i]);
                if (fc.exists()) cloudImages[i] = ImageIO.read(fc);
            }
        } catch (Exception e) {
            System.out.println("🚨 Lỗi tải ảnh nền trong GamePanel!");
        }

        resetGame();
        currentState = State.VOICE_TEST; 
        
        try {
            audioSensor = new AudioSensor();
            new Thread(audioSensor).start();
        } catch (Exception e) {}
        
        gameTimer = new Timer(16, this); 
        gameTimer.start();
    }

    // ==========================================
    // HÀM NHẬN CHỦ ĐỀ TỪ LEVEL PANEL
    // ==========================================
    public void setTheme(int themeId) {
        this.currentTheme = themeId;
    }

    private void loadHighScore() {
        try (BufferedReader br = new BufferedReader(new FileReader("highscore.txt"))) {
            String line = br.readLine();
            if (line != null) highScore = Integer.parseInt(line);
        } catch (Exception e) { highScore = 0; }
    }

    private void saveHighScore() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("highscore.txt"))) {
            bw.write(String.valueOf(highScore));
        } catch (Exception e) {}
    }
public void resetToVoiceTest() {
        resetGame();
        currentState = State.VOICE_TEST;
        fadeAlpha = 0f; 
        repaint();
    }

    private void resetGame() {
        score = 0; difficultyLevel = 1; meteorSpawnTimer = 0; walkSoundTimer = 0; startDelay = 40; 
        plainX = 0; hillsX = 0; piramidsX = 0; forestX = 0;
        platforms.clear(); bees.clear(); fallingObjects.clear(); particles.clear(); clouds.clear();
        
        for (int i = 0; i < 6; i++) { 
            float cx = random.nextInt(WIDTH + 200);
            int cy = random.nextInt(200);
            int cw = 80 + random.nextInt(100);
            int ch = (int)(cw * 0.6);
            float cs = 0.2f + random.nextFloat() * 0.8f;
            Image ci = cloudImages[random.nextInt(3)];
            clouds.add(new Cloud(cx, cy, cw, ch, cs, ci));
        }
        
        Platform startPlatform = new Platform(0, 480, 400, 250, false, false, false);
        platforms.add(startPlatform);

        if (player == null) player = new Player(150, 400);
        else { player.setX(150); player.setY(400); }
        
        for(int i = 0; i < 6; i++) {
            generateNextPlatform();
        }
    }

    private void startTransition() {
        if (currentState != State.VOICE_TEST) return;
        
        currentState = State.FADING_OUT;
        SoundManager.playSound("assets/sounds/jump.wav"); 
        
        // Dùng Lambda để tránh lỗi class not found
        fadeTimer = new Timer(30, e -> {
            if (currentState == State.FADING_OUT) {
                fadeAlpha += 0.05f;
                if (fadeAlpha >= 1.0f) {
                    fadeAlpha = 1.0f;
                    currentState = State.FADING_IN;
                    resetGame(); 
                    SoundManager.playBGM("assets/sounds/game_music.wav"); 
                }
            } else if (currentState == State.FADING_IN) {
                fadeAlpha -= 0.05f;
                if (fadeAlpha <= 0.0f) {
                    fadeAlpha = 0.0f;
                    fadeTimer.stop();
                    currentState = State.PLAYING;
                    startDelay = 40; 
                }
            }
            repaint();
        });
        fadeTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == State.FADING_OUT || currentState == State.FADING_IN) {
            updateParallaxBackground(1); 
            for (Cloud c : clouds) c.update(0); 
            repaint();
            return;
        }

        if (currentState == State.VOICE_TEST) {
            if (audioSensor != null && audioSensor.isCalibrated()) {
                if (audioSensor.getCurrentVolume() >= jumpThreshold) {
                    startTransition(); 
                }
            }
            updateParallaxBackground(1); 
            for (Cloud c : clouds) c.update(0); 
            repaint(); 
            return;
        }
if (currentState == State.GAMEOVER) { 
            updateParticles(); 
            repaint(); 
            return; 
        }

        int speed = 0;
        if (startDelay > 0) startDelay--;

        if (startDelay == 0 && audioSensor != null && audioSensor.isCalibrated()) {
            double vol = audioSensor.getCurrentVolume();
            if (vol >= jumpThreshold) { 
                player.jump(vol); 
                speed = 9 + (difficultyLevel / 2);
                score += 2; 
            } else if (vol >= walkThreshold) { 
                speed = 5 + (difficultyLevel / 2); 
                score += 1;
                walkSoundTimer++;
                if (walkSoundTimer >= 14 && player.isGrounded()) {
                    SoundManager.playSound("assets/sounds/walk.wav"); 
                    walkSoundTimer = 0;
                }
            } else { 
                walkSoundTimer = 14; 
            }
        }

        flagAnimTimer++;
        if (flagAnimTimer >= 10) { 
            isFlagA = !isFlagA; 
            flagAnimTimer = 0; 
        }

        difficultyLevel = (score / 1500) + 1;
        updateMeteorTimer();

        if (speed > 0) {
            updateParallaxBackground(speed);
        }

        for (Cloud c : clouds) c.update(speed);
        for (Platform p : platforms) p.update(speed);
        for (Bee b : bees) b.update(speed);
        for (FallingObject fo : fallingObjects) fo.update(speed);
        if (player != null) player.update(platforms);
        
        updateParticles();
        checkCollisions(); 

        platforms.removeIf(p -> p.x < -600);
        bees.removeIf(b -> b.x < -200);
        fallingObjects.removeIf(fo -> fo.y > HEIGHT + 100 || fo.x < -200);
        
        if (platforms.size() < 10) generateNextPlatform();
        if (player.getY() > HEIGHT) handleGameOver();
        
        repaint();
    }
    
    private void updateParallaxBackground(int baseSpeed) {
        plainX -= baseSpeed * 0.2f;        
        hillsX -= baseSpeed * 0.4f;        
        piramidsX -= baseSpeed * 0.6f;     
        forestX -= baseSpeed * 0.8f;      
        
        if (plainX <= -WIDTH) plainX += WIDTH;
        if (hillsX <= -WIDTH) hillsX += WIDTH;
        if (piramidsX <= -WIDTH) piramidsX += WIDTH;
        if (forestX <= -WIDTH) forestX += WIDTH;
    }

    private void handleGameOver() {
        SoundManager.stopBGM(); 
        SoundManager.playSound("assets/sounds/die.wav"); 
        
        int currentFinalScore = score / 10;
        if (currentFinalScore > highScore) {
            highScore = currentFinalScore;
            saveHighScore(); 
        }
        currentState = State.GAMEOVER;
    }

    private void checkCollisions() {
        Rectangle pHit = player.getHitbox();
        for (Platform p : platforms) {
            if ((p.getMouseHitbox() != null && pHit.intersects(p.getMouseHitbox())) || 
(p.getSawHitbox() != null && pHit.intersects(p.getSawHitbox()))) {
                spawnExplosion(player.getX() + 30, player.getY() + 40, Color.RED);
                handleGameOver(); 
                return;
            }
            for (Coin c : p.coins) {
                if (!c.isCollected && pHit.intersects(c.getHitbox())) { 
                    c.isCollected = true; 
                    score += 1000; 
                }
            }
        }
        for (Bee b : bees) {
            if (pHit.intersects(b.getHitbox())) { 
                spawnExplosion(player.getX() + 30, player.getY() + 40, Color.YELLOW); 
                handleGameOver(); 
                return; 
            }
        }
        for (FallingObject fo : fallingObjects) {
            if (pHit.intersects(fo.getHitbox())) { 
                spawnExplosion(player.getX() + 30, player.getY() + 40, new Color(178, 34, 34)); 
                handleGameOver(); 
                return; 
            }
        }
    }

    private void generateNextPlatform() {
        if (platforms.isEmpty()) return;
        Platform last = platforms.get(platforms.size() - 1);
     
        // =============================================
        // CẢI TIẾN 3: GIỚI HẠN GAP THEO VẬT LÝ NHẢY
        // JUMP_FORCE = 17, GRAVITY = 0.8
        // Max height ≈ 17² / (2 × 0.8) ≈ 180px
        // Max horizontal distance khi nhảy ≈ 280px (buffer an toàn)
        // → Cap gap tại 280 bất kể difficulty để luôn có thể vượt được
        // =============================================
        final int MAX_SAFE_GAP = 280;
        int gap = 160 + random.nextInt(Math.min(MAX_SAFE_GAP - 160,
                                                150 + (difficultyLevel * 10)));
     
        int nextX     = last.x + last.width + gap;
        int nextY     = Math.max(250, Math.min(520, last.y + (random.nextInt(160) - 80)));
        int nextWidth = Math.max(150, 250 - (difficultyLevel * 5)) + random.nextInt(150);
     
        int currentDisplayScore = score / 10;
        boolean canSpawnObstacles = currentDisplayScore >= 100;
     
        int beeChance    = Math.min(80, 20 + (difficultyLevel * 10));
        boolean willSpawnBee = canSpawnObstacles && (random.nextInt(100) < beeChance);
     
        boolean hasObstacle1       = false;
        boolean hasObstacle2       = false;
        boolean hasAdvancedObstacle = false;
     
        if (!willSpawnBee) {
            int obstacleChance  = Math.min(95, 30 + (difficultyLevel * 15));
            int advancedChance  = Math.min(85, 10 + (difficultyLevel * 12));
            hasObstacle1        = canSpawnObstacles && (random.nextInt(100) < obstacleChance);
            hasObstacle2        = canSpawnObstacles && (random.nextInt(100) < (obstacleChance - 25));
            hasAdvancedObstacle = canSpawnObstacles && (random.nextInt(100) < advancedChance);
        }
     
        Platform newPlatform = new Platform(nextX, nextY, nextWidth, 300,
hasObstacle1, hasObstacle2, hasAdvancedObstacle);
     
        if (currentDisplayScore < 100 || random.nextInt(100) > 20) {
            newPlatform.coins.clear();
        }
        platforms.add(newPlatform);
     
        if (canSpawnObstacles) {
            int gapObstacleChance = Math.min(90, 25 + (difficultyLevel * 10));
            if (gap > 220 && random.nextInt(100) < gapObstacleChance) {
                fallingObjects.add(new FallingObject(
                    last.x + last.width + (gap / 2) - 20, -100, 4 + random.nextInt(3)));
            }
            if (willSpawnBee) {
                bees.add(new Bee(nextX + random.nextInt(nextWidth),
                                 100 + random.nextInt(150), 120));
            }
        }
    }

    private void updateMeteorTimer() {
        int currentDisplayScore = score / 10;
        if (currentDisplayScore < 100) return;
        meteorSpawnTimer++;
        int interval = Math.max(30, 130 - (difficultyLevel * 15)); 
        if (meteorSpawnTimer >= interval) {
            int meteorDropChance = Math.min(90, 25 + (difficultyLevel * 10)); 
            if (random.nextInt(100) < meteorDropChance) {
                fallingObjects.add(new FallingObject(250 + random.nextInt(650), -50, 4 + random.nextInt(4)));
            }
            meteorSpawnTimer = 0; 
        }
    }

    private void updateParticles() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) { 
            Particle p = it.next(); 
            p.update(); 
            if (p.isDead()) it.remove(); 
        }
    }

    private void spawnExplosion(int x, int y, Color color) {
        for (int i = 0; i < 30; i++) { 
            particles.add(new Particle(x, y, color)); 
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // ==========================================
        // THAY ĐỔI MÀU NỀN DỰA THEO CHỦ ĐỀ CHỌN MÀN
        // ==========================================
        switch (currentTheme) {
            case 0: // Đồng cỏ
                g2d.setColor(new Color(193, 227, 245)); 
                break;
            case 1: // Sa mạc
                g2d.setColor(new Color(255, 200, 120)); 
                break;
            case 2: // Rừng đêm
                g2d.setColor(new Color(25, 25, 60)); 
                break;
        }
        g2d.fillRect(0, 0, getWidth(), getHeight()); 
        
        if (bgPlain != null) {
            g2d.drawImage(bgPlain, (int)plainX, 0, WIDTH, getHeight(), null);
            g2d.drawImage(bgPlain, (int)plainX + WIDTH, 0, WIDTH, getHeight(), null);
        }
        if (bgHills != null) {
            g2d.drawImage(bgHills, (int)hillsX, 0, WIDTH, getHeight(), null);
            g2d.drawImage(bgHills, (int)hillsX + WIDTH, 0, WIDTH, getHeight(), null);
        }
        if (bgPiramids != null) {
g2d.drawImage(bgPiramids, (int)piramidsX, 0, WIDTH, getHeight(), null);
            g2d.drawImage(bgPiramids, (int)piramidsX + WIDTH, 0, WIDTH, getHeight(), null);
        }
        if (bgForest != null) {
            g2d.drawImage(bgForest, (int)forestX, 0, WIDTH, getHeight(), null);
            g2d.drawImage(bgForest, (int)forestX + WIDTH, 0, WIDTH, getHeight(), null);
        }

        for (Cloud c : clouds) c.draw(g2d);

        if (currentState != State.VOICE_TEST && currentState != State.FADING_OUT) {
            for (Platform p : platforms) p.draw(g2d); 
            for (Bee b : bees) b.draw(g2d);
            for (FallingObject fo : fallingObjects) fo.draw(g2d);
            
            if (currentState == State.PLAYING && highScore > 0) {
                int flagX = player.getX() + ((highScore * 10) - score) * 5;
                if (flagX > -100 && flagX < WIDTH + 100) {
                    Image currentFlag = isFlagA ? flagImgA : flagImgB;
                    int flagY = 280; 
                    if (currentFlag != null) g2d.drawImage(currentFlag, flagX, flagY, 60, 60, null);
                    g2d.setColor(Color.WHITE); 
                    g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
                    g2d.drawString("KỶ LỤC CŨ", flagX - 12, flagY - 8); 
                }
            }

            if (player != null && currentState != State.GAMEOVER) player.draw(g2d);
            for (Particle p : particles) p.draw(g2d);
        }

        drawUI(g2d);

        if (fadeAlpha > 0f) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)); 
        }
    }

    private void drawUI(Graphics2D g2d) {
        long time = System.currentTimeMillis();

        if (currentState == State.VOICE_TEST || currentState == State.FADING_OUT) {
            g2d.setColor(new Color(0, 0, 0, 150)); 
            g2d.fillRect(0, 0, getWidth(), getHeight()); 
            
            if (audioSensor != null) {
                int v = (int) audioSensor.getCurrentVolume();
                int barWidth = Math.min(400, (int)(v * 15)); 
                int targetWidth = Math.min(400, (int)(jumpThreshold * 15)); 

                g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
                g2d.setColor(Color.WHITE);
                g2d.drawString("THỬ MIC TRƯỚC KHI VÀO:", WIDTH/2 - 135, HEIGHT/2 + 35);
                
                g2d.setColor(new Color(255, 255, 255, 60));
                g2d.fillRect(WIDTH/2 - 200, HEIGHT/2 + 50, 400, 20);
                
                if (v >= jumpThreshold) g2d.setColor(Color.RED);
                else if (v >= walkThreshold) g2d.setColor(Color.ORANGE);
                else g2d.setColor(Color.GREEN);
                
g2d.fillRect(WIDTH/2 - 200, HEIGHT/2 + 50, barWidth, 20);
                
                g2d.setColor(Color.YELLOW);
                g2d.fillRect(WIDTH/2 - 200 + targetWidth, HEIGHT/2 + 45, 4, 30);
            }

            g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
            g2d.setColor(Color.WHITE);
            if (currentState != State.FADING_OUT && time % 1000 < 600) {
                g2d.drawString(">>> HÉT VƯỢT VẠCH VÀNG ĐỂ BẮT ĐẦU <<<", WIDTH/2 - 235, HEIGHT/2 + 120);
            }
            
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("Phím [Lên/Xuống] để chỉnh ngưỡng vạch vàng", WIDTH/2 - 210, HEIGHT/2 + 160);
            
            // Vẽ thêm hướng dẫn thoát
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("[ BẤM 'ESC' ĐỂ QUAY LẠI MENU ]", WIDTH/2 - 165, HEIGHT/2 + 220);
            return;
        }

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(20, HUD_Y, WIDTH - 40, HUD_HEIGHT, 20, 20); 

        g2d.setFont(new Font("Monospaced", Font.BOLD, 22));

        String sT = " ĐIỂM: " + (score / 10);
        g2d.setColor(Color.BLACK); g2d.drawString(sT, 42, 47);
        g2d.setColor(new Color(255, 215, 0)); g2d.drawString(sT, 40, 45); 

        String hSText = "🏆 " + highScore;
        g2d.setColor(Color.BLACK); g2d.drawString(hSText, 222, 47);
        g2d.setColor(Color.CYAN); g2d.drawString(hSText, 220, 45); 

        String lT = "🔥 LVL: " + difficultyLevel;
        g2d.setColor(Color.BLACK); g2d.drawString(lT, WIDTH - 148, 47);
        g2d.setColor(new Color(255, 69, 0)); g2d.drawString(lT, WIDTH - 150, 45);

        if (audioSensor != null && (currentState == State.PLAYING || currentState == State.FADING_IN)) {
            g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
            String micText = "🎤 MIC:";
            g2d.setColor(Color.BLACK); g2d.drawString(micText, WIDTH/2 - 168, 47);
            g2d.setColor(Color.WHITE); g2d.drawString(micText, WIDTH/2 - 170, 45);

            int v = (int) audioSensor.getCurrentVolume();
            int barStartX = WIDTH/2 - 70; 

            for (int i = 0; i < 20; i++) {
                int cX = barStartX + (i * 12); 
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRect(cX, 31, 8, 14); 

                if (v >= i + 1) {
                    if (i < 8) g2d.setColor(new Color(50, 205, 50)); 
                    else if (i < 15) g2d.setColor(new Color(255, 140, 0)); 
                    else g2d.setColor(new Color(220, 20, 60)); 
                    g2d.fillRect(cX, 25, 8, 24); 
                }
            }
        }
        
        if (currentState == State.GAMEOVER) {
            g2d.setColor(new Color(0, 0, 0, 210)); 
g2d.fillRect(0, 0, getWidth(), getHeight()); 
            g2d.setFont(new Font("Monospaced", Font.BOLD, 80));
            g2d.setColor(new Color(255, 50, 50)); g2d.drawString("THẤT BẠI!", WIDTH/2 - 220, HEIGHT/2 - 60);
            
            g2d.setFont(new Font("Monospaced", Font.BOLD, 35));
            g2d.setColor(Color.WHITE);
            g2d.drawString("ĐIỂM ĐẠT ĐƯỢC: " + (score/10), WIDTH/2 - 190, HEIGHT/2 + 20);
            
            if ((score/10) >= highScore) {
                g2d.setColor(Color.YELLOW); g2d.drawString("🎉 KỶ LỤC MỚI XÁC LẬP! 🎉", WIDTH/2 - 230, HEIGHT/2 + 70);
            } else {
                g2d.setColor(Color.CYAN); g2d.drawString("KỶ LỤC CŨ: " + highScore, WIDTH/2 - 150, HEIGHT/2 + 70);
            }
            
            g2d.setFont(new Font("Monospaced", Font.BOLD, 22)); g2d.setColor(Color.WHITE); time = System.currentTimeMillis(); 
            if (time % 1000 < 700) g2d.drawString("[ NHẤN SPACE ĐỂ CHƠI LẠI ]", WIDTH/2 - 175, HEIGHT/2 + 130);
            
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("[ BẤM 'ESC' ĐỂ QUAY LẠI MENU ]", WIDTH/2 - 165, HEIGHT/2 + 180);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) { 
        int key = e.getKeyCode();
        
        // --- CHỨC NĂNG THOÁT RA MENU BẰNG NÚT ESCAPE ---
        if (key == KeyEvent.VK_ESCAPE && (currentState == State.VOICE_TEST || currentState == State.GAMEOVER)) {
            mainFrame.showMenu();
        }
        
        if (currentState == State.GAMEOVER && key == KeyEvent.VK_SPACE) {
            resetGame(); 
            currentState = State.PLAYING; 
            startDelay = 40; 
            SoundManager.playBGM("assets/sounds/game_music.wav"); 
        }
        
        if (currentState == State.VOICE_TEST && e.getKeyCode() == KeyEvent.VK_UP) {
            jumpThreshold = Math.max(walkThreshold + 1, jumpThreshold - 0.5);
        }
        if (currentState == State.VOICE_TEST && e.getKeyCode() == KeyEvent.VK_DOWN) {
            jumpThreshold += 0.5;
        }
    }
    
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public void setPlayerSkin(String color) {
        if (player != null) {
            player.loadCharacterImages(color);
        }
    }
}