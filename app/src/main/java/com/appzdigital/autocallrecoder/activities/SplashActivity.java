package com.appzdigital.autocallrecoder.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.appzdigital.autocallrecoder.R;

import static com.appzdigital.autocallrecoder.utils.LogUtils.makeLogTag;

/**
 * The type Splash activity.
 */
public class SplashActivity extends AppCompatActivity {
	private static final String TAG = makeLogTag (SplashActivity.class);

	/**
	 * Sets status bar gradiant.
	 *
	 * @param activity the activity
	 */
	@TargetApi (Build.VERSION_CODES.LOLLIPOP)
	public void setStatusBarGradiant (Activity activity) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = activity.getWindow ();
			DisplayMetrics displayMetrics = new DisplayMetrics ();
			getWindowManager ().getDefaultDisplay ().getMetrics (displayMetrics);
			int height = displayMetrics.heightPixels;
			int width = displayMetrics.widthPixels;
			Drawable background = activity.getResources ().getDrawable (R.drawable.gradient_bg);
			window.addFlags (WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.setStatusBarColor (activity.getResources ().getColor (android.R.color.transparent));
			window.setNavigationBarColor (activity.getResources ().getColor (android.R.color.transparent));
			window.setBackgroundDrawable (background);
		}
	}

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate (savedInstanceState);
		setStatusBarGradiant (this);
		setContentView (R.layout.activity_splash);
		find_views_by_id ();
		init_variables ();
	}

	private void find_views_by_id () {
	}

	private void init_variables () {
		Handler handler = new Handler ();
		handler.postDelayed (new Runnable () {
			public void run () {
				SplashActivity.this.startActivity (new Intent (SplashActivity.this, MainActivity.class));
				finish ();
			}
		}, 2000);
	}
}
