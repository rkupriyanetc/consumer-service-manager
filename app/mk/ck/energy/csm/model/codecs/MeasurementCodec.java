package mk.ck.energy.csm.model.codecs;

import mk.ck.energy.csm.model.Measurement;

import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

public class MeasurementCodec implements CollectibleCodec< Measurement > {
	
	private static final String			DB_FIELD_ID								= "_id";
	
	private static final String			DB_FIELD_METER_DEVICE_ID	= "meter_device_id";
	
	private static final String			DB_FIELD_MEASUREMENT_DATE	= "measurement_date";
	
	private static final String			DB_FIELD_MASTER_NAME			= "master_name";
	
	private static final String			DB_FIELD_TRANSFER_TYPE		= "transfer_type";
	
	private final Codec< Document >	documentCodec;
	
	public MeasurementCodec() {
		this.documentCodec = new DocumentCodec();
	}
	
	public MeasurementCodec( final Codec< Document > codec ) {
		this.documentCodec = codec;
	}
	
	@Override
	public void encode( final BsonWriter writer, final Measurement value, final EncoderContext encoderContext ) {
		final Document document = new Document( DB_FIELD_ID, value.getId() );
		document.append( DB_FIELD_METER_DEVICE_ID, value.getString( DB_FIELD_METER_DEVICE_ID ) );
		Object o = value.get( DB_FIELD_MEASUREMENT_DATE );
		long lon;
		if ( o != null ) {
			lon = ( ( Long )o ).longValue();
			document.append( DB_FIELD_MEASUREMENT_DATE, lon );
		}
		o = value.get( DB_FIELD_MASTER_NAME );
		if ( o != null )
			document.append( DB_FIELD_MASTER_NAME, o );
		o = value.get( DB_FIELD_TRANSFER_TYPE );
		if ( o != null )
			document.append( DB_FIELD_TRANSFER_TYPE, o );
		documentCodec.encode( writer, document, encoderContext );
	}
	
	@Override
	public Class< Measurement > getEncoderClass() {
		return Measurement.class;
	}
	
	@Override
	public Measurement decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final Measurement measurement = Measurement.create();
		measurement.setId( document.getString( DB_FIELD_ID ) );
		Object o = document.get( DB_FIELD_MEASUREMENT_DATE );
		long un;
		if ( o != null ) {
			un = ( ( Long )o ).longValue();
			measurement.put( DB_FIELD_MEASUREMENT_DATE, un );
		}
		o = document.get( DB_FIELD_MASTER_NAME );
		if ( o != null )
			measurement.put( DB_FIELD_MASTER_NAME, o );
		o = document.get( DB_FIELD_TRANSFER_TYPE );
		if ( o != null )
			measurement.put( DB_FIELD_TRANSFER_TYPE, o );
		return measurement;
	}
	
	@Override
	public boolean documentHasId( final Measurement document ) {
		return document.getId() == null;
	}
	
	@Override
	public Measurement generateIdIfAbsentFromDocument( final Measurement document ) {
		if ( documentHasId( document ) ) {
			document.createId();
			return document;
		} else
			return document;
	}
	
	@Override
	public BsonValue getDocumentId( final Measurement document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return BsonValue.class.cast( document.getId() );
	}
}
