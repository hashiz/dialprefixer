package jp.meridiani.apps.dialprefixer;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentActivity;

public class PreferencesActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();

    }

	public static class PrefsFragment extends PreferenceFragment {
		Prefs mPrefs;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			mPrefs = Prefs.getInstance(getActivity());
			addPreferencesFromResource(mPrefs.getPrefsResId());

		}
	}
}
