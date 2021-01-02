package com.appzdigital.autocallrecoder.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import com.appzdigital.autocallrecoder.R;
import com.appzdigital.autocallrecoder.adapters.CallRecyclerViewAdapter;
import com.appzdigital.autocallrecoder.adapters.IncomingCallRecyclerViewAdapter;
import com.appzdigital.autocallrecoder.adapters.OutgoingCallRecyclerViewAdapter;
import com.appzdigital.autocallrecoder.adapters.TabLayoutFragmentPagerAdapter;
import com.appzdigital.autocallrecoder.custom_ui.BlurLayout;
import com.appzdigital.autocallrecoder.envr.AppEnvr;
import com.appzdigital.autocallrecoder.fragments.AllCallTabFragment;
import com.appzdigital.autocallrecoder.fragments.FavouritTabFragment;
import com.appzdigital.autocallrecoder.fragments.IncomingTabFragment;
import com.appzdigital.autocallrecoder.fragments.OutgoingTabFragment;
import com.appzdigital.autocallrecoder.models.AdsRecoder;
import com.appzdigital.autocallrecoder.services.MainService;
import com.appzdigital.autocallrecoder.utils.AppUtil;
import com.appzdigital.autocallrecoder.utils.RequestIgnoreBatteryOptimizationsUtil;
import com.appzdigital.autocallrecoder.utils.ResourceUtil;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;

import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGD;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGE;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGI;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGW;

