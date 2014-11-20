package com.echo.block.sprite;

import android.graphics.Canvas;
import android.graphics.Paint;

public abstract class Sprite {

	public static final String TAG = "Sprite";
	public static final int STATUS_NORMAL = 0;
	public static final int STATUS_NOT_STARTED = 1;
	public static final int STATUS_GAME_OVER = 2;
	
	float x, y;
	
	public abstract void doDraw(Canvas canvas, Paint globalPaint, int status) ;
	public abstract boolean isAlive();
	public abstract boolean isHitted();
	public abstract int getScore();
	
	public void setX(float x){
		this.x = x;
	}
	
	public float getX(){
		return x;
	}
	
	public void setY(float y){
		this.y = y;
	}
	
	public float getY(){
		return y;
	}

}
