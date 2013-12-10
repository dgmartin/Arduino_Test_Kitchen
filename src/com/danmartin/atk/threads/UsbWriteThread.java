package com.danmartin.atk.threads;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
    private byte[] mBuffer;

//    private BufferedWriter mOutputStream;
    private OutputStream mOutputStream;

    public UsbWriteThread() {
        super("UsbWriter");
    }

    public UsbWriteThread(String threadName) {
        super(threadName);
    }

    public void init(OutputStream inOutputStream) {
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
    public void notify(byte[] buffer) {
        try {
            synchronized (mBuffer) {
                mBuffer = buffer;
            }
        } catch (Exception e) {
            mBuffer = buffer;
        }
        synchronized (this) {
            this.notifyAll();
        }
    }

//    @Override
//    public synchronized void run() {
//        String tempPayload = null;
//        while (mRun) {
//            if (mPayload != null) {
//                synchronized (mPayload) {
//                    byte[] mBuffer = new byte[4];
//
//                    tempPayload = mPayload;
//                    mPayload = null;
//                }
//                if (tempPayload != null && mOutputStream != null) {
//                    try {
//                        mOutputStream.write(tempPayload);
//                        tempPayload = null;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else {
//                try {
//                    this.wait();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    @Override
    public synchronized void run() {
        byte[] tempBuffer = null;
        while (mRun) {
            if (mBuffer != null) {
                synchronized (mBuffer) {
                    tempBuffer = mBuffer;
                    mBuffer = null;
                }
                if (tempBuffer != null && mOutputStream != null) {
                    try {
                        mOutputStream.write(tempBuffer);
                        tempBuffer = null;
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

