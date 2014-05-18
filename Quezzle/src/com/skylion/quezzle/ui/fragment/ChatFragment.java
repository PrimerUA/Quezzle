package com.skylion.quezzle.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.parse.ParseUser;
import com.skylion.quezzle.R;
import com.skylion.quezzle.contentprovider.QuezzleProviderContract;
import com.skylion.quezzle.datastorage.table.ChatPlaceTable;
import com.skylion.quezzle.datastorage.table.FullMessageTable;
import com.skylion.quezzle.notification.SendMessageNotification;
import com.skylion.quezzle.service.NetworkService;
import com.skylion.quezzle.ui.adapter.MessageListAdapter;
import com.skylion.quezzle.utility.Constants;

/**
 * Created with IntelliJ IDEA. User: Kvest Date: 24.03.14 Time: 23:10 To change
 * this template use File | Settings | File Templates.
 */
public class ChatFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, LocationListener {
	private static final String CHAT_MESSAGES_ORDER = FullMessageTable.UPDATED_AT_COLUMN + " DESC";
    private static final String[] CHAT_INFO_PROJECTION = new String[] {ChatPlaceTable.NAME_COLUMN, ChatPlaceTable.IS_SUBSCRIBED_COLUMN,
                                                                       ChatPlaceTable.CHAT_TYPE_COLUMN, ChatPlaceTable.LONGITUDE_COLUMN,
                                                                       ChatPlaceTable.LATITUDE_COLUMN, ChatPlaceTable.RADIUS_COLUMN };
	private static final int LOAD_CHAT_INFO_ID = 0;
	private static final int LOAD_MESSAGES_ID = 1;
	private static final String CHAT_KEY_ARGUMENT = "com.skylion.quezzle.ui.fragment.ChatFragment.CHAT_KEY";
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 30;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 5;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

	public static ChatFragment newInstance(String chatKey) {
		Bundle arguments = new Bundle();
		arguments.putString(CHAT_KEY_ARGUMENT, chatKey);

		ChatFragment result = new ChatFragment();
		result.setArguments(arguments);
		return result;
	}

    private boolean firstLoad = true;

	private String chatKey = null;
	private ImageView send;
	private EditText message;
	private ListView messageList;
	private ProgressBar progressBar;
	private MessageListAdapter messageListAdapter;

    private CheckBox subscribed;

    private int chatType;
    private double chatLongitude;
    private double chatLatitude;
    private int chatRadius;
    private Location currentUserLocation;
    float[] distanceResults = new float[1];

    private LocationClient locationClient;

