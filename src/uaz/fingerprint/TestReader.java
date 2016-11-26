package uaz.fingerprint;
import java.util.*;
/** 
* TODO 
*/
public class TestReader{
	public static void main(String args[]) throws InterruptedException{
		ArrayList<Reader> readers = Reader.listDevices();
		Reader reader = readers.get(0);
		reader.open();
		System.out.println("Escaneos obligatorios: " + reader.getNumberEnrollStages());
		EnrollResult result = reader.enrollFinger();
		while (result.getCode() != EnrollResult.COMPLETE){
			result = reader.enrollFinger();
		}
		EnrollResult sample = reader.getSample();
                System.out.println("Sample data: " + sample.getData());
                System.out.println("Enroll data: " + result.getData());
		System.out.println("SampleExecuted: " + sample.getCode());
		VerifyResult v = Reader.verify(result.getData(), sample.getData());
		System.out.println("VerifyExecuted! code: " + v.getCode() + " score: " + v.getScore());

		reader.close();
	}
}