package com.skylion.quezzle.ui.auth;

import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.parse.ParseInstallation;
import com.primerworldapps.vitasolution.R;
import com.primerworldapps.vitasolution.database.user.UserExecutor;
import com.primerworldapps.vitasolution.entity.user.User;
import com.primerworldapps.vitasolution.entity.utils.RegistrationResult;
import com.primerworldapps.vitasolution.utils.PreferencesController;
import com.primerworldapps.vitasolution.view.utils.MessageDialogs;

public class UserLoginScreen extends SherlockActivity implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private SignInButton loginButton;

	public final int REQUEST_CODE_RESOLVE_ERR = 9000;
	public final int REQUEST_CODE_TOKEN_AUTH = 9001;

	private PlusClient plusClient;
	private User user;

	private int authResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);

		plusClient = new PlusClient.Builder(this, this, this).setVisibleActivities(
				"http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity").build();
		plusClient.connect();
		user = User.getInstance();

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
		if (requestCode == REQUEST_CODE_TOKEN_AUTH) {
			sendDataToServer();
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
			Toast.makeText(this,
					getString(R.string.google_connection_failed) + " Error code: " + result.getErrorCode(),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		if (!user.isLoggedIn() && plusClient != null) {
			user.setLoggedIn(true);
			user.setName(plusClient.getCurrentPerson().getDisplayName());
			user.setEmail(plusClient.getAccountName());
			
			// Save the current Installation to Parse.
			ParseInstallation.getCurrentInstallation().saveInBackground();

			sendDataToServer();
		}
	}

	private void sendDataToServer() {
		final ProgressDialog myProgressDialog = ProgressDialog.show(this, getString(R.string.connection),
				getString(R.string.connecting_text_login), true);
		new Thread() {
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						loadAccessToken();
					}
				});
				myProgressDialog.dismiss();
			}
		}.start();
	}

	private void loadAccessToken() {
		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String token = null;
				try {
					token = GoogleAuthUtil.getToken(UserLoginScreen.this, plusClient.getAccountName(), "oauth2:"
							+ Scopes.PLUS_LOGIN);
				} catch (IOException transientEx) {
					transientEx.printStackTrace();
					showErrorDialog();
				} catch (UserRecoverableAuthException e) {
					Toast.makeText(UserLoginScreen.this, getString(R.string.token_error), Toast.LENGTH_SHORT).show();
					Intent recover = e.getIntent();
					startActivityForResult(recover, REQUEST_CODE_TOKEN_AUTH);
					e.printStackTrace();
					showErrorDialog();
				} catch (GoogleAuthException authEx) {
					Toast.makeText(UserLoginScreen.this, getString(R.string.token_fatal_error), Toast.LENGTH_SHORT)
							.show();
					authEx.printStackTrace();
					showErrorDialog();
				}
				return token;
			}

			@Override
			protected void onPostExecute(String token) {
				user.setAccessToken(token);
				String deviceId;
				TelephonyManager mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
				if (mTelephony.getDeviceId() != null) {
					deviceId = mTelephony.getDeviceId();
				} else {
					deviceId = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
				}
				authResult = new UserExecutor().checkUser(user.getName(), user.getEmail(), user.getAccessToken(),
						deviceId);
				if (authResult == RegistrationResult.SUCCESS) {
					PreferencesController.getInstance().saveUserInfo();
					Toast.makeText(UserLoginScreen.this, getString(R.string.google_connected), Toast.LENGTH_SHORT)
							.show();
					finish();
				} else {
					showErrorDialog();
				}
			}

		};
		task.execute();
	}

	public void showErrorDialog() {
		MessageDialogs.showDialog(UserLoginScreen.this, getString(R.string.auth_error_dialog_title),
				getString(R.string.auth_error_dialog_text), null, null, null, null, null, null);
	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, getString(R.string.google_disconnected), Toast.LENGTH_SHORT).show();
		user.clear();
		PreferencesController.getInstance().clear();
	}

	@Override
	public void onBackPressed() {
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(startMain);
	}

}
