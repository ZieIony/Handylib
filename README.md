# Handylib
A library with bunch of handy methods. Static context only, no permissions required, works on API 8+.

### Examples:

    public static Application getApplication() {}
    public static Activity getCurrentActivity() {}
    public static Handler getHandler() {}
    public static void runOnUIThread(Runnable r) {}
    public static Bitmap takeScreenshot() {}
    public static void setActivityLifecycleListener(ActivityLifecycleListener listener) {}
    
### FAQ

*How?*

It's mostly reflection and knowledge about Android API internals.

*Is it safe? I'm afraid of reflection*

Of course it's not safe! Reflection is not checked at compilation time. It means that if anything changes in a future release of Android, this library will stop working.

*Why don't you use ordinary core for these things?*

I wrote tons of ordinary code already. I like doing fun stuff and I'm a pretty lazy coder. The outcome is here.
