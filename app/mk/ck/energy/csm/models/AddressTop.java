package mk.ck.energy.csm.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

public class AddressTop {
	
	private static final Logger	LOGGER							= LoggerFactory.getLogger( AddressTop.class );
	
	static final String					DB_FIELD_ID					= "_id";
	
	static final String					DB_FIELD_NAME				= "name";
	
	static final String					DB_FIELD_REF_TO_TOP	= "ref_id";
	
	/**
	 * Id of Region or District name
	 */
	private long								id;
	
	/**
	 * Region or District name.
	 * Names can be like Маньківський р-н, Черкаська обл., Вінницька обл.,
	 * Уманський р-н. і т.д. But only this value.
	 */
	private String							name;
	
	/**
	 * Reference to Id of Region or District name.
	 * If <code>refId</code> = 0 then name is Region center.
	 * For example : id = 3, name = Черкаська обл., refId = 0
	 * But when the name is the District, then name = Маньківський р-н, refId = 3
	 */
	private long								refId;
	
	private AddressTop() {}
	
	public static AddressTop create( final String name, final long refId ) {
		final AddressTop addr = new AddressTop();
		addr.name = name;
		addr.refId = refId;
		addr.save();
		return addr;
	}
	
	public static AddressTop create( final DBObject dbo ) {
		final AddressTop addr = new AddressTop();
		addr.name = ( String )dbo.get( DB_FIELD_NAME );
		addr.refId = ( ( Long )dbo.get( DB_FIELD_REF_TO_TOP ) ).longValue();
		addr.id = ( ( Long )dbo.get( DB_FIELD_ID ) ).longValue();
		return addr;
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
	
	public long getRefId() {
		return refId;
	}
	
	public void setRefId( final long refId ) {
		this.refId = refId;
	}
	
	private long getOrCreateId() {
		long id = 1;
		try {
			final DBObject doc = new BasicDBObject( DB_FIELD_REF_TO_TOP, refId );
			doc.put( DB_FIELD_NAME, name );
			final DBObject rec = getAddressCollection().find( doc ).one();
			if ( rec != null && !rec.toMap().isEmpty() )
				id = ( ( Long )rec.get( DB_FIELD_ID ) ).longValue();
			else {
				final DBCursor cursor = getAddressCollection().find().sort( new BasicDBObject( DB_FIELD_ID, -1 ) ).limit( 1 );
				if ( cursor.hasNext() ) {
					final Object o = cursor.next().get( DB_FIELD_ID );
					final Long i = ( Long )o;
					id = i.longValue() + 1;
				}
			}
		}
		catch ( final MongoException me ) {
			LOGGER.debug( "Cannon find ID in AddressTop.getOrCreateId(). {}", me );
		}
		catch ( final NullPointerException npe ) {
			LOGGER.debug( "Cannon find ID in AddressTop.getOrCreateId(). {}", npe );
		}
		return id;
	}
	
	DBObject getDBObject() {
		id = getOrCreateId();
		final DBObject doc = new BasicDBObject( DB_FIELD_ID, id );
		if ( name != null && !name.isEmpty() )
			doc.put( DB_FIELD_NAME, name );
		doc.put( DB_FIELD_REF_TO_TOP, refId );
		return doc;
	}
	
	public void save() {
		final DBObject o = new BasicDBObject();
		if ( name != null && !name.isEmpty() )
			o.put( DB_FIELD_NAME, name );
		o.put( DB_FIELD_REF_TO_TOP, refId );
		if ( getAddressCollection().find( o ).count() < 1 )
			getAddressCollection().save( getDBObject() );
	}
	
	public static AddressTop findById( final long id ) throws AddressNotFoundException {
		if ( id > 0 )
			try {
				final DBObject doc = getAddressCollection().findOne( QueryBuilder.start( DB_FIELD_ID ).is( id ).get() );
				final AddressTop addr = AddressTop.create( doc );
				return addr;
			}
			catch ( final MongoException me ) {
				throw new AddressNotFoundException( "Exception in AddressTop.findById( id ) of DBCollection", me );
			}
		else
			if ( id == 0 )
				return null;
			else
				throw new AddressNotFoundException( "ID must be greater than zero in AddressTop.findById( id )" );
	}
	
	public static AddressTop findByName( final String name ) throws AddressNotFoundException {
		if ( name == null || name.isEmpty() )
			throw new AddressNotFoundException( "The parameter cannot be empty" );
		try {
			final DBObject doc = getAddressCollection().findOne( QueryBuilder.start( DB_FIELD_NAME ).is( name ).get() );
			if ( doc == null )
				throw new AddressNotFoundException( "Address " + name + " not found" );
			final AddressTop addr = AddressTop.create( doc );
			return addr;
		}
		catch ( final MongoException me ) {
			throw new AddressNotFoundException( "Exception in AddressTop.findByName( name ) of DBCollection", me );
		}
	}
	
	public static void remove( final AddressTop addr ) throws AddressNotFoundException, ForeignKeyException {
		if ( hasChildren( addr ) )
			throw new ForeignKeyException( "This record has dependencies" );
		else
			try {
				final DBObject doc = getAddressCollection().findOne( QueryBuilder.start( DB_FIELD_ID ).is( addr.id ).get() );
				final WriteResult wr = getAddressCollection().remove( doc );
				LOGGER.debug( "AddressTop object removed {}", wr );
			}
			catch ( final MongoException me ) {
				throw new AddressNotFoundException( "AddressTop not found" );
			}
	}
	
	private static boolean hasChildren( final AddressTop addr ) {
		final DBObject doc = new BasicDBObject( DB_FIELD_REF_TO_TOP, addr.id );
		final DBObject rec = getAddressCollection().find( doc ).one();
		final boolean b = rec != null && !rec.toMap().isEmpty();
		if ( b )
			return true;
		try {
			final List< AddressLocation > al = AddressLocation.findByAddressTop( addr );
			return !al.isEmpty();
		}
		catch ( final AddressNotFoundException anfe ) {
			return false;
		}
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer( name );
		try {
			AddressTop at = AddressTop.findById( refId );
			while ( at != null ) {
				sb.append( ", " );
				sb.append( at.getName() );
				at = AddressTop.findById( at.getRefId() );
			}
			return sb.toString();
		}
		catch ( final AddressNotFoundException anfe ) {
			LOGGER.error( "AddressTop.toString() Exception: {}", anfe );
			return null;
		}
	}
	
	public static Map< String, String > getMap() {
		final Map< String, String > references = new LinkedHashMap< String, String >( 0 );
		for ( final DBObject o : getAddressCollection().find() ) {
			final String name = ( String )o.get( DB_FIELD_NAME );
			final String _id = ( ( Long )o.get( DB_FIELD_ID ) ).toString();
			references.put( _id, name );
		}
		return references;
	}
	
	public static List< AddressTop > asClassType( final List< DBObject > all ) {
		final List< AddressTop > result = new ArrayList< AddressTop >();
		for ( final DBObject dbo : all )
			result.add( AddressTop.create( dbo ) );
		return result;
	}
	
	public static DBCollection getAddressCollection() {
		return Database.getInstance().getDatabase().getCollection( "topAddresses" );
	}
}
