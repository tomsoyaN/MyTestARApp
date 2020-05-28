package nyarapp.tom_soya.com.nyarapplication;

import android.content.res.AssetManager;
import android.gesture.Gesture;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import javax.microedition.khronos.opengles.GL10;

import jp.androidgroup.nyartoolkit.markersystem.NyARAndMarkerSystem;
import jp.androidgroup.nyartoolkit.markersystem.NyARAndSensor;
import jp.androidgroup.nyartoolkit.sketch.AndSketch;
import jp.androidgroup.nyartoolkit.utils.camera.CameraPreview;
import jp.androidgroup.nyartoolkit.utils.gl.AndGLBox;
import jp.androidgroup.nyartoolkit.utils.gl.AndGLView;
import jp.nyatla.nyartoolkit.markersystem.NyARMarkerSystemConfig;
import java.lang.String;

public class ARActivity extends AndSketch implements AndGLView.IGLFunctionEvent {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
    }

    CameraPreview mCamPreview;
    AndGLView mAndGLView;
    Camera.Size mCamSize;
    final public static String TAG = "NyARAppDebug";

    GestureDetector gestureDetector;
    JSONObject json;
    String word = "Screen Touched";

    SoundPool mSoundPool;
    int mSoundID;


    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG,"Initialize Camera");
        FrameLayout fr = ((FrameLayout) this.findViewById(R.id.mainLayout));
        this.mCamPreview = new CameraPreview(this);
        this.mCamSize = this.mCamPreview.getRecommendPreviewSize(320,240);
        Log.d(TAG,"Complete");

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int h = dm.heightPixels;
        int screen_w,screen_h;
        screen_w = (this.mCamSize.width*h/this.mCamSize.height);
        screen_h = h;

        Log.d(TAG,"Set Preview");
        fr.addView(mCamPreview,0,new FrameLayout.LayoutParams(screen_w,screen_h));
        this.mAndGLView = new AndGLView(this);
        fr.addView(mAndGLView,0,new FrameLayout.LayoutParams(screen_w,screen_h));
        Log.d(TAG,"Complete");

        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();
        mSoundPool = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .setMaxStreams(1)
                .build();
        mSoundID = mSoundPool.load(this,R.raw.se,1);

        gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e){
                GetAsyncTask getTask = new GetAsyncTask();
                PostAsyncTask postTask = new PostAsyncTask();
                getTask.setListener(new GetAsyncTask.Listener() {
                    @Override
                    public void onSuccess(String str) {
                        try{
                            json = new JSONObject(str);
                            AlertFromJson(json,"Text");
                        }
                        catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                });
                getTask.execute();
                postTask.execute(word);
                mSoundPool.play(mSoundID,1.0f,1.0f,0,0,1);
                return true;
            }
        });


    }


    NyARAndSensor mNySen;
    NyARAndMarkerSystem mMarkerSys;
    private  ID[] mId;
    AndGLBox box;
    final int MIN_ID = 1;
    final int MAX_ID = 30;
    @Override
    public void setupGL(GL10 i_gl) {

        mId = new  ID[MAX_ID-MIN_ID];

        try{
            Log.d(TAG,"Initialize NyARSystem");
            AssetManager assetMng = getResources().getAssets();
            this.mNySen = new NyARAndSensor(this.mCamPreview,this.mCamSize.width,this.mCamSize.height,30);
            this.mMarkerSys = new NyARAndMarkerSystem(new NyARMarkerSystemConfig(this.mCamSize.width,this.mCamSize.height));

            for(int i=0;i<MAX_ID-MIN_ID;i++) {
                mId[i] = new ID();
                this.mId[i].setId(this.mMarkerSys.addNyIdMarker(i,49));
            }

            mNySen.start();
            Log.d(TAG,"Complete");

            i_gl.glMatrixMode(GL10.GL_PROJECTION);
            i_gl.glLoadMatrixf(this.mMarkerSys.getGlProjectionMatrix(),0);
            this.box = new AndGLBox(this.mAndGLView,40);

        }
        catch (Exception e) {

        }
    }

    @Override
    public void drawGL(GL10 i_gl) {
        int c=0;
        try{
            i_gl.glClearColor(0,0,0,0);
            i_gl.glClear(GL10.GL_COLOR_BUFFER_BIT|GL10.GL_DEPTH_BUFFER_BIT);

            synchronized (this.mNySen){
                this.mMarkerSys.update(this.mNySen);
                for(int i=0;i<MAX_ID;i++) {
                    if (this.mMarkerSys.isExistMarker(this.mId[i].getID())) {
                        mId[i].setisExist(true);
                        i_gl.glMatrixMode(GL10.GL_MODELVIEW);
                        i_gl.glLoadMatrixf(this.mMarkerSys.getGlMarkerMatrix(mId[i].getID()), 0);
                        this.box.draw(0, 0, 20);
                        //Log.d(TAG,String.valueOf(mId[i].getID()));
                    } else {
                        mId[i].setisExist(false);
                    }
                    for (ID id : mId) {
                        if (id.isExist) c++;
                    }
                   // Log.d(TAG, "Exists:" + String.valueOf(c));
                }
            }


        }
        catch (Exception e){

        }
    }

    private class ID{
        private int id;
        private boolean isExist;

        public int getID(){
            return this.id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setisExist(boolean exist)
        {
            this.isExist=exist;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        gestureDetector.onTouchEvent(e);
        return true;
    }

    private void AlertFromJson(JSONObject jo,String tag){
        try {
            String message = jo.getString(tag);
            new AlertDialog.Builder(this)
                    .setTitle("Message from Server")
                    .setMessage(message)
                    .setPositiveButton("OK",null)
                    .show();
        }
        catch (JSONException e){
            e.printStackTrace();
        }
    }

}
