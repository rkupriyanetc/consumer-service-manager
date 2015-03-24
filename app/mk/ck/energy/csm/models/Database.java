package mk.ck.energy.csm.models;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;

/**
 * @author KYL
 */
public class Database {
	
	private static final Logger										LOGGER				= LoggerFactory.getLogger( Database.class );
	
	private static Configuration									configuration	= Configuration.getInstance();
	
	private static final Map< String, Database >	DATABASES			= new HashMap<>();
	
	private static final ReadWriteLock						DBS_LOCK			= new ReentrantReadWriteLock();
	
	private DB																		database;
	
	private final ReadWriteLock										lock					= new ReentrantReadWriteLock();
	
	private final String													name;
	
	private Database( final String name ) {
		this.name = name;
	}
	
	public static Database getInstance() {
		DBS_LOCK.readLock().lock();
		final String name = configuration.getActiveMongoDBName();
		final Database db = DATABASES.get( name );
		if ( db == null ) {
			DBS_LOCK.readLock().unlock();
			DBS_LOCK.writeLock().lock();
			try {
				if ( !DATABASES.containsKey( name ) )
					DATABASES.put( name, new Database( name ) );
				return DATABASES.get( name );
			}
			finally {
				DBS_LOCK.writeLock().unlock();
			}
		} else {
			DBS_LOCK.readLock().unlock();
			return db;
		}
	}
	
	public DB getDatabase() {
		connect();
		return database;
	}
	
	private void connect() {
		lock.readLock().lock();
		if ( database == null ) {
			lock.readLock().unlock();
			lock.writeLock().lock();
			if ( database == null ) {
				final play.Configuration config = play.Play.application().configuration().getConfig( name );
				LOGGER.trace( "Database {} configuration {}", name, config.getWrappedConfiguration() );
				try {
					final MongoClient mongoClient = new MongoClient( config.getString( "host" ), config.getInt( "port" ) );
					database = mongoClient.getDB( config.getString( "name" ) );
					final play.Configuration credentials = config.getConfig( "credentials" );
					if ( credentials != null )
						if ( !database.authenticate( credentials.getString( "user" ), credentials.getString( "password" ).toCharArray() ) )
							throw new IllegalStateException( "Cannot authenticate" );
				}
				catch ( final UnknownHostException e ) {
					throw new IllegalStateException( "Cannot connect to database", e );
				}
			}
			lock.writeLock().unlock();
		} else
			lock.readLock().unlock();
	}
	
	public static Configuration getConfiguration() {
		return configuration;
	}
}
