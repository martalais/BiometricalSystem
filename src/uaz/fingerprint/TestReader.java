package uaz.fingerprint;
import java.util.*;
/** 
* TODO 
*/
public class TestReader implements ReaderListener, Runnable{
    ArrayList<Reader> readers;
    static int flag = 0;
    static EnrollResult enrolledFinger;
    public static void main(String args[]) throws InterruptedException{
        TestReader test = new TestReader();
        Thread t1 = new Thread(test);
        Thread t2 = new Thread(test);
        t1.start();
        Thread.sleep(3000);
        t2.start();
        System.out.println("Main thread finished");
    }

    @Override
    public void onStartCapture(Reader reader) {
        System.out.println("El dispositivo ha empezado a capturar huellas");
    }

    @Override
    public void onCapture(Reader reader, EnrollResult enroll) {
        System.out.println("Test::onCapture(): " + enroll.getCode() + " " + enroll.getData());
        
    }

    @Override
    public void onStopCapture(Reader reader) {
        System.out.println("El dispositivo ha finalizado de capturar huellas");
     
    }

    @Override
    public void onOpen(Reader reader) {
        System.out.println("El dispositivo se ha abierto");
    }

    @Override
    public void onClose(Reader reader) {
        System.out.println("El dispositivo se ha cerrado");
    }
    
    @Override
    public void onError(Reader reader, int code){
        System.out.println("On error: " + code);
    }

    @Override
    public void run() {
        if (readers != null){
            for (Reader reader: readers){
                reader.stopCapture();
                reader.close();
            }
        }
        readers = Reader.listDevices();
        Reader reader = readers.get(0);
        reader.setDaemon(false); 
        reader.open();

        String driverName = reader.getDriverName();
        int stages = reader.getNumberEnrollStages();

        System.out.println("Device: " + driverName);
        System.out.println("Enroll stages: " + stages);

        reader.addListener(this);
        reader.startCapture();
    }
        
}