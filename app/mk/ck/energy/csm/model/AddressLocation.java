package mk.ck.energy.csm.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

public class AddressLocation extends CSMAbstractDocument< AddressLocation > {
	
	private static final long			serialVersionUID										= 1L;
	
	private static final String		COLLECTION_NAME_LOCATION_ADDRESS		= "locationAddresses";
	
	private static final String		DB_FIELD_LOCATION										= "location";
	
	private static final String		DB_FIELD_LOCATION_TYPE							= "location_type";
	
	private static final String		DB_FIELD_ADMINISTRATIVE_CENTER_TYPE	= "administrative_type";
	
	private static final String		DB_FIELD_REFERENCE_TO_TOP_ADDRESS		= "top_address_id";
	
	private final List< String >	administrativeTypes;
	
	private AddressTop						topAddress;
	
	private AddressLocation() {
		administrativeTypes = new LinkedList<>();
	}
	
	public String getTopAddressId() {
		return getString( DB_FIELD_REFERENCE_TO_TOP_ADDRESS );
	}
	
	public void setTopAddressId( final String addressTopId ) {
		if ( addressTopId != null && !addressTopId.isEmpty() ) {
			final String topId = getTopAddressId();
			if ( !addressTopId.equals( topId ) )
				try {
					topAddress = AddressTop.findById( addressTopId );
					put( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, addressTopId );
				}
				catch ( final AddressNotFoundException anfe ) {
					LOGGER.warn( "AddressLocation has now reference to top id NULL pointer" );
					remove( DB_FIELD_REFERENCE_TO_TOP_ADDRESS );
				}
		} else
			remove( DB_FIELD_REFERENCE_TO_TOP_ADDRESS );
	}
	
	/**
	 * Населенний пункт
	 */
	public String getLocation() {
		return getString( DB_FIELD_LOCATION );
	}
	
	public void setLocation( final String location ) {
		put( DB_FIELD_LOCATION, location );
	}
	
	/**
	 * Тип населенного пункту: місто, село, хутір, ...
	 */
	public LocationType getLocationType() {
		return LocationType.valueOf( getString( DB_FIELD_LOCATION_TYPE ) );
	}
	
	public void setLocationType( final LocationType locationType ) {
		put( DB_FIELD_LOCATION_TYPE, locationType.name() );
	}
	
	/**
	 * Типи адміністративного центру: столиця, область, район,
	 */
	/**
	 * public Set< AdministrativeCenterType > getAdministrativeCenterType() {
	 * final BsonArray list = ( BsonArray )get(
	 * DB_FIELD_ADMINISTRATIVE_CENTER_TYPE );
	 * final Set< AdministrativeCenterType > adcts = new LinkedHashSet<>();
	 * for ( final BsonValue key : list.getValues() ) {
	 * final AdministrativeCenterType at = AdministrativeCenterType.valueOf( ( (
	 * BsonString )key ).getValue() );
	 * adcts.add( at );
	 * }
	 * return adcts;
	 * }
	 */
	public Set< AdministrativeCenterType > getAdministrativeCenterType() {
		final Set< AdministrativeCenterType > acts = new LinkedHashSet<>();
		if ( administrativeTypes != null || !administrativeTypes.isEmpty() )
			for ( final String value : administrativeTypes )
				acts.add( AdministrativeCenterType.valueOf( value ) );
		return acts;
	}
	
	public void setAdministrativeCenterType( final Object listAdministrativeTypes ) {
		if ( administrativeTypes != null ) {
			administrativeTypes.addAll( extractListStringValues( listAdministrativeTypes ) );
			put( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE, administrativeTypes );
		}
	}
	
	public boolean addAdministrativeCenterType( final AdministrativeCenterType value ) {
		final boolean bool = administrativeTypes.add( value.name() );
		put( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE, administrativeTypes );
		return bool;
	}
	
	/**
	 * public void setAdministrativeCenterType( final Set<
	 * AdministrativeCenterType > administrativeTypes ) {
	 * final BsonArray dbList = new BsonArray();
	 * for ( final AdministrativeCenterType at : administrativeTypes )
	 * dbList.add( new BsonString( at.name() ) );
	 * put( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE, dbList );
	 * }
	 */
	public AddressTop getTopAddress() {
		return topAddress;
	}
	
	public void setTopAddress( final AddressTop topAddress ) {
		if ( topAddress != null ) {
			if ( !topAddress.equals( this.topAddress ) ) {
				this.topAddress = topAddress;
				put( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, topAddress.getId() );
			}
		} else
			remove( DB_FIELD_REFERENCE_TO_TOP_ADDRESS );
	}
	
	public void save() {
		final Bson value = Filters.and( Filters.eq( DB_FIELD_LOCATION, getLocation() ),
				Filters.eq( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, getTopAddressId() ),
				Filters.eq( DB_FIELD_LOCATION_TYPE, getString( DB_FIELD_LOCATION_TYPE ) ),
				Filters.eq( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE, get( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE ) ) );
		final AddressLocation addr = getCollection().find( value, AddressLocation.class ).first();
		if ( addr == null )
			insertIntoDB();
		else
			update( value );
	}
	
