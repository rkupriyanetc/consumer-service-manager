package mk.ck.energy.csm.model;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

public class MeterDevice extends CSMAbstractDocument< MeterDevice > {
	
	private static final long		serialVersionUID								= 1L;
	
	private static final String	COLLECTION_NAME_METERS_DEVICES	= "metersDevices";
	
	private static final String	DB_FIELD_NAME										= "name";
	
	private static final String	DB_FIELD_PHASING								= "phasing";
	
	private static final String	DB_FIELD_METHOD_TYPE						= "method";
	
	private static final String	DB_FIELD_INDUCTIVE_TYPE					= "inductive";
	
	private static final String	DB_FIELD_REGISTER_TYPE					= "register";
	
	private static final String	DB_FIELD_PRECISION							= "precision";
	
	private static final String	DB_FIELD_INTERVAL								= "interval";
	
	public enum MethodType {
		INDUCTION, ELECTRONIC, ;
		
		public boolean equals( final MethodType o ) {
			if ( o == null )
				return false;
			return name().equals( o.name() );
		}
	}
	
	public enum InductiveType {
		ACTIVE, REACTIVE, ACTIVE_REACTIVE, ;
		
		public boolean equals( final InductiveType o ) {
			if ( o == null )
				return false;
			return name().equals( o.name() );
		}
	}
	
	public enum RegisterType {
		STATE, SELF_REGIONAL, DISTRICT, ;
		
		public boolean equals( final RegisterType o ) {
			if ( o == null )
				return false;
			return name().equals( o.name() );
		}
	}
	
	private MeterDevice() {}
	
	/*
	 * Тип, назва лічильника
	 */
	public String getName() {
		return getString( DB_FIELD_NAME );
	}
	
	public void setName( final String name ) {
		put( DB_FIELD_NAME, name );
	}
	
	/*
	 * Одно- чи Три- фазний
	 */
	public byte getPhasing() {
		final Object phasing = get( DB_FIELD_PHASING );
		if ( phasing != null )
			return ( byte )phasing;
		else
			return 1;
	}
	
	public void setPhasing( final byte phasing ) {
		put( DB_FIELD_PHASING, phasing );
	}
	
	public MethodType getMethodType() {
		return MethodType.valueOf( getString( DB_FIELD_METHOD_TYPE ) );
	}
	
	public void setMethodType( final MethodType methodType ) {
		put( DB_FIELD_METHOD_TYPE, methodType.name() );
	}
	
	public InductiveType getInductiveType() {
		return InductiveType.valueOf( getString( DB_FIELD_INDUCTIVE_TYPE ) );
	}
	
	public void setInductiveType( final InductiveType inductiveType ) {
		put( DB_FIELD_INDUCTIVE_TYPE, inductiveType.name() );
	}
	
	public RegisterType getRegisterType() {
		return RegisterType.valueOf( getString( DB_FIELD_REGISTER_TYPE ) );
	}
	
	public void setRegisterType( final RegisterType registerType ) {
		put( DB_FIELD_REGISTER_TYPE, registerType.name() );
	}
	
	public double getPrecision() {
		return getDouble( DB_FIELD_PRECISION );
	}
	
	public void setPrecision( final double precision ) {
		put( DB_FIELD_PRECISION, precision );
	}
	
	public byte getInterval() {
		final Object interval = get( DB_FIELD_INTERVAL );
		if ( interval != null )
			return ( byte )interval;
		else
			return 0;
	}
	
	public void setInterval( final byte interval ) {
		put( DB_FIELD_INTERVAL, interval );
	}
	
	public static MeterDevice create() {
		return new MeterDevice();
	}
	
	public static MeterDevice create( final String name, final byte phasing, final MethodType methodType,
			final InductiveType inductiveType, final RegisterType registerType, final double precision, final byte interval ) {
		final MeterDevice meter = new MeterDevice();
		meter.setName( name );
		meter.setPhasing( phasing == 1 || phasing == 3 ? phasing : 1 );
		meter.setPrecision( precision );
		meter.setInterval( interval );
		meter.setMethodType( methodType == null ? MethodType.ELECTRONIC : methodType );
		meter.setInductiveType( inductiveType == null ? InductiveType.ACTIVE : inductiveType );
		meter.setRegisterType( registerType == null ? RegisterType.STATE : registerType );
		return meter;
	}
	
