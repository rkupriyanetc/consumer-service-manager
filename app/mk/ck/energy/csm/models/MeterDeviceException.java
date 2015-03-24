package mk.ck.energy.csm.models;

public class MeterDeviceException extends Exception {
	
	private static final long	serialVersionUID	= 1L;
	
	public MeterDeviceException() {}
	
	public MeterDeviceException( final String message ) {
		super( message );
	}
	
	public MeterDeviceException( final String message, final Throwable throwable ) {
		super( message, throwable );
	}
}