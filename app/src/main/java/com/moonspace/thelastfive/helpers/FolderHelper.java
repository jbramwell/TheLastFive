package com.moonspace.thelastfive.helpers;

import java.io.File;

public class FolderHelper
{
    /**
     * Returns the size of the file (or folder) as kilobytes (Kb) or megabytes (Mb).
     *
     * @param file the file (or folder) to return the size for.
     * @return the size of the file (or folder) as kilobytes (Kb) or megabytes (Mb).
     */
    public static String getFolderSizeLabel(File file)
    {
        // Get size and convert bytes into Kb.
        long size = getFolderSize(file) / 1024;

        if (size >= 1024)
        {
            return (size / 1024) + " Mb";
        }
        else
        {
            return size + " Kb";
        }
    }

    /**
     * Returns the size of the file (or folder) in bytes.
     *
     * @param file the file (or folder) to return the size for.
     * @return the size of the file (or folder) in bytes.
     */
    private static long getFolderSize(File file)
    {
        long size = 0;

        if (file.isDirectory())
        {
            for (File child : file.listFiles())
            {
                size += getFolderSize(child);
            }
        }
        else
        {
            size = file.length();
        }

        return size;
    }
}
