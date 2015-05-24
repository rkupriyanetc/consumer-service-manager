package mk.ck.energy.csm.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

public class Meter extends CSMAbstractDocument< Meter > {
	
	private static final long		serialVersionUID							= 1L;
	
	private static final String	COLLECTION_NAME_METERS				= "meters";
	
	private static final String	DB_FIELD_CONSUMER_ID					= "consumer_id";
	
	private static final String	DB_FIELD_METER_DEVICE_ID			= "meter_device_id";
	
	private static final String	DB_FIELD_NUMBER								= "number";
	
	private static final String	DB_FIELD_DIGITS								= "digits";
	
	private static final String	DB_FIELD_ORDER								= "order";
	
	private static final String	DB_FIELD_DATE_INSTALL					= "date_install";
	
	private static final String	DB_FIELD_DATE_UNINSTALL				= "date_uninstall";
	
	private static final String	DB_FIELD_DATE_TESTING					= "date_testing";
	
	private static final String	DB_FIELD_MASTER_NAME					= "master_name";
	
	private static final String	DB_FIELD_MIGHT_OUTTURN				= "might_outturn";
	
	private static final String	DB_FIELD_LOCATION_METER_TYPE	= "location_meter";
	
	public static Date					MAXDATE;
	
	public static Date					MAXDATE_PAKED;
	static {
		try {
			MAXDATE = new SimpleDateFormat( "yyyy.mm.dd" ).parse( "9999.12.31" );
			MAXDATE_PAKED = new SimpleDateFormat( "yyyy-mm-dd" ).parse( "2049-01-01" );
		}
		catch ( final Exception e ) {
			MAXDATE = new Date( 9999 * 12 * 31 * 24 * 60 * 60 * 1000 );
			MAXDATE_PAKED = new Date( 2049 * 24 * 60 * 60 * 1000 );
		}
	}
	
	private MeterDevice					meterDevice;
	
	/*
	 * Можливі пломби
	 */
	private final List< Plumb >	plumbs;
	
	private Meter() {
		plumbs = new LinkedList<>();
	}
	
	public static Meter create() {
		return new Meter();
	}
	
	public static Meter create( final String consumerId, final MeterDevice meterDevice, final String number, final byte digits,
			final long dateInstall, final short order, final String masterName, final LocationMeterType locationMeter ) {
		final Meter meter = new Meter();
		meter.setConsumerId( consumerId );
		meter.setMeterDevice( meterDevice );
		meter.setNumber( number );
		meter.setDigits( digits );
		meter.setDateInstall( dateInstall );
		meter.setOrder( order );
		meter.setMasterName( masterName );
		meter.setDateUninstall( MAXDATE.getTime() );
		meter.setLocationMeter( locationMeter == null ? LocationMeterType.APARTMENT : locationMeter );
		return meter;
	}
	
	public String getConsumerId() {
		return getString( DB_FIELD_CONSUMER_ID );
	}
	
