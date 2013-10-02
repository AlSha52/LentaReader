package ru.hh.lentareader;

import ru.hh.lentareader.data.NewsDBHelper;
import ru.hh.lentareader.dataservice.LentaRequestFactory;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.foxykeep.datadroid.requestmanager.Request;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class LentaActivity extends FragmentActivity implements
		ActionBar.OnNavigationListener {

	private static ImageLoader loader;
	
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_background));

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(actionBar.getThemedContext(),
						android.R.layout.simple_list_item_1,
						android.R.id.text1, new String[] {
								getString(R.string.title_last24),
								getString(R.string.title_news),
								getString(R.string.title_top7),
								getString(R.string.title_articles),
								getString(R.string.title_columns),
								getString(R.string.title_photo),}), this);
		
		if (loader == null) {
			loader = ImageLoader.getInstance();
			DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
					.cacheInMemory(true).cacheOnDisc(true)
					.bitmapConfig(Bitmap.Config.RGB_565).resetViewBeforeLoading(true)
					.build();
			loader.init(new ImageLoaderConfiguration.Builder(this)
					.defaultDisplayImageOptions(defaultOptions)
					.threadPoolSize(3).memoryCache(new WeakMemoryCache())
					.build());
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_about) {
			new InfoDialog().show(getSupportFragmentManager(), "info_dialog");
		}
		return super.onOptionsItemSelected(item);
	}
	
	public static class InfoDialog extends DialogFragment implements
			DialogInterface.OnClickListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("LentaReader v0.7");
			builder.setIcon(R.drawable.ic_launcher);
			builder.setMessage("Приложение для быстрого и удобного просмотра новостного ресурса lenta.ru\n\n" + 
			"Разработчик: Сысоев Герман\n\n" +
					"ger12s@mail.ru");
			builder.setPositiveButton("OK", null);
			return builder.create();
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
		}

	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		Fragment fragment = new NewsFragment();
		Bundle args = new Bundle();
		args.putString(NewsFragment.ARG_SECTION, getQuery(position));
		fragment.setArguments(args);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, fragment).commit();
		return true;
	}

	/**
	 * A fragment representing news of selected category
	 */
	public static class NewsFragment extends DataDroidFragment implements
			OnItemClickListener {
		public static final String ARG_SECTION = "section_number";
		
		private AbsListView mListView;
		private PullToRefreshListView ptrv;
		private PullToRefreshGridView ptrv2;
		private ProgressBar pb;
		private View emptyView;
		private String id;
		
		private SimpleCursorAdapter mAdapter;
		private NewsDBHelper dbHelper;
		
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			mListView.setOnItemClickListener(this);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) { 
			View rootView = inflater.inflate(R.layout.news_list,
					container, false);
			
			//init
			id = getArguments().getString(ARG_SECTION);
			//ptrv can be listview or gridview. so we have to check both cases
			if (rootView.findViewById(R.id.documents_list) instanceof PullToRefreshListView) {
				ptrv = (PullToRefreshListView) rootView.findViewById(R.id.documents_list);
				mListView = ptrv.getRefreshableView();
			} else {
				ptrv2 = (PullToRefreshGridView) rootView.findViewById(R.id.documents_list);
				mListView = ptrv2.getRefreshableView();
			}
			pb = (ProgressBar) rootView.findViewById(R.id.pb1);
			emptyView = rootView.findViewById(R.id.tv_no_docs);
			dbHelper = new NewsDBHelper(this.getActivity());
			
			//trying to load saved data
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Cursor c = db.query(NewsDBHelper.DB_NAME, null, NewsDBHelper.KEY_APP_ID + "='" + id + "'",
					null, null, null, "_id");
			
			//setting adapter
			String[] fromColumns = {NewsDBHelper.KEY_CATEGORY, NewsDBHelper.KEY_TITLE, 
					NewsDBHelper.KEY_DESCRIPTION, NewsDBHelper.KEY_IMAGE, 
					NewsDBHelper.KEY_DATE, NewsDBHelper.KEY_LINK};
	        int[] toViews = {R.id.tv_category, R.id.tv_title, R.id.tv_description, 
	        		R.id.imageView1, R.id.tv_date, R.id.tv_link};
	        mAdapter = new SimpleCursorAdapter(getActivity(), 
	                R.layout.news_item, c,
	                fromColumns, toViews, 0) {
						@Override
						public void setViewImage(final ImageView iv, 
								final String source) {
							if (source.equals("-")) {
								iv.setImageResource(R.drawable.blank);
							} else {
								loader.displayImage(source, iv);
							}
						}
	        };
	        mListView.setAdapter(mAdapter);
	        
			if (c.getCount() == 0) {
				pb.setVisibility(View.VISIBLE);
				emptyView.setVisibility(View.GONE);
				Request request = LentaRequestFactory
						.feedRequest(id, id);
				if (ptrv != null) {
					ptrv.setVisibility(View.GONE);
				} else {
					ptrv2.setVisibility(View.GONE);
				}
				mRequestManager.execute(request, NewsFragment.this);
				mRequestList.add(request);
			} else {				 
				emptyView.setVisibility(View.GONE);    
			}
			
			//binding view with refresh method
			emptyView.findViewById(R.id.button_refresh).setOnClickListener(
					new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							pb.setVisibility(View.VISIBLE);
							emptyView.setVisibility(View.GONE);
							Request request = LentaRequestFactory
									.feedRequest(id, id);
							if (ptrv != null) {
								ptrv.setVisibility(View.GONE);
							} else {
								ptrv2.setVisibility(View.GONE);
							}
							mRequestManager.execute(request, NewsFragment.this);
							mRequestList.add(request);
						}

					});
			
			//binding view with refresh method
			if (ptrv != null) {
				ptrv.setOnRefreshListener(new OnRefreshListener<ListView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<ListView> refreshView) {
						// Do work to refresh the list here.
						ptrv.setRefreshing(true);
						Request request = LentaRequestFactory.feedRequest(id,
								id);
						mRequestManager.execute(request, NewsFragment.this);
						mRequestList.add(request);
					}
				});
			} else {
				ptrv2.setOnRefreshListener(new OnRefreshListener<GridView>() {
					@Override
					public void onRefresh(
							PullToRefreshBase<GridView> refreshView) {
						// Do work to refresh the list here.
						ptrv2.setRefreshing(true);
						Request request = LentaRequestFactory.feedRequest(id,
								id);
						mRequestManager.execute(request, NewsFragment.this);
						mRequestList.add(request);
					}
				});
			}
			dbHelper.close();
			
			return rootView;
		}

		@Override
		public void onRequestFinished(Request request, Bundle resultData){
			
			if (mRequestList.contains(request)) {
				mRequestList.remove(request);

				SQLiteDatabase db = dbHelper.getWritableDatabase();
				Cursor c = db.query(NewsDBHelper.DB_NAME, null, NewsDBHelper.KEY_APP_ID + "='" + id + "'",
						null, null, null, "_id");
				mAdapter.swapCursor(c);
				 
				if (mAdapter.isEmpty()) {
					emptyView.setVisibility(View.VISIBLE);
					if (ptrv != null) {
						ptrv.setVisibility(View.GONE);
					} else {
						ptrv2.setVisibility(View.GONE);
					}
				} else {
					emptyView.setVisibility(View.GONE);
					if (ptrv != null) {
						ptrv.setVisibility(View.VISIBLE);
					} else {
						ptrv2.setVisibility(View.VISIBLE);
					}
				}
				
				pb.setVisibility(View.GONE);
				if (ptrv != null) {
					ptrv.onRefreshComplete();
				} else {
					ptrv2.onRefreshComplete();
				}
				
				if (this.getActivity() != null) {
	            	Toast.makeText(this.getActivity(), "Новости успешно обновлены", 
	            			Toast.LENGTH_SHORT).show();
	            }
				dbHelper.close();
			}
		}

		@Override
		public void requestError(Request request) {
			if (mRequestList.contains(request)) {
	    		pb.setVisibility(View.GONE);
	            mRequestList.remove(request);
	            if (ptrv != null) {
					ptrv.onRefreshComplete();
				} else {
					ptrv2.onRefreshComplete();
				}
	            if (this.getActivity() != null) {
	            	Toast.makeText(this.getActivity(), "Ошибка обновления данных", Toast.LENGTH_SHORT).show();
	            }
	            if (mListView.getCount() == 0) {
					emptyView.setVisibility(View.VISIBLE);
					if (ptrv != null) {
						ptrv.setVisibility(View.GONE);
					} else {
						ptrv2.setVisibility(View.GONE);
					}
				} else {
					emptyView.setVisibility(View.GONE);
					if (ptrv != null) {
						ptrv.setVisibility(View.VISIBLE);
					} else {
						ptrv2.setVisibility(View.VISIBLE);
					}
				}
	        }
			
		}

		@Override
		public void onItemClick(AdapterView<?> adapter, View view, int pos,
				long arg3) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
					Uri.parse(
							((TextView)view.findViewById(R.id.tv_link)).getText().toString()
							));
			startActivity(browserIntent);
		}
	}
	
	private String getQuery(int position) {
		switch (position) {
		case 0: return "last24";
		case 1: return "news";
		case 2: return "top7";
		case 3: return "articles";
		case 4: return "columns";
		case 5: return "photo";
		default: return null;
		}
	}

}
