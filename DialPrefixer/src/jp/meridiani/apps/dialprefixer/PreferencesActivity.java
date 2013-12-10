package jp.meridiani.apps.dialprefixer;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PreferencesActivity extends FragmentActivity {

    private static final String KEY_PREFIX               = "prefix";
    private static final String KEY_CALLERID_DENY        = "callerid_deny";
    private static final String KEY_CALLERID_PERMIT      = "callerid_permit";
    private static final String KEY_CALLLOG_DELETEPREFIX = "calllog_deleteprefix";
    private static final String KEY_CONFIRMNUMBER        = "confirmnumber";
    private static final String KEY_DISPLAYNUMBER        = "displaynumber";

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

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = super.onCreateView(inflater, container, savedInstanceState);

			findPreference(KEY_PREFIX);
			return rootView;
		}
	}
}
