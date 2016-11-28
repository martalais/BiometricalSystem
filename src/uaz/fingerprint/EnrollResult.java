package uaz.fingerprint;
public class EnrollResult{
        public static int CAPTURE_COMPLETE = 0;
	public static int COMPLETE = 1;
	public static int FAIL = 2; 
	public static int PASS = 3;
	public static int RETRY = 100;
	public static int RETRY_TOO_SHORT = 101;
	public static int RETRY_CENTER_FINGER = 102; 
	public static int RETRY_REMOVE_FINGER = 103;

	
	private int mCode;
	private byte[] mData;
	private FingerprintImage mImage;
	
	//Regresa un codigo de resultado
	public int getCode(){
		return mCode;
	}
	
	//Representacion binaria del enrollment
	public byte[] getData(){
		return mData;
	}

	//Imagen obtenida
	public FingerprintImage getImage(){
		return mImage;
	}

}