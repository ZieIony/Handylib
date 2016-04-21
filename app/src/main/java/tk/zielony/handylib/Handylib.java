package tk.zielony.handylib;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Created by Marcin on 2015-07-18.
 */
public class Handylib {

    private static Application application;

    private static Object activityThread;
    private static Class<?> activityThreadClass;

    private static Instrumentation instrumentation;
    private static Field instrumentationField;

    private static byte[] hash;

    private static Map<IBinder, Object> activities;

    private static WindowManager windowManager;
    private static Field viewsField;

    private static Field activityClientRecordPausedField;
    private static Field activityClientRecordActivityField;

    private static Handler handler;

    private Handylib() {
    }

    private static void initActivityThread() {
        try {
            activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentATMethod = activityThreadClass.getMethod("currentActivityThread");
            activityThread = currentATMethod.invoke(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the current application. This method should be called from the main thread.
     *
     * @return the application
     */
    public static
    @NonNull
    Application getApplication() {
        if (application != null)
            return application;
        if (activityThreadClass == null)
            initActivityThread();
        try {
            Method getApplicationMethod = activityThreadClass.getMethod("getApplication");
            application = (Application) getApplicationMethod.invoke(activityThread);
            return application;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;    // not really reached
    }

    private static void initInstrumentation() {
        if (activityThreadClass == null)
            initActivityThread();
        try {
            instrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
            instrumentationField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the instrumentation.
     *
     * @param instrumentation the instrumentation
     */
    public static void setInstrumentation(Instrumentation instrumentation) {
        if (instrumentationField == null)
            initInstrumentation();
        try {
            instrumentationField.set(activityThread, instrumentation);
            Handylib.instrumentation = instrumentation;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the current instrumentation.
     *
     * @return the instrumentation
     */
    public static Instrumentation getInstrumentation() {
        if (instrumentation != null)
            return instrumentation;
        if (instrumentationField == null)
            initInstrumentation();
        try {
            instrumentation = (Instrumentation) instrumentationField.get(activityThread);
            return instrumentation;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the global application hash computed as SHA-1 of hashes of all apk resources.
     *
     * @return the hash
     */
    public static byte[] getHash() {
        if (hash != null)
            return hash;

        Application app = getApplication();
        if (app == null)
            return null;

        ApplicationInfo ai = app.getApplicationInfo();
        String source = ai.sourceDir;

        try {
            JarFile jar = new JarFile(source);
            ZipEntry zipEntry = jar.getEntry("META-INF/MANIFEST.MF");
            InputStream inputStream = jar.getInputStream(zipEntry);
            int bytesRead = 0;
            byte[] buffer = new byte[(int) zipEntry.getSize()];
            while (bytesRead != zipEntry.getSize()) {
                bytesRead += inputStream.read(buffer, bytesRead,
                        (int) (zipEntry.getSize() - bytesRead));
            }
            jar.close();

            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-1");
                hash = md.digest(buffer);
                return hash;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            // should never happen
        }

        return null;
    }

    public static Map<IBinder, Object> getActivities() {
        try {
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            activities = (Map<IBinder, Object>) activitiesField
                    .get(activityThread);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return activities;
    }

    private static void initActivityInfo() {
        try {
            Class<?> activityClientRecordClass = Class.forName("android.app.ActivityThread.ActivityClientRecord");
            activityClientRecordPausedField = activityClientRecordClass.getDeclaredField("paused");
            activityClientRecordPausedField.setAccessible(true);
            activityClientRecordActivityField = activityClientRecordClass.getDeclaredField("activity");
            activityClientRecordActivityField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the current unpaused activity or null if there's no one
     *
     * @return the current activity
     */
    public static Activity getCurrentActivity() {
        if (activityClientRecordPausedField == null)
            initActivityInfo();
        try {
            for (Object o : activities.values()) {
                boolean paused = activityClientRecordPausedField.getBoolean(o);

                if (paused)
                    continue;

                Activity activity = (Activity) activityClientRecordActivityField.get(o);

                if (activity != null)
                    return activity;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Checks if the current thread is the main thread.
     *
     * @return true if the code is running on the main thread
     */
    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static Handler getHandler() {
        if (handler == null)
            handler = new Handler(Looper.getMainLooper());
        return handler;
    }

    public static void runOnUIThread(Runnable r) {
        if (handler == null)
            handler = new Handler(Looper.getMainLooper());
        handler.post(r);
    }

    private static void initWindowManager() {
        try {
            windowManager = (WindowManager) Class.forName("android.view.WindowManagerImpl").getDeclaredMethod("getDefault").invoke(null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            viewsField = windowManager.getClass().getDeclaredField("mViews");
            viewsField.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
    }

    /**
     * Gets all top level views (root views of all windows, dialogs and toasts)
     *
     * @return top level views
     */
    public static List<View> getViews() {
        if (windowManager == null)
            initWindowManager();
        List<View> views = null;
        try {
            Object viewsObj = viewsField.get(windowManager);
            if (viewsObj == null)
                return null;
            if (viewsObj instanceof View[]) {
                views = new ArrayList<>(Arrays.asList((View[]) viewsObj));
            } else {
                views = new ArrayList<>((List<View>) viewsObj);
            }
        } catch (IllegalAccessException ignored) {
        }
        return views;
    }

    private static List<ResolveInfo> getResolveInfos() {
        Application application = getApplication();
        PackageManager pm = application.getPackageManager();
        String packageName = application.getPackageName();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.setPackage(packageName);
        //mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        return pm.queryIntentActivities(mainIntent, 0);
    }

    /**
     * Gets the first app's entry point
     *
     * @return the first app's entry point
     */
    public static Intent getLaunchIntent() {
        List<ResolveInfo> resolveInfos = getResolveInfos();

        if (resolveInfos == null)
            return null;

        ActivityInfo activityInfo = resolveInfos.get(0).activityInfo;
        Intent launchIntent = new Intent();
        launchIntent.setClassName(activityInfo.packageName, activityInfo.name);

        return launchIntent;
    }

    /**
     * Gets the app's entry points
     *
     * @return the app's entry points
     */
    public static List<Intent> getLaunchIntents() {
        List<ResolveInfo> resolveInfos = getResolveInfos();

        if (resolveInfos == null)
            return null;

        List<Intent> intents = new ArrayList<>();

        for (ResolveInfo resolveInfo : resolveInfos) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            Intent launchIntent = new Intent();
            launchIntent.setClassName(activityInfo.packageName, activityInfo.name);
            intents.add(launchIntent);
        }

        return intents;
    }

    /**
     * Gets the first other app's entry points
     *
     * @return the first other app's entry points, null if there's no other entry points
     */
    public static Intent getOtherLaunchIntent(Class<? extends Activity> activityClass) {
        List<ResolveInfo> resolveInfos = getResolveInfos();

        if (resolveInfos == null)
            return null;

        for (ResolveInfo resolveInfo : resolveInfos) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo.name.equals(activityClass.getName()))
                continue;
            Intent launchIntent = new Intent();
            launchIntent.setClassName(activityInfo.packageName, activityInfo.name);
            return launchIntent;
        }

        return null;
    }

    private static Field tokenField;

    public static IBinder getToken(Context context) {
        if (tokenField == null) {
            try {
                tokenField = Activity.class.getDeclaredField("mToken");
                tokenField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        try {
            return (IBinder) tokenField.get(context);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Takes a screenshot. Captures all application's UI including dialogs and toasts
     * @return null if there's no UI
     */
    public static Bitmap takeScreenshot() {
        List<View> views = getViews();
        if (views == null || views.isEmpty())
            return null;
        Bitmap bitmap = Bitmap.createBitmap(windowManager.getDefaultDisplay().getWidth(), windowManager.getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        for (View v : views)
            v.draw(canvas);
        return bitmap;
    }

    /**
     * Sets an ActivityLifecycleListener
     *
     * @param listener an ActivityLifecycleListener
     */
    public static void setActivityLifecycleListener(ActivityLifecycleListener listener) {
        setInstrumentation(new tk.zielony.handylib.Instrumentation(getInstrumentation(), listener));
    }
}
