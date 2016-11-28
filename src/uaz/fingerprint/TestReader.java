package uaz.fingerprint;
import java.util.*;
/** 
* TODO 
*/
public class TestReader implements ReaderListener{
        static int flag = 0;
	public static void main(String args[]) throws InterruptedException{
            ArrayList<Reader> readers = Reader.listDevices();
            Reader reader = readers.get(0);
            reader.open();
            System.out.println("EnrollStages: " + reader.getNumberEnrollStages());
            reader.addListener(new TestReader());
            reader.startEnrollment();
            
            System.out.println("Terminado");
	}

    @Override
    public void onStartCapture(Reader reader) {
        System.out.println("El dispositivo ha empezado a capturar huellas");
    }

    @Override
    public void onCapture(Reader reader, EnrollResult result) {
        System.out.println("Test::onCapture(): " + result.getCode() + " " + result.getData());
        //reader.stopEnrollment();       
    }

    @Override
    public void onStopCapture(Reader reader) {
        reader.startEnrollment();
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
        
    }
        
}