package nl.uurapp.uurapp.fragments;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nl.uurapp.uurapp.ApplicationEx;
import nl.uurapp.uurapp.R;
import nl.uurapp.uurapp.ApplicationEx.Activities;
import nl.uurapp.uurapp.adapter.SimpleAdapterAlternateRowColor;
import nl.uurapp.uurapp.util.HttpPostTask;
import nl.uurapp.uurapp.util.HttpTaskListener;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class FragmentSelectActivity extends Fragment implements
		HttpTaskListener {

	final int DISPLAY_LOADING = 1;
	final int DISPLAY_EMPTY = 2;
	final int DISPLAY_LIST = 3;

	private ListView listView;
	private TextView tvListInfo;
	private FragmentEventsListener listener;
	private ApplicationEx mApp;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_select_activity,
				container, false);

		mApp = ApplicationEx.getContext();

		// get controls
		tvListInfo = (TextView) view.findViewById(R.id.listInfo);
		listView = (ListView) view.findViewById(R.id.listViewActivities);

		// Get organizations

		HttpPostTask httpRequest = new HttpPostTask(ApplicationEx.apiURL,
				FragmentSelectActivity.this, "activities");
		httpRequest.addSetting("cmd", "activities");
		httpRequest.addSetting("token", mApp.getToken());
		httpRequest.execute();

		setListViewMode(DISPLAY_LOADING);
		/*
		 * if ((progressDialog == null) || !progressDialog.isShowing())
		 * progressDialog = ProgressDialog.show(FragmentTimeEntryP2.this, "",
		 * "Ophalen organisaties...", true);
		 */

		// Set ListView listener
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				TextView tvName = (TextView) arg1
						.findViewById(R.id.rowValueName);
				TextView tvID = (TextView) arg1.findViewById(R.id.rowValueID);
				String activityID = (String) tvID.getText();
				String activityName = (String) tvName.getText();
				selectedActivity(activityID, activityName);

				listener.DetailChanged(true);
			}
		});

		setListViewMode(DISPLAY_LOADING);
		return view;
	}

	// Interface with Activity
	public interface FragmentEventsListener {
		public void DetailChanged(Boolean toMaster);

		public void Alert(String text);

		public void Toast(String text);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof FragmentEventsListener) {
			listener = (FragmentEventsListener) activity;
		} else {
			throw new ClassCastException(
					activity.toString()
							+ " must implemenet FragmentSelectActivity.FragmentEventsListener");
		}
	}

	public void selectedActivity(String activityID, String activityName) {
		mApp.getSelection().setActivityID(activityID);
		mApp.getSelection().setActivityName(activityName);
		listener.DetailChanged(true);
	}

	public void setListViewMode(int Mode) {
		int modeListView = 0;
		int modeTextField = 0;
		String Text = "";

		switch (Mode) {
		case DISPLAY_LOADING:
			modeListView = View.GONE;
			modeTextField = View.VISIBLE;
			Text = "Ophalen...";
			break;
		case DISPLAY_EMPTY:
			modeListView = View.GONE;
			modeTextField = View.VISIBLE;
			Text = "Er zijn geen activiteiten.";
			break;
		case DISPLAY_LIST:
			modeListView = View.VISIBLE;
			modeTextField = View.GONE;
			break;
		}
		listView.setVisibility(modeListView);
		tvListInfo.setVisibility(modeTextField);
		tvListInfo.setText(Text);
	}

	@Override
	public void taskFinishedWithData(String data, HttpPostTask task) {

		if (!this.isAdded() || this.isRemoving())
			return;
				
		// Get JSON Data
		if (data.startsWith("{")) // not array
		{
			try {
				JSONObject cDictionary = new JSONObject(data);

				if (!cDictionary.isNull(ApplicationEx.dictionary_RESULT)) {
					String result = cDictionary
							.getString(ApplicationEx.dictionary_RESULT);
					if (result.equals(ApplicationEx.result_NOT_LOGGED_ON)) {
						listener.Toast("Ophalen mislukt. U bent niet meer aangemeld.");
						return;
					}
				}
			} catch (Exception e) {
			}
			;
			listener.Toast("Ophalen activiteiten mislukt.");
			return;
		}

		mApp.activityList.clear();
		try {
			JSONArray jArray = new JSONArray(data);
			for (int i = 0; i < jArray.length(); i++) {
				JSONObject row = jArray.getJSONObject(i);
				Activities mActivities = new Activities(row.getString("id"),
						row.getString("description"));
				mApp.activityList.add(mActivities);
			}

		} catch (JSONException e) {
			e.printStackTrace();
			listener.Toast("Ophalen activiteiten mislukt.");
		}
		if (mApp.activityList.size() > 0) {
			try {

				setListViewMode(DISPLAY_LIST);
				// create list
				ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
				for (Activities row : mApp.activityList) {
					HashMap<String, String> HashRow = new HashMap<String, String>();
					HashRow.put("name", row.getDescription());
					HashRow.put("id", row.getId());
					list.add(HashRow);
				}
				// create adapter to connect listview to list
				SimpleAdapterAlternateRowColor adapter = new SimpleAdapterAlternateRowColor(
						getActivity(), list, R.layout.valuerow, new String[] {
								"name", "id" }, new int[] { R.id.rowValueName,
								R.id.rowValueID });

				// bind adapter to list
				listView.setAdapter(adapter);
			} catch (Exception ex) {
				ex.printStackTrace();
				setListViewMode(DISPLAY_EMPTY);
				listener.Toast("Kan activiteiten niet afbeelden.");
			}
		} else {
			setListViewMode(DISPLAY_EMPTY);
		}
	}

	@Override
	public void taskDidFail(HttpPostTask task) {
		
		if (!this.isAdded() || this.isRemoving())
			return;
		
		setListViewMode(DISPLAY_EMPTY);
		listener.Toast("Ophalen activiteiten mislukt.");
	}
}
