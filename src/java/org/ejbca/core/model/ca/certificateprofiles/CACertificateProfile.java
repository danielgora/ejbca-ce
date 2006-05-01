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
 
/*
 * CACertificateProfile.java
 *
 * Created on den 29 juli 2002, 22:08
 */
package org.ejbca.core.model.ca.certificateprofiles;



/**
 * CACertificateProfile is a class defining the fixed characteristics of a CA certificate profile.
 *
 * @version $Id: CACertificateProfile.java,v 1.2 2006-05-01 14:20:00 anatom Exp $
 */
public class CACertificateProfile extends CertificateProfile{

    // Public Constants
    public static final String CERTIFICATEPROFILENAME =  "SUBCA";

    // Public Methods

    /**
     * Creates a certificate with the characteristics of an end user.
     * General options are set in the superclass's default contructor that is called automatically.
     * You can override the general options by defining them again with different parameters here.
     */
    public CACertificateProfile() {

      setType(TYPE_SUBCA);

      setUseKeyUsage(true);
      setKeyUsage(new boolean[9]);
      setKeyUsage(DIGITALSIGNATURE,true);
      setKeyUsage(KEYCERTSIGN,true);
      setKeyUsage(CRLSIGN,true);
      setKeyUsageCritical(true);

    }

    // Public Methods.
    public void upgrade(){
      if(LATEST_VERSION != getVersion()){
        // New version of the class, upgrade
          super.upgrade(); 			        
      }
 
    }

    // Private fields.
}
