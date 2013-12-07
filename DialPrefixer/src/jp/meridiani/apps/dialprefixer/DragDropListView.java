package jp.meridiani.apps.dialprefixer;

import java.util.List;

import jp.meridiani.apps.dialprefixer.DragDropListItem.DragDropListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class DragDropListView extends ListView implements DragDropListener {

	private OnSortedListener mOnSortedListener = null;

	public interface OnSortedListener {
		public void onSorted(List<RuleEntry> list);
	}

	public DragDropListView(Context context) {
		super(context);
	}

	public DragDropListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DragDropListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setOnSortedListener(OnSortedListener listener) {
		mOnSortedListener = listener;
	}

	@Override
	public int getItemPosition(View dragItemView) {
		return getPositionForView(dragItemView);
	}

	@Override
	public void onDragLocation(int position, int y, int vCenter) {
		if (position == getFirstVisiblePosition()) {
			// top
			if (y < vCenter) {
				if (position > 0) {
					position--;
				}
				smoothScrollToPosition(position);
			}
		}
		else if (position == getLastVisiblePosition()) {
			// bottom
			if (y > vCenter) {
				if (position < getCount()-1) {
					position++;
				}
				smoothScrollToPosition(position);
			}
		}
	}

	@Override
	public void onItemDrop(int dragItemPosition, int dropItemPosition, DropAt pos) {
		RuleListAdapter adapter = (RuleListAdapter)getAdapter();
		if (dragItemPosition == dropItemPosition) {
			return;
		}
		else if (dragItemPosition < dropItemPosition) {
			if (pos == DropAt.ABOVE) {
				dropItemPosition--;
			}
		}
		else {
			if (pos == DropAt.BLOW) {
				dropItemPosition++;
			}
		}
		if (dragItemPosition == dropItemPosition) {
			return;
		}
		if (adapter.moveItem(this, dragItemPosition, dropItemPosition)) {
			if (mOnSortedListener != null) {
				mOnSortedListener.onSorted(adapter.getProfileList());
			}
		}
	}
}
