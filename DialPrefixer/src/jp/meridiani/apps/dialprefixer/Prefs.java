package jp.meridiani.apps.dialprefixer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import jp.meridiani.apps.dialprefixer.R;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.provider.Settings.System;

public class Prefs implements OnSharedPreferenceChangeListener {
	private static Prefs mInstance = null;
	static final String KEY_DISPLAYTOASTONPROFILECHANGE = "display_toast_on_profile_change";
	static final String KEY_VIBRATEONPROFILECHANGE      = "vibrate_on_profile_change";
	static final String KEY_PLAYSOUNDONVOLUMECHANGE     = "play_sound_on_volume_change";
	static final String KEY_SOUNDLEVELALERTHACK         = "sound_level_alert_hack";
	static final String KEY_DISPLAYTOASTONVOLUMELOCK    = "display_toast_on_volume_lock";

	private static final String PREFS_START = "<preferences>";
	private static final String PREFS_END   = "</preferences>";

	// for cyanogenmod
	private static final String VOLUME_LINK_NOTIFICATION = "VOLUME_LINK_NOTIFICATION";
	private boolean mHasVolumeLinkNotification = false;
	private String  mKeyVolumeLinkNotification = null;

	private Context mContext;
	private SharedPreferences mSharedPrefs;

	public int getPrefsResId() {
		return R.xml.prefs;
	}

	private Prefs(Context context) {
		mContext = context;
		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		PreferenceManager.setDefaultValues(context, getPrefsResId(), false);
		mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
		Class<System> c = android.provider.Settings.System.class;
		try {
			Field f = c.getField(VOLUME_LINK_NOTIFICATION);
			mKeyVolumeLinkNotification = (String)f.get(null);
			mHasVolumeLinkNotification = true;
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

	public static synchronized Prefs getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new Prefs(context);
		}
		return mInstance;
	}

	public void finalize() throws Throwable {
		try {
			if (mSharedPrefs != null) {
				mSharedPrefs.unregisterOnSharedPreferenceChangeListener(this);
			}
		}
		finally {
			super.finalize();
		}
	}

	private void setValue(String key, String value) {
		setBooleanValue(key, Boolean.parseBoolean(value));
	}

	private void setBooleanValue(String key, boolean value) {
		Editor editor = mSharedPrefs.edit();
		editor.putBoolean(key, value);
		editor.apply();
	}

	public boolean isDisplayToastOnProfileChange() {
		return mSharedPrefs.getBoolean(KEY_DISPLAYTOASTONPROFILECHANGE, true);
	}
	public void setDisplayToastOnProfileChange(boolean value) {
		setBooleanValue(KEY_DISPLAYTOASTONPROFILECHANGE, value);
	}

	public boolean isVibrateOnProfileChange() {
		return mSharedPrefs.getBoolean(KEY_VIBRATEONPROFILECHANGE, true);
	}
	public void setVibrateOnProfileChange(boolean value) {
		setBooleanValue(KEY_VIBRATEONPROFILECHANGE, value);
	}

	public boolean isPlaySoundOnVolumeChange() {
		return mSharedPrefs.getBoolean(KEY_PLAYSOUNDONVOLUMECHANGE, true);
	}
	public void setPlaySoundOnVolumeChange(boolean value) {
		setBooleanValue(KEY_PLAYSOUNDONVOLUMECHANGE, value);
	}

	public boolean isSoundLevelAlertHack() {
		return mSharedPrefs.getBoolean(KEY_SOUNDLEVELALERTHACK, true);
	}
	public void setSoundLevelAlertHack(boolean value) {
		setBooleanValue(KEY_SOUNDLEVELALERTHACK, value);
	}

	public boolean isDisplayToastOnVolumeLock() {
		return mSharedPrefs.getBoolean(KEY_DISPLAYTOASTONVOLUMELOCK, false);
	}
	public void setDisplayToastOnVolumeLock(boolean value) {
		setBooleanValue(KEY_DISPLAYTOASTONVOLUMELOCK, value);
	}

	public void writeToText(BufferedWriter wtr) throws IOException {
		Map <String, ?> map = mSharedPrefs.getAll();
		wtr.write(PREFS_START); wtr.newLine();
		for (String key : map.keySet()) {
			wtr.write(key + "=" + map.get(key)); wtr.newLine();
		}
		wtr.write(PREFS_END); wtr.newLine();
	}

	public boolean hasVolumeLinkNotification() {
		return mHasVolumeLinkNotification;
	}

	public void setVolumeLinkNotification(boolean link) {
		if (! hasVolumeLinkNotification()) {
			return;
		}
		int value = 0;
		if (link) {
			value = 1;
		}
		android.provider.Settings.System.putInt(mContext.getContentResolver(), mKeyVolumeLinkNotification, value);
	}

	public boolean isVolumeLinkNotification() {
		if (! hasVolumeLinkNotification()) {
			return true;
		}
		int linkNotification = 	android.provider.Settings.System.getInt(mContext.getContentResolver(), mKeyVolumeLinkNotification, 1);
		return linkNotification == 1;
	}

	public void setFromText(BufferedReader rdr) throws IOException {
    	String line;
    	boolean started = false;
		while ((line = rdr.readLine()) != null) {
			if (started) {
				if (PREFS_END.equals(line)) {
					break;
				}
				String[] tmp = line.split("=", 2);
				if (tmp.length < 2) {
					continue;
				}
				setValue(tmp[0], tmp[1]);
			}
			else {
				if (PREFS_START.equals(line)) {
					started = true;
				}
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// request backup
		BackupManager.dataChanged(mContext.getPackageName());
	}
}
