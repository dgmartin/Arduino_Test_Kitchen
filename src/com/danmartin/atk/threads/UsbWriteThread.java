package com.danmartin.atk.threads;

import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: danmartin
 * Date: 2/14/13
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class UsbWriteThread extends Thread {

    private boolean mRun = true;

    private String mPayload;

    private BufferedWriter mOutputStream;

    public UsbWriteThread() {
        super("UsbWriter");
    }

    public UsbWriteThread(String threadName) {
        super(threadName);
    }

    public void init(BufferedWriter inOutputStream) {
        mOutputStream = inOutputStream;
    }

    public void notify(String payload) {
        try {
            synchronized (mPayload) {
                mPayload = payload;
            }
        } catch (Exception e) {
            mPayload = payload;
        }
        synchronized (this) {
            this.notifyAll();
        }
    }

    @Override
    public synchronized void run() {
        String tempPayload = null;
        while (mRun) {
            if (mPayload != null) {
                synchronized (mPayload) {
                    tempPayload = mPayload;
                    mPayload = null;
                }
                if (tempPayload != null && mOutputStream != null) {
                    try {
                        mOutputStream.write(tempPayload);
                        tempPayload = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized void close() {
        mRun = false;
        try {
            mOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.notifyAll();
    }

}

