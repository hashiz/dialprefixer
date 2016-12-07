package jp.meridiani.apps.dialprefixer;

public interface RuleEditCallback {
	public void onProfileEditPositive(RuleEntry newProfile);
	public void onProfileEditNegative();
}
