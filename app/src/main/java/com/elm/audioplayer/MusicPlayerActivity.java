package com.elm.audioplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.elm.audioplayer.model.Song;
import com.elm.audioplayer.model.myMediaPlayer;
import com.elm.audioplayer.services.BackgroundService;
import com.elm.audioplayer.services.ForegroundService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MusicPlayerActivity extends AppCompatActivity {
    TextView title,currentTime,totalTime;
    SeekBar seekBar;
    ImageView pausePlayerBtn,nextBtn,previousBtn , musicIcon;
    ArrayList<Song> songsList;
    Song currentSong;
    MediaPlayer mediaPlayer = myMediaPlayer.getInstance();
    int x = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        title = findViewById(R.id.song_title);
        currentTime = findViewById(R.id.current_time);
        totalTime = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seekBar);
        pausePlayerBtn = findViewById(R.id.pause_play);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon_bg);

        title.setSelected(true);
        songsList = (ArrayList<Song>)getIntent().getSerializableExtra("LIST");


        setResourcesWithMusic();
       MusicPlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer!=null){
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTime.setText(convertToMMSS(mediaPlayer.getCurrentPosition()+""));

                    if (mediaPlayer.isPlaying()){
                        pausePlayerBtn.setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                        musicIcon.setRotation(x++);

                    }else{
                        pausePlayerBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                        musicIcon.setRotation(0);

                    }
                }

                new Handler().postDelayed(this,100);
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null &&fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        pausePlayerBtn.setOnClickListener(v -> pauseMusic());
        nextBtn.setOnClickListener(v -> playNextMusic());
        previousBtn.setOnClickListener(v -> playPerviousMusic());

        playMusic();



    }

    private void playMusic(){
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
            workFgService();
            workBgService();

        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void pauseMusic(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            Intent fg = new Intent(this,ForegroundService.class);
            stopService(fg);

            Intent bg = new Intent(this,BackgroundService.class);
            stopService(bg);
        }else{
            mediaPlayer.start();
            workFgService();
            workBgService();
        }

    }
    private void playNextMusic(){
        if(myMediaPlayer.currentIndex ==songsList.size()-1)
            return;

        myMediaPlayer.currentIndex+=1;
        mediaPlayer.reset();
        setResourcesWithMusic();
        playMusic();
        workFgService();
        workBgService();
        

    }
    private void playPerviousMusic(){
        if(myMediaPlayer.currentIndex ==0)
            return;

        myMediaPlayer.currentIndex-=1;
        mediaPlayer.reset();
        setResourcesWithMusic();
        playMusic();
        workFgService();
        workBgService();
    }





    void setResourcesWithMusic(){
        currentSong = songsList.get(myMediaPlayer.currentIndex);
        title.setText(currentSong.getTitle());
        totalTime.setText(convertToMMSS(currentSong.getDuration()));

    }

    public static String convertToMMSS(String duration){
        Long millies = Long.parseLong(duration);
        return String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millies)%TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millies)%TimeUnit.MINUTES.toSeconds(1));
    }

    void workFgService(){
        Intent fg = new Intent(this,ForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(fg);
        }else{
            startService(fg);
        }

    }
    void workBgService(){
        Intent bg = new Intent(this,BackgroundService.class);
        startService(bg);
    }
}