	public static MeterDevice findById( final String id ) throws MeterDeviceNotFoundException {
		if ( id == null || id.isEmpty() )
			throw new IllegalArgumentException( "The parameter should not be empty" );
		final MeterDevice device = getMongoCollection().find( Filters.eq( DB_FIELD_ID, id ), MeterDevice.class ).first();
		if ( device != null )
			return device;
		else
			throw new MeterDeviceNotFoundException( "MeterDevice by id: " + id + " not found" );
	}
	
	public static List< MeterDevice > findLikeName( final String pattern ) throws MeterDeviceNotFoundException {
		if ( pattern == null || pattern.isEmpty() )
			throw new IllegalArgumentException( "The parameter should not be empty" );
		final List< MeterDevice > devices = new LinkedList<>();
		final MongoCursor< MeterDevice > cursor = getMongoCollection().find(
				Filters.regex( DB_FIELD_NAME, Pattern.compile( pattern, Pattern.CASE_INSENSITIVE ) ), MeterDevice.class ).iterator();
		if ( cursor == null )
			throw new MeterDeviceNotFoundException( "MeterDevice by " + pattern + " not found" );
		while ( cursor.hasNext() ) {
			final MeterDevice o = cursor.next();
			devices.add( o );
		}
		return devices;
	}
	
	public static MeterDevice findByName( final String meterName ) throws MeterDeviceNotFoundException {
		if ( meterName == null || meterName.isEmpty() )
			throw new IllegalArgumentException( "The parameter should not be empty" );
		final MeterDevice meter = getMongoCollection().find( Filters.eq( DB_FIELD_NAME, meterName ), MeterDevice.class ).first();
		if ( meter == null )
			throw new MeterDeviceNotFoundException( "MeterDevice by " + meterName + " not found" );
		return meter;
	}
	
	public static Bson makeFilterToId( final String value ) {
		return Filters.eq( DB_FIELD_ID, value );
	}
	
	public static Bson makeFilterToName( final String value ) {
		return Filters.eq( DB_FIELD_NAME, value );
	}
	
	public static Bson makeFilterToPhasing( final byte value ) {
		return Filters.eq( DB_FIELD_PHASING, value );
	}
	
	public static Bson makeFilterToMethodType( final MethodType value ) {
		return Filters.eq( DB_FIELD_METHOD_TYPE, value.name() );
	}
	
	public static Bson makeFilterToInductiveType( final InductiveType value ) {
		return Filters.eq( DB_FIELD_INDUCTIVE_TYPE, value.name() );
	}
	
	public static Bson makeFilterToRegisterType( final RegisterType value ) {
		return Filters.eq( DB_FIELD_REGISTER_TYPE, value.name() );
	}
	
	public static Bson makeFilterToPrecision( final double value ) {
		return Filters.eq( DB_FIELD_PRECISION, value );
	}
	
	public static Bson makeFilterToInterval( final byte value ) {
		return Filters.eq( DB_FIELD_INTERVAL, value );
	}
	
	public void save() throws ImpossibleCreatingException {
		final Bson value = Filters.and( Filters.eq( DB_FIELD_NAME, getName() ), Filters.eq( DB_FIELD_PHASING, getPhasing() ) );
		final MeterDevice meter = getCollection().find( value, MeterDevice.class ).first();
		if ( meter == null )
			insertIntoDB();
		else {
			final String meterName = this.toString();
			LOGGER.warn( "Cannot save MeterDevice. MeterDevice already exists: {}", meterName );
			throw new ImpossibleCreatingException( "MeterDevice already exists " + meterName );
		}
	}
	
	@Override
	public boolean equals( final Object o ) {
		if ( o == null || !( o instanceof MeterDevice ) )
			return false;
		final MeterDevice meterDevice = ( MeterDevice )o;
		return meterDevice.getId().equals( getId() );
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer( "Марка - " );
		sb.append( getName() );
		sb.append( " F - " );
		sb.append( getPhasing() );
		return sb.toString();
	}
	
	@Override
	public < TDocument >BsonDocument toBsonDocument( final Class< TDocument > documentClass, final CodecRegistry codecRegistry ) {
		return new BsonDocumentWrapper< MeterDevice >( this, codecRegistry.get( MeterDevice.class ) );
	}
	
	public static MongoCollection< MeterDevice > getMongoCollection() {
		final MongoCollection< MeterDevice > collection = getDatabase().getCollection( COLLECTION_NAME_METERS_DEVICES,
				MeterDevice.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< MeterDevice > getCollection() {
		return getMongoCollection();
	}
}
