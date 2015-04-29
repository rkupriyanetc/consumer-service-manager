package mk.ck.energy.csm.model.codecs;

import java.util.List;

import mk.ck.energy.csm.model.AddressLocation;

import org.bson.BsonArray;
import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressLocationCodec implements CollectibleCodec< AddressLocation > {
	
	protected static final Logger		LOGGER															= LoggerFactory.getLogger( AddressLocationCodec.class );
	
	private static final String			DB_FIELD_ID													= "_id";
	
	private static final String			DB_FIELD_LOCATION										= "location";
	
	private static final String			DB_FIELD_LOCATION_TYPE							= "location_type";
	
	private static final String			DB_FIELD_ADMINISTRATIVE_CENTER_TYPE	= "administrative_type";
	
	private static final String			DB_FIELD_REFERENCE_TO_TOP_ADDRESS		= "top_address_id";
	
	private final Codec< Document >	documentCodec;
	
	public AddressLocationCodec() {
		this.documentCodec = new DocumentCodec();
	}
	
	public AddressLocationCodec( final Codec< Document > codec ) {
		this.documentCodec = codec;
	}
	
	@Override
	public void encode( final BsonWriter writer, final AddressLocation value, final EncoderContext encoderContext ) {
		final Document document = new Document( DB_FIELD_ID, value.getId() );
		document.append( DB_FIELD_LOCATION, value.getLocation() );
		document.append( DB_FIELD_LOCATION_TYPE, value.getString( DB_FIELD_LOCATION_TYPE ) );
		final String addrTop = value.getTopAddressId();
		if ( addrTop != null && !addrTop.isEmpty() )
			document.append( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, addrTop );
		final Object o = value.get( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE );
		if ( o != null )
			document.append( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE, o );
		documentCodec.encode( writer, document, encoderContext );
	}
	
	@Override
	public Class< AddressLocation > getEncoderClass() {
		return AddressLocation.class;
	}
	
	@Override
	public AddressLocation decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final AddressLocation addr = AddressLocation.create();
		addr.setId( document.getString( DB_FIELD_ID ) );
		addr.put( DB_FIELD_LOCATION, document.getString( DB_FIELD_LOCATION ) );
		addr.put( DB_FIELD_LOCATION_TYPE, document.getString( DB_FIELD_LOCATION_TYPE ) );
		addr.setTopAddressId( document.getString( DB_FIELD_REFERENCE_TO_TOP_ADDRESS ) );
		List< ? > list = null;
		final Object o = document.get( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE );
		if ( o == null )
			addr.setAdministrativeCenterType( null );
		else
			try {
				if ( List.class.isInstance( o ) ) {
					list = List.class.cast( o );
					list = addr.listStringAsBsonArray( addr.extractAsListStringValues( list ) );
				} else
					if ( BsonArray.class.isInstance( o ) )
						list = BsonArray.class.cast( o );
				addr.setAdministrativeCenterType( list );
			}
			catch ( final ClassCastException cce ) {
				LOGGER.warn( "Error casting array of AdministrativeCenterType {}", o );
			}
		return addr;
	}
	
	@Override
	public boolean documentHasId( final AddressLocation document ) {
		return document.getId() == null;
	}
	
	@Override
	public AddressLocation generateIdIfAbsentFromDocument( final AddressLocation document ) {
		if ( documentHasId( document ) ) {
			document.createId();
			return document;
		} else
			return document;
	}
	
	@Override
	public BsonValue getDocumentId( final AddressLocation document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return BsonValue.class.cast( document.getId() );
	}
}
