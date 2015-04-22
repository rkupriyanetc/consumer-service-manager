package mk.ck.energy.csm.model.codecs;

import mk.ck.energy.csm.model.AddressLocation;

import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

public class AddressLocationCodec implements CollectibleCodec< AddressLocation > {
	
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
		document.append( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE, value.get( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE ) );
		final String addrTop = value.getTopAddressId();
		if ( addrTop != null && !addrTop.isEmpty() )
			document.append( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, addrTop );
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
		addr.setAdministrativeCenterType( document.get( DB_FIELD_ADMINISTRATIVE_CENTER_TYPE ) );
		addr.setTopAddressId( document.getString( DB_FIELD_REFERENCE_TO_TOP_ADDRESS ) );
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