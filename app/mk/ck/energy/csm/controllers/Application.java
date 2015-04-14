package mk.ck.energy.csm.controllers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import mk.ck.energy.csm.model.Configuration;
import mk.ck.energy.csm.model.Database;
import mk.ck.energy.csm.model.Employee;
import mk.ck.energy.csm.model.auth.User;
import mk.ck.energy.csm.model.auth.UserRole;
import mk.ck.energy.csm.providers.MyUsernamePasswordAuthProvider;
import mk.ck.energy.csm.providers.MyUsernamePasswordAuthProvider.MyLogin;
import mk.ck.energy.csm.providers.MyUsernamePasswordAuthProvider.MySignup;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import play.Routes;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.about;
import views.html.aboutStaft;
import views.html.feedback;
import views.html.index;
import views.html.login;
import views.html.officialHome;
import views.html.phoneBook;
import views.html.profile;
import views.html.rates;
import views.html.restricted;
import views.html.signup;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;

public class Application extends Controller {
	
	private static final Logger	LOGGER						= LoggerFactory.getLogger( Application.class );
	
	public static final String	FLASH_MESSAGE_KEY	= "message";
	
	public static final String	FLASH_ERROR_KEY		= "error";
	
	public static Result index() {
		return ok( index.render() );
	}
	
	public static Result about() {
		return ok( about.render() );
	}
	
	public static Result feedback() {
		return ok( feedback.render() );
	}
	
	public static Result officialHome() {
		return ok( officialHome.render() );
	}
	
	public static Result phoneBook() {
		final List< Employee > employees = importEmployees();
		final List< Employee > tmpDelete = new ArrayList<>( employees.size() );
		for ( final Employee emp : employees )
			if ( !emp.isToPhoneBook() )
				tmpDelete.add( emp );
		employees.removeAll( tmpDelete );
		return ok( phoneBook.render( scala.collection.JavaConversions.asScalaBuffer( employees ) ) );
	}
	
	public static Result rates() {
		return ok( rates.render() );
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result listAboutStaft() {
		return ok( aboutStaft.render() );
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result doListAboutStaft() {
		final List< Employee > employees = importEmployees();
		return exportEmployeesToExcel( employees );
	}
	
	@SubjectPresent
	public static Result restricted() {
		final User localUser = User.getLocalUser( session() );
		return ok( restricted.render( localUser ) );
	}
	
	@SubjectPresent
	public static Result profile() {
		final User localUser = User.getLocalUser( session() );
		return ok( profile.render( localUser ) );
	}
	
	public static Result login() {
		return ok( login.render( MyUsernamePasswordAuthProvider.LOGIN_FORM ) );
	}
	
	public static Result doLogin() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< MyLogin > filledForm = MyUsernamePasswordAuthProvider.LOGIN_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not fill everything properly
			return badRequest( login.render( filledForm ) );
		else
			// Everything was filled
			return UsernamePasswordAuthProvider.handleLogin( ctx() );
	}
	
	public static Result signup() {
		return ok( signup.render( MyUsernamePasswordAuthProvider.SIGNUP_FORM ) );
	}
	
	public static Result jsRoutes() {
		return ok( Routes.javascriptRouter( "jsRoutes", //
				routes.javascript.Signup.forgotPassword(), //
				routes.javascript.Account.onChangeAddressTopSelect() //
				) ).as( "text/javascript" );
	}
	
	public static Result doSignup() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< MySignup > filledForm = MyUsernamePasswordAuthProvider.SIGNUP_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not fill everything properly
			return badRequest( signup.render( filledForm ) );
		else
			// Everything was filled
			// do something with your part of the form before handling the user
			// signup
			return UsernamePasswordAuthProvider.handleSignup( ctx() );
	}
	
	public static String formatTimestamp( final long t ) {
		return new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( new Date( t ) );
	}
	
