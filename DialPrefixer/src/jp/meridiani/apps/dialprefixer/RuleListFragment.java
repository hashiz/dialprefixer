package jp.meridiani.apps.dialprefixer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jp.meridiani.apps.dialplefixer.R;
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

		ArrayList<RuleEntry> plist = RuleStore.getInstance(context).listRules();
		int selPos = -1;
		UUID curId = RuleStore.getInstance(context).getCurrentProfile() ;
		for ( RuleEntry profile : plist) {
			mAdapter.add(profile);
			if (curId != null && curId.equals(profile.getUuid())) {
				selPos = mAdapter.getCount() - 1;
			}
		}
		if (selPos >= 0) {
			mRuleListView.setItemChecked(selPos, true);
		}

		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		RuleListAdapter adapter = (RuleListAdapter)parent.getAdapter();
		RuleEntry profile = adapter.getItem(pos);
		new AudioUtil(parent.getContext()).applyProfile(profile);
		RuleStore.getInstance(getActivity()).setCurrentProfile(profile.getUuid());
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.profile, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		int pos = info.position;
		ListView profileListView = (ListView)getView().findViewById(R.id.profile_list);
		RuleEntry profile = (RuleEntry)profileListView.getAdapter().getItem(pos);
		switch (item.getItemId()) {
		case R.id.action_rename_profile:
			ProfileNameDialog dialog = ProfileNameDialog.newInstance(profile, this, null, getString(R.string.input_dialog_rename_button), null);
			dialog.show(getFragmentManager(), dialog.getClass().getCanonicalName());
			return true;
		case R.id.action_edit_profile:
			Intent intent = new Intent(getActivity(), ProfileEditActivity.class);
			intent.putExtra(ProfileEditActivity.EXTRA_PROFILE, profile);
			startActivity(intent);
			return true;
		case R.id.action_delete_profile:
			RuleStore.getInstance(getActivity()).deleteProfile(profile.getUuid());
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
}
