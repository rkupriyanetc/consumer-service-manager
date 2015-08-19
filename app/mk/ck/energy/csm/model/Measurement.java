package mk.ck.energy.csm.model;

import java.util.LinkedHashMap;
import java.util.Map;

import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.client.MongoCollection;

public class Measurement extends CSMAbstractDocument< Measurement > {
	
	private static final long		serialVersionUID							= 1L;
	
	private static final String	COLLECTION_NAME_MEASUREMENTS	= "measurements";
	
	private static final String	DB_FIELD_METER_DEVICE_ID			= "meter_device_id";
	
	private static final String	DB_FIELD_MEASUREMENT_DATE			= "measurement_date";
	
	private static final String	DB_FIELD_MASTER_NAME					= "master_name";
	
	private static final String	DB_FIELD_TRANSFER_TYPE				= "transfer_type";
	
	enum TransferValueType {
		/**
		 * Контрольний огляд лічильника
		 */
		CONTROL,
		/**
		 * Повідомлення показів усно, по телефону, в квитанції чи через сайт.
		 * Відповідно, поле <code>master_name</code> має бути заповнено так: Усно,
		 * Телефон, Квитанція, Website.
		 */
		ABONENT,
		/**
		 * Запис показників при монтажі лічильника
		 */
		INSTALLATION,
		/**
		 * Запис показників при демонтажі лічильника
		 */
		DEINSTALLATION,
	}
	
	enum MultipleData {
		DAY, NIGHT, PEAK, TOTAL, ;
	}
	
	private MeterDevice									meterDevice;
	
	private final Map< String, Double >	values;
	
	private Measurement() {
		this.values = new LinkedHashMap< String, Double >();
	}
	
	public static Measurement create() {
		return new Measurement();
	}
	
	public static Measurement create( final MeterDevice meterDevice, final long measurementDate, final String master,
			final TransferValueType transferValueType ) {
		final Measurement mes = new Measurement();
		mes.setMeterDevice( meterDevice );
		mes.setMeasurementDate( measurementDate );
		mes.setMasterName( master );
		mes.setTransferValueType( transferValueType == null ? TransferValueType.ABONENT : transferValueType );
		return mes;
	}
	
	public MeterDevice getMeterDevice() {
		return meterDevice;
	}
	
	public void setMeterDevice( final MeterDevice meterDevice ) {
		if ( meterDevice != null ) {
			if ( !meterDevice.equals( this.meterDevice ) ) {
				this.meterDevice = meterDevice;
				put( DB_FIELD_METER_DEVICE_ID, meterDevice.getId() );
			}
		} else
			remove( DB_FIELD_METER_DEVICE_ID );
	}
	
	public String getMeterDeviceId() {
		return getString( DB_FIELD_METER_DEVICE_ID );
	}
	
	public void setMeterDeviceId( final String meterDeviceId ) {
		if ( meterDeviceId != null && !meterDeviceId.isEmpty() ) {
			final String deviceId = getMeterDeviceId();
			if ( !meterDeviceId.equals( deviceId ) )
				try {
					this.meterDevice = MeterDevice.findById( meterDeviceId );
					put( DB_FIELD_METER_DEVICE_ID, meterDeviceId );
				}
				catch ( final MeterDeviceNotFoundException mdnfe ) {
					LOGGER.warn( "Sorry. Cannot find MeterDevice by {}", meterDeviceId );
					remove( DB_FIELD_METER_DEVICE_ID );
				}
		} else
			remove( DB_FIELD_METER_DEVICE_ID );
	}
	
	/*
	 * Дата заміру лічильника
	 */
	public long getMeasurementDate() {
		return getLong( DB_FIELD_MEASUREMENT_DATE );
	}
	
	public void setMeasurementDate( final long dateMeasurement ) {
		put( DB_FIELD_MEASUREMENT_DATE, dateMeasurement );
	}
	
	public String getMasterName() {
		return getString( DB_FIELD_MASTER_NAME );
	}
	
	public void setMasterName( final String masterName ) {
		put( DB_FIELD_MASTER_NAME, masterName );
	}
	
	public TransferValueType getTransferValueType() {
		return TransferValueType.valueOf( getString( DB_FIELD_TRANSFER_TYPE ) );
	}
	
	public void setTransferValueType( final TransferValueType transferValueType ) {
		put( DB_FIELD_TRANSFER_TYPE, transferValueType.name() );
	}
	
	/*
	 * DBObject getDBObject() {
	 * final DBObject doc = new BasicDBObject( "meter_device_id", meterDeviceId );
	 * doc.put( "date_metering", dateMetering );
	 * if ( inspector != null )
	 * doc.put( "inspector", inspector );
	 * doc.put( "transfer_data_type", transferDataType.name() );
	 * if ( !values.isEmpty() )
	 * doc.put( "values", values );
	 * return doc;
	 * }
	 * public void save() {
	 * final DBObject query = new BasicDBObject( "meter_device_id", meterDeviceId
	 * );
	 * query.put( "date_metering", dateMetering );
	 * final DBObject doc = getDBObject();
	 * getMeteringDataCollection().update( query, new BasicDBObject( "$set", doc
	 * ), true, false );
	 * }
	 */
	@Override
	public < TDocument >BsonDocument toBsonDocument( final Class< TDocument > documentClass, final CodecRegistry codecRegistry ) {
		return new BsonDocumentWrapper< Measurement >( this, codecRegistry.get( Measurement.class ) );
	}
	
	public static MongoCollection< Measurement > getMongoCollection() {
		final MongoCollection< Measurement > collection = getDatabase().getCollection( COLLECTION_NAME_MEASUREMENTS,
				Measurement.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< Measurement > getCollection() {
		return getMongoCollection();
	}
}
