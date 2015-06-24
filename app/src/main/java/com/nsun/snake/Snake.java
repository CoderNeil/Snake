package com.nsun.snake;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import java.util.Vector;

/**
 * Created by Neil on 2015-02-04.
 */
public class Snake extends Activity {

    Paint snakeColor = new Paint();
    Paint foodColor = new Paint();
    Paint drawColor = new Paint();
    Paint boardColor = new Paint();
    private int spawnFood;
    private Bitmap environment; //used to store background
    private static GameView gamePanel;
    private RefreshHandler refreshHandler;
    private int delay = 400;
    private int verticalSquares;
    private int horizontalSquares;
    //game board
    private boolean[][] board;
    private int[][] spawnRandomFood;
    Vector<int[]> snakeBody = new Vector<int[]>();
    private boolean firstDraw = true; //whether the screen is being drawn for the first time. used for optimization
    private boolean newFood = true; //the previous ball was successfully clicked; redraw a new one at a random location within the border
    boolean gamePaused = false;
    private boolean up,left,right,down;
    private boolean hit = false; //whether the ball hit something
    private boolean receivedInput = false;

    //screen positions
    public static short screenHeight = 0; //screen height in pixels
    public static short screenWidth = 0; //screen width in pixels
    private int newSnakeX;
    private int newSnakeY;
    //Gameplay related variables
    private long lastUpdateTime;


    class RefreshHandler extends Handler { //Makes animation possible

        @Override
        public void handleMessage(Message msg) {
            Snake.this.update();
            Snake.gamePanel.invalidate();
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //remove the title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Set top and left boundaries; ball tends to get stuck on these
        //set up the application context for toast notifications
        //Context context = getApplicationContext();
        gamePanel = new GameView(this);
        up=true;
        setContentView(gamePanel);
        refreshHandler = new RefreshHandler();
        lastUpdateTime = System.currentTimeMillis();
        update();
    }

    @Override
    public void onPause() {
        gamePaused = true;
        refreshHandler.removeMessages(0);
        super.onStop();
    }

    @Override
    public void onResume() {
        gamePaused = false;
        //refreshHandler.removeMessages(0);
        refreshHandler = new RefreshHandler();
        super.onResume();
    }


    private static int random(int min, int max) { //random number generator
        return (int) ((Math.random() * max) + min);
    }

    private void update() {
        //contains some code borrowed from snake example
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime >= delay) {
            if (!gamePaused) {
                receivedInput = false;
                gamePanel.invalidate();
                lastUpdateTime = currentTime;
            }
        }
        refreshHandler.sleep(delay);
    }

    public boolean onTouchEvent(MotionEvent event) {
        //Only record it as a press if they press down, not if they let go.
        if (event.getAction() == MotionEvent.ACTION_DOWN && !receivedInput) {
            short userX = (short) event.getX();
            short userY = (short) event.getY();
            if (up || down){
                if(userX < screenWidth/2){
                    left=true;
                    up=false;
                    down=false;
                    right=false;
                }
                else{
                    right = true;
                    up=false;
                    down=false;
                    left=false;
                }
            }
            else if (left || right){
                if(userY < screenHeight/2){
                    up=true;
                    down=false;
                    right=false;
                    left=false;
                }
                else{
                    down = true;
                    right=false;
                    left=false;
                    up=false;
                }
            }
            receivedInput=false;
        }

        return true;
    }


    public class GameView extends View {

        public GameView(Context context) {
            super(context);
        }

        @Override
        public void onDraw(Canvas canvas) {
            int squareSize = 40;
            int[] tempSnake;
            if (firstDraw) {
                //Determine screen resolution
                screenHeight = (short) this.getHeight();
                screenWidth = (short) this.getWidth();
                environment = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.RGB_565); //allows the background and border rendering to only be done once
                canvas = new Canvas(environment);
                verticalSquares = screenHeight / squareSize;
                horizontalSquares = screenWidth / squareSize;
                board = new boolean[horizontalSquares][verticalSquares];
                spawnRandomFood = new int[2][verticalSquares*horizontalSquares-1];
                canvas.drawColor(Color.WHITE);
                boardColor.setColor(Color.WHITE);
                snakeColor.setColor(Color.BLACK);
                foodColor.setColor(Color.GRAY);
                //draw background
                for (short i = 0; i < verticalSquares * squareSize; i += squareSize) {
                    for (short j = 0; j < horizontalSquares * squareSize; j += squareSize) {
                        //canvas.drawRect(j, i, j + squareSize, i + squareSize, snakeColor);
                        //canvas.drawRect(j+2, i+2, j + squareSize -2, i + squareSize -2, boardColor);
                        board[j/ squareSize][i/ squareSize] = true; //sets all board cells to free
                    }

                }
                tempSnake = new int[2];
                tempSnake[0] = horizontalSquares/2* squareSize; //passing the x cor of the snake body
                tempSnake[1] = verticalSquares/2* squareSize; //passing the y cor of the snake body
                snakeBody.add(tempSnake); //building the snake
                board[horizontalSquares/2][verticalSquares/2] = false; //setting the board space to be not free
                canvas = new Canvas();
            }
            canvas.drawBitmap(environment, 0, 0, drawColor);
                //check if the snake hit the wall X-X

            if ((spawnRandomFood[0][spawnFood] == snakeBody.get(0)[0])&&
                    (spawnRandomFood[1][spawnFood] == snakeBody.get(0)[1])){ //if the ball exceeds the left boundary
                tempSnake = new int[2];
                tempSnake[0] = newSnakeX;
                tempSnake[1] = newSnakeY;
                snakeBody.add(tempSnake);
                board[snakeBody.get(snakeBody.size()-1)[0]/ squareSize][snakeBody.get(snakeBody.size()-1)[1]/ squareSize] = false;
                if (delay >= 150) {
                    delay -= 10;
                }
                newFood = true;
            }

