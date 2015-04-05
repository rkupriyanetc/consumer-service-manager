package mk.ck.energy.csm.providers;

import mk.ck.energy.csm.model.auth.MyUser;

import com.feth.play.module.pa.user.FirstLastNameIdentity;

public class MyFirstLastNameUserPasswordAuthUser extends MyUsernamePasswordAuthUser implements FirstLastNameIdentity {
	
	private static final long	serialVersionUID	= 1L;
	
	private final String			firstName;
	
	private final String			lastName;
	
	public MyFirstLastNameUserPasswordAuthUser( final MyUser signup ) {
		super( signup );
		firstName = signup.getFirstName();
		lastName = signup.getLastName();
	}
	
	@Override
	public String getFirstName() {
		return firstName;
	}
	
	@Override
	public String getLastName() {
		return lastName;
	}
}
