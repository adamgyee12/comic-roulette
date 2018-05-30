package com.example.adamgyee.comicstripbuilder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.FileInputStream;

public class OnlineFinalStrip extends AppCompatActivity {

    private LinearLayout mViewingPanel;
    private String mComicID;
    private int mComicSize;

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
            OnlineFinalStrip.this.finish();
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
        OnlineFinalStrip.this.finish();
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
        OnlineFinalStrip.this.finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_final_strip);

        mViewingPanel = (LinearLayout) findViewById(R.id.viewing_panel);

        Intent intent = getIntent();
        mComicSize = intent.getIntExtra("comicSize", 4);
        mComicID = intent.getStringExtra("comicID");

        // get http:localhost/getEntireComic?comicid=x

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getString(R.string.get_entire_comic_url, mComicID);

        // Request a json response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mComicID = response.get(getString(R.string.mComicId_string)).toString();
                            //mPrevFrame = response.get(getString(R.string.mPrevFrame_string)).toString();
                            //mCompletedFrames = response.get(getString(R.string.mCompletedFrames_string)).toString();
                            int completed = (int) response.get(getString(R.string.mCompletedFrames_string));
                            for (int i = 0; i < completed; i++){
                                String getFrameString = "f" + i;
                                String getFrame = response.get(getFrameString).toString();
                                try {
                                    if (getFrame != "null") {
                                        byte[] imageAsBytes = Base64.decode(getFrame.getBytes(), Base64.DEFAULT);
                                        ImageView imageView = new ImageView(getApplicationContext());
                                        Bitmap bitmap = (BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
                                        imageView.setImageBitmap(bitmap.createScaledBitmap(bitmap, 500, 500, false));
                                        imageView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.layout_border));
                                        mViewingPanel.addView(imageView);

                                    }
                                } catch (Exception e){
                                    Log.e("Exception", e.toString());
                                }

                            }

                        } catch (Exception e) {
                            Log.e("error", e.toString());
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Volley Error ", error.toString());

                    }
                });

        queue.add(jsObjRequest);

    }

    private void getExternalStoragePermission(){
        // Get permissions
        if (ContextCompat.checkSelfPermission(OnlineFinalStrip.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(OnlineFinalStrip.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(OnlineFinalStrip.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }
}
