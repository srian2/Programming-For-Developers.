import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.Timer;
public class TetrisGame extends JPanel implements ActionListener {
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int CELL_SIZE = 30;
    private static final int INITIAL_DELAY = 500;
    private static final int DELAY_DECREASE_RATE = 50;
    private static final int SCORE_FOR_LEVEL_UP = 200;

    private Timer timer;
    private Color[][] board;
    private Queue<Block> blockQueue;
    private Block currentBlock;
    private Block nextBlock;
    private int score;
    private int level;

    public TetrisGame() {
        setPreferredSize(new Dimension(BOARD_WIDTH * CELL_SIZE + 150, BOARD_HEIGHT * CELL_SIZE));
        setBackground(Color.BLACK);
        setFocusable(true);

        board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
        blockQueue = new LinkedList<>();
        score = 0;
        level = 1;

        initializeGame();
        startGame();
    }

    private void initializeGame() {
        for (Color[] row : board) {
            Arrays.fill(row, null);
        }
        blockQueue.clear();
        enqueueNewBlock();
        currentBlock = blockQueue.poll();
        enqueueNewBlock();
        nextBlock = blockQueue.peek();
    }

    private void startGame() {
        timer = new Timer(INITIAL_DELAY, this);
        timer.start();
    }

    private void enqueueNewBlock() {
        blockQueue.add(Block.getRandomBlock());
    }

    private void updateGame() {
        if (currentBlock.canMoveDown(board)) {
            currentBlock.moveDown();
        } else {
            placeBlock();
            checkCompletedRows();
            if (isGameOver()) {
                gameOver();
                return;
            }
            currentBlock = blockQueue.poll();
            enqueueNewBlock();
            nextBlock = blockQueue.peek();
        }
        updateLevel();
        repaint();
    }

    private void placeBlock() {
        int[][] shape = currentBlock.getShape();
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int x = currentBlock.getX() + j;
                    int y = currentBlock.getY() + i;
                    if (y >= 0 && y < BOARD_HEIGHT && x >= 0 && x < BOARD_WIDTH) {
                        board[y][x] = currentBlock.getColor();
                    }
                }
            }
        }
    }

    private void checkCompletedRows() {
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            boolean rowComplete = true;
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (board[y][x] == null) {
                    rowComplete = false;
                    break;
                }
            }
            if (rowComplete) {
                removeRow(y);
                score += 100;
            }
        }
    }

    private void removeRow(int row) {
        for (int y = row; y > 0; y--) {
            System.arraycopy(board[y - 1], 0, board[y], 0, BOARD_WIDTH);
        }
        Arrays.fill(board[0], null);
    }

    private boolean isGameOver() {
        for (int x = 0; x < BOARD_WIDTH; x++) {
            if (board[0][x] != null) {
                return true;
            }
        }
        return false;
    }

    private void gameOver() {
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over! Score: " + score + " Level: " + level, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        initializeGame();
        score = 0;
        level = 1;
        startGame();
    }

    private void updateLevel() {
        int newLevel = score / SCORE_FOR_LEVEL_UP + 1;
        if (newLevel != level) {
            level = newLevel;
            int newDelay = Math.max(INITIAL_DELAY - (level - 1) * DELAY_DECREASE_RATE, 100);
            timer.setDelay(newDelay);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(1));

        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                g.setColor(Color.GRAY);
                g.drawRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                if (board[y][x] != null) {
                    g.setColor(board[y][x]);
                    g.fillRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect(x * CELL_SIZE, y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }

        if (currentBlock != null) {
            currentBlock.draw(g, CELL_SIZE);
        }

        if (nextBlock != null) {
            int previewX = BOARD_WIDTH * CELL_SIZE + 20;
            int previewY = 20;

            Font labelFont = new Font("Arial", Font.BOLD, 18);
            g.setFont(labelFont);

            g.setColor(Color.BLACK);
            g.drawString("Next Block:", previewX + 2, previewY + 2);

            g.setColor(Color.WHITE);
            g.drawString("Next Block:", previewX, previewY);

            nextBlock.drawPreview(g, previewX, previewY + 20, CELL_SIZE);
        }

        String scoreText = "Score: " + score;
        String levelText = "Level: " + level;

        Font font = new Font("Arial", Font.BOLD, 20);
        g.setFont(font);

        g.setColor(Color.BLACK);
        g.drawString(scoreText, BOARD_WIDTH * CELL_SIZE + 22, 152);
        g.setColor(Color.WHITE);
        g.drawString(scoreText, BOARD_WIDTH * CELL_SIZE + 20, 150);

        g.setColor(Color.BLACK);
        g.drawString(levelText, BOARD_WIDTH * CELL_SIZE + 22, 182);
        g.setColor(Color.WHITE);
        g.drawString(levelText, BOARD_WIDTH * CELL_SIZE + 20, 180);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris Game");
        TetrisGame game = new TetrisGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        InputMap inputMap = game.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = game.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "left");
        actionMap.put("left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (game.currentBlock.canMoveLeft(game.board)) {
                    game.currentBlock.moveLeft();
                    game.repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "right");
        actionMap.put("right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (game.currentBlock.canMoveRight(game.board)) {
                    game.currentBlock.moveRight();
                    game.repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("UP"), "rotate");
        actionMap.put("rotate", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (game.currentBlock.canRotate(game.board)) {
                    game.currentBlock.rotate();
                    game.repaint();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("DOWN"), "down");
        actionMap.put("down", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (game.currentBlock.canMoveDown(game.board)) {
                    game.currentBlock.moveDown();
                    game.repaint();
                }
            }
        });
    }
}

