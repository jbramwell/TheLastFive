package com.moonspace.thelastfive.helpers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

public class VersionHelper
{
    /**
     * Gets the version name (e.g. 1.0) of the calling app.
     *
     * @param context the context of the calling app.
     * @return the version number of the calling app.
     */
    public static String getVersionName(Context context)
    {
        String version = "";
        PackageManager packageManager = context.getPackageManager();

        try
        {
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);

            version = String.format("%s (%s)", packageInfo.versionName, getVersionNumber(context));
        }
        catch (PackageManager.NameNotFoundException e)
        {
            // Swallow the exception... nom, nom!!!
        }

        return version;
    }

    /**
     * Gets the version number of the calling app.
     *
     * @param context the context of the calling app.
     * @return the version number of the calling app.
     */
    private static long getVersionNumber(Context context)
    {
        long versionNumber = 0;
        PackageManager packageManager = context.getPackageManager();

        try
        {
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            {
                versionNumber = packageInfo.getLongVersionCode();
            }
            else
            {
                versionNumber = packageInfo.versionCode;
            }
        }
        catch (PackageManager.NameNotFoundException e)
        {
            // Swallow the exception... nom, nom!!!
        }

        return versionNumber;
    }
}
