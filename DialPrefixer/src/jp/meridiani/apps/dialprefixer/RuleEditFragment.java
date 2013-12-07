package jp.meridiani.apps.dialprefixer;

import jp.meridiani.apps.dialprefixer.RuleEntry.RuleAction;
import android.os.Bundle;

public class RuleEditFragment extends RuleEditFragmentBase {

	private static final String BUNDLE_RULE_ENTRY = "ruleEntry";

	private RuleEntry mRuleEntry = null;
	
	public static RuleEditFragment newInstance(RuleEntry ruleEntry) {
		RuleEditFragment instance = new RuleEditFragment();
		Bundle args = new Bundle();
		args.putParcelable(BUNDLE_RULE_ENTRY, ruleEntry);
		instance.setArguments(args);
		return instance;
	}

	@Override
	protected String getRuleName() {
		return mRuleEntry.getName();
	}

	@Override
	protected void setRuleName(String name) {
		mRuleEntry.setName(name);
	}

	@Override
	protected RuleAction getRuleAction() {
		return mRuleEntry.getAction();
	}

	@Override
	protected void setRuleAction(RuleAction action) {
		mRuleEntry.setAction(action);
	}

	@Override
	protected boolean getRuleContinue() {
		return mRuleEntry.isContinue();
	}

	@Override
	protected void setRuleContinue(boolean cont) {
		mRuleEntry.setContinue(cont);
	}

	@Override
	protected String getRulePattern() {
		return mRuleEntry.getPattern();
	}

	@Override
	protected void setRulePattern(String pattern) {
		mRuleEntry.setPattern(pattern);
	}

	@Override
	protected boolean getRuleNegate() {
		return mRuleEntry.isNegate();
	}

	@Override
	protected void setRuleNegate(boolean negate) {
		mRuleEntry.setNegate(negate);
	}

}
