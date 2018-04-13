package com.example.charles.olympus;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.gson.JsonElement;

import net.gotev.speech.Speech;
import net.gotev.speech.SpeechDelegate;
import net.gotev.speech.SpeechRecognitionNotAvailable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AIListener {
    public HttpSingleton http = new HttpSingleton();
    private SurfaceView mPreview;
    private Camera mCamera;
    private MediaRecorder mRecorder;
    private static String mFileName = null;
    private MediaPlayer mPlayer = null;
    private AIService aiService;

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        public static final int MEDIA_TYPE_IMAGE = 1;

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Log.d("FILE", "File successfully written");
                postRequest(pictureFile);

            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            mCamera.startPreview();
        }
    };
    private boolean capturing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RequestUserPermission requestUserPermission = new RequestUserPermission(this);
        requestUserPermission.verifyStoragePermissions();
        wifiPrompt();
        Button one = findViewById(R.id.one);
        one.setOnClickListener(this); // calling onClick() method

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
//        getRequest();
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        safePicture();
                    }
                }
        );
        Button voiceButton = (Button) findViewById(R.id.voice);
        voiceButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listenButtonOnClick();
                    }
                }
        );
//        try {
//            Speech.init(this, getPackageName());
//
//            // you must have android.permission.RECORD_AUDIO granted at this point
//            Speech.getInstance().startListening(new SpeechDelegate() {
//                @Override
//                public void onStartOfSpeech() {
//                    Log.i("speech", "speech recognition is now active");
//                }
//
//                @Override
//                public void onSpeechRmsChanged(float value) {
//                    Log.d("speech", "rms is now: " + value);
//                }
//
//                @Override
//                public void onSpeechPartialResults(List<String> results) {
//                    StringBuilder str = new StringBuilder();
//                    for (String res : results) {
//                        str.append(res).append(" ");
//                    }
//
//                    Log.i("speech", "partial result: " + str.toString().trim());
//                    if ((str.toString().trim()).equals("capture")) {
//                        mCamera.takePicture(null, null, mPicture);
//                    }
//                }
//
//                @Override
//                public void onSpeechResult(String result) {
//                    Log.i("speech", "result: " + result);
//                }
//            });
//        } catch (SpeechRecognitionNotAvailable exc) {
//            Log.e("speech", "Speech recognition is not available on this device!");
//        }
//        // Record to the external cache directory for visibility
//        mFileName = getExternalCacheDir().getAbsolutePath();
//        mFileName += "/audiorecordtest.3gp";
        //Creates a listener for voice commands
        final AIConfiguration config = new AIConfiguration("d3277182e8c44eacb0e71b2e82c67d2f",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);
//        aiService.startListening();

    }

    public void safePicture() {
        if (!capturing) {
            capturing = true;
            mCamera.takePicture(null, null, mPicture);

        }
    }

    public void onResult(final AIResponse response) {
        Result result = response.getResult();

        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        Log.d("DialogFlow", "Query:" + result.getResolvedQuery() +
                "\nAction: " + result.getAction() +
                "\nParameters: " + parameterString);
        if (result.getResolvedQuery().equals("capture")) {
            safePicture();
        }
    }
    @Override
    public void onError(final AIError error) {
        Log.d("DialogFlow",error.toString());
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e("MIC", "prepare() failed");
        }

        mRecorder.start();
    }

//    private void stopRecording() {
//        mRecorder.stop();
//        mRecorder.release();
//        mRecorder = null;
//    }
//
//
//    private void startPlaying() {
//        mPlayer = new MediaPlayer();
//        try {
//            mPlayer.setDataSource(mFileName);
//            mPlayer.prepare();
//            mPlayer.start();
//        } catch (IOException e) {
//            Log.e("PLAY AUDIO", "prepare() failed");
//        }
//    }

    private void postRequest(File image) {
        String result = null;
        try {
            result = new HttpSingleton().execute(image).get().toString();
            Toast toast = Toast.makeText(this, result, Toast.LENGTH_LONG);
            toast.show();
            Log.d("Result", result);
            String[] sep = result.split("\n");
            String modelNo = sep[0];
            Log.d("model num", modelNo);
            if(modelNo.contains("GIF")) {
                //Trigger Video Activity
                Intent i = new Intent(this, VideoActivity.class);
                startActivity(i);
                Intent x = new Intent(this, MainActivity.class);
                startActivity(x);
                //finish();
                //Log.d("FINISH VIDEO ACTIVITY", modelNo);
            }
            else {
                Toast toast1 = Toast.makeText(this, "Invalid Model Number.", Toast.LENGTH_LONG);
                toast1.show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    public void setText(String text) {
//        TextView textView = findViewById(R.id.maintext);
//        textView.setText(text);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.one:
                releaseCamera();
                Intent i = new Intent(this, VideoActivity.class);
                i.putExtra("Video", "1");
                startActivity(i);
                break;
            case R.id.voice:
                listenButtonOnClick();
                break;
            default:
                break;
        }
    }

    public void listenButtonOnClick() {
        Log.d("Voice","Starting voice recognition");
        aiService.startListening();
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        Log.d("CAMERA", String.valueOf(c));
        return c; // returns null if camera is unavailable
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();              // release the camera immediately on pause event
    }


    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    /** Tests for internet connectivity, prompts wifi if no connectivity */
    public void wifiPrompt(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(!isConnected){
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        }
    }
}

