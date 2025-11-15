package com.android.amg;

import android.content.Context;

import java.io.File;

public class AMGUtil {
    static {
        System.loadLibrary("amg");
    }

    // Note: decryptTemplate returns empty string - use decrypt instead
    public static native String decryptTemplate(Context context, String source);
    public static native String getToken(Context context, String firebaseToken);
    public static native String decrypt(Context context, String source);
    public static native String encrypt(Context context, String source, String firebaseToken);

    public static native String encryptFile(Context context, String source, File file, String firebaseToken);
}