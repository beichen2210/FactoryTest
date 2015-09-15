package com.mlt.factorytest.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
/**
 * 
 * file name:GSensorOriView.java
 * Copyright MALATA ,ALL rights reserved
 * 2015-1-27
 * author:laiyang
 * 
 * custom view, show status of device's ori test
 * contains six ori (up,down,left,right,positive,negative)
 * 
 * Modification history
 * -------------------------------------
 * 
 * -------------------------------------
 */
public class GSensorOriView extends View {
	/**
	 * if moriTestSum == ORI_SUM ,means this test is success
	 */
	public static final int ORI_SUM = 6;
	
	/**
	 * the color of ori judge success and fail
	 */
	private final int SUCCESS_COLOR = Color.GREEN;
	private final int DEFAULT_COLOR = Color.GRAY;
	/**
	 * ori-positive
	 */
	public boolean isPositive = false;
	/**
	 * ori-negative
	 */
	public boolean isNegative = false;
	/**
	 * ori-left
	 */
	public boolean isLeft = false;
	/**
	 * ori-right
	 */
	public boolean isRight = false;
	/**
	 * ori-up
	 */
	public boolean isUp = false;
	/**
	 * ori-down
	 */
	public boolean isDown = false;
	/**
	 * the height of view
	 */
	private int view_height;
	/**
	 * the width of view
	 */
	private int view_width;
	/**
	 * up triangle's points
	 */
	private Point[] upTriangle = new Point[3];
	/**
	 * down triangle's points
	 */
	private Point[] downTriangle = new Point[3];
	/**
	 * left triangle's points
	 */
	private Point[] leftTriangle = new Point[3];
	/**
	 * right triangle's points
	 */
	private Point[] rightTriangle = new Point[3];
	/**
	 * positive circle's points (top,left,right)
	 */
	private Point[] positiveCircle = new Point[3];
	/**
	 * negative circle's points (bottom,left,right)
	 */
	private Point[] negativeCircle = new Point[3];
	/**
	 * paint
	 */
	Paint mPaint = new Paint();
	/**
	 * path
	 */
	Path mPath = new Path();
	/**
	 * backup canvas
	 */
	private Canvas mBackCanvas;
	/**
	 * backup bitmap
	 */
	private Bitmap mBitmap;
	
	private int mOriTestSum;
	
	public void addmOriTestSum() {
		mOriTestSum++;
	}

	public int getmOriTestSum() {
		return mOriTestSum;
	}

	/**
	 * init points
	 */
	public void init(){
		clearViewState();
		for(int i = 0 ;i < 3; i++) {
			upTriangle[i] = new Point();
			downTriangle[i] = new Point();
			leftTriangle[i] = new Point();
			rightTriangle[i] = new Point();
			positiveCircle[i] = new Point();
			negativeCircle[i] = new Point();
		}
	}
	
	public GSensorOriView(Context context) {
		super(context);
		init();
	}
	
