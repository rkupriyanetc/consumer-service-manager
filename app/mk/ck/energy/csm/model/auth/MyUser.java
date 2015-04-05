package mk.ck.energy.csm.model.auth;

import mk.ck.energy.csm.providers.MyUsernamePasswordAuthProvider;
import play.data.validation.Constraints.Required;

import com.feth.play.module.pa.user.FirstLastNameIdentity;

public class MyUser extends MyUsernamePasswordAuthProvider.MySignup implements FirstLastNameIdentity {
	
	@Required
	private String	role;
	
	@Required
	private String	firstName;
	
	@Required
	private String	lastName;
	
	public MyUser() {}
	
	public MyUser( final String role ) {
		this.role = role;
	}
	
	public String getRole() {
		return role;
	}
	
	public void setRole( final String role ) {
		this.role = role;
	}
	
	@Override
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName( final String firstName ) {
		this.firstName = firstName;
	}
	
	@Override
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName( final String lastName ) {
		this.lastName = lastName;
	}
}