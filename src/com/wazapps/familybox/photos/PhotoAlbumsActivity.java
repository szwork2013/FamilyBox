package com.wazapps.familybox.photos;

import com.wazapps.familybox.ActivityWithDrawer;
import com.wazapps.familybox.R;
import com.wazapps.familybox.newsfeed.NewsfeedActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

public class PhotoAlbumsActivity extends ActivityWithDrawer {
	static final String TAG_PHOTO_ALBUM = "photoAlbum";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle(R.string.photo_albums);
		overridePendingTransition(R.anim.enter, R.anim.exit); //TODO: handle transition animations in a better way
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.content_frame, new PhotoAlbumFragment(), TAG_PHOTO_ALBUM);
		ft.commit();
	}

	public void initDrawer() {
		super.initDrawer();
		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
	}

	@Override
	public void selectItem(int position) {
		mPosition = position;
		switch (position) {
		case MY_PROFILE_POS:

			break;
		case FAMILY_TREE_POS:

			break;
		case PHOTOS_POS:
			//TODO: THIS IS BAD!!!!! makes horrible bugs and breaks the tab interface
			//fix this
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.content_frame, new PhotoAlbumFragment(), TAG_PHOTO_ALBUM);
			ft.commit();
			break;
		case NOTES_POS:

			break;
		case NEWS_POS:
			Intent intent = new Intent(this, NewsfeedActivity.class);
			startActivity(intent);
			break;
		case EXPAND_NETWORK_POS:

			break;

		default:
			break;
		}
		mDrawerLayout.closeDrawer(mDrawerList);

	}

}
