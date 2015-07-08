package mk.ck.energy.csm.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import mk.ck.energy.csm.model.codecs.AddressLocationCodec;
import mk.ck.energy.csm.model.codecs.AddressPlaceCodec;
import mk.ck.energy.csm.model.codecs.AddressTopCodec;
import mk.ck.energy.csm.model.codecs.ConsumerCodec;
import mk.ck.energy.csm.model.codecs.MeasurementCodec;
import mk.ck.energy.csm.model.codecs.MeterCodec;
import mk.ck.energy.csm.model.codecs.MeterDeviceCodec;
import mk.ck.energy.csm.model.codecs.PlumbCodec;
import mk.ck.energy.csm.model.codecs.TokenActionCodec;
import mk.ck.energy.csm.model.codecs.UndefinedConsumerCodec;
import mk.ck.energy.csm.model.codecs.UserCodec;

import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

/**
 * @author KYL
 */
public class Database {
	
	private static final Logger										LOGGER				= LoggerFactory.getLogger( Database.class );
	
	private static Configuration									configuration	= Configuration.getInstance();
	
	private static final Map< String, Database >	DATABASES			= new HashMap<>();
	
	private static final ReadWriteLock						DBS_LOCK			= new ReentrantReadWriteLock();
	
	private MongoDatabase													database;
	
	private MongoClient														mongoClient;
	
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
	
	public MongoDatabase getDatabase() {
		connect();
		return database;
	}
	
	public MongoClient getMongoClient() {
		return mongoClient;
	}
	
	private void connect() {
		lock.readLock().lock();
		if ( database == null ) {
			lock.readLock().unlock();
			lock.writeLock().lock();
			if ( database == null ) {
				final play.Configuration config = play.Play.application().configuration().getConfig( name );
				LOGGER.trace( "Database {} configuration {}", name, config.getWrappedConfiguration() );
				final String dbName = config.getString( "name" );
				final play.Configuration credentials = config.getConfig( "credentials" );
				if ( credentials != null ) {
					final MongoCredential credential = MongoCredential.createCredential( credentials.getString( "user" ), dbName,
							credentials.getString( "password" ).toCharArray() );
					if ( mongoClient == null ) {
						final Codec< Document > defaultDocumentCodec = MongoClient.getDefaultCodecRegistry().get( Document.class );
						final UserCodec userCodec = new UserCodec( defaultDocumentCodec );
						final TokenActionCodec tokenActionCodec = new TokenActionCodec( defaultDocumentCodec );
						final AddressTopCodec addressTopCodec = new AddressTopCodec( defaultDocumentCodec );
						final AddressLocationCodec addressLocationCodec = new AddressLocationCodec( defaultDocumentCodec );
						final AddressPlaceCodec addressPlaceCodec = new AddressPlaceCodec( defaultDocumentCodec );
						final MeterDeviceCodec meterDeviceCodec = new MeterDeviceCodec( defaultDocumentCodec );
						final MeterCodec meterCodec = new MeterCodec( defaultDocumentCodec );
						final ConsumerCodec consumerCodec = new ConsumerCodec( defaultDocumentCodec );
						final UndefinedConsumerCodec undefinedConsumerCodec = new UndefinedConsumerCodec( defaultDocumentCodec );
						final PlumbCodec plumbCodec = new PlumbCodec( defaultDocumentCodec );
						final MeasurementCodec measurementCodec = new MeasurementCodec( defaultDocumentCodec );
						final CodecRegistry codecRegistry = CodecRegistries.fromRegistries( MongoClient.getDefaultCodecRegistry(),
								CodecRegistries.fromCodecs( userCodec, tokenActionCodec, addressTopCodec, addressLocationCodec,
										addressPlaceCodec, meterDeviceCodec, meterCodec, consumerCodec, undefinedConsumerCodec, plumbCodec,
										measurementCodec ) );
						final MongoClientOptions options = MongoClientOptions.builder().codecRegistry( codecRegistry ).build();
						mongoClient = new MongoClient( new ServerAddress( config.getString( "host" ) ), Arrays.asList( credential ), options );
					}
					database = mongoClient.getDatabase( dbName );
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
