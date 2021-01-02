package com.appzdigital.autocallrecoder.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.appzdigital.autocallrecoder.R;
import com.appzdigital.autocallrecoder.config.AppConfig;
import com.appzdigital.autocallrecoder.config.CallRecorderConfig;
import com.appzdigital.autocallrecoder.envr.AppEnvr;
import com.appzdigital.autocallrecoder.models.CallObject;

import java.io.File;
import java.util.Date;

import io.realm.Realm;

import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGD;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGE;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGI;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGW;

/**
 * The type Call recorder service.
 */
public class CallRecorderService extends Service {
	private static final String TAG = CallRecorderService.class.getSimpleName ();
	private static final int FOREGROUND_NOTIFICATION_ID = 2;
	/**
	 * The constant sIsServiceRunning.
	 */
	public static boolean sIsServiceRunning = false;
	private final MediaRecorder.OnInfoListener mMediaRecorderOnInfoListener = (mr, what, extra) -> {
		LOGD (TAG, "Media recorder info");
		switch (what) {
			case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
				LOGI (TAG, "Media recorder info: Unknown");
				break;
			case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
				LOGI (TAG, "Media recorder info: Max duration reached");
				break;
			case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
				LOGI (TAG, "Media recorder info: Max filesize reached");
				break;
			case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING:
				LOGI (TAG, "Media recorder info: Max filesize approaching");
				break;
			case MediaRecorder.MEDIA_RECORDER_INFO_NEXT_OUTPUT_FILE_STARTED:
				LOGI (TAG, "Media recorder info: Next output file started");
				break;
		}
		LOGD (TAG, "Media recorder info extra: " + extra);
	};
	private final MediaRecorder.OnErrorListener mMediaRecorderOnErrorListener = (mr, what, extra) -> {
		LOGD (TAG, "Media recorder error");
		switch (what) {
			case MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN:
				LOGW (TAG, "Media recorder error: Unknown");
				break;
			case MediaRecorder.MEDIA_ERROR_SERVER_DIED:
				LOGW (TAG, "Media error: Server died");
				break;
		}
		LOGD (TAG, "Media recorder error extra: " + extra);
	};
	private Realm mRealm = null;
	private NotificationManager mNotificationManager = null;
	@RequiresApi (api = Build.VERSION_CODES.O)
	private NotificationChannel mNotificationChannel = null;
	@RequiresPermission (Manifest.permission.READ_PHONE_STATE)
	private TelephonyManager mTelephonyManager = null;
	@RequiresPermission (Manifest.permission.VIBRATE)
	private Vibrator mVibrator = null;
	@RequiresPermission (Manifest.permission.MODIFY_AUDIO_SETTINGS)
	private AudioManager mAudioManager = null;
	private SharedPreferences mPreferenceManagerSharedPreferences = null;
	private boolean mIsIncoming = false;
	private boolean mIsOutgoing = false;
	private String mPhoneStateIncomingNumber = null;
	@RequiresPermission (Manifest.permission.RECORD_AUDIO)
	private MediaRecorder mMediaRecorder = null;
	private boolean mVibrate = true, mTurnOnSpeaker = false, mMaxUpVolume = true;
	private int mVoiceCallStreamVolume = -1;
	private CallObject mIncomingCallObject = null;
	private CallObject mOutgoingCallObject = null;
	private boolean mFavourit = false;

