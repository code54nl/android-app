package nl.uurapp.uurapp.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import nl.uurapp.uurapp.ApplicationEx;
import nl.uurapp.uurapp.R;
import nl.uurapp.uurapp.adapter.SimpleAdapterAlternateRowColor;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FragmentMaster extends Fragment {

	private ListView listView;
	private OnMasterClickListener listener;
	private ApplicationEx mApp;
	private ArrayList<HashMap<String, String>> list;
	private SimpleAdapterAlternateRowColor adapter;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater
				.inflate(R.layout.fragment_master, container, false);

		// get controls
		mApp = ApplicationEx.getContext();
		listView = (ListView) view.findViewById(R.id.listViewMaster);
		list = new ArrayList<HashMap<String, String>>();

		// Set ListView listener
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				int selectedPosition = arg2;
				switch (selectedPosition) {
				case 0:
					listener.SelectDate();
					break;
				case 1:
					listener.SelectOrganisation();
					break;
				case 2:
					listener.SelectTime();
					break;
				case 3:
					listener.SelectAddress();
					break;
				case 4:
					listener.SelectAcivity();
					break;
				case 5:
					listener.SelectProject();
					break;
				case 6:
					listener.SelectDescription();
					break;
				case 7:
					listener.SelectRemarks();
					break;
				}
			}
		});
		return view;
	}

	// Interface with Activity
	public interface OnMasterClickListener {
		public void SelectDate();

		public void SelectTime();

		public void SelectOrganisation();

		public void SelectAddress();

		public void SelectAcivity();

		public void SelectProject();

		public void SelectDescription();

		public void SelectRemarks();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof OnMasterClickListener) {
			listener = (OnMasterClickListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implemenet FragmentMaster.OnMasterClickListener");
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// create adapter to connect listview to list
		adapter = new SimpleAdapterAlternateRowColor(getActivity(), list,
				R.layout.master_row, new String[] { "key", "value" },
				new int[] { R.id.rowMasterKey, R.id.rowMasterValue });

		// bind adapter to list
		listView.setAdapter(adapter);
		UpdateList();
	}

	public void UpdateList() {
		// Fill Listview Masterlist

		if (!this.isAdded()) return;
		
		Log.i(ApplicationEx.TAG, "FragmentMaster: UpdateList()");

		SimpleDateFormat Formatter = new SimpleDateFormat("E d-M-yyyy",
				Locale.getDefault());
		String textDate = "-";
		if (mApp.getSelection().getDate() != null) {
			textDate = Formatter.format(mApp.getSelection().getDate());
		}
		list.clear();

		HashMap<String, String> HashRow = new HashMap<String, String>();
		HashRow.put("key", "Datum:");
		HashRow.put("value", textDate);
		list.add(HashRow);

		HashRow = new HashMap<String, String>();
		HashRow.put("key", "Organisatie:");
		HashRow.put("value", mApp.getSelection().getOrganizationName());
		list.add(HashRow);

		String timestr = "";
		if (mApp.getSelection().getHours().floatValue() == 0) {
			timestr = "-";
		} else {
			if (mApp.getSelection().getStartTime() != null) {
				timestr = "Van " + mApp.getSelection().getStartTimeString()
						+ " tot " + mApp.getSelection().getEndTimeString()
						+ ", ";
			}
			timestr += mApp.getSelection().getHours().toString() + " uren";
		}
		HashRow = new HashMap<String, String>();
		HashRow.put("key", "Tijd:");
		HashRow.put("value", timestr);
		list.add(HashRow);

		HashRow = new HashMap<String, String>();
		HashRow.put("key", "Adres:");
		HashRow.put("value", mApp.getSelection().getAddressName());
		list.add(HashRow);

		HashRow = new HashMap<String, String>();
		HashRow.put("key", "Activiteit:");
		HashRow.put("value", mApp.getSelection().getActivityName());
		list.add(HashRow);

		HashRow = new HashMap<String, String>();
		HashRow.put("key", "Project:");
		HashRow.put("value", mApp.getSelection().getProjectPhaseName());
		list.add(HashRow);

		HashRow = new HashMap<String, String>();
		HashRow.put("key", "Omschrijving:");
		HashRow.put("value", mApp.getSelection().getDescription());
		list.add(HashRow);

		HashRow = new HashMap<String, String>();
		HashRow.put("key", "Opmerking:");
		HashRow.put("value", mApp.getSelection().getRemarks());
		list.add(HashRow);

		adapter.notifyDataSetChanged();
	}
}
