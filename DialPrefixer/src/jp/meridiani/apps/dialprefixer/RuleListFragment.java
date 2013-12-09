package jp.meridiani.apps.dialprefixer;

import java.util.ArrayList;
import java.util.List;

import jp.meridiani.apps.dialprefixer.DragDropListView.OnSortedListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class RuleListFragment extends Fragment implements OnItemClickListener, RuleEditCallback, OnSortedListener {

	RuleListAdapter  mAdapter = null;
	DragDropListView mRuleListView = null;
	
	public static RuleListFragment newInstance() {
    	return new RuleListFragment();
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_rule_list,
				container, false);
		mAdapter = new RuleListAdapter(getActivity(),
				R.layout.rule_list_item, R.id.rule_item_summary);
		mRuleListView = (DragDropListView)rootView.findViewById(R.id.rule_list);
		mRuleListView.setAdapter(mAdapter);
		mRuleListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
		mRuleListView.setOnItemClickListener(this);
		mRuleListView.setOnSortedListener(this);
		registerForContextMenu(mRuleListView);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateList();
	}

	public void updateList() {
		Context context = getActivity();

		mAdapter.clear();

		ArrayList<RuleEntry> ruleList = RuleStore.getInstance(context).listRules(false);
		for ( RuleEntry ruleEntry : ruleList) {
			mAdapter.add(ruleEntry);
		}
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		RuleListAdapter adapter = (RuleListAdapter)parent.getAdapter();
		RuleEntry ruleEntry = adapter.getItem(pos);
		Intent intent = new Intent(getActivity(), RuleEditActivity.class);
		intent.putExtra(RuleEditActivity.EXTRA_RULE_ENTRY, ruleEntry);
		startActivity(intent);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.rule_list_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		int pos = info.position;
		ListView ruleListView = (ListView)getView().findViewById(R.id.rule_list);
		RuleEntry ruleEntry = (RuleEntry)ruleListView.getAdapter().getItem(pos);
		RuleStore ruleStore = RuleStore.getInstance(getActivity());
		switch (item.getItemId()) {
		case R.id.action_edit_rule:
			startRuleEdit(ruleEntry);
			return true;
		case R.id.action_toggle_enable_rule:
			ruleEntry.setEnable(!ruleEntry.isEnable());
			ruleStore.storeRuleEntry(ruleEntry);
			updateList();
			return true;
		case R.id.action_delete_rule:
			ruleStore.deleteRule(ruleEntry.getUuid());
			updateList();
			return true;
		}
		return false;
	}

	@Override
	public void onProfileEditPositive(RuleEntry newProfile) {
		RuleStore.getInstance(getActivity()).storeRuleEntry(newProfile);
		updateList();
	}

	@Override
	public void onProfileEditNegative() {
	}

	@Override
	public void onSorted(List<RuleEntry> list) {
		RuleStore.getInstance(getActivity()).updateOrder(list);
	}

	private void startRuleEdit(RuleEntry ruleEntry) {
		Intent intent = new Intent(getActivity(), RuleEditActivity.class);
		intent.putExtra(RuleEditActivity.EXTRA_RULE_ENTRY, ruleEntry);
		startActivity(intent);
	}
}
