package nl.uurapp.uurapp;

import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import nl.uurapp.uurapp.fragments.FragmentEnterDescription;
import nl.uurapp.uurapp.fragments.FragmentEnterRemarks;
import nl.uurapp.uurapp.fragments.FragmentMaster;
import nl.uurapp.uurapp.fragments.FragmentSelectActivity;
import nl.uurapp.uurapp.fragments.FragmentSelectAddress;
import nl.uurapp.uurapp.fragments.FragmentSelectDate;
import nl.uurapp.uurapp.fragments.FragmentSelectOrganisation;
import nl.uurapp.uurapp.fragments.FragmentSelectProject;
import nl.uurapp.uurapp.fragments.FragmentSelectTime;
import nl.uurapp.uurapp.util.HttpPostTask;
import nl.uurapp.uurapp.util.HttpTaskListener;
import nl.uurapp.uurapp.util.SoftKeyboard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public class ActivityEditEntry extends FragmentActivity implements
		FragmentSelectActivity.FragmentEventsListener,
		FragmentSelectAddress.FragmentEventsListener,
		FragmentSelectOrganisation.FragmentEventsListener,
		FragmentSelectProject.FragmentEventsListener,
		FragmentSelectTime.FragmentEventsListener,
		FragmentEnterDescription.FragmentEventsListener,
		FragmentEnterRemarks.FragmentEventsListener,
		FragmentSelectDate.FragmentEventsListener,
		FragmentMaster.OnMasterClickListener, HttpTaskListener {

	private ImageButton btnCancel;
	private ImageButton btnFinish;
	private ImageButton btnToday;
	private ImageButton btnDetailOK;
	private ProgressDialog progressDialog;
	private ApplicationEx mApp;
	private Boolean btnCancelShown;
	private Boolean btnFinishShown;
	private Boolean btnTodayShown;
	private Boolean btnDetailOKShown;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edittimerow);

		progressDialog = null;
		mApp = ApplicationEx.getContext();

		btnCancelShown = true;
		btnFinishShown = false;
		btnTodayShown = true;
		btnDetailOKShown = false;

		// get controls
		btnCancel = (ImageButton) findViewById(R.id.buttonCancel);
		btnFinish = (ImageButton) findViewById(R.id.buttonFinish);
		btnToday = (ImageButton) findViewById(R.id.buttonSelectToday);
		btnDetailOK = (ImageButton) findViewById(R.id.buttonDetailOK);

		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				setResult(RESULT_CANCELED);
				finish();
			}
		});

		btnDetailOK.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Activity host = (Activity) v.getContext();
				SoftKeyboard.hideSoftKeyboard(host);
				toMaster();
			}
		});

		btnToday.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				Calendar calToday = Calendar.getInstance();
				mApp.getSelection().setDate(calToday.getTime());

				DetailChanged(true);

				// Update SelectDateFragment if loaded
				FragmentSelectDate myFragment = (FragmentSelectDate) getSupportFragmentManager()
						.findFragmentByTag("FragmentSelectDate");
				if ((myFragment != null) && myFragment.isVisible()) {
					myFragment.updateDate();
				}
				Toast("Datum aangepast naar vandaag.");
			}
		});

		btnFinish.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				postIfValid();
			}
		});

		if (savedInstanceState == null) {
			// load Master Fragment
			FragmentMaster masterFragment = new FragmentMaster();
			masterFragment.setArguments(getIntent().getExtras());
			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			transaction.setCustomAnimations(R.anim.slide_in_bottom,
					R.anim.slide_out_top);
			// Check if the Detail container is loaded (only tablets), place
			// there
			if (findViewById(R.id.containerMaster) != null) {
				transaction.replace(R.id.containerMaster, masterFragment,
						"Master");
			} else {
				transaction.replace(R.id.containerMain, masterFragment,
						"Master");
			}
			transaction.commit();

		}

	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		btnCancelShown = savedInstanceState.getBoolean("btnCancelShown");
		btnFinishShown = savedInstanceState.getBoolean("btnFinishShown");
		btnTodayShown = savedInstanceState.getBoolean("btnTodayShown");
		btnDetailOKShown = savedInstanceState.getBoolean("btnDetailOKShown");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.

		savedInstanceState.putBoolean("btnCancelShown", btnCancelShown);
		savedInstanceState.putBoolean("btnFinishShown", btnFinishShown);
		savedInstanceState.putBoolean("btnTodayShown", btnTodayShown);
		savedInstanceState.putBoolean("btnDetailOKShown", btnDetailOKShown);
	}

	private void showDetailOKIconAndHideOthers() {
		// icons shown in single and multi pane mode

		if (findViewById(R.id.containerMaster) != null) {
			// we are multi pane
			btnDetailOK.setVisibility(View.GONE);
			btnFinish.setVisibility(View.VISIBLE);
			btnToday.setVisibility(View.VISIBLE);
			btnCancel.setVisibility(View.VISIBLE);
		} else {
			// we are single pane
			btnDetailOK.setVisibility(View.VISIBLE);
			btnFinish.setVisibility(View.GONE);
			btnToday.setVisibility(View.GONE);
			btnCancel.setVisibility(View.GONE);
		}

	}

	private void hideDetailOKIconAndShowOthers() {
		// icons shown in single pane and in master mode

		if (findViewById(R.id.containerMaster) != null) {
			// we are multi pane
			btnDetailOK.setVisibility(View.GONE);
			btnFinish.setVisibility(View.VISIBLE);
			btnToday.setVisibility(View.VISIBLE);
			btnCancel.setVisibility(View.VISIBLE);
		} else {
			// we are single pane
			btnDetailOK.setVisibility(View.GONE);
			btnFinish.setVisibility(View.VISIBLE);
			btnToday.setVisibility(View.VISIBLE);
			btnCancel.setVisibility(View.VISIBLE);
		}

	}

	private void toMaster() {
		if (findViewById(R.id.containerMaster) != null) // No master,
			return;

		// we are on single pane layout (Main only)
		FragmentMaster fragmentMaster = (FragmentMaster) getSupportFragmentManager()
				.findFragmentByTag("Master");
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_bottom,
				R.anim.slide_out_top);
		transaction.replace(R.id.containerMain, fragmentMaster, "Master");
		transaction.addToBackStack(null);
		transaction.commit();
		hideDetailOKIconAndShowOthers();
	}

	private void toFragmentSelectDate() {
		// Swap fragment for FragmentSelectTime
		FragmentSelectDate myFragment = new FragmentSelectDate();
		myFragment.setArguments(getIntent().getExtras());
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right,
				R.anim.slide_out_left);
		// Check if the Detail container is loaded (only tablets), place there
		if (findViewById(R.id.containerDetail) != null) {
			transaction.replace(R.id.containerDetail, myFragment,
					"FragmentSelectDate");
		} else {
			transaction.replace(R.id.containerMain, myFragment,
					"FragmentSelectDate");
		}
		transaction.addToBackStack(null);
		transaction.commit();
		showDetailOKIconAndHideOthers();

	}

	private void toFragmentSelectTime() {
		// Swap fragment for FragmentSelectTime
		FragmentSelectTime myFragment = new FragmentSelectTime();
		myFragment.setArguments(getIntent().getExtras());
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right,
				R.anim.slide_out_left);
		// Check if the Detail container is loaded (only tablets), place there
		if (findViewById(R.id.containerDetail) != null) {
			transaction.replace(R.id.containerDetail, myFragment,
					"FragmentSelectTime");
		} else {
			transaction.replace(R.id.containerMain, myFragment,
					"FragmentSelectTime");
		}
		transaction.addToBackStack(null);
		transaction.commit();
		showDetailOKIconAndHideOthers();

	}

	private void toFragmentSelectOrganisation() {
		// Swap fragment forFragmentSelectOrganisation
		FragmentSelectOrganisation myFragment = new FragmentSelectOrganisation();
		myFragment.setArguments(getIntent().getExtras());
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right,
				R.anim.slide_out_left);
		// Check if the Detail container is loaded (only tablets), place there
		if (findViewById(R.id.containerDetail) != null) {
			transaction.replace(R.id.containerDetail, myFragment,
					"FragmentSelectOrganisation");
		} else {
			transaction.replace(R.id.containerMain, myFragment,
					"FragmentSelectOrganisation");
		}
		transaction.addToBackStack(null);
		transaction.commit();
		showDetailOKIconAndHideOthers();
	}

	private void toFragmentSelectActivity() {
		// Swap fragment for FragmentSelectActivity
		FragmentSelectActivity myFragment = new FragmentSelectActivity();
		myFragment.setArguments(getIntent().getExtras());
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right,
				R.anim.slide_out_left);
		// Check if the Detail container is loaded (only tablets), place there
		if (findViewById(R.id.containerDetail) != null) {
			transaction.replace(R.id.containerDetail, myFragment,
					"FragmentSelectActivity");
		} else {
			transaction.replace(R.id.containerMain, myFragment,
					"FragmentSelectActivity");
		}
		transaction.addToBackStack(null);
		transaction.commit();
		showDetailOKIconAndHideOthers();
	}

	private void toFragmentSelectAddress() {
		if (mApp.getSelection().getOrganizationID() == "") {
			Alert("Je kunt pas een adres kiezen nadat je een organisatie hebt gekozen.");
			return;
		}

		// Swap fragment for FragmentSelectActivity
		FragmentSelectAddress myFragment = new FragmentSelectAddress();
		myFragment.setArguments(getIntent().getExtras());
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right,
				R.anim.slide_out_left);
		// Check if the Detail container is loaded (only tablets), place there
		if (findViewById(R.id.containerDetail) != null) {
			transaction.replace(R.id.containerDetail, myFragment,
					"FragmentSelectAddress");
		} else {
			transaction.replace(R.id.containerMain, myFragment,
					"FragmentSelectAddress");
		}
		transaction.addToBackStack(null);
		transaction.commit();
		showDetailOKIconAndHideOthers();

	}

	private void toFragmentSelectProject() {
		if (mApp.getSelection().getOrganizationID() == "") {
			Alert("Je kunt pas een project kiezen nadat je een organisatie hebt gekozen.");
			return;
		}
		// Swap fragment for FragmentSelectActivity
		FragmentSelectProject myFragment = new FragmentSelectProject();
		myFragment.setArguments(getIntent().getExtras());
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right,
				R.anim.slide_out_left);

		// Check if the Detail container is loaded (only tablets), place there
		if (findViewById(R.id.containerDetail) != null) {
			transaction.replace(R.id.containerDetail, myFragment,
					"FragmentSelectProject");
		} else {
			transaction.replace(R.id.containerMain, myFragment,
					"FragmentSelectProject");
		}
		transaction.addToBackStack(null);
		transaction.commit();
		showDetailOKIconAndHideOthers();

	}

	private void toFragmentEnterDescription() {
		// Swap fragment for FragmentSelectActivity
		FragmentEnterDescription myFragment = new FragmentEnterDescription();
		myFragment.setArguments(getIntent().getExtras());
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right,
				R.anim.slide_out_left);
		// Check if the Detail container is loaded (only tablets), place there
		if (findViewById(R.id.containerDetail) != null) {
			transaction.replace(R.id.containerDetail, myFragment,
					"FragmentEnterDescription");
		} else {
			transaction.replace(R.id.containerMain, myFragment,
					"FragmentEnterDescription");
		}
		transaction.addToBackStack(null);
		transaction.commit();
		showDetailOKIconAndHideOthers();

	}

	private void toFragmentEnterRemarks() {
		// Swap fragment for FragmentSelectActivity
		FragmentEnterRemarks myFragment = new FragmentEnterRemarks();
		myFragment.setArguments(getIntent().getExtras());
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.setCustomAnimations(R.anim.slide_in_right,
				R.anim.slide_out_left);
		// Check if the Detail container is loaded (only tablets), place there
		if (findViewById(R.id.containerDetail) != null) {
			transaction.replace(R.id.containerDetail, myFragment,
					"FragmentEnterRemarks");
		} else {
			transaction.replace(R.id.containerMain, myFragment,
					"FragmentEnterRemarks");
		}
		transaction.addToBackStack(null);
		transaction.commit();
		showDetailOKIconAndHideOthers();

	}

	public void postIfValid() {
		if (mApp.getSelection().getActivityID() == "") {
			Alert("Je moet een activiteit kiezen.");
			toFragmentSelectActivity();
			return;
		}

		if ((mApp.getSelection().getHours() == null)
				|| (mApp.getSelection().getHours().floatValue() == 0)) {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						postNewTimeRowEntry();
						return;
					case DialogInterface.BUTTON_NEGATIVE:
						toFragmentSelectTime();
						dialog.dismiss();
						return;
					}
				}
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Je hebt geen uren ingevoerd, toch opslaan?")
					.setPositiveButton("Ja", dialogClickListener)
					.setNegativeButton("Nee", dialogClickListener).show();
			return;
		}
		if ((mApp.getSelection().getOrganizationID() == null)
				|| (mApp.getSelection().getOrganizationID() == "")) {
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						postNewTimeRowEntry();
						return;
					case DialogInterface.BUTTON_NEGATIVE:
						// No button clicked
						toFragmentSelectOrganisation();
						dialog.dismiss();
						return;
					}
				}
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(
					"Je hebt geen organisatie gekozen, toch opslaan?")
					.setPositiveButton("Ja", dialogClickListener)
					.setNegativeButton("Nee", dialogClickListener).show();
			return;
		}

		// valid
		postNewTimeRowEntry();
	}

	private void postNewTimeRowEntry() {
		HttpPostTask httpRequest = new HttpPostTask(ApplicationEx.apiURL, this,
				"newtimeentry");
		httpRequest.addSetting("cmd", "newtimeentry");
		httpRequest.addSetting("timerowid", mApp.getSelection().getTimeRowID());
		httpRequest.addSetting("date", mApp.getSelection().getDateString()
				.toString());
		httpRequest.addSetting("starttime", mApp.getSelection()
				.getStartTimeString());
		httpRequest.addSetting("endtime", mApp.getSelection()
				.getEndTimeString());
		httpRequest.addSetting("hours", mApp.getSelection().getHours()
				.toString());
		httpRequest.addSetting("description", mApp.getSelection()
				.getDescription());
		httpRequest.addSetting("remarks", mApp.getSelection().getRemarks());
		httpRequest.addSetting("activityid", mApp.getSelection()
				.getActivityID());
		httpRequest.addSetting("addressid", mApp.getSelection().getAddressID());
		httpRequest.addSetting("organizationid", mApp.getSelection()
				.getOrganizationID());
		httpRequest.addSetting("projectphaseid", mApp.getSelection()
				.getProjectPhaseID());
		httpRequest.addSetting("token", mApp.getToken());
		httpRequest.execute();

		if ((progressDialog == null) || !progressDialog.isShowing())
			progressDialog = ProgressDialog.show(this, "",
					"Opslaan tijdregel...", true);

	}

	@Override
	protected void onPause() {

		Log.i(ApplicationEx.TAG, "ActivityEditEntry onPause()");
		btnCancelShown = btnCancel.getVisibility() == View.VISIBLE;
		btnFinishShown = btnFinish.getVisibility() == View.VISIBLE;
		btnTodayShown = btnToday.getVisibility() == View.VISIBLE;
		btnDetailOKShown = btnDetailOK.getVisibility() == View.VISIBLE;

		if (progressDialog != null)
			progressDialog.dismiss();
		progressDialog = null;

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		btnCancel.setVisibility(btnCancelShown ? View.VISIBLE : View.GONE);
		btnFinish.setVisibility(btnFinishShown ? View.VISIBLE : View.GONE);
		btnToday.setVisibility(btnTodayShown ? View.VISIBLE : View.GONE);
		btnDetailOK.setVisibility(btnDetailOKShown ? View.VISIBLE : View.GONE);
	}

	@Override
	public void Toast(String text) {
		android.widget.Toast.makeText(this, text,
				android.widget.Toast.LENGTH_LONG).show();
	}

	@Override
	public void Alert(String text) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Uurapp");
		alertDialog.setMessage(text);
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// TODO Add your code for the button here.
			}
		});
		// Set the Icon for the Dialog
		alertDialog.setIcon(R.drawable.ic_launcher);
		alertDialog.show();
	}

	/*
	 * @Override public void EnableFinish(Boolean setEnabled) {
	 * btnFinish.setEnabled(setEnabled); }
	 * 
	 * @Override public void NewTimeEntrySaved() { setResult(RESULT_OK);
	 * finish(); }
	 */

	@Override
	public void SelectTime() {
		toFragmentSelectTime();
	}

	@Override
	public void SelectOrganisation() {
		toFragmentSelectOrganisation();
	}

	@Override
	public void SelectAddress() {
		toFragmentSelectAddress();
	}

	@Override
	public void SelectAcivity() {
		toFragmentSelectActivity();
	}

	@Override
	public void SelectProject() {
		// TODO Auto-generated method stub
		toFragmentSelectProject();
	}

	@Override
	public void SelectDescription() {
		toFragmentEnterDescription();
	}

	@Override
	public void SelectRemarks() {
		toFragmentEnterRemarks();
	}

	@Override
	public void SelectDate() {
		toFragmentSelectDate();

	}

	@Override
	public void DetailChanged(Boolean toMaster) {

		FragmentMaster fragmentMaster = (FragmentMaster) getSupportFragmentManager()
				.findFragmentByTag("Master");
		if (fragmentMaster != null)
			fragmentMaster.UpdateList();
		if (toMaster)
			toMaster();
	}

	@Override
	public void taskFinishedWithData(String data, HttpPostTask task) {

		if (this.isFinishing())
			return;

		if (progressDialog != null)
			progressDialog.dismiss();

		// Get JSON Data

		try {

			JSONObject cDictionary = new JSONObject(data);
			if (!cDictionary.isNull(ApplicationEx.dictionary_RESULT)) {
				String result = cDictionary
						.getString(ApplicationEx.dictionary_RESULT);
				if (result.equals(ApplicationEx.result_SUCCESS)) {
					// Set StartTime to current Endtime in Selection
					mApp.getSelection().NewTimeRow();

					// Update timerowsavings until review
					if (mApp.getTimerowsavings_until_review() > 1) {
						mApp.setTimerowsavings_until_review(mApp
								.getTimerowsavings_until_review() - 1);
					}

					finish();
				}
				if (result.equals(ApplicationEx.result_FAILED)) {
					Toast("Fout tijdens opslaan.");
				}
			} else {
				Toast("Ongeldige reactie van server.");
			}

		} catch (JSONException e) {
			e.printStackTrace();
			Toast("Ophalen informatie mislukt.");
		}

	}

	@Override
	public void taskDidFail(HttpPostTask task) {

		if (this.isFinishing())
			return;

		if (progressDialog != null)
			progressDialog.dismiss();
		Toast("Ophalen informatie mislukt.");

	}

}
