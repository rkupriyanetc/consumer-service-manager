package mk.ck.energy.csm.model;

import mk.ck.energy.csm.model.mongodb.MongoDocumentException;

public class ForeignKeyException extends MongoDocumentException {
	
	private static final long	serialVersionUID	= 1L;
	
	public ForeignKeyException() {}
	
	public ForeignKeyException( final String message ) {
		super( message );
	}
	
	public ForeignKeyException( final String message, final Throwable throwable ) {
		super( message, throwable );
	}
}
