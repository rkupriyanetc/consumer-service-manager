package mk.ck.energy.csm.model.mongodb;

public class MongoDocumentException extends Exception {
	
	private static final long	serialVersionUID	= 1L;
	
	public MongoDocumentException() {}
	
	public MongoDocumentException( final String message ) {
		super( message );
	}
	
	public MongoDocumentException( final String message, final Throwable throwable ) {
		super( message, throwable );
	}
}
