package nl.uurapp.uurapp;

import org.json.JSONException;
import org.json.JSONObject;

import nl.uurapp.uurapp.util.HttpPostTask;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import nl.uurapp.uurapp.util.HttpTaskListener;


public class ActivityRegister extends Activity implements HttpTaskListener {

	//  Register new account 
	private ProgressDialog progressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register);
		
		//Set Register Hyperlink
		TextView tvConditions = (TextView)findViewById(R.id.textViewConditions);
		String linkText = "<a href='http://www.uurapp.nl/over-uurapp/leveringsvoorwaarden.html'>"
				+ "Lees voorwaarden</a>";				
		tvConditions.setText(Html.fromHtml(linkText));
		tvConditions.setMovementMethod(LinkMovementMethod.getInstance());
		
		//OnClick for Register-button		
		Button btnRegister = (Button)findViewById(R.id.buttonRegister);		
		btnRegister.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Save values
				String organization = getEditTextText(R.id.editTextOrganizationName);
				String firstname = getEditTextText(R.id.editTextFirstname);
				String lastname = getEditTextText(R.id.editTextLastname);
				String loginName = getEditTextText(R.id.editTextLoginName);
				String password1 = getEditTextText(R.id.editPassword1);
				String password2 = getEditTextText(R.id.editPassword2);
				String email = getEditTextText(R.id.editTextEmail);
				String address = getEditTextText(R.id.editTextAddress);
				String postcode = getEditTextText(R.id.editTextPostcode);
				String city = getEditTextText(R.id.editTextCity);
				String country = getEditTextText(R.id.editTextCountry);
				CheckBox cbConditions = (CheckBox)findViewById(R.id.checkBoxAcceptConditions);
				Boolean acceptConditions = cbConditions.isChecked();
				
				if ((organization.length() == 0) ||
					(firstname.length() == 0) ||
					(lastname.length() == 0) ||
					(loginName.length() == 0) ||
					(password1.length() == 0) ||
					(password2.length() == 0) ||
					(email.length() == 0) ||
					(address.length() == 0) ||
					(postcode.length() == 0) ||
					(city.length() == 0) ||
					(country.length() == 0))
				{
					Toast.makeText(ActivityRegister.this, "Niet alle velden zijn ingevuld.", Toast.LENGTH_LONG).show();
					return;
				}
				if (!isValidEmail(email))
				{
					Toast.makeText(ActivityRegister.this, "Vul een geldig email adres in. Er wordt een email gestuurd ter verificatie.", Toast.LENGTH_LONG).show();
					return;
				}
				if (!acceptConditions)
				{
					Toast.makeText(ActivityRegister.this, "Je dient de voorwaarden te accepteren.", Toast.LENGTH_LONG).show();
					return;
				}
				if (password1.compareTo(password2)!= 0)
				{
					Toast.makeText(ActivityRegister.this, "De twee wachtwoorden zijn niet gelijk.", Toast.LENGTH_LONG).show();
					return;
				}
				
				// request new account					
				HttpPostTask httpRequest = new HttpPostTask(ApplicationEx.apiURL, ActivityRegister.this, "register");					
				httpRequest.addSetting("cmd", "register");
				httpRequest.addSetting("organization", organization);
				httpRequest.addSetting("firstname", firstname);
				httpRequest.addSetting("lastname", lastname);
				httpRequest.addSetting("username", loginName);
				httpRequest.addSetting("password", password1);
				httpRequest.addSetting("email", email);
				httpRequest.addSetting("address", address);
				httpRequest.addSetting("postcode", postcode);
				httpRequest.addSetting("city", city);
				httpRequest.addSetting("country", country);	
				httpRequest.addSetting("apiversion", ApplicationEx.APIVersion);
				httpRequest.addSetting("referral", ApplicationEx.Referral);
				httpRequest.addSetting("device", getDeviceName());
				
				httpRequest.execute();
				if ((progressDialog == null) || !progressDialog.isShowing())
						progressDialog = ProgressDialog.show(ActivityRegister.this, "", 
		                "Account aanmaken...", true);
				
				Button btnRegister = (Button)findViewById(R.id.buttonRegister);
				btnRegister.setEnabled(false);
			}
		});		


	}
	
	public String getEditTextText(int editTextViewById)
	{
		EditText etID = (EditText)findViewById(editTextViewById);
		String ID = etID.getText().toString();
		return ID;
	}
	
	public final static boolean isValidEmail(CharSequence target) {
	    if (target == null) {
	        return false;
	    } else {
	        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
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

			if (progressDialog != null) progressDialog.dismiss();
			
			//We expect "result" = "success" to be returned on success or a "result" == "failure"		
			try {
				if (progressDialog != null) progressDialog.dismiss();
				JSONObject cDictionary = new JSONObject(data);				

						
				if (!cDictionary.isNull(ApplicationEx.dictionary_RESULT))				
				{
					String result = cDictionary.getString(ApplicationEx.dictionary_RESULT);
					if (result.equals(ApplicationEx.result_SUCCESS))
					{
						Intent intent = new Intent(ActivityRegister.this, ActivityRegisterSuccess.class);
						startActivity(intent);
						return;						
					}
					if (result.equals(ApplicationEx.result_FAILED))
					{
						Toast.makeText(this, "Registratie is mislukt. Controleer de gegevens en probeer het nogmaals of registreer via de website www.uurapp.nl", 
								Toast.LENGTH_LONG).show();
					}
					if (result.equals(ApplicationEx.result_UNSUPPORTED_API_VERSION))
					{
						Toast.makeText(this, "De versie van deze app wordt niet meer ondersteund. Update hem aub.", 
								Toast.LENGTH_LONG).show();
					}
					if (result.equals(ApplicationEx.result_PASSWORD_NOT_COMPLEX))
					{
						Toast.makeText(this, "Wachtwoord moet minimaal 6 (waarvan 1 speciaal) tekens bevatten.", 
								Toast.LENGTH_LONG).show();
					}
					if (result.equals(ApplicationEx.result_USERNAME_NOT_AVAILABLE))
					{
						Toast.makeText(this, "Deze inlognaam is niet meer beschikbaar. Kies een andere.", 
								Toast.LENGTH_LONG).show();
					}
					if (result.equals(ApplicationEx.result_INVALID_EMAIL_ADDRESS))
					{
						Toast.makeText(this, "Vul een geldig email adres in. Er wordt een email gestuurd ter verificatie.", 
								Toast.LENGTH_LONG).show();
					}
				}
				else 
				{					
					Toast.makeText(this, "Ongeldige reactie van server.", Toast.LENGTH_LONG).show();
				}
			} 
			catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Toast.makeText(this, "Registratie is mislukt. Controleer de gegevens en probeer het nogmaals of registreer via de website www.uurapp.nl", Toast.LENGTH_LONG).show();
			}
			finally
			{
				Button btnRegister = (Button)findViewById(R.id.buttonRegister);
				btnRegister.setEnabled(true);
			}

		}

		// HTTP POST Returned unsuccessful
		
		@Override
		public void taskDidFail(HttpPostTask task) {
			if (progressDialog != null) progressDialog.dismiss();
			Toast.makeText(this, "Verbinden met Uurapp server mislukt. Controleer je internet-verbinding.", Toast.LENGTH_LONG).show();
			Button btnRegister = (Button)findViewById(R.id.buttonRegister);
			btnRegister.setEnabled(true);
		}
	
	}
