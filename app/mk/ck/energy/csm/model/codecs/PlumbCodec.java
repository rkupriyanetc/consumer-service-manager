package mk.ck.energy.csm.model.codecs;

import mk.ck.energy.csm.model.Plumb;

import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

public class PlumbCodec implements CollectibleCodec< Plumb > {
	
	private static final String			DB_FIELD_ID							= "_id";
	
	private static final String			DB_FIELD_NUMBER					= "number";
	
	private static final String			DB_FIELD_DATE_INSTALL		= "date_install";
	
	private static final String			DB_FIELD_DATE_UNINSTALL	= "date_uninstall";
	
	private static final String			DB_FIELD_MASTER_NAME		= "master_name";
	
	private static final String			DB_FIELD_PLUMB_TYPE			= "plumb_type";
	
	private final Codec< Document >	documentCodec;
	
	public PlumbCodec() {
		this.documentCodec = new DocumentCodec();
	}
	
	public PlumbCodec( final Codec< Document > codec ) {
		this.documentCodec = codec;
	}
	
	@Override
	public void encode( final BsonWriter writer, final Plumb value, final EncoderContext encoderContext ) {
		final Document document = new Document( DB_FIELD_ID, value.getId() );
		Object o = value.get( DB_FIELD_NUMBER );
		if ( o != null )
			document.append( DB_FIELD_NUMBER, o );
		o = value.get( DB_FIELD_DATE_INSTALL );
		long lon;
		if ( o != null ) {
			lon = ( ( Long )o ).longValue();
			document.append( DB_FIELD_DATE_INSTALL, lon );
		}
		o = value.get( DB_FIELD_DATE_UNINSTALL );
		if ( o != null ) {
			lon = ( ( Long )o ).longValue();
			document.append( DB_FIELD_DATE_UNINSTALL, lon );
		}
		o = value.get( DB_FIELD_MASTER_NAME );
		if ( o != null )
			document.append( DB_FIELD_MASTER_NAME, o );
		o = value.get( DB_FIELD_PLUMB_TYPE );
		if ( o != null )
			document.append( DB_FIELD_PLUMB_TYPE, o );
		documentCodec.encode( writer, document, encoderContext );
	}
	
	@Override
	public Class< Plumb > getEncoderClass() {
		return Plumb.class;
	}
	
	@Override
	public Plumb decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		final Plumb plumb = Plumb.create();
		plumb.setId( document.getString( DB_FIELD_ID ) );
		Object o = document.get( DB_FIELD_NUMBER );
		if ( o != null )
			plumb.put( DB_FIELD_NUMBER, o );
		o = document.get( DB_FIELD_DATE_INSTALL );
		long un;
		if ( o != null ) {
			un = ( ( Long )o ).longValue();
			plumb.put( DB_FIELD_DATE_INSTALL, un );
		}
		o = document.get( DB_FIELD_DATE_UNINSTALL );
		if ( o != null ) {
			un = ( ( Long )o ).longValue();
			plumb.put( DB_FIELD_DATE_UNINSTALL, un );
		}
		o = document.get( DB_FIELD_MASTER_NAME );
		if ( o != null )
			plumb.put( DB_FIELD_MASTER_NAME, o );
		o = document.get( DB_FIELD_PLUMB_TYPE );
		if ( o != null )
			plumb.put( DB_FIELD_PLUMB_TYPE, o );
		return plumb;
	}
	
	@Override
	public boolean documentHasId( final Plumb document ) {
		return document.getId() == null;
	}
	
	@Override
	public Plumb generateIdIfAbsentFromDocument( final Plumb document ) {
		if ( documentHasId( document ) ) {
			document.createId();
			return document;
		} else
			return document;
	}
	
	@Override
	public BsonValue getDocumentId( final Plumb document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return BsonValue.class.cast( document.getId() );
	}
}
