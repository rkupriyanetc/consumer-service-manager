package mk.ck.energy.csm.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;

public class Meter {
	
	public static Date					MAXDATE;
	
	public static Date					MAXDATE_PAKED;
	static {
		try {
			MAXDATE = new SimpleDateFormat( "yyyy.mm.dd" ).parse( "9999.12.31" );
			MAXDATE_PAKED = new SimpleDateFormat( "yyyy.mm.dd" ).parse( "2049.01.01" );
		}
		catch ( final Exception e ) {
			MAXDATE = new Date( 9999 * 12 * 31 * 24 * 60 * 60 * 1000 );
			MAXDATE_PAKED = new Date( 2049 * 24 * 60 * 60 * 1000 );
		}
	}
	
	public static final short		UPDATING_READING_ALL						= 0;
	
	public static final short		UPDATING_READING_NUMBER					= 1;
	
	public static final short		UPDATING_READING_DIGITS					= 2;
	
	public static final short		UPDATING_READING_UNINSTALL_DATE	= 4;
	
	public static final short		UPDATING_READING_ORDER					= 8;
	
	public static final short		UPDATING_READING_TESTING_DATE		= 16;
	
	public static final short		UPDATING_READING_INSPECTOR			= 32;
	
	public static final short		UPDATING_READING_PLACE_INSTALL	= 64;
	
	public static final short		UPDATING_READING_MIGHTOUTTURN		= 128;
	
	public static final short		UPDATING_READING_PLUMBS					= 256;
	
	private static final Logger	LOGGER													= LoggerFactory.getLogger( Meter.class );
	
	private long								id;
	
	private long								meterDeviceId;
	
	private MeterDevice					meterDevice;
	
	/*
	 * Номер лвчильника
	 */
	private String							number;
	
	/*
	 * Кількість значних цифр
	 */
	private byte								digits;
	
	/*
	 * Початок дії ( дата встановлення ) лічильника
	 */
	private long								installDate;
	
	/*
	 * Закінчення дії ( дата зняття ) лічильника
	 */
	private long								uninstallDate;
	
	/*
	 * Номер розпорядження, згідно якого лічильник було встановлено
	 */
	private short								order;
	
	/*
	 * Остання дата повірки лічильника
	 */
	private long								testingDate;
	
	/*
	 * Інспектор, що встановив лічильник
	 */
	private String							inspector;
	
	/*
	 * Місце установки лічильника
	 */
	private PlaceMeterInstall		placeInstall;
	
	/*
	 * Можлива потужність ( 5 А - 150 А )
	 */
	private byte								mightOutturn;
	
	/*
	 * Можливі пломби
	 */
	private final List< Plumb >	plumbs;
	
	public Meter( final MeterDevice meterDevice, final String number, final byte digits, final long installDate, final short order,
			final String inspector, final PlaceMeterInstall placeInstall ) {
		this.meterDevice = meterDevice;
		this.meterDeviceId = meterDevice.getId();
		this.number = number;
		this.digits = digits;
		this.installDate = installDate;
		this.order = order;
		this.inspector = inspector;
		this.uninstallDate = MAXDATE.getTime();
		this.placeInstall = placeInstall == null ? PlaceMeterInstall.APARTMENT : placeInstall;
		this.plumbs = new ArrayList< Plumb >( 0 );
	}
	
	public Meter( final DBObject dbo ) {
		this.id = ( ( Long )dbo.get( "_id" ) ).longValue();
		try {
			this.meterDevice = MeterDevice.findById( ( ( Long )dbo.get( "meter_device_id" ) ).longValue() );
		}
		catch ( final MeterDeviceException mde ) {
			LOGGER.error( "Cannot create Meter because meter device id not found" );
		}
		this.number = ( String )dbo.get( "number" );
		this.digits = ( ( Byte )dbo.get( "digits" ) ).byteValue();
		this.installDate = ( ( Long )dbo.get( "install_date" ) ).longValue();
		this.uninstallDate = ( ( Long )dbo.get( "uninstall_date" ) ).longValue();
		this.order = ( ( Short )dbo.get( "uninstall_date" ) ).shortValue();
		this.testingDate = ( ( Long )dbo.get( "testing_date" ) ).longValue();
		this.mightOutturn = ( ( Byte )dbo.get( "might_outturn" ) ).byteValue();
		this.inspector = ( String )dbo.get( "inspector" );
		final String placeInstall = ( String )dbo.get( "place_install" );
		if ( placeInstall != null && !placeInstall.isEmpty() )
			this.placeInstall = PlaceMeterInstall.valueOf( placeInstall );
		this.plumbs = new ArrayList< Plumb >( 0 );
		final BasicDBList getList = ( BasicDBList )dbo.get( "plumbs" );
		for ( final Object o : getList )
			this.plumbs.add( ( Plumb )o );
	}
	
	public long getId() {
		return id;
	}
	
	public MeterDevice getMeterDevice() {
		return meterDevice;
	}
	
