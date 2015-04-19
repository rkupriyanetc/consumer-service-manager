package mk.ck.energy.csm.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

public class MeterDevice {
	
	private static final Logger	LOGGER									= LoggerFactory.getLogger( MeterDevice.class );
	
	static final String					DB_FIELD_ID							= "_id";
	
	static final String					DB_FIELD_NAME						= "name";
	
	static final String					DB_FIELD_PHASING				= "phasing";
	
	static final String					DB_FIELD_METHOD_TYPE		= "method";
	
	static final String					DB_FIELD_INDUCTIVE_TYPE	= "inductive";
	
	static final String					DB_FIELD_REGISTER_TYPE	= "register";
	
	static final String					DB_FIELD_PRECISION			= "precision";
	
	static final String					DB_FIELD_INTERVAL				= "interval";
	
	public enum MethodType {
		INDUCTION, ELECTRONIC,
	}
	
	public enum InductiveType {
		ACTIVE, REACTIVE, ACTIVE_REACTIVE,
	}
	
	public enum RegisterType {
		STATE, SELF_REGIONAL, DISTRICT,
	}
	
	private long					id;
	
	/*
	 * Тип, назва лічильника
	 */
	private String				name;
	
	/*
	 * Одно- чи Три- фазний
	 */
	private byte					phasing;
	
	private MethodType		methodType;
	
	private InductiveType	inductiveType;
	
	private RegisterType	registerType;
	
	private double				precision;
	
	private byte					interval;
	
	private MeterDevice() {}
	
	public static MeterDevice create( final String name, final byte phasing, final MethodType methodType,
			final InductiveType inductiveType, final RegisterType registerType, final double precision, final byte interval ) {
		final MeterDevice meter = new MeterDevice();
		meter.setName( name );
		meter.setPhasing( phasing == 1 || phasing == 3 ? phasing : 1 );
		meter.setMethodType( methodType == null ? MethodType.INDUCTION : methodType );
		meter.setInductiveType( inductiveType == null ? InductiveType.ACTIVE : inductiveType );
		meter.setRegisterType( registerType == null ? RegisterType.STATE : registerType );
		meter.setPrecision( precision );
		meter.setInterval( interval );
		return meter;
	}
	
	public static MeterDevice create( final DBObject dbo ) {
		if ( dbo == null )
			return null;
		final MeterDevice devices = new MeterDevice();
		devices.id = ( Long )dbo.get( DB_FIELD_ID );
		devices.name = ( String )dbo.get( DB_FIELD_NAME );
		devices.inductiveType = InductiveType.valueOf( ( String )dbo.get( DB_FIELD_INDUCTIVE_TYPE ) );
		devices.methodType = MethodType.valueOf( ( String )dbo.get( DB_FIELD_METHOD_TYPE ) );
		devices.registerType = RegisterType.valueOf( ( String )dbo.get( DB_FIELD_REGISTER_TYPE ) );
		devices.phasing = ( Byte )dbo.get( DB_FIELD_PHASING );
		devices.interval = ( Byte )dbo.get( DB_FIELD_INTERVAL );
		devices.precision = ( Double )dbo.get( DB_FIELD_PRECISION );
		return devices;
	}
	
	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName( final String name ) {
		this.name = name;
	}
	
	public byte getPhasing() {
		return phasing;
	}
	
	public void setPhasing( final byte phasing ) {
		this.phasing = phasing;
	}
	
	public MethodType getMethodType() {
		return methodType;
	}
	
	public void setMethodType( final MethodType methodType ) {
		this.methodType = methodType;
	}
	
	public InductiveType getInductiveType() {
		return inductiveType;
	}
	
	public void setInductiveType( final InductiveType inductiveType ) {
		this.inductiveType = inductiveType;
	}
	
	public RegisterType getRegisterType() {
		return registerType;
	}
	
	public void setRegisterType( final RegisterType registerType ) {
		this.registerType = registerType;
	}
	
