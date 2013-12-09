package jp.meridiani.apps.dialprefixer;

import jp.meridiani.apps.dialprefixer.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class RuleEditActivity extends FragmentActivity {
	public static final String EXTRA_RULE_ENTRY = "jp.meridiani.apps.volumeprofile.EXTRA_RULE_ENTRY";
	public static final String TAG_RULE_EDIT = "rule_edit_fragment";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rule_edit);

		Intent intent = getIntent();
		if (intent == null) {
			finish();
			return;
		}
		RuleEntry ruleEntry = (RuleEntry)intent.getParcelableExtra(EXTRA_RULE_ENTRY);
		if (ruleEntry == null) {
			finish();
			return;
		}
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.activity_rule_edit, RuleEditFragment.newInstance(ruleEntry), TAG_RULE_EDIT);
		transaction.commit();
	}
}
