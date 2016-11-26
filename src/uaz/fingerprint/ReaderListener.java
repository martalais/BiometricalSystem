package uaz.fingerprint;

public interface ReaderListener{
	public void onStart(Reader reader);
	public void onCapture(Reader reader, EnrollResult result);
	public void onClose(Reader reader);
}