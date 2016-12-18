/*
 * The MIT License
 *
 * Copyright 2016 xmbeat.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package uaz.fingerprint;

/**
 *
 * @author xmbeat
 */
class Message implements Comparable<Message> {

    public int mCode;
    public int mPriority;
    public Object mAdditionalData;
    public boolean mWaitable;
    public static final int NONE = 0;
    public static final int START_OPEN = 1;
    public static final int STOP_OPEN = 2;
    public static final int OPEN = 3;
    public static final int CLOSE = 4;
    public static final int START_CAPTURE = 5;
    public static final int STOP_CAPTURE = 6;
    public static final int CAPTURE = 7;
    public static final int START_ENROLL = 8;
    public static final int STOP_ENROLL = 9;
    public static final int ENROLL = 10;
    public static final int START_VERIFY = 11;
    public static final int STOP_VERIFY = 12;
    public static final int VERIFY = 13;
    public static final int GET_ENROLL_STAGES = 14;
    public static final int GET_DRIVER_NAME = 15;
    public static final int CLOSE_THREAD = 16;
    public static final int MESSAGE_LOW_PRIORITY = 10;
    public static final int MESSAGE_NORMAL_PRIORITY = 5;
    public static final int MESSAGE_HIGH_PRIORITY = 0;

    public Message(int code, int priority, boolean waitable) {
        mCode = code;
        mPriority = priority;
        mWaitable = waitable;
    }

    @Override
    public String toString() {
        switch (mCode) {
            case OPEN:
                return "OPEN";
            case CLOSE:
                return "CLOSE";
            case START_CAPTURE:
                return "START_CAPTURE";
            case STOP_CAPTURE:
                return "STOP_CAPTURE";
            case START_ENROLL:
                return "START_ENROLL";
            case STOP_ENROLL:
                return "STOP_ENROLL";
            case GET_DRIVER_NAME:
                return "GET_DRIVER_NAME";
            case GET_ENROLL_STAGES:
                return "GET_ENROLL_STAGES";
            default:
                return "CODE: " + mCode;
        }
    }

    public Message(int code, int priority) {
        this(code, priority, false);
    }

    @Override
    public int compareTo(Message other) {
        if (other == null) {
            return -1;
        }
        return mPriority - other.mPriority;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Message) {
            Message other = (Message) obj;
            return mCode == other.mCode && mPriority == other.mPriority;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.mCode;
        hash = 97 * hash + this.mPriority;
        return hash;
    }

}

class MessageResult {

    public final static int SUCCESS = 0;
    public final static int FAIL = 1;
    public final static int IGNORED = 2;
    int code;
    Object result;
}
