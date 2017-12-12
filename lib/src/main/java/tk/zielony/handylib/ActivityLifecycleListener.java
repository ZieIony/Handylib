package tk.zielony.handylib;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

public interface ActivityLifecycleListener {
    void onCreate(Activity activity, Bundle icicle);

    void onDestroy(Activity activity);

    void onResume(Activity activity);

    void onPause(Activity activity);

    void onStart(Activity activity);

    void onStop(Activity activity);

    void onRestart(Activity activity);

    void onNewIntent(Activity activity, Intent intent);

    void onPostCreate(Activity activity, Bundle icicle);

    void onRestoreInstanceState(Activity activity, Bundle savedInstanceState);

    void onSaveInstanceState(Activity activity, Bundle outState);

    void onUserLeaving(Activity activity);

    void onApplicationOnCreate(Application app);
}
