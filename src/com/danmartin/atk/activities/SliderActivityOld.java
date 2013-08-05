package com.danmartin.atk.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.android.future.usb.UsbManager;
import com.danmartin.atk.R;
import com.danmartin.atk.utils.BufferConverter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SliderActivityOld extends Activity implements SeekBar.OnSeekBarChangeListener {

    public final ArrayList<String> mLock = new ArrayList<String>();

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
    UsbManager mDevice;
    ParcelFileDescriptor mFileDescriptor;
    DataInputStream mInputStream;
    DataOutputStream mOutputStream;
    UsbDeviceConnection mConnection;
    UsbEndpoint mEndpointOut;
    UsbEndpoint mEndpointIn;
    PendingIntent mPermissionIntent;

    Handler mUSBRunHandle;

    boolean mChangeMade = true;

    private static final String USB_ACCESSORY_ATTACHED = "android.hardware.usb.action.USB_ACCESSORY_ATTACHED";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("ATK", "ACTION:  " + action);
            if (USB_ACCESSORY_ATTACHED.equals(action)) {
                mIsOn.setChecked(true);
                openAccessory();
            } else if (action.equalsIgnoreCase(USB_DEVICE_ATTACHED)) {
                mDevice =  UsbManager.getAccessory(intent);

            } else if (action.equalsIgnoreCase(ACTION_USB_PERMISSION)) {
                mDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            mDevice = device;
                            openDevice();
                        }
                    } else {
                        Log.d("ATK", "permission denied for device " + device);
                    }
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


