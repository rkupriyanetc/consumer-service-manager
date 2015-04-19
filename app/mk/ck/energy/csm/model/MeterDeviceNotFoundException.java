package mk.ck.energy.csm.model;

import mk.ck.energy.csm.model.mongodb.MongoDocumentException;

public class MeterDeviceNotFoundException extends MongoDocumentException {
	
	private static final long	serialVersionUID	= 1L;
	
	public MeterDeviceNotFoundException() {}
	
	public MeterDeviceNotFoundException( final String message ) {
		super( message );
	}
	
	public MeterDeviceNotFoundException( final String message, final Throwable throwable ) {
		super( message, throwable );
	}
}