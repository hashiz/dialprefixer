package jp.meridiani.apps.dialprefixer;

public interface DragDropListAdapter {

	// @param listView listview
	// @param srcPos move this item
	// @param dstPos insert item to above
	boolean moveItem(DragDropListView listView, int srcPos, int dstPos);

}
