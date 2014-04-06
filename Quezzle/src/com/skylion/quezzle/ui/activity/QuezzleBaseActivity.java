package com.skylion.quezzle.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import com.bugsense.trace.BugSenseHandler;
import com.skylion.quezzle.utility.Constants;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 06.04.14
 * Time: 16:17
 * To change this template use File | Settings | File Templates.
 */
public class QuezzleBaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BugSenseHandler.initAndStartSession(this, Constants.BUGSENS_API_KEY);
    }
}
