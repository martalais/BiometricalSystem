package uaz.fingerprint;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
/** 
* TODO 
*/
public class TestReader implements ReaderListener{
        static int flag = 0;
        static EnrollResult enrolledFinger;
	public static void main(String args[]) throws InterruptedException{
            ArrayList<Reader> readers = Reader.listDevices();
            Reader reader = readers.get(0);
            reader.setDaemon(false); 
            reader.open();
            
            String driverName = reader.getDriverName();
            int stages = reader.getNumberEnrollStages();
            
            System.out.println("Device: " + driverName);
            System.out.println("Enroll stages: " + stages);
            
            while((enrolledFinger = reader.enrollFinger()).getCode() != EnrollResult.COMPLETE){
                  
            }
            System.out.println("Enroll complete!");
            reader.addListener(new TestReader());
            reader.startCapture();
            System.out.println("Main thread finished");
	}

    @Override
    public void onStartCapture(Reader reader) {
        System.out.println("El dispositivo ha empezado a capturar huellas");
    }

    @Override
    public void onCapture(Reader reader, EnrollResult enroll) {
        System.out.println("Test::onCapture(): " + enroll.getCode() + " " + enroll.getData());
        VerifyResult result = Reader.verify(enrolledFinger.getData(), enroll.getData());
        if (result.getCode() == VerifyResult.MATCH){
            System.out.println("La huella coincide!");
        }
        else{
            System.out.println("La huella NO coincide!");
        }
        
        
        try {
            File output = new File(System.getProperty("user.home") + "/fingerprint.png");            
            ImageIO.write(enroll.getImage().toBufferedImage(), "png", output);
        } catch (IOException ex) {
        }
    }

    @Override
    public void onStopCapture(Reader reader) {
        System.out.println("El dispositivo ha finalizado de capturar huellas");
        reader.startCapture();
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
        
}