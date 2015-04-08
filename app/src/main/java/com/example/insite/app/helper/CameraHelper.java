package com.example.insite.app.helper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Xing Yi on 8/4/2015.
 */
public class CameraHelper {

    public static String generateFileName(){

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "inSITe_" + timeStamp + ".jpg";

        return fileName;
    }

    public static Uri getImageFileUri(File imageFile, String newFileName) {

        final String CAMERA_TAG = "getImageFileUri Method: ";

        // Create a storage directory for the images
        // To be safe(er), you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this
        imageFile = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "inSITe");

        Log.d(CAMERA_TAG, "Find " + imageFile.getAbsolutePath());

        if (!imageFile.exists()) {
            if (!imageFile.mkdirs()) {
                Log.d("CameraTestIntent", "failed to create directory");
                return null;
            } else {
                Log.d(CAMERA_TAG, "create new inSITe folder");
            }
        }

        // Create an image file name
        File image = new File(imageFile, newFileName);

        if (!image.exists()) {
            try {
                image.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //return image;
        // Create an File Uri
        return Uri.fromFile(image);
    }

    public static void galleryAddPic(Uri currentImageUri, Activity activity) {
        /**
         * copy current image to Gallery
         */
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(currentImageUri);
        activity.sendBroadcast(mediaScanIntent);
    }
}
