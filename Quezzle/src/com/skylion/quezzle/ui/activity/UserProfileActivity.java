package com.skylion.quezzle.ui.activity;

import android.content.Context;
import android.content.Intent;
import com.skylion.quezzle.R;
import android.os.Bundle;
import android.view.MenuItem;

public class UserProfileActivity extends QuezzleBaseActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, UserProfileActivity.class);

        context.startActivity(intent);
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_profile);
		
		getActionBar().setTitle(R.string.title_activity_user_profile);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.blue));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case android.R.id.home :
                finish();
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
	}

}