	public static AddressLocation create() {
		return new AddressLocation();
	}
	
	public static AddressLocation create( final AddressTop topId, final String location, final LocationType locationType,
			final Set< AdministrativeCenterType > administrativeTypes ) {
		final AddressLocation al = new AddressLocation();
		al.setLocation( location );
		al.setTopAddress( topId );
		for ( final AdministrativeCenterType act : administrativeTypes )
			al.addAdministrativeCenterType( act );
		al.setLocationType( locationType );
		return al;
	}
	
	public static AddressLocation findById( final String id ) throws AddressNotFoundException {
		if ( id != null && !id.isEmpty() ) {
			final AddressLocation doc = getMongoCollection().find( Filters.eq( DB_FIELD_ID, id ) ).first();
			if ( doc == null )
				throw new AddressNotFoundException( "Cannot find address-location by " + id );
			return doc;
		} else
			throw new IllegalArgumentException( "ID must be greater than zero in AddressLocation.findById( id )" );
	}
	
	public static List< AddressLocation > findByAddressTop( final String topAddrId ) throws AddressNotFoundException {
		final List< AddressLocation > locations = new LinkedList<>();
		final MongoCursor< AddressLocation > cursor = getMongoCollection().find(
				Filters.eq( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, topAddrId ) ).iterator();
		if ( cursor == null )
			throw new AddressNotFoundException( "Cannot find address-location by " + topAddrId );
		while ( cursor.hasNext() ) {
			final AddressLocation o = cursor.next();
			locations.add( o );
		}
		return locations;
	}
	
	/**
	 * @param pattern
	 *          db.collection.find({name: /pattern/}) //like '%a%'
	 * @return
	 * @throws AddressNotFoundException
	 */
	public static List< AddressLocation > findLikeLocationName( final String pattern ) throws AddressNotFoundException {
		final List< AddressLocation > locations = new LinkedList<>();
		final MongoCursor< AddressLocation > cursor = getMongoCollection().find( Filters.regex( DB_FIELD_LOCATION, pattern ) )
				.iterator();
		if ( cursor == null )
			throw new AddressNotFoundException( "Cannot find address-location by " + pattern );
		while ( cursor.hasNext() ) {
			final AddressLocation o = cursor.next();
			locations.add( o );
		}
		return locations;
	}
	
	public static void remove( final AddressLocation addr ) throws ForeignKeyException {
		if ( hasChildren( addr ) )
			throw new ForeignKeyException( "This record has dependencies" );
		else {
			final AddressLocation doc = getMongoCollection().findOneAndDelete( Filters.eq( DB_FIELD_ID, addr.getId() ) );
			LOGGER.debug( "AddressLocation was removed {}", doc );
		}
	}
	
	private static boolean hasChildren( final AddressLocation addr ) {
		return false;
	}
	
	/**
	 * @param refId
	 *          Indicates reference id, who have to get the select to map
	 * @param isAddrTop
	 *          If equals zero then does not participate
	 * @return
	 */
	public static Map< String, String > getMap( final String refId, final int isAddrTop ) {
		final Map< String, String > references = new LinkedHashMap<>();
		final MongoCursor< AddressLocation > o = getMongoCollection().find( Filters.eq( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, refId ) )
				.iterator();
		while ( o.hasNext() ) {
			final AddressLocation addr = o.next();
			final String name = addr.getString( DB_FIELD_LOCATION_TYPE ) + " " + addr.getString( DB_FIELD_LOCATION );
			final String _id = addr.getString( DB_FIELD_ID );
			references.put( _id, name );
		}
		if ( isAddrTop != 0 ) {
			int p = -1;
			for ( final String key : AddressTop.getMap( refId ).keySet() ) {
				references.put( new Integer( p-- ).toString(), "0" );
				references.putAll( getMap( key, 0 ) );
			}
		}
		return references;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( getString( DB_FIELD_LOCATION_TYPE ) );
		if ( sb.length() > 0 )
			sb.append( " " );
		sb.append( getString( DB_FIELD_LOCATION ) );
		return sb.toString();
	}
	
	@Override
	public boolean equals( final Object o ) {
		if ( o == null )
			return false;
		final AddressLocation al = ( AddressLocation )o;
		return getLocation().equals( al.getLocation() ) && getLocationType().equals( al.getLocationType() );
	}
	
	@Override
	public < TDocument >BsonDocument toBsonDocument( final Class< TDocument > documentClass, final CodecRegistry codecRegistry ) {
		return new BsonDocumentWrapper< AddressLocation >( this, codecRegistry.get( AddressLocation.class ) );
	}
	
	public static MongoCollection< AddressLocation > getMongoCollection() {
		final MongoCollection< AddressLocation > collection = getDatabase().getCollection( COLLECTION_NAME_LOCATION_ADDRESS,
				AddressLocation.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< AddressLocation > getCollection() {
		return getMongoCollection();
	}
}
