/**********************************************************************
 * Interface DataProvider
 * Copyright (c) 2010, German National Library / Deutsche Nationalbibliothek
 * Adickesallee 1, D-60322 Frankfurt am Main, Federal Republic of Germany 
 *
 * This program is free software.
 * For your convenience it is dual licensed.
 * You can redistribute it and/or modify it under the terms of
 * one of the following licenses:
 * 
 * 1.)
 * The GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * You should have received a copy of the GNU General Public License
 * along with this program (gpl-3.0.txt); if not please read
 * http://www.gnu.org/licenses/gpl.html
 * 
 * 2.)
 * The European Union Public Licence as published by
 * The European Commission (executive body of the European Union);
 * either version 1.1 of the License, or (at your option) any later version.
 * You should have received a copy of the European Union Public Licence
 * along with this program (eupl_v1.1_en.pdf); if not please read
 * http://www.osor.eu/eupl
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the above licenses for more details.
 * 
 * Jürgen Kett, Kadir Karaca Kocer -- German National Library
 * 
 **********************************************************************/


/* ********************************************************************
 * CHANGELOG:
 * 
 * 2010-05-05 Refactoring + new package structure by Karaca Kocer
 * 2010-04-25 Code formatting, comments and license text by Karaca Kocer
 * Created on 2010-03-17 by Jürgen Kett
 ********************************************************************/

package eu.europeanaconnect.erds;

import java.util.List;

/**
 * Interface DataProvider.
 * 
 * An abstract expression for all classes that provide
 * some kind of data to the user or partner system.
 * All implementing classes shall resolve requests for a
 * well defined pattern and return an implementation
 * of the class ResolverResponse.
 *   
 * @author Jürgen Kett (Original code)
 * @author Kadir Karaca Kocer (Refactoring, license text, comments & JavaDoc)
 * @param <R> An abstract response, will be defined by the implementing class.
 */

public interface DataProvider<R> {
	
	/**
	 * @return The identifier.
	 */
	public String getId();
	
	/**
	 * @return The list of all supported namespaces.
	 */
	public List<String> getSupportedNamespaces();
	
	/**
	 * @return The pattern of the identifier.
	 */
	public String getIdentifierPattern();

	/**
	 * Resolves the given request and returns the response object.
	 * 
	 * @param request A request for resolving a persistent identifier.
	 * @return A response
	 * @throws ResolverException If resolving could not be finished due to some error.
	 * @see ResolverRequest
	 * @see ResolverException
	 */
	public R getResponse(ResolverRequest request) throws ResolverException;
}