    private NewMessageEventReceiver receiver = new NewMessageEventReceiver();
	private SendMessageNotificationReceiver sendMessageNotificationReceiver = new SendMessageNotificationReceiver();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		getActivity().getActionBar().setHomeButtonEnabled(true);
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
		getActivity().getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.blue));

		View rootView = inflater.inflate(R.layout.chat_fragment, container, false);

		send = (ImageView) rootView.findViewById(R.id.send);
		send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});
		progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar_chatFragment);
		message = (EditText) rootView.findViewById(R.id.message);

        subscribed = (CheckBox)rootView.findViewById(R.id.subscribe);
        subscribed.setEnabled(false);
        subscribed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSubscribed(subscribed.isChecked());
            }
        });

		messageList = (ListView) rootView.findViewById(R.id.messages_list);
		messageListAdapter = new MessageListAdapter(getActivity(), MessageListAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
		messageList.setAdapter(messageListAdapter);

        chatType = Constants.ChatType.USUAL;
        chatLongitude = 0d;
        chatLatitude = 0d;
        chatRadius = 0;
        currentUserLocation = null;

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();

		getActivity().registerReceiver(receiver, new IntentFilter(NetworkService.NEW_MESSAGE_ACTION));
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(sendMessageNotificationReceiver,
				new IntentFilter(SendMessageNotification.ACTION));
	}

	@Override
	public void onPause() {
		super.onPause();

        Activity activity = getActivity();
        activity.unregisterReceiver(receiver);
		LocalBroadcastManager.getInstance(activity).unregisterReceiver(sendMessageNotificationReceiver);

        //stop tracking location
        currentUserLocation = null;
        if (activity != null && activity.isFinishing()) {
            if (locationClient != null) {
                locationClient.disconnect();
                locationClient = null;
            }
        }
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(LOAD_CHAT_INFO_ID, null, this);
		getLoaderManager().initLoader(LOAD_MESSAGES_ID, null, this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.chat, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			NetworkService.reloadChat(getActivity(), getChatKey());
			return true;
		case android.R.id.home:
			getActivity().finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void sendMessage() {
		final String text = message.getText().toString().trim();
		if (!TextUtils.isEmpty(text)) {
            if (canSendMessage()) {
                ParseUser user = ParseUser.getCurrentUser();
                if (user != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    NetworkService.sendMessage(getActivity(), getChatKey(), text, user.getObjectId());

                    //subscribe automatically
                    if (!subscribed.isChecked()) {
                        setSubscribed(true);
                    }
                } else {
                    showToast(R.string.not_logged_id);
                }
                message.setText("");
            }
		} else {
            showToast(R.string.empty_message);
		}
	}

    private boolean canSendMessage() {
        if (chatType == Constants.ChatType.GEO) {
            if (currentUserLocation == null) {
                showToast(R.string.no_location_error);
                return false;
            } else {
                Location.distanceBetween(chatLatitude, chatLongitude, currentUserLocation.getLatitude(),
                                         currentUserLocation.getLongitude(), distanceResults);
                if (distanceResults[0] > chatRadius) {
                    showToast(R.string.not_in_zone_error);
                    return false;
                }
            }
        }

        return true;
    }

    private void showToast(int stringId) {
        Toast.makeText(getActivity(), stringId, Toast.LENGTH_SHORT).show();
    }

	private String getChatKey() {
		if (chatKey == null) {
			Bundle arguments = getArguments();
			chatKey = (arguments != null && arguments.containsKey(CHAT_KEY_ARGUMENT)) ? arguments.getString(CHAT_KEY_ARGUMENT) : "";
		}

		return chatKey;
	}

	private void setChatInfo(Cursor cursor) {
		if (cursor.moveToFirst()) {
			String chatName = cursor.getString(cursor.getColumnIndex(ChatPlaceTable.NAME_COLUMN));
			getActivity().getActionBar().setTitle(chatName);

            subscribed.setChecked(cursor.getInt(cursor.getColumnIndex(ChatPlaceTable.IS_SUBSCRIBED_COLUMN)) != 0);
            subscribed.setEnabled(true);

            chatType = cursor.getInt(cursor.getColumnIndex(ChatPlaceTable.CHAT_TYPE_COLUMN));
            chatLongitude = cursor.getDouble(cursor.getColumnIndex(ChatPlaceTable.LONGITUDE_COLUMN));
            chatLatitude = cursor.getDouble(cursor.getColumnIndex(ChatPlaceTable.LATITUDE_COLUMN));
            chatRadius = cursor.getInt(cursor.getColumnIndex(ChatPlaceTable.RADIUS_COLUMN));

            if (chatType == Constants.ChatType.GEO) {
                startTrackUserPosition();
            }
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case LOAD_CHAT_INFO_ID:
			Uri uri = Uri.withAppendedPath(QuezzleProviderContract.CHAT_PLACES_URI, getChatKey());
			return new CursorLoader(getActivity(), uri, CHAT_INFO_PROJECTION, null, null, null);
		case LOAD_MESSAGES_ID:
			return new CursorLoader(getActivity(), QuezzleProviderContract.getMessagesUri(getChatKey()), MessageListAdapter.PROJECTION,
					null, null, CHAT_MESSAGES_ORDER);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
		case LOAD_CHAT_INFO_ID :
			setChatInfo(cursor);
			break;
		case LOAD_MESSAGES_ID :
			if (firstLoad && cursor.getCount() == 0) {
				// try to load chat messages
				NetworkService.reloadChat(getActivity(), getChatKey());
			}
			firstLoad = false;

			messageListAdapter.swapCursor(cursor);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
        case LOAD_CHAT_INFO_ID :
            subscribed.setEnabled(false);
            break;
		case LOAD_MESSAGES_ID :
			messageListAdapter.swapCursor(null);
			break;
		}
	}

    private void startTrackUserPosition() {
        if (!servicesConnected()) {
            showToast(R.string.playservices_unavailable);
            return;
        }

        if (locationClient != null) {
            locationClient.disconnect();
            locationClient = null;
        }

        locationClient = new LocationClient(getActivity(), new GooglePlayServicesClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setInterval(UPDATE_INTERVAL);
                locationRequest.setFastestInterval(FASTEST_INTERVAL);
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                locationClient.requestLocationUpdates(locationRequest, ChatFragment.this);
            }

            @Override
            public void onDisconnected() {
                currentUserLocation = null;
            }
        }, new GooglePlayServicesClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                currentUserLocation = null;

                showToast(R.string.location_connecting_error);
            }
        });
        locationClient.connect();
    }

    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
        // If Google Play services is available
        return  (ConnectionResult.SUCCESS == resultCode);
    }


    private void setSubscribed(boolean isSubscribed) {
        ContentValues values = new ContentValues(2);
        values.put(ChatPlaceTable.IS_SUBSCRIBED_COLUMN, isSubscribed ? 1 : 0);
        values.put(ChatPlaceTable.SYNC_STATUS_COLUMN, Constants.SyncStatus.NEED_UPLOAD);
        Activity activity = getActivity();
        if (activity != null) {
            activity.getContentResolver().update(Uri.withAppendedPath(QuezzleProviderContract.CHAT_PLACES_URI, getChatKey()),
                                                 values, null, null);
            NetworkService.uploadChatSubscriptions(activity);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentUserLocation = location;
    }

    private class NewMessageEventReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (getChatKey().equals(intent.getStringExtra(NetworkService.CHAT_KEY_EXTRA))) {
				setResultCode(Activity.RESULT_OK);
				abortBroadcast();
				progressBar.setVisibility(View.GONE);
			}
		}
	}

	private class SendMessageNotificationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!SendMessageNotification.isSuccessful(intent)) {
				Toast.makeText(getActivity(), getString(R.string.error_sending_message, SendMessageNotification.getErrorMessage(intent)),
						Toast.LENGTH_LONG).show();
			}
		}
	}
}
