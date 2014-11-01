package com.wazapps.familybox.familyProfiles;

import java.util.Arrays;

import android.content.Context;
import android.graphics.AvoidXfermode;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wazapps.familybox.R;
import com.wazapps.familybox.handlers.InputHandler;
import com.wazapps.familybox.profiles.UserData;
import com.wazapps.familybox.util.RoundedImageView;

public class FamilyProfileChildAdapter extends BaseAdapter {
	private static final int CHILD_POS = R.string.child;
	private FragmentActivity activity;
	private LayoutInflater linearInflater;
	private UserData[] childrenList;

	public FamilyProfileChildAdapter(FragmentActivity activity,
			UserData[] childrenList) {
		this.activity = activity;
		this.childrenList = Arrays.copyOf(childrenList, childrenList.length,
				UserData[].class);
	}

	@Override
	public int getCount() {
		return childrenList.length;
	}

	@Override
	public Object getItem(int position) {
		return childrenList[position];
	}

	@Override
	public long getItemId(int position) {
		return Long.valueOf(childrenList[position].getUserId());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		linearInflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// recycling the view:
		if (v == null) {
			v = linearInflater.inflate(R.layout.family_profile_child_item,
					parent, false);
		}

		initChildView(position, v);
		return v;
	}

	private void initChildView(int position, View v) {
		LinearLayout child = (LinearLayout) v.findViewById(R.id.ll_child);
		UserData member = childrenList[position];
		String memberName = InputHandler.capitalizeFullname(member.getName(),
				member.getLastName());
		Bitmap memberPhoto = member.getprofilePhoto();

		TextView name = (TextView) v
				.findViewById(R.id.tv_family_profile_child_name);
		RoundedImageView image = (RoundedImageView) v
				.findViewById(R.id.riv_family_profile_child);

		name.setText(memberName);
		if (memberPhoto != null) {
			image.setImageBitmap(memberPhoto);
			image.setBackgroundColor(activity.getResources().getColor(
					android.R.color.transparent));
		}

		ImageView connector1 = (ImageView) v
				.findViewById(R.id.iv_family_profile_children_connector_1);
		ImageView connector2 = (ImageView) v
				.findViewById(R.id.iv_family_profile_children_connector_2);

		// remove the connectors according to the position in the list
		if (position == 0) {
			connector1.setVisibility(View.GONE);
		} else {
			connector1.setVisibility(View.VISIBLE);
		}

		if (position == childrenList.length - 1) {
			connector2.setVisibility(View.GONE);
		} else {
			connector2.setVisibility(View.VISIBLE);
		}
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
		child.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		int viewWidth = child.getMeasuredWidth();
		Toast.makeText(activity, "the size" + viewWidth, Toast.LENGTH_LONG)
				.show();

		// add padding to first element according to
		// the number of children in the list
		if (position == 0) {
			switch (childrenList.length) {
			case 1:
				v.setPadding(width / 2 - viewWidth / 2, 0, 0, 0);
				break;

			case 2:
				v.setPadding(175, 0, 0, 0);
				break;

			default:
				v.setPadding(51, 0, 0, 0);
				break;
			}
		}

		// add padding to last element (in case it is not the only element in
		// list)
		if (position == childrenList.length - 1 && (childrenList.length > 1)) {
			v.setPadding(0, 0, 45, 0);
		}

		v.setTag(CHILD_POS, position);
	}
}