/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
	private static final String TAG = MainActivity.class.getSimpleName ();
	private TabLayout mTabLayout = null;
	private BlurLayout blurLayout;
	private TabLayout.OnTabSelectedListener mOnTabSelectedListener = null;
	private SharedPreferences mSharedPreferences = null;
	private boolean mRecordCalls = true;
	private boolean mRecordIncomingCalls = mRecordCalls;
	private boolean mRecordOutgoingCalls = mRecordCalls;
	private SwitchCompat scRecordCalls;
	private ImageView mSetting;
	private ImageView mClose;
	private ImageView mSearch;
	private ConstraintLayout clMain;
	private ConstraintLayout clSearch;
	private SearchView mSearchView = null;
	private AllCallTabFragment allCallTabFragment;
	private FavouritTabFragment favouritTabFragment;
	private IncomingTabFragment incomingTabFragment;
	private OutgoingTabFragment outgoingTabFragment;
	private AdsRecoder adsRecoder;

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
		setContentView (R.layout.activity_main);
		adsRecoder = new AdsRecoder (this);
		adsRecoder.initBanner ();
		blurLayout = findViewById (R.id.blurLayout);
		Toolbar toolbar = findViewById (R.id.toolbar);
		clMain = toolbar.findViewById (R.id.cl_main);
		clSearch = toolbar.findViewById (R.id.cl_search);
		mClose = toolbar.findViewById (R.id.appclose);
		mSearch = toolbar.findViewById (R.id.appsearch);
		mSearchView = toolbar.findViewById (R.id.tab_search_view);
		mSearch.setOnClickListener (view -> {
			clMain.setVisibility (View.GONE);
			clSearch.setVisibility (View.VISIBLE);
		});
		mClose.setOnClickListener (view -> {
			clSearch.setVisibility (View.GONE);
			clMain.setVisibility (View.VISIBLE);
		});
		toolbar.findViewById (R.id.appback).setVisibility (View.GONE);
		TextView title = toolbar.findViewById (R.id.toolbar_title);
		title.setPadding (0, 0, 0, 0);
		mSetting = toolbar.findViewById (R.id.appsetting);
		mSetting.setOnClickListener (this);
		ArrayList<TabLayoutFragmentPagerAdapter.ITabLayoutIconFragmentPagerAdapter> tabLayoutIconFragmentPagerAdapterArrayList = new ArrayList<> ();
		allCallTabFragment = new AllCallTabFragment ();
		favouritTabFragment = new FavouritTabFragment ();
		incomingTabFragment = new IncomingTabFragment ();
		outgoingTabFragment = new OutgoingTabFragment ();
		tabLayoutIconFragmentPagerAdapterArrayList.add (allCallTabFragment);
		tabLayoutIconFragmentPagerAdapterArrayList.add (favouritTabFragment);
		tabLayoutIconFragmentPagerAdapterArrayList.add (incomingTabFragment);
		tabLayoutIconFragmentPagerAdapterArrayList.add (outgoingTabFragment);
		TabLayoutFragmentPagerAdapter tabLayoutFragmentPagerAdapter = new TabLayoutFragmentPagerAdapter (getSupportFragmentManager (), null, tabLayoutIconFragmentPagerAdapterArrayList);
		ViewPager viewPager = findViewById (R.id.view_pager);
		viewPager.setAdapter (tabLayoutFragmentPagerAdapter);
		viewPager.addOnPageChangeListener (new ViewPager.OnPageChangeListener () {
			@Override
			public void onPageScrolled (int position, float positionOffset, int positionOffsetPixels) {
				if (clMain.getVisibility () == View.GONE) {
					clSearch.setVisibility (View.GONE);
					clMain.setVisibility (View.VISIBLE);
				}
			}

			@Override
			public void onPageSelected (int position) {
				Filter filter = null;
				switch (position) {
					case 0:
						filter = ((CallRecyclerViewAdapter) Objects.requireNonNull (allCallTabFragment.mRecyclerView.getAdapter ())).getFilter ();
						break;
					case 1:
						filter = ((CallRecyclerViewAdapter) Objects.requireNonNull (favouritTabFragment.mRecyclerView.getAdapter ())).getFilter ();
						break;
					case 2:
						filter = ((IncomingCallRecyclerViewAdapter) Objects.requireNonNull (incomingTabFragment.mRecyclerView.getAdapter ())).getFilter ();
						break;
					case 3:
						filter = ((OutgoingCallRecyclerViewAdapter) Objects.requireNonNull (outgoingTabFragment.mRecyclerView.getAdapter ())).getFilter ();
						break;
				}
				if (mSearchView != null) {
					mSearchView.setQuery (null, true);
					Filter finalFilter = filter;
					mSearchView.setOnQueryTextListener (new SearchView.OnQueryTextListener () {
						@Override
						public boolean onQueryTextSubmit (String s) {
							if (finalFilter != null) {
								finalFilter.filter (s);
								mSearchView.clearFocus ();
								return true;
							}
							return false;
						}

						@Override
						public boolean onQueryTextChange (String s) {
							if (finalFilter != null) {
								finalFilter.filter (s);
								return true;
							}
							return false;
						}
					});
				}
			}

			@Override
			public void onPageScrollStateChanged (int state) {
			}
		});
		mTabLayout = findViewById (R.id.tab_layout);
		mTabLayout.setupWithViewPager (viewPager);
		ColorFilter tabIconColorFilter = new PorterDuffColorFilter (ResourceUtil.getColor (this, R.color.cp_3), PorterDuff.Mode.SRC_IN);
		ColorFilter tabSelectedIconColorFilter = new PorterDuffColorFilter (ResourceUtil.getColor (this, R.color.cp_4), PorterDuff.Mode.SRC_IN);
		for (int i = 0 ; i < mTabLayout.getTabCount () ; i++) {
			TabLayout.Tab tab = null;
			try {
				tab = mTabLayout.getTabAt (i);
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			if (tab != null) {
				try {
					tab.setIcon (tabLayoutIconFragmentPagerAdapterArrayList.get (i).getIcon ());
					Drawable icon = tab.getIcon ();
					if (icon != null) {
						if (tab.getPosition () == 0) {
							icon.setColorFilter (tabSelectedIconColorFilter);
						} else {
							icon.setColorFilter (tabIconColorFilter);
						}
					}
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
			}
		}
		mOnTabSelectedListener = new TabLayout.ViewPagerOnTabSelectedListener (viewPager) {
			@Override
			public void onTabSelected (TabLayout.Tab tab) {
				if (tab == null) {
					return;
				}
				super.onTabSelected (tab);
				LOGD (TAG, "Tab select");
				if (tab.getText () != null) {
					LOGI (TAG, "Tab select: " + tab.getText ());
				}
				if (tab.getIcon () != null) {
					tab.getIcon ().setColorFilter (tabSelectedIconColorFilter);
				}
			}

			@Override
			public void onTabUnselected (TabLayout.Tab tab) {
				if (tab == null) {
					return;
				}
				super.onTabUnselected (tab);
				LOGD (TAG, "Tab unselect");
				if (tab.getText () != null) {
					LOGI (TAG, "Tab unselect: " + tab.getText ());
				}
				if (tab.getIcon () != null) {
					tab.getIcon ().setColorFilter (tabIconColorFilter);
				}
			}

			@Override
			public void onTabReselected (TabLayout.Tab tab) {
				if (tab == null) {
					return;
				}
				super.onTabReselected (tab);
				LOGD (TAG, "Tab reselect");
				if (tab.getText () != null) {
					LOGI (TAG, "Tab reselect: " + tab.getText ());
				}
			}
		};
		mTabLayout.addOnTabSelectedListener (mOnTabSelectedListener);
		try {
			mSharedPreferences = getSharedPreferences (getString (R.string.app_name), Context.MODE_PRIVATE);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		if (mSharedPreferences != null) {
			if (!mSharedPreferences.contains (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS)) {
				SharedPreferences.Editor editor = mSharedPreferences.edit ();
				editor.putBoolean (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS, true);
				editor.apply ();
			}
			if (!mSharedPreferences.contains (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS)) {
				SharedPreferences.Editor editor = mSharedPreferences.edit ();
				editor.putBoolean (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS, true);
				editor.apply ();
			}
			boolean recordIncomingCalls = mSharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls);
			boolean recordOutgoingCalls = mSharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
			if (recordIncomingCalls || recordOutgoingCalls) {
				if (!MainService.sIsServiceRunning) {
					AppUtil.startMainService (this);
				}
			}
		}
		scRecordCalls = findViewById (R.id.sc_recordCalls);
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

	@Override
	protected void onResume () {
		super.onResume ();
		adsRecoder.resumeBanner ();
		LOGE (TAG, "Main Resume");
		if (mSharedPreferences != null) {
			mRecordIncomingCalls = mSharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls);
			mRecordOutgoingCalls = mSharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
		} else {
			try {
				mSharedPreferences = getSharedPreferences (getString (R.string.app_name), Context.MODE_PRIVATE);
			} catch (Exception e) {
				LOGE (TAG, e.getMessage ());
				LOGE (TAG, e.toString ());
				e.printStackTrace ();
			}
			mRecordIncomingCalls = mSharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_INCOMING_CALLS, mRecordIncomingCalls);
			mRecordOutgoingCalls = mSharedPreferences.getBoolean (AppEnvr.SP_KEY_RECORD_OUTGOING_CALLS, mRecordOutgoingCalls);
		}
		mRecordCalls = mRecordIncomingCalls;
		scRecordCalls.setChecked (mRecordCalls);
	}

	@Override
	protected void onStart () {
		super.onStart ();
		LOGD (TAG, "Activity start");
		blurLayout.startBlur ();
		blurLayout.lockView ();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			ArrayList<String> runtimePermissionsArrayList = new ArrayList<> ();
			runtimePermissionsArrayList.add (Manifest.permission.INTERNET);
			runtimePermissionsArrayList.add (Manifest.permission.READ_PHONE_STATE);
			runtimePermissionsArrayList.add (Manifest.permission.CALL_PHONE);
			runtimePermissionsArrayList.add (Manifest.permission.RECORD_AUDIO);
			runtimePermissionsArrayList.add (Manifest.permission.VIBRATE);
			runtimePermissionsArrayList.add (Manifest.permission.RECEIVE_BOOT_COMPLETED);
			runtimePermissionsArrayList.add (Manifest.permission.READ_CONTACTS);
			runtimePermissionsArrayList.add (Manifest.permission.MODIFY_AUDIO_SETTINGS);
			runtimePermissionsArrayList.add (Manifest.permission.WAKE_LOCK);
			runtimePermissionsArrayList.add (Manifest.permission.READ_EXTERNAL_STORAGE);
			runtimePermissionsArrayList.add (Manifest.permission.WRITE_EXTERNAL_STORAGE);
			runtimePermissionsArrayList.add (Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				runtimePermissionsArrayList.add (Manifest.permission.FOREGROUND_SERVICE);
			}
			if (!runtimePermissionsArrayList.isEmpty ()) {
				ArrayList<String> requestRuntimePermissionsArrayList = new ArrayList<> ();
				for (String requestRuntimePermission : runtimePermissionsArrayList) {
					if (checkSelfPermission (requestRuntimePermission) != PackageManager.PERMISSION_GRANTED) {
						requestRuntimePermissionsArrayList.add (requestRuntimePermission);
					}
				}
				if (!requestRuntimePermissionsArrayList.isEmpty ()) {
					requestPermissions (requestRuntimePermissionsArrayList.toArray (new String[ 0 ]), 1);
				}
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission (Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS) == PackageManager.PERMISSION_GRANTED) {
				PowerManager powerManager = null;
				try {
					powerManager = (PowerManager) getSystemService (Context.POWER_SERVICE);
				} catch (Exception e) {
					LOGE (TAG, e.getMessage ());
					LOGE (TAG, e.toString ());
					e.printStackTrace ();
				}
				if (powerManager != null) {
					if (powerManager.isIgnoringBatteryOptimizations (getPackageName ())) {
						LOGI (TAG, "2. Request ignore battery optimizations (\"1.\" alternative; with package URI) - Entire application: Enabled");
					} else {
						LOGW (TAG, "2. Request ignore battery optimizations (\"1.\" alternative; with package URI) - Entire application: Not enabled");
						Intent intent = RequestIgnoreBatteryOptimizationsUtil.getRequestIgnoreBatteryOptimizationsIntent (this);
						if (intent != null) {
							startActivityForResult (intent, 2);
						}
					}
				}
			}
		}
	}

	@Override
	protected void onDestroy () {
		adsRecoder.destroyBanner ();
		super.onDestroy ();
		LOGD (TAG, "Activity destroy");
		if (mSharedPreferences != null) {
			mSharedPreferences = null;
		}
		if (mTabLayout != null) {
			if (mOnTabSelectedListener != null) {
				mTabLayout.removeOnTabSelectedListener (mOnTabSelectedListener);
				mOnTabSelectedListener = null;
			}
			mTabLayout = null;
		}
	}

	@Override
	protected void onPause () {
		super.onPause ();
		adsRecoder.pauseBanner ();
	}

	@Override
	protected void onStop () {
		blurLayout.pauseBlur ();
		super.onStop ();
	}

	@Override
	public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult (requestCode, permissions, grantResults);
		switch (requestCode) {
			case 1:
				if (grantResults.length > 0 && grantResults.length == permissions.length) {
					boolean allGranted = true;
					for (int grantResult : grantResults) {
						if (grantResult != PackageManager.PERMISSION_GRANTED) {
							allGranted = false;
						}
					}
					if (allGranted) {
						LOGI (TAG, "All requested permissions are granted");
					} else {
						LOGW (TAG, "Not all requested permissions are granted");
						AlertDialog.Builder builder = new AlertDialog.Builder (this);
						builder.setTitle (getString (R.string.runtime_permissions_not_granted_title));
						builder.setMessage (getString (R.string.runtime_permissions_not_granted_message));
						builder.setNeutralButton (android.R.string.ok, (dialogInterface, i) -> dialogInterface.dismiss ());
						AlertDialog alertDialog = builder.create ();
						alertDialog.show ();
					}
				}
				break;
		}
	}

	@Override
	public void onClick (View view) {
		switch (view.getId ()) {
			case R.id.appsetting:
				startActivity (new Intent (this, SettingsActivity.class));
				break;
		}
	}
}
