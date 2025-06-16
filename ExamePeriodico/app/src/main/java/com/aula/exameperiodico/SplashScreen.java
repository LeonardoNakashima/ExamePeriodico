package com.aula.exameperiodico;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_splash_screen2);

            VideoView videoView = findViewById(R.id.videoView);

            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video);
            videoView.setVideoURI(videoUri);

            videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
            });


            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                finish();
            }, 3500); //

            videoView.start();
        }

}