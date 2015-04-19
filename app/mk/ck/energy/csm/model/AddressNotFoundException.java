package mk.ck.energy.csm.model;

import mk.ck.energy.csm.model.mongodb.MongoDocumentException;

public class AddressNotFoundException extends MongoDocumentException {
	
	private static final long	serialVersionUID	= 1L;
	
	public AddressNotFoundException() {}
	
	public AddressNotFoundException( final String message ) {
		super( message );
	}
	
	public AddressNotFoundException( final String message, final Throwable throwable ) {
		super( message, throwable );
	}
}
