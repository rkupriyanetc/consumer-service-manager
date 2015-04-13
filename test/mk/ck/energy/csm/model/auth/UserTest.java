package mk.ck.energy.csm.model.auth;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import mk.ck.energy.csm.model.Configuration;
import mk.ck.energy.csm.model.Database;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.i18n.Messages;
import play.twirl.api.Content;

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
		final Content html = views.html.index.render();
		assertThat( contentType( html ) ).isEqualTo( "text/html" );
		assertThat( contentAsString( html ) ).contains( Messages.get( "page.home.title" ) );
		LOGGER.trace( config.getActiveMongoDBName() );
		LOGGER.trace( data.getDatabase().getName() );
	}
}
