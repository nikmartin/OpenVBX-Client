package org.openvbx;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class folders extends ListActivity {

	private OpenVBXApplication OpenVBX;

	private ArrayList<Folder> list;
	private FolderAdapter adapter;
	private LinearLayout progress;
	private PullToRefreshListView pullToRefreshListView;
   private static final int MENU_CALL = Menu.FIRST;
	private static final int MENU_TEXT = Menu.FIRST + 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       getActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.pullable_list);
        OpenVBX = (OpenVBXApplication) getApplication();
        progress = (LinearLayout) findViewById(R.id.progress);
        pullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pullable_listview);
        ListView listView = pullToRefreshListView.getRefreshableView();
        list = new ArrayList<Folder>();
        adapter = new FolderAdapter(this, R.layout.folders, list);
        listView.setAdapter(adapter);
        pullToRefreshListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                refresh();
            }
        });
        progress.setVisibility(View.VISIBLE);
        refresh();
        getOutgoingCallerIDs();
    }

    public void refresh() {
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("Accept", "application/json");
		client.setBasicAuth(OpenVBX.getEmail(), OpenVBX.getPassword());
		client.get(OpenVBX.getServer() + "/messages/inbox", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode,
                                  Header[] headers,
                                  JSONObject res) {
                try {
					JSONArray folders = res.getJSONArray("folders");
					list.clear();
					for(int i = 0; i < folders.length(); i++)
						list.add(new Folder(folders.getJSONObject(i).getInt("id"),
                                           folders.getJSONObject(i).getString("name"),
                                           folders.getJSONObject(i).getString("type"),
                                           folders.getJSONObject(i).getInt("total"),
                                           folders.getJSONObject(i).getInt("new")));
					adapter.notifyDataSetChanged();
					progress.setVisibility(View.GONE);
					pullToRefreshListView.onRefreshComplete();
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void getOutgoingCallerIDs() {
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("Accept", "application/json");
		client.setBasicAuth(OpenVBX.getEmail(), OpenVBX.getPassword());
		client.get(OpenVBX.getServer() + "/numbers/outgoingcallerid", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode,
                                  Header[] headers,
                                  JSONArray res) {
                try {
                	ArrayList<CallerID> callerids = new ArrayList<CallerID>();
                	for(int i = 0; i < res.length(); i++)
                		callerids.add(new CallerID(res.getJSONObject(i).getString("phone_number"), res.getJSONObject(i).getString("name"), res.getJSONObject(i).getJSONObject("capabilities").getBoolean("sms")));
					OpenVBX.setCallerIDs(callerids);
                } catch(JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	Folder folder = adapter.get(position - 1);
    	Intent i = new Intent(this, inbox.class);
    	i.putExtra("id", folder.getId());
    	i.putExtra("name", folder.getName());
    	startActivityForResult(i, 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 0 && resultCode == RESULT_OK)
			refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
      getMenuInflater().inflate(R.menu.folders_menu, menu);
    	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i;
    	switch(item.getItemId()) {
         case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
     		case R.id.action_call:
    			i = new Intent(getApplicationContext(), call.class);
    			startActivity(i);
    			return true;
    		case R.id.action_sms:
    			i = new Intent(getApplicationContext(), sms.class);
    			startActivity(i);
    			return true;
    	}
    	return super.onOptionsItemSelected(item);
    }
}
