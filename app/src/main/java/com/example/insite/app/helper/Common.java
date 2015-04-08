package com.example.insite.app.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.example.insite.app.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Xing Yi on 8/4/2015.
 */
public class Common {
    private static Context context;

    public static void setContext(Context mcontext) {
        if (context == null)
            context = mcontext;
    }

    public static String getTodayDate()
    {
        TimeZone currZone = TimeZone.getTimeZone("Asia/Singapore");
        Calendar myCal = Calendar.getInstance(currZone, new Locale("en"));
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd");


        return ( sdf.format(myCal.getTime()) );
    }

    public static String getCurrentTime()
    {
        TimeZone currZone = TimeZone.getTimeZone("Asia/Singapore");
        Calendar myCal = Calendar.getInstance(currZone, new Locale("en"));
        String timeNow = String.format("%02d", myCal.get(myCal.HOUR_OF_DAY) ) + ":" + String.format("%02d", myCal.get(myCal.MINUTE) )
                + ":" + String.format("%02d", myCal.get(myCal.SECOND));

        return timeNow;
    }


    public static boolean isInputEmpty(String stringToCheck)
    {
        //stringToCheck.trim();
        stringToCheck = stringToCheck.replaceAll(" ", "");
        stringToCheck = stringToCheck.replaceAll("\n", "");
        return stringToCheck.isEmpty();
    }

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    public static Bitmap setDefaultImage()
    {
        Bitmap bMap = BitmapFactory.decodeResource(context.getResources(), R.drawable.sit_logo);

        return resize(bMap, 1000, 1000);
    }

    public static Bitmap handleImageFrom_PictureFile(String path)
    {
        // Decode image url and retrieve the image
        Bitmap bMap = BitmapFactory.decodeFile(path);
        // Rotate image
        bMap = imageOrientationValidator(bMap,path);

        // Resize image
        return resize(bMap, 1000, 1000);
    }



    private static Bitmap imageOrientationValidator(Bitmap bitmap, String path) {

        ExifInterface ei;
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {

        Bitmap bitmap = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        try {
            bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                    matrix, true);
        } catch (OutOfMemoryError err) {
            err.printStackTrace();
        }
        return bitmap;
    }
}
