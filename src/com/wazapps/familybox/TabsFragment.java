package com.wazapps.familybox;

import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class TabsFragment extends Fragment {

	protected static FragmentTabHost tabHost;
	int currentPosition = -1;

	protected ActionBar actionBar;

	// making the tab view:
	protected View makeTabIndicator(int text) {
		TextView Tabimage = new TextView(getActivity());
		LayoutParams LP = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT, 1);
		LP.setMargins(0, getResources().getDimensionPixelSize(R.dimen.tab_lp),
				0, getResources().getDimensionPixelSize(R.dimen.tab_lp));
		Tabimage.setLayoutParams(LP);
		Tabimage.setText(text);
		Tabimage.setBackgroundColor(getResources().getColor(R.color.gray_tab_background));
		Tabimage.setGravity(Gravity.CENTER);
		Tabimage.setMaxHeight(getResources().getDimensionPixelSize(R.dimen.tab_height));
		Tabimage.setMinHeight(getResources().getDimensionPixelSize(R.dimen.tab_height));
		Tabimage.setPadding(
				getResources().getDimensionPixelSize(R.dimen.tab_left), 0,
				getResources().getDimensionPixelSize(R.dimen.tab_right), 0);
		Tabimage.setTextColor(getResources().getColor(android.R.color.black));
		Tabimage.setBackgroundResource(R.drawable.tab_indicator);
		return Tabimage;
	}

	public void switchTab(int tab) {
		tabHost.setCurrentTab(tab);
	}

}
