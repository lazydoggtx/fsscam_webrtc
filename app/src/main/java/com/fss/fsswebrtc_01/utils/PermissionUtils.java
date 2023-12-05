package com.fss.fsswebrtc_01.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

//http://karthikdroid.blogspot.com/2017/01/multiple-marshmallow-permissions-in_30.html
//https://gist.github.com/sreelallalu/f44f1ae1f88505ac886aacc5f6305710
public final class PermissionUtils {

    public static final int REQUEST_PERMISSIONS_ALL_APP = 124;
    public static final int REQUEST_PERMISSION_STORAGE = 101;
    public static final int REQUEST_PERMISSION_WIFI = 102;
    public static final int REQUEST_PERMISSION_LOCATION = 103;
    public static final int REQUEST_PERMISSION_CAMERA = 104;
    public static final int REQUEST_PERMISSION_CONTACTS = 105;
    public static final int REQUEST_PERMISSION_MICROPHONE = 106;

    public static final String[] PERMISSIONS_ALL_APP = new String[]{
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO
    };

    public static final String[] PERMISSIONS_WIFI = new String[]{
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
    };

    public static final String[] PERMISSIONS_LOCATION = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    public static final String[] PERMISSIONS_STORAGE = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final String[] PERMISSIONS_CAMERA = new String[]{
            Manifest.permission.CAMERA
    };

    public static final String[] PERMISSIONS_CONTACTS = new String[]{
            Manifest.permission.READ_CONTACTS
    };

    public static final String[] PERMISSIONS_MICROPHONE = new String[]{
            Manifest.permission.RECORD_AUDIO
    };


    public static boolean permissionCheck(FragmentActivity activity, String[] permissions, int requestCode) {
        int result;

        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(activity, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), requestCode);
            return false;
        }
        return true;
    }

    public static boolean permissionCheck(FragmentActivity activity, String[] permissions) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(activity, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        //đủ quyền -> false
        return !listPermissionsNeeded.isEmpty();
    }

    public static boolean permissionCheck(Context ctx, String[] permissions) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(ctx, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        //đủ quyền -> false
        return !listPermissionsNeeded.isEmpty();
    }

    public static boolean checkIfAlreadyhavePermission(Context context, String permission) {
        int result = ContextCompat.checkSelfPermission(context, permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

}
