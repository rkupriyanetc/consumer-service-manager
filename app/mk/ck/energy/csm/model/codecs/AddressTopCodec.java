package mk.ck.energy.csm.model.codecs;

import mk.ck.energy.csm.model.AddressTop;

import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

public class AddressTopCodec implements CollectibleCodec< AddressTop > {
	
	private static final String			DB_FIELD_ID												= "_id";
	
	private static final String			DB_FIELD_NAME											= "name";
	
	private static final String			DB_FIELD_REFERENCE_TO_TOP_ADDRESS	= "top_id";
	
	private final Codec< Document >	documentCodec;
	
	public AddressTopCodec() {
		this.documentCodec = new DocumentCodec();
	}
	
	public AddressTopCodec( final Codec< Document > codec ) {
		this.documentCodec = codec;
	}
	
	@Override
	public void encode( final BsonWriter writer, final AddressTop value, final EncoderContext encoderContext ) {
		final Document document = new Document( DB_FIELD_ID, value.getId() );
		document.append( DB_FIELD_NAME, value.getName() );
		final String addrTop = value.getTopAddressId();
		if ( addrTop != null && !addrTop.isEmpty() )
			document.append( DB_FIELD_REFERENCE_TO_TOP_ADDRESS, addrTop );
		documentCodec.encode( writer, document, encoderContext );
	}
	
	@Override
	public Class< AddressTop > getEncoderClass() {
		return AddressTop.class;
	}
	
	@Override
	public AddressTop decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final AddressTop addr = AddressTop.create();
		addr.setId( document.getString( DB_FIELD_ID ) );
		addr.put( DB_FIELD_NAME, document.getString( DB_FIELD_NAME ) );
		addr.setTopAddressId( document.getString( DB_FIELD_REFERENCE_TO_TOP_ADDRESS ) );
		return addr;
	}
	
	@Override
	public boolean documentHasId( final AddressTop document ) {
		return document.getId() == null;
	}
	
	@Override
	public AddressTop generateIdIfAbsentFromDocument( final AddressTop document ) {
		if ( documentHasId( document ) ) {
			document.createId();
			return document;
		} else
			return document;
	}
	
	@Override
	public BsonValue getDocumentId( final AddressTop document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return BsonValue.class.cast( document.getId() );
	}
}