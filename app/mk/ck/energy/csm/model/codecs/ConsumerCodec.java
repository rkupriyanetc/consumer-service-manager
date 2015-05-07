package mk.ck.energy.csm.model.codecs;

import mk.ck.energy.csm.model.Consumer;

import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

public class ConsumerCodec implements CollectibleCodec< Consumer > {
	
	private static final String			DB_FIELD_ID							= "_id";
	
	private static final String			DB_FIELD_USER_ID				= "user_id";
	
	private static final String			DB_FIELD_FULLNAME				= "full_name";
	
	private static final String			DB_FIELD_ADDRESS				= "address";
	
	private static final String			DB_FIELD_ACTIVE					= "active";
	
	private static final String			DB_FIELD_DOCUMENTS			= "documents";
	
	private static final String			DB_FIELD_CONSUMER_TYPE	= "type";
	
	private static final String			DB_FIELD_STATUS_TYPE		= "status";
	
	private static final String			DB_FIELD_HOUSE_TYPE			= "house_type";
	
	private final Codec< Document >	documentCodec;
	
	public ConsumerCodec() {
		this.documentCodec = new DocumentCodec();
	}
	
	public ConsumerCodec( final Codec< Document > codec ) {
		this.documentCodec = codec;
	}
	
	@Override
	public void encode( final BsonWriter writer, final Consumer value, final EncoderContext encoderContext ) {
		final Document document = new Document( DB_FIELD_ID, value.getId() );
		final String user = value.getString( DB_FIELD_USER_ID );
		if ( user != null && !user.isEmpty() )
			document.append( DB_FIELD_USER_ID, user );
		document.append( DB_FIELD_FULLNAME, value.getString( DB_FIELD_FULLNAME ) );
		Object doc = value.get( DB_FIELD_ADDRESS );
		if ( doc != null )
			document.append( DB_FIELD_ADDRESS, doc );
		document.append( DB_FIELD_ACTIVE, value.isActive() );
		doc = value.get( DB_FIELD_DOCUMENTS );
		if ( doc != null )
			document.append( DB_FIELD_DOCUMENTS, doc );
		doc = value.get( DB_FIELD_CONSUMER_TYPE );
		if ( doc != null )
			document.append( DB_FIELD_CONSUMER_TYPE, doc );
		doc = value.get( DB_FIELD_STATUS_TYPE );
		if ( doc != null )
			document.append( DB_FIELD_STATUS_TYPE, doc );
		doc = value.get( DB_FIELD_HOUSE_TYPE );
		if ( doc != null )
			document.append( DB_FIELD_HOUSE_TYPE, doc );
		documentCodec.encode( writer, document, encoderContext );
	}
	
	@Override
	public Class< Consumer > getEncoderClass() {
		return Consumer.class;
	}
	
	@Override
	public Consumer decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final Consumer consumer = Consumer.create( document.getString( DB_FIELD_ID ) );
		final String user = document.getString( DB_FIELD_USER_ID );
		if ( user != null && !user.isEmpty() )
			consumer.setUserId( user );
		consumer.put( DB_FIELD_FULLNAME, document.getString( DB_FIELD_FULLNAME ) );
		Object doc = document.get( DB_FIELD_ADDRESS );
		if ( doc != null )
			consumer.put( DB_FIELD_ADDRESS, doc );
		consumer.setActive( document.getBoolean( DB_FIELD_ACTIVE ) );
		doc = document.get( DB_FIELD_DOCUMENTS );
		if ( doc != null )
			consumer.put( DB_FIELD_DOCUMENTS, doc );
		doc = document.get( DB_FIELD_CONSUMER_TYPE );
		if ( doc != null )
			consumer.put( DB_FIELD_CONSUMER_TYPE, doc );
		doc = document.get( DB_FIELD_STATUS_TYPE );
		if ( doc != null )
			consumer.put( DB_FIELD_STATUS_TYPE, doc );
		doc = document.get( DB_FIELD_HOUSE_TYPE );
		if ( doc != null )
			consumer.put( DB_FIELD_HOUSE_TYPE, doc );
		return consumer;
	}
	
	@Override
	public boolean documentHasId( final Consumer document ) {
		return document.getId() == null;
	}
	
	@Override
	public Consumer generateIdIfAbsentFromDocument( final Consumer document ) {
		if ( documentHasId( document ) ) {
			document.createId();
			return document;
		} else
			return document;
	}
	
	@Override
	public BsonValue getDocumentId( final Consumer document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return BsonValue.class.cast( document.getId() );
	}
}