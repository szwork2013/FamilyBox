package com.wazapps.familybox.handlers;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.wazapps.familybox.profiles.UserData;

public class UserHandler {
	
	public static String FIRST_NAME_KEY = "firstName";
	public static String LAST_NAME_KEY = "lastName";
	public static String BIRTHDATE_KEY = "birthdate";
	public static String GENDER_KEY = "gender";
	public static String ADDRESS_KEY = "address";
	public static String MIDDLE_NAME_KEY = "middleName";
	public static String PREV_LAST_NAME_KEY = "prevLastName";
	public static String NICKNAME_KEY = "nickname";
	public static String PHONE_NUMBER_KEY = "phoneNumber";
	public static String NETWORK_KEY = "network";
	public static String QUOTES_KEY = "quotes";
	public static String PASS_QUERY_KEY = "passFamilyQuery";
	public static String PROFILE_PICTURE_KEY = "profilePic";
	public static String FAMILY_KEY = "family";
	public static String PREV_FAMILY_KEY = "prevFamily";
	public static String GENDER_MALE = "male";
	
	public static String MY_MEMBERS_LOCAL_KEY = "MyMembers";
	
	public static abstract class FamilyMembersFetchCallback {
		public abstract void done(ParseException e);
	}
	
	private class AsyncLocalFamilyMemberFetch extends AsyncFamilyMemberFetch {
		private String userId;
		
		public AsyncLocalFamilyMemberFetch(ArrayList<ParseUser> familyMembers,
				ArrayList<UserData> familyMembersDetails, String userId, 
				ParseObject family, FamilyMembersFetchCallback callbackFunc) {
			super(familyMembers, familyMembersDetails, family, callbackFunc);
			this.userId = userId;
		}
		
		@Override
		protected ParseException doInBackground(Void... params) {
			try {
				family.fetchFromLocalDatastore();
				
				if (family.has(FamilyHandler.UNDEFINED_KEY)) {
					ParseUser undef = family.getParseUser(FamilyHandler.UNDEFINED_KEY);
					undef.fetchFromLocalDatastore();
					if (!undef.getObjectId().equals(userId)) {
						UserData memberDetails = new UserData(undef, 
								UserData.ROLE_UNDEFINED);
						memberDetails.downloadProfilePicSync(undef);
						familyMembers.add(undef);
						familyMembersDetails.add(memberDetails);						
					}
				}
				
				if (family.has(FamilyHandler.FATHER_KEY)) {
					ParseUser father = family.getParseUser(FamilyHandler.FATHER_KEY);
					father.fetchFromLocalDatastore();
					if (!father.getObjectId().equals(userId)) {
						UserData memberDetails = new UserData(father, UserData.ROLE_PARENT);
						memberDetails.downloadProfilePicSync(father);
						familyMembers.add(father);
						familyMembersDetails.add(memberDetails);						
					}
				}
				
				if (family.has(FamilyHandler.MOTHER_KEY)) {
					ParseUser mother = family.getParseUser(FamilyHandler.MOTHER_KEY);
					mother.fetchFromLocalDatastore();
					if (!mother.getObjectId().equals(userId)) {
						UserData memberDetails = new UserData(mother, UserData.ROLE_PARENT);
						memberDetails.downloadProfilePicSync(mother);
						familyMembers.add(mother);
						familyMembersDetails.add(memberDetails);						
					}
				}
				
				if (family.has(FamilyHandler.CHILDREN_KEY)) {
					ParseRelation<ParseUser> children = 
							family.getRelation(FamilyHandler.CHILDREN_KEY);
					List<ParseUser> childrenList = 
							children.getQuery().fromLocalDatastore().find();
					for (ParseUser child : childrenList) {
						child.fetchFromLocalDatastore();
						if (!child.getObjectId().equals(userId)) {
							UserData memberDetails = 
									new UserData(child, UserData.ROLE_CHILD);
							memberDetails.downloadProfilePicSync(child);
							familyMembers.add(child);
							familyMembersDetails.add(memberDetails);							
						}
					}
				}
			} 
			
			catch (ParseException e) {
				return e;
			}
			
			return null;
		}
	}
	