	@Override
	public IBinder onBind (Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand (Intent intent, int flags, int startId) {
		super.onStartCommand (intent, flags, startId);
		LOGD (TAG, "Service start command");
		if (intent != null) {
			try {
				mPreferenceManagerSharedPreferences = PreferenceManager.getDefaultSharedPreferences (this);
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			if (intent.hasExtra (AppEnvr.INTENT_ACTION_INCOMING_CALL)) {
				mIsIncoming = intent.getBooleanExtra (AppEnvr.INTENT_ACTION_INCOMING_CALL, false);
				LOGE (TAG, "mIsIncoming " + mIsIncoming);
			}
			if (intent.hasExtra (AppEnvr.INTENT_ACTION_OUTGOING_CALL)) {
				mIsOutgoing = intent.getBooleanExtra (AppEnvr.INTENT_ACTION_OUTGOING_CALL, false);
				LOGE (TAG, "mIsOutgoing " + mIsOutgoing);
			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			} else {
				if (intent.hasExtra (TelephonyManager.EXTRA_INCOMING_NUMBER)) {
					mPhoneStateIncomingNumber = intent.getStringExtra (TelephonyManager.EXTRA_INCOMING_NUMBER);
					LOGE (TAG, "mPhoneStateIncomingNumber " + mPhoneStateIncomingNumber);
					Realm realmf = null;
					try {
						realmf = Realm.getDefaultInstance ();
					} catch (Exception e) {
						e.printStackTrace ();
					}
					if (realmf != null && !realmf.isClosed ()) {
						try {
							realmf.beginTransaction ();
							CallObject object = realmf.where (CallObject.class)
									.equalTo ("mPhoneNumber", mPhoneStateIncomingNumber)
									.findFirst ();
							if (object != null) {
								mFavourit = object.isFavourit ();
								realmf.commitTransaction ();
							} else {
								realmf.cancelTransaction ();
							}
							realmf.close ();
						} catch (Exception e) {
							e.printStackTrace ();
						}
					}
					if (!mPhoneStateIncomingNumber.trim ().isEmpty ()) {
						LOGI (TAG, "Phone state incoming number: " + mPhoneStateIncomingNumber);
					}
				}
			}
			if (mIsIncoming || mIsOutgoing) {
				if (ContextCompat.checkSelfPermission (this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
					int audioSource = CallRecorderConfig.CALL_RECORDER_DEFAULT_AUDIO_SOURCE;
					int outputFormat = CallRecorderConfig.CALL_RECORDER_DEFAULT_OUTPUT_FORMAT;
					int audioEncoder = CallRecorderConfig.CALL_RECORDER_DEFAULT_AUDIO_ENCODER;
					LOGE (TAG, "beginRecorder");
					beginRecorder (audioSource, outputFormat, audioEncoder);
				} else {
					try {
						LOGE (TAG, "stopSelf() 1");
						stopSelf ();
					} catch (Exception e) {
						LOGE (TAG, e.getMessage ());
						LOGE (TAG, e.toString ());
						e.printStackTrace ();
					}
				}
			} else {
				try {
					LOGE (TAG, "stopSelf() 2");
					stopSelf ();
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
		}
		return START_STICKY_COMPATIBILITY;
	}

	@Override
	public void onCreate () {
		super.onCreate ();
		LOGD (TAG, "Service create");
		sIsServiceRunning = true;
		try {
			mRealm = Realm.getDefaultInstance ();
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		try {
			mNotificationManager = (NotificationManager) getSystemService (Context.NOTIFICATION_SERVICE);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mNotificationManager != null) {
			CharSequence contentTitle = "Recording...", contentText = "Call recording is currently in progress.";
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
		if (ContextCompat.checkSelfPermission (this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
			try {
				mTelephonyManager = (TelephonyManager) getSystemService (Context.TELEPHONY_SERVICE);
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
		if (ContextCompat.checkSelfPermission (this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
			try {
				mVibrator = (Vibrator) getSystemService (Context.VIBRATOR_SERVICE);
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
		if (ContextCompat.checkSelfPermission (this, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
			try {
				mAudioManager = (AudioManager) getSystemService (Context.AUDIO_SERVICE);
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
	}

	@Override
	public void onDestroy () {
		if (ContextCompat.checkSelfPermission (this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
			endRecorder ();
		}
		if (mPhoneStateIncomingNumber != null) {
			mPhoneStateIncomingNumber = null;
		}
		if (mIsOutgoing) {
			mIsOutgoing = false;
		}
		if (mIsIncoming) {
			mIsIncoming = false;
		}
		if (mPreferenceManagerSharedPreferences != null) {
			mPreferenceManagerSharedPreferences = null;
		}
		super.onDestroy ();
		LOGD (TAG, "Service destroy");
		if (mAudioManager != null) {
			mAudioManager = null;
		}
		if (mVibrator != null) {
			mVibrator = null;
		}
		if (mTelephonyManager != null) {
			mTelephonyManager = null;
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
		if (mRealm != null) {
			if (!mRealm.isClosed ()) {
				try {
					mRealm.close ();
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
			mRealm = null;
		}
		sIsServiceRunning = false;
	}

	private boolean prepare () {
		if (mMediaRecorder != null) {
			LOGD (TAG, "Trying to prepare media recorder...");
			try {
				mMediaRecorder.prepare ();
				LOGI (TAG, "Prepare OK");
				return true;
			} catch (Exception e) {
				LOGE (TAG, "Exception while trying to prepare media recorder");
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		} else {
			LOGW (TAG, "Cannot prepare media recorder when it is null");
		}
		return false;
	}

	private boolean start () {
		if (mMediaRecorder != null) {
			LOGD (TAG, "Trying to start media recorder...");
			try {
				mMediaRecorder.start ();
				LOGI (TAG, "Start OK");
				return true;
			} catch (Exception e) {
				LOGE (TAG, "Exception while trying to prepare media recorder");
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		} else {
			LOGW (TAG, "Cannot start media recorder when it is null");
		}
		return false;
	}

	@RequiresApi (api = Build.VERSION_CODES.N)
	private boolean resume () {
		if (mMediaRecorder != null) {
			LOGD (TAG, "Trying to resume media recorder...");
			try {
				mMediaRecorder.resume ();
				LOGI (TAG, "Resume OK");
				return true;
			} catch (Exception e) {
				LOGE (TAG, "Exception while trying to resume media recorder");
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		} else {
			LOGW (TAG, "Cannot resume media recorder when it is null");
		}
		return false;
	}

	@RequiresApi (api = Build.VERSION_CODES.N)
	private boolean pause () {
		if (mMediaRecorder != null) {
			LOGD (TAG, "Trying to pause media recorder...");
			try {
				mMediaRecorder.pause ();
				LOGI (TAG, "Pause OK");
				return true;
			} catch (Exception e) {
				LOGE (TAG, "Exception while trying to pause media recorder");
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		} else {
			LOGW (TAG, "Cannot pause media recorder when it is null");
		}
		return false;
	}

	private boolean stop () {
		if (mMediaRecorder != null) {
			LOGD (TAG, "Trying to stop media recorder...");
			try {
				mMediaRecorder.stop ();
				LOGI (TAG, "Stop OK");
				return true;
			} catch (Exception e) {
				LOGE (TAG, "Exception while trying to stop media recorder");
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		} else {
			LOGW (TAG, "Cannot stop media recorder when it is null");
		}
		return false;
	}

	private boolean reset () {
		if (mMediaRecorder != null) {
			LOGD (TAG, "Trying to reset media recorder...");
			try {
				mMediaRecorder.reset ();
				LOGI (TAG, "Reset OK");
				return true;
			} catch (Exception e) {
				LOGE (TAG, "Exception while trying to reset media recorder");
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		} else {
			LOGW (TAG, "Cannot reset media recorder when it is null");
		}
		return false;
	}

	private boolean release () {
		if (mMediaRecorder != null) {
			LOGD (TAG, "Trying to release media recorder...");
			try {
				mMediaRecorder.release ();
				LOGI (TAG, "Release OK");
				return true;
			} catch (Exception e) {
				LOGE (TAG, "Exception while trying to release media recorder");
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		} else {
			LOGW (TAG, "Cannot release media recorder when it is null");
		}
		return false;
	}

	/**
	 * Begin recorder.
	 *
	 * @param audioSource  the audio source
	 * @param outputFormat the output format
	 * @param audioEncoder the audio encoder
	 */
	@SuppressLint ("HardwareIds")
	@RequiresPermission (Manifest.permission.RECORD_AUDIO)
	public void beginRecorder (@Nullable Integer audioSource, @Nullable Integer outputFormat, @Nullable Integer audioEncoder) {
		if (mMediaRecorder != null) {
			return;
		}
		long beginTimestamp = new Date ().getTime ();
		if (mPreferenceManagerSharedPreferences != null) {
			if (audioSource == null) {
				audioSource = Integer.valueOf (mPreferenceManagerSharedPreferences.getString (AppEnvr.FM_SP_AUDIO_SOURCE,
						String.valueOf (CallRecorderConfig.CALL_RECORDER_DEFAULT_AUDIO_SOURCE)));
			}
			if (outputFormat == null) {
				outputFormat = Integer.valueOf (mPreferenceManagerSharedPreferences.getString (AppEnvr.FM_SP_OUTPUT_FORMAT,
						String.valueOf (CallRecorderConfig.CALL_RECORDER_DEFAULT_OUTPUT_FORMAT)));
			}
			if (audioEncoder == null) {
				audioEncoder = Integer.valueOf (mPreferenceManagerSharedPreferences.getString (AppEnvr.FM_SP_AUDIO_ENCODER,
						String.valueOf (CallRecorderConfig.CALL_RECORDER_DEFAULT_AUDIO_ENCODER)));
			}
			mVibrate = mPreferenceManagerSharedPreferences.getBoolean (AppEnvr.FM_SP_VIBRATE, true);
			mTurnOnSpeaker = mPreferenceManagerSharedPreferences.getBoolean (AppEnvr.FM_SP_TURN_ON_SPEAKER, false);
			mMaxUpVolume = mPreferenceManagerSharedPreferences.getBoolean (AppEnvr.FM_SP_MAX_UP_VOLUME, true);
		}
		if (mMaxUpVolume) {
			if (mAudioManager != null) {
				try {
					mVoiceCallStreamVolume = mAudioManager.getStreamVolume (AudioManager.STREAM_VOICE_CALL);
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
		}
		if (audioSource == null) {
			audioSource = CallRecorderConfig.CALL_RECORDER_DEFAULT_AUDIO_SOURCE;
		}
		if (outputFormat == null) {
			outputFormat = CallRecorderConfig.CALL_RECORDER_DEFAULT_OUTPUT_FORMAT;
		}
		if (audioEncoder == null) {
			audioEncoder = CallRecorderConfig.CALL_RECORDER_DEFAULT_AUDIO_ENCODER;
		}
		String type = "-";
		if (mIsIncoming) {
			type = "-I--";
		}
		if (mIsOutgoing) {
			type = "--0-";
		}
		String valueExternal = getResources ().getStringArray (R.array.records_output_location_entry_values)[ 0 ];
		String valueInternal = getResources ().getStringArray (R.array.records_output_location_entry_values)[ 1 ];
		String recordsOutputDirectoryPath = null;
		if (mPreferenceManagerSharedPreferences != null) {
			if (mPreferenceManagerSharedPreferences.contains (AppEnvr.FM_SP_KEY_RECORDS_OUTPUT_LOCATION)) {
				String recordsOutputLocation = mPreferenceManagerSharedPreferences.getString (AppEnvr.FM_SP_KEY_RECORDS_OUTPUT_LOCATION, valueExternal);
				if (recordsOutputLocation.equals (valueExternal)) {
					recordsOutputDirectoryPath = AppEnvr.sExternalFilesDirPathMemory;
				}
				if (recordsOutputLocation.equals (valueInternal)) {
					recordsOutputDirectoryPath = AppEnvr.sFilesDirPathMemory;
				}
			}
		}
		if (recordsOutputDirectoryPath == null) {
			recordsOutputDirectoryPath = AppEnvr.sExternalFilesDirPathMemory;
		}
		String outputFilePath = recordsOutputDirectoryPath + File.separator + mPhoneStateIncomingNumber + type + beginTimestamp;
		LOGE (TAG, "outputFilePath " + outputFilePath);
		try {
			mMediaRecorder = new MediaRecorder ();
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mMediaRecorder == null) {
			try {
				LOGE (TAG, "stopSelf() 3");
				stopSelf ();
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			return;
		}
		mMediaRecorder.setOnInfoListener (mMediaRecorderOnInfoListener);
		mMediaRecorder.setOnErrorListener (mMediaRecorderOnErrorListener);
		mMediaRecorder.setAudioSource (audioSource);
		mMediaRecorder.setOutputFormat (outputFormat);
		mMediaRecorder.setAudioEncoder (audioEncoder);
		mMediaRecorder.setOutputFile (outputFilePath);
		boolean prepare = prepare ();
		boolean start = start ();
		boolean succeed = prepare && start;
		if (!succeed) {
			if (!prepare) {
				LOGW (TAG, "Media recorder (telephony) has prepare exception");
			}
			if (!start) {
				LOGW (TAG, "Media recorder (telephony) has start exception");
			}
			for (int otherAudioSource : CallRecorderConfig.getAudioSources ()) {
				if (otherAudioSource == audioSource) {
					continue;
				}
				audioSource = otherAudioSource;
				reset ();
				mMediaRecorder.setOnInfoListener (mMediaRecorderOnInfoListener);
				mMediaRecorder.setOnErrorListener (mMediaRecorderOnErrorListener);
				mMediaRecorder.setAudioSource (audioSource);
				mMediaRecorder.setOutputFormat (outputFormat);
				mMediaRecorder.setAudioEncoder (audioEncoder);
				mMediaRecorder.setOutputFile (outputFilePath);
				boolean otherPrepare = prepare ();
				boolean otherStart = start ();
				if (otherPrepare && otherStart) {
					succeed = true;
					break;
				} else {
					if (!otherPrepare) {
						LOGW (TAG, "Media recorder (telephony) has other prepare exception");
					}
					if (!otherStart) {
						LOGW (TAG, "Media recorder (telephony) has other start exception");
					}
				}
			}
		}
		if (!succeed) {
			try {
				LOGE (TAG, "stopSelf() 4");
				stopSelf ();
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			return;
		}
		if (mVibrate) {
			if (ContextCompat.checkSelfPermission (this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
				if (mVibrator != null) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						try {
							mVibrator.vibrate (VibrationEffect.createOneShot (AppConfig.BEGIN_RECORDER_VIBE_TIME, VibrationEffect.DEFAULT_AMPLITUDE));
						} catch (Exception e) {
							LOGE (TAG, e.getMessage ());
							LOGE (TAG, e.toString ());
							e.printStackTrace ();
						}
					} else {
						try {
							mVibrator.vibrate (AppConfig.BEGIN_RECORDER_VIBE_TIME);
						} catch (Exception e) {
							LOGE (TAG, e.getMessage ());
							LOGE (TAG, e.toString ());
							e.printStackTrace ();
						}
					}
				}
			}
		}
		if (ContextCompat.checkSelfPermission (this, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
			if (mAudioManager != null) {
				if (mTurnOnSpeaker) {
					try {
						if (!mAudioManager.isSpeakerphoneOn ()) {
							mAudioManager.setSpeakerphoneOn (true);
						}
					} catch (Exception e) {
						LOGE (TAG, e.getMessage ());
						LOGE (TAG, e.toString ());
						e.printStackTrace ();
					}
				}
				if (mMaxUpVolume) {
					if (mVoiceCallStreamVolume != -1) {
						try {
							mAudioManager.setStreamVolume (AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamMaxVolume (AudioManager.STREAM_VOICE_CALL), 0);
						} catch (Exception e) {
							LOGE (TAG, e.getMessage ());
							LOGE (TAG, e.toString ());
							e.printStackTrace ();
						}
					}
				}
			}
		}
		String simSerialNumber = null;
		String simOperator = null;
		String simOperatorName = null;
		String simCountryIso = null;
		String networkOperator = null;
		String networkOperatorName = null;
		String networkCountryIso = null;
		TelephonyManager telephonyManager = null;
		try {
			telephonyManager = (TelephonyManager) getSystemService (TELEPHONY_SERVICE);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (telephonyManager != null) {
			try {
				simSerialNumber = telephonyManager.getSimSerialNumber ();
				LOGI (TAG, "SIM Serial Number: " + simSerialNumber);
				simOperator = telephonyManager.getSimOperator ();
				simOperatorName = telephonyManager.getSimOperatorName ();
				simCountryIso = telephonyManager.getSimCountryIso ();
				LOGI (TAG, "SIM Operator: " + simOperator);
				LOGI (TAG, "SIM Operator Name: " + simOperatorName);
				LOGI (TAG, "SIM Country ISO: " + simCountryIso);
				networkOperator = telephonyManager.getNetworkOperator ();
				networkOperatorName = telephonyManager.getNetworkOperatorName ();
				networkCountryIso = telephonyManager.getNetworkCountryIso ();
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
		if (mRealm != null && !mRealm.isClosed ()) {
			final String finalSimSerialNumber = simSerialNumber;
			final String finalSimOperator = simOperator;
			final String finalSimOperatorName = simOperatorName;
			final String finalSimCountryIso = simCountryIso;
			final String finalNetworkOperator = networkOperator;
			final String finalNetworkOperatorName = networkOperatorName;
			final String finalNetworkCountryIso = networkCountryIso;
			final int finalAudioSource = audioSource;
			final int finalOutputFormat = outputFormat;
			final int finalAudioEncoder = audioEncoder;
			final String finalOutputFilePath = outputFilePath;
			try {
				if (mIsIncoming) {
					mRealm.executeTransaction (realm -> {
						mIncomingCallObject = realm.createObject (CallObject.class);
						LOGE (TAG, "inc mPhoneStateIncomingNumber " + mPhoneStateIncomingNumber);
						LOGE (TAG, "inc beginTimestamp" + beginTimestamp);
						if (mIncomingCallObject != null) {
							mIncomingCallObject.setPhoneNumber (mPhoneStateIncomingNumber);
							mIncomingCallObject.setBeginTimestamp (beginTimestamp);
							mIncomingCallObject.setSimOperator (finalSimOperator);
							mIncomingCallObject.setSimSerialNumber (finalSimSerialNumber);
							mIncomingCallObject.setSimOperatorName (finalSimOperatorName);
							mIncomingCallObject.setSimCountryIso (finalSimCountryIso);
							mIncomingCallObject.setNetworkOperator (finalNetworkOperator);
							mIncomingCallObject.setNetworkOperatorName (finalNetworkOperatorName);
							mIncomingCallObject.setNetworkCountryIso (finalNetworkCountryIso);
							mIncomingCallObject.setAudioSource (finalAudioSource);
							mIncomingCallObject.setOutputFormat (finalOutputFormat);
							mIncomingCallObject.setAudioEncoder (finalAudioEncoder);
							mIncomingCallObject.setOutputFile (finalOutputFilePath);
							mIncomingCallObject.setType ("incoming");
							mIncomingCallObject.setFavourit (mFavourit);
						}
					});
				}
				if (mIsOutgoing) {
					mRealm.executeTransaction (realm -> {
						mOutgoingCallObject = realm.createObject (CallObject.class);
						LOGE (TAG, "out mPhoneStateIncomingNumber " + mPhoneStateIncomingNumber);
						LOGE (TAG, "out beginTimestamp" + beginTimestamp);
						if (mOutgoingCallObject != null) {
							mOutgoingCallObject.setPhoneNumber (mPhoneStateIncomingNumber);
							mOutgoingCallObject.setBeginTimestamp (beginTimestamp);
							mOutgoingCallObject.setAudioSource (finalAudioSource);
							mOutgoingCallObject.setSimSerialNumber (finalSimSerialNumber);
							mOutgoingCallObject.setSimOperator (finalSimOperator);
							mOutgoingCallObject.setSimOperatorName (finalSimOperatorName);
							mOutgoingCallObject.setSimCountryIso (finalSimCountryIso);
							mOutgoingCallObject.setNetworkOperator (finalNetworkOperator);
							mOutgoingCallObject.setNetworkOperatorName (finalNetworkOperatorName);
							mOutgoingCallObject.setNetworkCountryIso (finalNetworkCountryIso);
							mOutgoingCallObject.setOutputFormat (finalOutputFormat);
							mOutgoingCallObject.setAudioEncoder (finalAudioEncoder);
							mOutgoingCallObject.setOutputFile (finalOutputFilePath);
							mOutgoingCallObject.setType ("outgoing");
							mOutgoingCallObject.setFavourit (mFavourit);
						}
					});
				}
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
	}

	/**
	 * End recorder.
	 */
	@SuppressLint ("HardwareIds")
	@RequiresPermission (Manifest.permission.RECORD_AUDIO)
	public void endRecorder () {
		if (mMediaRecorder == null) {
			return;
		}
		long endTimestamp = new Date ().getTime ();
		if (ContextCompat.checkSelfPermission (this, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
			if (mAudioManager != null) {
				if (mTurnOnSpeaker) {
					try {
						if (mAudioManager.isSpeakerphoneOn ()) {
							mAudioManager.setSpeakerphoneOn (false);
						}
					} catch (Exception e) {
						LOGE (TAG, e.getMessage ());
						LOGE (TAG, e.toString ());
						e.printStackTrace ();
					}
				}
				if (mMaxUpVolume) {
					if (mVoiceCallStreamVolume != -1) {
						try {
							mAudioManager.setStreamVolume (AudioManager.STREAM_VOICE_CALL, mVoiceCallStreamVolume, 0);
						} catch (Exception e) {
							LOGE (TAG, e.getMessage ());
							LOGE (TAG, e.toString ());
							e.printStackTrace ();
						}
					}
				}
			}
		}
		if (mVibrate) {
			if (ContextCompat.checkSelfPermission (this, Manifest.permission.VIBRATE) == PackageManager.PERMISSION_GRANTED) {
				if (mVibrator != null) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						try {
							mVibrator.vibrate (VibrationEffect.createOneShot (AppConfig.END_RECORDER_VIBE_TIME, VibrationEffect.DEFAULT_AMPLITUDE));
						} catch (Exception e) {
							LOGE (TAG, e.getMessage ());
							LOGE (TAG, e.toString ());
							e.printStackTrace ();
						}
					} else {
						try {
							mVibrator.vibrate (AppConfig.END_RECORDER_VIBE_TIME);
						} catch (Exception e) {
							LOGE (TAG, e.getMessage ());
							LOGE (TAG, e.toString ());
							e.printStackTrace ();
						}
					}
				}
			}
		}
		boolean stop = stop (), reset = reset (), release = release ();
		if (!stop || !reset || !release) {
			if (!stop) {
				LOGW (TAG, "Media recorder (telephony) has stop exception");
			}
			if (!reset) {
				LOGW (TAG, "Media recorder (telephony) has reset exception");
			}
			if (!release) {
				LOGW (TAG, "Media recorder (telephony) has release exception");
			}
		}
		mMediaRecorder = null;
		if (mVoiceCallStreamVolume != -1) {
			mVoiceCallStreamVolume = -1;
		}
		if (!mMaxUpVolume) {
			mMaxUpVolume = true;
		}
		if (mTurnOnSpeaker) {
			mTurnOnSpeaker = false;
		}
		if (!mVibrate) {
			mVibrate = true;
		}
		if (mRealm != null && !mRealm.isClosed ()) {
			try {
				if (mIncomingCallObject != null) {
					LOGE (TAG, "inc endTimestamp" + endTimestamp);
					mRealm.executeTransaction (realm -> mIncomingCallObject.setEndTimestamp (endTimestamp));
					mIncomingCallObject = null;
				}
				if (mOutgoingCallObject != null) {
					LOGE (TAG, "out endTimestamp" + endTimestamp);
					mRealm.executeTransaction (realm -> mOutgoingCallObject.setEndTimestamp (endTimestamp));
					mOutgoingCallObject = null;
				}
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
		}
	}
}
