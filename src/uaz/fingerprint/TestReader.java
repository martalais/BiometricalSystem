package uaz.fingerprint;
import java.util.*;
/** 
* TODO 
*/
public class TestReader{
        static int flag = 0;
	public static void main(String args[]) throws InterruptedException{
            ArrayList<Reader> readers = Reader.listDevices();
            Reader reader = readers.get(0);
            reader.open();
            Thread.sleep(3000);
            reader.startCapture();
            Thread.sleep(3000);
            reader.stopCapture();
            reader.close();
            System.out.println("Terminado");
	}
        
}