	public GSensorOriView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public GSensorOriView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	/**
	 * measure view and create back canvas and bitmap
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		view_height = MeasureSpec.getSize(heightMeasureSpec);
		view_width = MeasureSpec.getSize(widthMeasureSpec);
		measureView();
		if(mBitmap == null && view_height!=0 && view_width != 0) {
			mBitmap = Bitmap.createBitmap(view_width, view_height, Bitmap.Config.ARGB_4444);
			mBackCanvas = new Canvas();
			mBackCanvas.setBitmap(mBitmap);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
	}
	/**
	 * init the view's every point
	 * @date 2015-4-1 am 11:14:35
	 */
	private void measureView() {
		upTriangle[0].x = (int)(7f/20*view_width);
		upTriangle[0].y = (int)(3f/10*view_height);
		upTriangle[1].x = view_width/2;
		upTriangle[1].y = 0;
		upTriangle[2].x = view_width - (int)(7f/20*view_width);
		upTriangle[2].y = (int)(3f/10*view_height);
		
		downTriangle[0].x = (int)(7f/20*view_width);
		downTriangle[0].y = view_height - (int)(3f/10*view_height);
		downTriangle[1].x = view_width/2;
		downTriangle[1].y = view_height;
		downTriangle[2].x = view_width - (int)(7f/20*view_width);
		downTriangle[2].y = view_height - (int)(3f/10*view_height);
		
		leftTriangle[0].x = (int)(3f/10*view_width);
		leftTriangle[0].y = (int)(7f/20*view_height);
		leftTriangle[1].x = 0;
		leftTriangle[1].y = view_height/2;
		leftTriangle[2].x = (int)(3f/10*view_width);
		leftTriangle[2].y = view_height - (int)(7f/20*view_height);
		
		rightTriangle[0].x = view_width-(int)(3f/10*view_width);
		rightTriangle[0].y = (int)(7f/20*view_height);
		rightTriangle[1].x = view_width;
		rightTriangle[1].y = view_height/2;
		rightTriangle[2].x = view_width-(int)(3f/10*view_width);
		rightTriangle[2].y = view_height-(int)(7f/20*view_height);
		
		positiveCircle[0].x = (int)(7f/20*view_width);
		positiveCircle[0].y = view_height/2;
		positiveCircle[1].x = view_width/2;
		positiveCircle[1].y = (int)(7f/20*view_height);
		positiveCircle[2].x = view_width-(int)(7f/20*view_width);
		positiveCircle[2].y = view_height/2;

		negativeCircle[0].x = (int)(7f/20*view_width);
		negativeCircle[0].y = view_height/2;
		negativeCircle[1].x = view_width/2;
		negativeCircle[1].y = view_height-(int)(7f/20*view_height);
		negativeCircle[2].x = view_width-(int)(7f/20*view_width);
		negativeCircle[2].y = view_height/2;
		
	}
	/**
	 * draw this view
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		drawTriangle();
		drawCircle();
		canvas.drawBitmap(mBitmap, 0, 0, mPaint);
		super.onDraw(canvas);
	}
	/**
	 * draw triangle ,(up,down,right,left)
	 * if one ori test success, draw color is SUCCESS_COLOR ,or draw color is DEFAULT_COLOR
	 * @date 2015-4-1 am 11:15:27
	 */
	private void drawTriangle() {
		// draw up triangle
		if(isUp) {
			mPaint.setColor(SUCCESS_COLOR);
		} else {
			mPaint.setColor(DEFAULT_COLOR);
		}
		mPath.moveTo(upTriangle[0].x, upTriangle[0].y);
		mPath.lineTo(upTriangle[1].x, upTriangle[1].y);
		mPath.lineTo(upTriangle[2].x, upTriangle[2].y);
		mPath.close();
		mBackCanvas.drawPath(mPath, mPaint);
		mPath.reset();
		// draw down triangle
		if(isDown) {
			mPaint.setColor(SUCCESS_COLOR);
		} else {
			mPaint.setColor(DEFAULT_COLOR);
		}
		mPath.moveTo(downTriangle[0].x, downTriangle[0].y);
		mPath.lineTo(downTriangle[1].x, downTriangle[1].y);
		mPath.lineTo(downTriangle[2].x, downTriangle[2].y);
		mPath.close();
		mBackCanvas.drawPath(mPath, mPaint);
		mPath.reset();
		// draw left triangle
		if(isLeft) {
			mPaint.setColor(SUCCESS_COLOR);
		} else {
			mPaint.setColor(DEFAULT_COLOR);
		}
		mPath.moveTo(leftTriangle[0].x, leftTriangle[0].y);
		mPath.lineTo(leftTriangle[1].x, leftTriangle[1].y);
		mPath.lineTo(leftTriangle[2].x, leftTriangle[2].y);
		mPath.close();
		mBackCanvas.drawPath(mPath, mPaint);
		mPath.reset();
		// draw right triangle
		if(isRight) {
			mPaint.setColor(SUCCESS_COLOR);
		} else {
			mPaint.setColor(DEFAULT_COLOR);
		}
		mPath.moveTo(rightTriangle[0].x, rightTriangle[0].y);
		mPath.lineTo(rightTriangle[1].x, rightTriangle[1].y);
		mPath.lineTo(rightTriangle[2].x, rightTriangle[2].y);
		mPath.close();
		mBackCanvas.drawPath(mPath, mPaint);
	}
	/**
	 * show two half circle to test device's oritation
	 * if test success, draw color is SUCCESS_COLOR,else draw color is DEFAULT_COLOR
	 * @date 2015-4-1 am 11:15:51
	 */
	private void drawCircle() {
		// draw positive half circle
		if(isPositive) {
			mPaint.setColor(SUCCESS_COLOR);
		} else {
			mPaint.setColor(DEFAULT_COLOR);
		}
		mBackCanvas.drawArc(new RectF(positiveCircle[0].x, 
				positiveCircle[1].y, positiveCircle[2].x, negativeCircle[1].y), 
				180, 180, true, mPaint);
		mPaint.setColor(DEFAULT_COLOR);
		// draw negative half circle
		if(isNegative) {
			mPaint.setColor(SUCCESS_COLOR);
		} else {
			mPaint.setColor(DEFAULT_COLOR);
		}
		mBackCanvas.drawArc(new RectF(negativeCircle[0].x, 
				positiveCircle[1].y, negativeCircle[2].x, negativeCircle[1].y), 
				0, 180, true, mPaint);
	}
	/**
	 * the class is a java been
	 * is to means screen's one ponit x position and y position
	 */
	class Point { 
		int x;
		int y;
	}
	/**
	 * clear view state
	 * reset test status to wait test
	 */
	public void clearViewState() {
		isPositive = false;
		isNegative = false;
		isLeft = false;
		isRight = false;
		isUp = false;
		isDown = false;
		mOriTestSum = 0;
	}
}
