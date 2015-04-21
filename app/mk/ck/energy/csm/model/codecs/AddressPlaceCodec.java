package mk.ck.energy.csm.model.codecs;

import mk.ck.energy.csm.model.AddressPlace;

import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

public class AddressPlaceCodec implements CollectibleCodec< AddressPlace > {
	
	private static final String			DB_FIELD_ID						= "_id";
	
	private static final String			DB_FIELD_STREET_NAME	= "street";
	
	private static final String			DB_FIELD_STREET_TYPE	= "street_type";
	
	private final Codec< Document >	documentCodec;
	
	public AddressPlaceCodec() {
		this.documentCodec = new DocumentCodec();
	}
	
	public AddressPlaceCodec( final Codec< Document > codec ) {
		this.documentCodec = codec;
	}
	
	@Override
	public void encode( final BsonWriter writer, final AddressPlace value, final EncoderContext encoderContext ) {
		final Document document = new Document( DB_FIELD_ID, value.getId() );
		document.append( DB_FIELD_STREET_NAME, value.getStreet() );
		document.append( DB_FIELD_STREET_TYPE, value.getString( DB_FIELD_STREET_TYPE ) );
		documentCodec.encode( writer, document, encoderContext );
	}
	
	@Override
	public Class< AddressPlace > getEncoderClass() {
		return AddressPlace.class;
	}
	
	@Override
	public AddressPlace decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final AddressPlace addr = AddressPlace.create();
		addr.setId( document.getString( DB_FIELD_ID ) );
		addr.put( DB_FIELD_STREET_NAME, document.getString( DB_FIELD_STREET_NAME ) );
		addr.put( DB_FIELD_STREET_TYPE, document.getString( DB_FIELD_STREET_TYPE ) );
		return addr;
	}
	
	@Override
	public boolean documentHasId( final AddressPlace document ) {
		return document.getId() == null;
	}
	
	@Override
	public AddressPlace generateIdIfAbsentFromDocument( final AddressPlace document ) {
		if ( documentHasId( document ) ) {
			document.createId();
			return document;
		} else
			return document;
	}
	
	@Override
	public BsonValue getDocumentId( final AddressPlace document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return BsonValue.class.cast( document.getId() );
	}
}