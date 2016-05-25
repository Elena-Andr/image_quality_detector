package com.example.elenaa.myapplication.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageBitmapHelper {

    public static Bitmap getBitmapFromFile(String imagePath) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imagePath, bmOptions);
    }

    public static Bitmap reduceBitmapForImageView(ImageView imageView, String imagePath) {
        // Get the size of the ImageView
        int targetWidth = imageView.getWidth();
        int targetHeight = imageView.getHeight();

        //Get the width and height of the original image
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);

        int photoWidth = bmOptions.outWidth;
        int photoHeight = bmOptions.outHeight;

        // Calculate the scaling rate
        int scaleFactor = 1;
        if((targetWidth > 0) || (targetHeight > 0)) {
            scaleFactor = Math.min(photoWidth/targetWidth, photoHeight/targetHeight);
        }

        // Set bitmap options to scale the image decode target
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        // Decode the JPEG file into a Bitmap
        Bitmap reducedSizeImageBitmap = BitmapFactory.decodeFile(imagePath, bmOptions);

        return reducedSizeImageBitmap;
    }

    // The method does not work as expected
    // TODO: modify the method so the images to be deleted from the gallery
    public static void deleteFileFromStorage(String filePath) {
        File file = new File(filePath);

        if(file.exists() == true) {
             file.delete();
        }
    }

    public static File createImageFile(File storageDir, String extension) throws IOException {
        File imageFile = File.createTempFile(
                createImageFileName(),  /* prefix */
                extension,         /* suffix */
                storageDir   /* directory */
        );
        return imageFile;
    }

    public static String createImageFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";

        return imageFileName;
    }

}
