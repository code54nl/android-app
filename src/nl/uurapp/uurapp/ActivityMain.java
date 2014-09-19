package nl.uurapp.uurapp;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import nl.uurapp.uurapp.util.HttpPostTask;
import nl.uurapp.uurapp.util.HttpTaskListener;

public class ActivityMain extends Activity implements HttpTaskListener {

	private ProgressDialog progressDialog = null; // for "in progress spinner"
	private ApplicationEx mApp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mApp = ApplicationEx.getContext();

		// Set Uurapp Hyperlink
		TextView tvRegister = (TextView) findViewById(R.id.textViewLinkToWebsite);
		String linkText = "<a href='http://www.uurapp.nl'>Gebruik deze app samen met de web-applicatie</a>";
		tvRegister.setText(Html.fromHtml(linkText));
		tvRegister.setMovementMethod(LinkMovementMethod.getInstance());

		// OnClick for Login-button
		Button btnLogin = (Button) findViewById(R.id.buttonLogin);
		btnLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// Get username and password
				EditText etUsername = (EditText) findViewById(R.id.editTextUsername);
				EditText etPassword = (EditText) findViewById(R.id.editPassword);

				String tryUsername = etUsername.getText().toString();
				String tryPassword = etPassword.getText().toString();
				LoginPassword(tryUsername, tryPassword);
			}
		});

		// OnClick for Register-button
		Button btnRegister = (Button) findViewById(R.id.buttonToRegister);
		btnRegister.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ActivityMain.this,
						ActivityRegister.class);
				startActivity(intent);
			}
		});
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		if (progressDialog != null)
			progressDialog.dismiss();
		progressDialog = null;
	}

	// On App resume try to auto-logon
	protected void onResume() {
		super.onResume();
		// Get 'globals'

		mApp.Reset(); // remove old credentials, etc

		// Load saved username (if any) and set in globals and username textbox
		SharedPreferences preferences = getSharedPreferences(
				ApplicationEx.PREF_FILE_NAME, MODE_PRIVATE);

		mApp.setUsername(preferences.getString("username", ""));
		mApp.setToken(preferences.getString("token", ""));
		mApp.setAutologon(preferences.getBoolean("autologon", true));
		mApp.setTimerowsavings_until_review(preferences.getLong(
				"timerowsavings_until_review",
				ApplicationEx.savedTimeRowsBeforeReview));

		EditText etUsername = (EditText) findViewById(R.id.editTextUsername);
		etUsername.setText(mApp.getUsername());

		Checkable chkAutologon = (Checkable) findViewById(R.id.checkBoxAutoLogon);
		chkAutologon.setChecked(mApp.isAutologon());

		Button btnRegister = (Button) findViewById(R.id.buttonToRegister);
		if (mApp.getUsername().length() > 0)
			btnRegister.setVisibility(View.GONE);

		if (mApp.isAutologon() && (!mApp.getToken().equals(""))) {
			EditText etPassword = (EditText) findViewById(R.id.editPassword);
			etPassword.setText("******"); // for user experience, as if we
											// stored the password
			LoginToken(mApp.getToken());
		}
	}

	private void LoginToken(String tryToken) {
		mApp.Reset(); // remove old credentials, etc

		mApp.setToken(tryToken);
		// disable login button while http request runs
		Button btnLogin = (Button) findViewById(R.id.buttonLogin);
		btnLogin.setEnabled(false);

		// request login
		HttpPostTask httpRequest = new HttpPostTask(ApplicationEx.apiURL,
				ActivityMain.this, "testtoken");
		httpRequest.addSetting("cmd", "testtoken");
		httpRequest.addSetting("token", mApp.getToken());
		httpRequest.addSetting("apiversion", ApplicationEx.APIVersion);
		httpRequest.execute();
		if ((progressDialog == null) || !progressDialog.isShowing())
			progressDialog = ProgressDialog.show(ActivityMain.this, "",
					"Opnieuw verbinden...", true);
	}

	private void LoginPassword(String tryUsername, String tryPassword) {
		mApp.Reset(); // remove old credentials, etc

		if ((tryUsername.length() > 0) && (tryPassword.toString().length() > 0)) {
			// disable login button while http request runs
			Button btnLogin = (Button) findViewById(R.id.buttonLogin);
			btnLogin.setEnabled(false);

			// request login
			HttpPostTask httpRequest = new HttpPostTask(ApplicationEx.apiURL,
					ActivityMain.this, "login");
			httpRequest.addSetting("cmd", "login");
			httpRequest.addSetting("username", tryUsername);
			httpRequest.addSetting("password", tryPassword);
			httpRequest.addSetting("apiversion", ApplicationEx.APIVersion);
			httpRequest.addSetting("device", getDeviceName());
			httpRequest.execute();
			if ((progressDialog == null) || !progressDialog.isShowing())
				progressDialog = ProgressDialog.show(ActivityMain.this, "",
						"Bezig met aanmelden...", true);
		} else {
			Toast.makeText(ActivityMain.this, "Voor naam en wachtwoord in.",
					Toast.LENGTH_LONG).show();
		}
	}

	// Get device name
	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	// HTTP POST Returned successful

	@Override
	public void taskFinishedWithData(String data, HttpPostTask task) {

		if (this.isFinishing())
			return;

		if (progressDialog != null)
			progressDialog.dismiss();

		// We expect a "token" to be returned on success or a "result" on
		// failure
		try {
			if (progressDialog != null)
				progressDialog.dismiss();
			JSONObject cDictionary = new JSONObject(data);

			if (task.getTaskDescription().equals("login")) {

				if (!cDictionary.isNull("token")) {

					String token = cDictionary.getString("token");
					EditText etUsername = (EditText) findViewById(R.id.editTextUsername);
					String username = etUsername.getText().toString();
					;

					Checkable chkAutologon = (Checkable) findViewById(R.id.checkBoxAutoLogon);

					// since login is accepted, save username and token we tried
					mApp.setAutologon(chkAutologon.isChecked());
					mApp.setUsername(username);
					mApp.setToken(token);

					// save username, not token or password
					SharedPreferences preferences = getSharedPreferences(
							ApplicationEx.PREF_FILE_NAME, MODE_PRIVATE);
					SharedPreferences.Editor editor = preferences.edit();
					editor.putString("username", mApp.getUsername());
					editor.putString("token", mApp.getToken());
					editor.putBoolean("autologon", mApp.isAutologon());
					editor.commit();
					Intent intent = new Intent(ActivityMain.this,
							ActivityTimeList.class);
					startActivity(intent);
					return;
				}
			}

			if (task.getTaskDescription().equals("testtoken")) {
				if (!cDictionary.isNull(ApplicationEx.dictionary_RESULT)) {
					String result = cDictionary
							.getString(ApplicationEx.dictionary_RESULT);
					if (result.equals(ApplicationEx.result_SUCCESS)) {
						Intent intent = new Intent(ActivityMain.this,
								ActivityTimeList.class);
						startActivity(intent);
						return;
					}
				}

			}

			// if not 'result_success' on 'testtoken'
			// and not a 'token' on 'newtimeentry', what is the reason?

			if (!cDictionary.isNull(ApplicationEx.dictionary_RESULT)) {
				String result = cDictionary
						.getString(ApplicationEx.dictionary_RESULT);
				if (result.equals(ApplicationEx.result_UNSUPPORTED_API_VERSION)) {
					Toast.makeText(
							this,
							"De versie van deze app wordt niet meer ondersteund. Update hem aub.",
							Toast.LENGTH_LONG).show();
				}
				if (result.equals(ApplicationEx.result_LOGIN_PROCESS_FAILED)) {
					Toast.makeText(this,
							"Fout bij het openen van de administratie.",
							Toast.LENGTH_LONG).show();
				}
				if (result.equals(ApplicationEx.result_LOGIN_NOT_APPROVED)) {
					Toast.makeText(
							this,
							"Het is niet toegestaan met dit account in te loggen.",
							Toast.LENGTH_LONG).show();
				}
				if (result.equals(ApplicationEx.result_NOT_LOGGED_ON)) {
					Toast.makeText(this, "Je bent niet in ingelogd.",
							Toast.LENGTH_LONG).show();
				}
				if (result.equals(ApplicationEx.result_LOGIN_LOCKED_OUT)) {
					Toast.makeText(this,
							"Dit account is tijdelijk geblokkeerd.",
							Toast.LENGTH_LONG).show();
				}
				if (result.equals(ApplicationEx.result_LOGIN_INVALID)) {
					Toast.makeText(this, "Naam en/of wachtwoord is onjuist.",
							Toast.LENGTH_LONG).show();
				}
				if (result.equals(ApplicationEx.result_LOGIN_TOKEN_EXPIRED)) {
					EditText etPassword = (EditText) findViewById(R.id.editPassword);
					etPassword.setText(""); // for user experience, as if
											// password is not stored anymore
					Toast.makeText(this, "Opgeslagen wachtwoord is verlopen.",
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(this, "Ongeldige reactie van server.",
						Toast.LENGTH_LONG).show();
			}

			// if we end up here we were not able to login.
			// disable autologon
			mApp.setAutologon(false);
			SharedPreferences preferences = getSharedPreferences(
					ApplicationEx.PREF_FILE_NAME, MODE_PRIVATE);
			SharedPreferences.Editor editor = preferences.edit();
			editor.putBoolean("autologon", mApp.isAutologon());
			editor.commit();
			Checkable chkAutologon = (Checkable) findViewById(R.id.checkBoxAutoLogon);
			chkAutologon.setChecked(mApp.isAutologon());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, "Inloggen mislukt.", Toast.LENGTH_LONG).show();
		} finally {
			Button btnLogin = (Button) findViewById(R.id.buttonLogin);
			btnLogin.setEnabled(true);
		}

	}

	// HTTP POST Returned unsuccessful

	@Override
	public void taskDidFail(HttpPostTask task) {

		if (this.isFinishing())
			return;

		if (progressDialog != null)
			progressDialog.dismiss();
		Toast.makeText(
				this,
				"Verbinden met Uurapp server mislukt. Controleer je internet-verbinding.",
				Toast.LENGTH_LONG).show();
		Button btnLogin = (Button) findViewById(R.id.buttonLogin);
		btnLogin.setEnabled(true);
	}

	// Implement Options-menu (for 'About')

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
			Intent intent = new Intent(ActivityMain.this, ActivityAbout.class);
			startActivity(intent);
			return true;

		}
		return super.onOptionsItemSelected(item);
	}

}
