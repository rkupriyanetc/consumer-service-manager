package mk.ck.energy.csm.model.codecs;

import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;

public class GradeInserter {
	
	private static MongoClient	mc;
	
	public static void main( final String[] args ) {
		final Codec< Document > defaultDocumentCodec = MongoClient.getDefaultCodecRegistry().get( Document.class );
		System.out.print( "DocumentCodec default is " + defaultDocumentCodec );
		final GradesCodec gradeCodec = new GradesCodec( defaultDocumentCodec );
		final CodecRegistry codecRegistry = CodecRegistries.fromRegistries( MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromCodecs( gradeCodec ) );
		final MongoClientOptions options = MongoClientOptions.builder().codecRegistry( codecRegistry ).build();
		mc = new MongoClient( "localhost:27017", options );
		final MongoCollection< Grades > collection = mc.getDatabase( "test" ).getCollection( "test", Grades.class );
		collection.insertOne( new Grades( 3, "Work", 130.0 ) );
		final MongoCursor< Grades > cur = collection.find().iterator();
		while ( cur.hasNext() )
			System.out.println( cur.next() );
		mc.close();
	}
}
