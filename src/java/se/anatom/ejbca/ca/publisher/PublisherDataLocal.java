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
 
package se.anatom.ejbca.ca.publisher;


/**
 * For docs, see PublisherDataBean
 *
 * @version $Id: PublisherDataLocal.java,v 1.2 2004-04-16 07:38:55 anatom Exp $
 **/

public interface PublisherDataLocal extends javax.ejb.EJBLocalObject {

    // Public methods

    public Integer getId();

    public int getUpdateCounter();

    public void setName(String name);
    
	public String getName();
     
    public BasePublisher getPublisher();

    public void setPublisher(BasePublisher publisher);
}

