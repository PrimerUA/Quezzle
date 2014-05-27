package com.skylion.quezzle.ui.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
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
import com.skylion.quezzle.datamodel.QuezzleUserMetadata;
import com.skylion.quezzle.service.NetworkService;

public class UserLoginActivity extends QuezzleBaseActivity implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
    private static final String GPLUS_IMAGE_SIZE_PARAM = "sz";
    private static final String DEFAULT_PASSWORD = "my pass";

    public static void start(Context context) {
        Intent intent = new Intent(context, UserLoginActivity.class);

        context.startActivity(intent);
    }

	private SignInButton loginButton;

	public final int REQUEST_CODE_RESOLVE_ERR = 9000;

	private PlusClient plusClient;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen);
		progressDialog = new ProgressDialog(this);
		getActionBar().setTitle(R.string.title_activity_auth);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.blue));

		plusClient = new PlusClient.Builder(this, this, this).setVisibleActivities("http://schemas.google.com/AddActivity",
				"http://schemas.google.com/BuyActivity").build();
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
			progressDialog = ProgressDialog.show(this, getString(R.string.connecting), getString(R.string.loading_auth));
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
			progressDialog.dismiss();
			Toast.makeText(this, "G+ error code: " + result.getErrorCode(), Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		if (plusClient != null) {
			ParseUser user = new ParseUser();
			user.setUsername(plusClient.getCurrentPerson().getDisplayName());
			user.setPassword(DEFAULT_PASSWORD);
			user.setEmail(plusClient.getAccountName());
            user.put(QuezzleUserMetadata.IS_ADMIN, false);
            user.put(QuezzleUserMetadata.GPLUS_LINK, plusClient.getCurrentPerson().getUrl());
            if (plusClient.getCurrentPerson().hasImage()) {
                user.put(QuezzleUserMetadata.AVATAR_URL, processAvatarImageLink(plusClient.getCurrentPerson().getImage().getUrl()));
            }


			user.signUpInBackground(new SignUpCallback() {
				public void done(ParseException e) {
					if (e == null) {
						progressDialog.dismiss();

                        //update chat list
                        NetworkService.reloadChatList(UserLoginActivity.this);

						finish();
					} else {
						ParseUser.logInInBackground(plusClient.getCurrentPerson().getDisplayName(), DEFAULT_PASSWORD, new LogInCallback() {
                            public void done(ParseUser user, ParseException e) {
                                if (user != null) {
                                    //update user info
                                    user.put(QuezzleUserMetadata.GPLUS_LINK, plusClient.getCurrentPerson().getUrl());
                                    user.saveInBackground();

                                    progressDialog.dismiss();
                                    Toast.makeText(UserLoginActivity.this, getString(R.string.welcome), Toast.LENGTH_SHORT).show();

                                    //update chat list
                                    NetworkService.reloadChatList(UserLoginActivity.this);

                                    finish();
                                } else {
                                    progressDialog.dismiss();
                                    Toast.makeText(UserLoginActivity.this, "Error code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    private String processAvatarImageLink(String url) {
        Uri uri = Uri.parse(url);
        int avatarSize = StrictMath.round(getResources().getDimension(R.dimen.upload_avatar_size));
        return uri.buildUpon().clearQuery()
               .appendQueryParameter(GPLUS_IMAGE_SIZE_PARAM, Integer.toString(avatarSize))
               .build().toString();
    }
}