            if (!hit) {
                board[snakeBody.get(snakeBody.size() - 1)[0] / squareSize][snakeBody.get(snakeBody.size() - 1)[1] / squareSize] = true;
                if (up) {

                    newSnakeX = snakeBody.get(snakeBody.size() - 1)[0];
                    newSnakeY = snakeBody.get(snakeBody.size() - 1)[1];
                    for (int i = snakeBody.size() - 1; i > 0; i--) {
                        snakeBody.get(i)[0] = snakeBody.get(i - 1)[0]; //update the ball's location on the X axis
                        snakeBody.get(i)[1] = snakeBody.get(i - 1)[1];
                    }
                    snakeBody.get(0)[1] -= squareSize;
                    for (int i = 3; i < snakeBody.size(); i++) {
                        if ((snakeBody.get(0)[0] == snakeBody.get(i)[0]) && (snakeBody.get(0)[1] == snakeBody.get(i)[1])) {
                            hit = true;
                            break;
                        }
                    }
                } else if (left) {
                    newSnakeX = snakeBody.get(snakeBody.size() - 1)[0];
                    newSnakeY = snakeBody.get(snakeBody.size() - 1)[1];
                    for (int i = snakeBody.size() - 1; i > 0; i--) {
                        snakeBody.get(i)[0] = snakeBody.get(i - 1)[0]; //update the ball's location on the X axis
                        snakeBody.get(i)[1] = snakeBody.get(i - 1)[1];
                    }
                    snakeBody.get(0)[0] -= squareSize;
                    for (int i = 3; i < snakeBody.size(); i++) {
                        if ((snakeBody.get(0)[0] == snakeBody.get(i)[0]) && (snakeBody.get(0)[1] == snakeBody.get(i)[1])) {
                            hit = true;
                            break;
                        }
                    }
                } else if (right) {
                    newSnakeX = snakeBody.get(snakeBody.size() - 1)[0];
                    newSnakeY = snakeBody.get(snakeBody.size() - 1)[1];
                    for (int i = snakeBody.size() - 1; i > 0; i--) {
                        snakeBody.get(i)[0] = snakeBody.get(i - 1)[0]; //update the ball's location on the X axis
                        snakeBody.get(i)[1] = snakeBody.get(i - 1)[1];
                    }
                    snakeBody.get(0)[0] += squareSize;
                    for (int i = 3; i < snakeBody.size(); i++) {
                        if ((snakeBody.get(0)[0] == snakeBody.get(i)[0]) && (snakeBody.get(0)[1] == snakeBody.get(i)[1])) {
                            hit = true;
                            break;
                        }
                    }
                } else if (down) {
                    newSnakeX = snakeBody.get(snakeBody.size() - 1)[0];
                    newSnakeY = snakeBody.get(snakeBody.size() - 1)[1];
                    for (int i = snakeBody.size() - 1; i > 0; i--) {
                        snakeBody.get(i)[0] = snakeBody.get(i - 1)[0]; //update the ball's location on the X axis
                        snakeBody.get(i)[1] = snakeBody.get(i - 1)[1];
                    }
                    snakeBody.get(0)[1] += squareSize;
                    for (int i = 3; i < snakeBody.size(); i++) {
                        if ((snakeBody.get(0)[0] == snakeBody.get(i)[0]) && (snakeBody.get(0)[1] == snakeBody.get(i)[1])) {
                            hit = true;
                            break;
                        }
                    }
                }
                if (((snakeBody.get(0)[0]) < 0) ||
                        ((snakeBody.get(0)[1]) < 0) ||
                        ((snakeBody.get(0)[1]) >= verticalSquares * squareSize) ||
                        ((snakeBody.get(0)[0]) >= horizontalSquares * squareSize)) {
                    hit = true;
                    for(int i = 0; i< snakeBody.size()-1; i ++){
                        snakeBody.get(i)[0] = snakeBody.get(i+1)[0];
                        snakeBody.get(i)[1] = snakeBody.get(i+1)[1];
                    }
                    snakeBody.get(snakeBody.size()-1)[0] = newSnakeX;
                    snakeBody.get(snakeBody.size()-1)[1] = newSnakeY;
                } else {
                    board[snakeBody.get(0)[0] / squareSize][snakeBody.get(0)[1] / squareSize] = false;
                }
            }

            if (newFood) {
                int count = 0;
                spawnRandomFood = new int[2][verticalSquares*horizontalSquares-snakeBody.size()+1];
                for (short i = 0; i < board.length; i++) {
                    for (short j = 0; j < board[i].length; j++) {
                        if(board[i][j]){
                            spawnRandomFood[0][count]=i* squareSize;
                            spawnRandomFood[1][count]=j* squareSize;
                            count++;
                        }
                    }
                }
                spawnFood = random(0,spawnRandomFood[0].length-1);
                firstDraw=false;
                newFood = false;
            }
            canvas.drawRect(spawnRandomFood[0][spawnFood]+2,spawnRandomFood[1][spawnFood]+2,
                    spawnRandomFood[0][spawnFood]+ squareSize -2,spawnRandomFood[1][spawnFood]+ squareSize -2,foodColor);
            for (int i = 0; i<snakeBody.size();i++) {
                canvas.drawRect(snakeBody.get(i)[0]+2,snakeBody.get(i)[1]+2,snakeBody.get(i)[0]+ squareSize -2,snakeBody.get(i)[1]+ squareSize -2,snakeColor);
            }
        }

    }
}
