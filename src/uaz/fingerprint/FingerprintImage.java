package uaz.fingerprint;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class FingerprintImage{
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
        public BufferedImage toBufferedImage(){
            if (mData!=null){
                BufferedImage image = new BufferedImage(mWidth, mHeight, BufferedImage.TYPE_BYTE_GRAY);
                byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                System.arraycopy(mData, 0, data, 0, mData.length);            
                return image;
            }
            return null;
 
        }
}