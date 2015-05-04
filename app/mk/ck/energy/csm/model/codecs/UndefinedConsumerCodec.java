package mk.ck.energy.csm.model.codecs;

import mk.ck.energy.csm.model.UndefinedConsumer;

import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

public class UndefinedConsumerCodec implements CollectibleCodec< UndefinedConsumer > {
	
	private static final String			DB_FIELD_ID												= "_id";
	
	private static final String			DB_FIELD_UNDEFINED_CONSUMER_TYPE	= "type";
	
	private final Codec< Document >	documentCodec;
	
	public UndefinedConsumerCodec() {
		this.documentCodec = new DocumentCodec();
	}
	
	public UndefinedConsumerCodec( final Codec< Document > codec ) {
		this.documentCodec = codec;
	}
	
	@Override
	public void encode( final BsonWriter writer, final UndefinedConsumer value, final EncoderContext encoderContext ) {
		final Document document = new Document( DB_FIELD_ID, value.getId() );
		document.append( DB_FIELD_UNDEFINED_CONSUMER_TYPE, value.getString( DB_FIELD_UNDEFINED_CONSUMER_TYPE ) );
		documentCodec.encode( writer, document, encoderContext );
	}
	
	@Override
	public Class< UndefinedConsumer > getEncoderClass() {
		return UndefinedConsumer.class;
	}
	
	@Override
	public UndefinedConsumer decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final UndefinedConsumer consumer = UndefinedConsumer.create();
		consumer.put( DB_FIELD_ID, document.getString( DB_FIELD_ID ) );
		consumer.put( DB_FIELD_UNDEFINED_CONSUMER_TYPE, document.getString( DB_FIELD_UNDEFINED_CONSUMER_TYPE ) );
		return consumer;
	}
	
	@Override
	public boolean documentHasId( final UndefinedConsumer document ) {
		return document.getId() == null;
	}
	
	@Override
	public UndefinedConsumer generateIdIfAbsentFromDocument( final UndefinedConsumer document ) {
		if ( documentHasId( document ) ) {
			document.createId();
			return document;
		} else
			return document;
	}
	
	@Override
	public BsonValue getDocumentId( final UndefinedConsumer document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return BsonValue.class.cast( document.getId() );
	}
}