package com.example.tinyowl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class Two extends Activity {
	
	String course_id;
	TextView t;
	String univ_name;
	String dept;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.two);
		course_id = getIntent().getExtras().getString("course_id");
		univ_name = getIntent().getExtras().getString("univerity_name");
		dept = getIntent().getExtras().getString("departments");
		t = (TextView) findViewById(R.id.textView1);
            
		if(isNetworkAvailable()) {
	    	new HttpGetTask().execute();
	    } else {
	    	t.setText("check internet connectivity");
	    }		
	}
	    
	private boolean isNetworkAvailable() {
		ConnectivityManager cm =
		        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
		    return true;
		}
		return false;
	}
	
	
	private class HttpGetTask extends AsyncTask<Void,Void,String> {
		
		AndroidHttpClient e = AndroidHttpClient.newInstance("");
		
		@Override
		protected String doInBackground(Void... arg0) {
			HttpGet request = new HttpGet("https://api.coursera.org/api/catalog.v1/courses?id="+course_id+"&fields=shortDescription,instructor");
			Responsehandler response = new Responsehandler();
			try {
				return e.execute(request, response);
			} catch (ClientProtocolException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			 t.setText(result);		  
		}
		
	}
	
	
	private class Responsehandler implements ResponseHandler {

		@Override
		public Object handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			
			String s = "";
			String JSONResponse = new BasicResponseHandler().handleResponse(response);
			try {
				JSONObject a = (JSONObject) new JSONTokener(JSONResponse).nextValue();
				JSONArray items = a.getJSONArray("elements");				
				JSONObject item = (JSONObject) items.get(0);
				
				s += "Course Name: " + item.getString("name") + "\n\n";
				s += "Description: " + item.getString("shortDescription") + "\n\n";
				if(item.optString("instructor")=="") s += "Instructor: Not specified" + "\n\n";
				else s += "Instructor: " + item.optString("instructor") + "\n\n";				
				s += "University: " + univ_name + "\n\n";
				
				if (dept.equals("[]")) {
					s += "Department: Not Specified";
				} else s += "Department: " + dept; 				
			} catch (JSONException e) {
				e.printStackTrace();
			} 
			return s;
		}		
	}
}
