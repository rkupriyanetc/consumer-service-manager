/*
 * Copyright © 2014 Florian Hars, nMIT Solutions GmbH
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mk.ck.energy.csm.providers;

import com.feth.play.module.pa.providers.wwwauth.basic.BasicAuthProvider;
import com.feth.play.module.pa.user.AuthUser;
import com.google.inject.Inject;

import play.Application;
import play.mvc.Http.Context;
import play.twirl.api.Content;
import views.html.login;

/** A really simple basic auth provider that accepts one hard coded user */
public class MyStupidBasicAuthProvider extends BasicAuthProvider {
	
	public static String	GUEST_PROVIDER	= "basic";
																				
	public static String	GUEST_ID				= "basic";
																				
	@Inject
	public MyStupidBasicAuthProvider( final Application app ) {
		super( app );
	}
	
	@Override
	protected AuthUser authenticateUser( final String username, final String password ) {
		if ( username.equals( "guest" ) && password.equals( "guest" ) )
			return new AuthUser() {
				
				private static final long serialVersionUID = 1L;
				
				@Override
				public String getId() {
					return GUEST_ID;
				}
				
				@Override
				public String getProvider() {
					return GUEST_PROVIDER;
				}
			};
		return null;
	}
	
	@Override
	public String getKey() {
		return GUEST_ID;
	}
	
	/** Diplay the normal login form if HTTP authentication fails */
	@Override
	protected Content unauthorized( final Context context ) {
		return login.render( MyUsernamePasswordAuthProvider.LOGIN_FORM );
	}
}
