package mk.ck.energy.csm.model.codecs;

import mk.ck.energy.csm.model.auth.User;

import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

public class UserCodec implements CollectibleCodec< User > {
	
	private static final String			DB_FIELD_ID								= "_id";
	
	private static final String			DB_FIELD_EMAIL						= "email";
	
	private static final String			DB_FIELD_NAME							= "name";
	
	private static final String			DB_FIELD_FIRST_NAME				= "first_name";
	
	private static final String			DB_FIELD_LAST_NAME				= "last_name";
	
	private static final String			DB_FIELD_LAST_LOGIN				= "last_login";
	
	private static final String			DB_FIELD_ACTIVE						= "active";
	
	private static final String			DB_FIELD_EMAIL_VALIDATED	= "validated";
	
	private static final String			DB_FIELD_ROLES						= "roles";
	
	private static final String			DB_FIELD_LINKED_ACCOUNTS	= "linkeds";
	
	private static final String			DB_FIELD_PERMISSIONS			= "permissions";
	
	private final Codec< Document >	documentCodec;
	
	public UserCodec() {
		this.documentCodec = new DocumentCodec();
	}
	
	public UserCodec( final Codec< Document > codec ) {
		this.documentCodec = codec;
	}
	
	@Override
	public void encode( final BsonWriter writer, final User value, final EncoderContext encoderContext ) {
		final Document document = new Document( DB_FIELD_ID, value.getId() );
		document.append( DB_FIELD_EMAIL, value.getEmail() );
		String st = value.getFirstName();
		if ( st != null )
			document.append( DB_FIELD_FIRST_NAME, st );
		st = value.getName();
		document.append( DB_FIELD_NAME, st );
		st = value.getLastName();
		if ( st != null )
			document.append( DB_FIELD_LAST_NAME, st );
		document.append( DB_FIELD_ACTIVE, value.isActive() );
		document.append( DB_FIELD_EMAIL_VALIDATED, value.isEmailValidated() );
		Object o = value.get( DB_FIELD_ROLES );
		if ( o != null )
			document.append( DB_FIELD_ROLES, o );
		o = value.get( DB_FIELD_LINKED_ACCOUNTS );
		if ( o != null )
			document.append( DB_FIELD_LINKED_ACCOUNTS, o );
		o = value.get( DB_FIELD_PERMISSIONS );
		if ( o != null )
			document.append( DB_FIELD_PERMISSIONS, o );
		document.append( DB_FIELD_LAST_LOGIN, value.getLastLogin() );
		documentCodec.encode( writer, document, encoderContext );
	}
	
	@Override
	public Class< User > getEncoderClass() {
		return User.class;
	}
	
	@Override
	public User decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final User user = User.create();
		user.setId( document.getString( DB_FIELD_ID ) );
		user.put( DB_FIELD_EMAIL, document.getString( DB_FIELD_EMAIL ) );
		user.setFirstName( document.getString( DB_FIELD_FIRST_NAME ) );
		user.put( DB_FIELD_NAME, document.getString( DB_FIELD_NAME ) );
		user.setLastName( document.getString( DB_FIELD_LAST_NAME ) );
		user.put( DB_FIELD_ACTIVE, document.getBoolean( DB_FIELD_ACTIVE ) );
		user.put( DB_FIELD_EMAIL_VALIDATED, document.getBoolean( DB_FIELD_EMAIL_VALIDATED ) );
		user.setRoles( document.get( DB_FIELD_ROLES ) );
		user.setLinkedAccounts( document.get( DB_FIELD_LINKED_ACCOUNTS ) );
		user.setPermission( document.get( DB_FIELD_PERMISSIONS ) );
		user.put( DB_FIELD_LAST_LOGIN, document.getLong( DB_FIELD_LAST_LOGIN ) );
		return user;
	}
	
	@Override
	public boolean documentHasId( final User document ) {
		return document.getId() == null;
	}
	
	@Override
	public User generateIdIfAbsentFromDocument( final User document ) {
		if ( documentHasId( document ) ) {
			document.createId();
			return document;
		} else
			return document;
	}
	
	@Override
	public BsonValue getDocumentId( final User document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return BsonValue.class.cast( document.getId() );
	}
}
