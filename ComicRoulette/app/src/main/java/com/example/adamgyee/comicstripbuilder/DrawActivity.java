package com.example.adamgyee.comicstripbuilder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.simplify.ink.InkView;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Stack;

public class DrawActivity extends AppCompatActivity {

    private InkView mInk;
    private int mCount;
    private int mNumArtists;
    private Button mFinished, mUndo;
    private Button mBlue, mBlack, mWhite, mRed, mYellow, mGreen;
    private ArrayList<Bitmap> mBitmaps = new ArrayList<Bitmap>();
    Bitmap mUndoBitmap = null;

    @Override
    public void onBackPressed() {
        // Don't want users to mess up their masterpieces!
    }

    @Override
    protected void onStop() {
        super.onStop();

        for (int i = 0; i < mBitmaps.size(); i++) {
            if (mBitmaps.get(i) != null && !mBitmaps.get(i).isRecycled()) {
                mBitmaps.get(i).recycle();
            }
        }
        mBitmaps.clear();

        // Reset all variables
        // TODO: save state on close and restore on open
        ImageView imageView = (ImageView) findViewById(R.id.preview);
        imageView.setImageBitmap(null);
        mCount = 0;
        getSupportActionBar().setTitle(getResources().getString(R.string.frame_num, mCount+1, mNumArtists));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        mCount = 0;

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

        Intent intent = getIntent();
        mNumArtists = intent.getIntExtra("numArtists", 3);

        getSupportActionBar().setTitle(getResources().getString(R.string.frame_num, mCount+1, mNumArtists));

        mFinished = (Button) findViewById(R.id.finished_drawing_btn);
        mFinished.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frameDone(mCount);
            }
        });

        setUpButtonListeners();
        mBlack.callOnClick();
    }

    private void frameDone(int currentCount){

        final InkView ink = (InkView) findViewById(R.id.ink);

        // Grab image of canvas
        Bitmap current_drawing = ink.getBitmap(getResources().getColor(android.R.color.white)); // Grab image of canvas

        // Save image to bitmap
        mBitmaps.add(current_drawing);
        saveBitmap(current_drawing, currentCount);

        if (currentCount == mNumArtists-1){
            // End state of comic
            Intent nextIntent = new Intent(this, FinalStrip.class);
            nextIntent.putExtra("numArtists", mNumArtists);
            startActivity(nextIntent);
            DrawActivity.this.finish();
        } else {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.pass_image1);
            loadPhoto(imageView, 100, 100);
            getSupportActionBar().setTitle(getResources().getString(R.string.frame_num, mCount+2, mNumArtists)); // Set action bar to current frame number
            mBlack.callOnClick(); // Set ink color back to black
            setPrevious(current_drawing); // Set mini-display to the current drawing, then clear the canvas for the next drawing
            clearCanvas();
            mCount++;
        }
    }

    public String saveBitmap(Bitmap bitmap, int i) {

        String fileName = "image" + i + ".png";
        try {
            FileOutputStream stream = this.openFileOutput(fileName, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
        return fileName;
    }

    private void setPrevious(Bitmap current_drawing){
        // Show previous bitmap
        ImageView imageView = (ImageView) findViewById(R.id.preview);
        imageView.setImageBitmap(current_drawing);
    }

    private void clearCanvas(){
        final InkView ink = (InkView) findViewById(R.id.ink);
        ink.clear();
    }

    private void loadPhoto(ImageView imageView, int width, int height) {

        ImageView tempImageView = imageView;

        AlertDialog.Builder imageDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        View layout = inflater.inflate(R.layout.popup_instructions,
                (ViewGroup) findViewById(R.id.layout_root));
        ImageView image = (ImageView) layout.findViewById(R.id.fullimage);
        image.setImageDrawable(tempImageView.getDrawable());
        imageDialog.setView(layout);
        imageDialog.setPositiveButton(getString(R.string.action_done), new DialogInterface.OnClickListener(){

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        imageDialog.create();
        imageDialog.show();
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
