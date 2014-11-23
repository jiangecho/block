package com.echo.block.sprite;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class AnimationBlockSprite extends Sprite{
	
	/**
	 * the animation starts from the left side
	 */
	public static final int TYPE_LEFT_ANIMATION_BLOCK = 0;
	
	/**
	 * the animation starts from right side
	 */
	public static final int TYPE_RIGHT_ANIMATION_BLOCK = 1;

	private int type = TYPE_LEFT_ANIMATION_BLOCK;
	
	private float speed;
	
	private float w, h;
	
	public AnimationBlockSprite(float x, float y, float w, float h){
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	@Override
	public void doDraw(Canvas canvas, Paint globalPaint, int status) {
		switch (type) {
			case TYPE_LEFT_ANIMATION_BLOCK:
				x += speed;
				w -= speed;
				break;
			case TYPE_RIGHT_ANIMATION_BLOCK:
				x -= speed;
				w -= speed;
				break;
		}
		globalPaint.setColor(Color.parseColor("#FF2A3C50"));
		canvas.drawRect(x, y, x + w, y + h, globalPaint);

	}

	@Override
	public boolean isAlive() {
		return w > 0 ? true : false;
	}

	@Override
	public boolean isHitted() {
		return false;
	}

	@Override
	public int getScore() {
		return 0;
	}
	
	public void setSpeed(float speed){
		this.speed = speed;
	}
	
	public void setType(int type){
		this.type = type;
	}

}
