package jp.meridiani.apps.dialprefixer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
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

	static final String KEY_ENABLE_ADD_PREFIX    = "enable_add_prefix";
	static final String KEY_PREFIX_A             = "prefix_a";
	static final String KEY_PREFIX_B             = "prefix_b";
	static final String KEY_PREFIX_C             = "prefix_c";
	static final String KEY_CALLERID_DENY        = "callerid_deny";
	static final String KEY_CALLERID_PERMIT      = "callerid_permit";
	static final String KEY_CALLLOG_DELETEPREFIX = "calllog_deleteprefix";
	static final String KEY_CONFIRMNUMBER        = "confirmnumber";
    static final String KEY_DISPLAYNUMBER        = "displaynumber";

	private Context mContext;
	private SharedPreferences mSharedPrefs;

	public static enum Prefix {
		A,
		B,
		C
	}

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
		Editor editor = mSharedPrefs.edit();
		editor.putString(key, value);
		editor.apply();
	}

	public boolean isEnableAddPrefix() {
		return mSharedPrefs.getBoolean(KEY_ENABLE_ADD_PREFIX, false);
	}

	private String getPrefixKey(Prefix p) {
		String key = null;
		switch (p) {
		case A:
			key = KEY_PREFIX_A;
			break;
		case B:
			key = KEY_PREFIX_B;
			break;
		case C:
			key = KEY_PREFIX_C;
			break;
		}
		return key;
	}

	public String getPrefix(Prefix p) {
		String key = getPrefixKey(p);
		return mSharedPrefs.getString(key, "");
	}

	public Map<Prefix, String> getPrefixes() {
		LinkedHashMap<Prefix, String> map = new LinkedHashMap<Prefs.Prefix, String>();
		for ( Prefix p : Prefix.values() ) {
			map.put(p, getPrefix(p));
		}
		return map;
	}

	public String getCallerIdDeny() {
		return mSharedPrefs.getString(KEY_CALLERID_DENY, "");
	}

	public String getCallerIdPermit() {
		return mSharedPrefs.getString(KEY_CALLERID_PERMIT, "");
	}

	public boolean isCallLogDeletePrefix() {
		return mSharedPrefs.getBoolean(KEY_CALLLOG_DELETEPREFIX, false);
	}

	public boolean isConfirmNumber() {
		return mSharedPrefs.getBoolean(KEY_CONFIRMNUMBER, true);
	}

	public boolean isDisplayNumber() {
		return mSharedPrefs.getBoolean(KEY_DISPLAYNUMBER, true);
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
