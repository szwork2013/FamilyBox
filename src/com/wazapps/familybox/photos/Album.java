package com.wazapps.familybox.photos;

import android.content.Context;
import android.widget.Toast;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wazapps.familybox.R;
import com.wazapps.familybox.handlers.UserHandler;
import com.wazapps.familybox.newsfeed.NewsItem;
import com.wazapps.familybox.util.LogUtils;

//a class that hold all photo albums data
@ParseClassName("Album")
public class Album extends ParseObject {

	private int photoListSize;
	private int upLoadPhotoCounter = 0;
	private Context context;
	private int errorPhotoCounter = 0;

	public Album() {
	}

	public Album(Context context, int photoListSize) {
		super();
		this.context = context;
		this.photoListSize = photoListSize;
	}

	public String getAlbumFamily() {
		return getString("family");
	}

	public int getAlbumPhotoCount() {
		return getInt("albumPhotoCount");
	}

	public void setAlbumPhotoCount(int albumPhotoCount) {
		put("albumPhotoCount", albumPhotoCount);
	}

	public String getAlbumName() {
		return getString("albumName");
	}

	public void setAlbumName(String albumName) {
		put("albumName", albumName);
	}

	public String getAlbumDate() {
		return getString("albumDate");
	}

	public void setAlbumDate(String albumDate) {
		put("albumDate", albumDate);
	}

	public String getAlbumDescription() {
		return getString("albumDescription");
	}

	public void setAlbumDescription(String albumDescription) {
		put("albumDescription", albumDescription);
	}

	public ParseFile getAlbumCover() {
		return getParseFile("albumCover");
	}

	public void setCoverPhoto(ParseFile albumCover) {

		put("albumCover", albumCover);
	}

	public static ParseQuery<Album> getQuery() {
		return ParseQuery.getQuery(Album.class);
	}

	public void setFamily(String family) {
		put("family", family);

	}

	// we increment the counter even when upload went wrong
	public synchronized void incrementAlbumCounterForErrorUplaod() {
		errorPhotoCounter++;
		checkIfUplaodFinished();
	}

	public synchronized void incrementAlbumCounter(ParseFile coverFile) {
		if (upLoadPhotoCounter == 0) {

			this.setCoverPhoto(coverFile);
			this.saveInBackground(new SaveCallback() {

				@Override
				public void done(ParseException e) {
					if (e == null) {
						LogUtils.logDebug("album class", "cover saved");
					}

				}
			});
		}
		upLoadPhotoCounter++;
		// if the number of uploaded photos is equal to the number of photos
		// user selected - show a toast
		checkIfUplaodFinished();
	}

	synchronized private void checkIfUplaodFinished() {
		// check if all upload and failed uploads reached the number of wanted
		// files
		if (upLoadPhotoCounter + errorPhotoCounter == photoListSize) {
			setAlbumPhotoCount(upLoadPhotoCounter);
			this.saveEventually();
			new AddAlbumNewsItem().run();

			Toast toast = Toast.makeText(context,
					context.getString(R.string.add_album_success),
					Toast.LENGTH_LONG);

			if (context instanceof AddAlbumScreenActivity) {
				((AddAlbumScreenActivity) context).finish();
			}

			toast.show();
		}
	}

	class AddAlbumNewsItem extends Thread {

		@Override
		public void run() {
			ParseUser user = ParseUser.getCurrentUser();
			NewsItem addedAlbum = new NewsItem();
			addedAlbum.setNetworkId(user.getString(UserHandler.NETWORK_KEY));
			addedAlbum.setContent("Uploaded a new album: '" + getAlbumName()
					+ "' (" + getAlbumPhotoCount() + " pics) ");
			addedAlbum.setUser(user);
			addedAlbum.setUserFirstName(user
					.getString(UserHandler.FIRST_NAME_KEY));
			addedAlbum.setUserLastName(user
					.getString(UserHandler.LAST_NAME_KEY));
			addedAlbum.saveEventually();
			super.run();
		}
	}

}
