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

package at.ait.dme.yuma.suite.framework.pages.audio;

import org.apache.wicket.PageParameters;

import at.ait.dme.yuma.suite.framework.pages.BaseHostPage;

/**
 * Host page for the audio annotation tool.
 * 
 * @author Rainer Simon
 */
public class AudioHostPage extends BaseHostPage {
	
	public AudioHostPage(final PageParameters parameters) {
		super("YUMA Audio", "yuma.audio/yuma.audio.nocache.js", parameters);
		
		// TODO redirect to 'examples' if params are insufficient
	}

}
