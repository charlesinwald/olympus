package com.example.charles.olympus;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

public class VideoActivity extends AppCompatActivity {
    MediaSessionCompat mMediaSession;
    PlaybackStateCompat.Builder mStateBuilder;
    public HttpSingleton http = new HttpSingleton();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        //String url = "https://www.rmp-streaming.com/media/bbb-360p.mp4";
        final VideoView video = (VideoView)findViewById(R.id.videoView2);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/"+R.raw.videoplayback);
        Log.d("URI", String.valueOf(uri));
        //Uri uri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName(), new File(R.raw.videoplayback));

        video.setVideoURI(uri);
        video.start();

        Button playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                video.start();
            }
        });

        Button pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                video.pause();
            }
        });


        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Log.d("COMPLETED VIDEO", "OLYMPUS VIDEO THATS TOO LONG TO WATCH & TEST");
                video.stopPlayback();
                finish();
            }
        });

        //final MediaPlayer mediaPlayer;
        /**try {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.videoplayback);
            mediaPlayer.prepare();
            mediaPlayer.start(); // might take long! (for buffering, etc)
**/
        }}
    /**@Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playButton:
                (VideoView)findViewById(R.id.videoView2).resume();
                break;
            case R.id.pauseButton:
                (VideoView)findViewById(R.id.videoView2).pause();
                break;

<<<<<<< HEAD
            default:
                break;
        }
    }**/
//        // Create a MediaSessionCompat
//        mMediaSession = new MediaSessionCompat(this, "Video Player");
//
//        // Enable callbacks from MediaButtons and TransportControls
//        mMediaSession.setFlags(
//                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
//                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
//
//        // Do not let MediaButtons restart the player when the app is not visible
//        mMediaSession.setMediaButtonReceiver(null);
//
//        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
//        mStateBuilder = new PlaybackStateCompat.Builder()
//                .setActions(
//                        PlaybackStateCompat.ACTION_PLAY |
//                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
//        mMediaSession.setPlaybackState(mStateBuilder.build());
//
//        // MySessionCallback has methods that handle callbacks from a media controller
//        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
//            @Override
//            public void onCommand(String command, Bundle extras, ResultReceiver cb) {
//                super.onCommand(command, extras, cb);
//                switch (command)
//                {
//
//                }
//            }
//        });
//
//        // Create a MediaControllerCompat
//        MediaControllerCompat mediaController =
//                new MediaControllerCompat(this, mMediaSession);
//
//        MediaControllerCompat.setMediaController(this, mediaController);


