package com.example.elenaa.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.elenaa.myapplication.imageoperations.DetectionResultsHandler;
import com.example.elenaa.myapplication.imageoperations.enums.ExportFormatEnum;
import com.example.elenaa.myapplication.imageoperations.enums.ImageColorTypeEnum;
import com.example.elenaa.myapplication.utils.ImageBitmapHelper;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";

    private static final int REQUEST_IMAGE_CAPTURE  = 1;

    private final int IMAGE_MENU_SAVE_COLOR_TO_JPEG = 1;
    private final int IMAGE_MENU_SAVE_COLOR_TO_PNG = 2;
    private final int IMAGE_MENU_SAVE_BW_TO_JPEG = 3;
    private final int IMAGE_MENU_SAVE_BW_TO_PNG = 4;
    private final int IMAGE_MENU_DELETE = 5;

    private String currentPhotoPath;
    private Bitmap currentImageBitmap;
    private ImageView imageView;
    private TextView resultsTextView;
    private ProgressDialog progressDialog;
    private Observer observer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prepare MIContext for proper work
        MIContext.prepareMIContext(this);
        observer = createObserver();
        MIContext.getInstance().addObserver(observer);

        // Configure progress dialog
        progressDialog = createProgressDialog();

        setContentView(R.layout.activity_main);

        // Configure toolbar and views
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = (ImageView) findViewById(R.id.photo_captured_imageView);
        registerForContextMenu(imageView);

        resultsTextView = (TextView) findViewById(R.id.detection_results_textView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(progressDialog.isShowing() == true) {
            progressDialog.dismiss();
        }
        MIContext.getInstance().deleteObserver(observer);
    }

    @Override
    public void  onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        switch (v.getId()) {
            case R.id.photo_captured_imageView:
                menu.add(0, IMAGE_MENU_DELETE, 0, R.string.image_menu_delete_title);
                menu.add(0, IMAGE_MENU_SAVE_COLOR_TO_JPEG, 0, R.string.image_menu_save_color_jpeg_title);
                menu.add(0, IMAGE_MENU_SAVE_BW_TO_JPEG, 0, R.string.image_menu_save_bw_jpeg_title);
                menu.add(0, IMAGE_MENU_SAVE_COLOR_TO_PNG, 0, R.string.image_menu_save_color_png_title);
                menu.add(0, IMAGE_MENU_SAVE_BW_TO_PNG, 0, R.string.image_menu_save_bw_png_title);
        }
    }

    @Override
    public boolean onContextItemSelected (MenuItem item) {
        switch (item.getItemId()) {
            case IMAGE_MENU_SAVE_COLOR_TO_JPEG:
                MIContext.getInstance().saveMIImage(ExportFormatEnum.JPEG, ImageColorTypeEnum.COLOR);
                break;

            case IMAGE_MENU_SAVE_BW_TO_JPEG:
                MIContext.getInstance().saveMIImage(ExportFormatEnum.JPEG, ImageColorTypeEnum.BLACK_AND_WHITE);
                break;

            case IMAGE_MENU_SAVE_COLOR_TO_PNG:
                MIContext.getInstance().saveMIImage(ExportFormatEnum.PNG, ImageColorTypeEnum.COLOR);
                break;

            case IMAGE_MENU_SAVE_BW_TO_PNG:
                MIContext.getInstance().saveMIImage(ExportFormatEnum.PNG, ImageColorTypeEnum.BLACK_AND_WHITE);
                break;

            case IMAGE_MENU_DELETE:
                // Clear MIImage and update the UI
                MIContext.getInstance().clearMIImage();
                updateUI();
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Handle "Take photo" button clicks
    public void takePhotoButtonOnClick(View view) {
        updateUI();
        dispatchTakePictureIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            if(currentPhotoPath != null) {
                // Create Bitmap from the file
                currentImageBitmap = ImageBitmapHelper.getBitmapFromFile(currentPhotoPath);

                // Reduce the bitmap for setting it to image view
                Bitmap reducedBitmap = ImageBitmapHelper.reduceBitmapForImageView(imageView, currentPhotoPath);
                updateImageView(reducedBitmap);

                // Now temp file can be safety deleted
                // NOTE: This method does not work as expected
                ImageBitmapHelper.deleteFileFromStorage(currentPhotoPath);
            }

            if(currentImageBitmap != null) {
                // Start processing
                MIContext.getInstance().loadSourceImage(currentImageBitmap);
                MIContext.getInstance().run();
            }
        }
    }

    private ProgressDialog createProgressDialog() {
        final ProgressDialog _progressDialog = new ProgressDialog(this);
        _progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        _progressDialog.setCancelable(false);
        _progressDialog.setMax(100);
        _progressDialog.setProgress(0);
        _progressDialog.setMessage(getString(R.string.progress_dialog_message));

        return _progressDialog;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(takePictureIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                // Create temp file in the external cache directory
                File cacheDirectory = getExternalCacheDir();
                photoFile = ImageBitmapHelper.createImageFile(cacheDirectory, ".jpg");

            } catch (IOException ex) {
                Log.e(TAG, ex.getMessage());
            }

            if(photoFile != null) {
                currentPhotoPath = photoFile.getAbsolutePath();

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void updateTextView(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultsTextView.setText(message);
            }
        });
    }

    private void updateImageView(final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    // Destroy Bitmap object, release the memory (from MISDK standard sample)
    private void clearBitmap() {
        // need to do this "manually", to prevent OOM
        if( currentImageBitmap != null ) {
            currentImageBitmap.recycle();
            currentImageBitmap = null;
            System.gc();
        }
    }

    private void updateUI() {
        clearBitmap();
        updateTextView("");
        updateImageView(null);
    }

    private void updateProgressDialog(final int progress, final boolean shouldStop) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setProgress(progress);

                if (progressDialog.isShowing() == true && shouldStop == true) {
                    progressDialog.dismiss();
                }

                if (progressDialog.isShowing() == false && shouldStop == false) {
                    progressDialog.show();
                }

            }
        });
    }

    private Observer createObserver() {
        final Observer _observer = new Observer() {
            @Override
            public void update(Observable observable, Object data) {

                // Handle progress
                if(data instanceof Integer) {

                    final Integer progress = (Integer)data;
                    updateProgressDialog(progress, false);
                }

                // Handle results when the process finished
                if(data instanceof DetectionResultsHandler) {

                    // Hide the progress dialog
                    updateProgressDialog(100, true);

                    DetectionResultsHandler detectionResultsHandler = (DetectionResultsHandler) data;
                    updateTextView(detectionResultsHandler.getReadableResults());
                }
            }
        };
        return _observer;
    }
}
