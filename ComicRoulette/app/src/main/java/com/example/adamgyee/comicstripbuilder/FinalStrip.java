package com.example.adamgyee.comicstripbuilder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class FinalStrip extends AppCompatActivity {

    private int mNumArtists;
    private LinearLayout mViewingPanel;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        getExternalStoragePermission();

        // Show download option in ActionBar
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveComicToGallery();
            return true;
        } else if (id == R.id.action_home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            FinalStrip.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean saveComicToGallery(){

        LinearLayout layout = (LinearLayout)findViewById(R.id.viewing_panel);

        // combine all bitmaps into a single strip to save
        Bitmap combined = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < layout.getChildCount(); i++) {
            ImageView v = (ImageView) layout.getChildAt(i);
            if (((BitmapDrawable)v.getDrawable()).getBitmap() != null && !((BitmapDrawable)v.getDrawable()).getBitmap().isRecycled()) {

                // Add border around images, then combine them
                Bitmap bitmap = ((BitmapDrawable)v.getDrawable()).getBitmap();
                RectF targetRect = new RectF(0, 0, bitmap.getWidth()+5, bitmap.getHeight()+5);
                Bitmap dest = Bitmap.createBitmap(bitmap.getWidth()+20, bitmap.getHeight()+20, bitmap.getConfig());
                Canvas canvas = new Canvas(dest);
                canvas.drawColor(Color.BLACK);
                canvas.drawBitmap(bitmap, null, targetRect, null);
                combined = combineImages(combined, dest);
            }
        }

        // Save to local gallery
        MediaStore.Images.Media.insertImage(getContentResolver(), combined, "ComicRoulette Image", "Created with ComicRoulette");
        Toast.makeText(getApplicationContext(), "Saved comic to gallery", Toast.LENGTH_SHORT).show();

        return true;
    }

    public Bitmap combineImages(Bitmap c, Bitmap s) {
        Bitmap cs = null;
        int width, height = 0;

        width = s.getWidth();
        height = c.getHeight()+s.getHeight();

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(cs);
        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, 0f, c.getHeight(), null);
        return cs;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        FinalStrip.this.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();

        LinearLayout layout = (LinearLayout)findViewById(R.id.viewing_panel);

        for (int i = 0; i < layout.getChildCount(); i++) {
            ImageView v = (ImageView) layout.getChildAt(i);
            if (((BitmapDrawable)v.getDrawable()).getBitmap() != null && !((BitmapDrawable)v.getDrawable()).getBitmap().isRecycled()) {
                ((BitmapDrawable) v.getDrawable()).getBitmap().recycle();
            }
            v.setImageBitmap(null);
        }
        FinalStrip.this.finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_strip);

        mViewingPanel = (LinearLayout) findViewById(R.id.viewing_panel);

        Intent intent = getIntent();
        mNumArtists = intent.getIntExtra("numArtists", 3);

        // Retrieve bitmaps from file to display full strip
        for (int i = 0; i < mNumArtists; i++){

            try {
                ImageView imageView = new ImageView(getApplicationContext());
                FileInputStream is = this.openFileInput("image" + i + ".png");
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                imageView.setImageBitmap(bitmap);
                imageView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.layout_border));
                mViewingPanel.addView(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getExternalStoragePermission(){
        // Get permissions
        if (ContextCompat.checkSelfPermission(FinalStrip.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(FinalStrip.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(FinalStrip.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }
}
