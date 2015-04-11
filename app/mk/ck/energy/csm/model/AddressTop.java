package mk.ck.energy.csm.model;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class AddressTop extends CSMAbstractDocument< AddressTop > {
	
	private static final long		serialVersionUID						= 1L;
	
	private static final String	COLLECTION_NAME_TOP_ADDRESS	= "topAddresses";
	
	private static final String	DB_FIELD_NAME								= "name";
	
	private static final String	DB_FIELD_REF_TO_TOP					= "ref_id";
	
	public AddressTop( final String name, final String refId ) {
		setName( name );
		setRefId( refId );
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
	public String getRefId() {
		return getString( DB_FIELD_REF_TO_TOP );
	}
	
	public void setRefId( final String refId ) {
		put( DB_FIELD_REF_TO_TOP, refId );
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
		final List< AddressTop > list = new LinkedList<>();
		final MongoCursor< AddressTop > cursor = getMongoCollection().find( Filters.regex( DB_FIELD_NAME, "/" + pattern + "/" ) )
				.iterator();
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
		final AddressTop rec = getMongoCollection().find( Filters.eq( DB_FIELD_REF_TO_TOP, addr.getId() ) ).first();
		final boolean b = rec != null && !rec.isEmpty();
		if ( b )
			return true;
		try {
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
			cursor = collection.find( Filters.eq( DB_FIELD_REF_TO_TOP, refId ) ).iterator();
		while ( cursor.hasNext() ) {
			final AddressTop o = cursor.next();
			final String name = o.getName();
			final String _id = o.getId();
			references.put( _id, name );
		}
		return references;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder( getName() );
		try {
			AddressTop at = AddressTop.findById( getRefId() );
			while ( at != null ) {
				sb.append( ", " );
				sb.append( at.getName() );
				at = AddressTop.findById( at.getRefId() );
			}
			return sb.toString();
		}
		catch ( final AddressNotFoundException anfe ) {
			LOGGER.warn( "AddressTop.toString() Exception: {}", anfe );
			return sb.toString();
		}
	}
	
	public static MongoCollection< AddressTop > getMongoCollection() {
		final MongoDatabase db = Database.getInstance().getDatabase();
		final MongoCollection< AddressTop > collection = db.getCollection( COLLECTION_NAME_TOP_ADDRESS, AddressTop.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< AddressTop > getCollection() {
		return getMongoCollection();
	}
}
