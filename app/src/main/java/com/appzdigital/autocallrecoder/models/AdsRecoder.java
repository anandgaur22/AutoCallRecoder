package com.appzdigital.autocallrecoder.models;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.FrameLayout;

import com.appzdigital.autocallrecoder.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGI;
import static com.appzdigital.autocallrecoder.utils.LogUtils.makeLogTag;

/**
 * The type Ads recoder.
 */
public class AdsRecoder {
	private static final String TAG = makeLogTag (AdsRecoder.class);
	private Activity activity;
	private FrameLayout adContainerView;
	private AdView adView;
	private InterstitialAd interstitialAd;

	/**
	 * Instantiates a new Ads recoder.
	 */
	public AdsRecoder () {
	}

	/**
	 * Instantiates a new Ads recoder.
	 *
	 * @param activity the activity
	 */
	public AdsRecoder (Activity activity) {
		this.activity = activity;
		MobileAds.initialize (activity.getApplicationContext (), initializationStatus -> {
		});
	}

	/**
	 * Gets interstitial ad.
	 *
	 * @return the interstitial ad
	 */
	public InterstitialAd getInterstitialAd () {
		return interstitialAd;
	}

	/**
	 * Sets interstitial ad.
	 *
	 * @param interstitialAd the interstitial ad
	 */
	public void setInterstitialAd (InterstitialAd interstitialAd) {
		this.interstitialAd = interstitialAd;
	}

	/**
	 * Init banner.
	 */
	public void initBanner () {
		adContainerView = activity.findViewById (R.id.ad_view_container);
		adContainerView.post (this::loadBanner);
	}

	/**
	 * Init interstitial.
	 */
	public void initInterstitial () {
		interstitialAd = new InterstitialAd (activity.getApplicationContext ());
		interstitialAd.setAdUnitId (activity.getResources ().getString (R.string.admob_app_interstitial));
		interstitialAd.loadAd (new AdRequest.Builder ().build ());
		interstitialAd.setAdListener (new AdListener () {
			@Override
			public void onAdLoaded () {
				LOGI (TAG, "onAdLoaded()");
			}

			@Override
			public void onAdFailedToLoad (int errorCode) {
				LOGI (TAG, "onAdFailedToLoad() with error code: " + errorCode);
			}

			@Override
			public void onAdOpened () {
				LOGI (TAG, "onAdOpened()");
			}

			@Override
			public void onAdClicked () {
				LOGI (TAG, "onAdClicked()");
			}

			@Override
			public void onAdLeftApplication () {
				LOGI (TAG, "onAdLeftApplication()");
			}

			@Override
			public void onAdClosed () {
				LOGI (TAG, "onAdClosed()");
				if (!interstitialAd.isLoading () && !interstitialAd.isLoaded ()) {
					interstitialAd.loadAd (new AdRequest.Builder ().build ());
				}
			}
		});
	}

	private void showInterstitial () {
		if (interstitialAd != null && interstitialAd.isLoaded ()) {
			interstitialAd.show ();
		} else {
			LOGI (TAG, "d did not load");
		}
	}

	/**
	 * Pause banner.
	 */
	public void pauseBanner () {
		if (adView != null) {
			adView.pause ();
		}
	}

	/**
	 * Resume banner.
	 */
	public void resumeBanner () {
		if (adView != null) {
			adView.resume ();
		}
	}

	/**
	 * Destroy banner.
	 */
	public void destroyBanner () {
		if (adView != null) {
			adView.destroy ();
		}
	}

	private void loadBanner () {
		adView = new AdView (activity.getApplicationContext ());
		adView.setAdUnitId (activity.getResources ().getString (R.string.admob_app_banner));
		adContainerView.removeAllViews ();
		adContainerView.addView (adView);
		AdSize adSize = getAdSize ();
		adView.setAdSize (adSize);
		AdRequest adRequest = new AdRequest.Builder ()
				.addTestDevice (AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice ("D4AFDC0E95B2729297A2F1BA17F42425")
				.build ();
		adView.loadAd (adRequest);
	}

	private AdSize getAdSize () {
		Display display = activity.getWindowManager ().getDefaultDisplay ();
		DisplayMetrics outMetrics = new DisplayMetrics ();
		display.getMetrics (outMetrics);
		float density = outMetrics.density;
		float adWidthPixels = adContainerView.getWidth ();
		if (adWidthPixels == 0) {
			adWidthPixels = outMetrics.widthPixels;
		}
		int adWidth = (int) (adWidthPixels / density);
		return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize (activity.getApplicationContext (), adWidth);
	}
}
