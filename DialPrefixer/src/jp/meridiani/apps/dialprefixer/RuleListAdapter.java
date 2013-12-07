package jp.meridiani.apps.dialprefixer;

import java.util.ArrayList;

import jp.meridiani.apps.dialprefixer.DragDropListItem.DragDropListener;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RuleListAdapter extends BaseAdapter implements DragDropListAdapter {

	private Object mLock = new Object();

	private ArrayList<RuleEntry> mList = null;
	private LayoutInflater mInflater = null;
    private boolean mNotifyOnChange = true;

	private int mResource = -1;
	private int mTextViewId = -1;

	public RuleListAdapter(Context context, int resource, int textViewId) {
		this(context, resource, textViewId, new ArrayList<RuleEntry>());
	}

	public RuleListAdapter(Context context, int resource, int textViewId, ArrayList<RuleEntry> profileList) {
		super();
		mResource = resource;
		mTextViewId = textViewId;

		mList = profileList;
		mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View itemView;
		if (convertView == null) {
			itemView = mInflater.inflate(mResource, parent, false);
		}
		else {
			itemView = convertView;
		}
		TextView textView = (TextView)itemView.findViewById(mTextViewId);
		if (textView != null) {
			textView.setText(getItem(position).toString());
		}
		if (itemView instanceof DragDropListItem && parent instanceof DragDropListener) {
			((DragDropListItem)itemView).setDragDropListener((DragDropListener)parent);
		}
		return itemView;
	};

	public void setProfileList(ArrayList<RuleEntry> list) {
		synchronized (mLock) {
			mList = list;
		}
        if (mNotifyOnChange) {
        	notifyDataSetChanged();
        }
	}

	public ArrayList<RuleEntry> getProfileList() {
		return mList;
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public RuleEntry getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setNotifyOnChange(boolean notifyOnChange) {
		mNotifyOnChange = notifyOnChange;
	}

	public void add(RuleEntry profile) {
		addItem(profile);
	}

	public void addItem(RuleEntry profile) {
		synchronized (mLock) {
			mList.add(profile);
		}
        if (mNotifyOnChange) {
        	notifyDataSetChanged();
        }
	}

	public void clear() {
		synchronized (mLock) {
			mList.clear();
		}
        if (mNotifyOnChange) {
        	notifyDataSetChanged();
        }
	}

	@Override
	public boolean moveItem(DragDropListView listView, int srcPos, int dstPos) {
		if (srcPos == dstPos) {
			return false;
		}
		synchronized (mLock) {
			int max = getCount();
			if (srcPos < 0 || max - 1 < srcPos) {
				// index over
				return false;
			}
			if (dstPos < 0 || max - 1 < dstPos) {
				// index over
				return false;
			}
	
			RuleEntry tmpProfile = mList.get(srcPos);
			boolean checked = listView.isItemChecked(srcPos);
			if (srcPos < dstPos) {
				// up to down
				for (int pos = srcPos; pos < dstPos; pos++) {
					mList.set(pos, mList.get(pos+1));
					listView.setItemChecked(pos, listView.isItemChecked(pos+1));
					mList.get(pos).setOrder(pos);
				}
			}
			else {
				// down to up
				for (int pos = srcPos; pos > dstPos; pos--) {
					mList.set(pos, mList.get(pos-1));
					listView.setItemChecked(pos, listView.isItemChecked(pos-1));
					mList.get(pos).setOrder(pos);
				}
			}
			mList.set(dstPos, tmpProfile);
			listView.setItemChecked(dstPos, checked);
			mList.get(dstPos).setOrder(dstPos);
		}

        if (mNotifyOnChange) {
        	notifyDataSetChanged();
        }
		return true;
	}

 }