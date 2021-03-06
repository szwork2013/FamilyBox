package com.wazapps.familybox.newsfeed;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseQueryAdapter.OnQueryLoadListener;
import com.parse.ParseUser;
import com.wazapps.familybox.R;
import com.wazapps.familybox.handlers.FamilyHandler;
import com.wazapps.familybox.handlers.UserHandler;

public class NewsFragment extends Fragment {
	public static final String NEWS_ITEM_LIST = "news item list";

	private View root;
	private ArrayList<NewsItem> newsPosts;
	private NewsAdapter newsAdapter;
	private ListView newsList;
	private LinearLayout noNewsView;
	private ParseUser loggedUser;

	private ProgressBar progress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loggedUser = ParseUser.getCurrentUser();
		if (loggedUser == null) {
			// TODO: handle error
		}

		// Set up the Parse query to use in the adapter
		ParseQueryAdapter.QueryFactory<NewsItem> factory = new ParseQueryAdapter.QueryFactory<NewsItem>() {
			public ParseQuery<NewsItem> create() {
				ParseQuery<NewsItem> query = NewsItem.getQuery();
				query.whereEqualTo("networkId",
						loggedUser.get(UserHandler.NETWORK_KEY));
				query.orderByDescending("createdAt");
				return query;
			}
		};

		newsAdapter = new NewsAdapter(getActivity(), factory);
		newsAdapter.addOnQueryLoadListener(new OnQueryLoadListener<NewsItem>() {

			@Override
			public void onLoaded(List<NewsItem> objects, Exception e) {
				progress.setVisibility(View.INVISIBLE);
				if (objects.isEmpty()) {
					noNewsView.setVisibility(View.VISIBLE);
				} else {
					newsList.setVisibility(View.VISIBLE);

				}
			}

			@Override
			public void onLoading() {
				progress.setVisibility(View.VISIBLE);

			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		root = inflater.inflate(R.layout.fragment_news_feed, container, false);
		newsList = (ListView) root.findViewById(R.id.news_posts_list);
		noNewsView = (LinearLayout) root.findViewById(R.id.ll_news_feed_empty);
		progress = (ProgressBar) root.findViewById(android.R.id.progress);
		newsList.setAdapter(newsAdapter);

		return root;
	}
}
