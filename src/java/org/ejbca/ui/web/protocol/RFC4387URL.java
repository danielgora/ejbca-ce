/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.ui.web.protocol;

import org.ejbca.core.protocol.certificatestore.HashID;


/**
 * Create RFC4387 URLs and HTML references to such URLs. 
 * @author Lars Silven PrimeKey
 * @version  $Id$
 */
enum RFC4387URL {
	sHash,
	iHash,
	sKIDHash;
	/**
	 * @param url The URL except the query
	 * @param hash of the object to fetch
	 * @return URL to fetch certificate or CRL.
	 */
	String appendQueryToURL(String url, HashID hash, String extraParams) {
		return url+"?"+this.toString()+"="+hash.b64+extraParams;
	}
	/**
	 * @param url The URL except the query
	 * @param hash of the object to fetch
	 * @return URL to fetch certificate or CRL.
	 */
	String appendQueryToURL(String url, HashID hash) {
		return appendQueryToURL(url, hash, "");
	}
	/**
	 * HTML string that show the reference to fetch a certificate or CRL.
	 * @param url The URL except the query
	 * @param hash of the object to fetch
	 * @return URL to fetch certificate or CRL.
	 */
	String getRef(String url, HashID hash, String extraParams) {
		final String resURL = appendQueryToURL(url, hash, extraParams);
		return "<a href=\""+resURL+"\">"+resURL+"</a>";
	}
	/**
	 * @param url The URL except the query
	 * @param hash of the object to fetch
	 * @return URL to fetch certificate or CRL.
	 */
	String getRef(String url, HashID hash) {
		return getRef(url, hash, "");
	}
}