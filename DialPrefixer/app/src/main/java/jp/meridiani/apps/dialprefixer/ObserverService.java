package jp.meridiani.apps.dialprefixer;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;

public class ObserverService extends Service {

    private Context mContext = null;
    private Prefs mPrefs = null;
    private ContentObserver mObserver = null;

    public ObserverService() {
    }

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        mPrefs = Prefs.getInstance(mContext);
        mObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                mContext.getContentResolver().unregisterContentObserver(mObserver);
                if (mPrefs.isCallLogDeletePrefix()) {
                    CallLogManager.rewriteLastCallLog(mContext, mPrefs.isCallLogShowToast());
                }
                stopSelf();
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext.getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, true, mObserver);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mContext.getContentResolver().unregisterContentObserver(mObserver);
    }
}
