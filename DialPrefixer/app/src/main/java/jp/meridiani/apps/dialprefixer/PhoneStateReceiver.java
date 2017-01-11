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
		if (!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
			return;
		}
		if (!intent.hasExtra(TelephonyManager.EXTRA_STATE)) {
			return;
		}
		String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		Log.d(TAG, "onReceive: State: " + state);
		if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
//			final Prefs prefs = Prefs.getInstance(context);
//			if (prefs.isCallLogDeletePrefix()) {
//				context.startService(new Intent(context, ObserverService.class));
//			}
		}
	}

}
