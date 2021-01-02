package com.appzdigital.autocallrecoder.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

/**
 * The type Blur kit.
 */
public class BlurKit {
	private static final float FULL_SCALE = 1f;
	private static BlurKit instance;
	private static RenderScript rs;

	/**
	 * Init.
	 *
	 * @param context the context
	 */
	public static void init (Context context) {
		if (instance != null) {
			return;
		}
		instance = new BlurKit ();
		rs = RenderScript.create (context.getApplicationContext ());
	}

	/**
	 * Gets instance.
	 *
	 * @return the instance
	 */
	public static BlurKit getInstance () {
		if (instance == null) {
			throw new RuntimeException ("BlurKit not initialized!");
		}
		return instance;
	}

	/**
	 * Blur bitmap.
	 *
	 * @param src    the src
	 * @param radius the radius
	 * @return the bitmap
	 */
	public Bitmap blur (Bitmap src, int radius) {
		final Allocation input = Allocation.createFromBitmap (rs, src);
		final Allocation output = Allocation.createTyped (rs, input.getType ());
		final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create (rs, Element.U8_4 (rs));
		script.setRadius (radius);
		script.setInput (input);
		script.forEach (output);
		output.copyTo (src);
		return src;
	}

	/**
	 * Blur bitmap.
	 *
	 * @param src    the src
	 * @param radius the radius
	 * @return the bitmap
	 */
	public Bitmap blur (View src, int radius) {
		Bitmap bitmap = getBitmapForView (src);
		return blur (bitmap, radius);
	}

	/**
	 * Fast blur bitmap.
	 *
	 * @param src             the src
	 * @param radius          the radius
	 * @param downscaleFactor the downscale factor
	 * @return the bitmap
	 */
	public Bitmap fastBlur (View src, int radius, float downscaleFactor) {
		Bitmap bitmap = getBitmapForView (src, downscaleFactor);
		return blur (bitmap, radius);
	}

	private Bitmap getBitmapForView (View src, float downscaleFactor) {
		Bitmap bitmap = Bitmap.createBitmap (
				(int) (src.getWidth () * downscaleFactor),
				(int) (src.getHeight () * downscaleFactor),
				Bitmap.Config.ARGB_8888
		);
		Canvas canvas = new Canvas (bitmap);
		Matrix matrix = new Matrix ();
		matrix.preScale (downscaleFactor, downscaleFactor);
		canvas.setMatrix (matrix);
		src.draw (canvas);
		return bitmap;
	}

	private Bitmap getBitmapForView (View src) {
		Bitmap bitmap = Bitmap.createBitmap (
				src.getWidth (),
				src.getHeight (),
				Bitmap.Config.ARGB_8888
		);
		Canvas canvas = new Canvas (bitmap);
		src.draw (canvas);
		return bitmap;
	}
}
