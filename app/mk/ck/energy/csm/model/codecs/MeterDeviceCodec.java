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

public class MeterDeviceCodec implements CollectibleCodec< MeterDevice > {
	
	private static final String			DB_FIELD_ID							= "_id";
	
	private static final String			DB_FIELD_NAME						= "name";
	
	private static final String			DB_FIELD_PHASING				= "phasing";
	
	private static final String			DB_FIELD_METHOD_TYPE		= "method";
	
	private static final String			DB_FIELD_INDUCTIVE_TYPE	= "inductive";
	
	private static final String			DB_FIELD_REGISTER_TYPE	= "register";
	
	private static final String			DB_FIELD_PRECISION			= "precision";
	
	private static final String			DB_FIELD_INTERVAL				= "interval";
	
	private final Codec< Document >	documentCodec;
	
	public MeterDeviceCodec() {
		this.documentCodec = new DocumentCodec();
	}
	
	public MeterDeviceCodec( final Codec< Document > codec ) {
		this.documentCodec = codec;
	}
	
	@Override
	public void encode( final BsonWriter writer, final MeterDevice value, final EncoderContext encoderContext ) {
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
	public Class< MeterDevice > getEncoderClass() {
		return MeterDevice.class;
	}
	
	@Override
	public MeterDevice decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final MeterDevice meter = MeterDevice.create();
		meter.setId( document.getString( DB_FIELD_ID ) );
		meter.put( DB_FIELD_NAME, document.getString( DB_FIELD_NAME ) );
		Object o = document.get( DB_FIELD_PHASING );
		byte bp;
		if ( o != null )
			bp = ( ( Integer )o ).byteValue();
		else
			bp = ( byte )1;
		meter.setPhasing( bp );
		meter.setPrecision( document.getDouble( DB_FIELD_PRECISION ) );
		o = document.get( DB_FIELD_INTERVAL );
		if ( o != null )
			bp = ( ( Integer )o ).byteValue();
		else
			bp = ( byte )1;
		meter.setInterval( bp );
		meter.put( DB_FIELD_METHOD_TYPE, document.getString( DB_FIELD_METHOD_TYPE ) );
		meter.put( DB_FIELD_INDUCTIVE_TYPE, document.getString( DB_FIELD_INDUCTIVE_TYPE ) );
		meter.put( DB_FIELD_REGISTER_TYPE, document.getString( DB_FIELD_REGISTER_TYPE ) );
		return meter;
	}
	
	@Override
	public boolean documentHasId( final MeterDevice document ) {
		return document.getId() == null;
	}
	
	@Override
	public MeterDevice generateIdIfAbsentFromDocument( final MeterDevice document ) {
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