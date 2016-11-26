package uaz.fingerprint;

public class VerifyResult{
	private int mCode;
	private int mScore;
	public VerifyResult(int code, int score){
		mCode = code;
		mScore = score;
	}

	public int getScore(){
		return mScore;
	}
	
	public int getCode(){
		return mCode;
	}


	public static int NO_MATCH				= 0;
	public static int MATCH					= 1;
	public static int RETRY 				= EnrollResult.RETRY;
	public static int RETRY_TOO_SHORT		= EnrollResult.RETRY_TOO_SHORT; 
	public static int RETRY_CENTER_FINGER 	= EnrollResult.RETRY_CENTER_FINGER;
	public static int RETRY_REMOVE_FINGER 	= EnrollResult.RETRY_REMOVE_FINGER;
}