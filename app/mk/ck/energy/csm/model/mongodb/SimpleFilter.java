package mk.ck.energy.csm.model.mongodb;

import static com.mongodb.assertions.Assertions.notNull;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

public class SimpleFilter< TItem > implements Bson {
	
	private final String	fieldName;
	
	private final TItem		value;
	
	public SimpleFilter( final String fieldName, final TItem value ) {
		this.fieldName = notNull( "fieldName", fieldName );
		this.value = value;
	}
	
	@Override
	public < TDocument >BsonDocument toBsonDocument( final Class< TDocument > documentClass, final CodecRegistry codecRegistry ) {
		final BsonDocumentWriter writer = new BsonDocumentWriter( new BsonDocument() );
		writer.writeStartDocument();
		writer.writeName( fieldName );
		encodeValue( writer, value, codecRegistry );
		writer.writeEndDocument();
		return writer.getDocument();
	}
	
	@SuppressWarnings( "unchecked" )
	private static < TItem >void encodeValue( final BsonDocumentWriter writer, final TItem value, final CodecRegistry codecRegistry ) {
		if ( value == null )
			writer.writeNull();
		else
			if ( value instanceof Bson )
				( ( Encoder )codecRegistry.get( BsonDocument.class ) ).encode( writer,
						( ( Bson )value ).toBsonDocument( BsonDocument.class, codecRegistry ), EncoderContext.builder().build() );
			else
				( ( Encoder )codecRegistry.get( value.getClass() ) ).encode( writer, value, EncoderContext.builder().build() );
	}
}
