package com.skylion.quezzle.utility;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 03.04.14
 * Time: 22:26
 * To change this template use File | Settings | File Templates.
 */
public abstract class Constants {
    public static final String LOG_TAG = "Quezzle";
    public static final String PARSE_APP_ID = "RVCqyTO6a3jDJPh0GeKRzbbpdXZWGWtm13m0MN67";
    public static final String PARSE_CLIENT_KEY = "BBD41jgbfdaTUdvtTxutynfB07C2HJKRquCX8MR3";
    public static final String BUGSENS_API_KEY = "9b18f9b5";

    public interface SyncStatus {
        public static final int UP_TO_DATE = 0;
        public static final int NEW_ITEM = 1;
        public static final int NEED_UPLOAD = 2;
        public static final int NEED_REFRESH = 4;
    }
}