	public void setConsumerId( final String consumerId ) {
		put( DB_FIELD_CONSUMER_ID, consumerId );
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
	 * Номер лічильника
	 */
	public String getNumber() {
		return getString( DB_FIELD_NUMBER );
	}
	
	public void setNumber( final String number ) {
		put( DB_FIELD_NUMBER, number );
	}
	
	/*
	 * Кількість значних цифр
	 */
	public byte getDigits() {
		final Object digits = get( DB_FIELD_DIGITS );
		if ( digits != null )
			return ( ( Byte )digits ).byteValue();
		else
			return 0;
	}
	
	public void setDigits( final byte digits ) {
		put( DB_FIELD_DIGITS, digits );
	}
	
	/*
	 * Початок дії ( дата встановлення ) лічильника
	 */
	public long getDateInstall() {
		return getLong( DB_FIELD_DATE_INSTALL );
	}
	
	public void setDateInstall( final long dateInstall ) {
		put( DB_FIELD_DATE_INSTALL, dateInstall );
	}
	
	/*
	 * Закінчення дії ( дата зняття ) лічильника
	 */
	public long getDateUninstall() {
		return getLong( DB_FIELD_DATE_UNINSTALL );
	}
	
	public void setDateUninstall( final long dateUninstall ) {
		put( DB_FIELD_DATE_UNINSTALL, dateUninstall );
	}
	
	/*
	 * Номер розпорядження, згідно якого лічильник було встановлено
	 */
	public short getOrder() {
		final Object order = get( DB_FIELD_ORDER );
		if ( order != null )
			return ( ( Short )order ).shortValue();
		else
			return 0;
	}
	
	public void setOrder( final short order ) {
		put( DB_FIELD_ORDER, order );
	}
	
	/*
	 * Остання дата повірки лічильника
	 */
	public long getDateTesting() {
		return getLong( DB_FIELD_DATE_TESTING );
	}
	
	public void setDateTesting( final long testingDate ) {
		put( DB_FIELD_DATE_TESTING, testingDate );
	}
	
	/*
	 * Майстер, що встановив лічильник
	 */
	public String getMasterName() {
		return getString( DB_FIELD_MASTER_NAME );
	}
	
	public void setMasterName( final String masterName ) {
		put( DB_FIELD_MASTER_NAME, masterName );
	}
	
	/*
	 * Місце установки лічильника
	 */
	public LocationMeterType getLocationMeter() {
		return LocationMeterType.valueOf( getString( DB_FIELD_LOCATION_METER_TYPE ) );
	}
	
	public void setLocationMeter( final LocationMeterType locationMeter ) {
		put( DB_FIELD_LOCATION_METER_TYPE, locationMeter.name() );
	}
	
	/*
	 * Можлива потужність ( 5 А - 150 А )
	 */
	public byte getMightOutturn() {
		final Object might = get( DB_FIELD_MIGHT_OUTTURN );
		if ( might != null )
			return ( ( Byte )might ).byteValue();
		else
			return 0;
	}
	
	public void setMightOutturn( final byte mightOutturn ) {
		put( DB_FIELD_MIGHT_OUTTURN, mightOutturn );
	}
	
	public List< Plumb > getPlumbs() {
		return plumbs;
	}
	
	public boolean addPlumb( final Plumb plumb ) {
		return plumbs.add( plumb );
	}
	
	@Override
	public boolean equals( final Object o ) {
		if ( o == null || !( o instanceof Meter ) )
			return false;
		final Meter meter = ( Meter )o;
		return meter.getConsumerId().equals( getConsumerId() ) && meter.getMeterDevice().equals( getMeterDevice() )
				&& meter.getNumber().equals( getNumber() ) && meter.getDateInstall() == getDateInstall()
				&& meter.getDateUninstall() == getDateUninstall() && meter.getOrder() == getOrder()
				&& meter.getDateTesting() == getDateTesting() && meter.getDigits() == getDigits()
				&& meter.getMightOutturn() == getMightOutturn() && meter.getLocationMeter().equals( getLocationMeter() );
	}
	
	public void save() throws ImpossibleCreatingException {
		final Bson value = Filters.and( Filters.eq( DB_FIELD_METER_DEVICE_ID, getMeterDevice().getId() ),
				Filters.eq( DB_FIELD_NUMBER, getNumber() ), Filters.eq( DB_FIELD_DATE_INSTALL, getDateInstall() ) );
		final Meter meter = getCollection().find( value, Meter.class ).first();
		if ( meter == null )
			insertIntoDB();
		else {
			final String meterName = this.toString();
			LOGGER.warn( "Cannot save Meter. Meter already exists: {}", meterName );
		}
	}
	
	public static List< Meter > findByConsumerId( final String consumerId ) throws MeterNotFoundException {
		if ( consumerId != null && !consumerId.isEmpty() ) {
			final MongoCursor< Meter > cur = getMongoCollection().find( Filters.eq( DB_FIELD_CONSUMER_ID, consumerId ) )
					.sort( Filters.eq( DB_FIELD_DATE_INSTALL, 1 ) ).iterator();
			final List< Meter > userMeters = new LinkedList<>();
			if ( cur == null )
				throw new MeterNotFoundException( "The Consumer was found by " + consumerId );
			while ( cur.hasNext() ) {
				final Meter o = cur.next();
				userMeters.add( o );
			}
			return userMeters;
		} else
			throw new IllegalArgumentException( "UserId should not be empty in Meter.findByUserId( userId )" );
	}
	
	public static Bson makeFilterToId( final String value ) {
		return Filters.eq( DB_FIELD_ID, value );
	}
	
	public static Bson makeFilterToConsumerId( final String value ) {
		return Filters.eq( DB_FIELD_CONSUMER_ID, value );
	}
	
	public static Bson makeFilterToMeterDeviceId( final String value ) {
		return Filters.eq( DB_FIELD_METER_DEVICE_ID, value );
	}
	
	public static Bson makeFilterToDateInstall( final String value ) {
		return Filters.eq( DB_FIELD_DATE_INSTALL, value );
	}
	
	public static Bson makeFilterToDateUninstall( final String value ) {
		return Filters.eq( DB_FIELD_DATE_UNINSTALL, value );
	}
	
	public static Bson makeFilterToDateTesting( final String value ) {
		return Filters.eq( DB_FIELD_DATE_TESTING, value );
	}
	
	public static Bson makeFilterToNumber( final String value ) {
		return Filters.eq( DB_FIELD_NUMBER, value );
	}
	
	public static Bson makeFilterToOrder( final String value ) {
		return Filters.eq( DB_FIELD_ORDER, value );
	}
	
	public static Bson makeFilterToDigits( final String value ) {
		return Filters.eq( DB_FIELD_DIGITS, value );
	}
	
	public static Bson makeFilterToMasterName( final String value ) {
		return Filters.eq( DB_FIELD_MASTER_NAME, value );
	}
	
	public static Bson makeFilterToLocationType( final String value ) {
		return Filters.eq( DB_FIELD_LOCATION_METER_TYPE, value );
	}
	
	public static Bson makeFilterToMightOutturn( final String value ) {
		return Filters.eq( DB_FIELD_MIGHT_OUTTURN, value );
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer( "Марка - " );
		sb.append( getMeterDevice().getName() );
		sb.append( " № " );
		sb.append( getNumber() );
		sb.append( " дата: " );
		sb.append( getDateInstall() );
		return sb.toString();
	}
	
	@Override
	public < TDocument >BsonDocument toBsonDocument( final Class< TDocument > documentClass, final CodecRegistry codecRegistry ) {
		return new BsonDocumentWrapper< Meter >( this, codecRegistry.get( Meter.class ) );
	}
	
	public static MongoCollection< Meter > getMongoCollection() {
		final MongoCollection< Meter > collection = getDatabase().getCollection( COLLECTION_NAME_METERS, Meter.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< Meter > getCollection() {
		return getMongoCollection();
	}
}
