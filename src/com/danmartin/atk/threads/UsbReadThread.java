package com.danmartin.atk.threads;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: danmartin
 * Date: 2/14/13
 * Time: 12:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class UsbReadThread extends Thread {

    private boolean mRun = true;

    BufferedInputStream mInputStream;
    UsbReadCallback mListener;

    public UsbReadThread() {
        super("UsbReader");
    }

    public UsbReadThread(String threadName) {
        super(threadName);
    }

    public void init(BufferedInputStream inInputStream, UsbReadCallback listener) {
        mInputStream = inInputStream;
        mListener = listener;
    }

    @Override
    public void run() {
        while (mRun) {
            char[] results = null;
            int count = 0;
            do {
                try {
                    byte[] buffer = new byte[4];
                    count = mInputStream.read(buffer);
                    Log.e("ATK", (int) buffer[0] + ", " + (int)buffer[1] + ", " +(int) buffer[2] + ", " + (int)buffer[3]);
                    if (mListener != null) {
                        mListener.recieved(buffer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } while (count >= 4);
            try {
                mInputStream.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void close() {
        mRun = false;
        try {
            mInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface UsbReadCallback {
        public void recieved(byte[] results);
    }
}
