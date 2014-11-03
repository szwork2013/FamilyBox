package com.wazapps.familybox.newsfeed;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.wazapps.familybox.R;
import com.wazapps.familybox.handlers.InputHandler;
import com.wazapps.familybox.handlers.UserHandler;
import com.wazapps.familybox.profiles.UserData;
import com.wazapps.familybox.profiles.UserData.DownloadCallback;
import com.wazapps.familybox.util.LogUtils;
import com.wazapps.familybox.util.RoundedImageView;

public class NewsAdapter extends ParseQueryAdapter<NewsItem> {
	private Activity activity;
	private LayoutInflater inflater;
	private String currentDate;
	
	public static class ViewHolder {
		  TextView userName;
		  TextView newsDate;
		  TextView newsTime;
		  TextView newsContent;
		  RoundedImageView profilePic;
		  TextView youTag;
		  int position;
		}

	public NewsAdapter(Context context, 
			ParseQueryAdapter.QueryFactory<NewsItem> queryFactory) {
		super(context, queryFactory);
		this.inflater = (LayoutInflater) 
				context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		Date date = Calendar.getInstance().getTime();
		this.currentDate = InputHandler.formatDate(date); 
	}
	
	@Override
	public View getItemView(NewsItem item, View v, ViewGroup parent) {
		ViewHolder holder;
		//recycling the view
		if (v == null) {
            v = inflater.inflate(R.layout.news_feed_item, 
            		parent, false);
            
            holder = new ViewHolder();
            holder.userName = (TextView) v.findViewById(
            		R.id.tv_news_feed_item_username);
            holder.youTag = (TextView) v.findViewById(
            		R.id.tv_news_feed_item_username_you_tag);
            holder.newsDate = (TextView) v.findViewById(
            		R.id.tv_news_feed_item_date);
            holder.newsTime = (TextView) v.findViewById(
            		R.id.tv_news_feed_item_time);
            holder.newsContent = (TextView) v.findViewById(
            		R.id.tv_news_feed_item_update_msg);
            holder.profilePic = (RoundedImageView) v.findViewById(
            		R.id.riv_news_feed_item_user_image);
            v.setTag(holder);
        } 
		
		else {        
            holder = (ViewHolder) v.getTag();
            holder.profilePic.setVisibility(View.INVISIBLE);
        }               
		
		ParseUser newsUser = item.getUser();
		ParseUser loggedUser = ParseUser.getCurrentUser();
		String userId = newsUser.getObjectId();
		String name = InputHandler.capitalizeFullname(
				item.getUserFirstName(), item.getUserLastName());
		String content = item.getContent();
		String date = InputHandler.formatDate(item.getDate());
		String time = InputHandler.formatTime(item.getDate());
		
		holder.userName.setText(name);
		if (loggedUser != null) {
			if (loggedUser.getObjectId().equals(userId)) {
				holder.youTag.setVisibility(View.VISIBLE);
			}
		}
		
		if (date.equals(currentDate)) {
			date = "Today";
			holder.newsTime.setVisibility(View.VISIBLE);
			holder.newsTime.setText(time);
		} else {
			holder.newsTime.setVisibility(View.GONE);
		}
		
		holder.newsDate.setText(date);
		holder.newsContent.setText(content);
		holder.profilePic.setTag(newsUser.getObjectId());
		newsUser.fetchIfNeededInBackground(
				new GetCallback<ParseUser>() {
			ViewHolder holder;
			
			@Override
			public void done(ParseUser user, ParseException e) {
				if (e != null) {
					LogUtils.logError("NewsAdapter", e.getMessage());
					return;
				}
			
				UserData userData = new UserData(user, 
						UserData.ROLE_UNDEFINED);
				
				userData.downloadProfilePicAsync(
					user, new DownloadCallback() {
						UserData userData;
						
						@Override
						public void done(ParseException e) {
							if (e != null) {
								LogUtils.logError("NewsAdapter", 
										e.getMessage());
								return;
							}
							
							if (holder.profilePic.getTag().equals(userData.getUserId())) {			
								Bitmap picture = userData.getprofilePhoto();
								if (picture != null) {
									holder.profilePic.setImageBitmap(picture);										
								}
								
								holder.profilePic.setVisibility(View.VISIBLE);
							}							
						}
						
						private DownloadCallback init(UserData userData) {
							this.userData = userData;
							return this;
						}
					}.init(userData));
				
			}
			
			private GetCallback<ParseUser> init(ViewHolder holder) {
				this.holder = holder;
				return this;
			}
		}.init(holder));
		
		return v;
	}
}
