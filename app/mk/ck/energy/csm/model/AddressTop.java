package mk.ck.energy.csm.model;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

public class AddressTop extends CSMAbstractDocument< AddressTop > {
	
	private static final long		serialVersionUID									= 1L;
	
	private static final String	COLLECTION_NAME_TOP_ADDRESS				= "topAddresses";
	
	private static final String	DB_FIELD_NAME											= "name";
	
	private static final String	DB_FIELD_REFERENCE_TO_TOP_ADDRESS	= "top_id";
	
	private AddressTop					addressTop;
	
	private AddressTop() {}
	
	public static AddressTop create() {
		return new AddressTop();
	}
	
	public static AddressTop create( final String name, final String refId ) {
		final AddressTop addr = new AddressTop();
		addr.setName( name );
		addr.setTopAddressId( refId );
		return addr;
	}
	
	/**
	 * Region or District name.
	 * Names can be like Маньківський р-н, Черкаська обл., Вінницька обл.,
	 * Уманський р-н. і т.д. But only this value.
	 */
	public String getName() {
		return getString( DB_FIELD_NAME );
	}
	
	public void setName( final String name ) {
		put( DB_FIELD_NAME, name );
	}
	
	/**
	 * Reference to Id of Region or District name.
	 * If <code>refId</code> = 0 then name is Region center.
	 * For example : id = 3, name = Черкаська обл., refId = 0
	 * But when the name is the District, then name = Маньківський р-н, refId = 3
	 */
	public String getTopAddressId() {
		return getString( DB_FIELD_REFERENCE_TO_TOP_ADDRESS );
	}
	
	public void setTopAddressId( final String addressTopId ) {
		if ( addressTopId != null && !addressTopId.isEmpty() ) {
			final String topId = getTopAddressId();
			if ( !addressTopId.equals( topId ) )
				try {
					this.addressTop = AddressTop.findById( addressTopId );
					put( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, addressTopId );
				}
				catch ( final AddressNotFoundException anfe ) {
					LOGGER.warn( "Sorry. Cannot find AddressTop by {}", addressTopId );
					remove( DB_FIELD_REFERENCE_TO_TOP_ADDRESS );
				}
		} else
			remove( DB_FIELD_REFERENCE_TO_TOP_ADDRESS );
	}
	
	public AddressTop getTopAddress() {
		return addressTop;
	}
	
	public void setTopAddress( final AddressTop addressTop ) {
		if ( addressTop != null ) {
			if ( !addressTop.equals( this.addressTop ) ) {
				this.addressTop = addressTop;
				put( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, addressTop.getTopAddressId() );
			}
		} else
			remove( DB_FIELD_REFERENCE_TO_TOP_ADDRESS );
	}
	
	public void save() throws ImpossibleCreatingException {
		final Bson value = Filters.and( Filters.eq( DB_FIELD_NAME, getName() ),
				Filters.eq( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, getTopAddressId() ) );
		final AddressTop addr = getCollection().find( value, AddressTop.class ).first();
		if ( addr == null )
			insertIntoDB();
		else {
			final String top = this.toString();
			LOGGER.warn( "Cannot save AddressTop. Top address already exists: {}", top );
			throw new ImpossibleCreatingException( "Top address already exists " + top );
		}
	}
	
	public static AddressTop findById( final String id ) throws AddressNotFoundException {
		if ( id != null && !id.isEmpty() ) {
			final AddressTop doc = getMongoCollection().find( Filters.eq( DB_FIELD_ID, id ) ).first();
			if ( doc == null )
				throw new AddressNotFoundException( "Cannot find address-top by " + id );
			return doc;
		} else
			throw new IllegalArgumentException( "ID should not be empty in AddressTop.findById( id )" );
	}
	
