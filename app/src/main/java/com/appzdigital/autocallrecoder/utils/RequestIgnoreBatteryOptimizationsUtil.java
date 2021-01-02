package com.appzdigital.autocallrecoder.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGE;

/**
 * The type Request ignore battery optimizations util.
 */
public class RequestIgnoreBatteryOptimizationsUtil {
	private static final String TAG = RequestIgnoreBatteryOptimizationsUtil.class.getSimpleName ();

	/**
	 * Gets request ignore battery optimizations intent.
	 *
	 * @param context the context
	 * @return the request ignore battery optimizations intent
	 */
	@Nullable
	@RequiresApi (api = Build.VERSION_CODES.M)
	@RequiresPermission (Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
	@SuppressLint ("BatteryLife")
	public static Intent getRequestIgnoreBatteryOptimizationsIntent (@NonNull final Context context) {
		try {
			final String scheme = "package", schemeSpecificPart = context.getPackageName ();
			if (schemeSpecificPart != null) {
				final Uri uri = Uri.fromParts (scheme, schemeSpecificPart, null);
				if (uri != null) {
					if (uri.getScheme () != null && uri.getScheme ().equals (scheme)
							&& uri.getSchemeSpecificPart () != null && uri.getSchemeSpecificPart ().equals (schemeSpecificPart)) {
						final Intent requestIgnoreBatteryOptimizationsIntent = new Intent (Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
						requestIgnoreBatteryOptimizationsIntent.setData (uri);
						return requestIgnoreBatteryOptimizationsIntent;
					}
				}
			}
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		return null;
	}
}
