package jp.meridiani.apps.dialprefixer;

import java.util.Map;

import jp.meridiani.apps.dialprefixer.Prefs.Prefix;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.Manifest;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

public class CallLogManager {
	private final static String TAG = "CallLogManager";

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

	private enum Range {
		LAST,
		ALL,
	};

	public static boolean rewriteCallLog(Context context) {
		return rewriteCallLog(context, Range.ALL, false);
	}
	
	public static boolean rewriteLastCallLog(Context context, boolean showToast) {
		return rewriteCallLog(context, Range.LAST, showToast);
	}
	
	private static boolean rewriteCallLog(Context context, Range range, boolean showToast) {
		ContentResolver resolver = context.getContentResolver();
		Prefs prefs = Prefs.getInstance(context);
		Cursor log = null;
		String id = null;
		String number = null;
		int rows = 0;
		boolean rewritten = false;

		// permit/deny caller id
		StringBuffer regex = new StringBuffer(String.format("^(%1$s|%2$s|)", prefs.getPrefixNoSendCallerId(), prefs.getPrefixSendCallerId()));

		// dial prefix
		regex.append('(');
		String pipe = "";
		Map<Prefix, String> prefixes = prefs.getPrefixes();
		for (String prefix : prefixes.values()) {
			prefix = prefix.trim();
			if (!prefix.isEmpty()) {
				regex.append(pipe);
				regex.append(prefix);
				pipe = "|";
			}
		}
		regex.append(')');

		try {
			log = resolver.query(CallLog.Calls.CONTENT_URI,
								COLS,
								TYPEIS,
								OUTGOINGTYPE,
								CallLog.Calls.DEFAULT_SORT_ORDER);
			while (log.moveToNext()) {
				id = log.getString(_ID);
				number = log.getString(NUMBER);
				Log.d(TAG, "query: id=" + id + ", number=" + number);
				String newNumber = number.replaceFirst(regex.toString(), "$1");
				if (!newNumber.equals(number)) {
					Log.d(TAG, "rewrite:" + number +" to " + newNumber);
					ContentValues values = new ContentValues();
					values.put(CallLog.Calls.NUMBER, newNumber);
					rows = rows + resolver.update(CallLog.Calls.CONTENT_URI,
												values,
												_IDIS,
												new String[] {id});
					if (showToast) {
						Toast.makeText(context, "rewrite " + number + " to " + newNumber, Toast.LENGTH_SHORT).show();
					}
					rewritten = true;
				}
				else {
					if (showToast) {
						Toast.makeText(context, "no rewrite " + number, Toast.LENGTH_SHORT).show();
					}
				}
				if (range == Range.LAST) {
					break;
				}
			}
		}
		finally {
			if (log != null) {
				log.close();
			}
		}
		Log.d(TAG, "rewrite:" + rows);
		return rewritten;
	}
}