	public static List< Employee > importEmployees() {
		final List< Employee > employees = new LinkedList<>();
		final File xmlEmployee = Database.getConfiguration().getEmployeesFileXML();
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware( true );
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			final Document document = builder.parse( xmlEmployee );
			final XPath xPath = XPathFactory.newInstance().newXPath();
			final String expression = "/global/staff/employee";
			// Get list nodes Emloyees
			final NodeList nodeList = ( NodeList )xPath.compile( expression ).evaluate( document, XPathConstants.NODESET );
			for ( int i = 0; i < nodeList.getLength() - 1; i++ ) {
				final Employee employee = new Employee();
				// Get node
				final Node node = nodeList.item( i );
				if ( node != null ) {
					final NodeList properties = node.getChildNodes();
					for ( int j = 0; properties != null && j < properties.getLength(); j++ ) {
						final Node property = properties.item( j );
						if ( property.getNodeType() == Node.ELEMENT_NODE ) {
							final String value = property.getTextContent();
							final String element = property.getNodeName();
							switch ( element ) {
								case "rem_id" :
									employee.setRemId( value );
									break;
								case "fullname" :
									employee.setFullname( value );
									break;
								case "business_phone" :
									employee.setPhoneBusiness( value );
									break;
								case "home_phone" :
									employee.setPhoneHome( value );
									break;
								case "mobile_phone" :
									employee.setPhoneMobile( value );
									break;
								case "position" :
									employee.setPosition( value );
									break;
								case "birthday" :
									employee.setBirthday( value );
									break;
								case "employment_day" :
									employee.setEmploymentDay( value );
									break;
								case "to_phone_book" :
									employee.setToPhoneBook( value.equals( "1" ) || value.equalsIgnoreCase( "true" )
											|| value.equalsIgnoreCase( "yes" ) ? true : false );
									break;
							}
						}
					}
				}
				employees.add( employee );
			}
		}
		catch ( final FileNotFoundException fnfe ) {
			LOGGER.error( "File not found exception {}", fnfe );
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
		catch ( final XPathExpressionException xpe ) {
			LOGGER.error( "Error parPath Expression {}", xpe );
		}
		return employees;
	}
	