//        UsbManager manager = UsbManager.getInstance(this);


        IntentFilter filter = new IntentFilter();
        filter.addAction(USB_ACCESSORY_ATTACHED);
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(USB_DEVICE_ATTACHED);
        registerReceiver(mUsbReceiver, filter);

        UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        if (deviceIterator.hasNext()) {
            mDevice = deviceIterator.next();
            Log.e("ATK", "DEVICE:  " + mDevice.getDeviceName() + " Vendor ID: " + mDevice.getVendorId() + "PRODUCT ID: " + mDevice.getProductId());
            mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

            mUsbManager.requestPermission(mDevice, mPermissionIntent);
        }
    }

    private void openAccessory() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbAccessory[] accessoryList = manager.getAccessoryList();
        mAccessory = accessoryList[0];
        Log.d("ATK", "openAccessory: " + mAccessory);
        mFileDescriptor = manager.openAccessory(mAccessory);
        if (mFileDescriptor != null) {
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();

            FileInputStream fis = new FileInputStream(fd);
            mInputStream = new DataInputStream(fis);

            FileOutputStream fos = new FileOutputStream(fd);
            mOutputStream = new DataOutputStream(fos);
            TxCommThread thread = new TxCommThread("CommThread");
            thread.start();
        }
    }

    private void openDevice() {
        Log.e("ATK", "!!!!!!!!!!! Device Found !!!!!!!!!!!!!!");
        if (mDevice != null) {
            UsbManager mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            int count = mDevice.getInterfaceCount();
            UsbInterface intf = mDevice.getInterface(1);
            int endpointCount = intf.getEndpointCount();
            for (int i = 0; i < endpointCount; i++) {
                UsbEndpoint crntEndPoint = intf.getEndpoint(i);
                if (crntEndPoint.getDirection() == UsbConstants.USB_DIR_OUT && mEndpointOut == null) {
                    mEndpointOut = crntEndPoint;
                } else if (crntEndPoint.getDirection() == UsbConstants.USB_DIR_IN && mEndpointIn == null) {
                    mEndpointIn = crntEndPoint;
                }
                if (mEndpointOut != null && mEndpointIn != null) break;
            }
            mConnection = mUsbManager.openDevice(mDevice);
            if (mConnection != null) {
                mConnection.claimInterface(intf, true);

                if (mUSBRunHandle != null) {
                    mUSBRunHandle.sendEmptyMessage(0);
                } else {
                    TxCommThread thread = new TxCommThread("CommThread");
                    thread.start();
                }
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
//        if (mUSBRunHandle != null) {
        if (!mChangeMade) {
            mChangeMade = true;
//                Message msg = new Message();
//                msg.arg1 = 1;
//                mUSBRunHandle.sendMessage(msg);
            synchronized (mLock) {
                try {
                    mLock.notify();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
//        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        float percent = (float) (progress / 100f);
        int val = (int) (MAX_VALUE * percent);
        if (seekBar == mRed) {
            mRedVal = val;
        } else if (seekBar == mGreen) {
            mGreenVal = val;
        } else if (seekBar == mBlue) {
            mBlueVal = val;
        }
        if (mUSBRunHandle != null) {
            mChangeMade = true;

            Message msg = new Message();
            msg.arg1 = 1;
            mUSBRunHandle.sendMessage(msg);
//                mUSBRunHandle.sendEmptyMessage(0);
        }
    }


    public class TxCommThread extends Thread {


        public TxCommThread() {
            super();
        }

        public TxCommThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            try {
//                Looper.prepare();
//
//                mUSBRunHandle = new Handler() {
//
//                    public void handleMessage(Message msg) {
////                    if (msg.arg1 == 1) {
////                        mInputStream.notify();
//                        communicate();
////                        try {
////                            queue.notify();
////                        } catch (IllegalMonitorStateException e) {
////                            e.printStackTrace();
////                        }
////                    }
//                    }
//                };

                communicate();
//                Looper.loop();
            } catch (Throwable t) {
                Log.e("ATK", "halted due to an error", t);
            }


//            while (mFileDescriptor != null) {
//                try {
//                    if (!mChangeMade)
//                        mInputStream.wait();
//                } catch (InterruptedException e) {
//                    Thread.currentThread().interrupt();
//                }
        }

        //        }
        protected void communicate() {
            synchronized (mLock) {
                while (true) {

                    try {
                        while (mChangeMade) {
                            String myPackage = mRedVal + "," + mGreenVal + "," + mBlueVal + "\n";
                            UsbRequest request = new UsbRequest();
                            request.initialize(mConnection, mEndpointOut);
                            ByteBuffer bufferOut = ByteBuffer.allocate(myPackage.getBytes().length);
                            ByteBuffer bufferIn = ByteBuffer.allocate(8);
                            bufferOut.clear();
                            bufferIn.clear();
                            ByteBuffer tempBuffer = BufferConverter.str_to_bb(myPackage);
                            bufferOut.put(tempBuffer);
                            Log.e("ATK", "BufferOut Before: " + bufferOut(bufferOut));
                            request.queue(bufferOut, tempBuffer.limit());

                            UsbRequest result = mConnection.requestWait();
                            if (result == request) { // wait for confirmation (request was sent)
                                UsbRequest inRequest = new UsbRequest(); // URB for the incoming data
                                inRequest.initialize(mConnection, mEndpointIn); // the direction is dictated by this initialisation to the incoming endpoint.

                                if (inRequest.queue(bufferIn, 8)) {
                                    mConnection.requestWait(); // wait for this request to be completed
                                    // at this point buffer contains the data received
                                }
                                mChangeMade = false;
                            } else {
                                Log.e("ATK", "Request Failed");
                            }
                            Log.e("ATK", "BufferOut After: " + bufferOut.limit());
                            CharBuffer cBuf = bufferIn.asCharBuffer();
                            Log.e("ATK", "BufferIn: " + cBuf.toString());
                            String input = bufferOut(bufferIn);
                            Log.e("ATK", "Input is: " + input);

//                        String input = mInputStream.readLine();


//                if (input != null && !input.equalsIgnoreCase("")) {
//                    final String red = input.substring(0, 1);
//                    final String green = input.substring(2, 3);
//                    final String blue = input.substring(4, 5);
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mRedFeed.setText(getValueString(red));
//                            mGreenFeed.setText(getValueString(green));
//                            mBlueFeed.setText(getValueString(blue));
//                            if (Integer.parseInt(red) != mRedVal || Integer.parseInt(green) != mGreenVal || Integer.parseInt(blue) != mBlueVal)
//                                mChangeMade = true;
//                        }
//                    });
//
//                }
                        }
                        mLock.wait();
                    } catch (InterruptedException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }


        protected String getValueString(String startVal) {
            try {
                String hex = startVal;
                int val = Integer.parseInt(hex, 16);
                int percent = (val / MAX_VALUE) * 100;
                return "Hex: " + hex + " Value: " + val + " Percent: " + percent;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Error";
        }

        private String bufferOut(ByteBuffer buf) {
            String bufString = "";
            byte[] ray = buf.array();
            for (byte bite : ray) {
                bufString += bite + ", ";
            }
            return bufString;
        }
    }


//    public class USBRunner implements Runnable {
//
//        @Override
//        public void run() {
//            Looper.prepare();
//
//            mUSBRunHandle = new Handler() {
//
//                public void handleMessage(Message msg) {
//                    if (msg.arg1 == 1) {
//                        mInputStream.notify();
//                    }
//                }
//            };
//
//            while (mFileDescriptor != null) {
//                if (mChangeMade) {
//                    try {
//                        String myPackage = mRedVal + "," + mGreenVal + "," + mBlueVal + "\n";
//                        mOutputStream.writeChars(myPackage);
//                        mChangeMade = false;
//                    } catch (IOException e) {
//                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//                    }
//                }
//                try {
//                    String input = mInputStream.readLine();
//                    if (input != null && !input.equalsIgnoreCase("")) {
//                        String red = input.substring(0, 1);
//                        mRedFeed.setText(getValueString(red));
//                        String green = input.substring(2, 3);
//                        mGreenFeed.setText(getValueString(green));
//                        String blue = input.substring(4, 5);
//                        mBlueFeed.setText(getValueString(blue));
//                        if (Integer.parseInt(red) != mRedVal || Integer.parseInt(green) != mGreenVal || Integer.parseInt(blue) != mBlueVal)
//                            mChangeMade = true;
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    if (!mChangeMade)
//                        mInputStream.wait();
//                } catch (InterruptedException e) {
////                    Thread.currentThread().interrupt();
//                }
//            }
//        }
//
//        protected String getValueString(String startVal) {
//            String hex = startVal;
//            int val = Integer.parseInt(hex, 16);
//            int percent = (val / MAX_VALUE) * 100;
//            return "Hex: " + hex + " Value: " + val + " Percent: " + percent;
//        }
//    }
}
