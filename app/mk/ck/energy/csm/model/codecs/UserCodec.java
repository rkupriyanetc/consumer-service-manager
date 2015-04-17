package mk.ck.energy.csm.model.codecs;

import java.util.UUID;

import mk.ck.energy.csm.model.auth.User;

import org.bson.BsonReader;
import org.bson.BsonString;
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
		/*
		 * final Document document = new Document();
		 * final String id = value.getId();
		 * final String email = value.getEmail();
		 * final List< ? extends Role > roles = value.getRoles();
		 * final List< LinkedAccount > linkeds = value.getLinkedAccounts();
		 * final boolean active = value.isActive();
		 * if ( null != id )
		 * document.put( DB_FIELD_ID, id );
		 * if ( null != email )
		 * document.put( DB_FIELD_EMAIL, email );
		 * if ( null != roles )
		 * document.put( DB_FIELD_ROLES, roles );
		 * if ( null != linkeds )
		 * document.put( DB_FIELD_LINKED_ACCOUNTS, linkeds );
		 * if ( active )
		 * document.put( DB_FIELD_ACTIVE, active );
		 */
		documentCodec.encode( writer, value, encoderContext );
	}
	
	@Override
	public Class< User > getEncoderClass() {
		return User.class;
	}
	
	@Override
	public User decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final User user = User.create( document.getString( DB_FIELD_ID ) );
		user.setEmail( document.getString( DB_FIELD_EMAIL ) );
		user.setActive( document.getBoolean( DB_FIELD_ACTIVE ) );
		user.setName( document.getString( DB_FIELD_NAME ) );
		user.setFirstName( document.getString( DB_FIELD_FIRST_NAME ) );
		user.setLastName( document.getString( DB_FIELD_LAST_NAME ) );
		user.setLastLogin( document.getLong( DB_FIELD_LAST_LOGIN ) );
		user.setEmailValidated( document.getBoolean( DB_FIELD_EMAIL_VALIDATED ) );
		user.setRoles( document.get( DB_FIELD_ROLES ) );
		/*
		 * final List< Document > o = ( List< Document > )document.get(
		 * DB_FIELD_ROLES );
		 * for ( final Document doc : o ) {
		 * final String nameRole = doc.getString( "name" );
		 * user.addRole( UserRole.getInstance( nameRole ) );
		 * }
		 */
		return user;
	}
	
	@Override
	public boolean documentHasId( final User document ) {
		return document.getId() == null;
	}
	
	@Override
	public User generateIdIfAbsentFromDocument( final User document ) {
		if ( documentHasId( document ) ) {
			document.setId( UUID.randomUUID().toString().toLowerCase() );
			return document;
		} else
			return document;
	}
	
	@Override
	public BsonValue getDocumentId( final User document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return new BsonString( document.getId() );
	}
}