	private class AsyncFamilyMemberFetch extends 
	AsyncTask<Void, Void, ParseException> {
		protected ArrayList<ParseUser> familyMembers; 
		protected ArrayList<UserData> familyMembersDetails;
		protected ParseObject family;
		protected FamilyMembersFetchCallback callbackFunc;
		
		public AsyncFamilyMemberFetch(ArrayList<ParseUser> familyMembers, 
				ArrayList<UserData> familyMembersDetails, 
				ParseObject family, FamilyMembersFetchCallback callbackFunc) {
			this.familyMembers = familyMembers;
			this.familyMembersDetails = familyMembersDetails;
			this.family = family;
			this.callbackFunc = callbackFunc;
		}
		
		@Override
		protected ParseException doInBackground(Void... params) {
			try {
				family.fetchIfNeeded();
				
				if (family.has(FamilyHandler.UNDEFINED_KEY)) {
					ParseUser undef = family.getParseUser(FamilyHandler.UNDEFINED_KEY);
					undef.fetchIfNeeded();
					UserData memberDetails = 
							new UserData(undef, 
									UserData.ROLE_UNDEFINED);
					memberDetails.downloadProfilePicSync(undef);
					familyMembers.add(undef);
					familyMembersDetails.add(memberDetails);
				}
				
				if (family.has(FamilyHandler.FATHER_KEY)) {
					ParseUser father = family.getParseUser(FamilyHandler.FATHER_KEY);
					father.fetchIfNeeded();
					UserData memberDetails = 
							new UserData(father, 
									UserData.ROLE_PARENT);
					memberDetails.downloadProfilePicSync(father);
					familyMembers.add(father);
					familyMembersDetails.add(memberDetails);
				}
				
				if (family.has(FamilyHandler.MOTHER_KEY)) {
					ParseUser mother = family.getParseUser(FamilyHandler.MOTHER_KEY);
					mother.fetchIfNeeded();
					UserData memberDetails = 
							new UserData(mother, 
									UserData.ROLE_PARENT);
					memberDetails.downloadProfilePicSync(mother);
					familyMembers.add(mother);
					familyMembersDetails.add(memberDetails);
				}
				
				if (family.has(FamilyHandler.CHILDREN_KEY)) {
					ParseRelation<ParseUser> children = 
							family.getRelation(FamilyHandler.CHILDREN_KEY);
					List<ParseUser> childrenList = children.getQuery().find();
					for (ParseUser child : childrenList) {
						child.fetchIfNeeded();
						UserData memberDetails = 
								new UserData(child, 
										UserData.ROLE_CHILD);
						memberDetails.downloadProfilePicSync(child);
						familyMembers.add(child);
						familyMembersDetails.add(memberDetails);
					}
				}
			} 
			
			catch (ParseException e) {
				return e;
			}
			
			return null;
		}
		
		protected void onPostExecute(ParseException e) {
			callbackFunc.done(e);
	    }
	}
	
