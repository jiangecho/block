package com.echo.block.sprite;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.style.TextAppearanceSpan;

public class ScoreSprite extends Sprite{

	private int score = 0;
	private float textSize = 10;

	@Override
	public void doDraw(Canvas canvas, Paint globalPaint, int status) {
		globalPaint.setTextSize(textSize);
		globalPaint.setColor(Color.GREEN);
		canvas.drawText("" + score, x, y, globalPaint);
	}

	@Override
	public boolean isAlive() {
		return true;
	}

	@Override
	public boolean isHitted() {
		return false;
	}

	@Override
	public int getScore() {
		return 0;
	}
	
	public void setCurrentScore(int score){
		this.score = score;
	}
	
	public int getCurrentScore(){
		return score;
	}
	
	public void setTextSize(float textSize){
		this.textSize = textSize;
	}

}
