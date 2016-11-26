package uaz.fingerprint;
public class DeviceLock{
	private long mListDevices;
	private int mCountReference;

	public DeviceLock(long devices){
		mListDevices = devices;
	}

	public int pushReference(){
		mCountReference += 1;
		return mCountReference;
	}
	public int getCountReference(){
		return mCountReference;
	}
	public int popReference(){
		mCountReference -= 1;
		return mCountReference;
	}
	public long getListDevices(){
		return mListDevices;
	}
}