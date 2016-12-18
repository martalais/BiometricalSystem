package uaz.fingerprint;
import java.util.*;
/** 
* TODO 
*/
public class TestReader implements ReaderListener{
    ArrayList<Reader> readers;
    static int flag = 0;
    static EnrollResult enrolledFinger;
    static Reader reader;
    public static void main(String args[]) throws InterruptedException{
        TestReader test = new TestReader();
        reader = Reader.getDefault();
        reader.setDaemon(false);
        reader.open();
        System.out.println("Device: " + reader.getDriverName());
        System.out.println("Stages: " + reader.getEnrollStages());
        reader.startCapture();
        reader.addListener(test);
        
        System.out.println("Main thread finished");
    }

    @Override
    public void onCaptureStart(Reader reader) {
        System.out.println("TestReader::onCaptureStart()");
    }

    @Override
    public void onCapture(Reader readser, EnrollResult result) {
        
        System.out.println("TestReader::onCapture()");
        reader.stopCapture();
        reader.startEnrollment();
    }

    @Override
    public void onCaptureStop(Reader reader) {
        System.out.println("TestReader::onCaptureStop()");
    }

  

    @Override
    public void onEnrollStart(Reader reader) {
        System.out.println("TestReader::onEnrollStart()");
    }

    @Override
    public void onEnrollStop(Reader reader) {
        System.out.println("TestReader::onEnrollStop()");
        reader.close();
    }

    @Override
    public void onEnroll(Reader reader, EnrollResult result) {
        System.out.println("TestReader::onEnroll()");
    }

    @Override
    public void onVerifyStart(Reader reader) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onVerify(Reader reader, VerifyResult result) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onVerifyStop(Reader reader) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

        
}