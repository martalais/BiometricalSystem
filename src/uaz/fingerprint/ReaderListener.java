package uaz.fingerprint;

public interface ReaderListener{
	public void onStartCapture(Reader reader);
	public void onCapture(Reader reader, EnrollResult result);
	public void onStopCapture(Reader reader);
        public void onOpen(Reader reader);
        public void onClose(Reader reader);
        public void onError(Reader reader, int code);
}