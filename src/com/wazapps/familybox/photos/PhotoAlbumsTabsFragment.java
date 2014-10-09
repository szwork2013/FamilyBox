package com.wazapps.familybox.photos;

import com.wazapps.familybox.R;
import com.wazapps.familybox.TabsFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PhotoAlbumsTabsFragment extends TabsFragment {
	private static final String MY_FAMILY = "myFamily";
	private static final String SHARED_ALBUM = "sharedAlbum";
	private static final String FAVORITES = "favorites";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
		}
		this.actionBar = getActivity().getActionBar();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (container == null) {
			return null;
		}
		// TODO get real data!
		AlbumItem[] albumList = { null, null, null, null, null, null };
		String albumName = "Temp Album Name ";
		PhotoItem[] tempData = { null, null, null, null, null, null, null,
				null, null, null, null, null, null, null, null, null, null,
				null };

		for (int i = 0; i < 18; i++) {
			tempData[i] = new PhotoItem("11.2.201" + i, "www.bla.com",
					"This is me and my friend Dan " + i);
		}

		for (int i = 0; i < 6; i++) {

			albumList[i] = new AlbumItem(tempData, albumName + i,
					"December 201" + i);
		}
		Bundle args = new Bundle();
		args.putParcelableArray(PhotoAlbumScreenFragment.ALBUM_ITEM_LIST, albumList);
		args.putParcelable(PhotoAlbumScreenFragment.ALBUM_ITEM, albumList[0]);
		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.fragment_photo_album_store_tabs, null);
		this.tabHost = (FragmentTabHost) rootView
				.findViewById(android.R.id.tabhost);
		this.tabHost.setup(getActivity(), getChildFragmentManager(),
				R.id.realtabcontent);

		// add tabs to the tabs storage
		this.tabHost.addTab(
				this.tabHost.newTabSpec(MY_FAMILY).setIndicator(
						makeTabIndicator(R.string.photos_tab_my_family)),
				MyFamilyAlbumFragment.class, args);

		this.tabHost.addTab(
				this.tabHost.newTabSpec(SHARED_ALBUM).setIndicator(
						makeTabIndicator(R.string.photos_tab_shared_album)),
				SharedAlbumFragment.class, this.getArguments());

		this.tabHost.addTab(
				tabHost.newTabSpec(FAVORITES).setIndicator(
						makeTabIndicator(R.string.photos_tab_favorites)),
				PhotoAlbumScreenFragment.class, args);

		return this.tabHost;
	}

}