	/**
	 * @param pattern
	 *          db.collection.find({name: /pattern/}) //like '%a%'
	 * @return
	 * @throws AddressNotFoundException
	 */
	public static List< AddressTop > findLikeName( final String pattern ) throws AddressNotFoundException {
		if ( pattern == null || pattern.isEmpty() )
			throw new IllegalArgumentException( "The parameter should not be empty" );
		// не працює тут
		final List< AddressTop > list = new LinkedList<>();
		final MongoCursor< AddressTop > cursor = getMongoCollection().find( Filters.regex( DB_FIELD_NAME, pattern ) ).iterator();
		if ( cursor == null )
			throw new AddressNotFoundException( "Address " + pattern + " not found" );
		while ( cursor.hasNext() ) {
			final AddressTop o = cursor.next();
			list.add( o );
		}
		return list;
	}
	
	public static void remove( final AddressTop addr ) throws ForeignKeyException {
		if ( hasChildren( addr ) )
			throw new ForeignKeyException( "This record has dependencies" );
		else {
			final AddressTop doc = getMongoCollection().findOneAndDelete( Filters.eq( DB_FIELD_ID, addr.getId() ) );
			LOGGER.debug( "AddressTop object removed {}", doc );
		}
	}
	
	private static boolean hasChildren( final AddressTop addr ) {
		final AddressTop rec = getMongoCollection().find( Filters.eq( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, addr.getId() ) ).first();
		final boolean b = rec != null && !rec.isEmpty();
		if ( b )
			return true;
		try {
			// Тут тра переробити
			final List< AddressLocation > al = AddressLocation.findByAddressTop( addr.getId() );
			return !al.isEmpty();
		}
		catch ( final AddressNotFoundException anfe ) {
			return false;
		}
	}
	
	/**
	 * @param refId
	 *          If equals zero then select all
	 * @return
	 */
	public static Map< String, String > getMap( final String refId ) {
		final Map< String, String > references = new LinkedHashMap< String, String >( 0 );
		MongoCursor< AddressTop > cursor;
		final MongoCollection< AddressTop > collection = getMongoCollection();
		if ( refId == null || refId.isEmpty() || refId.equals( "0" ) )
			cursor = collection.find().iterator();
		else
			cursor = collection.find( Filters.eq( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, refId ) ).iterator();
		while ( cursor.hasNext() ) {
			final AddressTop o = cursor.next();
			final String name = o.getName();
			// Тут тра переробити
			final String _id = o.getId();
			references.put( _id, name );
		}
		return references;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder( getName() );
		final String topAddresId = getTopAddressId();
		if ( topAddresId != null && !topAddresId.isEmpty() )
			try {
				AddressTop at = AddressTop.findById( topAddresId );
				while ( at != null ) {
					sb.append( ", " );
					sb.append( at.getName() );
					final String atTopAddresId = at.getTopAddressId();
					if ( atTopAddresId != null && !atTopAddresId.isEmpty() )
						at = AddressTop.findById( atTopAddresId );
					else
						at = null;
				}
			}
			catch ( final AddressNotFoundException anfe ) {
				LOGGER.warn( "AddressTop.toString() Exception: {}", anfe );
				return sb.toString();
			}
		return sb.toString();
	}
	
	@Override
	public boolean equals( final Object o ) {
		if ( o == null )
			return false;
		final AddressTop at = ( AddressTop )o;
		return getName().equals( at.getName() )
				&& ( getTopAddressId() != null && getTopAddressId().equals( at.getTopAddressId() ) || getTopAddressId() == null
						&& at.getTopAddressId() == null );
	}
	
	@Override
	public < TDocument >BsonDocument toBsonDocument( final Class< TDocument > documentClass, final CodecRegistry codecRegistry ) {
		return new BsonDocumentWrapper< AddressTop >( this, codecRegistry.get( AddressTop.class ) );
	}
	
	public static MongoCollection< AddressTop > getMongoCollection() {
		final MongoCollection< AddressTop > collection = getDatabase().getCollection( COLLECTION_NAME_TOP_ADDRESS, AddressTop.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< AddressTop > getCollection() {
		return getMongoCollection();
	}
}
