package mk.ck.energy.csm.model.codecs;

import mk.ck.energy.csm.model.auth.TokenAction;
import mk.ck.energy.csm.model.auth.TokenType;

import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

public class TokenActionCodec implements CollectibleCodec< TokenAction > {
	
	private static final String			DB_FIELD_ID						= "_id";
	
	static final String							DB_FIELD_USER_ID			= "user_id";
	
	static final String							DB_FIELD_TOKEN				= "token";
	
	static final String							DB_FIELD_TOKEN_TYPE		= "type";
	
	static final String							DB_FIELD_DATE_CREATED	= "created";
	
	static final String							DB_FIELD_DATE_EXPIRES	= "expires";
	
	private final Codec< Document >	documentCodec;
	
	public TokenActionCodec() {
		this.documentCodec = new DocumentCodec();
	}
	
	public TokenActionCodec( final Codec< Document > codec ) {
		this.documentCodec = codec;
	}
	
	@Override
	public void encode( final BsonWriter writer, final TokenAction value, final EncoderContext encoderContext ) {
		final Document document = new Document( DB_FIELD_ID, value.getId() );
		document.append( DB_FIELD_USER_ID, value.getUserId() );
		document.append( DB_FIELD_TOKEN, value.getToken() );
		document.append( DB_FIELD_TOKEN_TYPE, value.getString( DB_FIELD_TOKEN_TYPE ) );
		document.append( DB_FIELD_DATE_CREATED, value.getCreated() );
		document.append( DB_FIELD_DATE_EXPIRES, value.getExpires() );
		documentCodec.encode( writer, document, encoderContext );
	}
	
	@Override
	public Class< TokenAction > getEncoderClass() {
		return TokenAction.class;
	}
	
	@Override
	public TokenAction decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final TokenAction token = TokenAction.create();
		token.setId( document.getString( DB_FIELD_ID ) );
		token.setUserId( document.getString( DB_FIELD_USER_ID ) );
		token.setToken( document.getString( DB_FIELD_TOKEN ) );
		token.setTokenType( TokenType.valueOf( document.getString( DB_FIELD_TOKEN_TYPE ) ) );
		token.setCreated( document.getLong( DB_FIELD_DATE_CREATED ) );
		token.setExpires( document.getLong( DB_FIELD_DATE_EXPIRES ) );
		return token;
	}
	
	@Override
	public boolean documentHasId( final TokenAction document ) {
		return document.getId() == null;
	}
	
	@Override
	public TokenAction generateIdIfAbsentFromDocument( final TokenAction document ) {
		if ( documentHasId( document ) ) {
			document.createId();
			return document;
		} else
			return document;
	}
	
	@Override
	public BsonValue getDocumentId( final TokenAction document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return BsonValue.class.cast( document.getId() );
	}
}
