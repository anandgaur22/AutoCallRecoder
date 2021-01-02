package com.appzdigital.autocallrecoder;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.os.Build;

import com.appzdigital.autocallrecoder.envr.AppEnvr;

import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;

import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGD;
import static com.appzdigital.autocallrecoder.utils.LogUtils.LOGE;
import static com.appzdigital.autocallrecoder.utils.LogUtils.makeLogTag;

/**
 * The type Recoder app.
 */
public class RecoderApp extends Application {
	/**
	 * The constant LOG_PREFIX.
	 */
	public static final String LOG_PREFIX = "_";
	/**
	 * The constant LOG_PREFIX_LENGTH.
	 */
	public static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length ();
	/**
	 * The constant MAX_LOG_TAG_LENGTH.
	 */
	public static final int MAX_LOG_TAG_LENGTH = 50;
	private static final String TAG = makeLogTag (RecoderApp.class);
	/**
	 * The constant LOGGING_ENABLED.
	 */
	public static boolean LOGGING_ENABLED = false;
	private final RealmMigration mRealmMigration = (realm, oldVersion, newVersion) -> {
	};

	@Override
	public void onCreate () {
		AppEnvr.sFilesDirMemory = getFilesDir ();
		AppEnvr.sFilesDirPathMemory = getFilesDir ().getPath ();
		AppEnvr.sCacheDirMemory = getCacheDir ();
		AppEnvr.sCacheDirPathMemory = getCacheDir ().getPath ();
		try {
			AppEnvr.sExternalFilesDirMemory = getExternalFilesDir (null);
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		try {
			AppEnvr.sExternalFilesDirPathMemory = Objects.requireNonNull (getExternalFilesDir (null)).getPath ();
		} catch (Exception e) {
			LOGE (TAG, e.getMessage ());
			LOGE (TAG, e.toString ());
			e.printStackTrace ();
		}
		AppEnvr.sExternalCacheDirMemory = getExternalCacheDir ();
		AppEnvr.sExternalCacheDirPathMemory = Objects.requireNonNull (getExternalCacheDir ()).getPath ();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			AppEnvr.sProcessName = getProcessName ();
		}
		super.onCreate ();
		LOGD (TAG, "Application create");
		Realm.init (this);
		RealmConfiguration realmConfiguration = new RealmConfiguration.Builder ()
				.migration (mRealmMigration)
				.build ();
		LOGD (TAG, "Realm configuration schema version: " + realmConfiguration.getSchemaVersion ());
		Realm.setDefaultConfiguration (realmConfiguration);
	}

	@Override
	public void onTrimMemory (int level) {
		super.onTrimMemory (level);
		LOGD (TAG, "Application trim memory");
		switch (level) {
			case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
				LOGD (TAG, "Application trim memory: Running moderate");
				break;
			case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
				LOGD (TAG, "Application trim memory: Running low");
				break;
			case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:
				LOGD (TAG, "Application trim memory: Running critical");
				break;
			case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:
				LOGD (TAG, "Application trim memory: UI hidden");
				break;
			case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
				LOGD (TAG, "Application trim memory: Background");
				break;
			case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
				LOGD (TAG, "Application trim memory: Moderate");
				break;
			case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:
				LOGD (TAG, "Application trim memory: Complete");
				if (AppEnvr.sFilesDirMemory == null) {
					AppEnvr.sFilesDirMemory = getFilesDir ();
				}
				if (AppEnvr.sFilesDirPathMemory == null) {
					AppEnvr.sFilesDirPathMemory = getFilesDir ().getPath ();
				}
				if (AppEnvr.sCacheDirMemory == null) {
					AppEnvr.sCacheDirMemory = getCacheDir ();
				}
				if (AppEnvr.sCacheDirPathMemory == null) {
					AppEnvr.sCacheDirPathMemory = getCacheDir ().getPath ();
				}
				if (AppEnvr.sExternalFilesDirMemory == null) {
					try {
						AppEnvr.sExternalFilesDirMemory = getExternalFilesDir (null);
					} catch (Exception e) {
						LOGE (TAG, e.getMessage ());
						LOGE (TAG, e.toString ());
						e.printStackTrace ();
					}
				}
				if (AppEnvr.sExternalFilesDirPathMemory == null) {
					try {
						AppEnvr.sExternalFilesDirPathMemory = Objects.requireNonNull (getExternalFilesDir (null)).getPath ();
					} catch (Exception e) {
						LOGE (TAG, e.getMessage ());
						LOGE (TAG, e.toString ());
						e.printStackTrace ();
					}
				}
				if (AppEnvr.sExternalCacheDirMemory == null) {
					AppEnvr.sExternalCacheDirMemory = getExternalCacheDir ();
				}
				if (AppEnvr.sExternalCacheDirPathMemory == null) {
					AppEnvr.sExternalCacheDirPathMemory = Objects.requireNonNull (getExternalCacheDir ()).getPath ();
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
					if (AppEnvr.sProcessName == null) {
						AppEnvr.sProcessName = getProcessName ();
					}
				}
				break;
		}
	}
}
