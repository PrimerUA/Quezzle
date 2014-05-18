package com.skylion.quezzle.ui.activity;

import android.app.ProgressDialog;
import android.content.*;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;
import com.skylion.quezzle.R;
import com.skylion.quezzle.notification.CreateChatNotification;
import com.skylion.quezzle.service.NetworkService;

/**
 * Created with IntelliJ IDEA. User: Kvest Date: 19.03.14 Time: 21:28 To change
 * this template use File | Settings | File Templates.
 */
public class NewChatActivity extends QuezzleBaseActivity {
    private static final int DEFAULT_RADIUS = 1000;
    private static final double MIN_LATITUDE = -90d;
    private static final double MAX_LATITUDE = 90d;
    private static final double MIN_LONGITUDE = -180d;
    private static final double MAX_LONGITUDE = 180d;
    private static final int MIN_RADIUS = 100;
    private static final int MAX_RADIUS = 10000;

    public static void start(Context context) {
        Intent intent = new Intent(context, NewChatActivity.class);
        context.startActivity(intent);
    }

	private EditText nameEdit;
	private EditText descEdit;
    private RadioGroup chatType;
    private LinearLayout geoChatParams;
    private EditText longitude;
    private EditText latitude;
    private EditText radius;
    private Button createButton;
	private ProgressDialog progressDialog;

	private CreateChatNotificationReceiver receiver = new CreateChatNotificationReceiver();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_chat);

		getActionBar().setTitle(R.string.title_activity_new_chat);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.blue));

		nameEdit = (EditText) findViewById(R.id.nameEdit);
		descEdit = (EditText) findViewById(R.id.descriptionEdit);
        longitude = (EditText) findViewById(R.id.longitude);
        latitude = (EditText) findViewById(R.id.latitude);
        radius = (EditText) findViewById(R.id.radius);
        geoChatParams = (LinearLayout) findViewById(R.id.geo_chat_params);
        chatType = (RadioGroup) findViewById(R.id.chat_type);
        chatType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.type_usual) {
                    geoChatParams.setVisibility(View.GONE);
                } else if (checkedId == R.id.type_geo) {
                    geoChatParams.setVisibility(View.VISIBLE);
                }
            }
        });

        setDefaultValues();

		createButton = (Button) findViewById(R.id.createLocalChatButton);
		createButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                createChat();
			}
		});
	}

    private void setDefaultValues() {
        radius.setText(Integer.toString(DEFAULT_RADIUS));

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        String provider = lm.getBestProvider(criteria, true);
        Location lastLocation = provider != null ? lm.getLastKnownLocation(provider) : null;

        longitude.setText(Double.toString(lastLocation != null ? lastLocation.getLongitude() : 0.0d));
        latitude.setText(Double.toString(lastLocation != null ? lastLocation.getLatitude() : 0.0d));
    }

	@Override
	public void onResume() {
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(CreateChatNotification.ACTION));
	}

	@Override
	public void onPause() {
		super.onPause();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}

    private boolean isDataValid() {
        boolean result = true;
        if (TextUtils.isEmpty(nameEdit.getText())) {
            nameEdit.setError(getString(R.string.empty_field));
            result = false;
        }
        if (chatType.getCheckedRadioButtonId() == R.id.type_geo) {
            if (TextUtils.isEmpty(longitude.getText())) {
                longitude.setError(getString(R.string.empty_field));
                result = false;
            } else {
                try {
                    double longitudeValue = Double.parseDouble(longitude.getText().toString());
                    if (longitudeValue <= MIN_LONGITUDE || longitudeValue >= MAX_LONGITUDE) {
                        longitude.setError("(" + MIN_LONGITUDE + ".." + MAX_LONGITUDE + ")");
                        result = false;
                    }
                } catch (NumberFormatException nfe) {
                    longitude.setError(getString(R.string.empty_field));
                    result = false;
                }

            }
            if (TextUtils.isEmpty(latitude.getText())) {
                latitude.setError(getString(R.string.empty_field));
                result = false;
            } else {
                try {
                    double latitudeValue = Double.parseDouble(latitude.getText().toString());
                    if (latitudeValue <= MIN_LATITUDE || latitudeValue >= MAX_LATITUDE) {
                        latitude.setError("(" + MIN_LATITUDE + ".." + MAX_LATITUDE + ")");
                        result = false;
                    }
                } catch (NumberFormatException nfe) {
                    latitude.setError(getString(R.string.empty_field));
                    result = false;
                }
            }
            if (TextUtils.isEmpty(radius.getText())) {
                radius.setError(getString(R.string.empty_field));
                result = false;
            } else {
                int radiusValue = Integer.parseInt(radius.getText().toString());
                if (radiusValue < MIN_RADIUS || radiusValue > MAX_RADIUS) {
                    radius.setError("[" + MIN_RADIUS + ".." + MAX_RADIUS + "]");
                    result = false;
                }
            }
        }

        return result;
    }

	private void createChat() {
		if (!isDataValid()) {
			return;
		}

        progressDialog = ProgressDialog.show(this, getString(R.string.connecting), getString(R.string.loading_new_chat));

        if (chatType.getCheckedRadioButtonId() == R.id.type_geo) {
            double latitudeValue = Double.parseDouble(latitude.getText().toString());
            double longitudeValue = Double.parseDouble(longitude.getText().toString());
            int radiusValue = Integer.parseInt(radius.getText().toString());
            NetworkService.createGeoChat(this, nameEdit.getText().toString(), descEdit.getText().toString(),
                                         latitudeValue, longitudeValue, radiusValue);
        } else {
            NetworkService.createUsualChat(this, nameEdit.getText().toString(), descEdit.getText().toString());
        }
	}

	private class CreateChatNotificationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			progressDialog.dismiss();
			if (CreateChatNotification.isSuccessful(intent)) {
				Toast.makeText(NewChatActivity.this, getString(R.string.new_chat_created), Toast.LENGTH_SHORT).show();
				finish();
			} else {
				Toast.makeText(NewChatActivity.this,
						getString(R.string.error_creating_chat, CreateChatNotification.getErrorMessage(intent)), Toast.LENGTH_LONG).show();
			}
		}
	}
}
