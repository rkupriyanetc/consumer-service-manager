package mk.ck.energy.csm.model.codecs;

import mk.ck.energy.csm.model.MeterDevice;

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
	
	private static final String			DB_FIELD_DOCUMENT				= "document";
	
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
		document.append( DB_FIELD_NAME, value.getName() );
		document.append( DB_FIELD_PHASING, value.getPhasing() );
		document.append( DB_FIELD_PRECISION, value.getPrecision() );
		document.append( DB_FIELD_INTERVAL, value.getInterval() );
		document.append( DB_FIELD_METHOD_TYPE, value.getString( DB_FIELD_METHOD_TYPE ) );
		document.append( DB_FIELD_INDUCTIVE_TYPE, value.getString( DB_FIELD_INDUCTIVE_TYPE ) );
		document.append( DB_FIELD_REGISTER_TYPE, value.getString( DB_FIELD_REGISTER_TYPE ) );
		documentCodec.encode( writer, document, encoderContext );
	}
	
	@Override
	public Class< Consumer > getEncoderClass() {
		return Consumer.class;
	}
	
	@Override
	public Consumer decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final Consumer meter = Consumer.create();
		meter.setId( document.getString( DB_FIELD_ID ) );
		meter.put( DB_FIELD_NAME, document.getString( DB_FIELD_NAME ) );
		meter.setPhasing( ( byte )document.get( DB_FIELD_PHASING ) );
		meter.setPrecision( document.getDouble( DB_FIELD_PRECISION ) );
		meter.setInterval( ( byte )document.get( DB_FIELD_INTERVAL ) );
		meter.put( DB_FIELD_METHOD_TYPE, document.getString( DB_FIELD_METHOD_TYPE ) );
		meter.put( DB_FIELD_INDUCTIVE_TYPE, document.getString( DB_FIELD_INDUCTIVE_TYPE ) );
		meter.put( DB_FIELD_REGISTER_TYPE, document.getString( DB_FIELD_REGISTER_TYPE ) );
		return meter;
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
	public BsonValue getDocumentId( final MeterDevice document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return BsonValue.class.cast( document.getId() );
	}
}