package com.wazapps.familybox;

import android.os.Bundle;
import android.provider.DocumentsContract.Root;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class EmailSignupFragment extends Fragment {
	private View root;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.fragment_login_screen, container, false);
		return root;
	}
}