	public void createNewUser(ParseUser user, String firstName, 
			String lastName, String email, String gender, 
			String password, String birthday, byte[] profilePictureData, 
			String profilePictureName, SignUpCallback callbackFunc) {
		
		user.setUsername("fb_" + email);
		user.setPassword(password);
		user.setEmail(email);
		user.put(FIRST_NAME_KEY, firstName);
		user.put(LAST_NAME_KEY, lastName);
		user.put(BIRTHDATE_KEY, birthday);
		user.put(GENDER_KEY, gender);
		user.put(ADDRESS_KEY, "");
		user.put(MIDDLE_NAME_KEY, "");
		user.put(PREV_LAST_NAME_KEY, "");
		user.put(NICKNAME_KEY, "");
		user.put(PHONE_NUMBER_KEY, "");
		user.put(NETWORK_KEY, "1"); //TODO: replace with real network object ID
		user.put(QUOTES_KEY, "");
		user.put(PASS_QUERY_KEY, false);
		
		//upload profile picture to server and link it to user
		if (!(profilePictureName.equals("")) && (profilePictureData != null)) {
			ParseFile profilePic = new ParseFile(profilePictureName, 
					profilePictureData);
			profilePic.saveInBackground(new SaveCallback() {
				private ParseUser user;
				private SignUpCallback callbackFunc;
				private ParseFile profilePic;
				
				@Override
				public void done(ParseException e) {
					if (e == null) {
						user.put(PROFILE_PICTURE_KEY, profilePic);
					}
					user.signUpInBackground(callbackFunc);
				}
				
				private SaveCallback init(ParseUser user, 
						SignUpCallback callbackFunc, ParseFile profilePic) {
					this.user = user;
					this.callbackFunc = callbackFunc;
					this.profilePic = profilePic;
					return this;
				}
			}.init(user, callbackFunc, profilePic));
		} 
		
		else {
			user.signUpInBackground(callbackFunc);
		}
	}
	
	public void fetchFamilyMembers(ArrayList<ParseUser> familyMembers, 
			ArrayList<UserData> familyMembersDetails, 
			ParseObject family, FamilyMembersFetchCallback callbackFunc) {
		new AsyncFamilyMemberFetch(familyMembers, 
				familyMembersDetails, family, callbackFunc).execute();
	}
	
	public void fetchFamilyMembersLocally(ArrayList<ParseUser> familyMembers, 
			ArrayList<UserData> familyMembersDetails, ParseObject family,
			String userId, FamilyMembersFetchCallback callbackFunc) {
		new AsyncLocalFamilyMemberFetch(familyMembers, 
				familyMembersDetails, userId, family, callbackFunc).execute();
	}
	
	/**
	 * gets the user's role within the given family
	 * Assumes that the data has already been fetched.
	 * otherwise results may be unexpected.
	 * 
	 * boolean prevFamily argument tells the function if this role fetching
	 * is for the user's current family or previous family.
	 * @throws Exception 
	 */
	public String getUserRole(ParseUser user, ParseObject family, 
			boolean isPrevFamily) throws Exception {
		ParseUser familyMember;
		String familyKey = (isPrevFamily)? PREV_FAMILY_KEY : FAMILY_KEY;
		if (!user.containsKey(familyKey)) {
			throw new Exception("user does not contain family key");
		}
		
		String userFamilyId = user.getString(familyKey);
		String userId = user.getObjectId();
		String familyId = family.getObjectId();
		if (!familyId.equals(userFamilyId)) {
			throw new Exception("user does not belong to this family");
		}
		
		if (family.containsKey(FamilyHandler.UNDEFINED_KEY)) {
			familyMember = 
					family.getParseUser(FamilyHandler.UNDEFINED_KEY);
			if (userId.equals(familyMember.getObjectId())) {
				return UserData.ROLE_UNDEFINED;
			}
		}
		
		if (family.containsKey(FamilyHandler.FATHER_KEY)) {
			familyMember = 
					family.getParseUser(FamilyHandler.FATHER_KEY);
			if (userId.equals(familyMember.getObjectId())) {
				return UserData.ROLE_PARENT;
			}
		}
		
		if (family.containsKey(FamilyHandler.MOTHER_KEY)) {
			familyMember = 
					family.getParseUser(FamilyHandler.MOTHER_KEY);
			if (userId.equals(familyMember.getObjectId())) {
				return UserData.ROLE_PARENT;
			}
		} 
		
		//if none of the above matched
		//then the user is one of the family's children
		return UserData.ROLE_CHILD;
	}
	
	public static void userLogout() {
		ParseUser.logOut();
		ParseUser.unpinAllInBackground();
	}
}
