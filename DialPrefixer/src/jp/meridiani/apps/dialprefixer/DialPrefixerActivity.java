package jp.meridiani.apps.dialprefixer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;

public class DialPrefixerActivity extends FragmentActivity {
	public static final String TAG_RULE_LIST = "rule_list_fragment";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.activity_main, RuleListFragment.newInstance(), TAG_RULE_LIST);
		transaction.commit();
	}
	
	@Override
	protected void onResume() {
		super.onResume();

	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add_rule:
			Intent i = new Intent(this, RuleEditActivity.class);
			i.putExtra(RuleEditActivity.EXTRA_RULE_ENTRY, new RuleEntry());
			startActivity(i);
			return true;
		case R.id.action_rewrite_calllog:
			CallLogManager.rewriteCallLog(getApplicationContext());
			return true;
		case R.id.action_reload_defaults:
			RuleStore.getInstance(this).reloadDefaultRules();
			((RuleListFragment)(getSupportFragmentManager().findFragmentByTag(TAG_RULE_LIST))).updateList();
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, PreferencesActivity.class));
			return true;
		case R.id.action_about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		}
		return false;
	}

}
