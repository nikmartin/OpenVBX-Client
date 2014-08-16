package org.openvbx;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

public class login extends ActionBarActivity {

	private OpenVBXApplication OpenVBX;
	private String email = null;
	private String password = null;
	private Context context = this;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.login);
        OpenVBX = (OpenVBXApplication) getApplication();
		((Button) findViewById(R.id.login)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (hasValidInput()) {
					OpenVBX.dialog(context, "Attempting to sign in...");
					AsyncHttpClient client = new AsyncHttpClient();
					client.addHeader("Accept", "application/json");
					client.setBasicAuth(email, password);
					client.get(OpenVBX.getServer() + "/messages/inbox", new JsonHttpResponseHandler() {
			            @Override
			            public void onSuccess(int statusCode,
                                           Header[] headers,
                                           JSONObject res) {
		                    OpenVBX.setLoginCredentials(email, password);
		                    OpenVBX.dismissDialog();
		                    if("".equals(OpenVBX.getDevice())) {
			                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			                    OpenVBX.setDevice(telephonyManager.getLine1Number());
		                    }
		                    if("".equals(OpenVBX.getDevice()) || Build.PRODUCT.equals("sdk") || Build.PRODUCT.equals("google_sdk")) {
		                    	Intent i = new Intent(getApplicationContext(), settings.class);
								startActivity(i);
		                    }
		                    else {
								Intent i = new Intent(getApplicationContext(), folders.class);
								startActivity(i);
		                    }
			            }

			            @Override
			            public void onFailure(int statusCode,
                                           Header[] headers,
                                           String content,
                                           Throwable error) {
			            	OpenVBX.alert(context, "Login Failed", "Your email address or password is invalid.");
			            }
			        });
				}
			}
		});
    	((EditText) findViewById(R.id.email)).setText(OpenVBX.getEmail());
    	((EditText) findViewById(R.id.password)).setText(OpenVBX.getPassword());
    }

    private boolean hasValidInput() {
    	email = ((EditText) findViewById(R.id.email)).getText().toString();
    	password = ((EditText) findViewById(R.id.password)).getText().toString();
    	return !"".equals(email) && !"".equals(password);
    }

	@Override
	protected void onPause() {
		super.onPause();
		OpenVBX.dismissDialog();
	}

   @Override
   public boolean onOptionsItemSelected(final MenuItem item) {
      switch (item.getItemId()) {
         case android.R.id.home:
            NavUtils.navigateUpFromSameTask(this);
            return true;
         default:
            return true;
      }
   }
}
