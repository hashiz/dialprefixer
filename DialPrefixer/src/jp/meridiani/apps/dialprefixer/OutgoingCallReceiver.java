package jp.meridiani.apps.dialprefixer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OutgoingCallReceiver extends BroadcastReceiver {

	private static final String TAG = "OutgoingCallReceiver";

	public OutgoingCallReceiver() {
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Prefs prefs = Prefs.getInstance(context);
		Log.d(TAG, "onReceive");
		if (!prefs.isEnableAddPrefix()) {
			return;
		}
		if (!intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			return;
		}
		if (!intent.hasExtra(Intent.EXTRA_PHONE_NUMBER)) {
			return;
		}
		String origNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		if (origNumber == null || origNumber.isEmpty()) {
			return;
		}
		RuleStore store = RuleStore.getInstance(context);
		String newNumber = origNumber;
		ruleEvalute : for ( RuleEntry ruleEntry : store.listRules(true) ) {
			String pattern = ruleEntry.getPatternEvaluted(prefs);
			switch (ruleEntry.getAction()) {
			case CHECK:
				if (newNumber.matches(pattern) ^ ruleEntry.isNegate()) {
					Log.d(TAG, "hit pattern=" + pattern);
					if (!ruleEntry.isContinue()) {
						break ruleEvalute;
					}
				}
				break;
			case REWRITE:
				if (newNumber.matches(pattern)) {
					Log.d(TAG, "hit pattern=" + pattern);
					newNumber = newNumber.replaceFirst(pattern, ruleEntry.getReplacementEvaluted(prefs));
					if (!ruleEntry.isContinue()) {
						break ruleEvalute;
					}
				}
				break;
			}
		}
		Log.d(TAG, "origNumber=" + origNumber + ",newNumber=" + newNumber);
		setResultData(newNumber);
	}
}
