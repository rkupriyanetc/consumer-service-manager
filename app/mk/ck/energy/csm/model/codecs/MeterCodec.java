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
	
	private static final String			DB_FIELD_CONSUMER_ID					= "consumer_id";
	
	private static final String			DB_FIELD_METER_DEVICE_ID			= "meter_device_id";
	
	private static final String			DB_FIELD_NUMBER								= "number";
	
	private static final String			DB_FIELD_DIGITS								= "digits";
	
	private static final String			DB_FIELD_ORDER								= "order";
	
	private static final String			DB_FIELD_DATE_INSTALL					= "date_install";
	
	private static final String			DB_FIELD_DATE_UNINSTALL				= "date_uninstall";
	
	private static final String			DB_FIELD_DATE_TESTING					= "date_testing";
	
	private static final String			DB_FIELD_MASTER_NAME					= "master_name";
	
	private static final String			DB_FIELD_MIGHT_OUTTURN				= "might_outturn";
	
	private static final String			DB_FIELD_LOCATION_METER_TYPE	= "location_meter";
	
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
		Object o = value.get( DB_FIELD_NUMBER );
		if ( o != null )
			document.append( DB_FIELD_NUMBER, o );
		byte by = value.getDigits();
		if ( by > 0 )
			document.append( DB_FIELD_DIGITS, by );
		final short or = value.getOrder();
		if ( or > 0 )
			document.append( DB_FIELD_ORDER, or );
		o = value.get( DB_FIELD_DATE_INSTALL );
		long lon;
		if ( o != null ) {
			lon = ( ( Long )o ).longValue();
			document.append( DB_FIELD_DATE_INSTALL, lon );
		}
		o = value.get( DB_FIELD_DATE_UNINSTALL );
		if ( o != null ) {
			lon = ( ( Long )o ).longValue();
			document.append( DB_FIELD_DATE_UNINSTALL, lon );
		}
		o = value.get( DB_FIELD_DATE_TESTING );
		if ( o != null ) {
			lon = ( ( Long )o ).longValue();
			document.append( DB_FIELD_DATE_TESTING, lon );
		}
		o = value.get( DB_FIELD_MASTER_NAME );
		if ( o != null )
			document.append( DB_FIELD_MASTER_NAME, o );
		o = value.get( DB_FIELD_LOCATION_METER_TYPE );
		if ( o != null )
			document.append( DB_FIELD_LOCATION_METER_TYPE, o );
		by = value.getMightOutturn();
		if ( by > 0 )
			document.append( DB_FIELD_MIGHT_OUTTURN, by );
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
		Object o = document.get( DB_FIELD_NUMBER );
		if ( o != null )
			meter.put( DB_FIELD_NUMBER, o );
		o = document.get( DB_FIELD_DIGITS );
		byte by;
		if ( o != null ) {
			by = ( ( Integer )o ).byteValue();
			if ( by > 0 )
				meter.put( DB_FIELD_DIGITS, by );
		}
		o = document.get( DB_FIELD_ORDER );
		short or;
		if ( o != null ) {
			or = ( ( Integer )o ).shortValue();
			if ( or > 0 )
				meter.put( DB_FIELD_ORDER, or );
		}
		o = document.get( DB_FIELD_DATE_INSTALL );
		long un;
		if ( o != null ) {
			un = ( ( Long )o ).longValue();
			meter.put( DB_FIELD_DATE_INSTALL, un );
		}
		o = document.get( DB_FIELD_DATE_UNINSTALL );
		if ( o != null ) {
			un = ( ( Long )o ).longValue();
			meter.put( DB_FIELD_DATE_UNINSTALL, un );
		}
		o = document.get( DB_FIELD_DATE_TESTING );
		if ( o != null ) {
			un = ( ( Long )o ).longValue();
			meter.put( DB_FIELD_DATE_TESTING, un );
		}
		o = document.get( DB_FIELD_MASTER_NAME );
		if ( o != null )
			meter.put( DB_FIELD_MASTER_NAME, o );
		o = document.get( DB_FIELD_LOCATION_METER_TYPE );
		if ( o != null )
			meter.put( DB_FIELD_LOCATION_METER_TYPE, o );
		o = document.get( DB_FIELD_MIGHT_OUTTURN );
		if ( o != null ) {
			by = ( ( Integer )o ).byteValue();
			if ( by > 0 )
				meter.put( DB_FIELD_MIGHT_OUTTURN, by );
		}
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