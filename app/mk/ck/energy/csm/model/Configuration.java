package mk.ck.energy.csm.model;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class Configuration {
	
	public static final String												MONGODB_DEFAULT_NAME			= "mongodb";
	
	public static final String												MONGODB_CONFIG_NAME				= MONGODB_DEFAULT_NAME + ".config";
	
	public static final String												MONGODB_DATABASE_DEFAULT	= MONGODB_DEFAULT_NAME + ".default";
	
	public static final String												MSSQL_DEFAULT_NAME				= "mssql.db14_r";
	
	public static final String												ADDRESS_REGIONS						= "address.regions";
	
	public static final String												ADDRESS_LOCATIONS					= "address.locations";
	
	public static final String												ADDRESS_STREETS						= "address.streets";
	
	public static final String												MSSQL_DB_TABLES						= "db.tables";
	
	public static final String												COMPANY_EMPLOYEES_XML			= "employees.xml";
	
	private static final Logger												LOGGER										= LoggerFactory.getLogger( Configuration.class );
	
	private static Map< String, String >							mongoURIs									= new HashMap< String, String >( 0 );
	
	private static Map< String, play.Configuration >	mongoConfigurations				= new HashMap< String, play.Configuration >( 0 );
	
	private String																		activeMongoDB;
	
	private File																			regionsFileXML;
	
	private List< File >															locationsFileXML;
	
	private File																			streetsFileXML;
	
	private File																			employeesFileXML;
	
	private final String															driverMSSQL;
	
	private final String															urlMSSQL;
	
	private Statement																	statementMSSQL;
	
	private Connection																connectionMSSQL;
	
	private final Map< String, String >								tablesMSSQL								= new HashMap< String, String >( 0 );
	
	private void createConfigurations() {
		if ( !mongoURIs.isEmpty() || !mongoConfigurations.isEmpty() ) {
			mongoURIs.clear();
			mongoConfigurations.clear();
		}
		final List< String > keys = play.Play.application().configuration().getStringList( MONGODB_CONFIG_NAME );
		// final play.Configuration cfg =
		// play.Play.application().configuration().getConfig( MONGODB_CONFIG_NAME );
		// final List< String > keys = cfg.getStringList( MONGODB_DEFAULT_NAME );
		LOGGER.info( "{}={}", MONGODB_CONFIG_NAME, keys );
		for ( final String key : keys ) {
			final String cName = MONGODB_DEFAULT_NAME + "." + key;
			if ( !key.equals( "default" ) ) {
				final play.Configuration cConf = play.Play.application().configuration().getConfig( cName );
				mongoConfigurations.put( cName, cConf );
				final StringBuilder sb = new StringBuilder( "mongodb://" ); // mongodb://
				sb.append( cConf.getString( "host" ) );
				sb.append( ":" );
				sb.append( cConf.getString( "port" ) );
				sb.append( "/" );
				sb.append( cConf.getString( "name" ) );
				mongoURIs.put( cName, sb.toString() );
			} else
				activeMongoDB = play.Play.application().configuration().getString( cName );
		}
	}
	
	private Configuration() {
		final play.Configuration config = play.Play.application().configuration().getConfig( MSSQL_DEFAULT_NAME );
		driverMSSQL = config.getString( "driver" );
		LOGGER.debug( "Driver is {}", driverMSSQL );
		urlMSSQL = config.getString( "url" );
		LOGGER.debug( "URL is {}", urlMSSQL );
		createConfigurations();
	}
	
	public static Configuration getInstance() {
		return new Configuration();
	}
	
	public Map< String, String > getMongoURIs() {
		if ( mongoURIs.isEmpty() )
			createConfigurations();
		return mongoURIs;
	}
	
	public Map< String, play.Configuration > getMongoConfigurations() {
		if ( mongoConfigurations.isEmpty() )
			createConfigurations();
		return mongoConfigurations;
	}
	
	public String getActiveMongoDBName() {
		return activeMongoDB;
	}
	
	public void setActiveMongoDBName( final String activeMongoDB ) {
		this.activeMongoDB = activeMongoDB;
	}
	
	public static String testConnection( final String keyURI ) {
		final play.Configuration config = mongoConfigurations.get( keyURI );
		final play.Configuration credentials = config.getConfig( "credentials" );
		if ( credentials != null ) {
			final String dbName = config.getString( "name" );
			final MongoCredential credential = MongoCredential.createCredential( credentials.getString( "user" ), dbName, credentials
					.getString( "password" ).toCharArray() );
			final MongoClient mongoClient = new MongoClient( new ServerAddress( keyURI ), Arrays.asList( credential ) );
			if ( !mongoClient.getDatabase( dbName ).getName().equals( dbName ) ) {
				mongoClient.close();
				return "Cannot authenticate";
			}
			mongoClient.close();
		}
		return "Connected";
	}
	
	public File getRegionsFileXML() {
		if ( regionsFileXML == null ) {
			regionsFileXML = play.Play.application().getFile(
					"/lib/" + play.Play.application().configuration().getString( ADDRESS_REGIONS ) );
			LOGGER.trace( "XML file is ready {}", regionsFileXML.getName() );
		}
		return regionsFileXML;
	}
	
	public File getLocationsFileXML( final int index ) {
		if ( locationsFileXML == null ) {
			final List< String > keys = play.Play.application().configuration().getStringList( ADDRESS_LOCATIONS );
			locationsFileXML = new ArrayList< File >( 0 );
			for ( final String key : keys )
				locationsFileXML.add( play.Play.application().getFile( "/lib/" + key ) );
		}
		final File file = locationsFileXML.get( index );
		LOGGER.trace( "XML file is ready {}", file.getName() );
		return file;
	}
	
	public File getStreetsFileXML() {
		if ( streetsFileXML == null ) {
			streetsFileXML = play.Play.application().getFile(
					"/lib/" + play.Play.application().configuration().getString( ADDRESS_STREETS ) );
			LOGGER.trace( "XML file is ready {}", streetsFileXML.getName() );
		}
		return streetsFileXML;
	}
	
	public File getEmployeesFileXML() {
		if ( employeesFileXML == null ) {
			employeesFileXML = play.Play.application().getFile( "/lib/" + COMPANY_EMPLOYEES_XML );
			LOGGER.trace( "XML file is ready {}", employeesFileXML.getName() );
		}
		return employeesFileXML;
	}
	
	public File getSQLFileByName( final String sqlFileName ) {
		final String ext = ".sql";
		final String fuleFileName = sqlFileName.indexOf( ext ) < 0 ? sqlFileName + ext : sqlFileName;
		final File file = play.Play.application().getFile( "/conf/db/sql/" + fuleFileName );
		LOGGER.trace( "SQL file is ready {}", file.getName() );
		return file;
	}
	
	public String getMSSQLDriver() {
		return driverMSSQL;
	}
	
	public String getMSSQLURL() {
		return urlMSSQL;
	}
	
	public Connection getMSSQLConnection() {
		try {
			if ( connectionMSSQL == null || connectionMSSQL.isClosed() )
				createMSSQLData();
		}
		catch ( final SQLException sqle ) {
			LOGGER.error( "A database access error occurs. {}", sqle );
		}
		return connectionMSSQL;
	}
	
	public Statement getMSSQLStatement() {
		try {
			if ( statementMSSQL == null || statementMSSQL.isClosed() )
				createMSSQLData();
		}
		catch ( final SQLException sqle ) {
			LOGGER.error( "A database access error occurs. {}", sqle );
		}
		return statementMSSQL;
	}
	
	private void createMSSQLData() {
		try {
			Class.forName( driverMSSQL );
			connectionMSSQL = DriverManager.getConnection( urlMSSQL );
			statementMSSQL = connectionMSSQL.createStatement();
		}
		catch ( final ClassNotFoundException cnfe ) {
			LOGGER.error( "ClassNotFoundException. Driver {} not found in {}.", driverMSSQL, this );
		}
		catch ( final SQLException sqle ) {
			LOGGER.error( "SQLException in connection. Driver {} not found in {}.", driverMSSQL, this );
		}
	}
	
	public scala.collection.mutable.Map< String, String > getMSSQLDBTablesAsScala() {
		return scala.collection.JavaConversions.mapAsScalaMap( getMSSQLDBTables() );
	}
	
	public Map< String, String > getMSSQLDBTables() {
		if ( tablesMSSQL.isEmpty() ) {
			final play.Configuration config = play.Play.application().configuration().getConfig( MSSQL_DB_TABLES );
			for ( final String key : config.keys() )
				tablesMSSQL.put( key, config.getString( key ) );
		}
		return tablesMSSQL;
	}
}
