package mk.ck.energy.csm.model.codecs;

import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

public class GradesCodec implements CollectibleCodec< Grades > {
	
	private final Codec< Document >	documentCodec;
	
	public GradesCodec() {
		this.documentCodec = new DocumentCodec();
	}
	
	public GradesCodec( final Codec< Document > codec ) {
		this.documentCodec = codec;
	}
	
	@Override
	public void encode( final BsonWriter writer, final Grades value, final EncoderContext encoderContext ) {
		final Document document = new Document();
		final ObjectId id = value.getId();
		final Double score = value.getScore();
		final Integer studentId = value.getStudentId();
		final String type = value.getType();
		if ( null != id )
			document.put( "_id", id );
		if ( null != score )
			document.put( "score", score );
		if ( null != studentId )
			document.put( "student_id", studentId );
		if ( null != type )
			document.put( "type", type );
		documentCodec.encode( writer, document, encoderContext );
	}
	
	@Override
	public Class< Grades > getEncoderClass() {
		return Grades.class;
	}
	
	@Override
	public Grades decode( final BsonReader reader, final DecoderContext decoderContext ) {
		final Document document = documentCodec.decode( reader, decoderContext );
		System.out.println( "document " + document );
		final Grades grade = new Grades();
		grade.setId( document.getObjectId( "_id" ) );
		grade.setStudentId( document.getInteger( "student_id" ) );
		grade.setType( document.getString( "type" ) );
		grade.setScore( document.getDouble( "score" ) );
		return grade;
	}
	
	@Override
	public boolean documentHasId( final Grades document ) {
		return document.getId() == null;
	}
	
	@Override
	public Grades generateIdIfAbsentFromDocument( final Grades document ) {
		return documentHasId( document ) ? document.withNewObjectId() : document;
	}
	
	@Override
	public BsonValue getDocumentId( final Grades document ) {
		if ( !documentHasId( document ) )
			throw new IllegalStateException( "The document does not contain an _id" );
		return new BsonString( document.getId().toHexString() );
	}
	
	@Override
	public String toString() {
		return "CanonicalName is " + getClass().getCanonicalName();
	}
}
