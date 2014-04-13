package com.skylion.quezzle.ui.activity;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.parse.ParseUser;
import com.skylion.quezzle.R;
import com.skylion.quezzle.datamodel.QuezzleUserMetadata;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class UserProfileActivity extends ActionBarActivity implements OnClickListener {
	
	private DisplayImageOptions options;
	
	private ImageView avatarView;
	private TextView nameView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_screen);
		
		getActionBar().setTitle(R.string.title_activity_user_profile);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.blue));
		
		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.def_icon).showImageForEmptyUri(R.drawable.def_icon)
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED).resetViewBeforeLoading(true).cacheInMemory(true).cacheOnDisc(true)
				.displayer(new RoundedBitmapDisplayer(Integer.MAX_VALUE)).build();
		
		avatarView = (ImageView) findViewById(R.id.avatarView_profileScreen);
		nameView = (TextView) findViewById(R.id.nameText_profileScreen);
		
		ImageLoader.getInstance().displayImage(ParseUser.getCurrentUser().getString(QuezzleUserMetadata.AVATAR_URL), avatarView, options);
		nameView.setText(ParseUser.getCurrentUser().getUsername());
	}

	@Override
	public void onClick(View v) {
		// кнопки деавторизация юзера и смены аватара (если это свой профиль)
		// кнопка открытия приватного чата (если это чужой профиль)
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		finish();
		return super.onOptionsItemSelected(item);
	}

}
