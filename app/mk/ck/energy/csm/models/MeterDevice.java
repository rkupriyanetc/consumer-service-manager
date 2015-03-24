package mk.ck.energy.csm.models;

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
	
	private static final Logger	LOGGER	= LoggerFactory.getLogger( MeterDevice.class );
	
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
		devices.id = ( ( Long )dbo.get( "_id" ) ).longValue();
		devices.name = ( String )dbo.get( "name" );
		String tmp = ( String )dbo.get( "inductive" );
		devices.inductiveType = InductiveType.valueOf( tmp );
		tmp = ( String )dbo.get( "method" );
		devices.methodType = MethodType.valueOf( tmp );
		tmp = ( String )dbo.get( "register" );
		devices.registerType = RegisterType.valueOf( tmp );
		Integer i = ( Integer )dbo.get( "phasing" );
		devices.phasing = i.byteValue();
		i = ( Integer )dbo.get( "interval" );
		devices.interval = i.byteValue();
		final Double d = ( Double )dbo.get( "precision" );
		devices.precision = d.doubleValue();
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
	
	public static MeterDevice findById( final long id ) throws MeterDeviceException {
		if ( id > 0 )
			try {
				final DBObject doc = getMetersDevicesCollection().findOne( QueryBuilder.start( "_id" ).is( id ).get() );
				final MeterDevice device = MeterDevice.create( doc );
				return device;
			}
			catch ( final MongoException me ) {
				throw new MeterDeviceException( "Exception in MeterDevice.findById( id ) of DBCollection", me );
			}
		else
			throw new MeterDeviceException( "ID must be greater than zero in MeterDevice.findById( id )" );
	}
	
	public static List< MeterDevice > findByName( final String name ) throws MeterDeviceException {
		if ( !( name == null ) ) {
			final Pattern pattern = Pattern.compile( name, Pattern.CASE_INSENSITIVE );
			final List< MeterDevice > devices = new ArrayList< MeterDevice >( 0 );
			try {
				final DBCursor cur = getMetersDevicesCollection().find( new BasicDBObject( "name", pattern ) );
				if ( cur == null )
					throw new MeterDeviceException( "MeterDevice by " + name + " not found" );
				while ( cur.hasNext() ) {
					final DBObject doc = cur.next();
					final MeterDevice device = MeterDevice.create( doc );
					devices.add( device );
				}
				return devices;
			}
			catch ( final MongoException me ) {
				throw new MeterDeviceException( "Exception in MeterDevice.findByName( name ) of DBCollection", me );
			}
		} else
			throw new MeterDeviceException( "Name should not be null in MeterDevice.findByName( name )" );
	}
	
	public void save() {
		final DBObject doc = getDBObject();
		doc.put( "_id", getOrCreateId() );
		getMetersDevicesCollection().save( doc );
	}
	
	DBObject getDBObject() {
		final DBObject doc = new BasicDBObject( "name", name );
		doc.put( "phasing", phasing );
		if ( methodType != null )
			doc.put( "method", methodType.name() );
		if ( inductiveType != null )
			doc.put( "inductive", inductiveType.name() );
		if ( registerType != null )
			doc.put( "register", registerType.name() );
		if ( precision > 0 )
			doc.put( "precision", precision );
		if ( interval > 0 )
			doc.put( "interval", interval );
		return doc;
	}
	
	private long getOrCreateId() {
		long id = 1;
		try {
			final DBObject doc = new BasicDBObject( "_id", this.id );
			// doc.put( "name", name );
			final DBObject rec = getMetersDevicesCollection().find( doc ).one();
			if ( rec != null && !rec.toMap().isEmpty() )
				id = ( ( Long )rec.get( "_id" ) ).longValue();
			else {
				final DBCursor cursor = getMetersDevicesCollection().find().sort( new BasicDBObject( "_id", -1 ) ).limit( 1 );
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
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer( "ID - " );
		sb.append( Long.valueOf( id ).toString() );
		sb.append( " " );
		sb.append( this.name );
		sb.append( " F - " );
		sb.append( Byte.valueOf( phasing ).toString() );
		return sb.toString();
	}
	
	private static DBCollection getMetersDevicesCollection() {
		return Database.getInstance().getDatabase().getCollection( "metersDevices" );
	}
}
