package jp.meridiani.apps.dialprefixer;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;

public class ObserverService extends Service {

    private ContentObserver mObserver = null;

    public ObserverService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final Context context = getApplicationContext();
        final Prefs prefs = Prefs.getInstance(context);
        mObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                if (!prefs.isCallLogDeletePrefix()) {
                    context.getContentResolver().unregisterContentObserver(this);
                    stopSelf();
                    return;
                }
                CallLogManager.rewriteCallLog(context);
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Context context = getApplicationContext();
        Prefs prefs = Prefs.getInstance(context);
        ContentResolver resolver = context.getContentResolver();

        if (prefs.isCallLogDeletePrefix()) {
            resolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, mObserver);
        }
        else {
            resolver.unregisterContentObserver(mObserver);
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Context context = getApplicationContext();
        ContentResolver resolver = context.getContentResolver();
        resolver.unregisterContentObserver(mObserver);
    }
}
