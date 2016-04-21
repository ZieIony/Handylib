package tk.zielony.handylib;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Marcin on 2016-04-22.
 */
class Instrumentation extends android.app.Instrumentation {

    private android.app.Instrumentation instrumentation;

    ActivityLifecycleListener listener;

    public Instrumentation(android.app.Instrumentation instrumentation, ActivityLifecycleListener listener) {
        this.instrumentation = instrumentation;
        this.listener = listener;
    }

    @Override
    public void callActivityOnPause(Activity activity) {
        instrumentation.callActivityOnPause(activity);
        listener.onPause(activity);
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        instrumentation.callActivityOnResume(activity);
        listener.onResume(activity);
    }

    @Override
    public void callActivityOnStart(Activity activity) {
        instrumentation.callActivityOnStart(activity);
        listener.onStart(activity);
    }

    @Override
    public void callActivityOnStop(Activity activity) {
        instrumentation.callActivityOnStop(activity);
        listener.onStop(activity);
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        instrumentation.callActivityOnDestroy(activity);
        listener.onDestroy(activity);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        instrumentation.callActivityOnCreate(activity, icicle);
        listener.onCreate(activity, icicle);
    }

    @Override
    public void callActivityOnRestart(Activity activity) {
        instrumentation.callActivityOnRestart(activity);
        listener.onRestart(activity);
    }

    @Override
    public void callActivityOnNewIntent(Activity activity, Intent intent) {
        instrumentation.callActivityOnNewIntent(activity, intent);
        listener.onNewIntent(activity, intent);
    }

    @Override
    public void callActivityOnPostCreate(Activity activity, Bundle icicle) {
        instrumentation.callActivityOnPostCreate(activity, icicle);
        listener.onPostCreate(activity, icicle);
    }

    @Override
    public void callActivityOnRestoreInstanceState(Activity activity, Bundle savedInstanceState) {
        instrumentation.callActivityOnRestoreInstanceState(activity, savedInstanceState);
        listener.onRestoreInstanceState(activity, savedInstanceState);
    }

    @Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
        instrumentation.callActivityOnSaveInstanceState(activity, outState);
        listener.onSaveInstanceState(activity, outState);
    }

    @Override
    public void callActivityOnUserLeaving(Activity activity) {
        instrumentation.callActivityOnUserLeaving(activity);
        listener.onUserLeaving(activity);
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        instrumentation.callApplicationOnCreate(app);
        listener.onApplicationOnCreate(app);
    }
}
