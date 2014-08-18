package org.openvbx;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class inbox extends ListActivity {

   private OpenVBXApplication OpenVBX;
   private int folder_id;
   private ArrayList<Messages> list;
   private MessagesAdapter adapter;
   private LinearLayout progress;

   private Context context = this;
   private Menu inboxMenu;
   private SwipeRefreshLayout swipeLayout;

   private static final int OPTION_ARCHIVE = Menu.FIRST + 2;

   /**
    * Called when the activity is first created.
    */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getActionBar().setDisplayHomeAsUpEnabled(true);
      setContentView(R.layout.swipe_list);
      ListView listView;
      swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
      swipeLayout.setEnabled(false);

      // sets the colors used in the refresh animation
      swipeLayout.setColorSchemeColors(Color.parseColor("#3d9400"),
                                            Color.DKGRAY,
                                            Color.WHITE,
                                            Color.LTGRAY);

      swipeLayout.setOnRefreshListener(onRefreshListener);

      OpenVBX = (OpenVBXApplication) getApplication();
      Bundle extras = getIntent().getExtras();
      listView = (ListView) findViewById(android.R.id.list);
      folder_id = extras.getInt("id");
      setTitle(extras.getString("name"));
      list = new ArrayList<Messages>();
      adapter = new MessagesAdapter(this, R.layout.inbox_item, list);
      listView.setAdapter(adapter);

      swipeLayout.setEnabled(true);
      refresh();
      registerForContextMenu(listView);
   }

   SwipeRefreshLayout.OnRefreshListener onRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {

      @Override
      public void onRefresh() {
         refresh();
      }
   };

   public void refresh() {
      swipeLayout.setRefreshing(true);
      AsyncHttpClient client = new AsyncHttpClient();
      client.addHeader("Accept", "application/json");
      client.setBasicAuth(OpenVBX.getEmail(), OpenVBX.getPassword());
      client.get(OpenVBX.getServer() + "/messages/inbox/" + folder_id, new JsonHttpResponseHandler() {
         @Override
         public void onSuccess(int statusCode,
                               Header[] headers,
                               JSONObject res) {
            try {
               JSONArray messages = res.getJSONObject("messages").getJSONArray("items");
               list.clear();
               for (int i = 0; i < messages.length(); i++)
                  list.add(new Messages(messages.getJSONObject(i).getInt("id"), messages.getJSONObject(i).getString("folder"), messages.getJSONObject(i).getString("type"), messages.getJSONObject(i).getString("caller"), messages.getJSONObject(i).getString("short_summary"), messages.getJSONObject(i).getBoolean("unread"), DateFormat.format("M/dd/yy", new SimpleDateFormat("yyyy-MMM-dd'T'HH:mm:ssZ").parse(messages.getJSONObject(i).getString("received_time"))).toString()));
               adapter.notifyDataSetChanged();

            } catch (JSONException e) {
               e.printStackTrace();
            } catch (ParseException e) {
               e.printStackTrace();
            }
            swipeLayout.setRefreshing(false);
         }
      });
   }


   protected void onListItemClick(ListView l, View v, int position, long id) {
      super.onListItemClick(l, v, position, id);
      Messages message = adapter.get(position - 1);
      Intent i = new Intent(this, message.class);
      i.putExtra("id", message.getId());
      ((TextView) v.findViewById(R.id.caller)).setTypeface(null, Typeface.NORMAL);
      ((TextView) v.findViewById(R.id.folder)).setTypeface(null, Typeface.NORMAL);
      ((TextView) v.findViewById(R.id.short_summary)).setTypeface(null, Typeface.NORMAL);
      ((TextView) v.findViewById(R.id.received_time)).setTypeface(null, Typeface.NORMAL);
      startActivityForResult(i, 0);
      setResult(RESULT_OK);
   }

   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == 0 && resultCode == RESULT_OK) {
         Bundle extras = data.getExtras();
         int message_id = extras.getInt("message_id");
         for (int i = 0; i < list.size(); i++)
            if (list.get(i).getId() == message_id)
               list.remove(i);
         adapter.notifyDataSetChanged();
      }
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {

      inboxMenu = menu;
      getMenuInflater().inflate(R.menu.folders_menu, inboxMenu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(final MenuItem item) {
      final Intent i;
      switch (item.getItemId()) {
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

   @Override
   public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      menu.add(0, OPTION_ARCHIVE, 0, "Delete Message");
   }

   @Override
   public boolean onContextItemSelected(MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      final Messages message = adapter.get(info.position - 1);
      switch (item.getItemId()) {
         case OPTION_ARCHIVE:
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("Accept", "application/json");
            client.setBasicAuth(OpenVBX.getEmail(), OpenVBX.getPassword());
            RequestParams params = new RequestParams();
            params.put("archived", "true");
            client.post(OpenVBX.getServer() + "/messages/details/" + message.getId(), params, new JsonHttpResponseHandler() {
               @Override
               public void onSuccess(int statusCode,
                                     Header[] headers,
                                     JSONObject res) {
                  list.remove(adapter.indexOf(message.getId()));
                  adapter.notifyDataSetChanged();
                  OpenVBX.status(context, "Message deleted");
                  setResult(RESULT_OK);
               }
            });
            return true;
      }
      return super.onContextItemSelected(item);
   }
}
