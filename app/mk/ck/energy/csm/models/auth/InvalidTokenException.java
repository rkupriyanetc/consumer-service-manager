package mk.ck.energy.csm.models.auth;

/**
 * @author KYL
 */
public class InvalidTokenException extends Exception {
	
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
