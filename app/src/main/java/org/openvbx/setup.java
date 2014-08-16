package org.openvbx;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

public class setup extends ActionBarActivity {

	private OpenVBXApplication OpenVBX;
	private String server = null;
	private Context context = this;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.setup);
        OpenVBX = (OpenVBXApplication) getApplication();
		((Button) findViewById(R.id.next)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (hasValidInput()) {
					OpenVBX.dialog(context, "Verifying OpenVBX Server...");
					AsyncHttpClient client = new AsyncHttpClient();
					client.addHeader("Accept", "application/json");
					client.get(server + "/client", new JsonHttpResponseHandler() {
			            @Override
			            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
			                try {
				    			if(false == res.getBoolean("error"))
				    				OpenVBX.setServer(server);
			                } catch(JSONException e) {
			                    e.printStackTrace();
			                }
			                OpenVBX.dismissDialog();
			    			if("".equals(OpenVBX.getServer()))
			    				OpenVBX.alert(context, "Error", "The URL provided doesn't seem to point to a an OpenVBX installation.");
			    			else {
			    				Intent i = new Intent(getApplicationContext(), login.class);
			    				startActivity(i);
			    			}
			            }

			            @Override
			            public void onFailure(int statusCode, Header[] headers,  String content, Throwable error) {
			            	OpenVBX.alert(context, "Error", "The URL provided doesn't seem to point to a an OpenVBX installation.");
			            }
			        });
				}else {
               OpenVBX.alert(context, "Error", "The URL provided is not valid.");
            }
			}
		});
        ((EditText) findViewById(R.id.server)).setText(OpenVBX.getServer());
    }

    private boolean hasValidInput() {
    	server = ((EditText) findViewById(R.id.server)).getText().toString();
    	return Patterns.WEB_URL.matcher(server).matches();
    }

	@Override
	protected void onPause() {
		super.onPause();
		OpenVBX.dismissDialog();
	}
}
