package uaz.fingerprint;

import java.util.ArrayList;
import java.io.*;
import uaz.nativeutils.*;

public class Reader implements NativeReaderCallback{

    public static int FP_VERIFY_NO_MATCH = 0;
    public static int FP_VERIFY_MATCH = 1;
    //Contador de referencias para la lista resultante dispositivos detectados (Lista proveniente desde codigo nativo)
    private DeviceLock mDeviceLock;

    //Puntero C a un elemento de la lista de los dispositivos
    private long mDevice;

    //Puntero C que hace referencia al dispositivo cuando se abre y que habilitan las operaciones de este.
    private long mDeviceHandle;
    //Puntero C que hace referencia un AsynCaptureData para este objeto
    private long mAsynCaptureData;
    
    //Variable que indicara si este dispositivo esta abierto para leer de el
    private boolean mIsOpened = false;
    private final ReaderDispatcher mDispatcher;
    private ArrayList<ReaderListener> mListeners;
    private ArrayList<ReaderListener> mCaptureListeners;
    private Thread mThread;
    
    
    public native void nativeOpen();
    public native void nativeClose();
    public native EnrollResult nativeGetCapture();
    public native void nativeStartCapture(NativeReaderCallback callback, Object usarData);
    public native void nativeStopCapture(NativeReaderCallback callback, Object userData);
    public native int nativeHandleEvents();
    public native EnrollResult nativeEnrollFinger();
     
     @Override
    public void onCapture(EnrollResult result, Object userData) {
        System.out.println("Imagen obtenida");
        synchronized(mCaptureListeners){
            for(ReaderListener listener: mCaptureListeners){
                listener.onCapture(this, result);
            }
        }
    }

    @Override
    public void onCaptureStop(Object userData) {
        System.out.println("Captura detenida");
        synchronized(mCaptureListeners){
            for (ReaderListener listener: mCaptureListeners){
                listener.onStopCapture(this);
            }
        }
    }
    
    /**
     * Inicia el proceso de captura de huellas en el dispositivo, puede ser llamado desde cualquier hilo
     */
    public void startCapture(){
        mDispatcher.sendMessage(new Message(Message.START_CAPTURE, 1));
    }
    
    public void stopCapture(){
        mDispatcher.sendMessage(new Message(Message.STOP_CAPTURE, 1));
    }
    
    
    public void open(){        
        if (mThread == null){
            mThread = new Thread(mDispatcher);
            mThread.start();
        }
        mDispatcher.sendMessage(new Message(Message.OPEN, 0));
    }
    public void close(){
        mDispatcher.sendMessage(new Message(Message.CLOSE, 0));
    }
    
  
    public synchronized  EnrollResult enrollFinger(){
        return null;
    }
    
    
    public Reader(){
        mDispatcher = new ReaderDispatcher(this, this);
        mListeners = new ArrayList<ReaderListener>();
        mCaptureListeners = new ArrayList<>();
        System.out.println("Reader::init");
    }
    public void addListener(ReaderListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }
    
    public void addCaptureListener(ReaderListener listener){
        synchronized(mCaptureListeners){
            mCaptureListeners.add(listener);
        }
    }
    
    public void removeListener(ReaderListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

   

    public synchronized void start() {
      
    }

    public synchronized void stop() {
        
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
