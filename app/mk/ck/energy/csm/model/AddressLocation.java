package mk.ck.energy.csm.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

public class AddressLocation extends CSMAbstractDocument< AddressLocation > {
	
	private static final long		serialVersionUID										= 1L;
	
	private static final String	COLLECTION_NAME_LOCATION_ADDRESS		= "locationAddresses";
	
	private static final String	DB_FIELD_LOCATION										= "location";
	
	private static final String	DB_FIELD_LOCATION_TYPE							= "location_type";
	
	private static final String	DB_FIELD_ADMINISTRATIVE_CENTER_TYPE	= "administrative_type";
	
	private static final String	DB_FIELD_REFERENCE_TO_TOP_ADDRESS		= "top_address_id";
	
	private final BsonArray			administrativeTypes;
	
	private AddressTop					topAddress;
	
	private boolean							isRegisteredAdministrativeTypes;
	
	private AddressLocation() {
		administrativeTypes = new BsonArray();
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
		if ( location != null && !location.isEmpty() )
			put( DB_FIELD_LOCATION, location );
		else
			remove( DB_FIELD_LOCATION );
	}
	
	/**
	 * Тип населенного пункту: місто, село, хутір, ...
	 */
	public LocationType getLocationType() {
		return LocationType.valueOf( getString( DB_FIELD_LOCATION_TYPE ) );
	}
	
	public void setLocationType( final LocationType locationType ) {
		if ( locationType != null )
			put( DB_FIELD_LOCATION_TYPE, locationType.name() );
		else
			remove( DB_FIELD_LOCATION_TYPE );
	}
	
	/**
	 * Типи адміністративного центру: столиця, область, район,
	 */
	public Set< AdministrativeCenterType > getAdministrativeCenterType() {
		final Set< AdministrativeCenterType > acts = new LinkedHashSet<>();
		if ( administrativeTypes != null && !administrativeTypes.isEmpty() )
			for ( final BsonValue value : administrativeTypes.getValues() )
				acts.add( AdministrativeCenterType.valueOf( value.asString().getValue() ) );
		return acts;
	}
	
	public void setAdministrativeCenterType( final Object listAdministrativeTypes ) {
		if ( listAdministrativeTypes != null ) {
			administrativeTypes.addAll( ( BsonArray )listAdministrativeTypes );
			if ( !administrativeTypes.isEmpty() ) {
				if ( !isRegisteredAdministrativeTypes ) {
					put( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE, administrativeTypes );
					isRegisteredAdministrativeTypes = true;
				}
			} else {
				remove( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE );
				isRegisteredAdministrativeTypes = false;
			}
		} else {
			remove( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE );
			isRegisteredAdministrativeTypes = false;
		}
	}
	
	public boolean addAdministrativeCenterType( final AdministrativeCenterType value ) {
		final boolean bool = administrativeTypes.add( new BsonString( value.name() ) );
		if ( !isRegisteredAdministrativeTypes ) {
			put( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE, administrativeTypes );
			isRegisteredAdministrativeTypes = true;
		}
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
	
	// c936fa76-2634-43e1-8059-5fc151706328
	public void save() throws ImpossibleCreatingException {
		AddressLocation alExists = null;
		final String capital = AdministrativeCenterType.CAPITAL.name();
		final MongoCollection< AddressLocation > collection = getCollection();
		if ( administrativeTypes.contains( new BsonString( capital ) ) )
			alExists = collection.find( Filters.in( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE, capital ) ).first();
		if ( alExists == null ) {
			final Bson value = Filters.and( Filters.eq( DB_FIELD_LOCATION, getLocation() ),
					Filters.eq( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, getTopAddressId() ),
					Filters.eq( DB_FIELD_LOCATION_TYPE, getString( DB_FIELD_LOCATION_TYPE ) ),
					Filters.eq( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE, get( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE ) ) );
			final AddressLocation addr = collection.find( value, AddressLocation.class ).first();
			if ( addr == null )
				insertIntoDB();
			else {
				final String location = this.toString();
				LOGGER.warn( "Cannot save AddressLocation. Location address already exists: {}", location );
				throw new ImpossibleCreatingException( "Location address already exists " + location );
			}
		} else {
			LOGGER.warn( "Cannot save AddressLocation bun only one CAPITAL city. Your: {}", this );
			throw new ImpossibleCreatingException( "Allowed only one CAPITAL city!" );
		}
	}
	
	public static AddressLocation create() {
		return new AddressLocation();
	}
	
	public static AddressLocation create( final AddressTop topId, final String location, final LocationType locationType,
			final Set< AdministrativeCenterType > administrativeTypes ) {
		final AddressLocation al = new AddressLocation();
		al.setLocation( location );
		al.setTopAddress( topId );
		if ( administrativeTypes != null )
			for ( final AdministrativeCenterType act : administrativeTypes )
				al.addAdministrativeCenterType( act );
		else
			al.setAdministrativeCenterType( null );
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
			final String name = addr.getLocationType().toString( Address.LOCATION_TYPE_SHORTNAME ) + " " + addr.getLocation();
			final String _id = addr.getId();
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
		sb.append( getLocationType().toString( Address.LOCATION_TYPE_SHORTNAME ) );
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
		return getTopAddress().equals( al.getTopAddress() ) && getLocation().equals( al.getLocation() )
				&& getLocationType().equals( al.getLocationType() );
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
