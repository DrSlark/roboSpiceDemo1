package ztt.robospicedownloadtest1;

/**
 * Created by 123 on 2014/12/24.
 */
public abstract class GlobalConstants {
    private static final String AppRootDirName="zttDownLoadedTest";
    public static final String AppRootDir=Utils.makePath(Utils.getSdDirectory(), AppRootDirName);

    public static final String JSON_FILENAME="DownLoadTask.json";

    public static final String ACTIONSTRING_BROADCASTRECEIVER
            ="android.intent.action.MYRECEIVER";

    public static final String BUNDLEKEY_DOWNLOADTASK="downloadtask";

    public static final String BUNDLEKEY_NOTIFICATION_ID = "BUNDLE_KEY_NOTIFICATION_ID";

    public static final String BUNDLEKEY_NOTIFICATION2ACTIVITY_UUID="BUNDLEKEY_NOTIFICATION2ACTIVITY_UUID";










}
