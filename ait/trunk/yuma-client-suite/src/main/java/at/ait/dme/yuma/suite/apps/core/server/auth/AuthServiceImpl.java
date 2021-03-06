/*
 * Copyright 2008-2010 Austrian Institute of Technology
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package at.ait.dme.yuma.suite.apps.core.server.auth;

import at.ait.dme.yuma.suite.apps.core.shared.model.User;
import at.ait.dme.yuma.suite.apps.core.shared.server.auth.AuthService;
import at.ait.dme.yuma.suite.apps.core.shared.server.auth.AuthServiceException;
import at.ait.dme.yuma.suite.framework.YUMAWebSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of the auth service.
 * 
 * @author Rainer Simon
 */
public class AuthServiceImpl extends RemoteServiceServlet implements AuthService {

	private static final long serialVersionUID = -3607603963872144162L;

	@Override
	public User getUser() throws AuthServiceException {	
		try {
			return YUMAWebSession.get().getUser();
		} catch (Throwable t) {
			throw new AuthServiceException(t.getMessage());
		}
	}

}
