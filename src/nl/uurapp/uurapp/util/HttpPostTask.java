package nl.uurapp.uurapp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
//import android.util.Log;
import android.util.Log;


public class HttpPostTask extends AsyncTask<Void, Void, String> {
	public static int BUFFERED_READER_SIZE = 8192;
	private HttpClient mHttpClient;
	private String taskDescription;
	
	public HttpPostTask(String url, HttpTaskListener listener, String task) {
		super();
		mUrl = url;
		mListener = listener;
		mNameValuePairs = new ArrayList<NameValuePair>();
		mHttpClient = new DefaultHttpClient();
		taskDescription = task;
	}
	
	public String getTaskDescription()
	{
		return taskDescription;
	}
	
	@Override
	protected String doInBackground(Void... params) {
		String responseStr = null;
		try {
			HttpPost httpPost = new HttpPost(mUrl);
			//Log.i("HttpPostTask", "doInBackground: URL=" + mUrl);
			//Log.i("HttpPostTask", "doInBackground: " + mNameValuePairs);
			httpPost.setEntity(new UrlEncodedFormEntity(mNameValuePairs));
			HttpResponse response = mHttpClient.execute(httpPost);

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()), BUFFERED_READER_SIZE);
			StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = rd.readLine()) != null) {
				sb.append(line);
				//Log.i("HttpPostTask", "doInBackground: line: " + line);
			}
			responseStr = sb.toString();
		} catch (ClientProtocolException e) {
			//Log.i("HttpPostTask", "doInBackground: ClientProtocolException");
		} catch (IOException e) {
			Log.i("HttpPostTask", "doInBackground: IOException: " + e);
		}
		
		return responseStr;
	}
	
	public void addSetting(String name, String value) {
		mNameValuePairs.add(new BasicNameValuePair(name, value));
	}
	
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);

		if (result == null) {
			//Log.i("HttpPostTask", "result null");
			mListener.taskDidFail(this);
		}
		else {
			//Log.i("HttpPostTask", "result not null");
			mListener.taskFinishedWithData(result, this);
		}

	}
	
	String mUrl;
	HttpTaskListener mListener;
	List<NameValuePair> mNameValuePairs;
}
