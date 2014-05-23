package com.skylion.quezzle.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.parse.ParseUser;
import com.skylion.quezzle.R;
import com.skylion.quezzle.datamodel.QuezzleUserMetadata;
import com.skylion.quezzle.notification.UpdateProfileNotification;
import com.skylion.quezzle.service.NetworkService;
import com.skylion.quezzle.utility.Utils;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 15.04.14
 * Time: 20:57
 * To change this template use File | Settings | File Templates.
 */
public class UserProfileFragment extends Fragment {
    private static final int PICK_IMAGE = 1;

    private DisplayImageOptions options;
    private ImageView avatarView;
    //private TextView nameView;
    private Button saveButton;

    private String avatarFilePath = "";
    private ProgressDialog progressDialog;

    UpdateProfileNotificationReceiver receiver = new UpdateProfileNotificationReceiver();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_profile_fragment, container, false);
        
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.default_avatar).showImageForEmptyUri(R.drawable.default_avatar)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).resetViewBeforeLoading(true).cacheInMemory(true).cacheOnDisc(true)
                .displayer(new RoundedBitmapDisplayer(Integer.MAX_VALUE)).build();

        avatarView = (ImageView) rootView.findViewById(R.id.avatarView_profileScreen);
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAvatar();
            }
        });
        //nameView = (TextView) rootView.findViewById(R.id.nameText_profileScreen);
        saveButton = (Button) rootView.findViewById(R.id.save_profileScreen);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        ParseUser user = ParseUser.getCurrentUser();
        if (user != null) {
            ImageLoader.getInstance().displayImage(user.getString(QuezzleUserMetadata.AVATAR_URL), avatarView, options);
            //nameView.setText(user.getUsername());
            getActivity().getActionBar().setTitle(user.getUsername());
        }

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE) {
            Uri uri = data != null ? data.getData() : null;
            avatarFilePath = Utils.getImagePathByUri(getActivity(), uri);
            if (!TextUtils.isEmpty(avatarFilePath)) {
                //show new avatar
                ImageLoader.getInstance().displayImage("file:/" + avatarFilePath, avatarView, options);

                //set save button enabled
                saveButton.setEnabled(true);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(receiver,
                                          new IntentFilter(UpdateProfileNotification.ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(receiver);
    }

    private void save() {
        //set save button disable
        saveButton.setEnabled(false);
        //display progress dialog
        progressDialog = ProgressDialog.show(getActivity(), getString(R.string.connecting), getString(R.string.loading_profile_update));
        //save user info
        NetworkService.updateUserProfile(getActivity(), avatarFilePath);
    }

    private void selectAvatar() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE);
    }

    private class UpdateProfileNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Activity activity = getActivity();
            if (activity != null) {
                if (UpdateProfileNotification.isSuccessful(intent)) {
                    Toast.makeText(activity, getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, UpdateProfileNotification.getErrorMessage(intent), Toast.LENGTH_LONG).show();
                }
            }
            progressDialog.dismiss();
        }
    }
}
