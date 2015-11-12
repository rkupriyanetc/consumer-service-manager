package mk.ck.energy.csm.controllers;

import static play.data.Form.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.pa.controllers.AuthenticateBase;

import play.data.Form;
import play.data.validation.Constraints.MinLength;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.account.meter.transfer_report;

public class Meter extends Controller {
	
	private static final Logger LOGGER = LoggerFactory.getLogger( Application.class );
	
	public static class Report {
		
		@MinLength( 4 )
		private String report;
		
		public String getReport() {
			return report;
		}
		
		public void setReport( final String report ) {
			this.report = report;
		}
	}
	
	private static final Form< Report > REPORT_FORM = form( Report.class );
	
	
	public static Result transferReport() {
		AuthenticateBase.noCache( response() );
		return ok( transfer_report.render( REPORT_FORM ) );
	}
	
	public static Result doTransferReport() {
		AuthenticateBase.noCache( response() );
		final Form< Report > filledForm = REPORT_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not select whether to link or not link
			return badRequest( transfer_report.render( filledForm ) );
		else {
			LOGGER.debug( "Transfer report the end" );
			return redirect( routes.Application.index() );
		}
	}
}
