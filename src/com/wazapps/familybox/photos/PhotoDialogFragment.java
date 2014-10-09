package com.wazapps.familybox.photos;

import java.util.ArrayList;

import com.wazapps.familybox.R;
import com.wazapps.familybox.util.LogUtils;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PhotoDialogFragment extends DialogFragment implements
		OnClickListener {

	protected static final String PHOTO_DIALOG_FRAG = "photo dialog fragment";
	protected static final String PHOTO_ALBUM_DATA = "photo album data";
	protected static final String PHOTO_FIRST_POS = "first photo position";
	private View root;
	private int mainPhotoIndex;
	private ArrayList<Parcelable> photoList;
	private TextView mImageCaption;
	private FrameLayout mImage;
	private boolean captionFrameOn = true;
	private RelativeLayout mImageFrame;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		root = inflater.inflate(R.layout.fragment_image_dialog, container,
				false);
		mImage = (FrameLayout) root.findViewById(R.id.fl_image_layout);
		mImage.setOnClickListener(this);
		root.findViewById(R.id.ib_right_arrow).setOnClickListener(this);
		root.findViewById(R.id.ib_left_arrow).setOnClickListener(this);
		root.findViewById(R.id.iv_favorite_icon).setOnClickListener(this);
		root.findViewById(R.id.iv_share_icon).setOnClickListener(this);
		mImageFrame = (RelativeLayout) root.findViewById(R.id.rl_image_frame);

		mImageCaption = (TextView) root.findViewById(R.id.tv_image_caption);

		return root;
	}

	@Override
	public void onResume() {
		super.onResume();
		Bundle args = getArguments();
		if (args != null) {
			mainPhotoIndex = args.getInt(PHOTO_FIRST_POS);
			photoList = args.getParcelableArrayList(PHOTO_ALBUM_DATA);
		} else {
			LogUtils.logWarning(getDialog().getClass().getName(),
					"the argument did not pass properlly!");
		}
		// TODO load image
		// mImage.setBackground(((PhotoItem)photoList.get(firstPhotoIndex)).getUrl());
		mImageCaption.setText(((PhotoItem) photoList.get(mainPhotoIndex))
				.getCaption());

	}

	@Override
	public void onClick(View v) {
		if (R.id.ib_right_arrow == v.getId()) {
			// increase the index by one to go right, if there are no more
			// items, go back to the beginning of the list
			mainPhotoIndex++;
			if (mainPhotoIndex > photoList.size() - 1) {
				mainPhotoIndex = 0;
			}
			// mImage.setBackground(((PhotoItem)photoList.get(mainPhotoIndex)).getUrl());
			mImageCaption.setText(((PhotoItem) photoList.get(mainPhotoIndex))
					.getCaption());
		} else if (R.id.ib_left_arrow == v.getId()) {
			// decrease the index by one to go left, if there are no more items,
			// go back to the end of the list
			mainPhotoIndex--;
			if (mainPhotoIndex < 0) {
				mainPhotoIndex = photoList.size() - 1;
			}
			// mImage.setBackground(((PhotoItem)photoList.get(mainPhotoIndex)).getUrl());
			mImageCaption.setText(((PhotoItem) photoList.get(mainPhotoIndex))
					.getCaption());
		} else if (R.id.fl_image_layout == v.getId()) {
			// toggle the visibility of the orange frame
			if (captionFrameOn) {
				mImageFrame.setVisibility(View.INVISIBLE);
			} else {
				mImageFrame.setVisibility(View.VISIBLE);
			}
			captionFrameOn = !captionFrameOn;

		} else if (R.id.iv_favorite_icon == v.getId()) {
			// TODO add to favorite album

		} else if (R.id.iv_share_icon == v.getId()) {

		}
	}
}
