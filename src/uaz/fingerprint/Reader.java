package uaz.fingerprint;

import java.util.ArrayList;
import java.io.*;

import uaz.nativeutils.*;

public class Reader {

    public static int FP_VERIFY_NO_MATCH = 0;
    public static int FP_VERIFY_MATCH = 1;
    //Contador de referencias para la lista resultante dispositivos detectados (Lista proveniente desde codigo nativo)
    private DeviceLock mDeviceLock;

    //Puntero C a un elemento de la lista de los dispositivos
    private long mDevice;

    //Puntero C que hace referencia al dispositivo cuando se abre y que habilitan las operaciones de este.
    private long mDeviceHandle;
    
    //Variable que indicara si este dispositivo esta abierto para leer de el
    private boolean mIsOpened = false;
    private Dispatcher mDispatcher = new Dispatcher();
    private ArrayList<ReaderListener> mListeners;
    
    public void open(){
        nativeOpen();
        mIsOpened = true;
    }
    public void close(){
        mIsOpened = false;
        nativeClose();
    }
    
  
    
    public synchronized  EnrollResult enrollFinger(){
        if (!mDispatcher.mIsRunning){
            return enrollFinger2();
        }
        else{
            final ArrayList<EnrollResult> result = new ArrayList<>();
            ReaderListener listener = new ReaderListener() {
                @Override
                public void onStart(Reader reader) {}

                @Override
                public void onCapture(Reader reader, EnrollResult enrollment) {    
                    result.add(enrollment);
                }
                                
                @Override
                public void onClose(Reader reader) {}
            };
            this.addListener(listener);
            while(result.size() == 0 && mDispatcher.mIsRunning){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {}
            }
            if (result.size() == 0){
                return enrollFinger2();
            }
            else{
                return result.remove(0);
            }
        }
    }
    
    
    public Reader(){
        mListeners = new ArrayList<ReaderListener>();
        System.out.println("Reader::init");
    }
    public void addListener(ReaderListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    public void removeListener(ReaderListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    private class Dispatcher implements Runnable {

        private boolean mStop = false;
        private boolean mIsRunning = false;

        public void stop() {
            mStop = true;
            Reader.this.close();
        }

        public void run() {
            mStop = false;
            mIsRunning = true;
            Reader.this.open();
            synchronized (mListeners) {
                for (ReaderListener listener : mListeners) {
                    listener.onStart(Reader.this);
                }
            }

            while (!mStop) {
                EnrollResult result = Reader.this.enrollFinger2();
                synchronized (mListeners) {
                    for (ReaderListener listener : mListeners) {
                        listener.onCapture(Reader.this, result);
                    }
                }
            }
            synchronized (mListeners) {
                for (ReaderListener listener : mListeners) {
                    listener.onClose(Reader.this);
                }
            }
            
            mIsRunning = false;
        }
    }

    public synchronized void start() {
       //El despachador de eventos no esta corriendo, creamos un nuevo thread q lo haga
       if (!mDispatcher.mIsRunning){
           new Thread(mDispatcher).start();
       }
       
    }

    public synchronized void stop() {
        if (mDispatcher.mIsRunning){
            mDispatcher.stop();
        }
    }

    //fp_init
    private static native void init();

    //fp_exit && fp_dscv_dev_free
    private static native void exit();
    
    //fp_discover_devs && fp_dscv_dev_free
    public static native ArrayList<Reader> listDevices();

    public String toString() {
        return "Digital Persona U.are.U";
    }

    public static Reader getDefault() {
        ArrayList<Reader> listDevices = listDevices();
        if (listDevices.size() > 0) {
            return listDevices.get(0);
        } else {
            return null;
        }
    }

    protected void finalize() throws Throwable {
        try {
            synchronized (mDeviceLock) {
                if (mDeviceLock.popReference() <= 0) {
                    System.out.println("Liberando dispositivos reader");
                    Reader.freeDiscoveredDevices(mDeviceLock.getListDevices());
                }
            }
        } finally {
            super.finalize();
        }
    }

    public native static void freeDiscoveredDevices(long pointer);

    /* asyn not implemented yet
		//fp_dscv_dev_get_devtype
		public native int getType();
		//fp_dscv_dev_get_driver
		public native Driver getDriver();
		Gets the devtype for a discovered device. 
		int 	fp_dscv_dev_supports_print_data (struct fp_dscv_dev *dev, struct fp_print_data *data)
		 	Determines if a specific stored print appears to be compatible with a discovered device. 
		int 	fp_dscv_dev_supports_dscv_print (struct fp_dscv_dev *dev, struct fp_dscv_print *data)
		 	Determines if a specific discovered print appears to be compatible with a discovered device. 
		struct fp_dscv_dev * 	fp_dscv_dev_for_print_data (struct fp_dscv_dev **devs, struct fp_print_data *data)
		 	Searches a list of discovered devices for a device that appears to be compatible with a stored print. 
		struct fp_dscv_dev * 	fp_dscv_dev_for_dscv_print (struct fp_dscv_dev **devs, struct fp_dscv_print *print)
		 	Searches a list of discovered devices for a device that appears to be compatible with a discovered print. 

     */
    public native void nativeOpen();

    public native void nativeClose();

    private native EnrollResult enrollFinger2();

    public native EnrollResult getSample();

    public native int getNumberEnrollStages();

    public native VerifyResult verify(byte[] enrolledFinger);

    public native VerifyResult identify(byte[][] enrolledFingers);

    public native static VerifyResult verify(byte[] print, byte[] anotherprint);

    static {

        try {
            System.loadLibrary("fingerprint"); // used for tests. This library in classpath only
            Reader.init();
        } catch (UnsatisfiedLinkError e) {
            try {
                NativeUtils.loadLibraryFromJar("/nativelibs/" + System.mapLibrary‌​Name("fingerprint"));
                Reader.init();
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }
    }
}
