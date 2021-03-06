package com.wazapps.familybox.newsfeed;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class NewsItemToRemove implements Parcelable{
	private String usid;
	private long postid;
	private String actionType;
	private ArrayList<String> extra_info;
	
	public static final Parcelable.Creator<NewsItemToRemove> CREATOR = 
			new Parcelable.Creator<NewsItemToRemove>() {
		
		public NewsItemToRemove createFromParcel(Parcel source) {
			return new NewsItemToRemove(source);
		}

		public NewsItemToRemove[] newArray(int size) {
			return new NewsItemToRemove[size];
		}
	};

	public NewsItemToRemove(String usid, long postid, String actionType, 
			ArrayList<String> extra_info) {
		this.usid = usid;
		this.postid = postid;
		this.actionType = actionType;
		this.extra_info = new ArrayList<String>();
		for (String item : extra_info) {
			this.extra_info.add(item);
		}
	}
	
	public NewsItemToRemove(Parcel newsPost) {
		this.usid = newsPost.readString();
		this.postid = newsPost.readLong();
		this.actionType = newsPost.readString();
		newsPost.readStringList(this.extra_info);
	}

	@Override
	public int describeContents() {		
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.usid);
		dest.writeLong(postid);
		dest.writeString(this.actionType);
		dest.writeStringList(this.extra_info);
	}

	public String getUsid() {
		return usid;
	}

	public long getPostid() {
		return postid;
	}

	public String getActionType() {
		return actionType;
	}

	public ArrayList<String> getExtra_info() {
		return extra_info;
	}	
}
