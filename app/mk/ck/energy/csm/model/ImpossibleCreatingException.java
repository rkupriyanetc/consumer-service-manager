package mk.ck.energy.csm.model;

import mk.ck.energy.csm.model.mongodb.MongoDocumentException;

public class ImpossibleCreatingException extends MongoDocumentException {
	
	private static final long	serialVersionUID	= 1L;
	
	public ImpossibleCreatingException() {}
	
	public ImpossibleCreatingException( final String message ) {
		super( message );
	}
	
	public ImpossibleCreatingException( final String message, final Throwable throwable ) {
		super( message, throwable );
	}
}
