package com.mlt.factorytest.customview;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.mlt.factorytest.R;
import com.mlt.factorytest.item.TouchPanel;
/**
 * 
 * file name:TouchPanelView.java
 * Copyright MALATA ,ALL rights reserved
 * 2015-3-13
 * author:laiyang
 * Modification history
 * -------------------------------------
 * modified by laiyang
 * modification time 2015-3-13
 * JIRA NO.SXTASK-13
 * 
 * -------------------------------------
 */
public class TouchPanelView extends View implements 
	View.OnTouchListener, View.OnClickListener {
	// The text size of hint text
	private final int HINT_TEXT_SIZE = 50;
	// every side's squares number
	private final int SQUARE_NUM = 12;
	// windows width and height
	int width;
	int height;
	int step = 50;
	// touch point now
	float touchX;
	float touchY;
	// pre touch point
	float preX;
	float preY;
	// current touch sum
	int currentTch;
	int touchSum;
	// n of map
	int mProcess; 
	private Canvas mBackCanvas;
	private Bitmap mBitmap;
	private Paint mPaint;
	private Paint mPaintLine;
	private Paint mPaintText;
	private ArrayList<CustomRect> mRects;
	// list of Parallelograms
	private ArrayList<Parallelogram> mParallelograms;
	// each square side
	private double side;
	// if isNext equals true,means view will change to next map
	private boolean isNext;
	// if isFinish equals true,means view will finish and send Toast message
	private boolean isFinish;
	// true draw touch path
	private boolean isDrawPath;
	// true draw touch path
	private boolean isShowText;
	// parent view's instance
	private TouchPanel mTP;
	/**
	 * set parent view's instance ,
	 * use to send message to handler to finish current test 
	 * @date 2015-2-10 pm 4:29:42
	 * @param tp parent view's instance
	 */
	public void setTouchPanel(TouchPanel tp) {
		mTP = tp;
	}
	
	public void init() {
		setOnTouchListener(this);
		width = getResources().getDisplayMetrics().widthPixels;
		height = getResources().getDisplayMetrics().heightPixels;
		step = width / SQUARE_NUM;
		mBitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		isDrawPath = true;
		mBackCanvas = new Canvas();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
//		mPaint.setStrokeWidth(8f);
		mPaint.setStrokeWidth(4f);
		mPaint.setColor(Color.WHITE);
		mPaint.setStyle(Style.STROKE);
		// init paint to 
		mPaintLine = new Paint();
		mPaintLine.setAntiAlias(true);
		mPaintLine.setStrokeWidth(4f);
		mPaintLine.setStyle(Style.STROKE);
		// init paint to draw hint text
		mPaintText = new Paint();
		mPaintText.setColor(Color.WHITE);
		mPaintText.setTextSize(HINT_TEXT_SIZE);
		
		mBackCanvas.setBitmap(mBitmap);
		
		initMap();
		mProcess = 1;
		touchSum = mRects.size();
	}
	/**
	 * init rectangle arrays position 
	 */
	private void initRect() {
		mRects = new ArrayList<CustomRect>();
		int numW = width/step;
		int numH = height/step;
//		int residueH = height%step;
		int residueW = width%step;
		for(int i = 0; i <= numW-1;i++) {
			for(int j = 0; j <= numH-1; j++) {
				if(i == 0 || j == 0 || i == numW-1 || j == numH-1) {
					if(i == numW-1 && j == numH-1) {
						mRects.add(new CustomRect(new Rect(i*step, j*step,
								(i+1)*step+residueW, height - 10), false));//(j+1)*step+residueH
						continue;
					}
					if(j == numH-1) {
						mRects.add(new CustomRect(new Rect(i*step, j*step,
								(i+1)*step, height - 10), false));//(j+1)*step+residueH
						continue;
					}
					if(i == numW - 1){
						mRects.add(new CustomRect(new Rect(i*step, j*step,
								(i+1)*step+residueW, (j+1)*step), false));
						continue;
					}	
					mRects.add(new CustomRect(new Rect(i*step, j*step,
							(i+1)*step, (j+1)*step), false));
				}
			}
		} 
	}
	/**
	 * init trapezoid array's position 
	 * 
	 */
	private void initTrapezoid() {
		mPaintLine.setColor(Color.RED);
		double sideSum = Math.sqrt(width*width+height*height);
		double angle = Math.acos(height/sideSum);
		double side = step / Math.cos(angle);
		this.side = side;
		int wSum = (int) (sideSum/side);
		int hSum = (int) (height / step);

		mParallelograms = new ArrayList<Parallelogram>();
		Parallelogram t = null;
		// left up corner
		mParallelograms.add(new Parallelogram(-step,0,-step,step,(int)(Math.sin(angle)*side),0,
				(int)side, step,false));
		// right up corner
		t = new Parallelogram((int)(wSum*(Math.sin(angle)*side)),0,
				(int)((wSum-1)*(Math.sin(angle)*side)),step,
				width+step, 0,
				width+step,step,false);
		mParallelograms.add(t);
		int temp1 = 1;
		int temp2 = 1;
		int temp3 = 1;
		int temp4 = hSum-1;
		for(int w = 0; w < wSum; w++ ) {
			for(int h = 0; h < hSum; h++) {
				if((w == 0 && h == 0) || (h == 0 && w == wSum - 1)) {
						continue;
				} 
				// right down corner fill screen
				if(h == hSum - 1 && w == wSum - 1) {
					t = new Parallelogram((int)((w-1)*Math.sin(angle)*side),(int)(step*h),
							(int)((w+1)*Math.sin(angle)*side),(int)(step*(h+2)),
							(int)(Math.sin(angle)*side+w*Math.sin(angle)*side),(int)(step*h),
							(int)(side + (w+1)*Math.sin(angle)*side), step*(h+2),
							false);
			 		temp1++;
					temp2++;
					mParallelograms.add(t);
					t = null;
					continue;
				}
				// left down corner fill screen
				if(h == hSum - 1 && w == 1) {
					t = new Parallelogram((int)(w*(Math.sin(angle)*side)),step*h,
							(int)((w-2)*(Math.sin(angle)*side)),step*(h+2),
							(int)(w*(Math.sin(angle)*side)+side), step*h,
							(int)((w-2)*(Math.sin(angle)*side)+side),step*(h+2)
							,false);
			 		temp3++;
					temp4--;
					mParallelograms.add(t);
					t = null;
					continue;
				}
			 	if(w == temp1 && h == temp2) {
			 		t = new Parallelogram((int)((w-1)*Math.sin(angle)*side),(int)(step*h),
							(int)(w*Math.sin(angle)*side),(int)(step*(h+1)),
							(int)(Math.sin(angle)*side+w*Math.sin(angle)*side),(int)(step*h),
							(int)(side + w*Math.sin(angle)*side), step*(h+1),
							false);
			 		temp1++;
					temp2++;
					mParallelograms.add(t);
					t = null;
				}
				if(w == temp3 && h == temp4) {
					t = new Parallelogram((int)(w*(Math.sin(angle)*side)),step*h,
							(int)((w-1)*(Math.sin(angle)*side)),step*(h+1),
							(int)(w*(Math.sin(angle)*side)+side), step*h,
							(int)((w-1)*(Math.sin(angle)*side)+side),step*(h+1)
							,false);
			 		temp3++;
					temp4--;
					mParallelograms.add(t);
					t = null;
				}
			}
		}
	}
	
	private void initMap() {
		initRect();
		initTrapezoid();
	}
	
	
	public TouchPanelView(Context context) {
		super(context);
		init();
	}

	public TouchPanelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TouchPanelView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width,  
                View.MeasureSpec.EXACTLY);  
        heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height,  
                View.MeasureSpec.EXACTLY);  
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	/**
	 * draw this view
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.BLACK);
		drawTouchPanel(mBackCanvas);
		if(isDrawPath) {
			if(!(preX == touchX && preY == touchY)) {
				mBackCanvas.drawLine(preX, preY, touchX, touchY, mPaint);
			}	
		} else if(!isDrawPath && isShowText) {
			canvas.drawText(getContext().getString(R.string.tp_test_pass_hint), 100, 100, mPaintText);
		}
		canvas.drawBitmap(mBitmap, 0, 0, mPaint);
		super.onDraw(canvas);
	}

	private void drawTouchPanel(Canvas canvas) {
		if(mProcess == 1) {
			for(CustomRect rect : mRects) {
				rect.draw(canvas, mPaintLine);
			}
		} else {
			for(Parallelogram p : mParallelograms) {
				p.draw(canvas, mPaintLine);
			}
		}
	}
	/**
	 * when touch screen, get event X,Y position and judge the point whether in test area
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		preX = touchX;
		preY = touchY;
		touchX = event.getX();
		touchY = event.getY();
		switch(action) {
		case MotionEvent.ACTION_DOWN:
			preX = touchX;
			preY = touchY;
			isTouch();
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			isTouch();
			invalidate();
			break;
		}
		return true;
	}
	/**
	 * judge the square wether touched
	 * @date 2015-2-10 pm 4:51:49
	 */
	private void isTouch() {
		if(mProcess == 1) {
			for(CustomRect r:mRects) {
				if(r.isTouch) { //if touched, continue
					continue;
				}
				if(r.inSide((int)touchX, (int)touchY)) {
					r.isTouch = true;
					currentTch++;
				}
			}
		} else {
			for(Parallelogram t:mParallelograms) {
				if(t.isTouch) { //if touched, continue
					continue;
				}
				if(t.inSide((int)touchX, (int)touchY)) {
					t.isTouch = true;
					currentTch++;
				}
			}
		}
		if(currentTch == touchSum) {
			if(mProcess ==  1 ) {
				if(!isNext) {// step to next test
					isNext = true;
					isShowText = true;
					isDrawPath = false;
					setOnTouchListener(null);
					setOnClickListener(this);
				}
			} else {// finish Touch panel test
				if(!isFinish) {
					isFinish = true;
					isShowText = false;
					isDrawPath = false;
					setOnTouchListener(null);
					mTP.mHandler.sendEmptyMessage(TouchPanel.MSG_STEP_TO_FINISH);
				}
			}
		}
	}
	
	public void stepToNext() {
		isDrawPath = true;
		setOnTouchListener(this);
		mProcess++;
		currentTch = 0;
		touchSum = mParallelograms.size();
		if(!mBitmap.isRecycled()) {
			mBitmap.recycle();
			mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			mBackCanvas.setBitmap(mBitmap);
		}
		touchX = 0;
		touchY = 0;
		preX = 0;
		preY = 0;
		postInvalidate();
	}
	/**
	 * when finish test, recycle bitmap
	 * @date 2015-4-1 pm 2:18:16
	 */
	public void stepToFinish() {
		if(!mBitmap.isRecycled()) {
			mBitmap.recycle();
		}
	}
	/**
	 * Parallelogram
	 */
	class Parallelogram {
		final static int polySides = 4;
		int x1;
		int y1;
		int x2;
		int y2;
		int x3;
		int y3;
		int x4;
		int y4;
		boolean isTouch;
		float polyY[] = new float[4];
		float polyX[] = new float[4];
		
		public Parallelogram(int x1, int y1, int x2, int y2, int x3, int y3,
				int x4, int y4, boolean isTouch) {
			polyX[0] = this.x1 = x1;
			polyY[0] = this.y1 = y1;
			polyX[3] = this.x2 = x2;
			polyY[3] = this.y2 = y2;
			polyX[1] = this.x3 = x3;
			polyY[1] = this.y3 = y3;
			polyX[2] = this.x4 = x4;
			polyY[2] = this.y4 = y4;
			
			this.isTouch = isTouch;
		}
		/**
		 * draw parallelogram
		 * @date 2015-4-1 pm 2:14:39
		 * @param canvas
		 * @param paint
		 */
		public void draw(Canvas canvas, Paint paint) {
			
			if(isTouch) {
				paint.setColor(Color.GREEN);
			} else {
				paint.setColor(Color.RED);
			}
			canvas.drawLine(x1, y1, x2, y2, paint);
			canvas.drawLine(x1, y1, x3, y3, paint);
			canvas.drawLine(x3, y3, x4, y4, paint);
			canvas.drawLine(x2, y2, x4, y4, paint);
		}
		/**
		 * modified:  laiyang
		 * modify time: 2015-03-13
		 * JIRA NO. SXTASK-13
		 * modify content: delete algorithm before,add new algorithm to jude the point is in polygon 
		 */
		public boolean inSide(float x, float y) {
			int i,j= polySides - 1;
			boolean oddNodes = false;
			for(i = 0; i < polySides; i++) {
				if((polyY[i] < y && polyY[j] >= y || polyY[j] < y && polyY[i] >= y)
						&& (polyX[i] <= x || polyX[j] <= x)) {
					if(polyX[i]+(y-polyY[i])/(polyY[j]-polyY[i])*(polyX[j]-polyX[i])<x) {
						oddNodes = !oddNodes;
					}
				}
				j = i;
			}
			return oddNodes;
		}
	}
	/**
	 * Custom Rectangle
	 *
	 */
	class CustomRect {
		Rect rect;
		boolean isTouch;
		
		public CustomRect(Rect rect, boolean isTouch) {
			this.rect = rect;
			this.isTouch = isTouch;
		}
		
		public void draw(Canvas canvas, Paint paint) {
			if(isTouch) {
				paint.setColor(Color.GREEN);
			} else {
				paint.setColor(Color.RED);
			}
			canvas.drawRect(rect, paint);
		}
		/**
		 * @param x
		 * @param y
		 * @return
		 */
		public boolean inSide(int x, int y) {
			return rect.contains(x, y);
		}
	}
	/*
	 * when click screen,step to next test
	 * (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if(mProcess == 1) {
			isShowText = false;
			setOnClickListener(null);
			stepToNext();
			postInvalidate();
		}
	}
}

