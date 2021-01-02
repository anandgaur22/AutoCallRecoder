package com.appzdigital.autocallrecoder.envr;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.File;

/**
 * The type App envr.
 */
public class AppEnvr {
	/**
	 * The constant LOG_V.
	 */
	public static final boolean LOG_V = true;
	/**
	 * The constant LOG_D.
	 */
	public static final boolean LOG_D = true;
	/**
	 * The constant LOG_I.
	 */
	public static final boolean LOG_I = true;
	/**
	 * The constant LOG_W.
	 */
	public static final boolean LOG_W = true;
	/**
	 * The constant LOG_E.
	 */
	public static final boolean LOG_E = true;
	/**
	 * The constant LOG_WTF.
	 */
	public static final boolean LOG_WTF = true;
	/**
	 * The constant SP_KEY_RECORD_INCOMING_CALLS.
	 */
	public static final String SP_KEY_RECORD_INCOMING_CALLS = "record_incoming_calls";
	/**
	 * The constant SP_KEY_RECORD_OUTGOING_CALLS.
	 */
	public static final String SP_KEY_RECORD_OUTGOING_CALLS = "record_outgoing_calls";
	/**
	 * The constant FM_SP_KEY_RECORDS_OUTPUT_LOCATION.
	 */
	public static final String FM_SP_KEY_RECORDS_OUTPUT_LOCATION = "records_output_location";
	/**
	 * The constant FM_SP_AUDIO_SOURCE.
	 */
	public static final String FM_SP_AUDIO_SOURCE = "audio_source";
	/**
	 * The constant FM_SP_OUTPUT_FORMAT.
	 */
	public static final String FM_SP_OUTPUT_FORMAT = "output_format";
	/**
	 * The constant FM_SP_AUDIO_ENCODER.
	 */
	public static final String FM_SP_AUDIO_ENCODER = "audio_encoder";
	/**
	 * The constant FM_SP_VIBRATE.
	 */
	public static final String FM_SP_VIBRATE = "vibrate";
	/**
	 * The constant FM_SP_TURN_ON_SPEAKER.
	 */
	public static final String FM_SP_TURN_ON_SPEAKER = "turn_on_speaker";
	/**
	 * The constant FM_SP_MAX_UP_VOLUME.
	 */
	public static final String FM_SP_MAX_UP_VOLUME = "max_up_volume";
	/**
	 * The constant FM_SP_CHANGE_CONSENT_INFORMATION.
	 */
	public static final String FM_SP_CHANGE_CONSENT_INFORMATION = "change_consent_information";
	/**
	 * The constant INTENT_ACTION_INCOMING_CALL.
	 */
	public static final String INTENT_ACTION_INCOMING_CALL = "incoming_call";
	/**
	 * The constant INTENT_ACTION_OUTGOING_CALL.
	 */
	public static final String INTENT_ACTION_OUTGOING_CALL = "outgoing_call";
	/**
	 * The constant sFilesDirMemory.
	 */
	public static File sFilesDirMemory = null, /**
	 * The S cache dir memory.
	 */
	sCacheDirMemory = null;
	/**
	 * The constant sFilesDirPathMemory.
	 */
	public static String sFilesDirPathMemory = null, /**
	 * The S cache dir path memory.
	 */
	sCacheDirPathMemory = null;
	/**
	 * The constant sExternalFilesDirMemory.
	 */
	public static File sExternalFilesDirMemory = null, /**
	 * The S external cache dir memory.
	 */
	sExternalCacheDirMemory = null;
	/**
	 * The constant sExternalFilesDirPathMemory.
	 */
	public static String sExternalFilesDirPathMemory = null, /**
	 * The S external cache dir path memory.
	 */
	sExternalCacheDirPathMemory = null;
	/**
	 * The constant sProcessName.
	 */
	@RequiresApi (api = Build.VERSION_CODES.P)
	public static String sProcessName = null;
}
