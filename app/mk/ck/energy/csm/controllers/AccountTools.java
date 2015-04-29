package mk.ck.energy.csm.controllers;

import static play.data.Form.form;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import mk.ck.energy.csm.model.AddressLocation;
import mk.ck.energy.csm.model.AddressNotFoundException;
import mk.ck.energy.csm.model.AddressPlace;
import mk.ck.energy.csm.model.AddressTop;
import mk.ck.energy.csm.model.AdministrativeCenterType;
import mk.ck.energy.csm.model.ImpossibleCreatingException;
import mk.ck.energy.csm.model.LocationType;
import mk.ck.energy.csm.model.StreetType;
import mk.ck.energy.csm.model.auth.UserRole;

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

import com.mongodb.client.model.Filters;

public class AccountTools extends Controller {
	
	private static final Logger	LOGGER	= LoggerFactory.getLogger( AccountTools.class );
	
	public static class AddrTop {
		
		@Required
		private String	name;
		
		private String	refId;
		
		@Required
		private String	id;
		
		public AddrTop() {
			id = UUID.randomUUID().toString().toLowerCase();
		}
		
		public String getId() {
			return id;
		}
		
		public void setId( final String id ) {
			this.id = id;
		}
		
		public String getRefId() {
			return refId;
		}
		
		public void setRefId( final String refId ) {
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
		private String					id;
		
		@Required
		private String					refId;
		
		@Required
		private String					location;
		
		@Required
		private String					locationType;
		
		private List< String >	administrativeCenterType;
		
		public AddrLocation() {
			id = UUID.randomUUID().toString().toLowerCase();
			administrativeCenterType = new LinkedList<>();
		}
		
		public String getId() {
			return id;
		}
		
		public void setId( final String id ) {
			this.id = id;
		}
		
		public String getRefId() {
			return refId;
		}
		
		public void setRefId( final String refId ) {
			this.refId = refId;
		}
		
		public String getLocation() {
			return location;
		}
		
		public void setLocation( final String location ) {
			this.location = location;
		}
		
		public String getLocationType() {
			return locationType;
		}
		
		public void setLocationType( final String locationType ) {
			this.locationType = locationType;
		}
		
		public List< String > getAdministrativeCenterType() {
			return administrativeCenterType;
		}
		
		public void setAdministrativeCenterType( final List< String > administrativeCenterType ) {
			this.administrativeCenterType = administrativeCenterType;
		}
	}
	
	public static class AddrPlace {
		
		@Required
		private String	id;
		
		@Required
		private String	streetType;
		
		@Required
		private String	street;
		
		public AddrPlace() {
			id = UUID.randomUUID().toString().toLowerCase();
		}
		
		public String getId() {
			return id;
		}
		
