package com.segway.fesample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.google.gson.Gson;
import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.base.log.Logger;
import com.segway.robot.sdk.vision.Vision;
import com.segway.robot.sdk.vision.frame.Frame;
import com.segway.robot.sdk.vision.stream.StreamInfo;
import com.segway.robot.sdk.vision.stream.StreamType;

/**
 * This activity shows how to use fisheye stream on your loomo.
 */
public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "fesample";
    // just for log
    Gson gson = new Gson();

    Vision mVision;
    ImageView mImageView;
    Bitmap fishEyeBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        /*
            make sure to obtain the instance of Vision and call bindService.
            the service is successfully binded when callback onBind(), call getActivatedStreamInfo in onBind().
         */
        mVision = Vision.getInstance();
        mVision.bindService(this, new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {
                Logger.i(TAG, "onbind.");
                if (mVision != null) {
                    StreamInfo[] activatedStreamInfo = mVision.getActivatedStreamInfo();
                    Logger.i(TAG, " activatedStreamInfo :" + activatedStreamInfo.length);
                    for (StreamInfo info : activatedStreamInfo) {
                        if (info != null && info.getStreamType() == StreamType.FISH_EYE) {
                            Logger.i(TAG, gson.toJson(info));
                            fishEyeBitmap = Bitmap.createBitmap(info.getWidth(), info.getHeight()
                                    , Bitmap.Config.ALPHA_8);
                        }
                    }
                } else {
                    Logger.e(TAG, "mVision is null.");
                }
            }

            @Override
            public void onUnbind(String reason) {
                Logger.i(TAG, "onUnbind.");

            }
        });


    }

    private void initViews() {
        mImageView = findViewById(R.id.fisheye_iv);
        ((Switch) findViewById(R.id.fisheye_sw)).setOnCheckedChangeListener(this);
    }


    void fishEyeSwitch(boolean checked) {
        if (checked) {
            for (StreamInfo streamInfo : mVision.getActivatedStreamInfo()) {
                if (streamInfo.getStreamType() != StreamType.FISH_EYE) {
                    continue;
                }
                /*
                *  call com.segway.robot.sdk.vision.Vision.startListenFrame to listen frame by the streamtype you passed in. call com.segway.robot.sdk.vision.Vision.stopListenFrame when you don't need frame any longer.
                *  the stream type should be activated by default
                * */
                mVision.startListenFrame(StreamType.FISH_EYE, new Vision.FrameListener() {
                    @Override
                    public void onNewFrame(int streamType, Frame frame) {
                        if (frame != null) {
                            fishEyeBitmap.copyPixelsFromBuffer(frame.getByteBuffer());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mImageView.setImageBitmap(fishEyeBitmap);
                                }
                            });
                        }
                    }
                });
                return;
            }
        } else {
            mVision.stopListenFrame(StreamType.FISH_EYE);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.fisheye_sw) {
            fishEyeSwitch(isChecked);
        }
    }
}
