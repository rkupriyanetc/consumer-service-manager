package mk.ck.energy.csm.model.codecs;

import mk.ck.energy.csm.model.Meter;

import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

public class MeterCodec implements CollectibleCodec< Meter > {
	
	private static final String			DB_FIELD_ID										= "_id";
	
	static final String							DB_FIELD_CONSUMER_ID					= "consumer_id";
	
	static final String							DB_FIELD_METER_DEVICE_ID			= "meter_device_id";
	
	static final String							DB_FIELD_NUMBER								= "number";
	
	static final String							DB_FIELD_DIGITS								= "digits";
	
	static final String							DB_FIELD_ORDER								= "order";
	
	static final String							DB_FIELD_DATE_INSTALL					= "date_install";
	
	static final String							DB_FIELD_DATE_UNINSTALL				= "date_uninstall";
	
	static final String							DB_FIELD_DATE_TESTING					= "date_testing";
	
	static final String							DB_FIELD_MASTER_NAME					= "master_name";
	
	static final String							DB_FIELD_MIGHT_OUTTURN				= "might_outturn";
	
	static final String							DB_FIELD_LOCATION_METER_TYPE	= "location_meter";
	
	private final Codec< Document >	documentCodec;
	
	public MeterCodec() {
		this.documentCodec = new DocumentCodec();
	}
	
	public MeterCodec( final Codec< Document > codec ) {
		this.documentCodec = codec;
	}
	
	@Override
	public void encode( final BsonWriter writer, final Meter value, final EncoderContext encoderContext ) {
		final Document document = new Document( DB_FIELD_ID, value.getId() );
		document.append( DB_FIELD_CONSUMER_ID, value.getString( DB_FIELD_CONSUMER_ID ) );
		document.append( DB_FIELD_METER_DEVICE_ID, value.getString( DB_FIELD_METER_DEVICE_ID ) );
		document.append( DB_FIELD_NUMBER, value.getString( DB_FIELD_NUMBER ) );
		document.append( DB_FIELD_DIGITS, value.get( DB_FIELD_DIGITS ) );
		document.append( DB_FIELD_ORDER, value.get( DB_FIELD_ORDER ) );
		document.append( DB_FIELD_DATE_INSTALL, value.getLong( DB_FIELD_DATE_INSTALL ) );
		final Long un = value.getLong( DB_FIELD_DATE_UNINSTALL );
		if ( un != null )
			document.append( DB_FIELD_DATE_UNINSTALL, un.longValue() );
		document.append( DB_FIELD_DATE_TESTING, value.getLong( DB_FIELD_DATE_TESTING ) );
		document.append( DB_FIELD_MASTER_NAME, value.getString( DB_FIELD_MASTER_NAME ) );
		document.append( DB_FIELD_MIGHT_OUTTURN, value.get( DB_FIELD_MIGHT_OUTTURN ) );
		document.append( DB_FIELD_LOCATION_METER_TYPE, value.getString( DB_FIELD_LOCATION_METER_TYPE ) );
		documentCodec.encode( writer, document, encoderContext );
	}
	
	@Override
	public Class< Meter > getEncoderClass() {
		return Meter.class;
	}
	
	@Override
	public Meter decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final Meter meter = Meter.create();
		meter.setId( document.getString( DB_FIELD_ID ) );
		meter.setConsumerId( document.getString( DB_FIELD_CONSUMER_ID ) );
		meter.setMeterDeviceId( document.getString( DB_FIELD_METER_DEVICE_ID ) );
		meter.put( DB_FIELD_NUMBER, document.getString( DB_FIELD_NUMBER ) );
		meter.put( DB_FIELD_DIGITS, document.get( DB_FIELD_DIGITS ) );
		meter.put( DB_FIELD_ORDER, document.get( DB_FIELD_ORDER ) );
		meter.put( DB_FIELD_DATE_INSTALL, document.getLong( DB_FIELD_DATE_INSTALL ) );
		final Long un = document.getLong( DB_FIELD_DATE_UNINSTALL );
		if ( un != null )
			meter.put( DB_FIELD_DATE_UNINSTALL, un.longValue() );
		meter.put( DB_FIELD_DATE_TESTING, document.getLong( DB_FIELD_DATE_TESTING ) );
		meter.put( DB_FIELD_MASTER_NAME, document.getString( DB_FIELD_MASTER_NAME ) );
		meter.put( DB_FIELD_MIGHT_OUTTURN, document.get( DB_FIELD_MIGHT_OUTTURN ) );
		meter.put( DB_FIELD_LOCATION_METER_TYPE, document.getString( DB_FIELD_LOCATION_METER_TYPE ) );
		return meter;
	}
	
	@Override
	public boolean documentHasId( final Meter document ) {
		return document.getId() == null;
	}
	
	@Override
	public Meter generateIdIfAbsentFromDocument( final Meter document ) {
		if ( documentHasId( document ) ) {
			document.createId();
			return document;
		} else
			return document;
	}
	
	@Override
	public BsonValue getDocumentId( final Meter document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return BsonValue.class.cast( document.getId() );
	}
}