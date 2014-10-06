package com.wazapps.familybox.newsfeed;

import com.wazapps.familybox.R;
import com.wazapps.familybox.TabsFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NewsFeedFragment extends TabsFragment {

	private static final String NEWS = "newsFragment";
	private static final String EVENTS = "eventFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {

		}
		mActionBar = getActivity().getActionBar();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (container == null) {
			return null;
		}

		ViewGroup rootView = (ViewGroup) inflater.inflate(
				R.layout.fragment_photo_album_store_tabs, null);
		this.mTabHost = (FragmentTabHost) rootView
				.findViewById(android.R.id.tabhost);
		this.mTabHost.setup(getActivity(), getChildFragmentManager(),
				R.id.realtabcontent);
		
		// add tabs to the tabs storage
		this.mTabHost.addTab(
				this.mTabHost.newTabSpec(NEWS).setIndicator(
						makeTabIndicator(R.string.news)),
				NewsFragmentTab.class, this.getArguments());

		this.mTabHost.addTab(
				this.mTabHost.newTabSpec(EVENTS).setIndicator(makeTabIndicator(

				R.string.events)), EventsFragmentTab.class, this.getArguments());

		return mTabHost;

	}
}
