
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class RacingGame extends JPanel implements ActionListener, KeyListener {

    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;
    private static final int ROAD_LEFT_BOUNDARY = 150;
    private static final int ROAD_WIDTH = 300;
    private static final int FINISH_LINE_SCORE = 1000;
    private static final int INITIAL_LIVES = 3;

    private final Timer timer;
    private final Random random = new Random();

    private int carX = 250;
    private int carY = 500;
    private int carWidth = 40;
    private int carHeight = 60;
    private int speed = 15;
    private int obstacleSpeed = 5;
    private int score = 0;
    private int remainingLives = INITIAL_LIVES;

    private boolean moveLeft = false;
    private boolean moveRight = false;
    private boolean hasFinished = false;
    private boolean isGameOver = false;
    private boolean showWinDialog = false;

    private final ArrayList<Rectangle> obstacles = new ArrayList<>();

    public RacingGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.darkGray);
        setFocusable(true);
        addKeyListener(this);

        timer = new Timer(1000 / 60, this);
        timer.start();

        spawnObstacles();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawRoad(g);
        drawCar(g);
        drawObstacles(g);
        drawUI(g);
    }

    private void drawRoad(Graphics g) {
        // Draw road
        g.setColor(Color.white);
        g.fillRect(ROAD_LEFT_BOUNDARY, 0, ROAD_WIDTH, WINDOW_HEIGHT);

        // Draw road lines
        g.setColor(Color.yellow);
        for (int i = 0; i < WINDOW_HEIGHT; i += 40) {
            g.fillRect(295, i, 10, 20);
        }

        // Draw finish line when near victory
        if (score >= FINISH_LINE_SCORE) {
            g.setColor(Color.white);
            g.fillRect(ROAD_LEFT_BOUNDARY, 0, 10, WINDOW_HEIGHT);
        }
    }

    private void drawCar(Graphics g) {
        g.setColor(Color.red);
        g.fillRect(carX, carY, carWidth, carHeight);
    }

    private void drawObstacles(Graphics g) {
        g.setColor(Color.green);
        for (Rectangle obstacle : obstacles) {
            g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }
    }

    private void drawUI(Graphics g) {
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 30);
        g.drawString("Lives: " + remainingLives, 500, 30);

        if (hasFinished) {
            g.setColor(Color.green);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("You Win!", 220, 150);

            if (showWinDialog) {
                showWinDialog = false;
                SwingUtilities.invokeLater(this::handleGameWin);
            }
        }

        if (isGameOver) {
            g.setColor(Color.red);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Game Over!", 200, 150);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isGameOver && !hasFinished) {
            updateCarPosition();
            updateObstacles();
            checkCollisions();
            updateGameState();
        }
        repaint();
    }

    private void updateCarPosition() {
        if (moveLeft && carX > ROAD_LEFT_BOUNDARY) {
            carX -= speed;
        }
        if (moveRight && carX < ROAD_LEFT_BOUNDARY + ROAD_WIDTH - carWidth) {
            carX += speed;
        }
    }

    private void updateObstacles() {
        obstacles.removeIf(obstacle -> obstacle.y > WINDOW_HEIGHT);

        for (Rectangle obstacle : obstacles) {
            obstacle.y += obstacleSpeed;
        }

        if (random.nextDouble() < 0.05) {
            spawnObstacles();
        }
    }

    private void updateGameState() {
        if (!hasFinished) {
            score++;

            // Increase difficulty
            if (score % 100 == 0 && obstacleSpeed < 20) {
                obstacleSpeed++;
            }
        }

        if (score >= FINISH_LINE_SCORE && !hasFinished) {
            hasFinished = true;
            showWinDialog = true;
            timer.stop();
        }
    }

    private void spawnObstacles() {
        int x = random.nextInt(200) + 200;
        int width = 50 + random.nextInt(50);
        obstacles.add(new Rectangle(x, -100, width, 50));
    }

    private void checkCollisions() {
        Rectangle carBounds = new Rectangle(carX, carY, carWidth, carHeight);
        for (Rectangle obstacle : obstacles) {
            if (obstacle.intersects(carBounds)) {
                handleCollision();
                break;
            }
        }
    }

    private void handleCollision() {
        remainingLives--;
        if (remainingLives <= 0) {
            isGameOver = true;
            timer.stop();
            SwingUtilities.invokeLater(this::handleGameOver);
        } else {
            resetForNewAttempt();
        }
    }

    private void handleGameOver() {
        int response = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                "You lost all your lives. Try again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION
        );

        if (response == JOptionPane.YES_OPTION) {
            resetGame();
            timer.start();
        } else {
            System.exit(0);
        }
    }

    private void handleGameWin() {
        int response = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                "Congratulations! Play again?",
                "Victory!",
                JOptionPane.YES_NO_OPTION
        );

        if (response == JOptionPane.YES_OPTION) {
            resetGame();
            timer.start();
        } else {
            System.exit(0);
        }
    }

    private void resetForNewAttempt() {
        carX = 250;
        carY = 500;
        score = 0;
        obstacles.clear();
        spawnObstacles();
    }

    private void resetGame() {
        carX = 250;
        carY = 500;
        score = 0;
        obstacleSpeed = 5;
        obstacles.clear();
        spawnObstacles();
        remainingLives = INITIAL_LIVES;
        hasFinished = false;
        isGameOver = false;
        showWinDialog = false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT ->
                moveLeft = true;
            case KeyEvent.VK_RIGHT ->
                moveRight = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT ->
                moveLeft = false;
            case KeyEvent.VK_RIGHT ->
                moveRight = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Racing Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new RacingGame());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