	public double getPrecision() {
		return precision;
	}
	
	public void setPrecision( final double precision ) {
		this.precision = precision;
	}
	
	public byte getInterval() {
		return interval;
	}
	
	public void setInterval( final byte interval ) {
		this.interval = interval;
	}
	
	public static MeterDevice findById( final long id ) throws MeterDeviceNotFoundException {
		if ( id > 0 )
			try {
				final DBObject doc = getMetersDevicesCollection().findOne( QueryBuilder.start( DB_FIELD_ID ).is( id ).get() );
				final MeterDevice device = MeterDevice.create( doc );
				return device;
			}
			catch ( final MongoException me ) {
				throw new MeterDeviceNotFoundException( "Exception in MeterDevice.findById( id ) of DBCollection", me );
			}
		else
			throw new MeterDeviceNotFoundException( "ID must be greater than zero in MeterDevice.findById( id )" );
	}
	
	public static List< MeterDevice > findLikeName( final String name ) throws MeterDeviceNotFoundException {
		if ( !( name == null ) ) {
			final Pattern pattern = Pattern.compile( name, Pattern.CASE_INSENSITIVE );
			final List< MeterDevice > devices = new ArrayList< MeterDevice >( 0 );
			try {
				final DBCursor cur = getMetersDevicesCollection().find( new BasicDBObject( DB_FIELD_NAME, pattern ) );
				if ( cur == null )
					throw new MeterDeviceNotFoundException( "MeterDevice by " + name + " not found" );
				while ( cur.hasNext() ) {
					final DBObject doc = cur.next();
					final MeterDevice device = MeterDevice.create( doc );
					devices.add( device );
				}
				return devices;
			}
			catch ( final MongoException me ) {
				throw new MeterDeviceNotFoundException( "Exception in MeterDevice.findByName( name ) of DBCollection", me );
			}
		} else
			throw new MeterDeviceNotFoundException( "Name should not be null in MeterDevice.findByName( name )" );
	}
	
	private long getOrCreateId() {
		long id = 1;
		try {
			final DBObject doc = new BasicDBObject( DB_FIELD_NAME, this.name );
			final DBObject rec = getMetersDevicesCollection().find( doc ).one();
			if ( rec != null && !rec.toMap().isEmpty() )
				id = ( Long )rec.get( DB_FIELD_ID );
			else {
				final DBCursor cursor = getMetersDevicesCollection().find().sort( new BasicDBObject( DB_FIELD_ID, -1 ) ).limit( 1 );
				if ( cursor.hasNext() ) {
					final Object o = cursor.next().get( DB_FIELD_ID );
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
	
	DBObject getDBObject() {
		final DBObject doc = new BasicDBObject( DB_FIELD_NAME, name );
		doc.put( DB_FIELD_PHASING, phasing );
		if ( methodType != null )
			doc.put( DB_FIELD_METHOD_TYPE, methodType.name() );
		if ( inductiveType != null )
			doc.put( DB_FIELD_INDUCTIVE_TYPE, inductiveType.name() );
		if ( registerType != null )
			doc.put( DB_FIELD_REGISTER_TYPE, registerType.name() );
		if ( precision > 0 )
			doc.put( DB_FIELD_PRECISION, precision );
		if ( interval > 0 )
			doc.put( DB_FIELD_INTERVAL, interval );
		return doc;
	}
	
	public void save() {
		final DBObject doc = getDBObject();
		doc.put( DB_FIELD_ID, getOrCreateId() );
		getMetersDevicesCollection().save( doc );
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer( "ID - " );
		sb.append( this.id );
		sb.append( " " );
		sb.append( this.name );
		sb.append( " F - " );
		sb.append( this.phasing );
		return sb.toString();
	}
	
	private static DBCollection getMetersDevicesCollection() {
		return null;// Database.getInstance().getDatabase().getCollection(
								// "metersDevices" );
	}
}