		public void setId( final String id ) {
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
				scala.collection.JavaConversions.asScalaIterator( AddressTop.getMongoCollection().find()
						.sort( Filters.and( Filters.eq( "top_id", 1 ), Filters.eq( "name", 1 ) ) ).iterator() ) ) );
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result doTestTopAddress() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< AddrTop > filledForm = ADDRTOP_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			return badRequest( addressTop.render( filledForm,
					scala.collection.JavaConversions.asScalaIterator( AddressTop.getMongoCollection().find().iterator() ) ) );
		else {
			final AddrTop u = filledForm.get();
			final AddressTop at = AddressTop.create( u.getName(), u.getRefId() );
			LOGGER.trace( "Address top saved {}", at );
			at.save();
			// Тут тра переробити
			filledForm.data().put( "id", at.getId() );
			LOGGER.trace( "Address top saved {}", at );
			return ok( addressTop.render(
					filledForm,
					scala.collection.JavaConversions.asScalaIterator( AddressTop.getMongoCollection().find()
							.sort( Filters.and( Filters.eq( "top_id", 1 ), Filters.eq( "name", 1 ) ) ).iterator() ) ) );
		}
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result testLocationAddress() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		return ok( addressLocation.render(
				ADDRLOCATION_FORM,
				scala.collection.JavaConversions.asScalaIterator( AddressLocation.getMongoCollection().find()
						.sort( Filters.eq( "top_address_id", 1 ) ).iterator() ) ) );
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result doTestLocationAddress() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< AddrLocation > filledForm = ADDRLOCATION_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			return badRequest( addressLocation.render( filledForm,
					scala.collection.JavaConversions.asScalaIterator( AddressLocation.getMongoCollection().find().iterator() ) ) );
		else {
			final AddrLocation u = filledForm.get();
			final Set< AdministrativeCenterType > act = new LinkedHashSet<>();
			try {
				for ( final String i : u.getAdministrativeCenterType() )
					act.add( AdministrativeCenterType.valueOf( i ) );
			}
			catch ( final NumberFormatException nfe ) {
				LOGGER.warn( "Error convertation StreetType of {}", u.getAdministrativeCenterType() );
			}
			AddressLocation al = null;
			try {
				final AddressTop at = AddressTop.findById( u.getRefId() );
				al = AddressLocation.create( at, u.getLocation(), LocationType.valueOf( u.getLocationType() ), act );
				al.save();
				// Тут тра переробити
				filledForm.data().put( "id", al.getId() );
				LOGGER.trace( "Address location saved {}", al );
			}
			catch ( final AddressNotFoundException anfe ) {
				LOGGER.warn( "Address Not Found in doTestLocationAddress() method!" );
				filledForm.reject( "Address Not Found in doTestLocationAddress() method!" );
				return badRequest( addressLocation.render( filledForm,
						scala.collection.JavaConversions.asScalaIterator( AddressLocation.getMongoCollection().find().iterator() ) ) );
			}
			catch ( final ImpossibleCreatingException ice ) {
				LOGGER.warn( "Cannot save AddressLocation bun only one CAPITAL city. Your: {}", al );
				filledForm.reject( "Allowed only one CAPITAL city in DB" );
				return badRequest( addressLocation.render( filledForm,
						scala.collection.JavaConversions.asScalaIterator( AddressLocation.getMongoCollection().find().iterator() ) ) );
			}
			return ok( addressLocation.render(
					filledForm,
					scala.collection.JavaConversions.asScalaIterator( AddressLocation.getMongoCollection().find()
							.sort( Filters.eq( "top_address_id", 1 ) ).iterator() ) ) );
		}
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result testPlaceAddress() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		return ok( addressPlace.render(
				ADDRPLACE_FORM,
				scala.collection.JavaConversions.asScalaIterator( AddressPlace.getMongoCollection().find()
						.sort( Filters.eq( "street", 1 ) ).iterator() ) ) );
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result doTestPlaceAddress() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< AddrPlace > filledForm = ADDRPLACE_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			return badRequest( addressPlace.render( filledForm,
					scala.collection.JavaConversions.asScalaIterator( AddressPlace.getMongoCollection().find().iterator() ) ) );
		else {
			final AddrPlace u = filledForm.get();
			final AddressPlace ap = AddressPlace.create( StreetType.valueOf( u.getStreetType() ), u.getStreet() );
			ap.save();
			// Тут тра переробити
			filledForm.data().put( "id", ap.getId() );
			LOGGER.trace( "Address place saved {}", ap );
			return ok( addressPlace.render(
					filledForm,
					scala.collection.JavaConversions.asScalaIterator( AddressPlace.getMongoCollection().find()
							.sort( Filters.eq( "street", 1 ) ).iterator() ) ) );
		}
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result removeTopAddress( final String id ) {
		try {
			AddressTop.remove( AddressTop.findById( id ) );
			return ok( addressTop.render(
					ADDRTOP_FORM,
					scala.collection.JavaConversions.asScalaIterator( AddressTop.getMongoCollection().find()
							.sort( Filters.eq( "top_id", 1 ) ).iterator() ) ) );
		}
		catch ( final Exception e ) {
			flash( Application.FLASH_MESSAGE_KEY, e.getMessage() );
			final Form< AddrTop > filledForm = ADDRTOP_FORM.bindFromRequest();
			filledForm.reject( e.getMessage() );
			return badRequest( addressTop.render( filledForm,
					scala.collection.JavaConversions.asScalaIterator( AddressTop.getMongoCollection().find().iterator() ) ) );
		}
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result removeLocationAddress( final String id ) {
		try {
			AddressLocation.remove( AddressLocation.findById( id ) );
			return ok( addressLocation.render(
					ADDRLOCATION_FORM,
					scala.collection.JavaConversions.asScalaIterator( AddressLocation.getMongoCollection().find()
							.sort( Filters.eq( "_id", 1 ) ).iterator() ) ) );
		}
		catch ( final Exception e ) {
			flash( Application.FLASH_MESSAGE_KEY, e.getMessage() );
			final Form< AddrLocation > filledForm = ADDRLOCATION_FORM.bindFromRequest();
			filledForm.reject( e.getMessage() );
			return badRequest( addressLocation.render(
					filledForm,
					scala.collection.JavaConversions.asScalaIterator( AddressLocation.getMongoCollection().find()
							.sort( Filters.eq( "top_address_id", 1 ) ).iterator() ) ) );
		}
	}
}
