package com.moonspace.thelastfive.helpers;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionsHelper
{
    /**
     * Scans a list of required permissions and builds a list containing the set of permissions the
     * user has not yet granted.
     *
     * @param requiredPermissions the list of required permissions.
     * @return a list of permissions not yet granted by the user.
     */
    public static String[] buildPermissionsList(Context context, String[] requiredPermissions)
    {
        List<String> missingPermissions = new ArrayList<>();

        for (String requiredPermission : requiredPermissions)
        {
            if (ContextCompat.checkSelfPermission(context, requiredPermission) != PackageManager.PERMISSION_GRANTED)
            {
                missingPermissions.add(requiredPermission);
            }
        }

        String[] missingPermissionsArray = new String[missingPermissions.size()];

        return missingPermissions.toArray(missingPermissionsArray);
    }

    /**
     * Scans the list of permissions grant results to determine if all permissions were granted
     * by the user.
     *
     * @param grantResults a list of permission grant results.
     * @return true if all permissions were granted; Otherwise, false.
     */
    public static Boolean isAllPermissionsGranted(int[] grantResults)
    {
        for (int grantResult : grantResults)
        {
            if (grantResult != PackageManager.PERMISSION_GRANTED) return false;
        }

        return true;
    }
}
