package com.appzdigital.autocallrecoder.custom_ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.appzdigital.autocallrecoder.R;
import com.appzdigital.autocallrecoder.utils.BlurKit;
import com.appzdigital.autocallrecoder.utils.BlurKitException;

import java.lang.ref.WeakReference;

/**
 * The type Blur layout.
 */
public class BlurLayout extends FrameLayout {
	/**
	 * The constant DEFAULT_DOWNSCALE_FACTOR.
	 */
	public static final float DEFAULT_DOWNSCALE_FACTOR = 0.12f;
	/**
	 * The constant DEFAULT_BLUR_RADIUS.
	 */
	public static final int DEFAULT_BLUR_RADIUS = 12;
	/**
	 * The constant DEFAULT_FPS.
	 */
	public static final int DEFAULT_FPS = 60;
	/**
	 * The constant DEFAULT_CORNER_RADIUS.
	 */
	public static final float DEFAULT_CORNER_RADIUS = 0.f;
	/**
	 * The constant DEFAULT_ALPHA.
	 */
	public static final float DEFAULT_ALPHA = Float.NaN;
	private float mDownscaleFactor;
	private int mBlurRadius;
	private int mFPS;
	private float mCornerRadius;
	private float mAlpha;
	private boolean mRunning;
	private boolean mAttachedToWindow;
	private boolean mPositionLocked;
	private boolean mViewLocked;
	private RoundedImageView mImageView;
	private WeakReference<View> mActivityView;
	private Point mLockedPoint;
	private Bitmap mLockedBitmap;
	private Choreographer.FrameCallback invalidationLoop = new Choreographer.FrameCallback () {
		@Override
		public void doFrame (long frameTimeNanos) {
			invalidate ();
			Choreographer.getInstance ().postFrameCallbackDelayed (this, 1000 / mFPS);
		}
	};

	/**
	 * Instantiates a new Blur layout.
	 *
	 * @param context the context
	 */
	public BlurLayout (Context context) {
		super (context, null);
	}

	/**
	 * Instantiates a new Blur layout.
	 *
	 * @param context the context
	 * @param attrs   the attrs
	 */
	public BlurLayout (Context context, AttributeSet attrs) {
		super (context, attrs);
		if (!isInEditMode ()) {
			BlurKit.init (context);
		}
		TypedArray a = context.getTheme ().obtainStyledAttributes (
				attrs,
				R.styleable.BlurLayout,
				0, 0);
		try {
			mDownscaleFactor = a.getFloat (R.styleable.BlurLayout_blk_downscaleFactor, DEFAULT_DOWNSCALE_FACTOR);
			mBlurRadius = a.getInteger (R.styleable.BlurLayout_blk_blurRadius, DEFAULT_BLUR_RADIUS);
			mFPS = a.getInteger (R.styleable.BlurLayout_blk_fps, DEFAULT_FPS);
			mCornerRadius = a.getDimension (R.styleable.BlurLayout_blk_cornerRadius, DEFAULT_CORNER_RADIUS);
			mAlpha = a.getDimension (R.styleable.BlurLayout_blk_alpha, DEFAULT_ALPHA);
		} finally {
			a.recycle ();
		}
		mImageView = new RoundedImageView (getContext ());
		mImageView.setScaleType (ImageView.ScaleType.FIT_XY);
		addView (mImageView);
		setCornerRadius (mCornerRadius);
	}

	/**
	 * Start blur.
	 */
	public void startBlur () {
		if (mRunning) {
			return;
		}
		if (mFPS > 0) {
			mRunning = true;
			Choreographer.getInstance ().postFrameCallback (invalidationLoop);
		}
	}

	/**
	 * Pause blur.
	 */
	public void pauseBlur () {
		if (!mRunning) {
			return;
		}
		mRunning = false;
		Choreographer.getInstance ().removeFrameCallback (invalidationLoop);
	}

	@Override
	protected void onAttachedToWindow () {
		super.onAttachedToWindow ();
		mAttachedToWindow = true;
		startBlur ();
	}

	@Override
	protected void onDetachedFromWindow () {
		super.onDetachedFromWindow ();
		mAttachedToWindow = false;
		pauseBlur ();
	}

