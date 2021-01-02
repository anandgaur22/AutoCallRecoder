package com.appzdigital.autocallrecoder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.appzdigital.autocallrecoder.R;
import com.appzdigital.autocallrecoder.envr.AppEnvr;
import com.appzdigital.autocallrecoder.services.MainService;
import com.appzdigital.autocallrecoder.utils.AppUtil;

import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGD;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGE;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGW;

/**
 * The type Boot completed receiver.
 */
public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = BootCompletedReceiver.class.getSimpleName ();

	@Override
	public void onReceive (Context context, Intent intent) {
		LOGD (TAG, "Receiver receive");
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
		if (!intentAction.equals (Intent.ACTION_BOOT_COMPLETED)) {
			LOGW (TAG, "Receiver receive: Intent action mismatch");
			return;
		}
		LOGD (TAG, "Receiver receive: OK");
		onReceiveOk (context, intent);
	}

	private void onReceiveOk (@NonNull Context context, @NonNull Intent intent) {
		SharedPreferences sharedPreferences = null;
		try {
			sharedPreferences = context.getSharedPreferences (context.getString (R.string.app_name), Context.MODE_PRIVATE);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (sharedPreferences != null) {
			if (!sharedPreferences.contains (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS)) {
				SharedPreferences.Editor editor = sharedPreferences.edit ();
				editor.putBoolean (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS, true);
				editor.apply ();
			}
			if (!sharedPreferences.contains (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS)) {
				SharedPreferences.Editor editor = sharedPreferences.edit ();
				editor.putBoolean (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS, true);
				editor.apply ();
			}
			boolean recordIncomingCalls = sharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS, true);
			boolean recordOutgoingCalls = sharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS, true);
			if (recordIncomingCalls || recordOutgoingCalls) {
				if (!MainService.sIsServiceRunning) {
					AppUtil.startMainService (context);
				}
			}
		}
	}
}
