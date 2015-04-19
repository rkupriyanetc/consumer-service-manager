package mk.ck.energy.csm.model;

import mk.ck.energy.csm.model.mongodb.MongoDocumentException;

public class MeterNotFoundException extends MongoDocumentException {
	
	private static final long	serialVersionUID	= 1L;
	
	public MeterNotFoundException() {}
	
	public MeterNotFoundException( final String message ) {
		super( message );
	}
	
	public MeterNotFoundException( final String message, final Throwable throwable ) {
		super( message, throwable );
	}
}