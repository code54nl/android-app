package nl.uurapp.uurapp;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import nl.uurapp.uurapp.ApplicationEx.TimeRow;
import nl.uurapp.uurapp.adapter.TimeRowAdapter;
import nl.uurapp.uurapp.adapter.TimeRowAdapter.ListRowHolder;
import nl.uurapp.uurapp.util.HttpPostTask;
import nl.uurapp.uurapp.util.HttpTaskListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityTimeList extends Activity implements HttpTaskListener {

	private ProgressDialog progressDialog = null;
	private TimeRowAdapter adapter;

	// Status User Interface
	private final int DISPLAY_LOADING = 1;
	private final int DISPLAY_EMPTY = 2;
	private final int DISPLAY_LIST = 3;

	private CountDownTimer waitTimer;
	private static final long timerInterval = 1000;
	private ListView lView;
	private DatePicker datePicker;
	private ImageButton btnAdd;
	private ImageButton btnToday;
	private ImageButton btnLogOff;
	private TextView tvListInfo;
	private Boolean updatingTimeRows = false;
	private ApplicationEx mApp;
	private Boolean skipOnDateChangedEvent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_list);
		mApp = ApplicationEx.getContext();

		// get controls
		lView = (ListView) findViewById(R.id.timeList);
		datePicker = (DatePicker) findViewById(R.id.datePicker1);
		btnAdd = (ImageButton) findViewById(R.id.buttonListAdd);
		btnToday = (ImageButton) findViewById(R.id.buttonListToday);
		btnLogOff = (ImageButton) findViewById(R.id.buttonListLogOff);
		tvListInfo = (TextView) findViewById(R.id.timeListInfo);

		skipOnDateChangedEvent = false;

		// Set DatePicker to Selection
		Calendar mDate = Calendar.getInstance();
		mDate.setTime(mApp.getSelection().getDate());

		// Create OnDateChangedListener
		datePicker.init(mDate.get(Calendar.YEAR), mDate.get(Calendar.MONTH),
				mDate.get(Calendar.DAY_OF_MONTH), new OnDateChangedListener() {

					@Override
					public void onDateChanged(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {

						if (skipOnDateChangedEvent)
							return;

						// On Changed data, save selected data and update
						// time-rows.
						Calendar pickedDate = Calendar.getInstance();
						pickedDate.set(year, monthOfYear, dayOfMonth);
						mApp.getSelection().setDate(pickedDate.getTime());
						if (waitTimer != null) {
							waitTimer.cancel();
							waitTimer.start();
						}
					}
				});

		View header = (View) getLayoutInflater().inflate(
				R.layout.timerowheader, null);
		lView.addHeaderView(header);

		// Create OnClik Listener for Add-button

		btnAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mApp.getSelection().Reset();
				Intent intent = new Intent(ActivityTimeList.this,
						ActivityEditEntry.class);
				ActivityTimeList.this.startActivity(intent);
			}
		});

		// Create OnClik Listener for Today-button

		btnToday.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SetSelectionAndCalenderToday();
			}
		});
		// Create OnClik Listener for Logoff-button

		btnLogOff.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Create Are You Sure dialog
				// http://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-in-android
				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:

							// disable autologon
							SharedPreferences preferences = getSharedPreferences(
									ApplicationEx.PREF_FILE_NAME, MODE_PRIVATE);
							SharedPreferences.Editor editor = preferences
									.edit();
							editor.putBoolean("autologon", false);
							editor.commit();

							Intent intent = new Intent(ActivityTimeList.this,
									ActivityMain.class);
							ActivityTimeList.this.startActivity(intent);
							finish();
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							// No button clicked
							dialog.dismiss();
							break;
						}
					}
				};
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ActivityTimeList.this);
				builder.setMessage("Afmelden?")
						.setPositiveButton("Ja", dialogClickListener)
						.setNegativeButton("Nee", dialogClickListener).show();
			}
		});

		// Set ListView long click listener to open the context-menu only if
		// this row is not declared yet
		lView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				ListRowHolder holder = (ListRowHolder) arg1.getTag();

				if (!holder.declared) {
					// show context menu
					mApp.getSelection().setTimeRowID(holder.timeRowID);
					registerForContextMenu(lView);
					openContextMenu(lView);
					unregisterForContextMenu(lView);
				} else {
					Toast.makeText(
							ActivityTimeList.this,
							"Regel kan niet gewijzigd worden omdat hij al gedeclareerd is.",
							Toast.LENGTH_SHORT).show();
				}
				return true;
			}

		});

		// Set waittimer, to wait 1 second before updating timerow
		waitTimer = new CountDownTimer(timerInterval, timerInterval) {

			public void onTick(long millisUntilFinished) {
				// never called (0)
			}

			public void onFinish() {
				// After timerInterval-milliseconds finish
				// Toast.makeText(TimeList.this, "Timer Finish.",
				// Toast.LENGTH_SHORT).show();
				skipOnDateChangedEvent = true;
				updateDate(); // this forces the spinner to spin just a little
								// back or forth, so it is straight on the
								// selection
				skipOnDateChangedEvent = false;
				updateTimeRows();
			}
		};
	}

	private void updateDate() {
		// Get addresses
		Calendar mDate = Calendar.getInstance();
		mDate.setTime(mApp.getSelection().getDate());
		datePicker.updateDate(mDate.get(Calendar.YEAR),
				mDate.get(Calendar.MONTH), mDate.get(Calendar.DAY_OF_MONTH));
	}

	private Date RemoveTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	private Boolean SelectedTimeEqualsDateControl() {
		Calendar calToday = Calendar.getInstance();
		Date dateSelected = mApp.getSelection().getDate();
		return RemoveTime(calToday.getTime()).equals(RemoveTime(dateSelected));
	}

	private void SetSelectionAndCalenderToday() {
		Calendar calToday = Calendar.getInstance();
		mApp.getSelection().setDate(calToday.getTime());

		int year = calToday.get(Calendar.YEAR);
		int month = calToday.get(Calendar.MONTH);
		int day = calToday.get(Calendar.DAY_OF_MONTH);
		datePicker.updateDate(year, month, day);
		Toast.makeText(ActivityTimeList.this, "Datum aangepast naar vandaag.",
				Toast.LENGTH_SHORT).show();
	}

	// On App resume update Timerows and set to Calendar today if not.
	protected void onResume() {
		super.onResume();

		// Set ListView adapter
		adapter = new TimeRowAdapter(this, R.layout.timerow, mApp.timeRowList);
		lView.setAdapter(adapter);

		if ((mApp.getSelection() != null) &&
				(mApp.getSelection().getDate() != null)) {
			updateDate();
		}
		/*
		 * Only UpdateTimeRows if we resume on the same day as selected. If
		 * day changed, we change the DatePicker-control that triggers an
		 * update by itself. if (!SelectedTimeEqualsDateControl()) {
		 * SetSelectionAndCalenderToday(); } else updateTimeRows();
		 */
		
		// Time to request review?
		if (mApp.getTimerowsavings_until_review() == 1) // 0 = never ask,
														// already reviewed
		{

			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						// Yes button clicked
						mApp.setTimerowsavings_until_review((long) 0); // no
																		// more
						Uri marketUri = Uri
								.parse("market://details?id=nl.uurapp.uurapp");
						Intent marketIntent = new Intent(Intent.ACTION_VIEW,
								marketUri);
						startActivity(marketIntent);
						break;

					case DialogInterface.BUTTON_NEGATIVE:
						mApp.setTimerowsavings_until_review(ApplicationEx.savedTimeRowsBeforeReview); // ask
																										// again,
																										// after
																										// xxx
																										// saved
																										// timerows
						break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Bedankt voor het gebruik van Uurapp! Als je tevreden bent kun je ons enorm helpen "
							+ " door het plaatsen van een positieve review. "
							+ "Wil je dat nu doen?")
					.setPositiveButton("Ja", dialogClickListener)
					.setNegativeButton("Niet nu", dialogClickListener).show();

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			Intent intent = new Intent(ActivityTimeList.this,
					ActivityAbout.class);
			startActivity(intent);
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (progressDialog != null)
			progressDialog.dismiss();
		progressDialog = null;
	}

	public void setListViewMode(int Mode) {
		int modeListView = 0;
		int modeTextField = 0;
		boolean activateDatePicker = true;
		String Text = "";

		switch (Mode) {
		case DISPLAY_LOADING:
			modeListView = View.GONE;
			activateDatePicker = false;
			break;
		case DISPLAY_EMPTY:
			modeListView = View.INVISIBLE;
			modeTextField = View.VISIBLE;
			Text = "Er zijn geen geboekte uren.";
			activateDatePicker = true;
			break;
		case DISPLAY_LIST:
			modeListView = View.VISIBLE;
			modeTextField = View.GONE;
			activateDatePicker = true;
			break;
		}

		lView.setVisibility(modeListView);
		tvListInfo.setText(Text);
		tvListInfo.setVisibility(modeTextField);
		datePicker.setEnabled(activateDatePicker);

		// display mode changed, reset selection of timerow
		ResetSelection();
	}

	public void ResetSelection() {
		mApp.getSelection().setTimeRowID("");
	}

	public void postRequestTimeRowDetails() {
		String timeRowID = mApp.getSelection().getTimeRowID();
		// HTTP POST
		HttpPostTask httpRequest = new HttpPostTask(ApplicationEx.apiURL,
				ActivityTimeList.this, "rowdetails");
		httpRequest.addSetting("cmd", "rowdetails");
		httpRequest.addSetting("timerowid", timeRowID);
		httpRequest.addSetting("token", mApp.getToken());
		httpRequest.execute();
		if ((progressDialog == null) || !progressDialog.isShowing())
			progressDialog = ProgressDialog.show(ActivityTimeList.this, "",
					"Ophalen tijdregel...", true);
	}

	public void postRequestTimeRowDelete() {
		String timeRowID = mApp.getSelection().getTimeRowID();
		// HTTP POST
		HttpPostTask httpRequest = new HttpPostTask(ApplicationEx.apiURL,
				ActivityTimeList.this, "rowdelete");
		httpRequest.addSetting("cmd", "rowdelete");
		httpRequest.addSetting("timerowid", timeRowID);
		httpRequest.addSetting("token", mApp.getToken());
		httpRequest.execute();
		if ((progressDialog == null) || !progressDialog.isShowing())
			progressDialog = ProgressDialog.show(ActivityTimeList.this, "",
					"Verwijderen tijdregel...", true);
	}

	public void updateTimeRows() {
		if (updatingTimeRows)
			return; // allready updating...
		updatingTimeRows = true;
		setListViewMode(DISPLAY_LOADING);
		String dateStr = mApp.getSelection().getDateString();

		// HTTP POST
		HttpPostTask httpRequest = new HttpPostTask(ApplicationEx.apiURL,
				ActivityTimeList.this, "timerows");
		httpRequest.addSetting("cmd", "timerows");
		httpRequest.addSetting("date", dateStr);
		httpRequest.addSetting("token", mApp.getToken());
		httpRequest.execute();
		try {
			progressDialog = ProgressDialog.show(ActivityTimeList.this, "",
					"Ophalen tijdregels...", true);
		}
		catch (Exception e)
		{
			//nothing
		}	
		// Enable btnTday only when selected day is not today
		btnToday.setEnabled(!SelectedTimeEqualsDateControl());
	}

	@Override
	public void taskFinishedWithData(String data, HttpPostTask task) {

		if (this.isFinishing())
			return;
		try 
		{
			if (progressDialog != null)
				progressDialog.dismiss();
		}
		catch (Exception e)
		{
			//nothing
		}
		updatingTimeRows = false;

		// Get JSON Data
		if (task.getTaskDescription().equals("timerows")) {
			if (data.startsWith("{")) // not array
			{
				try {
					JSONObject cDictionary = new JSONObject(data);
					if (!cDictionary.isNull(ApplicationEx.dictionary_RESULT)) {
						String result = cDictionary
								.getString(ApplicationEx.dictionary_RESULT);
						if (result.equals(ApplicationEx.result_NOT_LOGGED_ON)) {
							Toast.makeText(
									this,
									"Ophalen mislukt. U bent niet meer aangemeld.",
									Toast.LENGTH_LONG).show();
							return;
						}
					}
				} catch (Exception e) {
				}
				;
				Toast.makeText(this, "Ophalen tijdregels mislukt.",
						Toast.LENGTH_LONG).show();
				return;
			}

			try {
				mApp.timeRowList.clear();
				JSONArray jArray = new JSONArray(data);
				for (int i = 0; i < jArray.length(); i++) {
					JSONObject row = jArray.getJSONObject(i);
					mApp.timeRowList.add(new TimeRow(row.getString("id"), row
							.getString("startTime"), row.getString("endTime"),
							row.getString("hours"),
							row.getString("activityID"), row
									.getString("activityAbbrv"), row
									.getString("activityDescription"), row
									.getString("organizationID"), row
									.getString("organizationAbbrv"), row
									.getString("organizationName"), row
									.getString("projectPhaseID"), row
									.getString("projectPhaseAbbrv").equals(
											"null") ? "" : row
									.getString("projectPhaseAbbrv"), row
									.getString("projectPhaseName").equals(
											"null") ? "" : row
									.getString("projectPhaseName"),
							row.getString("projectAbbrv").equals("null") ? ""
									: row.getString("projectAbbrv"),
							row.getString("projectName").equals("null") ? ""
									: row.getString("projectName"), row
									.getString("remarks"), row
									.getString("description"), row
									.getBoolean("declared")));
				}
				Collections.sort(mApp.timeRowList);
				adapter.notifyDataSetChanged();

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setListViewMode(DISPLAY_EMPTY);
				Toast.makeText(this, "Ophalen tijdregels mislukt.",
						Toast.LENGTH_LONG).show();
			}

			if (mApp.timeRowList.size() > 0)
				setListViewMode(DISPLAY_LIST);
			else
				setListViewMode(DISPLAY_EMPTY);

		}
		if (task.getTaskDescription().equals("rowdetails")) {
			try {
				JSONObject jObject = new JSONObject(data);
				mApp.getSelection().setDate(jObject.getString("date"));
				mApp.getSelection()
						.setStartTime(jObject.getString("starttime"));
				mApp.getSelection().setEndTime(jObject.getString("endtime"));
				mApp.getSelection().setHours(jObject.getString("hours"));
				mApp.getSelection().setDescription(
						jObject.getString("description"));
				mApp.getSelection().setRemarks(jObject.getString("remarks"));
				mApp.getSelection().setActivityID(
						jObject.getString("activityId"));
				mApp.getSelection().setActivityName(
						jObject.getString("activityDescription"));
				mApp.getSelection()
						.setAddressID(jObject.getString("addressId"));
				mApp.getSelection().setAddressName(
						jObject.getString("addressDescription"));
				mApp.getSelection().setProjectPhaseID(
						jObject.getString("projectphaseId"));
				mApp.getSelection().setProjectPhaseName(
						jObject.getString("projectphaseName"));
				mApp.getSelection().setOrganizationID(
						jObject.getString("organizationId"));
				mApp.getSelection().setOrganizationName(
						jObject.getString("organizationName"));
				// to next page
				Intent intent = new Intent(ActivityTimeList.this,
						ActivityEditEntry.class);
				ActivityTimeList.this.startActivity(intent);
				return;
			} catch (Exception ex) {
				ex.printStackTrace();
				setListViewMode(DISPLAY_EMPTY);
				Toast.makeText(this, "Ophalen details tijdregels mislukt.",
						Toast.LENGTH_LONG).show();

			}
		}
		if (task.getTaskDescription().equals("rowdelete")) {
			if (data.startsWith("{")) // not array
			{
				try {
					JSONObject cDictionary = new JSONObject(data);
					String result = cDictionary
							.getString(ApplicationEx.dictionary_RESULT);
					if (!cDictionary.isNull(ApplicationEx.dictionary_RESULT)) {
						if (result.equals(ApplicationEx.result_SUCCESS)) {
							updateTimeRows();
							return;
						}

						if (result.equals(ApplicationEx.result_NOT_LOGGED_ON)) {
							Toast.makeText(
									this,
									"Verwijderen mislukt. U bent niet meer aangemeld.",
									Toast.LENGTH_LONG).show();
							return;
						}
					}
				} catch (Exception e) {
				}
				;
				Toast.makeText(this, "Verwijderen mislukt.", Toast.LENGTH_LONG)
						.show();
				return;
			}
		}
	}

	@Override
	public void taskDidFail(HttpPostTask task) {

		if (this.isFinishing())
			return;
		try 
		{
			if (progressDialog != null)
				progressDialog.dismiss();
		}
		catch (Exception e)
		{
			//nothing
		}	
		updatingTimeRows = false;
		setListViewMode(DISPLAY_EMPTY);
		Toast.makeText(this, "Fout tijdens communiceren met server.",
				Toast.LENGTH_LONG).show();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater mi = this.getMenuInflater();
		mi.inflate(R.menu.list_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_edit:
			postRequestTimeRowDetails();
			return true;

		case R.id.menu_delete:
			// Create Are You Sure dialog
			// http://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-in-android
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						postRequestTimeRowDelete();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						// No button clicked
						dialog.dismiss();
						break;
					}
				}
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(
					ActivityTimeList.this);
			builder.setMessage("Regel verwijderen?")
					.setPositiveButton("Ja", dialogClickListener)
					.setNegativeButton("Nee", dialogClickListener).show();
			return true;
		}
		return super.onContextItemSelected(item);
	}
}
