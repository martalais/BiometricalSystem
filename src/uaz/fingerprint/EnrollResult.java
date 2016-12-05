package uaz.fingerprint;
public class EnrollResult{
        public static final int CAPTURE_COMPLETE = 0;
	public static final int COMPLETE = 1;
	public static final int FAIL = 2; 
	public static final int PASS = 3;
	public static final int RETRY = 100;
	public static final int RETRY_TOO_SHORT = 101;
	public static final int RETRY_CENTER_FINGER = 102; 
	public static final int RETRY_REMOVE_FINGER = 103;

	
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
        
        public void setData(byte[] data){
            mData = data;
        }
        public void setCode(int code){
            mCode = code;
        }
	//Imagen obtenida
	public FingerprintImage getImage(){
		return mImage;
	}

}