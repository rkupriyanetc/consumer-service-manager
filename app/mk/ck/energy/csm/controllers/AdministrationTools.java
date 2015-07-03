package mk.ck.energy.csm.controllers;

import static play.data.Form.form;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import mk.ck.energy.csm.model.Address;
import mk.ck.energy.csm.model.AddressLocation;
import mk.ck.energy.csm.model.AddressNotFoundException;
import mk.ck.energy.csm.model.AddressPlace;
import mk.ck.energy.csm.model.AddressTop;
import mk.ck.energy.csm.model.AdministrativeCenterType;
import mk.ck.energy.csm.model.Configuration;
import mk.ck.energy.csm.model.Consumer;
import mk.ck.energy.csm.model.ConsumerException;
import mk.ck.energy.csm.model.ConsumerStatusType;
import mk.ck.energy.csm.model.ConsumerType;
import mk.ck.energy.csm.model.Database;
import mk.ck.energy.csm.model.Documents;
import mk.ck.energy.csm.model.HouseType;
import mk.ck.energy.csm.model.ImpossibleCreatingException;
import mk.ck.energy.csm.model.LocationMeterType;
import mk.ck.energy.csm.model.LocationType;
import mk.ck.energy.csm.model.Meter;
import mk.ck.energy.csm.model.MeterDevice;
import mk.ck.energy.csm.model.MeterDeviceNotFoundException;
import mk.ck.energy.csm.model.MeterNotFoundException;
import mk.ck.energy.csm.model.StreetType;
import mk.ck.energy.csm.model.UndefinedConsumer;
import mk.ck.energy.csm.model.UndefinedConsumerType;
import mk.ck.energy.csm.model.auth.MyUser;
import mk.ck.energy.csm.model.auth.User;
import mk.ck.energy.csm.model.auth.UserNotFoundException;
import mk.ck.energy.csm.model.auth.UserRole;
import mk.ck.energy.csm.providers.MyFirstLastNameUserPasswordAuthUser;

import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.admin.index;
import views.html.admin.userAdd;
import views.html.admin.viewXML;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

public class AdministrationTools extends Controller {
	
	private static final Logger					LOGGER										= LoggerFactory.getLogger( User.class );
	
	private static final Configuration	CONFIGURATION							= Database.getConfiguration();
	
	private static final String					CONSUMER_CODE_STATUS_NAME	= "consumer.code.status.type";
	
	private static final String					METER_CODE_PLACE_NAME			= "place.meter.install.type";
	
	private static final String					METERDEVICES_RESULT				= "MeterDevices_Result.txt";
	
	private static final String					CONSUMERS_RESULT					= "Consumers_Result.txt";
	
	private static final String					METERS_RESULT							= "Meters_Result.txt";
	
	public static class XMLText {
		
		private String	textXML;
		
		private int			fileNumber;
		
		public XMLText() {}
		
		public String getTextXML() {
			return textXML;
		}
		
		public void setTextXML( final String textXML ) {
			this.textXML = textXML;
		}
		
		public int getFileNumber() {
			return fileNumber;
		}
		
		public void setFileNumber( final int fileNumber ) {
			this.fileNumber = fileNumber;
		}
	}
	
	public static class StepByStep {
		
		private boolean					addressTopXML;
		
		private boolean					addressLocationXML;
		
		private boolean					dBTableLocations;
		
		private boolean					locationXML;
		
		private boolean					dBTableStreets;
		
		private boolean					addressStreetsXML;
		
		private boolean					fillingMeterDevices;
		
		private boolean					updateConsumers;
		
		private boolean					updateMeters;
		
		private List< String >	references;
		
		public static StepByStep newInstance() {
			return new StepByStep();
		}
		
		public StepByStep() {}
		
		public boolean isAddressTopXML() {
			return addressTopXML;
		}
		
		public void setAddressTopXML( final boolean addressTopXML ) {
			this.addressTopXML = addressTopXML;
		}
		
		public boolean isAddressLocationXML() {
			return addressLocationXML;
		}
		
		public void setAddressLocationXML( final boolean addressLocationXML ) {
			this.addressLocationXML = addressLocationXML;
		}
		
		public boolean isdBTableLocations() {
			return dBTableLocations;
		}
		
		public void setdBTableLocations( final boolean dBTableLocations ) {
			this.dBTableLocations = dBTableLocations;
		}
		
		public boolean isLocationXML() {
			return locationXML;
		}
		
		public void setLocationXML( final boolean locationXML ) {
			this.locationXML = locationXML;
		}
		
		public boolean isdBTableStreets() {
			return dBTableStreets;
		}
		
		public void setdBTableStreets( final boolean dBTableStreets ) {
			this.dBTableStreets = dBTableStreets;
		}
		
		public boolean isAddressStreetsXML() {
			return addressStreetsXML;
		}
		
		public void setAddressStreetsXML( final boolean addressStreetsXML ) {
			this.addressStreetsXML = addressStreetsXML;
		}
		
		public boolean isFillingMeterDevices() {
			return fillingMeterDevices;
		}
		
		public void setFillingMeterDevices( final boolean fillingMeterDevices ) {
			this.fillingMeterDevices = fillingMeterDevices;
		}
		
		public boolean isUpdateConsumers() {
			return updateConsumers;
		}
		
		public void setUpdateConsumers( final boolean updateConsumers ) {
			this.updateConsumers = updateConsumers;
		}
		
		public boolean isUpdateMeters() {
			return updateMeters;
		}
		
		public void setUpdateMeters( final boolean updateMeters ) {
			this.updateMeters = updateMeters;
		}
		
		public List< String > getReferences() {
			return references;
		}
		
		public void setReferences( final List< String > references ) {
			this.references = references;
		}
	}
	
	public static final Form< MyUser >			USER_FORM					= form( MyUser.class );
	
	public static final Form< XMLText >			XML_TEXT_FORM			= form( XMLText.class );
	
	public static final Form< StepByStep >	STEP_BY_STEP_FORM	= form( StepByStep.class );
	
	@Restrict( @Group( UserRole.ADMIN_ROLE_NAME ) )
	public static Result index( final int indexTab ) {
		return index( STEP_BY_STEP_FORM.fill( new StepByStep() ), indexTab );
	}
	
	@Restrict( @Group( UserRole.ADMIN_ROLE_NAME ) )
	public static Result index( final Form< ? > form, final int indexTab ) {
		return ok( index.render( CONFIGURATION, findAllUsersByAdminAndOperRoles(), form, indexTab ) );
	}
	
	@Restrict( @Group( UserRole.ADMIN_ROLE_NAME ) )
	public static Result addUser( final String role ) {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final MyUser u = new MyUser( role );
		return ok( userAdd.render( USER_FORM.fill( u ) ) );
	}
	
