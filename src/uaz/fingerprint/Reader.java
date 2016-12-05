package uaz.fingerprint;

import java.util.ArrayList;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import uaz.nativeutils.*;

public class Reader implements NativeReaderCallback{

    public static int FP_VERIFY_NO_MATCH = 0;
    public static int FP_VERIFY_MATCH = 1;
    private String mDriverName;
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
    private Thread mThread;
    private final ArrayList<EnrollResult> mLastEnroll;
    
    
    public native int nativeOpen();
    public native int nativeClose();
    public native String nativeGetDriverName();
    public native EnrollResult nativeGetCapture();
    public native int nativeGetNumberEnrollStages();
    public native int nativeStartCapture(NativeReaderCallback callback, Object usarData);
    public native int nativeStopCapture(NativeReaderCallback callback, Object userData);
    public native int nativeStartEnrollment(NativeReaderCallback callback, Object userData);
    public native int nativeStopEnrollment(NativeReaderCallback callback, Object userData);
    public native int nativeHandleEvents(int timeout);
    public native EnrollResult nativeEnrollFinger();
    
    public void setDaemon(boolean value){
        mIsDaemon = value;
    }
    public String getDriverName(){
        if (mDriverName == null){
           checkForDispatcher();
           Message  msg = new Message(Message.GET_DRIVER_NAME, Message.MESSAGE_NORMAL_PRIORITY, true);
           MessageResult result = mDispatcher.sendMessage(msg);
           if (result.code == MessageResult.FAIL){
               throw new ReaderException("El dispositivo no ha sido abierto, no se puede obtener informacion de éste");
           }
           else{
               mDriverName = (String) result.result;
           }
        }
        return mDriverName;
    }
    
 
    public int getNumberEnrollStages(){
        
        if (mEnrollStages == -1){
            checkForDispatcher();
            Message msg = new Message(Message.GET_ENROLL_STAGES, Message.MESSAGE_NORMAL_PRIORITY, true);
            MessageResult result = mDispatcher.sendMessage(msg);
            if (result.code == MessageResult.SUCCESS){
                Integer num = (Integer) result.result;
                mEnrollStages = num.intValue();
            }
        }
        return mEnrollStages;
    }
    
    public EnrollResult enrollFinger(){
        this.startEnrollment();
        synchronized(mLastEnroll){
            mLastEnroll.clear();
            while(mLastEnroll.isEmpty()){
                try {
                    mLastEnroll.wait();
                } catch (InterruptedException ex) {}
            }
            return mLastEnroll.get(0);
        }
    }
    
    private void checkForDispatcher(){
        if (mThread == null){
            mThread = new Thread(mDispatcher);
            mThread.setDaemon(mIsDaemon);
            mThread.start();
        }
    }
    @Override
    public void onGetDriverName(String name, Object userData){
        mDriverName = name;
    }
    @Override
    public void onGetEnrollStages(int enrollStages, Object userData) {
        mEnrollStages = enrollStages;
    }
    
    @Override
    public void onCaptureStart(Object userData){
        synchronized(mListeners){
            for(ReaderListener listener: mListeners){
                listener.onStartCapture(this);
            }
        }
    }
    
    @Override
    public void onCapture(EnrollResult enroll, Object userData) {
        if (enroll.getCode() == EnrollResult.COMPLETE){
            Message msg = new Message(Message.STOP_ENROLLMENT, Message.MESSAGE_NORMAL_PRIORITY);
            MessageResult result = mDispatcher.sendMessage(msg);
            if (result != null){
                
            }
        }
        synchronized(mListeners){
            for(ReaderListener listener: mListeners){
                listener.onCapture(this, enroll);
            }
        }
        if (enroll.getCode() != EnrollResult.CAPTURE_COMPLETE){
            synchronized(mLastEnroll){
                mLastEnroll.add(enroll);
                mLastEnroll.notify();
            }
        }
    }

    @Override
    public void onCaptureStop(Object userData) {
        synchronized(mListeners){
            for (ReaderListener listener: mListeners){
                listener.onStopCapture(this);
            }
        }
    }
    
    @Override
    public void onOpen(Object userData){
        synchronized(mListeners){
            for (ReaderListener listener: mListeners){
                listener.onOpen(this);
            }
        }
    }
    
    @Override
    public void onClose(Object userData){
        synchronized(mListeners){
            for (ReaderListener listener: mListeners){
                listener.onClose(this);
            }
        }
    }
    
    @Override
    public void onError(Object userData){
        Message msg = (Message) userData;
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
        mDispatcher.sendMessage(new Message((Message.START_ENROLLMENT),Message.MESSAGE_NORMAL_PRIORITY));
    }
    public void stopEnrollment(){
        mDispatcher.sendMessage(new Message(Message.STOP_ENROLLMENT, 1));
    }
    
    public void open(){        
        checkForDispatcher();
        Message msg = new Message(Message.OPEN, Message.MESSAGE_NORMAL_PRIORITY, true);
        MessageResult result = mDispatcher.sendMessage(msg);
        if (result.code == MessageResult.FAIL){
            throw new ReaderException("Otro proceso esta utilizando el dispositivo y no se puede tener acceso a este");
        }
    }
    public void close(){
        mDispatcher.sendMessage(new Message(Message.CLOSE, 0, true));
    }
    
   
    
    public Reader(){
        mDispatcher = new ReaderDispatcher(this, this);
        mListeners = new ArrayList<ReaderListener>();
        mLastEnroll = new ArrayList<EnrollResult>();
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
        if (mDriverName != null){
            return mDriverName;
        }
        return "Dispositivo no disponible";
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