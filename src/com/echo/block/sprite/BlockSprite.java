package com.echo.block.sprite;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class BlockSprite extends Sprite{
	
	private float speed;
	private float w;
	private float h;
	
	public static final int DIRECTION_UP = 0;
	public static final int DIRECTION_DOWN = 1;
	
	private int direction = DIRECTION_UP;
	
	private boolean isHitted = false;
	
	public static BlockSprite obtainInstance(float x, float y, float w, float h){
		return new BlockSprite(x, y, w, h);
	}
	
	private BlockSprite(float x, float y, float w, float h){
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	@Override
	public void doDraw(Canvas canvas, Paint globalPaint, int status) {
		globalPaint.setColor(Color.parseColor("#FF2A3C50"));

		if (direction == DIRECTION_UP) {
			y -= speed;
		}else {
			y += speed;
		}
		canvas.drawRect(x, y, x + w, y + h, globalPaint);
	}

	@Override
	public boolean isAlive() {
		return !isHitted;
	}

	@Override
	public boolean isHitted() {
		return isHitted;
	}

	@Override
	public int getScore() {
		return 0;
	}
	
	public void setHitted(boolean hitted){
		isHitted = hitted;
	}

	public void setSpeed(float speed){
		this.speed = speed;
	}
	
	public void setDirection(int direction){
		this.direction = direction;
	}
	
}
