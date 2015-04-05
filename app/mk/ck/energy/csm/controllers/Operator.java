package mk.ck.energy.csm.controllers;

import mk.ck.energy.csm.model.auth.UserRole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.oper.index;
import views.html.oper.script;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class Operator extends Controller {
	
	private static final Logger	LOGGER	= LoggerFactory.getLogger( AccountTools.class );
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result index() {
		return ok( index.render() );
	}
	
	@Restrict( { @Group( UserRole.OPER_ROLE_NAME ), @Group( UserRole.ADMIN_ROLE_NAME ) } )
	public static Result script() {
		LOGGER.debug( "Run the script" );
		return ok( script.render() );
	}
}
