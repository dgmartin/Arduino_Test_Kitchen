package com.danmartin.atk.threads;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: danmartin
 * Date: 2/14/13
 * Time: 12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class UsbReadThread extends Thread{

    private boolean mRun =true;

    UsbDeviceConnection mConnection;
    UsbEndpoint mEndpointIn;
    UsbReadCallback mListener;

    public UsbReadThread() {
        super("UsbReader");
    }

    public UsbReadThread(String threadName) {
        super(threadName);
    }
    public void init(UsbDeviceConnection connection,UsbEndpoint endpoint_in,UsbReadCallback listener){
        mConnection=connection;
        mEndpointIn=endpoint_in;
        mListener = listener;
    }

    @Override
    public void run() {
//        synchronized (this) {
            UsbRequest inRequest = new UsbRequest(); // URB for the incoming data
            inRequest.initialize(mConnection, mEndpointIn); // the direction is dictated by this initialisation to the incoming endpoint.
            ByteBuffer bufferIn = ByteBuffer.allocate(8);

            while (mRun) {
                bufferIn.clear();
                    if (inRequest.queue(bufferIn, 8)) {
                        mConnection.requestWait(); // wait for this request to be completed
                        // at this point buffer contains the data received

                } else {
                    Log.e("ATK", "Request Failed");
                }
                CharBuffer cBuf = bufferIn.asCharBuffer();
                Log.e("ATK", "BufferIn: " + bufferIn.array().toString());
                String input = bufferOut(bufferIn);
                Log.e("ATK", "Input is: " + input);

//                if(mListener!=null){
//                    mListener.recieved(bufferIn.array());
//                }

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
//        }
    }
    private String bufferOut(ByteBuffer buf) {
        String bufString = "";
        byte[] ray = buf.array();
        for (byte bite : ray) {
            bufString += bite + ", ";
        }
        return bufString;
    }
    public interface UsbReadCallback {
        public void recieved(byte[] results);
    }
}
