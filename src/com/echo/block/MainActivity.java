package com.echo.block;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import com.echo.block.sprite.AnimationBlockSprite;
import com.echo.block.sprite.BlockSprite;
import com.echo.block.sprite.ScoreSprite;
import com.echo.block.sprite.Sprite;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements Callback {

	private SurfaceHolder surfaceHolder;
	private SurfaceView surfaceView;
	private LinearLayout resultLayer;
	private LinearLayout bottomButtonsLayout;
	private TextView currentScoreTextView, bestScoreTextView;
	
	private Button[] bottomButtons;
	
	private DrawingThread drawingThread;

	private float surfaceWidth, surfaceHeight;
	private float blockWidth, blockHeight;

	private int[][] matrix;
	/**
	 * the block which will be hit firstly
	 */
	private int[] firstHitBlockIndexes;
	public static final int DEFAULT_COLUMN_COUNT = 4;
	private int columnCount = DEFAULT_COLUMN_COUNT;
	private int rowCount;

	private int MOVE_STEP_PER_BLOCK = 20;
	private int moveStepCount = 0;
	private float moveHeightPerStep;

	private static final int CELL_TYPE_BLANK = 1;
	private static final int CELL_TYPE_BLOCK = 2;
	/**
	 * the cell being cleaned
	 */
	private static final int CELL_TYPE_ANIMATION = 3;

	/**
	 * | ___| type 0 |_ __| type 1 |__ _| type 2 |___ | type 3
	 */
	private static final int ROW_TYPE_0 = 0;
	private static final int ROW_TYPE_1 = 1;
	private static final int ROW_TYPE_2 = 2;
	private static final int ROW_TYPE_3 = 3;

	private static final long GAP = 10 * 2;
	private List<Sprite> sprites;
	private Paint globalPaint;

	private Random random;

	private Object lock = new Object();

	private int gameState = GAME_STATE_NOT_START;
	private static final int GAME_STATE_NOT_START = 0;
	private static final int GAME_STATE_ONGOING = 1;
	private static final int GAME_STATE_OVER = 2;
	
	private int currentScore = 0;
	private int bestScore = 0;
	private final static int SCORE_PER_ROW = 5;
	private ScoreSprite scoreSprite;
	
	private static final int GAME_TYPE = 0;

    private ViewGroup adsWidgetContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		surfaceView = (SurfaceView) findViewById(R.id.surface_view);
		//surfaceView.setKeepScreenOn(true);
		surfaceView.setZOrderOnTop(true);

		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

		resultLayer = (LinearLayout) findViewById(R.id.resultLayer);
		bottomButtonsLayout = (LinearLayout) findViewById(R.id.bottom_buttons_layout);
//		LayoutInflater layoutInflater = LayoutInflater.from(getBaseContext());
//		resultLayer = (LinearLayout) layoutInflater.inflate(R.layout.result_layer, null);
//		LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//		resultLayer.setLayoutParams(layoutParamsControl);

		bottomButtons = new Button[columnCount];
		bottomButtons[0] = (Button) findViewById(R.id.button_0);
		bottomButtons[1] = (Button) findViewById(R.id.button_1);
		bottomButtons[2] = (Button) findViewById(R.id.button_2);
		bottomButtons[3] = (Button) findViewById(R.id.button_3);
		
		currentScoreTextView = (TextView) findViewById(R.id.score_textview);
		bestScoreTextView = (TextView) findViewById(R.id.best_score_textview);
		adsWidgetContainer = (ViewGroup) findViewById(R.id.ads_widget_container);
		
		sprites = new ArrayList<Sprite>();
		globalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		random = new Random();

		firstHitBlockIndexes = new int[columnCount];
		
		scoreSprite = new ScoreSprite();
		scoreSprite.setTextSize(100);
		
		int screenW = ViewUtil.getScreenWidth(this);
		scoreSprite.setX(screenW / 2);
		scoreSprite.setY(100);
		sprites.add(scoreSprite);
		
		bestScore = (int) App.getBestScore(GAME_TYPE);
		reset();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopDrawingThread();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public void onType0ButtonClick(View view) {
		addNewBlock(0, surfaceHeight, blockWidth, blockHeight);
		if (gameState != GAME_STATE_ONGOING) {
			onGameStart();
		}
	}

	public void onType1ButtonClick(View view) {
		addNewBlock(blockWidth, surfaceHeight, blockWidth, blockHeight);
		if (gameState != GAME_STATE_ONGOING) {
			onGameStart();
		}

	}

	public void onType2ButtonClick(View view) {
		addNewBlock(blockWidth * 2, surfaceHeight, blockWidth, blockHeight);
		if (gameState != GAME_STATE_ONGOING) {
			onGameStart();
		}

	}

	public void onType3ButtonClick(View view) {
		addNewBlock(blockWidth * 3, surfaceHeight, blockWidth, blockHeight);
		if (gameState != GAME_STATE_ONGOING) {
			gameState = GAME_STATE_ONGOING;
			onGameStart();
		}
	}
	
	private void addNewBlock(float x, float y, float w, float h){
		BlockSprite blockSprite = BlockSprite.obtainInstance(x, y, w, h);
		blockSprite.setSpeed(blockHeight / 3);
		synchronized (lock) {
			sprites.add(blockSprite);

		}
		
	}

	public void onRestartButtonClick(View view) {
		reset();
		startDrawingThread();
	}
	
	private void reset(){
		currentScore = 0;
		scoreSprite.setCurrentScore(currentScore);
		resetBlocks();
		resultLayer.setVisibility(View.INVISIBLE);
		bottomButtonsLayout.setVisibility(View.VISIBLE);
		
		int startIndex = random.nextInt(columnCount);
		for (int i = 0; i < columnCount; i++) {
			bottomButtons[i].setTextColor(Color.WHITE);
			bottomButtons[i].setText("");
			bottomButtons[i].setClickable(false);
		}
		bottomButtons[startIndex].setText(R.string.start);
		bottomButtons[startIndex].setClickable(true);
		
	}
	

	private void resetBlocks() {
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < columnCount; j++) {
				matrix[i][j] = CELL_TYPE_BLANK;
			}
		}

		for (int i = 0; i < columnCount; i++) {
			firstHitBlockIndexes[i] = 0;
		}
	}

	/**
	 * add one new row to the top
	 * 
	 * @param rowType
	 */
	private void addRow(int rowType) {
		// firstly, move all cells down
		// and then, add new row
		for (int i = rowCount - 1; i > 0; i--) {
			for (int j = 0; j < columnCount; j++) {
				matrix[i][j] = matrix[i - 1][j];
			}
		}

		switch (rowType) {
			case ROW_TYPE_0:
				matrix[0][0] = CELL_TYPE_BLANK;
				matrix[0][1] = CELL_TYPE_BLOCK;
				matrix[0][2] = CELL_TYPE_BLOCK;
				matrix[0][3] = CELL_TYPE_BLOCK;
				break;
			case ROW_TYPE_1:
				matrix[0][0] = CELL_TYPE_BLOCK;
				matrix[0][1] = CELL_TYPE_BLANK;
				matrix[0][2] = CELL_TYPE_BLOCK;
				matrix[0][3] = CELL_TYPE_BLOCK;
				break;
			case ROW_TYPE_2:
				matrix[0][0] = CELL_TYPE_BLOCK;
				matrix[0][1] = CELL_TYPE_BLOCK;
				matrix[0][2] = CELL_TYPE_BLANK;
				matrix[0][3] = CELL_TYPE_BLOCK;
				break;
			case ROW_TYPE_3:
				matrix[0][0] = CELL_TYPE_BLOCK;
				matrix[0][1] = CELL_TYPE_BLOCK;
				matrix[0][2] = CELL_TYPE_BLOCK;
				matrix[0][3] = CELL_TYPE_BLANK;
				break;
		}
	}

	private void updateFirstHitBlockIndexes() {
		for (int i = 0; i < columnCount; i++) {
			firstHitBlockIndexes[i] = -1;
		}
		for (int i = 0; i < columnCount; i++) {
			for (int j = 0; j < rowCount; j++) {
				if (matrix[j][i] == CELL_TYPE_BLOCK) {
					firstHitBlockIndexes[i] = j;
				}
			}

		}
	}

	/**
	 * mark one to be being cleaned when the animation finishes, the row will be
	 * cleaned
	 */
	private void markRow(int rowIndex) {
		for (int i = 0; i < columnCount; i++) {
			matrix[rowIndex][i] = CELL_TYPE_ANIMATION;
		}
	}

	// TODO bug: 清楚动画行之后，可以上上移的块没有上移
	/**
	 * clean one row, called after the animation finishes
	 * 
	 * @param rowIndex
	 */
	private void cleanAnimationRow() {
		int rowIndex = rowCount;
		for (int i = 0; i < rowCount; i++) {
			if (matrix[i][0] == CELL_TYPE_ANIMATION) {
				rowIndex = i;
				break;
			}
		}
		if (rowIndex < rowCount) {
			for (int i = rowIndex; i < rowCount - 1; i++) {
				for (int j = 0; j < columnCount; j++) {
					matrix[i][j] = matrix[i + 1][j];
				}
			}
		}
		for (int i = 0; i < columnCount; i++) {
			matrix[rowCount - 1][i] = CELL_TYPE_BLANK;
		}
	}

	private void drawAllBlocks(Canvas canvas, Paint paint) {
		int cellType;
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < columnCount; j++) {
				cellType = matrix[i][j];
				switch (cellType) {
					case CELL_TYPE_BLOCK:
						drawBlock(canvas, paint, i, j);
						break;
					case CELL_TYPE_ANIMATION: // yeah, just transfer down
					case CELL_TYPE_BLANK:
						// do nothing
						break;

				}
			}
		}
	}

	private void drawBlock(Canvas canvas, Paint paint, int rowIndex, int columnIndex) {
		paint.setColor(Color.parseColor("#FF2A3C50"));

		float x = columnIndex * blockWidth;
		float y = (rowIndex - 1) * blockHeight + moveHeightPerStep * moveStepCount;

		// canvas.drawRect(x, y, x + blockWidth, y + blockHeight, paint);
		canvas.drawRect(x, y, x + blockWidth, y + blockHeight - 3, paint);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		surfaceWidth = width;
		surfaceHeight = height;

		// TODO consider the gap between each block
		blockWidth = surfaceWidth / 4;
		blockHeight = blockWidth / 3;

		moveHeightPerStep = blockHeight / MOVE_STEP_PER_BLOCK;

		rowCount = (int) (surfaceHeight / blockHeight);
		matrix = new int[rowCount][columnCount];

		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < columnCount; j++) {
				matrix[i][j] = CELL_TYPE_BLANK;
			}

		}

		startDrawingThread();

		Log.d("jyj", "jyj surfaceChanged: " + width + " " + height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("jyj", "jyj surfaceCreated");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("jyj", "jyj surfaceDestroyed");
	}

	private void startDrawingThread() {
		stopDrawingThread();
		drawingThread = new DrawingThread();
		drawingThread.start();
	}

	private void stopDrawingThread() {
		if (drawingThread != null) {
			drawingThread.interrupt();

			try {
				drawingThread.join();
			} catch (InterruptedException e) {
			}

			drawingThread = null;
		}
	}

	private void cleanCanvas(Canvas canvas) {
		canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
	}

	private void onGameStart(){
		gameState = GAME_STATE_ONGOING;
		for (int i = 0; i < columnCount; i++) {
			bottomButtons[i].setClickable(true);
			bottomButtons[i].setText("");
		}
	}
	private void onGameOver() {
		gameState = GAME_STATE_OVER;
		for (int i = 0; i < columnCount; i++) {
			bottomButtons[i].setClickable(false);
		}


		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (currentScore > bestScore) {
					bestScore = currentScore;
					App.updateBestScore(GAME_TYPE, currentScore);
				}
				showAd();
				currentScoreTextView.setText(getString(R.string.current_score, currentScore));
				bestScoreTextView.setText(getString(R.string.best_score, bestScore));
				resultLayer.setVisibility(View.VISIBLE);
				bottomButtonsLayout.setVisibility(View.INVISIBLE);
			}
		});
	}

	private void showAd(){
		App.showInterstitialAd(this, adsWidgetContainer, App.AD_TAG);
	}
	
	private void onGetScore(){
		currentScore += SCORE_PER_ROW;
		scoreSprite.setCurrentScore(currentScore);
	}

	private class DrawingThread extends Thread {

		@Override
		public void run() {
			super.run();

			long startTime;
			int newCellType;

			float x, y, firstHitBlockBottomY;
			int xIndex, yIndex, firstHitBlockIndex;

			out: while (!Thread.interrupted()) {
				startTime = System.currentTimeMillis();
				
				Canvas canvas = surfaceHolder.lockCanvas();
				if (canvas == null) {
					continue;
				}

				try {
					cleanCanvas(canvas);

					if (gameState != GAME_STATE_ONGOING) {
						continue;
					}

					if (moveStepCount > MOVE_STEP_PER_BLOCK) {
						moveStepCount = 0;
					}

					if (moveStepCount == 0) {
						// TODO check game over
						for (int i = 0; i < columnCount; i++) {
							if (firstHitBlockIndexes[i] == rowCount - 1) {
								// TODO handle game over
								cleanCanvas(canvas);
								//resultLayer.draw(canvas);
								onGameOver();
								break out;
							}
						}

						newCellType = random.nextInt(4);
						addRow(newCellType);
						updateFirstHitBlockIndexes();
					}
					moveStepCount++;

					drawAllBlocks(canvas, globalPaint);

					synchronized (lock) {

						ListIterator<Sprite> iterator = sprites.listIterator();
						while (iterator.hasNext()) {
							Sprite sprite = (Sprite) iterator.next();
							if (sprite.isAlive()) {
								sprite.doDraw(canvas, globalPaint, 0);
							} else {
								if (sprite instanceof AnimationBlockSprite) {
									cleanAnimationRow();
									updateFirstHitBlockIndexes();
								}
								iterator.remove();
							}
						}

						// check hit or not
						iterator = sprites.listIterator();
						while (iterator.hasNext()) {
							Sprite sprite = iterator.next();
							if (sprite instanceof BlockSprite) {
								x = sprite.getX();
								y = sprite.getY();
								xIndex = (int) (x / blockWidth);

								firstHitBlockIndex = firstHitBlockIndexes[xIndex];
								firstHitBlockBottomY = firstHitBlockIndex * blockHeight + moveStepCount
										* moveHeightPerStep;

								if (firstHitBlockBottomY >= y) {

									// TODO need to check whether it hits the
									// most bottom block or not?
									// if so, game over
									if (firstHitBlockIndex == rowCount - 1) {
										// TODO handle game over
										cleanCanvas(canvas);
										//resultLayer.draw(canvas);
										onGameOver();
										break out;
									}
									// set row
									matrix[firstHitBlockIndex + 1][xIndex] = CELL_TYPE_BLOCK;
									updateFirstHitBlockIndexes();

									// Log.d("jyj", "jyj HIT");
									((BlockSprite) sprite).setHitted(true);
									// iterator.remove();
									int i = 0;
									for (i = 0; i < columnCount; i++) {
										if (matrix[firstHitBlockIndex + 1][i] != CELL_TYPE_BLOCK) {
											break;
										}
									}
									// the row is full
									if (i == columnCount) {
										// mark the animation row
										for (i = 0; i < columnCount; i++) {
											matrix[firstHitBlockIndex + 1][i] = CELL_TYPE_ANIMATION;
										}
										// add animation block
										AnimationBlockSprite animationBlockSprite = new AnimationBlockSprite(0,
												firstHitBlockBottomY, blockWidth * columnCount, blockHeight);
										animationBlockSprite.setSpeed(blockWidth / 5);
										iterator.add(animationBlockSprite);
										onGetScore();
									}

								}

							}

						}

					}
				} finally {
					//cleanCanvas(canvas);
					surfaceHolder.unlockCanvasAndPost(canvas);

				}

				long duration = System.currentTimeMillis() - startTime;
				if (duration < GAP) {
					try {
						Thread.sleep(GAP - duration);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}

			}

		}

	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			if(adsWidgetContainer.getVisibility() == View.VISIBLE){
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
		
	}

}
