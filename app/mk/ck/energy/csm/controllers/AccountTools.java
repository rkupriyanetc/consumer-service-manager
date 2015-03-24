package mk.ck.energy.csm.controllers;

import static play.data.Form.form;

import java.util.ArrayList;
import java.util.List;

import mk.ck.energy.csm.models.AddressLocation;
import mk.ck.energy.csm.models.AddressNotFoundException;
import mk.ck.energy.csm.models.AddressPlace;
import mk.ck.energy.csm.models.AddressTop;
import mk.ck.energy.csm.models.ImpossibleCreatingException;
import mk.ck.energy.csm.models.LocationType;
import mk.ck.energy.csm.models.StreetType;
import mk.ck.energy.csm.models.auth.UserRole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.addressLocation;
import views.html.addressPlace;
import views.html.addressTop;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class AccountTools extends Controller {
	
	private static final Logger	LOGGER	= LoggerFactory.getLogger( AccountTools.class );
	
	public static class AddrTop {
		
		@Required
		private String	name;
		
		@Required
		private long		refId;
		
		@Required
		private long		id;
		
		public AddrTop() {
			id = 0;
		}
		
		public long getId() {
			return id;
		}
		
		public void setId( final long id ) {
			this.id = id;
		}
		
		public long getRefId() {
			return refId;
		}
		
		public void setRefId( final long refId ) {
			this.refId = refId;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName( final String name ) {
			this.name = name;
		}
	}
	
	public static class AddrLocation {
		
		@Required
		private long						id;
		
		@Required
		private long						refId;
		
		@Required
		private String					location;
		
		@Required
		private List< String >	locationsTypes;
		
		public AddrLocation() {
			id = 0;
			refId = 0;
			locationsTypes = new ArrayList< String >( 0 );
		}
		
		public long getId() {
			return id;
		}
		
		public void setId( final long id ) {
			this.id = id;
		}
		
		public String getLocation() {
			return location;
		}
		
		public void setLocation( final String location ) {
			this.location = location;
		}
		
		public List< String > getLocationsTypes() {
			return locationsTypes;
		}
		
		public void setLocationsTypes( final List< String > locationsTypes ) {
			this.locationsTypes = locationsTypes;
		}
		
		public long getRefId() {
			return refId;
		}
		
		public void setRefId( final long refId ) {
			this.refId = refId;
		}
	}
	
	public static class AddrPlace {
		
		@Required
		private long		id;
		
		@Required
		private String	streetType;
		
		@Required
		private String	street;
		
		public AddrPlace() {
			id = 0;
		}
		
		public long getId() {
			return id;
		}
		
		public void setId( final long id ) {
			this.id = id;
		}
		
		public String getStreet() {
			return street;
		}
		
		public void setStreet( final String street ) {
			this.street = street;
		}
		
		public String getStreetType() {
			return streetType;
		}
		
		public void setStreetType( final String streetType ) {
			this.streetType = streetType;
		}
	}
	
	public static class CountRows {
		
		private int	countRows;
		
		public CountRows() {
			countRows = 15;
		}
		
		public int getCountRows() {
			return countRows;
		}
		
		public void setCountRows( final int countRows ) {
			this.countRows = countRows;
		}
	}
	
	private static final Form< AddrTop >			ADDRTOP_FORM			= form( AddrTop.class );
	
	private static final Form< AddrLocation >	ADDRLOCATION_FORM	= form( AddrLocation.class );
	
	private static final Form< AddrPlace >		ADDRPLACE_FORM		= form( AddrPlace.class );
	
	private static final Form< CountRows >		COUNTROWS_FORM		= form( CountRows.class );
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result testTopAddress() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		return ok( addressTop.render(
				ADDRTOP_FORM,
				scala.collection.JavaConversions.asScalaBuffer( AddressTop.asClassType( AddressTop.getAddressCollection().find()
						.sort( new BasicDBObject( "_id", 1 ) ).toArray() ) ) ) );
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result doTestTopAddress() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< AddrTop > filledForm = ADDRTOP_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			return badRequest( addressTop.render(
					filledForm,
					scala.collection.JavaConversions.asScalaBuffer( AddressTop.asClassType( AddressTop.getAddressCollection().find()
							.toArray() ) ) ) );
		else {
			final AddrTop u = filledForm.get();
			final AddressTop at = AddressTop.create( u.getName(), u.getRefId() );
			filledForm.data().put( "id", String.valueOf( at.getId() ) );
			LOGGER.info( "Address top saved {}", at );
			final List< AddressTop > items = AddressTop.asClassType( AddressTop.getAddressCollection().find()
					.sort( new BasicDBObject( "_id", 1 ) ).toArray() );
			return ok( addressTop.render( filledForm, scala.collection.JavaConversions.asScalaBuffer( items ) ) );
		}
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result testLocationAddress() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		return ok( addressLocation.render(
				ADDRLOCATION_FORM,
				scala.collection.JavaConversions.asScalaBuffer( AddressLocation.asClassType( AddressLocation.getAddressCollection()
						.find().sort( new BasicDBObject( "_id", 1 ) ).toArray() ) ) ) );
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result doTestLocationAddress() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< AddrLocation > filledForm = ADDRLOCATION_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			return badRequest( addressLocation.render(
					filledForm,
					scala.collection.JavaConversions.asScalaBuffer( AddressLocation.asClassType( AddressLocation.getAddressCollection()
							.find().toArray() ) ) ) );
		else {
			final AddrLocation u = filledForm.get();
			final List< LocationType > slt = new ArrayList< LocationType >( 0 );
			try {
				for ( final String i : u.getLocationsTypes() )
					slt.add( LocationType.valueOf( i ) );
			}
			catch ( final NumberFormatException nfe ) {
				LOGGER.error( "Error convertation StreetType of {}", u.getLocationsTypes() );
			}
			try {
				final AddressTop at = AddressTop.findById( u.getRefId() );
				final AddressLocation al = AddressLocation.create( at, u.getLocation(), slt );
				al.save();
				filledForm.data().put( "id", String.valueOf( al.getId() ) );
				LOGGER.info( "Address location saved {}", al );
			}
			catch ( final AddressNotFoundException anfe ) {
				LOGGER.error( "Address Not Found in doTestLocationAddress() method!" );
				filledForm.reject( "Address Not Found in doTestLocationAddress() method!" );
				return badRequest( addressLocation.render(
						filledForm,
						scala.collection.JavaConversions.asScalaBuffer( AddressLocation.asClassType( AddressLocation.getAddressCollection()
								.find().toArray() ) ) ) );
			}
			catch ( final ImpossibleCreatingException ice ) {
				LOGGER.error( "Impossible to duplicate CAPITAL type" );
				filledForm.reject( "Impossible to duplicate CAPITAL type" );
				return badRequest( addressLocation.render(
						filledForm,
						scala.collection.JavaConversions.asScalaBuffer( AddressLocation.asClassType( AddressLocation.getAddressCollection()
								.find().toArray() ) ) ) );
			}
			final List< AddressLocation > items = AddressLocation.asClassType( AddressLocation.getAddressCollection().find()
					.sort( new BasicDBObject( "_id", 1 ) ).toArray() );
			return ok( addressLocation.render( filledForm, scala.collection.JavaConversions.asScalaBuffer( items ) ) );
		}
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result testPlaceAddress() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		return ok( addressPlace.render(
				ADDRPLACE_FORM,
				scala.collection.JavaConversions.asScalaBuffer( AddressPlace.getAddressCollection().find()
						.sort( new BasicDBObject( "_id", 1 ) ).toArray() ) ) );
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result doTestPlaceAddress() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< AddrPlace > filledForm = ADDRPLACE_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			return badRequest( addressPlace.render( filledForm,
					scala.collection.JavaConversions.asScalaBuffer( AddressPlace.getAddressCollection().find().toArray() ) ) );
		else {
			final AddrPlace u = filledForm.get();
			final AddressPlace at = AddressPlace.create( StreetType.valueOf( u.getStreetType() ), u.getStreet() );
			at.save();
			filledForm.data().put( "id", String.valueOf( at.getId() ) );
			LOGGER.info( "Address place saved {}", at );
			final List< DBObject > items = AddressPlace.getAddressCollection().find().sort( new BasicDBObject( "_id", 1 ) ).toArray();
			return ok( addressPlace.render( filledForm, scala.collection.JavaConversions.asScalaBuffer( items ) ) );
		}
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result removeTopAddress( final long id ) {
		try {
			AddressTop.remove( AddressTop.findById( id ) );
			return ok( addressTop.render(
					ADDRTOP_FORM,
					scala.collection.JavaConversions.asScalaBuffer( AddressTop.asClassType( AddressTop.getAddressCollection().find()
							.sort( new BasicDBObject( "_id", 1 ) ).toArray() ) ) ) );
		}
		catch ( final Exception e ) {
			flash( Application.FLASH_MESSAGE_KEY, e.getMessage() );
			final Form< AddrTop > filledForm = ADDRTOP_FORM.bindFromRequest();
			filledForm.reject( e.getMessage() );
			return badRequest( addressTop.render(
					filledForm,
					scala.collection.JavaConversions.asScalaBuffer( AddressTop.asClassType( AddressTop.getAddressCollection().find()
							.toArray() ) ) ) );
		}
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result removeLocationAddress( final long id ) {
		try {
			AddressLocation.remove( AddressLocation.findById( id ) );
			return ok( addressLocation.render(
					ADDRLOCATION_FORM,
					scala.collection.JavaConversions.asScalaBuffer( AddressLocation.asClassType( AddressLocation.getAddressCollection()
							.find().sort( new BasicDBObject( "_id", 1 ) ).toArray() ) ) ) );
		}
		catch ( final Exception e ) {
			flash( Application.FLASH_MESSAGE_KEY, e.getMessage() );
			final Form< AddrLocation > filledForm = ADDRLOCATION_FORM.bindFromRequest();
			filledForm.reject( e.getMessage() );
			return badRequest( addressLocation.render(
					filledForm,
					scala.collection.JavaConversions.asScalaBuffer( AddressLocation.asClassType( AddressLocation.getAddressCollection()
							.find().toArray() ) ) ) );
		}
	}
}
