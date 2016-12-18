package uaz.fingerprint;

public interface ReaderListener{
	public void onCaptureStart(Reader reader);
	public void onCapture(Reader reader, EnrollResult result);
	public void onCaptureStop(Reader reader);
        public void onEnrollStart(Reader reader);
        public void onEnrollStop(Reader reader);
        public void onEnroll(Reader reader, EnrollResult result);
        public void onVerifyStart(Reader reader);
        public void onVerify(Reader reader, VerifyResult result);
        public void onVerifyStop(Reader reader);
        
}