	public void setMeterDevice( final MeterDevice meterDevice ) {
		this.meterDevice = meterDevice;
		this.meterDeviceId = meterDevice.getId();
	}
	
	public String getNumber() {
		return number;
	}
	
	public void setNumber( final String number ) {
		this.number = number;
	}
	
	public byte getDigits() {
		return digits;
	}
	
	public void setDigits( final byte digits ) {
		this.digits = digits;
	}
	
	public long getInstallDate() {
		return installDate;
	}
	
	public void setInstallDate( final long installDate ) {
		this.installDate = installDate;
	}
	
	public long getUninstallDate() {
		return uninstallDate;
	}
	
	public void setUninstallDate( final long uninstallDate ) {
		this.uninstallDate = uninstallDate;
	}
	
	public short getOrder() {
		return order;
	}
	
	public void setOrder( final short order ) {
		this.order = order;
	}
	
	public long getTestingDate() {
		return testingDate;
	}
	
	public void setTestingDate( final long testingDate ) {
		this.testingDate = testingDate;
	}
	
	public String getInspector() {
		return inspector;
	}
	
	public void setInspector( final String inspector ) {
		this.inspector = inspector;
	}
	
	public PlaceMeterInstall getPlaceInstall() {
		return placeInstall;
	}
	
	public void setPlaceInstall( final PlaceMeterInstall placeInstall ) {
		this.placeInstall = placeInstall;
	}
	
	public byte getMightOutturn() {
		return mightOutturn;
	}
	
	public void setMightOutturn( final byte mightOutturn ) {
		this.mightOutturn = mightOutturn;
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
		return meter.getMeterDevice().getId() == meterDevice.getId() && meter.getNumber().compareTo( number ) == 0
				&& meter.getInstallDate() == installDate;
	}
	
	DBObject getDBObject( final short updateSet ) {
		final DBObject doc = new BasicDBObject( "meter_device_id", meterDeviceId );
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_NUMBER ) == UPDATING_READING_NUMBER )
			if ( number != null )
				doc.put( "number", number );
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_DIGITS ) == UPDATING_READING_DIGITS )
			if ( digits > 0 )
				doc.put( "digits", digits );
		doc.put( "install_date", installDate );
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_UNINSTALL_DATE ) == UPDATING_READING_UNINSTALL_DATE )
			if ( uninstallDate != MAXDATE.getTime() )
				doc.put( "uninstall_date", uninstallDate );
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_ORDER ) == UPDATING_READING_ORDER )
			doc.put( "order", order );
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_INSPECTOR ) == UPDATING_READING_INSPECTOR )
			if ( inspector != null )
				doc.put( "inspector", inspector );
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_TESTING_DATE ) == UPDATING_READING_TESTING_DATE )
			if ( testingDate > 0 )
				doc.put( "testing_date", testingDate );
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_MIGHTOUTTURN ) == UPDATING_READING_MIGHTOUTTURN )
			if ( mightOutturn > 4 )
				doc.put( "might_outturn", mightOutturn );
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_PLACE_INSTALL ) == UPDATING_READING_PLACE_INSTALL )
			doc.put( "place_install", placeInstall.name() );
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_PLUMBS ) == UPDATING_READING_PLUMBS ) {
			final BasicDBList dbPlumbs = new BasicDBList();
			for ( final Plumb p : plumbs )
				dbPlumbs.add( p.getDBObject() );
			if ( !dbPlumbs.isEmpty() )
				doc.put( "plumbs", dbPlumbs );
		}
		return doc;
	}
	
	public void save( final short updateSet ) {
		final DBObject query = new BasicDBObject( "_id", getOrCreateId() );
		final DBObject doc = getDBObject( updateSet );
		getMetersCollection().update( query, new BasicDBObject( "$set", doc ), true, false );
	}
	
	private long getOrCreateId() {
		long id = 1;
		try {
			final DBObject doc = new BasicDBObject( "_id", this.id );
			// doc.put( "name", name );
			final DBObject rec = getMetersCollection().find( doc ).one();
			if ( rec != null && !rec.toMap().isEmpty() )
				id = ( ( Long )rec.get( "_id" ) ).longValue();
			else {
				final DBCursor cursor = getMetersCollection().find().sort( new BasicDBObject( "_id", -1 ) ).limit( 1 );
				if ( cursor.hasNext() ) {
					final Object o = cursor.next().get( "_id" );
					final Long i = ( Long )o;
					id = i.longValue() + 1;
				}
			}
		}
		catch ( final MongoException me ) {
			LOGGER.debug( "Cannon find ID in MeterDevice.getOrCreateId(). {}", me );
		}
		catch ( final NullPointerException npe ) {
			LOGGER.debug( "Cannon find ID in MeterDevice.getOrCreateId(). {}", npe );
		}
		return id;
	}
	
	private static DBCollection getMetersCollection() {
		return Database.getInstance().getDatabase().getCollection( "meters" );
	}
}
