package com.example.adamgyee.comicstripbuilder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.simplify.ink.InkView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OnlineDrawActivity extends AppCompatActivity {

    final private int mStripLength = 4;
    private Button mFinished, mUndo;
    private InkView mInk;
    private Button mBlue, mBlack, mWhite, mRed, mYellow, mGreen;
    private String mComicID, mPrevFrame, mCompletedFrames;
    private Bitmap mUndoBitmap = null;

    @Override
    public void onBackPressed() {
        // Don't want users to mess up their masterpieces!
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_draw);

        getOnlineComic();

        mInk = (InkView) findViewById(R.id.ink);
        mUndo = (Button) findViewById(R.id.undo_btn);
        mUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUndoBitmap != null){
                    Bitmap tempBitmap = mInk.getBitmap();
                    mInk.clear();
                    Paint p = new Paint();
                    mInk.drawBitmap(mUndoBitmap,0,0,p);
                    mUndoBitmap = tempBitmap;
                }
            }
        });

        mInk.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    mUndoBitmap = mInk.getBitmap();
                    mInk.onTouchEvent(event);
                } else{
                    mInk.onTouchEvent(event);
                }
                return true;
            }
        });

        mFinished = (Button) findViewById(R.id.finished_drawing_btn);
        mFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishOnlineFrame();
            }
        });

        setUpButtonListeners();
        mBlack.callOnClick();
    }

    private void getOnlineComic(){

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = getString(R.string.get_comic_url);

        // Request a json response from the provided URL.
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
            (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    try {
                        mComicID = response.get(getString(R.string.mComicId_string)).toString();
                        mPrevFrame = response.get(getString(R.string.mPrevFrame_string)).toString();
                        mCompletedFrames = response.get(getString(R.string.mCompletedFrames_string)).toString();
                        int completed = (int) response.get(getString(R.string.mCompletedFrames_string));

                        // Decode previous image and set it to the preview box
                        byte[] imageAsBytes = Base64.decode(mPrevFrame.getBytes(), Base64.DEFAULT);
                        setPrevious(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));

                        // Set actionbar to reflect which frame user is editing
                        getSupportActionBar().setTitle(getResources().getString(R.string.frame_num, completed+1, mStripLength));

                    } catch (Exception e) {
                        Log.e("error", e.toString());
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Volley Error ", error.toString());
                    Toast.makeText(OnlineDrawActivity.this, "Cannot connect to server", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OnlineDrawActivity.this, MainActivity.class);
                    startActivity(intent);
                    OnlineDrawActivity.this.finish();

                }
            });

        queue.add(jsObjRequest);
    }

    private void finishOnlineFrame(){

        final InkView ink = (InkView) findViewById(R.id.ink);

        // Grab image of canvas
        Bitmap current_drawing = Bitmap.createScaledBitmap(ink.getBitmap(getResources().getColor(android.R.color.white)), 120, 120, false);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        current_drawing.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final byte[] imageBytes = baos.toByteArray();
        final String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        // make call to save online comic, send encodedImage, ID, and current frame
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = getString(R.string.post_comic_url);
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        Intent intent = new Intent(OnlineDrawActivity.this, OnlineFinalStrip.class);
                        intent.putExtra("comicID", mComicID);
                        intent.putExtra("comicSize", mStripLength);
                        startActivity(intent);
                        OnlineDrawActivity.this.finish();

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                // TODO Check if these exist before sending them
                params.put(getString(R.string.frame_string), encodedImage);
                params.put(getString(R.string.mComicId_string), mComicID);
                params.put(getString(R.string.putFrame_string), mCompletedFrames);
                return params;
            }
        };
        queue.add(postRequest);
    }

    private void setPrevious(Bitmap current_drawing){
        // Show previous bitmap
        ImageView imageView = (ImageView) findViewById(R.id.preview);
        imageView.setImageBitmap(current_drawing);
    }

    private void setUpButtonListeners(){

        final InkView ink = (InkView) findViewById(R.id.ink);

        mBlue = (Button) findViewById(R.id.blue_btn);
        mBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ink.setColor(getResources().getColor(android.R.color.holo_blue_light));
                ink.setMinStrokeWidth(1.5f);
                ink.setMaxStrokeWidth(6f);
            }
        });
        mBlack = (Button) findViewById(R.id.black_btn);
        mBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ink.setColor(getResources().getColor(android.R.color.black));
                ink.setMinStrokeWidth(1.5f);
                ink.setMaxStrokeWidth(6f);
            }
        });
        mWhite = (Button) findViewById(R.id.white_btn);
        mWhite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ink.setColor(getResources().getColor(android.R.color.white));
                ink.setMinStrokeWidth(10f);
                ink.setMaxStrokeWidth(16f);
            }
        });
        mYellow = (Button) findViewById(R.id.yellow_btn);
        mYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ink.setColor(getResources().getColor(android.R.color.holo_orange_light));
                ink.setMinStrokeWidth(1.5f);
                ink.setMaxStrokeWidth(6f);
            }
        });
        mGreen = (Button) findViewById(R.id.green_btn);
        mGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ink.setColor(getResources().getColor(android.R.color.holo_green_dark));
                ink.setMinStrokeWidth(1.5f);
                ink.setMaxStrokeWidth(6f);
            }
        });
        mRed = (Button) findViewById(R.id.red_btn);
        mRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ink.setColor(getResources().getColor(android.R.color.holo_red_dark));
                ink.setMinStrokeWidth(1.5f);
                ink.setMaxStrokeWidth(6f);
            }
        });
    }
}
