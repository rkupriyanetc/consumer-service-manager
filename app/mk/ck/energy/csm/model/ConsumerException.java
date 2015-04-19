package mk.ck.energy.csm.model;

import mk.ck.energy.csm.model.mongodb.MongoDocumentException;

public class ConsumerException extends MongoDocumentException {
	
	private static final long	serialVersionUID	= 1L;
	
	public ConsumerException() {}
	
	public ConsumerException( final String message ) {
		super( message );
	}
	
	public ConsumerException( final String message, final Throwable throwable ) {
		super( message, throwable );
	}
}
