package com.appzdigital.autocallrecoder.utils;

import android.util.Log;

import static com.appzdigital.autocallrecoder.RecoderApp.LOGGING_ENABLED;
import static com.appzdigital.autocallrecoder.RecoderApp.LOG_PREFIX;
import static com.appzdigital.autocallrecoder.RecoderApp.LOG_PREFIX_LENGTH;
import static com.appzdigital.autocallrecoder.RecoderApp.MAX_LOG_TAG_LENGTH;

/**
 * The type Log utils.
 */
public class LogUtils {
	private static final String TAG = makeLogTag (LogUtils.class);

	private LogUtils () {
	}

	/**
	 * Make log tag string.
	 *
	 * @param str the str
	 * @return the string
	 */
	public static String makeLogTag (String str) {
		if (str.length () > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
			return LOG_PREFIX + str.substring (0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
		}
		return LOG_PREFIX + str;
	}

	/**
	 * Make log tag string.
	 *
	 * @param cls the cls
	 * @return the string
	 */
	public static String makeLogTag (Class cls) {
		return makeLogTag (cls.getSimpleName ());
	}

	/**
	 * Logd.
	 *
	 * @param tag     the tag
	 * @param message the message
	 */
	public static void LOGD (final String tag, String message) {
		if (LOGGING_ENABLED) {
			if (Log.isLoggable (tag, Log.DEBUG)) {
				Log.d (tag, message);
			}
		}
	}

	/**
	 * Logd.
	 *
	 * @param tag     the tag
	 * @param message the message
	 * @param cause   the cause
	 */
	public static void LOGD (final String tag, String message, Throwable cause) {
		if (LOGGING_ENABLED) {
			if (Log.isLoggable (tag, Log.DEBUG)) {
				Log.d (tag, message, cause);
			}
		}
	}

	/**
	 * Logv.
	 *
	 * @param tag     the tag
	 * @param message the message
	 */
	public static void LOGV (final String tag, String message) {
		if (LOGGING_ENABLED) {
			if (Log.isLoggable (tag, Log.VERBOSE)) {
				Log.v (tag, message);
			}
		}
	}

	/**
	 * Logv.
	 *
	 * @param tag     the tag
	 * @param message the message
	 * @param cause   the cause
	 */
	public static void LOGV (final String tag, String message, Throwable cause) {
		if (LOGGING_ENABLED) {
			if (Log.isLoggable (tag, Log.VERBOSE)) {
				Log.v (tag, message, cause);
			}
		}
	}

	/**
	 * Logi.
	 *
	 * @param tag     the tag
	 * @param message the message
	 */
	public static void LOGI (final String tag, String message) {
		if (LOGGING_ENABLED) {
			Log.i (tag, message);
		}
	}

	/**
	 * Logi.
	 *
	 * @param tag     the tag
	 * @param message the message
	 * @param cause   the cause
	 */
	public static void LOGI (final String tag, String message, Throwable cause) {
		if (LOGGING_ENABLED) {
			Log.i (tag, message, cause);
		}
	}

	/**
	 * Logw.
	 *
	 * @param tag     the tag
	 * @param message the message
	 */
	public static void LOGW (final String tag, String message) {
		if (LOGGING_ENABLED) {
			Log.w (tag, message);
		}
	}

	/**
	 * Logw.
	 *
	 * @param tag     the tag
	 * @param message the message
	 * @param cause   the cause
	 */
	public static void LOGW (final String tag, String message, Throwable cause) {
		if (LOGGING_ENABLED) {
			Log.w (tag, message, cause);
		}
	}

	/**
	 * Loge.
	 *
	 * @param tag     the tag
	 * @param message the message
	 */
	public static void LOGE (final String tag, String message) {
		if (LOGGING_ENABLED) {
			Log.e (tag, message);
		}
	}

	/**
	 * Loge.
	 *
	 * @param tag     the tag
	 * @param message the message
	 * @param cause   the cause
	 */
	public static void LOGE (final String tag, String message, Throwable cause) {
		if (LOGGING_ENABLED) {
			Log.e (tag, message, cause);
		}
	}
}