	public static Result exportEmployeesToExcel( final List< Employee > employees ) {
		final ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		try {
			final Workbook wb = new HSSFWorkbook(); // or new XSSFWorkbook();
			final Sheet sheet = wb.createSheet( "1" );
			sheet.setMargin( Sheet.LeftMargin, 0.1976 );
			sheet.setMargin( Sheet.TopMargin, 0.395257 );
			sheet.setMargin( Sheet.RightMargin, 0.1976 );
			sheet.setMargin( Sheet.BottomMargin, 0.1976 );
			// The Title page
			short rowNum = 2;
			final Row rowTitle = sheet.createRow( rowNum );
			rowTitle.setHeightInPoints( 30 );
			final CellStyle styleTitle = wb.createCellStyle();
			styleTitle.setAlignment( CellStyle.ALIGN_CENTER );
			styleTitle.setVerticalAlignment( CellStyle.VERTICAL_CENTER );
			final Font fontTitle = wb.createFont();
			fontTitle.setBold( true );
			fontTitle.setFontHeightInPoints( ( short )22 );
			styleTitle.setFont( fontTitle );
			final CellRangeAddress regionTitle = new CellRangeAddress( rowNum, rowNum, 0, 12 );
			sheet.addMergedRegion( regionTitle );
			createCell( wb, rowTitle, ( short )0, styleTitle, Messages.get( "page.staff.about.title" ) );
			// The Header row cells
			rowNum += 2;
			final Row rowHeader = sheet.createRow( rowNum );
			rowHeader.setHeightInPoints( 15 );
			final Row rowHeader1 = sheet.createRow( rowNum + 1 );
			rowHeader1.setHeightInPoints( 15 );
			final CellStyle styleHeader = wb.createCellStyle();
			styleHeader.setAlignment( CellStyle.ALIGN_CENTER );
			styleHeader.setVerticalAlignment( CellStyle.VERTICAL_CENTER );
			// The Header OrderNum column cell
			short colNum = 0;
			final CellRangeAddress regionHeader00 = new CellRangeAddress( rowNum, rowNum + 1, colNum, colNum );
			sheet.addMergedRegion( regionHeader00 );
			sheet.setColumnWidth( colNum, 5 * 256 ); // width = 4.29
			final Font fontHeader = wb.createFont();
			fontHeader.setBold( true );
			fontHeader.setFontHeightInPoints( ( short )11 );
			styleHeader.setFont( fontHeader );
			styleHeader.setWrapText( true );
			styleHeader.setBorderBottom( CellStyle.BORDER_THIN );
			styleHeader.setBorderLeft( CellStyle.BORDER_THIN );
			styleHeader.setBorderTop( CellStyle.BORDER_THIN );
			styleHeader.setBorderRight( CellStyle.BORDER_THIN );
			styleHeader.setFillForegroundColor( IndexedColors.GREY_25_PERCENT.getIndex() );
			styleHeader.setFillPattern( CellStyle.SOLID_FOREGROUND );
			final Cell cell0 = rowHeader1.createCell( colNum );
			cell0.setCellStyle( styleHeader );
			createCell( wb, rowHeader, colNum, styleHeader, Messages.get( "page.staff.about.number" ) );
			// The Header Account column cell
			sheet.setColumnWidth( ++colNum, ( int )( 11.5 * 256 ) ); // width = 10.71
			final CellRangeAddress regionHeader01 = new CellRangeAddress( rowNum, rowNum + 1, colNum, colNum );
			sheet.addMergedRegion( regionHeader01 );
			final Cell cell01 = rowHeader1.createCell( colNum );
			cell01.setCellStyle( styleHeader );
			createCell( wb, rowHeader, colNum, styleHeader, Messages.get( "page.staff.about.account" ) );
			// The Header FLN column cell
			sheet.setColumnWidth( ++colNum, 41 * 256 ); // width = 40.29
			final CellRangeAddress regionHeader02 = new CellRangeAddress( rowNum, rowNum + 1, colNum, colNum );
			sheet.addMergedRegion( regionHeader02 );
			final Cell cell02 = rowHeader1.createCell( colNum );
			cell02.setCellStyle( styleHeader );
			createCell( wb, rowHeader, colNum, styleHeader, Messages.get( "page.staff.about.fnl" ) );
			// The Header Address column cell
			sheet.setColumnWidth( ++colNum, 55 * 256 ); // width = 54.29
			final CellRangeAddress regionHeader03 = new CellRangeAddress( rowNum, rowNum + 1, colNum, colNum );
			sheet.addMergedRegion( regionHeader03 );
			final Cell cell03 = rowHeader1.createCell( colNum );
			cell03.setCellStyle( styleHeader );
			createCell( wb, rowHeader, colNum, styleHeader, Messages.get( "page.staff.about.address" ) );
			// The Header Position column cell
			sheet.setColumnWidth( ++colNum, ( int )( 21.5 * 256 ) ); // width = 20.71;
			// 42.75 heigth when zam
			final CellRangeAddress regionHeader04 = new CellRangeAddress( rowNum, rowNum + 1, colNum, colNum );
			sheet.addMergedRegion( regionHeader04 );
			final Cell cell04 = rowHeader1.createCell( colNum );
			cell04.setCellStyle( styleHeader );
			createCell( wb, rowHeader, colNum, styleHeader, Messages.get( "page.staff.about.position" ) );
			// The Header Phones column cell
			sheet.setColumnWidth( ++colNum, 11 * 256 ); // width = 10.29
			final CellRangeAddress regionHeader05 = new CellRangeAddress( rowNum, rowNum, colNum, colNum + 1 );
			sheet.addMergedRegion( regionHeader05 );
			createCell( wb, rowHeader, colNum, styleHeader, Messages.get( "page.staff.about.phones" ) );
			createCell( wb, rowHeader1, colNum, styleHeader, Messages.get( "page.staff.about.phone.home" ) );
			sheet.setColumnWidth( ++colNum, ( int )( 12.8 * 256 ) ); // width = 12.14
			final Cell cell05 = rowHeader.createCell( colNum );
			cell05.setCellStyle( styleHeader );
			createCell( wb, rowHeader1, colNum, styleHeader, Messages.get( "page.staff.about.phone.mobile" ) );
			// The Header Counter column cell
			sheet.setColumnWidth( ++colNum, ( int )( 17.5 * 256 ) ); // width = 16.71
			final CellRangeAddress regionHeader06 = new CellRangeAddress( rowNum, rowNum, colNum, colNum + 3 );
			sheet.addMergedRegion( regionHeader06 );
			createCell( wb, rowHeader, colNum, styleHeader, Messages.get( "page.staff.about.counter" ) );
			createCell( wb, rowHeader1, colNum, styleHeader, Messages.get( "page.staff.about.counter.name" ) );
			sheet.setColumnWidth( ++colNum, ( int )( 14.5 * 256 ) ); // width = 13.71
			final Cell cell06 = rowHeader.createCell( colNum );
			cell06.setCellStyle( styleHeader );
			createCell( wb, rowHeader1, colNum, styleHeader, Messages.get( "page.staff.about.counter.number" ) );
			sheet.setColumnWidth( ++colNum, ( int )( 3.3 * 256 ) ); // width = 2.57
			final Cell cell07 = rowHeader.createCell( colNum );
			cell07.setCellStyle( styleHeader );
			createCell( wb, rowHeader1, colNum, styleHeader, Messages.get( "page.staff.about.counter.voltage" ) );
			sheet.setColumnWidth( ++colNum, 5 * 256 ); // width = 4.29
			final Cell cell08 = rowHeader.createCell( colNum );
			cell08.setCellStyle( styleHeader );
			createCell( wb, rowHeader1, colNum, styleHeader, Messages.get( "page.staff.about.counter.current" ) );
			// The Header Stamps column cell
			sheet.setColumnWidth( ++colNum, 17 * 256 ); // width = 16.29
			final CellRangeAddress regionHeader07 = new CellRangeAddress( rowNum, rowNum + 1, colNum, colNum );
			sheet.addMergedRegion( regionHeader07 );
			final Cell cell09 = rowHeader1.createCell( colNum );
			cell09.setCellStyle( styleHeader );
			createCell( wb, rowHeader, colNum, styleHeader, Messages.get( "page.staff.about.stamps" ) );
			// The Header LatterFigure column cell
			sheet.setColumnWidth( ++colNum, ( int )( 10.3 * 256 ) ); // width = 9.57
			final CellRangeAddress regionHeader08 = new CellRangeAddress( rowNum, rowNum + 1, colNum, colNum );
			sheet.addMergedRegion( regionHeader08 );
			final Cell cell10 = rowHeader1.createCell( colNum );
			cell10.setCellStyle( styleHeader );
			createCell( wb, rowHeader, colNum, styleHeader, Messages.get( "page.staff.about.latterFigure" ) );
			// The data read
			final Configuration config = Database.getConfiguration();
			final Connection connection = config.getMSSQLConnection();
			short val;
			try {
				final String selectAddress = "select t.nazva_pos, s.nazva_street, case when a.house is null "
						+ "then '' when rtrim( a.house ) = '' then '' when rtrim( a.house ) = '0' then '' else "
						+ "ltrim( rtrim( a.house ) ) end + case when a.flat is null then '' when rtrim( a.flat ) = '' "
						+ "then '' when rtrim( a.flat ) = '0' then '' else ', кв. ' + ltrim( a.flat ) end from _abonent a "
						+ "left join _sl_respos t on t.code_pos = a.code_pos "
						+ "left join _sl_streets s on s.code_street = a.code_street where a.code_abon = ?";
				final String selectMeter = "select m.nazva_marka, n.nomer, m.fazi, n.amp, n.plomba from "
						+ "( select distinct tn.code_ab from _tonastr tn where tn.code_to > 0 )	_n left join _abonent a "
						+ "on a.code_ab = _n.code_ab left join _accnastr n on n.code_ab = _n.code_ab and n.k_date =	'20490101' "
						+ "left join sl_marka_acc m on m.code_marka = n.code_marka where a.code_abon = ?";
				final String selectLatterFigure = "select k.pokaz, n.razr from _acckontr k left join _accnastr n on "
						+ "n.code_acc = k.code_acc left join _tonastr t on t.code_ab = n.code_ab where n.k_date = '20490101' "
						+ "and t.code_to = n.code_to and t.code_status != 2 and k.status = 1 and k.date_r = "
						+ "( select max( date_r ) from _acckontr where code_acc = k.code_acc and status = 1 ) and "
						+ "n.code_ab = ( select a.code_ab from _abonent a where a.code_abon = ? )";
				final PreparedStatement statementAddress = connection.prepareStatement( selectAddress );
				final PreparedStatement statementMeter = connection.prepareStatement( selectMeter );
				final PreparedStatement statementLatterFigure = connection.prepareStatement( selectLatterFigure );
				for ( final Employee employee : employees ) {
					final String remId = employee.getRemId();
					LOGGER.trace( "Rem ID is {}:", remId );
					if ( !( remId == "" || remId.isEmpty() ) ) {
						statementAddress.setString( 1, remId );
						final ResultSet resultAddress = statementAddress.executeQuery();
						String s;
						if ( resultAddress.next() ) {
							// The city name
							s = resultAddress.getString( 1 ).trim();
							final StringBuffer addrBuf = new StringBuffer();
							if ( s != "" ) {
								addrBuf.append( s );
								addrBuf.append( ", " );
							}
							// The street name
							s = resultAddress.getString( 2 ).trim();
							if ( s != "" ) {
								addrBuf.append( s );
								addrBuf.append( ", " );
							}
							// The house and appartment number
							s = resultAddress.getString( 3 ).trim();
							if ( s != "" )
								addrBuf.append( s );
							employee.setAddress( addrBuf.toString() );
						}
						statementMeter.setString( 1, remId );
						final ResultSet resultMeter = statementMeter.executeQuery();
						if ( resultMeter.next() ) {
							// The meter name
							s = resultMeter.getString( 1 ).trim();
							employee.setMeterName( s );
							// The meter number
							s = resultMeter.getString( 2 ).trim();
							employee.setMeterNumber( s );
							// The meter voltage
							val = resultMeter.getShort( 3 );
							if ( val == 3 )
								employee.setMeterPhase( ( byte )val );
							else
								employee.setMeterPhase( ( byte )1 );
							// The meter current
							val = resultMeter.getShort( 4 );
							employee.setMeterCurrent( val );
							// The meter plumbs
							s = resultMeter.getString( 5 );
							employee.setMeterPlumb( s );
						}
						statementLatterFigure.setString( 1, remId );
						final ResultSet resultLatterFigure = statementLatterFigure.executeQuery();
						if ( resultLatterFigure.next() ) {
							// The latter figure meter
							final int v = resultLatterFigure.getInt( 1 );
							final StringBuffer vS = new StringBuffer( String.valueOf( v ) );
							s = vS.toString();
							final byte cn = resultLatterFigure.getByte( 2 );
							for ( byte i = cn; i > s.length(); i-- )
								vS.insert( 0, '0' );
							employee.setLatterFigure( vS.toString() );
						}
						resultAddress.close();
						resultMeter.close();
						resultLatterFigure.close();
					}
				}
				statementAddress.close();
				statementMeter.close();
				statementLatterFigure.close();
			}
			catch ( final SQLFeatureNotSupportedException sfnse ) {
				LOGGER.error( "SQLFeatureNotSupportedException in the ResultSet.first(): {}", sfnse );
			}
			catch ( final SQLException sqle ) {
				LOGGER.error( "Query no retrive result. Exception: {}", sqle );
			}
			catch ( final NumberFormatException nfe ) {
				LOGGER.error( "NumberFormatException in parse String. Exception: {}", nfe );
			}
			catch ( final IndexOutOfBoundsException ibe ) {
				LOGGER.error( "IndexOutOfBoundsException for insert to StringBuffer. Exception: {}", ibe );
			}
			// Start export Employee to Excel
			// The Header row cells
			rowNum += 2;
			final CellStyle styleLeft = wb.createCellStyle();
			styleLeft.setAlignment( CellStyle.ALIGN_LEFT );
			styleLeft.setVerticalAlignment( CellStyle.VERTICAL_CENTER );
			final CellStyle styleCenter = wb.createCellStyle();
			styleCenter.setAlignment( CellStyle.ALIGN_CENTER );
			styleCenter.setVerticalAlignment( CellStyle.VERTICAL_CENTER );
			final Font fontCurrent = wb.createFont();
			fontCurrent.setFontHeightInPoints( ( short )11 );
			styleLeft.setFont( fontCurrent );
			styleLeft.setWrapText( true );
			styleLeft.setBorderBottom( CellStyle.BORDER_THIN );
			styleLeft.setBorderLeft( CellStyle.BORDER_THIN );
			styleLeft.setBorderTop( CellStyle.BORDER_THIN );
			styleLeft.setBorderRight( CellStyle.BORDER_THIN );
			styleCenter.setFont( fontCurrent );
			styleCenter.setWrapText( true );
			styleCenter.setBorderBottom( CellStyle.BORDER_THIN );
			styleCenter.setBorderLeft( CellStyle.BORDER_THIN );
			styleCenter.setBorderTop( CellStyle.BORDER_THIN );
			styleCenter.setBorderRight( CellStyle.BORDER_THIN );
			// The Cycl for Order Num to Latter Figure
			short numPP = 1;
			for ( final Employee employee : employees ) {
				colNum = 0;
				final Row rowCurrent = sheet.createRow( rowNum );
				rowCurrent.setHeightInPoints( 15 );
				// Number of
				final Cell cellE00 = rowCurrent.createCell( colNum );
				cellE00.setCellStyle( styleCenter );
				createCell( wb, rowCurrent, colNum++ , styleCenter, String.valueOf( numPP++ ) );
				// Rem ID
				String sE = employee.getRemId();
				final Cell cellE01 = rowCurrent.createCell( colNum );
				cellE01.setCellStyle( styleLeft );
				createCell( wb, rowCurrent, colNum++ , styleLeft, sE );
				// Fullname
				sE = employee.getFullname();
				final Cell cellE02 = rowCurrent.createCell( colNum );
				cellE02.setCellStyle( styleLeft );
				createCell( wb, rowCurrent, colNum++ , styleLeft, sE );
				// Address
				sE = employee.getAddress();
				final Cell cellE03 = rowCurrent.createCell( colNum );
				cellE03.setCellStyle( styleLeft );
				createCell( wb, rowCurrent, colNum++ , styleLeft, sE );
				// Position
				sE = employee.getPosition();
				final Cell cellE04 = rowCurrent.createCell( colNum );
				cellE04.setCellStyle( styleLeft );
				createCell( wb, rowCurrent, colNum++ , styleLeft, sE );
				// Phones data
				sE = employee.getPhoneHome();
				final Cell cellE05 = rowCurrent.createCell( colNum );
				cellE05.setCellStyle( styleCenter );
				createCell( wb, rowCurrent, colNum++ , styleCenter, sE );
				sE = employee.getPhoneMobile();
				final Cell cellE06 = rowCurrent.createCell( colNum );
				cellE06.setCellStyle( styleCenter );
				createCell( wb, rowCurrent, colNum++ , styleCenter, sE );
				// Meters data
				sE = employee.getMeterName();
				final Cell cellE07 = rowCurrent.createCell( colNum );
				cellE07.setCellStyle( styleLeft );
				createCell( wb, rowCurrent, colNum++ , styleLeft, sE );
				sE = employee.getMeterNumber();
				final Cell cellE08 = rowCurrent.createCell( colNum );
				cellE08.setCellStyle( styleLeft );
				createCell( wb, rowCurrent, colNum++ , styleLeft, sE );
				val = employee.getMeterPhase();
				final Cell cellE09 = rowCurrent.createCell( colNum );
				cellE09.setCellStyle( styleCenter );
				createCell( wb, rowCurrent, colNum++ , styleCenter, String.valueOf( val ) );
				val = employee.getMeterCurrent();
				final Cell cellE10 = rowCurrent.createCell( colNum );
				cellE10.setCellStyle( styleCenter );
				createCell( wb, rowCurrent, colNum++ , styleCenter, String.valueOf( val ) );
				// Plumbs
				sE = employee.getMeterPlumb();
				final Cell cellE11 = rowCurrent.createCell( colNum );
				cellE11.setCellStyle( styleCenter );
				createCell( wb, rowCurrent, colNum++ , styleCenter, sE );
				// Latter figure
				sE = employee.getLatterFigure();
				final Cell cellE12 = rowCurrent.createCell( colNum );
				cellE12.setCellStyle( styleCenter );
				createCell( wb, rowCurrent, colNum++ , styleCenter, sE );
				rowNum++ ;
			}
			// The zoom view sheet
			sheet.setZoom( 5, 6 );
			// 1 page to width - 1 page to height
			sheet.setFitToPage( true );
			// The page setup for print
			final PrintSetup ps = sheet.getPrintSetup();
			ps.setPaperSize( PrintSetup.A4_PAPERSIZE );
			ps.setLandscape( true );
			sheet.setAutobreaks( true );
			ps.setFitHeight( ( short )2 );
			ps.setFitWidth( ( short )1 );
			// Write the output to a file
			wb.write( streamOut );
		}
		catch ( final IOException ioe ) {
			LOGGER.error( "An I/O error occurs. {}", ioe );
		}
		catch ( final UnsupportedOperationException uoe ) {
			LOGGER.error( "The current platform does not support the Desktop.Action.OPEN action" );
		}
		response().setContentType( "application/x-download" );
		response().setHeader( "Content-disposition", "attachment; filename=" + Employee.XLS_FILENAME_OUTPUT );
		return created( streamOut.toByteArray() );
	}
	
	private static void createCell( final Workbook wb, final Row row, final short column, final CellStyle cellStyle,
			final String textOutput ) {
		final CreationHelper ch = wb.getCreationHelper();
		final Cell cell = row.createCell( column );
		cell.setCellValue( ch.createRichTextString( textOutput ) );
		cell.setCellStyle( cellStyle );
	}
}