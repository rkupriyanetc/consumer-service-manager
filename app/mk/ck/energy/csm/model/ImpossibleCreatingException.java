package mk.ck.energy.csm.model;

public class ImpossibleCreatingException extends Exception {
	
	private static final long	serialVersionUID	= 1L;
	
	public ImpossibleCreatingException() {}
	
	public ImpossibleCreatingException( final String message ) {
		super( message );
	}
	
	public ImpossibleCreatingException( final String message, final Throwable throwable ) {
		super( message, throwable );
	}
}
