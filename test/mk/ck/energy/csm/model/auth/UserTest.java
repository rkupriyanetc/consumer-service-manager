package mk.ck.energy.csm.model.auth;

import static org.fest.assertions.Assertions.assertThat;
import mk.ck.energy.csm.model.Configuration;
import mk.ck.energy.csm.model.Database;

import org.bson.Document;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;

public class UserTest {
	
	private static final Logger		LOGGER	= LoggerFactory.getLogger( UserTest.class );
	
	private static Configuration	config;
	
	private static Database				data;
	
	@BeforeClass
	public static void beforeBegin() {
		config = Configuration.getInstance();
		data = Database.getInstance();
	}
	
	@Test
	public void testSimple() {
		final int a = 1 + 1;
		assertThat( a ).isEqualTo( 2 );
	}
	
	@Test
	public void test() {
		final MongoCollection< Document > csm = data.getDatabase().getCollection( "users" );
		assertThat( 2 == csm.count() );
		LOGGER.trace( config.getActiveMongoDBName() );
		LOGGER.trace( data.getDatabase().getName() );
	}
}
