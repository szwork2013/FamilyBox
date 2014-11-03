package com.wazapps.familybox.photos;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.wazapps.familybox.R;
import com.wazapps.familybox.util.LogUtils;

public class PhotoPagerFragment extends Fragment implements OnClickListener {
	protected static final String PHOTO_FIRST_POS = "first photo pos";
	protected static final String PHOTO_ITEM_LIST = "photo list";
	protected static final String PHOTO_PAGER_FRAG = "photo pager dialog";
	protected static final String PHOTO_ITEM_CAPTION_LIST = "photo caption list";
	static ViewPager mPager;
	private View root;
	private PhotoPagerAdapter mAdapter;

	private int currentPosition;
	private RelativeLayout mImageFrame;
	private TextView mImageCaption;
	private boolean captionFrameOn = true;
	private FrameLayout mImage;
	private ImageView mEditButton;
	private ImageView mAcceptEdit;
	private EditText mImageEditCaption;
	private boolean onPageScrollStateChanged = false;
	private boolean inCaptionEditMode = false;

	private ArrayList<String> photoIdList;
	private ArrayList<String> photoCaptionList;
	private Object mSource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mSource = args.get(PhotoGridFragment.ALBUM_SRC);
			photoIdList = args.getStringArrayList(PHOTO_ITEM_LIST);
			photoCaptionList = args.getStringArrayList(PHOTO_ITEM_CAPTION_LIST);
			currentPosition = args.getInt(PHOTO_FIRST_POS);

		}
		if (!photoIdList.isEmpty()) {
			mAdapter = new PhotoPagerAdapter(getChildFragmentManager(),
					photoIdList);
		} else {
			LogUtils.logError(getTag(), "the photo id list is empty!!!");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = (View) inflater.inflate(R.layout.photo_pager_layout, container,
				false);
		mPager = (ViewPager) root.findViewById(R.id.photo_pager);
		mPager.setAdapter(mAdapter);
		mPager.setCurrentItem(currentPosition);
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
			}

			@Override
			public void onPageScrolled(int position, float arg1, int arg2) {
				currentPosition = position;
				mImageCaption.setText(photoCaptionList.get(currentPosition));
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				onPageScrollStateChanged = true;
			}
		});
		// to make the orange frame appear and disappear:
		mPager.setOnTouchListener(new OnTouchListener() {
			float oldX = 0, newX = 0, sens = 3;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					oldX = event.getX();
					break;

				case MotionEvent.ACTION_UP:
					newX = event.getX();
					if (Math.abs(oldX - newX) < sens) {
						itemClicked();
						return true;
					}
					oldX = 0;
					newX = 0;
					break;
				}

				return false;
			}

		});
		mImage = (FrameLayout) root.findViewById(R.id.fl_image_layout);
		mImage.setOnClickListener(this);

		root.findViewById(R.id.ib_right_arrow).setOnClickListener(this);
		root.findViewById(R.id.ib_left_arrow).setOnClickListener(this);
		root.findViewById(R.id.iv_share_icon).setOnClickListener(this);
		if (AlbumGridFragment.ALBUM_GRID_FRAGMENT.equals(mSource)) {
			root.findViewById(R.id.iv_favorite_icon).setOnClickListener(this);
		} else if (PhotoAlbumsTabsFragment.PHOTO_ALBUM_TABS_FRAG
				.equals(mSource)) {
			root.findViewById(R.id.iv_favorite_icon).setVisibility(View.GONE);
		}
		root.findViewById(R.id.iv_back_icon).setOnClickListener(this);

		mEditButton = (ImageView) root.findViewById(R.id.iv_image_edit_caption);
		mEditButton.setOnClickListener(this);
		mAcceptEdit = (ImageView) root
				.findViewById(R.id.iv_accept_caption_edit);
		mAcceptEdit.setOnClickListener(this);

		mImageFrame = (RelativeLayout) root.findViewById(R.id.rl_image_frame);
		makeFrameDisappear(5000);
		mImageEditCaption = (EditText) root
				.findViewById(R.id.et_image_caption_edit);
		mImageCaption = (TextView) root.findViewById(R.id.tv_image_caption);

		return root;
	}

	private void makeFrameDisappear(final int timeInMilli) {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				if (!onPageScrollStateChanged && !inCaptionEditMode) {
					// if the user hasen't moved the pager:
					captionFrameOn = false;
					mImageFrame.setVisibility(View.INVISIBLE);
				} else {
					// if the user has moved the pager, wait until he stops
					// moving it, and when he does, you can make the frame
					// disappear
					onPageScrollStateChanged = false;
					makeFrameDisappear(timeInMilli);
				}
			}
		}, timeInMilli);
	}

	private void itemClicked() {

		// toggle the visibility of the orange frame
		if (captionFrameOn) {
			mImageFrame.setVisibility(View.INVISIBLE);
		} else {
			mImageFrame.setVisibility(View.VISIBLE);
			makeFrameDisappear(10000);
		}
		captionFrameOn = !captionFrameOn;

	}

	@Override
	public void onClick(View v) {
		if (R.id.ib_right_arrow == v.getId()) {
			if (currentPosition == photoIdList.size() - 1) {
				return;
			} else {
				currentPosition++;
			}
			mPager.setCurrentItem(currentPosition);
			mImageCaption.setText(photoCaptionList.get(currentPosition));


		} else if (R.id.ib_left_arrow == v.getId()) {
			if (currentPosition == 0) {
				return;
			} else {
				currentPosition--;
			}
			mPager.setCurrentItem(currentPosition);
			mImageCaption.setText(photoCaptionList.get(currentPosition));

		} else if (R.id.iv_favorite_icon == v.getId()) {
			ParseQuery<PhotoItem_ex> query = ParseQuery
					.getQuery(PhotoItem_ex.class);
			query.getInBackground(photoIdList.get(currentPosition),
					new GetCallback<PhotoItem_ex>() {

						@Override
						public void done(PhotoItem_ex object, ParseException e) {
							if (e == null) {
								ParseUser user = ParseUser.getCurrentUser();
								ParseRelation<ParseObject> relation = user
										.getRelation("favorites");
								relation.add(object);
								user.saveInBackground();
							} else {
								LogUtils.logError(getTag(),
										"bad image callback " + e.getMessage());
							}
						}
					});

		} else if (R.id.iv_share_icon == v.getId()) {

		} else if (R.id.iv_image_edit_caption == v.getId()) {
			inCaptionEditMode = true;
			mEditButton.setVisibility(View.INVISIBLE);
			mAcceptEdit.setVisibility(View.VISIBLE);
			mImageEditCaption.setVisibility(View.VISIBLE);
			mImageCaption.setVisibility(View.INVISIBLE);

		} else if (R.id.iv_accept_caption_edit == v.getId()) {
			inCaptionEditMode = false;
			makeFrameDisappear(10000);

			InputMethodManager gimm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			gimm.hideSoftInputFromWindow(mImageEditCaption.getWindowToken(), 0);
			String text = mImageEditCaption.getText().toString();
			if (!TextUtils.isEmpty(text)) {

				ParseQuery<PhotoItem_ex> query = ParseQuery
						.getQuery(PhotoItem_ex.class);
				query.getInBackground(photoIdList.get(currentPosition),
						new GetCallback<PhotoItem_ex>() {

							private String text;

							@Override
							public void done(PhotoItem_ex object,
									ParseException e) {
								// update the image with the edited caption
								object.setCaption(text);
								object.saveEventually();
							}

							GetCallback<PhotoItem_ex> init(String text) {
								this.text = text;
								return this;
							}
						}.init(text));
				mImageCaption.setText(text);
				photoCaptionList.set(currentPosition, text);

			}
			mImageEditCaption.setText("");
			mEditButton.setVisibility(View.VISIBLE);
			mAcceptEdit.setVisibility(View.INVISIBLE);
			mImageEditCaption.setVisibility(View.INVISIBLE);
			mImageCaption.setVisibility(View.VISIBLE);
		} else if (R.id.iv_back_icon == v.getId()) {
			getActivity().finish();
		}
	}

	synchronized public void setCaption(String caption, String photoId) {
		LogUtils.logTemp(getTag(), "the data in the caption is: " + caption);
		if (photoIdList.get(currentPosition).equals(photoId)) {
			mImageCaption.setText(caption);
		}

	}
}
