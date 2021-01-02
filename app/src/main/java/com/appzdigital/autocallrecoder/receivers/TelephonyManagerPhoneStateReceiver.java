package com.appzdigital.autocallrecoder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.appzdigital.autocallrecoder.R;
import com.appzdigital.autocallrecoder.envr.AppEnvr;
import com.appzdigital.autocallrecoder.services.CallRecorderService;

import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGE;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGI;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGW;

/**
 * The type Telephony manager phone state receiver.
 */
public class TelephonyManagerPhoneStateReceiver extends BroadcastReceiver {
	private static final String TAG = TelephonyManagerPhoneStateReceiver.class.getSimpleName ();
	private static boolean sIsIncoming = false;
	private static boolean sIsOutgoing = false;

	@Override
	public void onReceive (Context context, Intent intent) {
		if (context == null || intent == null) {
			if (context == null) {
				LOGW (TAG, "Receiver receive: Context lack");
			}
			if (intent == null) {
				LOGW (TAG, "Receiver receive: Intent lack");
			}
			return;
		}
		String intentAction = null;
		try {
			intentAction = intent.getAction ();
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (intentAction == null) {
			LOGW (TAG, "Receiver receive: Intent action lack");
			return;
		}
		if (!intentAction.equals (TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
			LOGW (TAG, "Receiver receive: Intent action mismatch");
			return;
		}
		onReceiveOk (context, intent);
	}

	private void onReceiveOk (@NonNull Context context, @NonNull Intent intent) {
		TelephonyManager telephonyManager = null;
		try {
			telephonyManager = (TelephonyManager) context.getSystemService (Context.TELEPHONY_SERVICE);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		String phoneStateExtraState = null;
		try {
			phoneStateExtraState = intent.getStringExtra (TelephonyManager.EXTRA_STATE);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (phoneStateExtraState != null) {
			if (phoneStateExtraState.equals (TelephonyManager.EXTRA_STATE_IDLE)) {
				LOGI (TAG, "Phone state: Idle");
				if (telephonyManager != null) {
					if (telephonyManager.getCallState () == TelephonyManager.CALL_STATE_IDLE) {
						onCallStateChange (context, intent, TelephonyManager.CALL_STATE_IDLE);
					}
				} else {
					onCallStateChange (context, intent, TelephonyManager.CALL_STATE_IDLE);
				}
			}
			if (phoneStateExtraState.equals (TelephonyManager.EXTRA_STATE_RINGING)) {
				LOGI (TAG, "Phone state: Ringing");
				if (telephonyManager != null) {
					if (telephonyManager.getCallState () == TelephonyManager.CALL_STATE_RINGING) {
						onCallStateChange (context, intent, TelephonyManager.CALL_STATE_RINGING);
					}
				} else {
					onCallStateChange (context, intent, TelephonyManager.CALL_STATE_RINGING);
				}
			}
			if (phoneStateExtraState.equals (TelephonyManager.EXTRA_STATE_OFFHOOK)) {
				LOGI (TAG, "Phone state: Offhook");
				if (telephonyManager != null) {
					if (telephonyManager.getCallState () == TelephonyManager.CALL_STATE_OFFHOOK) {
						onCallStateChange (context, intent, TelephonyManager.CALL_STATE_OFFHOOK);
					}
				} else {
					onCallStateChange (context, intent, TelephonyManager.CALL_STATE_OFFHOOK);
				}
			}
		}
	}

	private void onCallStateChange (@NonNull Context context, @NonNull Intent intent, int callState) {
		SharedPreferences sharedPreferences = null;
		try {
			sharedPreferences = context.getSharedPreferences (context.getString (R.string.app_name), Context.MODE_PRIVATE);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (sharedPreferences != null) {
			switch (callState) {
				case TelephonyManager.CALL_STATE_IDLE:
					if (CallRecorderService.sIsServiceRunning) {
						stopRecorder (context, intent);
					}
					if (sIsIncoming) {
						sIsIncoming = false;
					}
					if (sIsOutgoing) {
						sIsOutgoing = false;
					}
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					if (!sIsOutgoing) {
						sIsIncoming = true;
					}
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					if (!sIsIncoming) {
						sIsOutgoing = true;
					}
					if (!CallRecorderService.sIsServiceRunning) {
						if (sIsIncoming) {
							LOGI (TAG, "Call type: Incoming");
							if (sharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS, true)) {
								startRecorder (context, intent);
							}
							sIsIncoming = false;
						}
						if (sIsOutgoing) {
							LOGI (TAG, "Call type: Outgoing");
							if (sharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS, true)) {
								startRecorder (context, intent);
							}
							sIsOutgoing = false;
						}
					}
					break;
			}
		}
	}

	private void startRecorder (@NonNull Context context, @NonNull Intent intent) {
		if (CallRecorderService.sIsServiceRunning) {
			return;
		}
		intent.setClass (context, CallRecorderService.class);
		intent.putExtra (AppEnvr.INTENT_ACTION_INCOMING_CALL, sIsIncoming);
		intent.putExtra (AppEnvr.INTENT_ACTION_OUTGOING_CALL, sIsOutgoing);
		try {
			context.startService (intent);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
	}

	private void stopRecorder (@NonNull Context context, @NonNull Intent intent) {
		if (!CallRecorderService.sIsServiceRunning) {
			return;
		}
		intent.setClass (context, CallRecorderService.class);
		try {
			context.stopService (intent);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
	}
}
