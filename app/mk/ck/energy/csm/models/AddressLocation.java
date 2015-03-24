package mk.ck.energy.csm.models;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.i18n.Messages;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

public class AddressLocation {
	
	private static final Logger		LOGGER	= LoggerFactory.getLogger( AddressLocation.class );
	
	private long									id;
	
	/**
	 * Типи населенних пунктів: столиця, обласний центр, районний, місто, село,
	 * хутір
	 */
	private List< LocationType >	locationsTypes;
	
	/**
	 * Населенний пункт
	 */
	private String								location;
	
	private AddressTop						topAddress;
	
	private AddressLocation() {}
	
	public static AddressLocation create( final AddressTop topId, final String location, final List< LocationType > locationsTypes ) {
		final AddressLocation addr = new AddressLocation();
		addr.setLocation( location );
		addr.setTopAddress( topId );
		addr.setLocationsTypes( locationsTypes );
		try {
			addr.save();
		}
		catch ( final ImpossibleCreatingException ice ) {
			LOGGER.debug( "Create error in AddressLocation.create( addr, location, type ). {}", ice );
		}
		return addr;
	}
	
	public static AddressLocation create( final DBObject dbo ) throws ImpossibleCreatingException {
		if ( dbo == null )
			return null;
		final AddressLocation addr = new AddressLocation();
		addr.locationsTypes = new ArrayList< LocationType >( 0 );
		try {
			addr.topAddress = AddressTop.findById( ( ( Long )dbo.get( "refId" ) ).longValue() );
		}
		catch ( final AddressNotFoundException anfe ) {
			throw new ImpossibleCreatingException( "Cannot AddressLocation.create( DBObject dbo ) because could not find AddressTop" );
		}
		addr.location = ( String )dbo.get( "location" );
		final BasicDBList getList = ( BasicDBList )dbo.get( "locationsTypes" );
		for ( final Object o : getList )
			addr.locationsTypes.add( LocationType.valueOf( ( String )o ) );
		addr.id = ( ( Long )dbo.get( "_id" ) ).longValue();
		return addr;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId( final long id ) {
		this.id = id;
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation( final String location ) {
		this.location = location;
	}
	
	public List< LocationType > getLocationsTypes() {
		return locationsTypes;
	}
	
	public void setLocationsTypes( final List< LocationType > locationsTypes ) {
		this.locationsTypes = locationsTypes;
	}
	
	public AddressTop getTopAddress() {
		return topAddress;
	}
	
	public void setTopAddress( final AddressTop topAddress ) {
		this.topAddress = topAddress;
	}
	
	private long getOrCreateId() {
		long id = 1;
		try {
			final DBObject doc = new BasicDBObject( "_id", id );
			final DBObject rec = getAddressCollection().find( doc ).one();
			if ( rec != null && !rec.toMap().isEmpty() )
				id = ( ( Long )rec.get( "_id" ) ).longValue();
			else {
				final DBCursor cursor = getAddressCollection().find().sort( new BasicDBObject( "_id", -1 ) ).limit( 1 );
				if ( cursor.hasNext() ) {
					final Object o = cursor.next().get( "_id" );
					final Long i = ( Long )o;
					id = i.longValue() + 1;
				}
			}
		}
		catch ( final MongoException me ) {
			LOGGER.debug( "Cannon find ID in AddressLocation.getOrCreateId(). {}", me );
		}
		catch ( final NullPointerException npe ) {
			LOGGER.debug( "Cannon find ID in AddressLocation.getOrCreateId(). {}", npe );
		}
		return id;
	}
	
	DBObject getDBObject() {
		final DBObject doc = new BasicDBObject();
		if ( location != null && !location.isEmpty() )
			doc.put( "location", location );
		final BasicDBList dbTypes = new BasicDBList();
		for ( final LocationType type : locationsTypes )
			dbTypes.add( type.name() );
		if ( !dbTypes.isEmpty() )
			doc.put( "locationsTypes", dbTypes );
		if ( topAddress != null )
			doc.put( "refId", topAddress.getId() );
		else
			doc.put( "refId", 0L );
		return doc;
	}
	
	public void save() throws ImpossibleCreatingException {
		if ( locationsTypes.contains( LocationType.CAPITAL ) ) {
			final BasicDBList dbo = new BasicDBList();
			dbo.add( LocationType.CAPITAL.name() );
			final DBObject qu = new BasicDBObject( "locationsTypes", new BasicDBObject( "$in", dbo ) );
			if ( getAddressCollection().find( qu ).hasNext() )
				throw new ImpossibleCreatingException();
		}
		final DBObject o = getDBObject();
		o.put( "_id", getOrCreateId() );
		getAddressCollection().save( o );
	}
	
	public static AddressLocation findById( final long id ) throws AddressNotFoundException {
		if ( id > 0 )
			try {
				final DBObject doc = getAddressCollection().findOne( QueryBuilder.start( "_id" ).is( id ).get() );
				try {
					final AddressLocation addr = AddressLocation.create( doc );
					return addr;
				}
				catch ( final ImpossibleCreatingException ice ) {
					return null;
				}
			}
			catch ( final MongoException me ) {
				throw new AddressNotFoundException( "Exception in AddressLocation.findById( id ) of DBCollection", me );
			}
		else
			throw new AddressNotFoundException( "ID must be greater than zero in AddressLocation.findById( id )" );
	}
	
	public static AddressLocation findByAddressTop( final AddressTop topAddr ) throws AddressNotFoundException {
		try {
			final DBObject doc = getAddressCollection().findOne( QueryBuilder.start( "refId" ).is( topAddr.getId() ).get() );
			if ( doc == null )
				throw new AddressNotFoundException( "Address " + topAddr.getName() + " not found" );
			final AddressLocation addr = AddressLocation.create( doc );
			return addr;
		}
		catch ( final MongoException me ) {
			return null;
		}
		catch ( final ImpossibleCreatingException ice ) {
			throw new AddressNotFoundException(
					"ImpossibleCreatingException in AddressLocation.findByAddressTop( addressTop ) in AddressLocation.create( doc )", ice );
		}
	}
	
	public static List< AddressLocation > findByAddress( final DBObject address ) throws AddressNotFoundException {
		final List< AddressLocation > locations = new ArrayList< AddressLocation >( 0 );
		final BasicDBList lts = ( BasicDBList )address.get( "locationsTypes" );
		final DBObject qu = new BasicDBObject();
		if ( lts != null )
			qu.put( "locationsTypes", new BasicDBObject( "$in", lts ) );
		final DBObject name = ( DBObject )address.get( "location" );
		if ( name != null )
			qu.put( "location", name.get( "location" ) );
		final DBObject ref = ( DBObject )address.get( "refId" );
		if ( ref != null )
			qu.put( "refId", ref.get( "refId" ) );
		try {
			final DBCursor cursor = getAddressCollection().find( qu );
			if ( cursor == null )
				throw new AddressNotFoundException( "AddressLocation by " + address + " not found" );
			while ( cursor.hasNext() ) {
				final DBObject o = cursor.next();
				final AddressLocation addr = AddressLocation.create( o );
				locations.add( addr );
			}
			return locations;
		}
		catch ( final MongoException me ) {
			return null;
		}
		catch ( final ImpossibleCreatingException ice ) {
			throw new AddressNotFoundException(
					"ImpossibleCreatingException in AddressLocation.findByAddress( address ) in AddressLocation.create( doc )", ice );
		}
	}
	
	public static List< AddressLocation > findByLocationName( final String locationName ) throws AddressNotFoundException {
		final List< AddressLocation > locations = new ArrayList< AddressLocation >( 0 );
		try {
			final DBCursor cursor = getAddressCollection().find( QueryBuilder.start( "location" ).is( locationName ).get() );
			if ( cursor == null )
				throw new AddressNotFoundException( "Location by " + locationName + " not found" );
			while ( cursor.hasNext() ) {
				final DBObject o = cursor.next();
				final AddressLocation addr = AddressLocation.create( o );
				locations.add( addr );
			}
			return locations;
		}
		catch ( final MongoException me ) {
			return null;
		}
		catch ( final ImpossibleCreatingException ice ) {
			throw new AddressNotFoundException(
					"ImpossibleCreatingException in AddressLocation.findByLocationName( locationName ) in AddressLocation.create( doc )",
					ice );
		}
	}
	
	public static void remove( final AddressLocation addr ) throws AddressNotFoundException, ForeignKeyException {
		if ( hasChildren( addr ) )
			throw new ForeignKeyException( "This record has dependencies" );
		else
			try {
				final DBObject doc = getAddressCollection().findOne( QueryBuilder.start( "_id" ).is( addr.id ).get() );
				final WriteResult wr = getAddressCollection().remove( doc );
				LOGGER.debug( "AddressLocation object removed {}", wr );
			}
			catch ( final MongoException me ) {
				throw new AddressNotFoundException( "AddressTop not found" );
			}
	}
	
	private static boolean hasChildren( final AddressLocation addr ) {
		return false;
	}
	
	public static List< AddressLocation > asClassType( final List< DBObject > all ) {
		final List< AddressLocation > result = new ArrayList< AddressLocation >();
		for ( final DBObject dbo : all )
			try {
				result.add( AddressLocation.create( dbo ) );
			}
			catch ( final ImpossibleCreatingException ice ) {
				LOGGER.debug( "Error in converter AddressLocation {}", ice );
			}
		return result;
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		if ( locationsTypes.contains( LocationType.CITY ) )
			sb.append( Messages.get( Address.LOCATION_TYPE_SHORTNAME + "." + LocationType.CITY.name().toLowerCase() ) );
		else
			if ( locationsTypes.contains( LocationType.HAMLET ) )
				sb.append( Messages.get( Address.LOCATION_TYPE_SHORTNAME + "." + LocationType.HAMLET.name().toLowerCase() ) );
			else
				if ( locationsTypes.contains( LocationType.TOWNSHIP ) )
					sb.append( Messages.get( Address.LOCATION_TYPE_SHORTNAME + "." + LocationType.TOWNSHIP.name().toLowerCase() ) );
				else
					if ( locationsTypes.contains( LocationType.VILLAGE ) )
						sb.append( Messages.get( Address.LOCATION_TYPE_SHORTNAME + "." + LocationType.VILLAGE.name().toLowerCase() ) );
		if ( sb.length() > 0 )
			sb.append( " " );
		sb.append( location );
		return sb.toString();
	}
	
	public static DBCollection getAddressCollection() {
		return Database.getInstance().getDatabase().getCollection( "locationAddresses" );
	}
}
