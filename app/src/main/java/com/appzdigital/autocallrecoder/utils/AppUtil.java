package com.appzdigital.autocallrecoder.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.appzdigital.autocallrecoder.services.MainService;

import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGD;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGE;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGW;

/**
 * The type App util.
 */
public class AppUtil {
	private static final String TAG = AppUtil.class.getSimpleName ();

	/**
	 * Start main service.
	 *
	 * @param context the context
	 */
	public static void startMainService (@NonNull final Context context) {
		if (MainService.sIsServiceRunning) {
			LOGW (TAG, "Will not start \"MainService\", it is running");
			return;
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			try {
				context.startForegroundService (new Intent (context, MainService.class));
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		} else {
			try {
				context.startService (new Intent (context, MainService.class));
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
	}

	/**
	 * Stop main service.
	 *
	 * @param context the context
	 */
	public static void stopMainService (@NonNull final Context context) {
		if (!MainService.sIsServiceRunning) {
			LOGW (TAG, "Will not stop \"MainService\", it is not running");
			return;
		}
		try {
			context.stopService (new Intent (context, MainService.class));
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
	}

	/**
	 * Acquire wake lock.
	 *
	 * @param wakeLock the wake lock
	 * @param timeout  the timeout
	 */
	@RequiresPermission (Manifest.permission.WAKE_LOCK)
	public static void acquireWakeLock (@NonNull final PowerManager.WakeLock wakeLock, final long timeout) {
		LOGD (TAG, "Trying to acquire wake lock with timeout...");
		try {
			wakeLock.acquire (timeout);
		} catch (Exception e) {
			LOGE (TAG, "Exception while trying to acquire wake lock with timeout");
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		try {
			if (wakeLock.isHeld ()) {
				LOGD (TAG, "Wake lock acquired");
				return;
			} else {
				LOGW (TAG, "Wake lock not acquired");
				return;
			}
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		LOGW (TAG, "Wake lock not acquired");
	}

	/**
	 * Acquire wake lock.
	 *
	 * @param wakeLock the wake lock
	 */
	@SuppressLint ("WakelockTimeout")
	@RequiresPermission (Manifest.permission.WAKE_LOCK)
	public static void acquireWakeLock (@NonNull final PowerManager.WakeLock wakeLock) {
		LOGD (TAG, "Trying to acquire wake lock without timeout...");
		try {
			wakeLock.acquire ();
		} catch (Exception e) {
			LOGE (TAG, "Exception while trying to acquire wake lock without timeout");
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		try {
			if (wakeLock.isHeld ()) {
				LOGD (TAG, "Wake lock acquired");
				return;
			} else {
				LOGW (TAG, "Wake lock not acquired");
				return;
			}
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		LOGD (TAG, "Wake lock not acquired");
	}

	/**
	 * Release wake lock.
	 *
	 * @param wakeLock the wake lock
	 */
	@RequiresPermission (Manifest.permission.WAKE_LOCK)
	public static void releaseWakeLock (@NonNull final PowerManager.WakeLock wakeLock) {
		LOGD (TAG, "Trying to release wake lock...");
		try {
			wakeLock.release ();
		} catch (Exception e) {
			LOGE (TAG, "Exception while trying to release wake lock");
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		try {
			if (wakeLock.isHeld ()) {
				LOGW (TAG, "Wake lock not released");
				return;
			} else {
				LOGD (TAG, "Wake lock released");
				return;
			}
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		LOGD (TAG, "Wake lock not released");
	}

	/**
	 * Open package in market.
	 *
	 * @param context the context
	 */
	public static void openPackageInMarket (@NonNull final Context context) {
		try {
			context.startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse ("market://details?id=" + context.getPackageName ())));
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
			try {
				context.startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse ("https://play.google.com/store/apps/details?id=" + context.getPackageName ())));
			} catch (Exception ex) {
				LOGE (TAG, ex.getMessage ());
				LOGE (TAG, ex.toString ());
				ex.printStackTrace ();
			}
		}
	}
}
