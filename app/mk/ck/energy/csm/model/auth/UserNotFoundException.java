package mk.ck.energy.csm.model.auth;

import mk.ck.energy.csm.model.mongodb.MongoDocumentException;

/**
 * @author RVK
 */
public class UserNotFoundException extends MongoDocumentException {
	
	private static final long	serialVersionUID	= 1L;
	
	public UserNotFoundException() {}
}
