package jp.meridiani.apps.dialprefixer;

import jp.meridiani.apps.dialprefixer.RuleEntry.RuleAction;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;

public class RuleEditFragment extends Fragment {

	private static final String BUNDLE_RULE_ENTRY = "ruleEntry";

	private RuleEntry mRuleEntry = null;

	// listener
	private TextWatcher             mNameChangedListener;
	private TextWatcher             mPatternChangedListener;
	private TextWatcher             mReplacementChangedListener;

	private class ActionItem {
		private RuleAction mRuleAction = null;
		private String     mString = null;

		public ActionItem(Context context, RuleAction action) {
			mRuleAction = action;
			switch (mRuleAction) {
			case CHECK:
				mString = context.getString(R.string.rule_action_check);
				break;
			case REWRITE:
				mString = context.getString(R.string.rule_action_rewrite);
				break;
			}
		}

		@Override
		public String toString() {
			return mString;
		}

		public RuleAction getValue() {
			return mRuleAction;
		}
	}

	private class RuleActionAdapter extends ArrayAdapter<ActionItem> {

		public RuleActionAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		public int getPosition(RuleAction action) {
			for (int i = 0; i < this.getCount(); i++ ) {
				if (this.getItem(i).getValue() == action) {
					return i;
				}
			}
			return -1;
		}
		
	}

	public static RuleEditFragment newInstance(RuleEntry ruleEntry) {
		RuleEditFragment instance = new RuleEditFragment();
		Bundle args = new Bundle();
		args.putParcelable(BUNDLE_RULE_ENTRY, ruleEntry);
		instance.setArguments(args);
		return instance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mRuleEntry = savedInstanceState.getParcelable(BUNDLE_RULE_ENTRY);
		}
		else {
			mRuleEntry = getArguments().getParcelable(BUNDLE_RULE_ENTRY);
		}
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_rule_edit,
				container, false);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		View rootView = getView();
		Activity activity = getActivity();

		// Rule Action Item
		Spinner actionView = (Spinner)rootView.findViewById(R.id.action_value);

		RuleActionAdapter adapter = new RuleActionAdapter(activity,
				android.R.layout.simple_spinner_item);
		adapter.add(new ActionItem(activity, RuleAction.CHECK));
		adapter.add(new ActionItem(activity, RuleAction.REWRITE));
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		actionView.setAdapter(adapter);

	};

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.rule_edit, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_save_rule:
			RuleStore.getInstance(getActivity()).storeRuleEntry(mRuleEntry);
		case R.id.action_cancel_rule:
			getActivity().finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();

		final View rootView = getView();

		// set value

		// enable
		CheckBox enable = (CheckBox)rootView.findViewById(R.id.enable_value);
		enable.setChecked(mRuleEntry.isEnable());
		enable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mRuleEntry.setEnable(isChecked);
			}
		});
		
		// name
		EditText name = (EditText)rootView.findViewById(R.id.name_value);
		name.setText(mRuleEntry.getName());
		name.addTextChangedListener(mNameChangedListener = new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				mRuleEntry.setName(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// NOP
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,	int count) {
				// NOP
			}
		});
		if (!mRuleEntry.isUserRule()) {
			name.setEnabled(false);
		}

		// action
		Spinner action = (Spinner)rootView.findViewById(R.id.action_value);
		action.setSelection(((RuleActionAdapter)action.getAdapter()).getPosition(mRuleEntry.getAction()));
		action.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				RuleAction action = ((ActionItem)parent.getAdapter().getItem(position)).getValue();
				mRuleEntry.setAction(action);
				switch (action) {
				case CHECK:
					rootView.findViewById(R.id.negate_container).setVisibility(View.VISIBLE);
					rootView.findViewById(R.id.replacement_container).setVisibility(View.GONE);
					break;
				case REWRITE:
					rootView.findViewById(R.id.negate_container).setVisibility(View.GONE);
					rootView.findViewById(R.id.replacement_container).setVisibility(View.VISIBLE);
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// NOP
			}
		});
		if (!mRuleEntry.isUserRule()) {
			action.setEnabled(false);
		}

		// continue
		CheckBox cont = (CheckBox)rootView.findViewById(R.id.continue_value);
		cont.setChecked(mRuleEntry.isContinue());
		cont.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mRuleEntry.setContinue(isChecked);
			}
		});
		if (!mRuleEntry.isUserRule()) {
			cont.setEnabled(false);
		}

		// pattern
		EditText pattern = (EditText)rootView.findViewById(R.id.pattern_value);
		pattern.setText(mRuleEntry.getPattern());
		pattern.addTextChangedListener(mPatternChangedListener = new TextWatcher() {
			
			@Override
			public void afterTextChanged(Editable s) {
				mRuleEntry.setPattern(s.toString());
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
		});
		if (!mRuleEntry.isUserRule()) {
			pattern.setEnabled(false);
		}

		// negate
		CheckBox negate = (CheckBox)rootView.findViewById(R.id.negate_value);
		negate.setChecked(mRuleEntry.isNegate());
		negate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mRuleEntry.setNegate(isChecked);
			}
		});
		if (!mRuleEntry.isUserRule()) {
			negate.setEnabled(false);
		}

		// replacement
		EditText replacement = (EditText)rootView.findViewById(R.id.replacement_value);
		replacement.setText(mRuleEntry.getReplacement());
		replacement.addTextChangedListener(mReplacementChangedListener = new TextWatcher() {
			
			@Override
			public void afterTextChanged(Editable s) {
				mRuleEntry.setReplacement(s.toString());
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
		});
		if (!mRuleEntry.isUserRule()) {
			replacement.setEnabled(false);
		}

	}
	
	@Override
	public void onPause() {
		super.onPause();

		View rootView = getView();

		// delete listener

		// name
		EditText name = (EditText)rootView.findViewById(R.id.name_value);
		name.removeTextChangedListener(mNameChangedListener);

		// action
		Spinner action = (Spinner)rootView.findViewById(R.id.action_value);
		action.setOnItemSelectedListener(null);

		// continue
		CheckBox cont = (CheckBox)rootView.findViewById(R.id.continue_value);
		cont.setOnCheckedChangeListener(null);

		// pattern
		EditText pattern = (EditText)rootView.findViewById(R.id.pattern_value);
		pattern.removeTextChangedListener(mPatternChangedListener);

		// negate
		CheckBox negate = (CheckBox)rootView.findViewById(R.id.negate_value);
		negate.setOnCheckedChangeListener(null);

		// replacement
		EditText replacement = (EditText)rootView.findViewById(R.id.replacement_value);
		replacement.removeTextChangedListener(mReplacementChangedListener);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(BUNDLE_RULE_ENTRY, mRuleEntry);
	}
}
