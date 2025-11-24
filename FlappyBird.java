import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;
import java.io.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener{
    int boardWidth = 360;
    int boardHeight = 640;

    // Images
    Image backgroundImg;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    //Bird
    int birdX = boardWidth/8;
    int birdY = boardHeight/2;
    int birdWidth = 51;
    int birdHeight = 36;

    class Bird{
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;
        boolean hasShield = false;
        int shieldTimer = 0;

        Bird(Image img){
            this.img = img;
        }
    }
    
    //Pipes
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        Image img;
        boolean passed = false;
        boolean shieldUsed = false; // Track if shield was used on this pipe
        Color color = null; // For colored pipes
        
        Pipe(Image img){
            this.img = img;
        }
    }

    // Power-up class
    class PowerUp {
        int x, y;
        int width = 30;
        int height = 30;
        String type; // "shield", "slowmo", "points"
        boolean collected = false;
        
        PowerUp(int x, int y, String type) {
            this.x = x;
            this.y = y;
            this.type = type;
        }
    }

    //game logic
    Bird bird;
    int velocityX = -4; //move the pipe to the left speed (so like bird is moving to right)
    int velocityY = 0; //for the bird to go up
    int gravity = 1; //for the frame to slow down by 1 pixel
    int baseVelocityX = -4;

    ArrayList<Pipe> pipes; 
    ArrayList<PowerUp> powerUps;
    Random random = new Random();

    Timer gameLoop;
    Timer placePipesTimer;

    boolean gameOver = false;
    boolean gameStarted = false; // Track if the game has started
    boolean gamePaused = false;
    boolean autoPlay = false; // AI autopilot mode

    double score = 0;
    int highScore = 0;
    int combo = 0;
    int maxCombo = 0;
    
    // Power-up effects
    boolean slowMotionActive = false;
    int slowMotionTimer = 0;
    
    // Particle effects
    ArrayList<Particle> particles;
    
    class Particle {
        int x, y;
        int vx, vy;
        int size;
        Color color;
        int life;
        
        Particle(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.vx = random.nextInt(10) - 5;
            this.vy = random.nextInt(10) - 5;
            this.size = random.nextInt(5) + 3;
            this.color = color;
            this.life = 30;
        }
        
        void update() {
            x += vx;
            y += vy;
            vy += 1; // gravity
            life--;
        }
    }

    //Action performed every 60 secs
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted && !gamePaused) { // Only move the bird and pipes if the game has started
            if (autoPlay) {
                autoPlayLogic(); // AI makes decisions
            }
            move();
            updatePowerUps();
            updateParticles();
            repaint();
        }
        if (gameOver) {
            placePipesTimer.stop();
            gameLoop.stop();
        }
    }

    FlappyBird(){
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        // setBackground(Color.blue);

        setFocusable(true); //make sure this is the one that takes in our key events
        addKeyListener(this);

        //load images
        backgroundImg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        //bird
        bird = new Bird(birdImg);
        pipes = new ArrayList<Pipe>();
        powerUps = new ArrayList<PowerUp>();
        particles = new ArrayList<Particle>();
        
        loadHighScore();
        
        //place pipes timer - dynamically adjust timing based on velocity
        placePipesTimer = new Timer(1200, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a){
                if (!gamePaused) {
                    placePipes();
                    // Adjust timer delay based on current velocity to keep spacing consistent
                    int newDelay = (int)(1200 * (baseVelocityX / (double)velocityX));
                    placePipesTimer.setDelay(newDelay);
                }
            }
        });
        //placePipesTimer.start();

        //game timer
        gameLoop = new Timer(1000/60, this);
        gameLoop.start();
    }
    
    public void placePipes(){
        int randomPipeY = (int)(pipeY - pipeHeight/4 - Math.random()*(pipeHeight/2));
        int openingSpace = boardHeight/4;
        //we gonna add a new pipe every 1.5 secs
        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
        
        // Occasionally spawn power-ups (20% chance)
        if (random.nextDouble() < 0.2) {
            String[] types = {"shield", "points"}; // Removed slowmo
            String type = types[random.nextInt(types.length)];
            int powerUpY = topPipe.y + pipeHeight + openingSpace/2 - 15;
            powerUps.add(new PowerUp(pipeX, powerUpY, type));
        }
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        draw(g);
    }

    public void draw (Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        
        //background
        g.drawImage(backgroundImg, 0, 0, boardWidth, boardHeight, null);

        // Draw particles
        for (Particle p : particles) {
            g2d.setColor(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), 
                                   Math.min(255, p.life * 8)));
            g2d.fillOval(p.x, p.y, p.size, p.size);
        }

        //pipes
        for(int i = 0 ; i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }
        
        // Draw power-ups
        for (PowerUp pu : powerUps) {
            if (!pu.collected) {
                g2d.setColor(getPowerUpColor(pu.type));
                g2d.fillOval(pu.x, pu.y, pu.width, pu.height);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                String symbol = getPowerUpSymbol(pu.type);
                g2d.drawString(symbol, pu.x + 8, pu.y + 20);
            }
        }

        //bird with shield effect
        if (bird.hasShield) {
            g2d.setColor(new Color(100, 200, 255, 100));
            g2d.fillOval(bird.x - 5, bird.y - 5, bird.width + 10, bird.height + 10);
        }
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        // UI Elements
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        if (gameOver){
            g.drawString("Game Over!", boardWidth/2 - 100, boardHeight/2 - 50);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Score: " + (int)score, boardWidth/2 - 60, boardHeight/2);
            g.drawString("High Score: " + highScore, boardWidth/2 - 90, boardHeight/2 + 30);
            if (maxCombo > 1) {
                g.drawString("Max Combo: " + maxCombo + "x", boardWidth/2 - 90, boardHeight/2 + 60);
            }
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Press SPACE to restart", boardWidth/2 - 100, boardHeight/2 + 100);
        }
        else{
            g.drawString(String.valueOf((int) score), 10, 35);
            
            // Combo indicator
            if (combo > 1) {
                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 24));
                g.drawString("Combo x" + combo, 10, 65);
            }
            
            // Power-up indicators
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            int indicatorY = 90;
            if (bird.hasShield) {
                g.drawString("Shield: " + (bird.shieldTimer/60) + "s", 10, indicatorY);
            }
            
            // High score display
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Best: " + highScore, boardWidth - 100, 25);
        }
        
        // Pause indicator
        if (gamePaused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, boardWidth, boardHeight);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("PAUSED", boardWidth/2 - 90, boardHeight/2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press P to resume", boardWidth/2 - 90, boardHeight/2 + 40);
        }
        
        // Start screen
        if (!gameStarted && !gameOver) {
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRect(0, 0, boardWidth, boardHeight);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Flappy Bird+", boardWidth/2 - 110, boardHeight/2 - 40);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press SPACE to start", boardWidth/2 - 100, boardHeight/2 + 10);
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("P - Pause | A - AutoPlay", boardWidth/2 - 90, boardHeight/2 + 50);
        }
        
        // AutoPlay indicator
        if (autoPlay && gameStarted && !gameOver) {
            g.setColor(new Color(255, 100, 100));
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("● AUTO", boardWidth - 80, boardHeight - 20);
        }
    }

    public void move(){
        // Update difficulty based on score - slower progression
        if ((int)score % 10 == 0 && (int)score > 0) {
            // Reduce velocity increase from 1 per 10 points to 0.5 per 10 points
            velocityX = (int)(baseVelocityX - ((int)score / 20.0));
            // Cap the maximum speed increase
            velocityX = Math.max(velocityX, baseVelocityX - 3);
        }
        
        // Apply slow motion
        int currentVelocityX = velocityX;
        
        //birds
        velocityY += gravity;
        bird.y += velocityY;  //so it doesn't go out of our frame
        bird.y = Math.max(bird.y, 0);

        //pipes
        for (int i = 0 ; i < pipes.size(); i++){
            Pipe pipe = pipes.get(i);
            pipe.x += currentVelocityX;

            if(!pipe.passed && bird.x > pipe.x + pipe.width){ // if the bird has not passed this pipe and the x position is passed the right side of the pipe
                pipe.passed = true;
                score += 0.5;
                combo++;
                maxCombo = Math.max(maxCombo, combo);
                
                // Combo bonus
                if (combo > 2) {
                    score += combo * 0.5;
                }
                
                // Create score particles
                createParticles(pipe.x + pipe.width, boardHeight/2, Color.YELLOW, 5);
            }

            if (collision(bird, pipe)) {
                if (bird.hasShield && !pipe.shieldUsed) {
                    bird.hasShield = false;
                    bird.shieldTimer = 0;
                    pipe.shieldUsed = true; // Mark this pipe so shield isn't used multiple times
                    createParticles(bird.x, bird.y, Color.CYAN, 15);
                } else if (!pipe.shieldUsed) {
                    gameOver = true;
                    createParticles(bird.x, bird.y, Color.RED, 20);
                    if (score > highScore) {
                        highScore = (int)score;
                        saveHighScore();
                    }
                }
            }
        }

        if (bird.y > boardHeight){
            gameOver = true;
            combo = 0;
            if (score > highScore) {
                highScore = (int)score;
                saveHighScore();
            }
        }
        
        // Update shield timer
        if (bird.hasShield) {
            bird.shieldTimer--;
            if (bird.shieldTimer <= 0) {
                bird.hasShield = false;
            }
        }
    }
    
    public void updatePowerUps() {
        // Determine current velocity for power-ups movement
        int currentVelocityX = velocityX;
        
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp pu = powerUps.get(i);
            pu.x += currentVelocityX; // Use the same velocity as pipes
            
            if (!pu.collected && collision(bird, pu)) {
                pu.collected = true;
                applyPowerUp(pu.type);
                createParticles(pu.x, pu.y, getPowerUpColor(pu.type), 10);
            }
            
            if (pu.x < -pu.width || pu.collected) {
                powerUps.remove(i);
            }
        }
    }
    
    public void applyPowerUp(String type) {
        switch(type) {
            case "shield":
                bird.hasShield = true;
                bird.shieldTimer = 300; // 5 seconds
                break;
            case "points":
                score += 5;
                break;
        }
    }
    
    public void updateParticles() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (p.life <= 0) {
                particles.remove(i);
            }
        }
    }
    
    // AI autopilot logic
    public void autoPlayLogic() {
        // Find the closest pipe
        Pipe closestPipe = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (Pipe pipe : pipes) {
            if (pipe.x + pipe.width > bird.x && pipe.x < bird.x + 200) {
                int distance = pipe.x - bird.x;
                if (distance < minDistance) {
                    minDistance = distance;
                    closestPipe = pipe;
                }
            }
        }
        
        if (closestPipe != null) {
            // Calculate the middle of the gap
            int gapMiddle = closestPipe.y + closestPipe.height + (boardHeight/4) / 2;
            
            // If bird is below the middle of the gap, jump
            // Add some buffer to make it smoother (adjustable)
            int buffer = 20;
            if (bird.y + bird.height/2 > gapMiddle + buffer) {
                velocityY = -9; // Make the bird jump
            }
        } else {
            // No pipe nearby, maintain middle height
            if (bird.y > boardHeight / 2) {
                velocityY = -9;
            }
        }
        
        // Collect power-ups
        for (PowerUp pu : powerUps) {
            if (!pu.collected && pu.x > bird.x && pu.x < bird.x + 150) {
                // Adjust bird position to collect power-up
                if (Math.abs((bird.y + bird.height/2) - (pu.y + pu.height/2)) > 30) {
                    if (bird.y + bird.height/2 > pu.y + pu.height/2 + 10) {
                        velocityY = -9;
                    }
                }
            }
        }
    }
    
    public void createParticles(int x, int y, Color color, int count) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(x, y, color));
        }
    }
    
    public Color getPowerUpColor(String type) {
        switch(type) {
            case "shield": return new Color(100, 200, 255);
            case "slowmo": return new Color(255, 200, 100);
            case "points": return new Color(100, 255, 100);
            default: return Color.WHITE;
        }
    }
    
    public String getPowerUpSymbol(String type) {
        switch(type) {
            case "shield": return "S";
            case "points": return "+";
            default: return "?";
        }
    }

    public boolean collision(Bird a, Pipe b){
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height && 
                a.y + a.height > b.y;
    }
    
    public boolean collision(Bird a, PowerUp b){
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height && 
                a.y + a.height > b.y;
    }
    
    public void saveHighScore() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("highscore.txt"));
            writer.println(highScore);
            writer.close();
        } catch (IOException e) {
            System.out.println("Could not save high score");
        }
    }
    
    public void loadHighScore() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("highscore.txt"));
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
            reader.close();
        } catch (IOException e) {
            highScore = 0;
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!gameStarted) {
                gameStarted = true;
                placePipesTimer.start();

            }
            if (!gameOver && !gamePaused && !autoPlay) { // Don't allow manual control in auto mode
                velocityY = -9;
            }
            if (gameOver) {
                // Restart the game
                bird.y = birdY;
                velocityY = 0;
                velocityX = baseVelocityX;
                pipes.clear();
                powerUps.clear();
                particles.clear();
                score = 0;
                combo = 0;
                maxCombo = 0;
                gameOver = false;
                gameStarted = false;
                bird.hasShield = false;
                bird.shieldTimer = 0;
                gameLoop.start();
                //placePipesTimer.start();
            }
        }
        
        if (e.getKeyCode() == KeyEvent.VK_P && gameStarted && !gameOver) {
            gamePaused = !gamePaused;
        }
        
        // Toggle AutoPlay mode with 'A' key
        if (e.getKeyCode() == KeyEvent.VK_A) {
            autoPlay = !autoPlay;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}