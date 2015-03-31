package jp.meridiani.apps.dialprefixer;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
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
		Log.d(TAG, "onReceive: Action:" + intent.getAction());
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
		Log.d(TAG, "onReceive: State: " + state);
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
					CallLogManager.rewriteLastCallLog(context);
				}
			});
			return;
		}
	}

}
