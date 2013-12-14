package jp.meridiani.apps.dialprefixer;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.CallLog;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateReceiver extends BroadcastReceiver {
	private final static String TAG = "PhoneStateReceiver";

	public PhoneStateReceiver() {
	}

	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.d(TAG, "onReceive:" + intent.getAction());
		if (!intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
			return;
		}
		if (!intent.hasExtra(TelephonyManager.EXTRA_STATE)) {
			return;
		}
		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		if (state == null) {
			return;
		}
		if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
			Prefs prefs = Prefs.getInstance(context);
			if (!prefs.isCallLogDeletePrefix()) {
				return;
			}
			final ContentResolver resolver = context.getContentResolver();
			resolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, new ContentObserver(new Handler()) {
				@Override
				public void onChange(boolean selfChange) {
					super.onChange(selfChange);
					resolver.unregisterContentObserver(this);
					rewriteLastCallLog(context);
				}
			});
			return;
		}
	}

	private final static int _ID    = 0;
	private final static int TYPE   = 1;
	private final static int NUMBER = 2;
	private final static int DATE   = 3;
	private final static String[] COLS = {
			CallLog.Calls._ID,
			CallLog.Calls.TYPE,
			CallLog.Calls.NUMBER,
			CallLog.Calls.DATE,
	};
	private final static String TYPEIS = CallLog.Calls.TYPE + "=?";
	private final static String [] OUTGOINGTYPE = {
		Integer.toString(CallLog.Calls.OUTGOING_TYPE),
	};
	private final static String _IDIS = CallLog.Calls._ID + "=?";
	private static void rewriteLastCallLog(Context context) {
		ContentResolver resolver = context.getContentResolver();
		Prefs prefs = Prefs.getInstance(context);
		Cursor log = null;
		String id = null;
		String number = null;
		try {
			log = resolver.query(CallLog.Calls.CONTENT_URI,
								COLS,
								TYPEIS,
								OUTGOINGTYPE,
								CallLog.Calls.DEFAULT_SORT_ORDER);
			if (!log.moveToFirst()) {
				return;
			}
			id = log.getString(_ID);
			number = log.getString(NUMBER);
		}
		finally {
			if (log != null) {
				log.close();
			}
		}
		if (id == null || number == null) {
			return;
		}
		Log.d(TAG, "query: id=" + id + ", number=" + number);

		String regex = String.format("^(%1$s|%2$s|)(%3$s)", prefs.getCallerIdDeny(), prefs.getCallerIdPermit(), prefs.getPrefix());
		
		String rewrite = number.replaceFirst(regex, "$1");
		if (!rewrite.equals(number)) {
			Log.d(TAG, "rewrite:" + number +" to " + rewrite);
			ContentValues values = new ContentValues();
			values.put(CallLog.Calls.NUMBER, rewrite);
			int rows = resolver.update(CallLog.Calls.CONTENT_URI,
										values,
										_IDIS,
										new String[] {id});
			if (rows < 1) {
				return;
			}
			Log.d(TAG, "rewrite:" + rows);
		}
	}
}
