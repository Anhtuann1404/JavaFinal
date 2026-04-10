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
    
    private Main mainFrame;
    private int currentTheme = 0; 
    
    // --- HỆ THỐNG PARALLAX ĐỘNG ---
    private List<List<Image>> bgLayers = new ArrayList<>(); 
    private float[] layerX = new float[15]; 
    private float[] parallaxSpeeds = {
        0.05f, 0.1f, 0.2f, 0.35f, 0.5f, 0.65f, 0.8f, 0.9f, 0.95f, 1.0f, 1.1f, 1.2f
    }; 
    
    private Player player;
    private AudioSensor audioSensor;
    private Timer gameTimer;
    
    private float fadeAlpha = 0f;
    private Timer fadeTimer;
    
    // --- HỆ THỐNG MÂY BAY XUYÊN MAP (Persistent Clouds) ---
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
    
    // --- HỆ THỐNG BẪY (3 KHỐI: 0=Base, 1=Wave High, 2=Wave Low) ---
    private Image[][] hazardImgs = new Image[3][3]; 
    private List<GapHazard> gapHazards = new ArrayList<>(); 
    private int hazardAnimTimer = 0; 
    
    private int score = 0, highScore = 0, difficultyLevel = 1;
    private Random random = new Random();
    private int meteorSpawnTimer = 0, walkSoundTimer = 0, startDelay = 0; 

    private double walkThreshold = 1.5, jumpThreshold = 7.0; 
    private final int HUD_Y = 15, HUD_HEIGHT = 50;

    // --- CLASS BẪY THUNG LŨNG ---
    private class GapHazard {
        int x, y, width, height;
        public GapHazard(int x, int y, int width) {
            this.x = x; this.y = y; this.width = width;
            this.height = HEIGHT - y;
        }
        public void update(int speed) { this.x -= speed; }
        public void draw(Graphics2D g) {
            Shape oldClip = g.getClip();
            // Cắt khung vẽ để nước/lava không tràn ra ngoài khe hở
            g.setClip(x, y, width, height); 
            
            int tileW = 50; 
            int cols = (width / tileW) + 2; 
            int waveFrame = ((hazardAnimTimer / 15) % 2 == 0) ? 1 : 2;

            for (int i = 0; i < cols; i++) {
                int drawX = x + (i * tileW);
                // Vẽ phần thân nước/lava ở dưới
                Image baseImg = hazardImgs[currentTheme][0]; 
                if (baseImg != null) g.drawImage(baseImg, drawX, y + 20, tileW, height, null);
                // Vẽ phần gợn sóng nhấp nhô ở trên
                Image waveImg = hazardImgs[currentTheme][waveFrame]; 
                if (waveImg != null) g.drawImage(waveImg, drawX, y, tileW, 40, null);
            }
            g.setClip(oldClip); 
        }
    }

    // --- CLASS MÂY ---
    private class Cloud {
        float x, windSpeed;
        int y, width, height;
        Image img;
        public Cloud(float x, int y, int width, int height, float windSpeed, Image img) {
            this.x = x; this.y = y; this.width = width; this.height = height;
            this.windSpeed = windSpeed; this.img = img;
        }
        public void update(int scrollSpeed) {
            this.x -= (scrollSpeed * 0.1f) + this.windSpeed;
            if (this.x + this.width < -100) {
                this.x = WIDTH + 100;
                this.y = random.nextInt(250);
                this.img = cloudImages[random.nextInt(3)];
            }
        }
        public void draw(Graphics2D g) {
            if (img != null) g.drawImage(img, (int)x, y, width, height, null);
        }
    }

    public GamePanel(Main mainFrame) {
        this.mainFrame = mainFrame; 
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(this); 
        
        SoundManager.loadAllSounds();
        loadHighScore(); 
        
        try { 
            String[] folderNames = {"plain", "desert", "forest"};
            // Nạp Background Parallax
            for (int t = 0; t < 3; t++) {
                List<Image> themeLayers = new ArrayList<>();
                int layerIndex = 0; 
                while (true) { 
                    File fBg = new File("assets/images/bg/" + folderNames[t] + "/layer" + layerIndex + ".png");
                    if (fBg.exists()) { themeLayers.add(ImageIO.read(fBg)); layerIndex++; } 
                    else break; 
                }
                bgLayers.add(themeLayers); 
            }
            
            // Nạp Bẫy (Base, High, Low)
            for(int t=0; t<3; t++) {
                String f = folderNames[t];
                // Bạn đặt tên file theo cấu trúc này nhé
                hazardImgs[t][0] = loadImg("assets/images/objects/" + f + "/hazard_base.png");
                hazardImgs[t][1] = loadImg("assets/images/objects/" + f + "/hazard_wave_high.png");
                hazardImgs[t][2] = loadImg("assets/images/objects/" + f + "/hazard_wave_low.png");
            }

            // Nạp Mây
            for (int i = 0; i < 3; i++) {
                File fc = new File(cloudFileNames[i]);
                if (fc.exists()) cloudImages[i] = ImageIO.read(fc);
            }

            File fFlagA = new File("assets/images/flag_blue_a.png");
            if(fFlagA.exists()) flagImgA = ImageIO.read(fFlagA);
            File fFlagB = new File("assets/images/flag_blue_b.png");
            if(fFlagB.exists()) flagImgB = ImageIO.read(fFlagB);

        } catch (Exception e) { e.printStackTrace(); }

        resetGame();
        currentState = State.VOICE_TEST; 
        
        try {
            audioSensor = new AudioSensor();
            new Thread(audioSensor).start();
        } catch (Exception e) {}
        
        gameTimer = new Timer(16, this); 
        gameTimer.start();
    }
    
    private Image loadImg(String path) {
        try { return ImageIO.read(new File(path)); } catch (Exception e) { return null; }
    }

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
        for(int i = 0; i < layerX.length; i++) layerX[i] = 0;
        
        // KHÔNG clear clouds để mây bay liên tục
        platforms.clear(); bees.clear(); fallingObjects.clear(); particles.clear(); gapHazards.clear();
        
        if (clouds.isEmpty()) {
            for (int i = 0; i < 6; i++) { 
                clouds.add(new Cloud(random.nextInt(WIDTH), random.nextInt(250), 
                           80 + random.nextInt(70), 50, 0.2f + random.nextFloat() * 0.8f, 
                           cloudImages[random.nextInt(3)]));
            }
        }
        
        Platform startPlatform = new Platform(0, 480, 400, 250, false, false, false, currentTheme);
        platforms.add(startPlatform);

        if (player == null) player = new Player(150, 400);
        else { player.setX(150); player.setY(400); }
        
        for(int i = 0; i < 6; i++) generateNextPlatform();
    }

    private void startTransition() {
        if (currentState != State.VOICE_TEST) return;
        currentState = State.FADING_OUT;
        SoundManager.playSound("assets/sounds/jump.wav"); 
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
        int speed = 0;
        
        if (currentState == State.PLAYING) {
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
                    if (++walkSoundTimer >= 14 && player.isGrounded()) {
                        SoundManager.playSound("assets/sounds/walk.wav"); 
                        walkSoundTimer = 0;
                    }
                } else walkSoundTimer = 14;
            }
        }

        // Cập nhật mây (luôn chạy)
        for (Cloud c : clouds) c.update(speed);

        if (currentState == State.FADING_OUT || currentState == State.FADING_IN || currentState == State.VOICE_TEST) {
            updateParallaxBackground(1); 
            repaint();
            return;
        }

        if (currentState == State.GAMEOVER) { updateParticles(); repaint(); return; }

        hazardAnimTimer++;
        flagAnimTimer++;
        if (flagAnimTimer >= 10) { isFlagA = !isFlagA; flagAnimTimer = 0; }

        difficultyLevel = (score / 1500) + 1;
        updateMeteorTimer();

        if (speed > 0) updateParallaxBackground(speed); 

        for (Platform p : platforms) p.update(speed);
        for (GapHazard h : gapHazards) h.update(speed); 
        for (Bee b : bees) b.update(speed);
        for (FallingObject fo : fallingObjects) fo.update(speed);
        if (player != null) player.update(platforms);
        
        updateParticles();
        checkCollisions(); 

        platforms.removeIf(p -> p.x < -600);
        gapHazards.removeIf(h -> h.x + h.width < -100);
        bees.removeIf(b -> b.x < -200);
        fallingObjects.removeIf(fo -> fo.y > HEIGHT + 100 || fo.x < -200);
        
        if (platforms.size() < 10) generateNextPlatform();
        if (player.getY() > HEIGHT) handleGameOver(); 
        
        repaint();
    }
    
    private void updateParallaxBackground(int baseSpeed) {
        if (currentTheme >= bgLayers.size()) return; 
        int numberOfLayers = bgLayers.get(currentTheme).size(); 
        for (int i = 0; i < numberOfLayers; i++) {
            int speedIndex = Math.min(i, parallaxSpeeds.length - 1); 
            layerX[i] -= baseSpeed * parallaxSpeeds[speedIndex]; 
            if (layerX[i] <= -WIDTH) layerX[i] += WIDTH; 
        }
    }

    private void handleGameOver() {
        SoundManager.stopBGM(); 
        SoundManager.playSound("assets/sounds/die.wav"); 
        int currentFinalScore = score / 10;
        if (currentFinalScore > highScore) { highScore = currentFinalScore; saveHighScore(); }
        currentState = State.GAMEOVER;
    }

    private void checkCollisions() {
        Rectangle pHit = player.getHitbox();
        for (Platform p : platforms) {
            if ((p.getMouseHitbox() != null && pHit.intersects(p.getMouseHitbox())) || 
                (p.getSawHitbox() != null && pHit.intersects(p.getSawHitbox()))) {
                spawnExplosion(player.getX() + 30, player.getY() + 40, Color.RED);
                handleGameOver(); return;
            }
            for (Coin c : p.coins) if (!c.isCollected && pHit.intersects(c.getHitbox())) { c.isCollected = true; score += 1000; }
        }
        for (Bee b : bees) if (pHit.intersects(b.getHitbox())) { spawnExplosion(player.getX()+30, player.getY()+40, Color.YELLOW); handleGameOver(); return; }
        for (FallingObject fo : fallingObjects) if (pHit.intersects(fo.getHitbox())) { spawnExplosion(player.getX()+30, player.getY()+40, new Color(178, 34, 34)); handleGameOver(); return; }
    }

    private void generateNextPlatform() {
        if (platforms.isEmpty()) return;
        Platform last = platforms.get(platforms.size() - 1);
        int gap = 160 + random.nextInt(Math.min(300, 150 + (difficultyLevel * 10)));
        gapHazards.add(new GapHazard(last.x + last.width, 560, gap));

        int nextX = last.x + last.width + gap;
        int nextY = Math.max(250, Math.min(520, last.y + (random.nextInt(160) - 80)));
        int nextWidth = Math.max(150, 250 - (difficultyLevel * 5)) + random.nextInt(150);
        
        boolean canSpawn = (score / 10) >= 100;
        Platform newPlatform = new Platform(nextX, nextY, nextWidth, 300, canSpawn && random.nextInt(100) < 40, canSpawn && random.nextInt(100) < 30, false, currentTheme);
        platforms.add(newPlatform);
        
        if (canSpawn) {
            if (gap > 220 && random.nextInt(100) < 30) fallingObjects.add(new FallingObject(last.x + last.width + (gap/2), -100, 5, currentTheme));
            if (random.nextInt(100) < 20) bees.add(new Bee(nextX, 150, 120, currentTheme));
        }
    }

    private void updateMeteorTimer() {
        if ((score / 10) < 100) return;
        if (++meteorSpawnTimer >= Math.max(30, 130 - (difficultyLevel * 15))) {
            fallingObjects.add(new FallingObject(250 + random.nextInt(600), -50, 4, currentTheme));
            meteorSpawnTimer = 0; 
        }
    }

    private void updateParticles() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) { Particle p = it.next(); p.update(); if (p.isDead()) it.remove(); }
    }

    private void spawnExplosion(int x, int y, Color color) { for (int i = 0; i < 30; i++) particles.add(new Particle(x, y, color)); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Vẽ nền trời theo theme
        switch (currentTheme) {
            case 0: g2d.setColor(new Color(193, 227, 245)); break; 
            case 1: g2d.setColor(new Color(255, 200, 120)); break; 
            case 2: g2d.setColor(new Color(25, 25, 60)); break;    
        }
        g2d.fillRect(0, 0, getWidth(), getHeight()); 
        
        // Vẽ Parallax
        if (currentTheme < bgLayers.size()) {
            List<Image> layers = bgLayers.get(currentTheme);
            for (int i = 0; i < layers.size(); i++) {
                g2d.drawImage(layers.get(i), (int)layerX[i], 0, WIDTH, getHeight(), null);
                g2d.drawImage(layers.get(i), (int)layerX[i] + WIDTH, 0, WIDTH, getHeight(), null);
            }
        }

        // Vẽ mây
        for (Cloud c : clouds) c.draw(g2d);

        if (currentState != State.VOICE_TEST && currentState != State.FADING_OUT) {
            for (GapHazard h : gapHazards) h.draw(g2d); 
            for (Platform p : platforms) p.draw(g2d); 
            for (Bee b : bees) b.draw(g2d);
            for (FallingObject fo : fallingObjects) fo.draw(g2d);
            
            if (currentState == State.PLAYING && highScore > 0) {
                int flagX = player.getX() + ((highScore * 10) - score) * 5;
                if (flagX > -100 && flagX < WIDTH + 100) {
                    g2d.drawImage(isFlagA ? flagImgA : flagImgB, flagX, 280, 60, 60, null);
                }
            }
            if (player != null && currentState != State.GAMEOVER) player.draw(g2d);
            for (Particle p : particles) p.draw(g2d);
        }

        drawUI(g2d);

        if (fadeAlpha > 0f) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
            g2d.setColor(Color.BLACK); g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)); 
        }
    }

    private void drawUI(Graphics2D g2d) {
        long time = System.currentTimeMillis();
        if (currentState == State.VOICE_TEST || currentState == State.FADING_OUT) {
            g2d.setColor(new Color(0, 0, 0, 180)); g2d.fillRect(0, 0, getWidth(), getHeight()); 
            if (audioSensor != null) {
                int v = (int) audioSensor.getCurrentVolume();
                g2d.setFont(new Font("Monospaced", Font.BOLD, 22)); g2d.setColor(Color.WHITE);
                g2d.drawString("THỬ MIC TRƯỚC KHI VÀO:", WIDTH/2 - 140, HEIGHT/2 + 20);
                g2d.fillRect(WIDTH/2 - 200, HEIGHT/2 + 50, 400, 25);
                g2d.setColor(v >= jumpThreshold ? Color.RED : Color.GREEN);
                g2d.fillRect(WIDTH/2 - 200, HEIGHT/2 + 50, Math.min(400, v*15), 25);
            }
            return;
        }

        g2d.setColor(new Color(0, 0, 0, 150)); g2d.fillRoundRect(20, HUD_Y, WIDTH - 40, HUD_HEIGHT, 15, 15); 
        g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2d.setColor(Color.YELLOW); g2d.drawString("ĐIỂM: " + (score / 10), 40, 45); 
        g2d.setColor(Color.CYAN); g2d.drawString("🏆 KỶ LỤC: " + highScore, 220, 45); 

        if (currentState == State.GAMEOVER) {
            g2d.setColor(new Color(0, 0, 0, 200)); g2d.fillRect(0, 0, getWidth(), getHeight()); 
            g2d.setFont(new Font("Monospaced", Font.BOLD, 70)); g2d.setColor(Color.RED);
            g2d.drawString("GAME OVER", WIDTH/2 - 180, HEIGHT/2 - 50);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 30)); g2d.setColor(Color.WHITE);
            if (time % 1000 < 700) g2d.drawString("[ SPACE ĐỂ CHƠI LẠI ]", WIDTH/2 - 170, HEIGHT/2 + 50);
        }
    }

    @Override public void keyPressed(KeyEvent e) { 
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) mainFrame.showMenu();
        if (currentState == State.GAMEOVER && e.getKeyCode() == KeyEvent.VK_SPACE) { resetGame(); currentState = State.PLAYING; }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    public void setPlayerSkin(String color) { if (player != null) player.loadCharacterImages(color); }
}