	@Override
	protected void onSizeChanged (int w, int h, int oldw, int oldh) {
		super.onSizeChanged (w, h, oldw, oldh);
		invalidate ();
	}

	@Override
	public void invalidate () {
		super.invalidate ();
		Bitmap bitmap = blur ();
		if (bitmap != null) {
			mImageView.setImageBitmap (bitmap);
		}
	}

	private Bitmap blur () {
		if (getContext () == null || isInEditMode ()) {
			return null;
		}
		if (mActivityView == null || mActivityView.get () == null) {
			mActivityView = new WeakReference<> (getActivityView ());
			if (mActivityView.get () == null) {
				return null;
			}
		}
		Point pointRelativeToActivityView;
		if (mPositionLocked) {
			if (mLockedPoint == null) {
				mLockedPoint = getPositionInScreen ();
			}
			pointRelativeToActivityView = mLockedPoint;
		} else {
			pointRelativeToActivityView = getPositionInScreen ();
		}
		super.setAlpha (0);
		int screenWidth = mActivityView.get ().getWidth ();
		int screenHeight = mActivityView.get ().getHeight ();
		int width = (int) (getWidth () * mDownscaleFactor);
		int height = (int) (getHeight () * mDownscaleFactor);
		int x = (int) (pointRelativeToActivityView.x * mDownscaleFactor);
		int y = (int) (pointRelativeToActivityView.y * mDownscaleFactor);
		int xPadding = getWidth () / 8;
		int yPadding = getHeight () / 8;
		int leftOffset = -xPadding;
		leftOffset = x + leftOffset >= 0 ? leftOffset : 0;
		int rightOffset = xPadding;
		rightOffset = x + screenWidth - rightOffset <= screenWidth ? rightOffset : screenWidth + screenWidth - x;
		int topOffset = -yPadding;
		topOffset = y + topOffset >= 0 ? topOffset : 0;
		int bottomOffset = yPadding;
		bottomOffset = y + getHeight () + bottomOffset <= screenHeight ? bottomOffset : 0;
		Bitmap bitmap;
		if (mViewLocked) {
			if (mLockedBitmap == null) {
				lockView ();
			}
			if (width == 0 || height == 0) {
				return null;
			}
			bitmap = Bitmap.createBitmap (mLockedBitmap, x, y, width, height);
		} else {
			try {
				bitmap = getDownscaledBitmapForView (
						mActivityView.get (),
						new Rect (
								pointRelativeToActivityView.x + leftOffset,
								pointRelativeToActivityView.y + topOffset,
								pointRelativeToActivityView.x + getWidth () + Math.abs (leftOffset) + rightOffset,
								pointRelativeToActivityView.y + getHeight () + Math.abs (topOffset) + bottomOffset
						),
						mDownscaleFactor
				);
			} catch (BlurKitException e) {
				return null;
			} catch (NullPointerException e) {
				return null;
			}
		}
		if (!mViewLocked) {
			bitmap = BlurKit.getInstance ().blur (bitmap, mBlurRadius);
			bitmap = Bitmap.createBitmap (
					bitmap,
					(int) (Math.abs (leftOffset) * mDownscaleFactor),
					(int) (Math.abs (topOffset) * mDownscaleFactor),
					width,
					height
			);
		}
		if (Float.isNaN (mAlpha)) {
			super.setAlpha (1);
		} else {
			super.setAlpha (mAlpha);
		}
		return bitmap;
	}

	private View getActivityView () {
		Activity activity;
		try {
			activity = (Activity) getContext ();
		} catch (ClassCastException e) {
			return null;
		}
		return activity.getWindow ().getDecorView ().findViewById (android.R.id.content);
	}

	private Point getPositionInScreen () {
		PointF pointF = getPositionInScreen (this);
		return new Point ((int) pointF.x, (int) pointF.y);
	}

	private PointF getPositionInScreen (View view) {
		if (getParent () == null) {
			return new PointF ();
		}
		ViewGroup parent;
		try {
			parent = (ViewGroup) view.getParent ();
		} catch (Exception e) {
			return new PointF ();
		}
		if (parent == null) {
			return new PointF ();
		}
		PointF point = getPositionInScreen (parent);
		point.offset (view.getX (), view.getY ());
		return point;
	}

