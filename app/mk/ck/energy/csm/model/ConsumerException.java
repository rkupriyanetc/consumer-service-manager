package mk.ck.energy.csm.model;

public class ConsumerException extends Exception {
	
	private static final long	serialVersionUID	= 1L;
	
	public ConsumerException() {}
	
	public ConsumerException( final String message ) {
		super( message );
	}
	
	public ConsumerException( final String message, final Throwable throwable ) {
		super( message, throwable );
	}
}
