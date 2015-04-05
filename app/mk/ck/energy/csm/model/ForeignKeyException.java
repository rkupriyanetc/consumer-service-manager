package mk.ck.energy.csm.model;

public class ForeignKeyException extends Exception {
	
	private static final long	serialVersionUID	= 1L;
	
	public ForeignKeyException() {}
	
	public ForeignKeyException( final String message ) {
		super( message );
	}
	
	public ForeignKeyException( final String message, final Throwable throwable ) {
		super( message, throwable );
	}
}
