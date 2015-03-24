package mk.ck.energy.csm.models;

public class AddressNotFoundException extends Exception {
	
	private static final long	serialVersionUID	= 1L;
	
	public AddressNotFoundException() {}
	
	public AddressNotFoundException( final String message ) {
		super( message );
	}
	
	public AddressNotFoundException( final String message, final Throwable throwable ) {
		super( message, throwable );
	}
}