	@Restrict( @Group( UserRole.ADMIN_ROLE_NAME ) )
	public static Result doAddUser() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< MyUser > filledForm = USER_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not select whether to link or not link
			return badRequest( userAdd.render( filledForm ) );
		else {
			final MyUser u = filledForm.get();
			flash( Application.FLASH_MESSAGE_KEY, Messages.get( "page.admin.addUser.success" ) );
			final MyFirstLastNameUserPasswordAuthUser auth = new MyFirstLastNameUserPasswordAuthUser( u );
			final User user = User.create( auth );
			user.verify();
			final String ro = u.getRole();
			if ( ro.equals( UserRole.OPER_ROLE_NAME ) )
				user.addRole( UserRole.OPER );
			else
				if ( ro.equals( UserRole.ADMIN_ROLE_NAME ) ) {
					user.addRole( UserRole.OPER );
					user.addRole( UserRole.ADMIN );
				}
			user.updateRoles();
			return index( 0 );
		}
	}
	
	@Restrict( @Group( UserRole.ADMIN_ROLE_NAME ) )
	public static Result doChangeActiveDB() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Map< String, String[] > mapa = request().body().asFormUrlEncoded();
		final String s = mapa.keySet().iterator().next();
		CONFIGURATION.setActiveMongoDBName( mapa.get( s )[ 0 ] );
		flash( Application.FLASH_MESSAGE_KEY, Messages.get( "page.admin.adminTab.hostChange.success" ) );
		return index( 1 );
	}
	
	@Restrict( @Group( UserRole.ADMIN_ROLE_NAME ) )
	public static Result removeUser( final String userId ) {
		final User localUser = User.getLocalUser( session() );
		if ( !localUser.getId().equals( userId ) )
			try {
				final User u = User.remove( userId );
				flash( Application.FLASH_MESSAGE_KEY, Messages.get( "page.admin.delUser.success" ) );
				if ( !u.getId().equals( userId ) )
					LOGGER.warn( "O-o-o-ops! IDs are not the same. The specified Id is {}. Found Id is {}", userId, u.getId() );
			}
			catch ( final UserNotFoundException unfe ) {
				LOGGER.warn( "Could not found user by id {}.", userId );
			}
		else
			flash( Application.FLASH_MESSAGE_KEY, Messages.get( "page.admin.delUser.noSuccess" ) );
		return index( 0 );
	}
	
	@Restrict( @Group( UserRole.ADMIN_ROLE_NAME ) )
	public static Result viewXML( final int fileN ) {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final XMLText text = new XMLText();
		File f = null;
		switch ( fileN ) {
			case 0 :
				f = CONFIGURATION.getRegionsFileXML();
				break;
			case 1 :
			case 2 :
				f = CONFIGURATION.getLocationsFileXML( fileN - 1 );
				break;
			case 3 :
				f = CONFIGURATION.getStreetsFileXML();
				break;
			default :
				LOGGER.error( "The parameter is invalid in viewXML" );
				break;
		}
		text.setFileNumber( fileN );
		final int size = ( int )f.length();
		int s = 0;
		final byte b[] = new byte[ size + 1 ];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream( f );
			s = fis.read( b );
			if ( s != size )
				LOGGER.error( "Error read the file {}", f );
			else
				text.setTextXML( new String( new String( b, "UTF-8" ).getBytes( "cp1251" ) ) );
		}
		catch ( final FileNotFoundException fnfe ) {
			LOGGER.error( "Do't exists {}", f );
		}
		catch ( final IOException ioe ) {
			LOGGER.error( "Do't read the stream {} to byte[] array", fis );
		}
		finally {
			try {
				fis.close();
			}
			catch ( final IOException ioe ) {
				LOGGER.error( "Do't close the stream {}", fis );
			}
		}
		return ok( viewXML.render( XML_TEXT_FORM.fill( text ) ) );
	}
	
	@Restrict( @Group( UserRole.ADMIN_ROLE_NAME ) )
	public static Result doViewXML() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< XMLText > filledForm = XML_TEXT_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not select whether to link or not link
			return badRequest( viewXML.render( filledForm ) );
		else {
			final XMLText text = filledForm.get();
			File f = null;
			final int num = text.getFileNumber();
			switch ( num ) {
				case 0 :
					f = CONFIGURATION.getRegionsFileXML();
					break;
				case 1 :
				case 2 :
					f = CONFIGURATION.getLocationsFileXML( num - 1 );
					break;
				case 3 :
					f = CONFIGURATION.getStreetsFileXML();
					break;
				default :
					LOGGER.error( "The parameter is invalid in doViewXML" );
					break;
			}
			FileOutputStream fos = null;
			try {
				final byte b[] = text.getTextXML().getBytes( "UTF-8" );
				fos = new FileOutputStream( f );
				fos.write( b );
				fos.flush();
				fos.close();
			}
			catch ( final FileNotFoundException fnfe ) {
				LOGGER.error( "Do't exists {}", f );
			}
			catch ( final IOException ioe ) {
				LOGGER.error( "Do't write byte[] array to stream {}", fos );
			}
			return ok( index.render( CONFIGURATION, findAllUsersByAdminAndOperRoles(), filledForm, 2 ) );
		}
	}
	
	@Restrict( @Group( UserRole.ADMIN_ROLE_NAME ) )
	public static Result doSteppingSynchronizationDB() {
		final Form< StepByStep > filledForm = STEP_BY_STEP_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not select whether to link or not link
			return badRequest( index.render( CONFIGURATION, findAllUsersByAdminAndOperRoles(), filledForm, 2 ) );
		else {
			final StepByStep step = filledForm.get();
			// Processed prepared template. Path is "./lib/addressTop.xml"
			if ( step.isAddressTopXML() ) {
				final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware( true );
				try {
					final DocumentBuilder builder = factory.newDocumentBuilder();
					final Document document = builder.parse( CONFIGURATION.getRegionsFileXML() );
					final NodeList list = document.getDocumentElement().getChildNodes();
					for ( int i = 0; i < list.getLength(); i++ ) {
						final Node node = list.item( i );
						if ( node instanceof Element ) {
							final NodeList childNodes = node.getChildNodes();
							String name = "";
							String id = "";
							String nodeName = "";
							String ref = "";
							for ( int j = 0; j < childNodes.getLength(); j++ ) {
								final Node cNode = childNodes.item( j );
								if ( cNode instanceof Element ) {
									final String content = cNode.getLastChild().getTextContent().trim();
									nodeName = cNode.getNodeName();
									switch ( nodeName ) {
										case "name" :
											name = content;
											break;
										case "refId" :
											id = content;
											ref = "refId";
											break;
										case "references" :
											id = content;
											ref = "references";
											break;
									}
								}
							}
							switch ( ref ) {
								case "references" :
									try {
										final List< AddressTop > addr = AddressTop.findLikeName( id );
										final AddressTop at = AddressTop.create( name, addr.get( 0 ).getId() );
										at.save();
										LOGGER.trace( "Address top saved {}", at );
									}
									catch ( final AddressNotFoundException anfe ) {}
									catch ( final ImpossibleCreatingException ice ) {}
									break;
								default :
									if ( "0".equals( id ) )
										id = null;
									try {
										final AddressTop aat = AddressTop.create( name, id );
										aat.save();
										LOGGER.trace( "Address top saved {}", aat );
									}
									catch ( final ImpossibleCreatingException ice ) {}
									break;
							}
						}
					}
				}
				catch ( final ParserConfigurationException pce ) {
					LOGGER.error( "Error in builder {}", pce );
				}
				catch ( final SAXException saxe ) {
					LOGGER.error( "Error parcing in {}", saxe );
				}
				catch ( final IOException ioe ) {
					LOGGER.error( "Error parcing in {}", ioe );
				}
			}
			// Processed prepared template. Path is "./lib/location.xml"
			if ( step.isLocationXML() ) {
				final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				try {
					final DocumentBuilder builder = factory.newDocumentBuilder();
					final Document document = builder.parse( CONFIGURATION.getLocationsFileXML( 0 ) );
					final NodeList list = document.getDocumentElement().getChildNodes();
					for ( int i = 0; i < list.getLength(); i++ ) {
						final Node node = list.item( i );
						if ( node instanceof Element ) {
							final NodeList childNodes = node.getChildNodes();
							String name = "";
							String nameType = "";
							String admType = "";
							String id = "";
							String nodeName = "";
							String ref = "";
							for ( int j = 0; j < childNodes.getLength(); j++ ) {
								final Node cNode = childNodes.item( j );
								if ( cNode instanceof Element ) {
									final String content = cNode.getLastChild().getTextContent().trim();
									nodeName = cNode.getNodeName();
									switch ( nodeName ) {
										case "name" :
											name = content;
											break;
										case "ref_id" :
											id = content;
											ref = "refId";
											break;
										case "references" :
											id = content;
											ref = "references";
											break;
										case "location_type" :
											nameType = content;
											break;
										case "adm_type" :
											admType = content;
											break;
									}
								}
							}
							final List< AddressTop > addr = new LinkedList<>();
							switch ( ref ) {
								case "references" :
									try {
										addr.addAll( AddressTop.findLikeName( id ) );
									}
									catch ( final AddressNotFoundException anfe ) {
										LOGGER.error( "Cannot find addressTop", anfe );
									}
									break;
								default :
									try {
										addr.add( AddressTop.findById( id ) );
									}
									catch ( final AddressNotFoundException anfe ) {
										LOGGER.error( "{} for parse refId in file AddresTop.xml", anfe );
									}
									break;
							}
							final LocationType lt = LocationType.abbreviationToLocationType( nameType );
							if ( !lt.equals( LocationType.UNSPECIFIED ) ) {
								Set< AdministrativeCenterType > at = new LinkedHashSet<>();
								final StringTokenizer st = new StringTokenizer( admType, "," );
								while ( st.hasMoreTokens() ) {
									final String token = st.nextToken().trim();
									try {
										final AdministrativeCenterType act = AdministrativeCenterType.abbreviationToAdministrativeCenterType( token );
										at.add( act );
									}
									catch ( final IllegalArgumentException iae ) {
										LOGGER.debug( "This token {} is no AdministrativeCenterType", token );
									}
									catch ( final NullPointerException npe ) {
										LOGGER.debug( "Parameter should not be empty in valueOf({})", token );
									}
								}
								AddressLocation al = null;
								if ( at.isEmpty() )
									at = null;
								try {
									al = AddressLocation.create( addr.get( 0 ), name, lt, at );
									al.save();
								}
								catch ( final ImpossibleCreatingException ice ) {}
							} else
								LOGGER.warn( "Specifig LocationType {} is not save. Undefined...", nameType );
						}
					}
				}
				catch ( final ParserConfigurationException pce ) {
					LOGGER.error( "Error in builder {}", pce );
				}
				catch ( final SAXException saxe ) {
					LOGGER.error( "Error parcing in {}", saxe );
				}
				catch ( final IOException ioe ) {
					LOGGER.error( "Error parcing in {}", ioe );
				}
			}
			// Processing dBTableLocations - creates a xml-file addresses settlements.
			// Path is "./lib/addressLocation.xml"
			if ( step.isdBTableLocations() ) {
				final String locationTable = CONFIGURATION.getMSSQLDBTables().get( "locations" );
				final Statement statement = CONFIGURATION.getMSSQLStatement();
				try {
					final String select = "select nazva_pos from " + locationTable + " where code_pos != 0 order by nazva_pos";
					final ResultSet result = statement.executeQuery( select );
					final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setNamespaceAware( true );
					final DocumentBuilder builder = factory.newDocumentBuilder();
					final Document document = builder.newDocument();
					final Element root = document.createElement( "locations" );
					document.appendChild( root );
					while ( result.next() ) {
						final Element location = document.createElement( "location" );
						root.appendChild( location );
						final String field = result.getString( 1 );
						final int pos = field.indexOf( ". " ) + 1;
						final String typeStr = field.substring( 0, pos ).trim();
						final String nameStr = field.substring( pos ).trim();
						final Element name = document.createElement( "name" );
						name.appendChild( document.createTextNode( nameStr ) );
						location.appendChild( name );
						final Element refId = document.createElement( "ref_id" );
						final String key = step.getReferences().get( 0 );
						refId.appendChild( document.createTextNode( key ) );
						location.appendChild( refId );
						final Element type = document.createElement( "location_type" );
						type.appendChild( document.createTextNode( typeStr ) );
						location.appendChild( type );
					}
					final TransformerFactory transformerFactory = TransformerFactory.newInstance();
					transformerFactory.setAttribute( "indent-number", 2 );
					final Transformer transformer = transformerFactory.newTransformer();
					transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
					transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
					removeWhitespaceNodes( document.getDocumentElement() );
					final DOMSource source = new DOMSource( document );
					final StreamResult xmlFile = new StreamResult( CONFIGURATION.getLocationsFileXML( 1 ) );
					transformer.transform( source, xmlFile );
				}
				catch ( final SQLFeatureNotSupportedException sfnse ) {
					LOGGER.error( "SQLFeatureNotSupportedException in the ResultSet.first(): {}", sfnse );
				}
				catch ( final SQLException sqle ) {
					LOGGER.error( "Query no retrive result. Exception: {}", sqle );
				}
				catch ( final FactoryConfigurationError fce ) {
					LOGGER.error( "DocumentBuilderFactory.newInstance(). Exception: {}", fce );
				}
				catch ( final ParserConfigurationException pce ) {
					LOGGER.error( "Building Document. Exception: {}", pce );
				}
				catch ( final DOMException de ) {
					LOGGER.error( "Building tree exception: {}", de );
				}
				catch ( final TransformerFactoryConfigurationError tfce ) {
					LOGGER.error( "TransformerFactory.newInstance(). Exception: {}", tfce );
				}
				catch ( final TransformerConfigurationException tce ) {
					LOGGER.error( "Building Transformer. Exception: {}", tce );
				}
				catch ( final TransformerException te ) {
					LOGGER.error( "Transform to XML-File. Exception: {}", te );
				}
			}
			// Processed prepared template. Path is "./lib/addressLocation.xml". It is
			// necessary to edit the regional center
			if ( step.isAddressLocationXML() ) {
				final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				try {
					final DocumentBuilder builder = factory.newDocumentBuilder();
					final Document document = builder.parse( CONFIGURATION.getLocationsFileXML( 1 ) );
					final NodeList list = document.getDocumentElement().getChildNodes();
					for ( int i = 0; i < list.getLength(); i++ ) {
						final Node node = list.item( i );
						if ( node instanceof Element ) {
							final NodeList childNodes = node.getChildNodes();
							String name = "";
							String nameType = "";
							String admType = "";
							String id = "";
							String nodeName = "";
							String ref = "";
							for ( int j = 0; j < childNodes.getLength(); j++ ) {
								final Node cNode = childNodes.item( j );
								if ( cNode instanceof Element ) {
									final String content = cNode.getLastChild().getTextContent().trim();
									nodeName = cNode.getNodeName();
									switch ( nodeName ) {
										case "name" :
											name = content;
											break;
										case "ref_id" :
											id = content;
											ref = "refId";
											break;
										case "references" :
											id = content;
											ref = "references";
											break;
										case "location_type" :
											nameType = content;
											break;
										case "adm_type" :
											admType = content;
											break;
									}
								}
							}
							final List< AddressTop > addr = new LinkedList<>();
							switch ( ref ) {
								case "references" :
									try {
										addr.addAll( AddressTop.findLikeName( id ) );
									}
									catch ( final AddressNotFoundException anfe ) {
										LOGGER.error( "Cannot find addressTop", anfe );
									}
									break;
								default :
									try {
										addr.add( AddressTop.findById( id ) );
									}
									catch ( final AddressNotFoundException anfe ) {
										LOGGER.error( "{} for parse refId in file AddresTop.xml", anfe );
									}
									break;
							}
							final LocationType lt = LocationType.abbreviationToLocationType( nameType );
							if ( !lt.equals( LocationType.UNSPECIFIED ) ) {
								Set< AdministrativeCenterType > at = new LinkedHashSet<>();
								final StringTokenizer st = new StringTokenizer( admType, "," );
								while ( st.hasMoreTokens() ) {
									final String token = st.nextToken().trim();
									try {
										final AdministrativeCenterType act = AdministrativeCenterType.abbreviationToAdministrativeCenterType( token );
										at.add( act );
									}
									catch ( final IllegalArgumentException iae ) {
										LOGGER.debug( "This token {} is no AdministrativeCenterType", token );
									}
									catch ( final NullPointerException npe ) {
										LOGGER.debug( "Parameter should not be empty in valueOf({})", token );
									}
								}
								AddressLocation al = null;
								if ( at.isEmpty() )
									at = null;
								try {
									al = AddressLocation.create( addr.get( 0 ), name, lt, at );
									al.save();
								}
								catch ( final ImpossibleCreatingException ice ) {}
							} else
								LOGGER.warn( "Specifig LocationType {} is not save. Undefined...", nameType );
						}
					}
				}
				catch ( final ParserConfigurationException pce ) {
					LOGGER.error( "Error in builder {}", pce );
				}
				catch ( final SAXException saxe ) {
					LOGGER.error( "Error parcing in {}", saxe );
				}
				catch ( final IOException ioe ) {
					LOGGER.error( "Error parcing in {}", ioe );
				}
			}
			// Processing dBTableStreets - creates a xml-file addresses streets.
			// Path is "./lib/addressStreet.xml"
			if ( step.isdBTableStreets() ) {
				final String streetTable = CONFIGURATION.getMSSQLDBTables().get( "streets" );
				final Statement statement = CONFIGURATION.getMSSQLStatement();
				try {
					final String select = "select nazva_street from " + streetTable;
					final ResultSet result = statement.executeQuery( select );
					final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setNamespaceAware( true );
					final DocumentBuilder builder = factory.newDocumentBuilder();
					final Document document = builder.newDocument();
					final Element root = document.createElement( "streets" );
					document.appendChild( root );
					while ( result.next() ) {
						final String field = result.getString( 1 );
						final int pos = field.indexOf( "." ) + 1;
						String typeStr = field.substring( 0, pos ).trim();
						final String nameStr = field.substring( pos ).trim();
						final StreetType st = StreetType.abbreviationToStreetType( typeStr );
						if ( st.equals( StreetType.UNCERTAIN ) ) {
							final Element street = document.createElement( "street" );
							root.appendChild( street );
							final Element name = document.createElement( "name" );
							name.appendChild( document.createTextNode( nameStr ) );
							street.appendChild( name );
							final Element type = document.createElement( "type" );
							// It's Empty element but write ' ' else error for reading
							if ( typeStr.isEmpty() )
								typeStr = ".";
							type.appendChild( document.createTextNode( typeStr ) );
							street.appendChild( type );
						} else
							if ( !st.equals( StreetType.UNSPECIFIED ) )
								try {
									final AddressPlace al = AddressPlace.create( st, nameStr );
									al.save();
								}
								catch ( final ImpossibleCreatingException ice ) {}
					}
					final TransformerFactory transformerFactory = TransformerFactory.newInstance();
					transformerFactory.setAttribute( "indent-number", 2 );
					final Transformer transformer = transformerFactory.newTransformer();
					transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
					transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
					removeWhitespaceNodes( document.getDocumentElement() );
					final DOMSource source = new DOMSource( document );
					final StreamResult xmlFile = new StreamResult( CONFIGURATION.getStreetsFileXML() );
					transformer.transform( source, xmlFile );
				}
				catch ( final SQLFeatureNotSupportedException sfnse ) {
					LOGGER.error( "SQLFeatureNotSupportedException in the ResultSet.first(): {}", sfnse );
				}
				catch ( final SQLException sqle ) {
					LOGGER.error( "Query no retrive result. Exception: {}", sqle );
				}
				catch ( final FactoryConfigurationError fce ) {
					LOGGER.error( "DocumentBuilderFactory.newInstance(). Exception: {}", fce );
				}
				catch ( final ParserConfigurationException pce ) {
					LOGGER.error( "Building Document. Exception: {}", pce );
				}
				catch ( final DOMException de ) {
					LOGGER.error( "Building tree exception: {}", de );
				}
				catch ( final TransformerFactoryConfigurationError tfce ) {
					LOGGER.error( "TransformerFactory.newInstance(). Exception: {}", tfce );
				}
				catch ( final TransformerConfigurationException tce ) {
					LOGGER.error( "Building Transformer. Exception: {}", tce );
				}
				catch ( final TransformerException te ) {
					LOGGER.error( "Transform to XML-File. Exception: {}", te );
				}
			}
			// Processed prepared template. Path is "./lib/addressStreet.xml". To it
			// recorded only vague streets
			if ( step.isAddressStreetsXML() ) {
				final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				try {
					final DocumentBuilder builder = factory.newDocumentBuilder();
					final Document document = builder.parse( CONFIGURATION.getStreetsFileXML() );
					final NodeList list = document.getDocumentElement().getChildNodes();
					for ( int i = 0; i < list.getLength(); i++ ) {
						final Node node = list.item( i );
						if ( node instanceof Element ) {
							final NodeList childNodes = node.getChildNodes();
							String name = "";
							String nameType = "";
							String nodeName = "";
							for ( int j = 0; j < childNodes.getLength(); j++ ) {
								final Node cNode = childNodes.item( j );
								if ( cNode instanceof Element ) {
									final String content = cNode.getLastChild().getTextContent().trim();
									nodeName = cNode.getNodeName();
									switch ( nodeName ) {
										case "name" :
											name = content;
											break;
										case "type" :
											nameType = content;
											break;
									}
								}
							}
							final StreetType st = StreetType.abbreviationToStreetType( nameType );
							if ( !st.equals( StreetType.UNSPECIFIED ) )
								try {
									final AddressPlace al = AddressPlace.create( st, name );
									al.save();
								}
								catch ( final ImpossibleCreatingException ice ) {}
						}
					}
				}
				catch ( final ParserConfigurationException pce ) {
					LOGGER.error( "Error in builder {}", pce );
				}
				catch ( final SAXException saxe ) {
					LOGGER.error( "Error parcing in {}", saxe );
				}
				catch ( final IOException ioe ) {
					LOGGER.error( "Error parcing in {}", ioe );
				}
			}
			// Processing filling MeterDevices
			if ( step.isFillingMeterDevices() ) {
				// Run process
				final String meterDevicesTable = CONFIGURATION.getMSSQLDBTables().get( "meterDevices" );
				final Statement statement = CONFIGURATION.getMSSQLStatement();
				int meterDeviceSize = 0;
				int createdCount = 0;// Create count MeterDevice
				int updatedCount = 0;// Update count MeterDevice
				final ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
				try {
					final String select = "select nazva_marka, fazi, code_method_type, code_power_type, code_reestr, "
							+ "klass, interval from " + meterDevicesTable + " order by nazva_marka, fazi";
					final ResultSet result = statement.executeQuery( select );
					while ( result.next() ) {
						// Meter Name
						final String meterName = result.getString( 1 ).trim();
						// Phasing
						final byte phasing = result.getByte( 2 );
						// Meter MethodType
						byte methodType = result.getByte( 3 );
						if ( methodType == 0 )
							methodType = 1;
						--methodType;
						// Meter InductiveType
						byte inductiveType = result.getByte( 4 );
						if ( inductiveType == 0 )
							inductiveType = 1;
						--inductiveType;
						// Meter RegisterType
						byte registerType = result.getByte( 5 );
						if ( registerType == 0 )
							registerType = 1;
						--registerType;
						// Meter precision
						final double meterPrecision = result.getDouble( 6 );
						// Meter interval
						final byte meterInterval = result.getByte( 7 );
						final MeterDevice meterDevice = MeterDevice.create( meterName, phasing,
								MeterDevice.MethodType.values()[ methodType ], MeterDevice.InductiveType.values()[ inductiveType ],
								MeterDevice.RegisterType.values()[ registerType ], meterPrecision, meterInterval );
						try {
							final MeterDevice md = MeterDevice.findByName( meterName );
							if ( md == null ) {
								LOGGER.trace( "Meter device {} saved. Count Meter devices is {}", meterDevice, ++createdCount );
								streamOut.write( ( "\nCreate MeterDevice " + meterDevice.toString() + "\n" ).getBytes() );
								meterDevice.save();
							} else
								if ( !md.equals( meterDevice ) ) {
									final Bson cQuery = MeterDevice.makeFilterToId( md.getId() );
									final List< Bson > cUpdates = new LinkedList< Bson >();
									if ( md.getPhasing() != meterDevice.getPhasing() )
										cUpdates.add( MeterDevice.makeFilterToPhasing( meterDevice.getPhasing() ) );
									if ( !md.getMethodType().equals( meterDevice.getMethodType() ) )
										cUpdates.add( MeterDevice.makeFilterToMethodType( meterDevice.getMethodType() ) );
									if ( !md.getInductiveType().equals( meterDevice.getInductiveType() ) )
										cUpdates.add( MeterDevice.makeFilterToInductiveType( meterDevice.getInductiveType() ) );
									if ( !md.getRegisterType().equals( meterDevice.getRegisterType() ) )
										cUpdates.add( MeterDevice.makeFilterToRegisterType( meterDevice.getRegisterType() ) );
									if ( md.getPrecision() != meterDevice.getPrecision() )
										cUpdates.add( MeterDevice.makeFilterToPrecision( meterDevice.getPrecision() ) );
									if ( md.getInterval() != meterDevice.getInterval() )
										cUpdates.add( MeterDevice.makeFilterToInterval( meterDevice.getInterval() ) );
									if ( !cUpdates.isEmpty() ) {
										streamOut.write( ( "\nBefore modify meter device " + md.toString() + "\n" ).getBytes() );
										final Bson cUpdate = Filters.and( cUpdates );
										md.update( cQuery, cUpdate );
										streamOut.write( ( "After modify meter device " + meterDevice.toString() + "\n" ).getBytes() );
										LOGGER.trace( "MeterDevice {} {} modified. Count meter devices is {}", md.getId(), md.getName(),
												++updatedCount );
									}
								} else
									LOGGER.trace( "This is the same meter device : {}", meterDevice );
						}
						catch ( final MeterDeviceNotFoundException mnfe ) {}
						catch ( final ImpossibleCreatingException ice ) {}
						meterDeviceSize++ ;
					}
					streamOut.write( ( "\nAll meter devices modified : " + updatedCount ).getBytes() );
					streamOut.write( ( "\nAll meter devices created : " + createdCount ).getBytes() );
					streamOut.write( ( "\nAll meter devices selected : " + meterDeviceSize ).getBytes() );
				}
				catch ( final SQLFeatureNotSupportedException sfnse ) {
					LOGGER.error( "SQLFeatureNotSupportedException in the ResultSet.first(): {}", sfnse );
				}
				catch ( final SQLException sqle ) {
					LOGGER.error( "Query no retrive result. Exception: {}", sqle );
				}
				catch ( final IOException ioe ) {
					LOGGER.error( "Write to stream error. Exception: {}", ioe );
				}
				LOGGER.trace( "MeterDevice created {} document(s). MeterDevice changed {} document(s).", createdCount, updatedCount );
				LOGGER.trace( "Import the MeterDevice from MSSQL server successfull!" );
				response().setContentType( "application/x-download" );
				response().setHeader( "Content-disposition", "attachment; filename=" + METERDEVICES_RESULT );
				return created( streamOut.toByteArray() );
			}
			// Processing UpdateConsumers
			if ( step.isUpdateConsumers() ) {
				// Run process
				// This is temporary variable
				final ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
				final String keyReferences = step.getReferences().get( 0 );
				int consumerSize = 0;
				int createdCount = 0;// Create count Consumers
				int updatedCount = 0;// Update count Consumers
				final String sqlText = readSQLFile( "consumers" );
				boolean isReadSQLFile = sqlText != "";
				if ( isReadSQLFile )
					try {
						streamOut.write( "All consumers that were created or modified written to the file.\n".getBytes() );
						final PreparedStatement statement = CONFIGURATION.getMSSQLConnection().prepareStatement( sqlText );
						int pCount = 0;
						if ( !statement.execute() )
							while ( !statement.getMoreResults() )
								pCount++ ;
						LOGGER.trace( "Count results is {} in select consumers", pCount );
						final ResultSet result = statement.getResultSet();
						final List< UndefinedConsumer > undefinedConsumers = new LinkedList<>();
						final List< Consumer > updateBeforeConsumers = new LinkedList<>();
						final List< Consumer > updateAfterConsumers = new LinkedList<>();
						boolean undefinedConsomerTry = false;
						while ( result.next() ) {
							UndefinedConsumer ndefinedConsomer = null;
							// a.code_abon, w.surname, t.nazva_pos, s.nazva_street, a.house,
							// a.flat, a.indeks, a.privat, sa.code_status, a.unicod, a.doc,
							// w.doc
							// Account ID
							String field = result.getString( 1 ).trim();
							// Create Consumer with consumerId
							Consumer consumer;
							if ( field != null && !field.isEmpty() )
								consumer = Consumer.create( field );
							else
								consumer = null;
							if ( consumer != null ) {
								// Population - standard created
								consumer.setConsumerType( ConsumerType.INDIVIDUAL );
								// Fullname
								field = result.getString( 2 );
								if ( field != null && !field.isEmpty() )
									consumer.setFullName( field );
								else {
									ndefinedConsomer = UndefinedConsumer.create( consumer.getId(), UndefinedConsumerType.CONSUMER_NAME_UNDEFINED,
											field );
									undefinedConsomerTry = true;
								}
								// Is private house
								final boolean isPriv = result.getBoolean( 8 );
								consumer.setHouseType( isPriv ? HouseType.MANSION : HouseType.APARTMENT_HOUSE );
								// Documents
								final String idCode = result.getString( 10 );
								final String passport = result.getString( 11 );
								String passportTmp = result.getString( 12 );
								String pasSeries = null;
								String pasNumber = null;
								if ( passport != null && passport.contains( " " ) && passport.length() > 7 ) {
									final int pPas = passport.indexOf( " " );
									pasSeries = passport.substring( 0, pPas );
									pasNumber = passport.substring( pPas + 1 );
								} else
									if ( passportTmp != null && passportTmp.contains( " " ) && passportTmp.length() > 7 ) {
										final int pPasp = passportTmp.indexOf( " " );
										pasSeries = passportTmp.substring( 0, pPasp );
										pasNumber = passportTmp.substring( pPasp + 1 );
									}
								final boolean passBoll = pasSeries != null && !pasSeries.isEmpty() && pasNumber != null && !pasNumber.isEmpty();
								if ( passBoll )
									if ( pasNumber.length() == 2 ) {
										passportTmp = pasSeries;
										pasSeries = pasNumber;
										pasNumber = passportTmp;
									}
								Documents documents;
								if ( idCode != null && !idCode.isEmpty() && idCode.length() > 8 || passBoll )
									documents = Documents.create( idCode, pasSeries, pasNumber );
								else
									documents = null;
								if ( documents != null )
									consumer.setDocuments( documents );
								// Address
								final Address address = Address.create();
								field = result.getString( 5 );
								if ( field != null && !field.isEmpty() )
									address.setHouse( field.trim() );
								field = result.getString( 6 );
								if ( field != null && !field.isEmpty() )
									address.setApartment( field.trim() );
								// Address Street
								field = result.getString( 4 );
								final int posS = field.indexOf( "." ) + 1;
								final String typeStr = field.substring( 0, posS ).trim();
								final String nameStr = field.substring( posS ).trim();
								final StreetType st = StreetType.abbreviationToStreetType( typeStr );
								try {
									final AddressPlace addr = AddressPlace.find( nameStr, st );
									address.setAddressPlace( addr );
								}
								catch ( final AddressNotFoundException anfe ) {
									// It's Empty LocationType. But write to undefined list
									final StringBuilder sb = new StringBuilder( nameStr );
									sb.append( " *** " );
									sb.append( st.name() );
									if ( !undefinedConsomerTry ) {
										ndefinedConsomer = UndefinedConsumer.create( consumer.getId(), UndefinedConsumerType.ADDRESSPLACE_UNDEFINED,
												sb.toString() );
										undefinedConsomerTry = true;
									} else {
										sb.append( " *** " );
										sb.append( ndefinedConsomer.getError() );
										ndefinedConsomer.setError( sb.toString() );
										ndefinedConsomer.addUndefinedConsumerType( UndefinedConsumerType.ADDRESSPLACE_UNDEFINED );
									}
								}
								// Address City
								field = result.getString( 3 );
								final int pos = field.indexOf( "." ) + 1;
								final String typeCity = field.substring( 0, pos ).trim();
								final String nameCity = field.substring( pos ).trim();
								final LocationType lt = LocationType.abbreviationToLocationType( typeCity );
								if ( lt == null || lt != null && lt.equals( LocationType.UNSPECIFIED ) ) {
									final StringBuilder sb = new StringBuilder();
									if ( lt == null )
										sb.append( "LocationType is null" );
									else
										sb.append( lt.name() );
									// It's Empty LocationType. But write to undefined list
									if ( !undefinedConsomerTry ) {
										ndefinedConsomer = UndefinedConsumer.create( consumer.getId(), UndefinedConsumerType.LOCATIONTYPE_UNDEFINED,
												sb.toString() );
										undefinedConsomerTry = true;
									} else {
										sb.append( " *** " );
										sb.append( ndefinedConsomer.getError() );
										ndefinedConsomer.addUndefinedConsumerType( UndefinedConsumerType.LOCATIONTYPE_UNDEFINED );
										ndefinedConsomer.setError( sb.toString() );
									}
								} else {
									List< AddressLocation > addrLocations = null;
									try {
										addrLocations = AddressLocation.findLikeLocationName( nameCity );
										boolean bool = false;
										int indexK = 0;
										for ( int k = 0; k < addrLocations.size() && !bool; k++ ) {
											final String al = addrLocations.get( k ).getTopAddressId();
											final boolean isAddrTop = keyReferences.equals( al );
											bool = lt.name().equals( addrLocations.get( k ).getLocationType().name() ) && isAddrTop;
											if ( bool ) {
												address.setAddressLocation( addrLocations.get( k ) );
												break;
											} else
												indexK = k;
										}
										if ( !bool ) {
											final StringBuilder sb = new StringBuilder( lt.name() );
											sb.append( " *** " );
											sb.append( addrLocations.get( indexK ).getLocationType().name() );
											if ( !undefinedConsomerTry ) {
												ndefinedConsomer = UndefinedConsumer.create( consumer.getId(),
														UndefinedConsumerType.ADDRESSLOCATION_UNDEFINED, sb.toString() );
												undefinedConsomerTry = true;
											} else {
												sb.append( " *** " );
												sb.append( ndefinedConsomer.getError() );
												ndefinedConsomer.setError( sb.toString() );
												ndefinedConsomer.addUndefinedConsumerType( UndefinedConsumerType.ADDRESSLOCATION_UNDEFINED );
											}
										}
									}
									catch ( final AddressNotFoundException anfe ) {
										final StringBuilder sb = new StringBuilder( nameCity );
										if ( !undefinedConsomerTry ) {
											ndefinedConsomer = UndefinedConsumer.create( consumer.getId(),
													UndefinedConsumerType.ADDRESSLOCATION_UNDEFINED, sb.toString() );
											undefinedConsomerTry = true;
										} else {
											sb.append( " *** " );
											sb.append( ndefinedConsomer.getError() );
											ndefinedConsomer.setError( sb.toString() );
											ndefinedConsomer.addUndefinedConsumerType( UndefinedConsumerType.ADDRESSLOCATION_UNDEFINED );
										}
									}
									catch ( final NumberFormatException nfe ) {
										LOGGER.trace( "This should not happen" );
									}
								}
								// Address postal code
								field = result.getString( 7 );
								if ( field != null && !field.isEmpty() && field.length() > 3 )
									address.setPostalCode( field.trim() );
								consumer.setAddress( address );
								// Consumer code status
								final int status = result.getInt( 9 );
								if ( status > 0 && status < 10 )
									field = Messages.get( CONSUMER_CODE_STATUS_NAME + "." + String.valueOf( status ) );
								else
									field = null;
								if ( field != null )
									consumer.setStatusType( ConsumerStatusType.valueOf( field ) );
								try {
									final Consumer c = Consumer.findById( consumer.getId() );
									final Consumer beforeUpdate = c.copyInstance();
									final Bson cQuery = Consumer.makeFilterToId( c.getId() );
									final List< Bson > cUpdates = new LinkedList< Bson >();
									if ( consumer.getFullName() != null && !consumer.getFullName().equals( c.getFullName() ) )
										cUpdates.add( Consumer.makeFilterToFullName( consumer.getFullName() ) );
									if ( !consumer.getAddress().equals( c.getAddress() ) )
										cUpdates.add( Consumer.makeFilterToAddress( consumer.getAddress() ) );
									if ( consumer.getDocuments() != null && !consumer.getDocuments().equals( c.getDocuments() )
											|| consumer.getDocuments() == null && c.getDocuments() != null )
										cUpdates.add( Consumer.makeFilterToDocuments( consumer.getDocuments() ) );
									if ( !consumer.getConsumerType().equals( c.getConsumerType() ) )
										cUpdates.add( Consumer.makeFilterToConsumerType( consumer.getConsumerType() ) );
									if ( !consumer.getHouseType().equals( c.getHouseType() ) )
										cUpdates.add( Consumer.makeFilterToHouseType( consumer.getHouseType() ) );
									if ( !consumer.getStatusType().equals( c.getStatusType() ) )
										cUpdates.add( Consumer.makeFilterToStatusType( consumer.getStatusType() ) );
									if ( !cUpdates.isEmpty() ) {
										final Bson cUpdate = Filters.and( cUpdates );
										updateBeforeConsumers.add( beforeUpdate );
										consumer.update( cQuery, cUpdate );
										updateAfterConsumers.add( consumer );
										LOGGER.trace( "Consumer {} modified. Modified {} record!", consumer.getId(), ++updatedCount );
										streamOut.write( ( "\nBefore modify consumer " + beforeUpdate.toString() + "\n" ).getBytes() );
										streamOut.write( ( "After  modify consumer " + consumer.toString() + "\n" ).getBytes() );
									}
								}
								catch ( final ConsumerException ce ) {
									consumer.save();
									LOGGER.trace( "Consumer {} created. Writed {} record!", consumer.getId(), ++createdCount );
									streamOut.write( ( "\nCreate consumer " + consumer.toString() + "\n" ).getBytes() );
								}
								if ( undefinedConsomerTry && ndefinedConsomer != null ) {
									ndefinedConsomer.save();
									undefinedConsumers.add( ndefinedConsomer );
									undefinedConsomerTry = false;
									ndefinedConsomer = null;
								}
								consumerSize++ ;
							} else
								LOGGER.trace( "Consumer ID should not be empty! Sorry..." );
						}
						// Finish all Consumers
						result.close();
						LOGGER.trace( "Consumer created {} document(s). Consumer changed {} document(s).", createdCount, updatedCount );
						streamOut.write( ( "\nAll consumers modified : " + updatedCount ).getBytes() );
						streamOut.write( ( "\nAll consumers created : " + createdCount ).getBytes() );
						streamOut.write( ( "\nAll consumers selected : " + consumerSize ).getBytes() );
						LOGGER.trace( "This update before change Consumers is {}", updateBeforeConsumers );
						LOGGER.trace( "This update after change Consumers is {}", updateAfterConsumers );
					}
					catch ( final SQLException sqle ) {
						isReadSQLFile = false;
						LOGGER.trace( "SQLException for PrepareStatement.execute(). {}", sqle );
					}
					catch ( final Exception e ) {
						isReadSQLFile = false;
						LOGGER.trace( "Exception : {}", e );
					}
				if ( isReadSQLFile )
					LOGGER.trace( "Import the consumers from MSSQL server successful!" );
				else
					LOGGER.trace( "Import the consumers from MSSQL server unsuccessful!" );
				response().setContentType( "application/x-download" );
				response().setHeader( "Content-disposition", "attachment; filename=" + CONSUMERS_RESULT );
				return created( streamOut.toByteArray() );
			}
			// Processing UpdateMeters
			if ( step.isUpdateMeters() ) {
				final ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
				int metersCount = 0;
				int updateCount = 0;
				final String sqlText = readSQLFile( "meters" );
				boolean isReadSQLFile = sqlText != "";
				if ( isReadSQLFile )
					try {
						streamOut.write( "All meters that were created or modified written to the file.\n".getBytes() );
						final PreparedStatement statement = CONFIGURATION.getMSSQLConnection().prepareStatement( sqlText );
						int pass = 1;
						final int docs = 1000;
						MongoCursor< Consumer > cursor = Consumer.getMongoCollection().find().skip( ( pass - 1 ) * docs ).limit( docs )
								.iterator();
						while ( cursor != null && cursor.hasNext() ) {
							int pCount = 0;
							while ( cursor.hasNext() ) {
								final Consumer consumer = cursor.next();
								statement.setString( 1, consumer.getId() );
								if ( !statement.execute() )
									while ( !statement.getMoreResults() )
										pCount++ ;
								LOGGER.trace( "Count results is {} in select meters", pCount );
								final ResultSet result = statement.getResultSet();
								while ( result.next() ) {
									// m.nazva_marka, n.nomer, n.razr, n.n_date, n.k_date,
									// i.inspektor, n.doc, n.amp, ma.code_mestoacc, n.date_pov
									// Meter InstallDate
									final Date installDate = result.getDate( 4 );
									Meter meterConsumer;
									try {
										meterConsumer = Meter.findByConsumerIdAndDateInstall( consumer.getId(), installDate.getTime() );
									}
									catch ( final MeterNotFoundException mnfe ) {
										meterConsumer = null;
									}
									// Meter name
									final String meterName = result.getString( 1 );
									final MeterDevice device = MeterDevice.findByName( meterName );
									// Meter place
									final int meterPlaceId = result.getInt( 9 );
									String field;
									if ( meterPlaceId > 0 && meterPlaceId < 7 )
										field = Messages.get( METER_CODE_PLACE_NAME + "." + String.valueOf( meterPlaceId ) );
									else
										field = null;
									LocationMeterType place;
									if ( field == null )
										place = LocationMeterType.OTHER;
									else
										place = LocationMeterType.valueOf( field );
									// Meter Inspector
									String inspector = result.getString( 6 );
									if ( inspector != null && ( inspector.isEmpty() || inspector.equals( "_невідомий" ) ) )
										inspector = null;
									// Meter Number
									String number = result.getString( 2 );
									int p = number.indexOf( "[" );
									if ( p < 0 )
										p = number.indexOf( "{" );
									if ( p < 0 )
										p = number.indexOf( "(" );
									if ( p >= 0 )
										number = number.substring( 0, p - 1 );
									// Meter Digits
									final byte digits = result.getByte( 3 );
									// Meter Order
									field = result.getString( 7 );
									short order;
									try {
										order = Short.parseShort( field );
									}
									catch ( final NumberFormatException nfe ) {
										order = 0;
									}
									Meter meter = null;
									if ( device != null ) {
										meter = Meter.create( consumer.getId(), device, number, digits, installDate.getTime(), order, inspector,
												place );
										final byte amp = result.getByte( 8 );
										if ( amp > 0 )
											meter.setMightOutturn( amp );
										Date date = result.getDate( 5 );
										if ( date.getTime() != Meter.MAXDATE_PAKED.getTime() )
											meter.setDateUninstall( date.getTime() );
										date = result.getDate( 10 );
										if ( date != null )
											meter.setDateTesting( date.getTime() );
									}
									if ( meterConsumer != null ) {
										if ( meterConsumer.equals( meter ) )
											LOGGER.trace( "This is the same meter : {}", meter );
										else {
											final Bson cQuery = Meter.makeFilterToId( meterConsumer.getId() );
											final List< Bson > cUpdates = new LinkedList< Bson >();
											if ( meterConsumer.getOrder() != meter.getOrder() )
												cUpdates.add( Meter.makeFilterToOrder( meter.getOrder() ) );
											if ( meterConsumer.getDateInstall() != meter.getDateInstall() )
												cUpdates.add( Meter.makeFilterToDateInstall( meter.getDateInstall() ) );
											if ( meterConsumer.getDateUninstall() != meter.getDateUninstall() )
												cUpdates.add( Meter.makeFilterToDateUninstall( meter.getDateUninstall() ) );
											if ( meterConsumer.getDateTesting() != meter.getDateTesting() )
												cUpdates.add( Meter.makeFilterToDateTesting( meter.getDateTesting() ) );
											if ( meterConsumer.getNumber() != null && meter.getNumber() != null
													&& !meterConsumer.getNumber().equals( meter.getNumber() ) || meterConsumer.getNumber() == null
													&& meter.getNumber() != null )
												cUpdates.add( Meter.makeFilterToNumber( meter.getNumber() ) );
											if ( meterConsumer.getMightOutturn() != meter.getMightOutturn() )
												cUpdates.add( Meter.makeFilterToMightOutturn( meter.getMightOutturn() ) );
											if ( !meterConsumer.getMeterDeviceId().equals( meter.getMeterDeviceId() ) )
												cUpdates.add( Meter.makeFilterToMeterDeviceId( meter.getMeterDeviceId() ) );
											if ( meterConsumer.getDigits() != meter.getDigits() )
												cUpdates.add( Meter.makeFilterToDigits( meter.getDigits() ) );
											if ( meterConsumer.getMasterName() != null && meter.getMasterName() != null
													&& !meterConsumer.getMasterName().equals( meter.getMasterName() )
													|| meterConsumer.getMasterName() == null && meter.getMasterName() != null )
												cUpdates.add( Meter.makeFilterToMasterName( meter.getMasterName() ) );
											if ( !meterConsumer.getLocationMeter().equals( meter.getLocationMeter() ) )
												cUpdates.add( Meter.makeFilterToLocationType( meter.getLocationMeter() ) );
											if ( !cUpdates.isEmpty() ) {
												streamOut.write( ( "\nBefore modify meter " + meterConsumer.toString() + "\n" ).getBytes() );
												final Bson cUpdate = Filters.and( cUpdates );
												meterConsumer.update( cQuery, cUpdate );
												streamOut.write( ( "After  modify meter " + meterConsumer.toString() + "\n" ).getBytes() );
												LOGGER.trace( "Meter {} modified. Count meters is {}", meterConsumer.getId(), ++updateCount );
											}
										}
									} else
										if ( meter != null ) {
											meter.save();
											LOGGER.warn( "Meter consumer {} saved. Count meters is {}", meter, ++metersCount );
											streamOut.write( ( "\nCreate Meter " + meter.toString() + "\n" ).getBytes() );
										}
								}
								result.close();
							}
							LOGGER.trace( "Number pass is {}.", pass++ );
							cursor.close();
							cursor = Consumer.getMongoCollection().find().skip( ( pass - 1 ) * docs ).limit( docs ).iterator();
						}
						streamOut.write( ( "\nAll meters modified : " + updateCount ).getBytes() );
						streamOut.write( ( "\nAll meters created : " + metersCount ).getBytes() );
					}
					catch ( final SQLException sqle ) {
						isReadSQLFile = false;
						LOGGER.trace( "SQLException for PrepareStatement.execute(). {}", sqle );
					}
					catch ( final Exception e ) {
						isReadSQLFile = false;
						LOGGER.trace( "Exception : {}", e );
					}
				if ( isReadSQLFile )
					LOGGER.trace( "Import the consumers from MSSQL server successful!" );
				else
					LOGGER.trace( "Import the consumers from MSSQL server unsuccessful!" );
				LOGGER.trace( "Meter saved is {}", metersCount );
				LOGGER.trace( "Meter modified is {}", updateCount );
				response().setContentType( "application/x-download" );
				response().setHeader( "Content-disposition", "attachment; filename=" + METERS_RESULT );
				return created( streamOut.toByteArray() );
			}
		}
		return ok( index.render( CONFIGURATION, findAllUsersByAdminAndOperRoles(), filledForm, 2 ) );
	}
	
	private static List< User > findAllUsersByAdminAndOperRoles() {
		try {
			final List< User > users = User.findByRole( UserRole.OPER );
			final List< User > admins = User.findByRole( UserRole.ADMIN );
			for ( final User a : admins ) {
				boolean b = false;
				for ( final User u : users ) {
					b = u.getEmail().equals( a.getEmail() ) || b;
					if ( b )
						break;
				}
				if ( !b )
					users.add( a );
			}
			return users;
		}
		catch ( final UserNotFoundException une ) {
			return null;
		}
	}
	
	/**
	 * @see http://www.java.net/node/667186
	 */
	public static void removeWhitespaceNodes( final Element e ) {
		final NodeList children = e.getChildNodes();
		for ( int i = children.getLength() - 1; i >= 0; i-- ) {
			final Node child = children.item( i );
			if ( child instanceof Text && ( ( Text )child ).getData().trim().length() == 0 )
				e.removeChild( child );
			else
				if ( child instanceof Element )
					removeWhitespaceNodes( ( Element )child );
		}
	}
	
	public static String readSQLFile( final String fileName ) {
		final File sqlFile = CONFIGURATION.getSQLFileByName( fileName );
		final int size = ( int )sqlFile.length();
		int sis = 0;
		final byte b[] = new byte[ size + 1 ];
		FileInputStream fis = null;
		String strProc = "";
		try {
			fis = new FileInputStream( sqlFile );
			sis = fis.read( b );
			if ( sis != size )
				LOGGER.error( "Error read the sqlFile {}", sqlFile );
			else
				strProc = new String( b );
		}
		catch ( final FileNotFoundException fnfe ) {
			LOGGER.trace( "FileNotFoundException in create FileInputStream( {} )", sqlFile );
		}
		catch ( final IOException ioe ) {
			LOGGER.trace( "IQException for FileInputStream.read( byte[] ) in readSQLFile( {} ). {}", fileName, ioe );
		}
		finally {
			if ( fis != null )
				try {
					fis.close();
				}
				catch ( final IOException ioe ) {
					LOGGER.trace( "IQException for FileInputStream.close() in readSQLFile( {} ). {}", fileName, ioe );
				}
		}
		return strProc;
	}
}
