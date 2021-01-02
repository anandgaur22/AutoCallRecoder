package com.appzdigital.autocallrecoder.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.appzdigital.autocallrecoder.R;
import com.appzdigital.autocallrecoder.envr.AppEnvr;
import com.appzdigital.autocallrecoder.services.MainService;
import com.appzdigital.autocallrecoder.utils.AppUtil;

import java.util.Objects;

import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGD;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGE;

/**
 * The type Settings activity.
 */
public class SettingsActivity extends AppCompatActivity {
	private static final String TAG = SettingsActivity.class.getSimpleName ();
	private Toolbar toolbar;
	private boolean mRecordCalls;
	private boolean mRecordIncomingCalls = mRecordCalls;
	private boolean mRecordOutgoingCalls = mRecordCalls;
	private SwitchCompat scRecordCalls;
	private SharedPreferences mSharedPreferences = null;
	private ImageView mBack;

	/**
	 * Sets status bar gradiant.
	 *
	 * @param activity the activity
	 */
	public void setStatusBarGradiant (Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = activity.getWindow ();
			DisplayMetrics displayMetrics = new DisplayMetrics ();
			getWindowManager ().getDefaultDisplay ().getMetrics (displayMetrics);
			int height = displayMetrics.heightPixels;
			int width = displayMetrics.widthPixels;
			LayerDrawable background = (LayerDrawable) activity.getResources ().getDrawable (R.drawable.gradient_theme);
			background.setLayerInset (1, -10, 0, -10, 3 * height / 4);
			window.addFlags (WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor (activity.getResources ().getColor (android.R.color.transparent));
			window.setNavigationBarColor (activity.getResources ().getColor (android.R.color.transparent));
			window.setBackgroundDrawable (background);
		}
	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		LOGD (TAG, "Activity create");
		setStatusBarGradiant (this);
		setContentView (R.layout.settings_activity);
		toolbar = findViewById (R.id.toolbar);
		toolbar.findViewById (R.id.appsetting).setVisibility (View.GONE);
		toolbar.findViewById (R.id.appsearch).setVisibility (View.GONE);
		toolbar.findViewById (R.id.sc_recordCalls).setVisibility (View.GONE);
		TextView title = toolbar.findViewById (R.id.toolbar_title);
		title.setText ("Setting");
		mBack = toolbar.findViewById (R.id.appback);
		mBack.setOnClickListener (view -> finish ());
		getSupportFragmentManager ()
				.beginTransaction ()
				.replace (R.id.settings, new SettingsFragment ())
				.commit ();
		try {
			mSharedPreferences = getSharedPreferences (getString (R.string.app_name), Context.MODE_PRIVATE);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mSharedPreferences != null) {
			mRecordIncomingCalls = mSharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls);
			mRecordOutgoingCalls = mSharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
		}
		mRecordCalls = mRecordIncomingCalls;
		scRecordCalls = findViewById (R.id.ssc_recordCalls);
		scRecordCalls.setChecked (mRecordCalls);
		scRecordCalls.setOnCheckedChangeListener ((compoundButton, b) -> {
			mRecordCalls = b;
			mRecordIncomingCalls = mRecordCalls;
			mRecordOutgoingCalls = mRecordCalls;
			if (mSharedPreferences != null) {
				SharedPreferences.Editor editor = mSharedPreferences.edit ();
				editor.putBoolean (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls);
				editor.putBoolean (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
				editor.apply ();
			}
			if (mRecordIncomingCalls && !MainService.sIsServiceRunning) {
				AppUtil.startMainService (this);
			}
			if (mRecordOutgoingCalls && !MainService.sIsServiceRunning) {
				AppUtil.startMainService (this);
			}
		});
	}

	/**
	 * The type Settings fragment.
	 */
	public static class SettingsFragment extends PreferenceFragmentCompat {
		private Context getContextNonNull () {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				return Objects.requireNonNull (getContext ());
			} else {
				return getContext ();
			}
		}

		@Override
		public void onCreatePreferences (Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource (R.xml.root_preferences, rootKey);
			Preference changeConsentInformationPreference = findPreference (AppEnvr.FM_SP_CHANGE_CONSENT_INFORMATION);
		}
	}
}
