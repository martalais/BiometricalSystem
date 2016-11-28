package uaz.fingerprint;

import java.util.ArrayList;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private boolean mIsDaemon = true;
    private final ReaderDispatcher mDispatcher;
    private ArrayList<ReaderListener> mListeners;
    private int mEnrollStages = -1;
    private boolean mIsOpen;
    private Thread mThread;
    
    
    public native int nativeOpen();
    public native int nativeClose();
    public native EnrollResult nativeGetCapture();
    public native int nativeGetNumberEnrollStages();
    public native int nativeStartCapture(NativeReaderCallback callback, Object usarData);
    public native int nativeStopCapture(NativeReaderCallback callback, Object userData);
    public native int nativeStartEnrollment(NativeReaderCallback callback, Object userData);
    public native int nativeStopEnrollment(NativeReaderCallback callback, Object userData);
    public native int nativeHandleEvents();
    public native EnrollResult nativeEnrollFinger();
    
    public void setDaemon(boolean value){
        mIsDaemon = value;
    }
    
    public int getNumberEnrollStages(){
        if (mEnrollStages == -1){
            checkForDispatcher();
            mDispatcher.sendMessage(new Message(Message.GET_ENROLL_STAGES, 1));
            while(mEnrollStages == -1){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Reader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return mEnrollStages;
    }
    
    private void checkForDispatcher(){
        if (mThread == null){
            mThread = new Thread(mDispatcher);
            mThread.setDaemon(mIsDaemon);
            mThread.start();
        }
    }
    
    @Override
    public void onGetEnrollStages(int enrollStages, Object userData) {
        mEnrollStages = enrollStages;
    }
    
    @Override
    public void onCaptureStart(Object userData){
        System.out.println("Reader::onCaptureStart()");
        synchronized(mListeners){
            for(ReaderListener listener: mListeners){
                listener.onStartCapture(this);
            }
        }
    }
    
    @Override
    public void onCapture(EnrollResult result, Object userData) {
        System.out.println("Reader::onCapture(): " + result.getCode());
        if (result.getCode() == EnrollResult.COMPLETE){
            mDispatcher.sendMessage(new Message(Message.STOP_ENROLLMENT, 1));
        }
        synchronized(mListeners){
            for(ReaderListener listener: mListeners){
                listener.onCapture(this, result);
            }
        }
        
    }

    @Override
    public void onCaptureStop(Object userData) {
        System.out.println("Reader::onCaptureStop()");
        synchronized(mListeners){
            for (ReaderListener listener: mListeners){
                listener.onStopCapture(this);
            }
        }
    }
    
    @Override
    public void onOpen(Object userData){
        System.out.println("Open");
        synchronized(mListeners){
            for (ReaderListener listener: mListeners){
                listener.onOpen(this);
            }
        }
    }
    
    @Override
    public void onClose(Object userData){
        System.out.println("Close");
        synchronized(mListeners){
            for (ReaderListener listener: mListeners){
                listener.onClose(this);
            }
        }
    }
    
    @Override
    public void onError(Object userData){
        Message msg = (Message) userData;
        System.out.println("Error al procesar este mensaje:" + msg.mCode);
        synchronized(mListeners){
            for(ReaderListener listener: mListeners){
                listener.onError(this, msg.mCode);
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
    public void startEnrollment(){
        mDispatcher.sendMessage(new Message((Message.START_ENROLLMENT),1));
    }
    public void stopEnrollment(){
        mDispatcher.sendMessage(new Message(Message.STOP_ENROLLMENT, 1));
    }
    
    public void open(){        
        checkForDispatcher();
        mDispatcher.sendMessage(new Message(Message.OPEN, 1));
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