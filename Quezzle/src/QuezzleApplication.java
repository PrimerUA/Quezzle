import android.app.Application;
import com.parse.Parse;
import com.parse.PushService;
import com.skylion.quezzle.ui.activity.ChatsListActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 17.03.14
 * Time: 23:47
 * To change this template use File | Settings | File Templates.
 */
public class QuezzleApplication extends Application{

    public void onCreate() {
        Parse.initialize(this, "RVCqyTO6a3jDJPh0GeKRzbbpdXZWGWtm13m0MN67", "BBD41jgbfdaTUdvtTxutynfB07C2HJKRquCX8MR3");
        PushService.setDefaultPushCallback(this, ChatsListActivity.class);
    }
}
