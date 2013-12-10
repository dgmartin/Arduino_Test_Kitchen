package com.danmartin.atk.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.danmartin.atk.R;
import com.danmartin.atk.threads.UsbReadThread;
import com.danmartin.atk.threads.UsbWriteThread;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SliderActivity extends Activity implements SeekBar.OnSeekBarChangeListener, UsbReadThread.UsbReadCallback {

    public final int MAX_VALUE = 255;
    ToggleButton mIsOn;
    SeekBar mRed;
    SeekBar mGreen;
    SeekBar mBlue;

    TextView mRedFeed;
    TextView mGreenFeed;
    TextView mBlueFeed;

    int mRedVal = 0;
    int mGreenVal = 0;
    int mBlueVal = 0;

    UsbAccessory mAccessory;
    UsbAccessory mDevice;
    ParcelFileDescriptor mFileDescriptor;
    BufferedInputStream mInputStream;
    BufferedOutputStream mOutputStream;

    UsbWriteThread mWriter;
    UsbReadThread mReader;


    private static final String USB_ACCESSORY_ATTACHED = "android.hardware.usb.action.USB_ACCESSORY_ATTACHED";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("ATK", "ACTION:  " + action);
            if (USB_ACCESSORY_ATTACHED.equals(action)) {
                mIsOn.setChecked(true);
//                openAccessory();
            } else if (action.equalsIgnoreCase(USB_DEVICE_ATTACHED)) {
                mDevice = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

            } else if (action.equalsIgnoreCase(ACTION_USB_PERMISSION)) {
                synchronized (this) {
                    UsbAccessory accesory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (accesory != null) {
                            mAccessory = accesory;
                            openAccessory();
                        }
                    } else {
                        Log.d("ATK", "permission denied for device " + accesory);
                    }
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(android.hardware.usb.UsbManager.EXTRA_ACCESSORY);
                if (accessory != null) {
                    detachAccessory();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mIsOn = (ToggleButton) findViewById(R.id.ison);
        mIsOn.setChecked(mDevice != null);
        mRed = (SeekBar) findViewById(R.id.red);
        mRed.setOnSeekBarChangeListener(this);
        mGreen = (SeekBar) findViewById(R.id.green);
        mGreen.setOnSeekBarChangeListener(this);
        mBlue = (SeekBar) findViewById(R.id.blue);
        mBlue.setOnSeekBarChangeListener(this);

        mRedFeed = (TextView) findViewById(R.id.fbred);
        mGreenFeed = (TextView) findViewById(R.id.fbgreen);
        mBlueFeed = (TextView) findViewById(R.id.fbblue);


        startReceiver();

        openAccessory();
    }

    private void openAccessory() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (mAccessory == null) {
            UsbAccessory[] accessoryList = manager.getAccessoryList();
            if (accessoryList != null && accessoryList.length > 0) {
                mAccessory = accessoryList[0];
            }
        }

        if (mAccessory != null) {
            Log.d("ATK", "openAccessory: " + mAccessory);
            mFileDescriptor = manager.openAccessory(mAccessory);
            if (mFileDescriptor != null) {
                FileDescriptor fd = mFileDescriptor.getFileDescriptor();

                FileInputStream fis = new FileInputStream(fd);
//            mInputStream = new DataInputStream(fis);
                mInputStream = new BufferedInputStream(fis);
                mReader = new UsbReadThread();
                mReader.init(mInputStream, this);
                mReader.start();

                FileOutputStream fos = new FileOutputStream(fd);
//                mOutputStream = new BufferedWriter(new OutputStreamWriter(fos));
                BufferedOutputStream buf = new BufferedOutputStream(fos);
                mWriter = new UsbWriteThread();
//                mWriter.init(buf);
                mWriter.init(fos);
                mWriter.start();
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        float percent = (float) (progress / 100f);
        int val = (int) (MAX_VALUE * percent);
        if (seekBar == mRed) {
            mRedVal = val;
        } else if (seekBar == mGreen) {
            mGreenVal = val;
        } else if (seekBar == mBlue) {
            mBlueVal = val;
        }
        sendPayload();
    }

    public void sendPayload() {
        if (mWriter != null) {
//            String payload = mRedVal + "," + mGreenVal + "," + mBlueVal + "\n";

            byte[] payload = new byte[4];
            payload[0] = (byte) mRedVal;
            payload[1] = (byte) mGreenVal;
            payload[2] = (byte) mBlueVal;
            payload[3] = 13;
            mWriter.notify(payload);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
//        int progress = seekBar.getProgress();
//        float percent = (float) (progress / 100f);
//        int val = (int) (MAX_VALUE * percent);
//        if (seekBar == mRed) {
//            mRedVal = val;
//        } else if (seekBar == mGreen) {
//            mGreenVal = val;
//        } else if (seekBar == mBlue) {
//            mBlueVal = val;
//        }
////        if (mUSBRunHandle != null) {
//        if (mWriter != null) {
//            String payload = mRedVal + "," + mGreenVal + "," + mBlueVal + "\n";
//            mWriter.notify(payload);
//        }
////        }
    }

    @Override
    public void recieved(final byte[] results) {
        if (results != null && results.length >= 3) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRedFeed != null)
                        mRedFeed.setText(String.valueOf(unsignedToBytes(results[0])));
                    if (mGreenFeed != null)
                        mGreenFeed.setText(String.valueOf(unsignedToBytes(results[1])));
                    if (mBlueFeed != null)
                        mBlueFeed.setText(String.valueOf(unsignedToBytes(results[2])));
                }
            });


//            mRedFeed.setText(results[0]);
//            mGreenFeed.setText((results[1]));
//            mBlueFeed.setText((results[2]));
//            if ((Character.getNumericValue(results[0]) != mRedVal
//                    || Character.getNumericValue(results[1]) != mGreenVal
//                    || Character.getNumericValue(results[2]) != mBlueVal)) {
//                sendPayload();
//            }
        }
    }

    public static int unsignedToBytes(byte b) {
        return b & 0xFF;
    }

    public void detachAccessory() {
        if (mWriter != null) {
            mWriter.close();
        }
        if (mReader != null) {
            mReader.close();
        }
        mAccessory = null;
    }

    protected void startReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(USB_ACCESSORY_ATTACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }
}