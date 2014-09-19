package nl.uurapp.uurapp;




import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;



public class ActivityRegisterSuccess extends Activity {

	//  Register Success 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_register_success);

		//OnClick for Register-button		
		Button btnRegister = (Button)findViewById(R.id.buttonToLogin);
		btnRegister.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ActivityRegisterSuccess.this, ActivityMain.class);
				startActivity(intent);
			}
		});
		
	}

}
