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
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class RuleEditFragment extends Fragment {

	private static final String BUNDLE_RULE_ENTRY = "ruleEntry";

	private RuleEntry mRuleEntry = null;
	
	private class ActionItem {
		private RuleAction mRuleAction = null;
		private String     mString = null;

		public ActionItem(Context context, RuleAction action) {
			mRuleAction = action;
			switch (mRuleAction) {
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

		View rootView = getView();

		// set value

		// name
		EditText name = (EditText)rootView.findViewById(R.id.name_value);
		name.setText(mRuleEntry.getName());
		name.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence paramCharSequence,
					int paramInt1, int paramInt2, int paramInt3) {
				// nop
			}

			@Override
			public void onTextChanged(CharSequence paramCharSequence,
					int paramInt1, int paramInt2, int paramInt3) {
				// nop
			}

			@Override
			public void afterTextChanged(Editable paramEditable) {
				// nop
			}
			
		});

		// action
		Spinner action = (Spinner)rootView.findViewById(R.id.action_value);
		action.setSelection(((RuleActionAdapter)action.getAdapter()).getPosition(mRuleEntry.getAction()));

		// continue
		CheckBox cont = (CheckBox)rootView.findViewById(R.id.continue_value);
		cont.setChecked(mRuleEntry.isContinue());

		// pattern
		EditText pattern = (EditText)rootView.findViewById(R.id.pattern_value);
		pattern.setText(mRuleEntry.getPattern());

		// negate
		CheckBox negate = (CheckBox)rootView.findViewById(R.id.negate_value);
		negate.setChecked(mRuleEntry.isNegate());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(BUNDLE_RULE_ENTRY, mRuleEntry);
	}
}
