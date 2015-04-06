package mk.ck.energy.csm.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mk.ck.energy.csm.model.db.AbstractMongoCollection;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCursor;

public class AddressTop extends AbstractMongoCollection {
	
	private static final Logger	LOGGER							= LoggerFactory.getLogger( AddressTop.class );
	
	static final String					DB_FIELD_NAME				= "name";
	
	static final String					DB_FIELD_REF_TO_TOP	= "ref_id";
	
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
	private String							refId;
	
	private AddressTop() {}
	
	public static AddressTop create( final String name, final String refId ) {
		final AddressTop addr = new AddressTop();
		addr.name = name;
		addr.refId = refId;
		addr.save( COLLECTION_NAME_TOP_ADDRESS );
		return addr;
	}
	
	public static AddressTop create( final Document dbo ) {
		final AddressTop addr = new AddressTop();
		addr.setName( ( String )dbo.get( DB_FIELD_NAME ) );
		addr.setRefId( ( String )dbo.get( DB_FIELD_REF_TO_TOP ) );
		addr.setId( ( String )dbo.get( DB_FIELD_ID ) );
		return addr;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName( final String name ) {
		this.name = name;
	}
	
	public String getRefId() {
		return refId;
	}
	
	public void setRefId( final String refId ) {
		this.refId = refId;
	}
	
	public static AddressTop findById( final String id ) throws AddressNotFoundException {
		if ( id != null && !id.isEmpty() ) {
			final Document doc = getCollection( COLLECTION_NAME_TOP_ADDRESS ).find( new Document( DB_FIELD_ID, id ) ).first();
			if ( doc == null )
				throw new AddressNotFoundException( "Address not found in AddressTop.findById( " + id + " )" );
			final AddressTop addr = AddressTop.create( doc );
			return addr;
		} else
			throw new AddressNotFoundException( "ID must be greater than zero in AddressTop.findById( id )" );
	}
	
	public static AddressTop findByName( final String name ) throws AddressNotFoundException {
		if ( name == null || name.isEmpty() )
			throw new AddressNotFoundException( "The parameter cannot be empty" );
		final Document doc = getCollection( COLLECTION_NAME_TOP_ADDRESS ).find( new Document( DB_FIELD_NAME, name ) ).first();
		if ( doc == null )
			throw new AddressNotFoundException( "Address " + name + " not found" );
		final AddressTop addr = AddressTop.create( doc );
		return addr;
	}
	
	public static void remove( final AddressTop addr ) throws AddressNotFoundException, ForeignKeyException {
		if ( hasChildren( addr ) )
			throw new ForeignKeyException( "This record has dependencies" );
		else {
			final Document doc = getCollection( COLLECTION_NAME_TOP_ADDRESS ).findOneAndDelete(
					new Document( DB_FIELD_ID, addr.getId() ) );
			LOGGER.debug( "AddressTop object removed {}", doc );
		}
	}
	
	private static boolean hasChildren( final AddressTop addr ) {
		final Document doc = new Document( DB_FIELD_REF_TO_TOP, addr.getId() );
		final Document rec = getCollection( COLLECTION_NAME_TOP_ADDRESS ).find( doc ).first();
		final boolean b = rec != null && !rec.isEmpty();
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
	
	/**
	 * @param refId
	 *          If equals zero then select all
	 * @return
	 */
	public static Map< String, String > getMap( final String refId ) {
		final Map< String, String > references = new LinkedHashMap< String, String >( 0 );
		MongoCursor< Document > cursor;
		if ( refId == null || refId.isEmpty() || refId.equals( "0" ) )
			cursor = getCollection( COLLECTION_NAME_TOP_ADDRESS ).find().iterator();
		else
			cursor = getCollection( COLLECTION_NAME_TOP_ADDRESS ).find( new Document( DB_FIELD_REF_TO_TOP, refId ) ).iterator();
		while ( cursor.hasNext() ) {
			final Document o = cursor.next();
			final String name = ( String )o.get( DB_FIELD_NAME );
			final String _id = ( String )o.get( DB_FIELD_ID );
			references.put( _id, name );
		}
		return references;
	}
	
	public static List< AddressTop > asClassType( final List< Document > all ) {
		final List< AddressTop > result = new ArrayList< AddressTop >();
		for ( final Document dbo : all )
			result.add( AddressTop.create( dbo ) );
		return result;
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
	
	@Override
	public Document getDocument() {
		final Document doc = new Document();
		if ( name != null && !name.isEmpty() )
			doc.put( DB_FIELD_NAME, name );
		doc.put( DB_FIELD_REF_TO_TOP, refId );
		return doc;
	}
}
