package mk.ck.energy.csm.model.auth;

import org.bson.Document;

import com.feth.play.module.pa.user.AuthUserIdentity;

/**
 * @author RVK
 */
public class LinkedAccount {
	
	protected static final String	DB_FIELD_PROVIDER	= "provider";
	
	protected static final String	DB_FIELD_USER_ID	= "user_id";
	
	private final String					provider;
	
	private final String					userId;
	
	private LinkedAccount( final String provider, final String userId ) {
		this.provider = provider;
		this.userId = userId;
	}
	
	public static LinkedAccount getInstance( final String provider, final String userId ) {
		return new LinkedAccount( provider, userId );
	}
	
	public static LinkedAccount getInstance( final AuthUserIdentity authUserIdentity ) {
		return new LinkedAccount( authUserIdentity.getProvider(), authUserIdentity.getId() );
	}
	
	public static LinkedAccount getInstance( final LinkedAccount linkedAccount ) {
		return new LinkedAccount( linkedAccount.provider, linkedAccount.userId );
	}
	
	public static LinkedAccount getInstance( final Document linkedAccount ) {
		return new LinkedAccount( linkedAccount.getString( DB_FIELD_PROVIDER ), linkedAccount.getString( DB_FIELD_USER_ID ) );
	}
	
	public String getProvider() {
		return provider;
	}
	
	public String getUserId() {
		return userId;
	}
	
	Document getDocument() {
		return new Document( DB_FIELD_PROVIDER, provider ).append( DB_FIELD_USER_ID, userId );
	}
}