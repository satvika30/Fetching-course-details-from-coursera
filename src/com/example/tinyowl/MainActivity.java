package com.example.tinyowl;
import java.io.IOException;
import java.util.ArrayList;
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

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

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

public class MainActivity extends ListActivity{
	
	ListView listview;
	String univ_name;
	ArrayList <String> resu;
	ArrayList <String> course_id;
	ArrayList <String> university_id;
	ArrayList <String> instructor_id;
	ArrayList <String> url;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		course_id = new ArrayList <String>();
		university_id = new ArrayList <String>();
		instructor_id = new ArrayList <String>();
		resu = new ArrayList<String>();
		
	    if (isNetworkAvailable()) {
	        new HttpGetTask().execute();
	    } else {
	    	resu.add("check internet connectivity");
	    	setListAdapter(new ArrayAdapter(this, R.layout.listview, resu));
	    }		
	}
	
	private boolean isNetworkAvailable() {
		ConnectivityManager cm =
		        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		 
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
		    return true;
		} else {
		    return false;
		}
	}
	
	private class HttpGetTask extends AsyncTask<Void,Void,ArrayList<String>> {
		AndroidHttpClient e = AndroidHttpClient.newInstance("");
		
		@Override
		protected ArrayList <String> doInBackground(Void... arg0) {
			HttpGet request = new HttpGet("https://api.coursera.org/api/catalog.v1/courses?fields=instructor&includes=universities,instructors");
			Responsehandler response = new Responsehandler();
			try {
				return e.execute(request, response);
			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(final ArrayList <String> result) {
						
			for (int u = 0; u < result.size(); u++) {
				String item = result.get(u);
				String univ_id = university_id.get(u);		        
		        univ_id = univ_id.replace("[", "");
		        univ_id = univ_id.replace("]", "");
		        Log.e("university id", univ_id);	        
		        
		        try {
					univ_name = new HttpGetTask1().execute(univ_id).get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
		        
		        Log.e("name", univ_name);
		        
		        item += "University: " + univ_name;
		        result.set(u, item);
			}
			
			setListAdapter(new ArrayAdapter(MainActivity.this, R.layout.listview, result));
			 
			getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

			      @Override
			      public void onItemClick(AdapterView<?> parent, final View view,
			          int position, long id) {
			    	  
			    	  String u_name = "";
			    	  String item = result.get(position);
			    	  
			          String c_id = course_id.get(position);
			          Log.e("cid", c_id);
			        
			          ArrayList <String> depo = new ArrayList<String>();
			          String instr_id = instructor_id.get(position);			        
			        
			          if (instr_id != "null" && !item.contains("Instructor: Not specified")) {
			        	  instr_id = instr_id.replace("[", "");
			        	  instr_id = instr_id.replace("]", "");
			        	  Log.e("instructor ids", instr_id);
			        	  String[] instructors = instr_id.split(",");
			        	
			        	  for (int y = 0; y < instructors.length; y++) {
			        		  String dept = "";
							  try {
								  dept = new HttpGetTask2().execute(instructors[y]).get();
							  } catch (InterruptedException e) {
								  e.printStackTrace();
							  } catch (ExecutionException e) {
								  e.printStackTrace();
							  }
			        		  if (dept != "") depo.add(dept);
			        	  }
			          }
			        
			          Log.e("departments", depo.toString());
			        
			          String id2 = university_id.get(position);
			          id2 = id2.replace("[", "");
			          id2 = id2.replace("]", "");
			         
			          try {
						  u_name = new HttpGetTask1().execute(id2).get();
					  } catch (InterruptedException | ExecutionException e) {
				          e.printStackTrace();
					  }			        
			        
			          Intent a = new Intent(MainActivity.this, Two.class);
					  a.putExtra("course_id", c_id);
					  a.putExtra("university_name", u_name);
					  a.putExtra("departments", depo.toString());
					  startActivity(a);			        
			      }
			});			
	    }		
	}
	
	
	private class Responsehandler implements ResponseHandler {

		@Override
		public Object handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			
			ArrayList <String> courses_list = new ArrayList <String>();
			String JSONResponse = new BasicResponseHandler().handleResponse(response);
			try {
				JSONObject a = (JSONObject) new JSONTokener(JSONResponse).nextValue();
				JSONArray items = a.getJSONArray("elements");
	            
				/*Currently fetching only 20 courses.
				  Remove the i < 20 condition to fetch all the courses*/
				for (int i = 0; i < items.length() && i < 20; i++) {
					JSONObject item = (JSONObject) items.get(i);
					
					String course_item = "";
					
					course_item += "Course Name: " + item.getString("shortName") + "\n";
					if(item.optString("instructor")=="") course_item += "Instructor: Not specified" + "\n";
					else course_item += "Instructor: " + item.optString("instructor") + "\n";					
					
					course_id.add(item.getString("id"));
					university_id.add(item.getJSONObject("links").getString("universities"));
					
					String op = "null";
					if(item.getJSONObject("links").optString("instructors") != "") instructor_id.add(item.getJSONObject("links").optString("instructors"));
					else instructor_id.add(op);
					
					courses_list.add(course_item);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} 
			return courses_list;
		}		
	}
	
	
	private class HttpGetTask1 extends AsyncTask<String,Void,String> {
		AndroidHttpClient e = AndroidHttpClient.newInstance("");
		
		protected String doInBackground(String... param) {
			
			HttpGet request = new HttpGet("https://api.coursera.org/api/catalog.v1/universities?id="+param[0]);
			
			Responsehandler1 response = new Responsehandler1();
			try {
				return e.execute(request, response);
			} catch (ClientProtocolException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}		
	}
	
	
	private class Responsehandler1 implements ResponseHandler {

		@Override
		public Object handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			
			String u_name = "";
			String JSONResponse = new BasicResponseHandler().handleResponse(response);
			try {
				JSONObject a = (JSONObject) new JSONTokener(JSONResponse).nextValue();
				JSONArray items = a.getJSONArray("elements");
				
				JSONObject item = (JSONObject) items.get(0);
				u_name = item.getString("shortName");			
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return u_name;
		}		
	}
	
	
	private class HttpGetTask2 extends AsyncTask<String,Void,String> {
		AndroidHttpClient e = AndroidHttpClient.newInstance("");
		
		protected String doInBackground(String... param) {
			
			HttpGet request = new HttpGet("https://api.coursera.org/api/catalog.v1/instructors?id="+param[0]+"&fields=department");
			
			Responsehandler2 response = new Responsehandler2();
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
	}
	
	
	private class Responsehandler2 implements ResponseHandler {

		@Override
		public Object handleResponse(HttpResponse response)
				throws ClientProtocolException, IOException {
			
			String dept_name = "";
			String JSONResponse = new BasicResponseHandler().handleResponse(response);
			try {
				JSONObject a = (JSONObject) new JSONTokener(JSONResponse).nextValue();
				JSONArray items = a.getJSONArray("elements");
				JSONObject item = (JSONObject) items.get(0);
				dept_name += item.getString("department");		
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return dept_name;
		}		
	}	
}