class Block {
    private static final int[][][] SHAPES = {
            {{1, 1, 1, 1}}, // I
            {{1, 1}, {1, 1}}, // O
            {{1, 1, 1}, {0, 1, 0}}, // T
            {{1, 1, 0}, {0, 1, 1}}, // Z
            {{0, 1, 1}, {1, 1, 0}}, // S
            {{1, 1, 1}, {1, 0, 0}}, // L
            {{1, 1, 1}, {0, 0, 1}}  // J
    };

    private int[][] shape;
    private int x, y;
    private Color color;

    public Block(int[][] shape, Color color) {
        this.shape = shape;
        this.color = color;
        this.x = 4;
        this.y = 0;
    }

    public static Block getRandomBlock() {
        Random random = new Random();
        int index = random.nextInt(SHAPES.length);
        return new Block(SHAPES[index], getRandomColor());
    }

    public static Color getRandomColor() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return new Color(r, g, b);
    }

    public int[][] getShape() {
        return shape;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }

    public boolean canMoveDown(Color[][] board) {
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int newX = x + j;
                    int newY = y + i + 1;
                    if (newY >= board.length || (newY >= 0 && board[newY][newX] != null)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void moveDown() {
        y++;
    }

    public void moveLeft() {
        x--;
    }

    public void moveRight() {
        x++;
    }

    public boolean canMoveLeft(Color[][] board) {
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int newX = x + j - 1;
                    if (newX < 0 || (newX >= 0 && board[y + i][newX] != null)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean canMoveRight(Color[][] board) {
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int newX = x + j + 1;
                    if (newX >= board[0].length || (newX < board[0].length && board[y + i][newX] != null)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean canRotate(Color[][] board) {
        int[][] rotated = new int[shape[0].length][shape.length];
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                rotated[j][shape.length - 1 - i] = shape[i][j];
            }
        }

        for (int i = 0; i < rotated.length; i++) {
            for (int j = 0; j < rotated[i].length; j++) {
                if (rotated[i][j] != 0) {
                    int newX = x + j;
                    int newY = y + i;
                    if (newX < 0 || newX >= board[0].length || newY >= board.length || (newY >= 0 && board[newY][newX] != null)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void rotate() {
        int[][] rotated = new int[shape[0].length][shape.length];
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                rotated[j][shape.length - 1 - i] = shape[i][j];
            }
        }
        shape = rotated;
    }

    public void draw(Graphics g, int cellSize) {
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int xPos = (x + j) * cellSize;
                    int yPos = (y + i) * cellSize;
                    g.setColor(color);
                    g.fillRect(xPos, yPos, cellSize, cellSize);
                    g.setColor(Color.BLACK);
                    g.drawRect(xPos, yPos, cellSize, cellSize);
                }
            }
        }
    }

    public void drawPreview(Graphics g, int x, int y, int cellSize) {
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    g.setColor(color);
                    g.fillRect(x + j * cellSize, y + i * cellSize, cellSize, cellSize);
                    g.setColor(Color.BLACK);
                    g.drawRect(x + j * cellSize, y + i * cellSize, cellSize, cellSize);
                }
            }
        }
    }
}