	private Bitmap getDownscaledBitmapForView (View view, Rect crop, float downscaleFactor) throws BlurKitException, NullPointerException {
		View screenView = view.getRootView ();
		int width = (int) (crop.width () * downscaleFactor);
		int height = (int) (crop.height () * downscaleFactor);
		if (screenView.getWidth () <= 0 || screenView.getHeight () <= 0 || width <= 0 || height <= 0) {
			throw new BlurKitException ("No screen available (width or height = 0)");
		}
		float dx = -crop.left * downscaleFactor;
		float dy = -crop.top * downscaleFactor;
		Bitmap bitmap = Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas (bitmap);
		Matrix matrix = new Matrix ();
		matrix.preScale (downscaleFactor, downscaleFactor);
		matrix.postTranslate (dx, dy);
		canvas.setMatrix (matrix);
		screenView.draw (canvas);
		return bitmap;
	}

	/**
	 * Gets downscale factor.
	 *
	 * @return the downscale factor
	 */
	public float getDownscaleFactor () {
		return this.mDownscaleFactor;
	}

	/**
	 * Sets downscale factor.
	 *
	 * @param downscaleFactor the downscale factor
	 */
	public void setDownscaleFactor (float downscaleFactor) {
		this.mDownscaleFactor = downscaleFactor;
		this.mLockedBitmap = null;
		invalidate ();
	}

	/**
	 * Gets blur radius.
	 *
	 * @return the blur radius
	 */
	public int getBlurRadius () {
		return this.mBlurRadius;
	}

	/**
	 * Sets blur radius.
	 *
	 * @param blurRadius the blur radius
	 */
	public void setBlurRadius (int blurRadius) {
		this.mBlurRadius = blurRadius;
		this.mLockedBitmap = null;
		invalidate ();
	}

	/**
	 * Gets fps.
	 *
	 * @return the fps
	 */
	public int getFPS () {
		return this.mFPS;
	}

	/**
	 * Sets fps.
	 *
	 * @param fps the fps
	 */
	public void setFPS (int fps) {
		if (mRunning) {
			pauseBlur ();
		}
		this.mFPS = fps;
		if (mAttachedToWindow) {
			startBlur ();
		}
	}

	/**
	 * Gets corner radius.
	 *
	 * @return the corner radius
	 */
	public float getCornerRadius () {
		return mCornerRadius;
	}

	/**
	 * Sets corner radius.
	 *
	 * @param cornerRadius the corner radius
	 */
	public void setCornerRadius (float cornerRadius) {
		this.mCornerRadius = cornerRadius;
		if (mImageView != null) {
			mImageView.setCornerRadius (cornerRadius);
		}
		invalidate ();
	}

	public float getAlpha () {
		return mAlpha;
	}

	public void setAlpha (float alpha) {
		mAlpha = alpha;
		if (!mViewLocked) {
			super.setAlpha (mAlpha);
		}
	}

	/**
	 * Lock view.
	 */
	public void lockView () {
		mViewLocked = true;
		if (mActivityView != null && mActivityView.get () != null) {
			View view = mActivityView.get ().getRootView ();
			try {
				super.setAlpha (0f);
				mLockedBitmap = getDownscaledBitmapForView (view, new Rect (0, 0, view.getWidth (), view.getHeight ()), mDownscaleFactor);
				if (Float.isNaN (mAlpha)) {
					super.setAlpha (1);
				} else {
					super.setAlpha (mAlpha);
				}
				mLockedBitmap = BlurKit.getInstance ().blur (mLockedBitmap, mBlurRadius);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Unlock view.
	 */
	public void unlockView () {
		mViewLocked = false;
		mLockedBitmap = null;
	}

	/**
	 * Gets view locked.
	 *
	 * @return the view locked
	 */
	public boolean getViewLocked () {
		return mViewLocked;
	}

	/**
	 * Lock position.
	 */
	public void lockPosition () {
		mPositionLocked = true;
		mLockedPoint = getPositionInScreen ();
	}

	/**
	 * Unlock position.
	 */
	public void unlockPosition () {
		mPositionLocked = false;
		mLockedPoint = null;
	}

	/**
	 * Gets position locked.
	 *
	 * @return the position locked
	 */
	public boolean getPositionLocked () {
		return mPositionLocked;
	}
}
