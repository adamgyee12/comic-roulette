package com.example.adamgyee.comicstripbuilder;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.simplify.ink.InkView;

public class MainActivity extends AppCompatActivity {

    private ImageButton mNewStrip, mOnlineRoulette;

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        // Get permissions
        getExternalStoragePermission();

        mNewStrip = (ImageButton) findViewById(R.id.new_strip_btn);
        mNewStrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            // Prompt spinner and send to drawactivity
            loadPopup();

            }
        });

        mOnlineRoulette = (ImageButton) findViewById(R.id.online_btn);
        mOnlineRoulette.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getInternetPermissions();

                Intent nextIntent = new Intent(MainActivity.this, OnlineDrawActivity.class);
                MainActivity.this.startActivity(nextIntent);
                MainActivity.this.finish();
            }
        });
    }

    private void loadPopup() {

        AlertDialog.Builder imageDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        final View layout = inflater.inflate(R.layout.popup_num_artists,
                (ViewGroup) findViewById(R.id.layout_root));

        imageDialog.setView(layout);
        imageDialog.setPositiveButton(getString(R.string.action_done), new DialogInterface.OnClickListener(){

            public void onClick(DialogInterface dialog, int which) {

            //numArtistSpinner.getSelectedItem().toString();
            RadioGroup radioGroup = (RadioGroup) layout.findViewById(R.id.num_artsts_rdio);
            int selectedId = radioGroup.getCheckedRadioButtonId();

            // find the radiobutton by returned id
            RadioButton radioButton = (RadioButton) layout.findViewById(selectedId);

            int numArtistsInt = Integer.parseInt(radioButton.getText().toString());
            dialog.dismiss();
            Intent nextIntent = new Intent(MainActivity.this, DrawActivity.class);
            nextIntent.putExtra("numArtists", numArtistsInt);
            MainActivity.this.startActivity(nextIntent);
            MainActivity.this.finish();
            }
        });

        imageDialog.create();
        imageDialog.show();
    }

    private void getInternetPermissions(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.INTERNET)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.INTERNET},
                        1);
            }
        }
    }

    private void getExternalStoragePermission(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }
}
