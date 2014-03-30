package com.skylion.quezzle.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.skylion.quezzle.R;

public class UserLoginActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private SignInButton loginButton;

	public final int REQUEST_CODE_RESOLVE_ERR = 9000;

	private PlusClient plusClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);

		plusClient = new PlusClient.Builder(this, this, this).setVisibleActivities(
				"http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity").build();
		plusClient.connect();

		screenInit();
	}

	private void screenInit() {
		loginButton = (SignInButton) findViewById(R.id.LoginScreen_googleSignInButton);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startGooglePlus();
			}
		});
	}

	protected void startGooglePlus() {
		if (!plusClient.isConnected()) {
			plusClient.connect();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		plusClient.disconnect();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_RESOLVE_ERR && resultCode == RESULT_OK) {
			plusClient.connect();
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (result.hasResolution()) {
			try {
				result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
			} catch (IntentSender.SendIntentException e) {
				plusClient.connect();
			}
		} else {
			Toast.makeText(this, "G+ error code: " + result.getErrorCode(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		if (plusClient != null) {
			ParseUser user = new ParseUser();
			user.setUsername(plusClient.getCurrentPerson().getDisplayName());
			user.setPassword("my pass");
			user.setEmail(plusClient.getAccountName());

			user.signUpInBackground(new SignUpCallback() {
				public void done(ParseException e) {
					if (e == null) {
						finish();
					} else {
						ParseUser.logInInBackground(plusClient.getCurrentPerson().getDisplayName(), "my pass",
								new LogInCallback() {
									public void done(ParseUser user, ParseException e) {
										if (user != null) {
											finish();
										} else {
											Toast.makeText(UserLoginActivity.this, "Error code: " + e.getMessage(),
													Toast.LENGTH_SHORT).show();
										}
									}
								});
					}
				}
			});
		}
	}

	@Override
	public void onDisconnected() {
		// Toast.makeText(this, getString(R.string.google_disconnected),
		// Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onBackPressed() {
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}

}
