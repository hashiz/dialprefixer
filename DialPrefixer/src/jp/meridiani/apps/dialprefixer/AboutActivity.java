package jp.meridiani.apps.dialprefixer;

import jp.meridiani.apps.dialprefixer.R;
import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends Activity {

	private String mVersionName = null;
	private int mVersionCode = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		try {
			mVersionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			mVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		TextView version = (TextView)findViewById(R.id.about_version);
		version.setText(String.format("%s(%d)", mVersionName, mVersionCode));
	}
}
