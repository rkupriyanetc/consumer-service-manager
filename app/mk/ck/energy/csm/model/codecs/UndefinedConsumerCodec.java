package mk.ck.energy.csm.model.codecs;

import java.util.List;

import mk.ck.energy.csm.model.UndefinedConsumer;

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

public class UndefinedConsumerCodec implements CollectibleCodec< UndefinedConsumer > {
	
	protected static final Logger		LOGGER														= LoggerFactory.getLogger( UndefinedConsumerCodec.class );
	
	private static final String			DB_FIELD_ID												= "_id";
	
	private static final String			DB_FIELD_UNDEFINED_CONSUMER_TYPES	= "types";
	
	private static final String			DB_FIELD_UNDEFINED_STRING					= "error";
	
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
		Object o = value.get( DB_FIELD_UNDEFINED_CONSUMER_TYPES );
		if ( o != null )
			document.append( DB_FIELD_UNDEFINED_CONSUMER_TYPES, o );
		o = value.get( DB_FIELD_UNDEFINED_STRING );
		if ( o != null )
			document.append( DB_FIELD_UNDEFINED_STRING, o );
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
		consumer.setId( document.getString( DB_FIELD_ID ) );
		List< ? > list = null;
		Object o = document.get( DB_FIELD_UNDEFINED_CONSUMER_TYPES );
		if ( o == null )
			consumer.setUndefinedConsumerTypes( null );
		else
			try {
				if ( List.class.isInstance( o ) ) {
					list = List.class.cast( o );
					list = consumer.listStringAsBsonArray( consumer.extractAsListStringValues( list ) );
				} else
					if ( BsonArray.class.isInstance( o ) )
						list = BsonArray.class.cast( o );
				consumer.setUndefinedConsumerTypes( list );
			}
			catch ( final ClassCastException cce ) {
				LOGGER.warn( "Error casting array of UndefinedConsumerType {}", o );
			}
		o = document.get( DB_FIELD_UNDEFINED_STRING );
		if ( o != null )
			consumer.put( DB_FIELD_UNDEFINED_STRING, o );
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