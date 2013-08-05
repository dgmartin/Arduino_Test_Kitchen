package com.danmartin.atk.threads;

import java.io.BufferedReader;
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

    BufferedReader mInputStream;
    UsbReadCallback mListener;

    public UsbReadThread() {
        super("UsbReader");
    }

    public UsbReadThread(String threadName) {
        super(threadName);
    }

    public void init(BufferedReader inInputStream, UsbReadCallback listener) {
        mInputStream = inInputStream;
        mListener = listener;
    }

    @Override
    public void run() {
        while (mRun) {
            char[] results = null;
            try {
                if (mInputStream.ready()) {
                    String nextLine = mInputStream.readLine();
                    if (nextLine != null) {
                        results     =new char[nextLine.length()];
                                nextLine.getChars(0,nextLine.length(),results,0);
                        if (mListener != null) {
                            mListener.recieved(results);
                        }
                    } else {
                        mInputStream.wait();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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
        public void recieved(char[] results);
    }
}
