package nl.uurapp.uurapp;




import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class ActivityAbout extends Activity {

	//  About Uurapp 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_info);
		
		TextView tvRegister = (TextView)findViewById(R.id.textViewRegister);
		String linkText = "<a href='http://www.uurapp.nl'>Meer informatie</a>";
		tvRegister.setText(Html.fromHtml(linkText));
	    tvRegister.setMovementMethod(LinkMovementMethod.getInstance());
	    
	    TextView tvAbout = (TextView)findViewById(R.id.textViewAbout);
	    String aboutText = getResources().getString(R.string.text_about);
	    tvAbout.setText(Html.fromHtml(aboutText));
	    
	    //OnClick for To Play Store button		
  		Button btnToPlayStore = (Button)findViewById(R.id.buttonToPlayStore);
  		btnToPlayStore.setOnClickListener(new View.OnClickListener() {
  			
  			@Override
  			public void onClick(View v) {
  				Uri marketUri = Uri.parse("market://details?id=nl.uurapp.uurapp&reviewId=0");
				Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
				startActivity(marketIntent);
  			}
  		});
	  		
	    try {
	    TextView tvVersion = (TextView)findViewById(R.id.textViewVersion);
	    String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
	    tvVersion.setText("Versie " + versionName);
	    }
	    catch (Exception e)
	    {
	    	TextView tvVersion = (TextView)findViewById(R.id.textViewVersion);
	    	tvVersion.setText("Versie -?-");
	    }
		
	}
	

}
