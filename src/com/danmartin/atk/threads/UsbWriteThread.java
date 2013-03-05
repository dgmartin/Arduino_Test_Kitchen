package com.danmartin.atk.threads;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: danmartin
 * Date: 2/14/13
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class UsbWriteThread extends Thread {

    private boolean mRun = true;
    private boolean mChangeMade = true;

    private String mPayload;

    UsbDeviceConnection mConnection;
    UsbEndpoint mEndpointOut;
    UsbWriteCallback mListener;

    public UsbWriteThread() {
        super("UsbWriter");
    }

    public UsbWriteThread(String threadName) {
        super(threadName);
    }

    public void init(UsbDeviceConnection connection, UsbEndpoint endpoint_out, UsbWriteCallback listener) {
        mConnection = connection;
        mEndpointOut = endpoint_out;
        mListener = listener;
    }

    public void notify(String payload) {
        mPayload = payload;
        mChangeMade = true;
        synchronized (this) {
            this.notify();
        }
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                UsbRequest request = new UsbRequest();
                request.initialize(mConnection, mEndpointOut);
                while (mRun) {
                    while (mChangeMade) {
                        mChangeMade = false;
                        ByteBuffer bufferOut;
//                        bufferOut = BufferConverter.str_to_bb(mPayload);
                        bufferOut = ByteBuffer.allocate(mPayload.getBytes().length);
                        bufferOut.clear();
                        bufferOut.put(mPayload.getBytes());
                        Log.e("ATK", "BufferOut Before: " + bufferOut(bufferOut));
                        request.queue(bufferOut, bufferOut.limit());

                        mListener.sent(true);
//                        UsbRequest result = mConnection.requestWait();
//                        if (result == request && mListener != null) {
//                            mListener.sent(true);
//                        }
                    }
                    this.wait();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String bufferOut(ByteBuffer buf) {
        String bufString = "";
        byte[] ray = buf.array();
        for (byte bite : ray) {
            bufString += bite + ", ";
        }
        return bufString;
    }

    public interface UsbWriteCallback {
        public void sent(boolean sent);
    }
}

