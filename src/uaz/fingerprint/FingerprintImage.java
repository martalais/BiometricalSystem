package uaz.fingerprint;

class FingerprintImage{
	private byte[] mData;
	private int mWidth;
	private int mHeight;

	public FingerprintImage(byte[] data, int width, int height){
		mData = data;
		mWidth = width;
		mHeight = height;
	}

	public byte[] getData(){
		return mData;
	}

	public int getWidth(){
		return mWidth;
	}

	public int getHeight(){
		return mHeight;
	}
}