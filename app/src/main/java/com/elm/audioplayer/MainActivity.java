package com.elm.audioplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.elm.audioplayer.adapter.MusicAdapter;
import com.elm.audioplayer.model.Song;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView rv;
    TextView noMusicText;
    ArrayList<Song> songList = new ArrayList<>();
    MusicAdapter musicAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Inflate the layout for this fragment

        rv = findViewById(R.id.songs_rv);
        noMusicText = findViewById(R.id.no_songs_text);

        String[] songDetails = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC+" !=0";


        //check if permission accepted or not
        if (checkPermissionStorage() == false) {
            requestPermissionStorage();

        }

        //get audios from storage

        Cursor cursor =getContentResolver()
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,songDetails,selection,null,null);

        while (cursor.moveToNext()){
            Song song = new Song(cursor.getString(1),cursor.getString(0),cursor.getString(2));
            if(new File(song.getPath()).exists())
                songList.add(song);
        }



        if (songList.size()==0)
            noMusicText.setVisibility(View.VISIBLE);
        else
            rv.setLayoutManager(new LinearLayoutManager(this));
        musicAdapter = new MusicAdapter(songList,this);
        rv.setAdapter(musicAdapter);
    }


    void requestPermissionStorage(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            Toast.makeText(this, "Please Allow permission", Toast.LENGTH_SHORT).show();
        }else{
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},123);

        }

    }


    boolean checkPermissionStorage(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED)
            return  true;
        else
            return false;
    }

}