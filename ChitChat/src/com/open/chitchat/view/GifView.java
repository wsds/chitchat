package com.open.chitchat.view;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class GifView extends View {

	private Movie mMovie;
	private static final int DEFAULT_MOVIE_DURATION = 1000;
	private float mScale, mLeft, mTop;
	private int mMeasuredMovieWidth, mMeasuredMovieHeight, mCurrentAnimationTime = 0;
	private boolean mVisible = true, mPaused;

	private long mMovieStart = 0;

	public GifView(Context context) {
		super(context);
	}

	public GifView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GifView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setGifPath(String gifPath) {
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(gifPath), 16 * 1024);
			is.mark(16 * 1024);
			if (is != null) {
				mMovie = Movie.decodeStream(is);
			}
			is.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		requestLayout();
		invalidate();
	}

	public void setPaused(boolean paused) {
		this.mPaused = paused;
		if (!paused) {
			mMovieStart = android.os.SystemClock.uptimeMillis() - mCurrentAnimationTime;
		}
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mMovie != null) {
			int movieWidth = mMovie.width();
			int movieHeight = mMovie.height();
			int maximumWidth = MeasureSpec.getSize(widthMeasureSpec);
			float scaleW = (float) movieWidth / (float) maximumWidth;
			mScale = 1f / scaleW;
			mMeasuredMovieWidth = maximumWidth;
			mMeasuredMovieHeight = (int) (movieHeight * mScale);
			setMeasuredDimension(mMeasuredMovieWidth, mMeasuredMovieHeight);
		} else {
			setMeasuredDimension(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mLeft = (getWidth() - mMeasuredMovieWidth) / 2f;
		mTop = (getHeight() - mMeasuredMovieHeight) / 2f;
		mVisible = getVisibility() == View.VISIBLE;

	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mMovie != null) {
			if (!mPaused) {
				updateAnimationTime();
				drawMovieFrame(canvas);
				invalidateView();
			} else {
				drawMovieFrame(canvas);
			}
		}
	}

	@SuppressLint("NewApi")
	private void invalidateView() {
		if (mVisible) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				postInvalidateOnAnimation();
			} else {
				invalidate();
			}
		}
	}

	private void updateAnimationTime() {
		long now = android.os.SystemClock.uptimeMillis();
		if (mMovieStart == 0) {
			mMovieStart = now;
		}
		int dur = mMovie.duration();
		if (dur == 0) {
			dur = DEFAULT_MOVIE_DURATION;
		}
		mCurrentAnimationTime = (int) ((now - mMovieStart) % dur);
	}

	private void drawMovieFrame(Canvas canvas) {
		mMovie.setTime(mCurrentAnimationTime);
		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.scale(mScale, mScale);
		mMovie.draw(canvas, mLeft / mScale, mTop / mScale);
		canvas.restore();
	}

	@SuppressLint("NewApi")
	@Override
	public void onScreenStateChanged(int screenState) {
		super.onScreenStateChanged(screenState);
		mVisible = screenState == SCREEN_STATE_ON;
		invalidateView();
	}

	@Override
	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		mVisible = visibility == View.VISIBLE;
		invalidateView();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		mVisible = visibility == View.VISIBLE;
		invalidateView();
	}

}
