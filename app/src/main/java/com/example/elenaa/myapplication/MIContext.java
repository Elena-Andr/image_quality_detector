package com.example.elenaa.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.abbyy.mobile.imaging.FineOperation;
import com.abbyy.mobile.imaging.MICallback;
import com.abbyy.mobile.imaging.MIExporter;
import com.abbyy.mobile.imaging.MIImage;
import com.abbyy.mobile.imaging.MILicenser;
import com.abbyy.mobile.imaging.MIProcessor;
import com.abbyy.mobile.imaging.errors.MIGenericException;
import com.abbyy.mobile.imaging.operations.FineBinarize;
import com.example.elenaa.myapplication.imageoperations.BlurDetectionController;
import com.example.elenaa.myapplication.imageoperations.DetectionResultsHandler;
import com.example.elenaa.myapplication.imageoperations.GlareDetectionController;
import com.example.elenaa.myapplication.imageoperations.NoiseDetectionController;
import com.example.elenaa.myapplication.imageoperations.OperationController;
import com.example.elenaa.myapplication.imageoperations.enums.ExportFormatEnum;
import com.example.elenaa.myapplication.imageoperations.enums.ImageColorTypeEnum;
import com.example.elenaa.myapplication.utils.ImageBitmapHelper;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class MIContext extends Observable {

    private static final String TAG = "MIContext";

    private static MIContext INSTANCE = null;
    private Context mainContext;
    private MIImage sourceMIImage;

    private MIContext(){}

    public static MIContext getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new MIContext();
        }
        return INSTANCE;
    }

    public static void prepareMIContext(Context context) {
        getInstance().mainContext = context;

        System.loadLibrary("MobileImagingEngine");

        try {
            final InputStream licenseStream = getInstance().mainContext.getAssets().open("MISample.license");
            MILicenser.setLicense(licenseStream, "Android_ID");

        } catch (final Exception exception) {
            Log.e(TAG, "Set license failed");
        }
    }

    // The method deletes the previous MIImage (if exists) and load the new MIImage from Bitmap
    public void loadSourceImage(final Bitmap sourceImageBitmap) {
        clearMIImage();
        sourceMIImage = new MIImage(sourceImageBitmap);
    }

    public Context getMainContext() {
        return mainContext;
    }

    public void run() {
        final OperationController[] operationControllers = createOperationControllers();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    MIProcessor miProcessor = new MIProcessor(operationControllers[0].createOperation(),
                            operationControllers[1].createOperation(),
                            operationControllers[2].createOperation());

                    miProcessor.processImage(sourceMIImage, createMICallback());
                    DetectionResultsHandler resultsHandler = new DetectionResultsHandler(operationControllers);

                    setChanged();
                    notifyObservers(resultsHandler);

                } catch (MIGenericException miException) {
                    Log.e(TAG, "Image processing failed");
                }
            }
        });

        thread.start();
    }

    public void saveMIImage(ExportFormatEnum exportFormat, ImageColorTypeEnum imageColorType) {
        if(sourceMIImage == null) {
            // do nothing
            return;
        }

        try {

            if (exportFormat == ExportFormatEnum.JPEG && imageColorType == ImageColorTypeEnum.COLOR) {
                saveMIImageToJPEG(sourceMIImage);
            }
            if (exportFormat == ExportFormatEnum.JPEG && imageColorType == ImageColorTypeEnum.BLACK_AND_WHITE) {
                saveMIImageAsBW(sourceMIImage, exportFormat.JPEG);
            }
            if (exportFormat == ExportFormatEnum.PNG && imageColorType == imageColorType.COLOR) {
                saveMIImageToPNG(sourceMIImage);
            }
            if (exportFormat == ExportFormatEnum.PNG && imageColorType == imageColorType.BLACK_AND_WHITE) {
                saveMIImageAsBW(sourceMIImage, exportFormat.PNG);
            }

        } catch(IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void clearMIImage() {
        if(getInstance().sourceMIImage != null) {
            getInstance().sourceMIImage.destroy();
            getInstance().sourceMIImage = null;
        }
    }

    private OperationController[] createOperationControllers() {
        // Retrieve the settings from the preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mainContext);
        boolean isGrayscaleValue = sharedPreferences.getBoolean(mainContext.getString(R.string.pref_image_is_greyscale_key), false);
        boolean isBlurFastMode = sharedPreferences.getBoolean(mainContext.getString(R.string.pref_blur_fast_mode_key), false);
        boolean isGlareFastMode = sharedPreferences.getBoolean(mainContext.getString(R.string.pref_glare_fast_mode_key), false);
        boolean isNoiseFastMode = sharedPreferences.getBoolean(mainContext.getString(R.string.pref_noise_fast_mode_key), false);

        // Initialize operation controllers with these setting
        OperationController glareDetectionController = new GlareDetectionController(isGrayscaleValue, isGlareFastMode);
        OperationController noiseDetectionController = new NoiseDetectionController(isGrayscaleValue, isNoiseFastMode);
        OperationController blurDetectionController = new BlurDetectionController(isGrayscaleValue, isBlurFastMode);

        List<OperationController> operationControllersList = new ArrayList<>();
        operationControllersList.add(glareDetectionController);
        operationControllersList.add(noiseDetectionController);
        operationControllersList.add(blurDetectionController);

        return operationControllersList.toArray(new OperationController[operationControllersList.size()]);
    }

    private MICallback createMICallback() {
        MICallback callback = new MICallback() {
            @Override
            public int onProgressUpdated(int i) {

                // Pass the progress data to the main thread
                setChanged();
                notifyObservers(new Integer(i));

                return 0;
            }
        };
        return callback;
    }

    private void saveMIImageToJPEG(MIImage miImage) throws IOException {
        FileOutputStream outputStream = null;
        try {

            String filePath = Environment.getExternalStorageDirectory() + "/" + ImageBitmapHelper.createImageFileName() + ".jpg";

            outputStream = new FileOutputStream( filePath, false );
            MIExporter.exportJPEG(miImage, 1.0f, outputStream);

            // Update the gallery (in order the exported images to be shown in the gallery)
            MediaScannerConnection.scanFile(mainContext, new String[]{filePath}, null, null);

        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch (MIGenericException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            outputStream.close();
        }
    }

    private void saveMIImageToPNG(MIImage miImage) throws IOException {
        FileOutputStream outputStream = null;
        try {

            String filePath = Environment.getExternalStorageDirectory() + "/" + ImageBitmapHelper.createImageFileName() + ".png";

            outputStream = new FileOutputStream( filePath, false );
            MIExporter.exportPNG(miImage, outputStream);

            // Update the gallery
            MediaScannerConnection.scanFile(mainContext, new String[]{filePath}, null, null);

        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
        } catch (MIGenericException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            outputStream.close();
        }
    }

    private void saveMIImageAsBW(MIImage miImage, final ExportFormatEnum exportFormat) throws IOException {
        final FineOperation fineBinarize = new FineBinarize(false, 0.5f);
        final MIImage resultMIImage = new MIImage(miImage);

        // Perform binarization
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    MIProcessor miProcessor = new MIProcessor(fineBinarize);
                    miProcessor.processImage(resultMIImage, new MICallback() {
                        @Override
                        public int onProgressUpdated(int i) {
                            // not used
                            return 0;
                        }
                    });

                    // Export
                    if (exportFormat == ExportFormatEnum.JPEG)
                        saveMIImageToJPEG(resultMIImage);

                    if (exportFormat == ExportFormatEnum.PNG)
                        saveMIImageToPNG(resultMIImage);

                } catch (MIGenericException e) {
                    Log.e(TAG, e.getMessage());
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
        thread.start();
    }
}
