package com.appzdigital.autocallrecoder.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

import com.appzdigital.autocallrecoder.R;
import com.appzdigital.autocallrecoder.envr.AppEnvr;
import com.appzdigital.autocallrecoder.receivers.TelephonyManagerPhoneStateReceiver;
import com.appzdigital.autocallrecoder.utils.AppUtil;

import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGD;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGE;

/**
 * The type Main service.
 */
public class MainService extends Service {
	private static final String TAG = MainService.class.getSimpleName ();
	private static final int FOREGROUND_NOTIFICATION_ID = 1;
	/**
	 * The constant sIsServiceRunning.
	 */
	public static boolean sIsServiceRunning = false;
	private final SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = (sharedPreferences, s) -> {
		if (sharedPreferences == null || s == null) {
			return;
		}
		LOGD (TAG, "Shared preference change listener - Shared preference changed");
		if (s.equals (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS)) {
			if (!sharedPreferences.contains (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS)) {
				SharedPreferences.Editor editor = sharedPreferences.edit ();
				editor.putBoolean (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS, true);
				editor.apply ();
			}
		}
		if (s.equals (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS)) {
			if (!sharedPreferences.contains (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS)) {
				SharedPreferences.Editor editor = sharedPreferences.edit ();
				editor.putBoolean (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS, true);
				editor.apply ();
			}
		}
		boolean recordIncomingCalls = sharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS, true);
		boolean recordOutgoingCalls = sharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS, true);
		if (!recordIncomingCalls && !recordOutgoingCalls) {
			try {
				stopSelf ();
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
	};
	private final TelephonyManagerPhoneStateReceiver mTelephonyManagerPhoneStateReceiver = new TelephonyManagerPhoneStateReceiver ();
	private NotificationManager mNotificationManager = null;
	@RequiresApi (api = Build.VERSION_CODES.O)
	private NotificationChannel mNotificationChannel = null;
	private PowerManager mPowerManager = null;
	private PowerManager.WakeLock mWakeLock = null;
	private SharedPreferences mSharedPreferences = null;

	@Override
	public IBinder onBind (Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		super.onStartCommand (intent, flags, startId);
		LOGD (TAG, "Service start command");
		return START_STICKY_COMPATIBILITY;
	}

	@Override
	public void onCreate () {
		super.onCreate ();
		LOGD (TAG, "Service create");
		sIsServiceRunning = true;
		try {
			mNotificationManager = (NotificationManager) getSystemService (NOTIFICATION_SERVICE);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mNotificationManager != null) {
			CharSequence contentTitle = "Running...", contentText = getString (R.string.app_name) + " is active.";
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				try {
					mNotificationChannel = new NotificationChannel (getString (R.string.service) + "-" + FOREGROUND_NOTIFICATION_ID, getString (R.string.service), NotificationManager.IMPORTANCE_NONE);
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
				if (mNotificationChannel != null) {
					try {
						mNotificationManager.createNotificationChannel (mNotificationChannel);
					} catch (Exception e) {
						LOGE (TAG, e.getMessage ());
						LOGE (TAG, e.toString ());
						e.printStackTrace ();
					}
					Icon logoIcon = Icon.createWithResource (this, R.drawable.ic_stat_name);
					Icon largeIcon = Icon.createWithResource (this, R.mipmap.ic_launcher);
					try {
						startForeground (FOREGROUND_NOTIFICATION_ID, new Notification.Builder (this, getString (R.string.service) + "-" + FOREGROUND_NOTIFICATION_ID)
								.setSmallIcon (logoIcon)
								.setLargeIcon (largeIcon)
								.setContentTitle (contentTitle)
								.setContentText (contentText)
								.build ());
					} catch (Exception e) {
						LOGE (TAG, e.getMessage ());
						LOGE (TAG, e.toString ());
						e.printStackTrace ();
					}
				}
			} else {
				Notification.Builder builder = new Notification.Builder (this);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					Icon logoIcon = Icon.createWithResource (this, R.drawable.ic_stat_name);
					Icon largeIcon = Icon.createWithResource (this, R.mipmap.ic_launcher);
					builder.setSmallIcon (logoIcon);
					builder.setLargeIcon (largeIcon);
				} else {
					builder.setSmallIcon (R.drawable.ic_stat_name);
				}
				builder.setContentTitle (contentTitle);
				builder.setContentText (contentText);
				builder.setOngoing (true);
				try {
					mNotificationManager.notify (FOREGROUND_NOTIFICATION_ID, builder.build ());
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
		}
		try {
			mPowerManager = (PowerManager) getSystemService (POWER_SERVICE);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mPowerManager != null) {
			try {
				mWakeLock = mPowerManager.newWakeLock (PowerManager.PARTIAL_WAKE_LOCK, getString (R.string.app_name));
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			if (mWakeLock != null) {
				AppUtil.acquireWakeLock (mWakeLock);
			}
		}
		try {
			mSharedPreferences = getSharedPreferences (getString (R.string.app_name), Context.MODE_PRIVATE);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mSharedPreferences != null) {
			try {
				mSharedPreferences.registerOnSharedPreferenceChangeListener (mOnSharedPreferenceChangeListener);
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
		try {
			registerReceiver (mTelephonyManagerPhoneStateReceiver, new IntentFilter (TelephonyManager.ACTION_PHONE_STATE_CHANGED));
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
	}

	@Override
	public void onDestroy () {
		super.onDestroy ();
		LOGD (TAG, "Service destroy");
		try {
			unregisterReceiver (mTelephonyManagerPhoneStateReceiver);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mSharedPreferences != null) {
			try {
				mSharedPreferences.unregisterOnSharedPreferenceChangeListener (mOnSharedPreferenceChangeListener);
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			mSharedPreferences = null;
		}
		if (mPowerManager != null) {
			if (mWakeLock != null) {
				AppUtil.releaseWakeLock (mWakeLock);
				mWakeLock = null;
			}
			mPowerManager = null;
		}
		if (mNotificationManager != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				if (mNotificationChannel != null) {
					try {
						stopForeground (true);
					} catch (Exception e) {
						LOGE (TAG, e.getMessage ());
						LOGE (TAG, e.toString ());
						e.printStackTrace ();
					}
					try {
						mNotificationManager.deleteNotificationChannel (mNotificationChannel.getId ());
					} catch (Exception e) {
						LOGE (TAG, e.getMessage ());
						LOGE (TAG, e.toString ());
						e.printStackTrace ();
					}
					mNotificationChannel = null;
				}
			} else {
				try {
					mNotificationManager.cancel (FOREGROUND_NOTIFICATION_ID);
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
			mNotificationManager = null;
		}
		sIsServiceRunning = false;
	}
}
