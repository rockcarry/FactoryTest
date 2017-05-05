package com.apical.factorytest;

import android.os.Environment;
import java.io.*;

public class Utils {
    public static final String SDCARD_ROOT = "/storage/extsd";

    public static boolean isSdcardInsert() {
        String state = Environment.getStorageState(new File(SDCARD_ROOT));
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return false;
        }
        if (Environment.MEDIA_CHECKING.equals(state)) {
            return false;
        }
        return true;
    }
}

