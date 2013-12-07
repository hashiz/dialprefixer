package jp.meridiani.apps.dialprefixer;

import jp.meridiani.apps.dialprefixer.RuleEntry.RuleAction;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public abstract class RuleEditFragmentBase extends Fragment {

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
		Spinner ringerModeView = (Spinner)rootView.findViewById(R.id.action_value);

		RuleActionAdapter adapter = new RuleActionAdapter(activity,
				android.R.layout.simple_spinner_item);
		adapter.add(new ActionItem(activity, RuleAction.REWRITE));
		adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		ringerModeView.setAdapter(adapter);

	};

	abstract protected String getRuleName();
	abstract protected void setRuleName(String name);

	abstract protected RuleAction getRuleAction();
	abstract protected void setRuleAction(RuleAction action);

	abstract protected boolean getRuleContinue();
	abstract protected void setRuleContinue(boolean cont);

	abstract protected String getRulePattern();
	abstract protected void setRulePattern(String pattern);

	abstract protected boolean getRuleNegate();
	abstract protected void setRuleNegate(boolean negate);

}
