package jp.meridiani.apps.dialprefixer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class Prefs implements OnSharedPreferenceChangeListener {
	private static Prefs mInstance = null;

	private static final String PREFS_START = "<preferences>";
	private static final String PREFS_END   = "</preferences>";

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

	public void writeToText(BufferedWriter wtr) throws IOException {
		Map <String, ?> map = mSharedPrefs.getAll();
		wtr.write(PREFS_START); wtr.newLine();
		for (String key : map.keySet()) {
			wtr.write(key + "=" + map.get(key)); wtr.newLine();
		}
		wtr.write(PREFS_END); wtr.newLine();
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
