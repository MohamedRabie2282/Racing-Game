
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

    private static final Color ROAD_COLOR = new Color(50, 50, 50);
    private static final Color GRASS_COLOR = new Color(34, 139, 34);
    private static final Color CAR_COLOR = new Color(255, 69, 0);
    private static final Color OBSTACLE_COLOR = new Color(70, 130, 180);
    private static final Color ROAD_LINE_COLOR = new Color(255, 215, 0);

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
        initializeKeyListener();

        timer = new Timer(1000 / 60, this);
        timer.start();

        spawnObstacles();
        initializeKeyListener();
    }

    private void initializeKeyListener() {
        addKeyListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawRoad(g);
        drawCar((Graphics2D) g);
        drawObstacles((Graphics2D) g);
        drawUI((Graphics2D) g);
    }

    private void drawRoad(Graphics g) {
        g.setColor(GRASS_COLOR);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        // رسم الطريق
        g.setColor(ROAD_COLOR);
        g.fillRect(ROAD_LEFT_BOUNDARY, 0, ROAD_WIDTH, WINDOW_HEIGHT);

        // رسم خطوط الطريق
        g.setColor(ROAD_LINE_COLOR);
        float[] dashPattern = {20.0f, 40.0f};
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dashPattern, 0.0f));
        g2d.drawLine(295, 0, 295, WINDOW_HEIGHT);

        //رسم خط النهاية 
        if (score >= FINISH_LINE_SCORE - 100) {
            g.setColor(Color.WHITE);
            float[] checkeredPattern = {20.0f, 20.0f};
            g2d.setStroke(new BasicStroke(20, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, checkeredPattern, 0.0f));
            g.drawLine(ROAD_LEFT_BOUNDARY, 0, ROAD_LEFT_BOUNDARY + ROAD_WIDTH, 0);
        }
    }

    private void drawCar(Graphics2D g) {
        // رسم جسم السيارة
        g.setColor(CAR_COLOR);
        g.fillRoundRect(carX, carY, carWidth, carHeight, 10, 10);

        // رسم النوافذ
        g.setColor(Color.CYAN);
        g.fillRoundRect(carX + 5, carY + 10, carWidth - 10, 15, 5, 5);

        // رسم العجلات
        g.setColor(Color.BLACK);
        g.fillOval(carX - 5, carY + 5, 10, 20);
        g.fillOval(carX + carWidth - 5, carY + 5, 10, 20);
        g.fillOval(carX - 5, carY + carHeight - 25, 10, 20);
        g.fillOval(carX + carWidth - 5, carY + carHeight - 25, 10, 20);
    }

    private void drawObstacles(Graphics2D g) {
        g.setColor(OBSTACLE_COLOR);
        for (Rectangle obstacle : obstacles) {
            // رسم العوائق بشكل منحني
            g.fillRoundRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height, 15, 15);

            // إضافة تأثير ظل
            g.setColor(new Color(0, 0, 0, 50));
            g.fillRoundRect(obstacle.x + 5, obstacle.y + 5, obstacle.width, obstacle.height, 15, 15);
            g.setColor(OBSTACLE_COLOR);
        }
    }

    private void drawUI(Graphics2D g) {
        // رسم لوحة المعلومات
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(10, 10, 150, 40, 10, 10);
        g.fillRoundRect(WINDOW_WIDTH - 160, 10, 150, 40, 10, 10);

        // رسم النص
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("النقاط: " + score, 20, 35);
        g.drawString("الأرواح: " + remainingLives, WINDOW_WIDTH - 150, 35);

        if (hasFinished) {
            drawGameMessage(g, "!مبروك الفوز", Color.GREEN);
            if (showWinDialog) {
                showWinDialog = false;
                SwingUtilities.invokeLater(this::handleGameWin);
            }
        }

        if (isGameOver) {
            drawGameMessage(g, "!انتهت اللعبة", Color.RED);
        }
    }

    private void drawGameMessage(Graphics2D g, String message, Color color) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(150, 100, 300, 80, 20, 20);

        g.setColor(color);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(message);
        g.drawString(message, (WINDOW_WIDTH - textWidth) / 2, 150);
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
        moveLeft = false;
        moveRight = false;
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
        moveLeft = false;
        moveRight = false;
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

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Racing Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new RacingGame());
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
