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
 
package se.anatom.ejbca.ra;

import java.security.NoSuchAlgorithmException;

/**
 * For docs, see UserDataBean
 *
 * @version $Id: UserDataLocal.java,v 1.12 2004-04-16 07:38:56 anatom Exp $
 **/
public interface UserDataLocal extends javax.ejb.EJBLocalObject {

    // Constants for Status of user

    public static final int STATUS_NEW = 10;        // New user
    public static final int STATUS_FAILED = 11;     // Generation of user certificate failed
    public static final int STATUS_INITIALIZED = 20;// User has been initialized
    public static final int STATUS_INPROCESS = 30;  // Generation of user certificate in process
    public static final int STATUS_GENERATED = 40;  // A certificate has been generated for the user
    public static final int STATUS_REVOKED = 50;  // The user has been revoked and should not have any more certificates issued
    public static final int STATUS_HISTORICAL = 60; // The user is old and archived
    public static final int STATUS_KEYRECOVERY  = 70; // The user is should use key recovery functions in next certificate generation.

    // public methods

    public String getUsername();
    /** username must be called 'striped' using StringTools.strip()
    * @see se.anatom.ejbca.util.StringTools
    */
    public void setUsername(String username);
    public String getSubjectDN();
    public void setSubjectDN(String subjectDN);
    public int getCAId();
    public void setCAId(int caid);
    
    public String getSubjectAltName();
    public void setSubjectAltName(String subjectAltName);
    public String getSubjectEmail();
    public void setSubjectEmail(String subjectEmail);
    public int getStatus();
    public void setStatus(int status);
    public int getType();
    public void setType(int type);
    public String getClearPassword();
    public void setClearPassword(String clearPassword);
    public String getPasswordHash();
    public void setPasswordHash(String passwordHash);

    public long getTimeCreated();
    public long getTimeModified();
    public void setTimeModified(long createtime);
    public int getEndEntityProfileId();
    public void setEndEntityProfileId(int endentityprofileid);
    public int getCertificateProfileId();
    public void setCertificateProfileId(int certificateprofileid);
    public int getTokenType();
    public void setTokenType(int tokentype);
    public int getHardTokenIssuerId();
    public void setHardTokenIssuerId(int hardtokenissuerid);
    public ExtendedInformation getExtendedInformation();
    public void setExtendedInformation(ExtendedInformation extendedinformation);
    
    public void setDN(String dn);
    public void setPassword(String password) throws  NoSuchAlgorithmException;
    public void setOpenPassword(String password) throws  NoSuchAlgorithmException;
    public boolean comparePassword(String password) throws NoSuchAlgorithmException;
}

