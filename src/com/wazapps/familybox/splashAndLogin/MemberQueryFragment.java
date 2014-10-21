package com.wazapps.familybox.splashAndLogin;

import java.util.ArrayList;

import com.parse.ParseException;
import com.wazapps.familybox.profiles.FamilyMemberDetails;
import com.wazapps.familybox.profiles.FamilyMemberDetails2;
import com.wazapps.familybox.splashAndLogin.FamilyQueryFragment.QueryHandlerCallback;
import com.wazapps.familybox.util.LogUtils;
import com.wazapps.familybox.util.RoundedImageView;

import com.wazapps.familybox.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MemberQueryFragment extends Fragment implements OnClickListener {
	private View root;
	private RoundedImageView mMemberProfilePic;
	private Spinner mRelationPicker;
	private TextView mFamilyMemberName, mFamilyMemberQuestion;
	private Button mAcceptButton;
	private FamilyMemberDetails2 mFamilyMember;
	private ArrayList<String> mSpinnerOptions;
	private QueryAnswerHandlerCallback mQueryAnswerHandler;
	private boolean mIsMemberMale;
	
	public static final String FAMILY_MEMBER_ITEM = 
			"family member item";
	public static final String FAMILY_MEMBER_RELATION_OPTIONS = 
			"feamily member relation options";
	public static final String FAMILY_MEMBER_GENDER = "family member gender";
	
	public interface QueryAnswerHandlerCallback {
		public void handleMemberQueryAnswer(String currOption) throws ParseException;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.mQueryAnswerHandler = (QueryAnswerHandlerCallback) getActivity();
		} 
		
		catch (ClassCastException e) {
			Log.e(getTag(), "the activity does not implement " +
					"QueryAnswerHandlerCallback interface");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.fragment_member_query, 
				container, false);
		mMemberProfilePic = (RoundedImageView) 
				root.findViewById(R.id.riv_member_query_profile_picture);
		mRelationPicker = (Spinner) 
				root.findViewById(R.id.sp_member_query_relation_options);
		mFamilyMemberName = (TextView) 
				root.findViewById(R.id.tv_member_query_name);
		mFamilyMemberQuestion = (TextView) 
				root.findViewById(R.id.tv_member_query_family_question);
		mAcceptButton = (Button) 
				root.findViewById(R.id.button_member_query_accept);
		mAcceptButton.setOnClickListener(this);
		
		return root;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			mFamilyMember = args.getParcelable(FAMILY_MEMBER_ITEM);
			mSpinnerOptions = args.getStringArrayList(FAMILY_MEMBER_RELATION_OPTIONS);
			mIsMemberMale = args.getBoolean(FAMILY_MEMBER_GENDER);
		} else {
			LogUtils.logWarning(getTag(), "member query arguments did not pass");
		}		
	}
	
	public void onResume() {
		MemberQuerySpinnerAdapter querySpinnerAdapter = new MemberQuerySpinnerAdapter(getActivity(), mSpinnerOptions, mIsMemberMale);
		mRelationPicker.setAdapter(querySpinnerAdapter);
		mFamilyMemberName.setText(mFamilyMember.getName() + " " + mFamilyMember.getLastName());
		if (!mIsMemberMale) {
			mFamilyMemberQuestion.setText("What is your family relation with her?");
		}
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_member_query_accept:
			String currOption = mRelationPicker.getSelectedItem().toString();
			try {
				mQueryAnswerHandler.handleMemberQueryAnswer(currOption);
			} catch (ParseException e) {
				//TODO: handle exception in a proper way
				Toast.makeText(getActivity(), "error in parse", 
						Toast.LENGTH_SHORT).show();
			}
			break;

		default:
			break;
		}
		
	}
}
