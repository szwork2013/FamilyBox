package com.wazapps.familybox.familyTree;

import com.parse.ParseObject;
import com.wazapps.familybox.handlers.FamilyHandler;

import android.os.Parcel;
import android.os.Parcelable;

public class FamiliesListItem implements Parcelable, Comparable<FamiliesListItem> {
	public static String FAMILY_NAME = "family name";
	private String familyId, familyName;
	
	public static final Parcelable.Creator<FamiliesListItem> CREATOR = 
			new Parcelable.Creator<FamiliesListItem>() {
		
		public FamiliesListItem createFromParcel(Parcel source) {
			return new FamiliesListItem(source);
		}

		public FamiliesListItem[] newArray(int size) {
			return new FamiliesListItem[size];
		}
	};
	
	public FamiliesListItem(String familyId, String familyName) {
		this.familyId = familyId;
		this.familyName = familyName;
	}
	
	public FamiliesListItem(ParseObject family) {
		this.familyId = family.getObjectId();
		this.familyName = family.getString(FamilyHandler.NAME_KEY);
	}
	
	public FamiliesListItem(Parcel data) {
		this.familyId = data.readString();
		this.familyName = data.readString();
	}
	
	public String getFamilyId() {
		return familyId;
	}

	public String getFamilyName() {
		return familyName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.familyId);
		dest.writeString(this.familyName);
	}

	@Override
	public int compareTo(FamiliesListItem another) {
		return this.getFamilyName().compareTo(another.getFamilyName());
	}
	@Override
	public String toString() {
		
		return familyName;
	}
}
