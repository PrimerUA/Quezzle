package com.skylion.quezzle.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.parse.ParseUser;
import com.skylion.quezzle.R;
import com.skylion.quezzle.datamodel.QuezzleUserMetadata;
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
    private TextView nameView;
    private Button saveButton;

    private String avatarFilePath = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_profile_fragment, container, false);

        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.def_icon).showImageForEmptyUri(R.drawable.def_icon)
                .imageScaleType(ImageScaleType.EXACTLY_STRETCHED).resetViewBeforeLoading(true).cacheInMemory(true).cacheOnDisc(true)
                .displayer(new RoundedBitmapDisplayer(Integer.MAX_VALUE)).build();

        avatarView = (ImageView) rootView.findViewById(R.id.avatarView_profileScreen);
        avatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAvatar();
            }
        });
        nameView = (TextView) rootView.findViewById(R.id.nameText_profileScreen);
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
            nameView.setText(user.getUsername());
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

    private void save() {
        //set save button disable
        saveButton.setEnabled(false);

        //save user info
        NetworkService.updateUserProfile(getActivity(), avatarFilePath);
    }

    private void selectAvatar() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE);
    }
}
