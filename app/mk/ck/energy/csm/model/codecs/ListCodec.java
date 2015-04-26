package mk.ck.energy.csm.model.codecs;

import java.util.LinkedList;
import java.util.List;

import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

import com.mongodb.util.JSON;

public class ListCodec implements CollectibleCodec< ListCodec > {
	
	private final Codec< Document >	documentCodec;
	
	public ListCodec() {
		this.documentCodec = new DocumentCodec();
	}
	
	public ListCodec( final Codec< Document > codec ) {
		this.documentCodec = codec;
	}
	
	@Override
	public void encode( final BsonWriter writer, final List< Object > value, final EncoderContext encoderContext ) {
		final Document document = new Document( "json_string", JSON.serialize( value ) );
		documentCodec.encode( writer, document, encoderContext );
	}
	
	@Override
	public Class< List< Object > > getEncoderClass() {
		return List.class;
	}
	
	@Override
	public List< Object > decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final List< Object > obj = ( List< Object > )JSON.parse( document.getString( "json_string" ) );
		return obj;
	}
	
	@Override
	public boolean documentHasId( final List< Object > document ) {
		return document == null;
	}
	
	@Override
	public List< Object > generateIdIfAbsentFromDocument( List< Object > document ) {
		if ( documentHasId( document ) ) {
			document = new LinkedList< Object >();
			return document;
		} else
			return document;
	}
	
	@Override
	public BsonValue getDocumentId( final List< Object > document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return BsonValue.class.cast( document.toArray() );
	}
}