package mk.ck.energy.csm.model.auth;

import mk.ck.energy.csm.model.mongodb.MongoDocumentException;

/**
 * @author KYL
 */
public class InvalidTokenException extends MongoDocumentException {
	
	private static final long	serialVersionUID	= 1L;
	
	private final TokenType		tokenType;
	
	private final String			token;
	
	public InvalidTokenException( final TokenType tokenType, final String token ) {
		this.tokenType = tokenType;
		this.token = token;
	}
	
	public TokenType getTokenType() {
		return tokenType;
	}
	
	public String getToken() {
		return token;
	}
}
