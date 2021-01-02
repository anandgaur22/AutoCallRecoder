package com.appzdigital.autocallrecoder.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGE;

/**
 * The type Resource util.
 */
public class ResourceUtil {
	private static final String TAG = ResourceUtil.class.getSimpleName ();

	/**
	 * Gets drawable.
	 *
	 * @param context the context
	 * @param id      the id
	 * @return the drawable
	 */
	public static Drawable getDrawable (@NonNull final Context context, final int id) {
		Drawable drawable = null;
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				drawable = context.getDrawable (id);
			} else {
				drawable = ContextCompat.getDrawable (context, id);
			}
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (drawable == null) {
			try {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					drawable = context.getResources ().getDrawable (id, null);
				} else {
					drawable = context.getResources ().getDrawable (id);
				}
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
		return drawable;
	}

	/**
	 * Gets color.
	 *
	 * @param context the context
	 * @param id      the id
	 * @return the color
	 */
	public static int getColor (@NonNull final Context context, final int id) {
		int color = -1;
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				color = context.getColor (id);
			} else {
				color = ContextCompat.getColor (context, id);
			}
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (color == -1) {
			try {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					color = context.getResources ().getColor (id, null);
				} else {
					color = context.getResources ().getColor (id);
				}
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
		return color;
	}

	/**
	 * Gets font.
	 *
	 * @param context the context
	 * @param id      the id
	 * @return the font
	 */
	public static Typeface getFont (@NonNull final Context context, final int id) {
		Typeface font = null;
		try {
			font = ResourcesCompat.getFont (context, id);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (font == null) {
			try {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					font = context.getResources ().getFont (id);
				}
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
		return font;
	}

	/**
	 * Gets bitmap clipped circle.
	 *
	 * @param bitmap the bitmap
	 * @return the bitmap clipped circle
	 */
	public static Bitmap getBitmapClippedCircle (@NonNull final Bitmap bitmap) {
		int width = bitmap.getWidth (), height = bitmap.getHeight ();
		Bitmap outputBitmap = Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);
		Path path = new Path ();
		path.addCircle ((float) (width / 2), (float) (height / 2), (float) Math.min (width, (height / 2)), Path.Direction.CCW);
		Canvas canvas = new Canvas (outputBitmap);
		canvas.clipPath (path);
		canvas.drawBitmap (bitmap, 0, 0, null);
		return outputBitmap;
	}
}
