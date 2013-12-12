package jp.meridiani.apps.dialprefixer;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PreferencesActivity extends FragmentActivity {

    private static final String[] EDIT_TEXT_PREFERENCES = {
        Prefs.KEY_PREFIX,
        Prefs.KEY_CALLERID_DENY,
        Prefs.KEY_CALLERID_PERMIT,
    };

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

			for (String key : EDIT_TEXT_PREFERENCES) {
				Preference p = findPreference(key);
				if (p instanceof EditTextPreference) {
					p.setSummary(((EditTextPreference)p).getText());
				}
				p.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference p, Object newValue) {
						if (p instanceof EditTextPreference) {
							p.setSummary((String)newValue);
						}
						return true;
					}
				});
			}
			return rootView;
		}
	}
}
