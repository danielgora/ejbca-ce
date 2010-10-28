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

package org.ejbca.core.ejb.log;

import javax.ejb.CreateException;

import org.ejbca.core.ejb.BaseEntityBean;
import org.ejbca.core.model.log.ProtectedLogEventIdentifier;
import org.ejbca.core.model.log.ProtectedLogEventRow;
import org.ejbca.util.Base64;
import org.ejbca.util.GUIDGenerator;
import org.ejbca.util.StringTools;

/** Entity bean should not be used directly, use though Session beans.
*
* Entity Bean representing a log entry in the log database.
* Information stored:
* <pre>
*  pk (Primary Key) is a 32 byte GUID generated by org.ejbca.util.GUIDGenerator
*  
*  adminType is pricipally the type of data stored in the admindata field, should be one of org.ejbca.core.model.log.Admin.TYPE_ constants.
*  adminData is the data identifying the administrator, should be certificate snr or ip-address when no certificate could be retrieved.
*  caId is the Id of the CA performing the event.
*  module indicates the module (CA,RA ...) using the logsession bean.
*  eventTime is the time the event occured.
*  username the name of the user involved or null if no user is involved.
*  certificateSerialNumber is the serial number of the certificate involved in the event or null if no certificate is involved.
*  certificateIssuerDN is the issuers DN of the certificate involved in the event or null if no certificate is involved.
*  eventId is id of the event, should be one of the org.ejbca.core.model.log.LogConstants.EVENT_ constants.
*  eventComment an optional comment of the event.
*  
*  nodeGUID is the current node ID.  
*  counter is a sequential number. Together with GUID a unique identifier for this log-row. 
*  b64LinkedInEventIdentifiers is a collection of (nodeGUID, counter) pairs of linked in rows or null if none were linked to.
*  b64LinkedInEventsHash is the hash of ( The row for each linkedInEventIdentifiers )
*  currentHashAlgorithm is the hash algorithm that should be used to calculate the hash of this LogEventRow and producing b64LinkedInEventsHash.
*  protectionKeyIdentifier is the identifier for the key used to protect the current row or null if the row is unprotected.
*  protectionKeyAlgorithm is the algorithm identifier used to protect this row or null if the row is unprotected.
*  b64Protection is the signature of all the previous columns or null if the row is unprotected.
* </pre>
*
* @ejb.bean
*   description="This enterprise bean entity represents a Log Entry with accompanying data"
*   display-name="ProtectedLogDataEB"
*   name="ProtectedLogData"
*   jndi-name="ProtectedLogData"
*   view-type="local"
*   type="CMP"
*   reentrant="False"
*   cmp-version="2.x"
*   transaction-type="Container"
*   schema="ProtectedLogDataBean"
*   primkey-field="pk"
*
* @ejb.pk
*   generate="false"
*   class="java.lang.String"
*
* @ejb.persistence table-name = "ProtectedLogData"
* 
* @ejb.home
*   generate="local"
*   local-extends="javax.ejb.EJBLocalHome"
*   local-class="org.ejbca.core.ejb.log.ProtectedLogDataLocalHome"
*
* @ejb.interface
*   generate="local"
*   local-extends="javax.ejb.EJBLocalObject"
*   local-class="org.ejbca.core.ejb.log.ProtectedLogDataLocal"
*
* @ejb.transaction type="Required"
* 
* @ejb.finder
*   description="findByNodeGUIDandCounter"
*   signature="org.ejbca.core.ejb.log.ProtectedLogDataLocal findByNodeGUIDandCounter(int nodeGUID, long counter)"
*   query="SELECT OBJECT(a) from ProtectedLogDataBean a WHERE a.nodeGUID=?1 AND a.counter=?2"
*
* @ejb.finder
*   description="findNewProtectedLogEvents"
*   signature="java.util.Collection findNewProtectedLogEvents(int nodeToExclude, long newerThan)"
*   query="SELECT OBJECT(a) from ProtectedLogDataBean a WHERE a.nodeGUID<>?1 AND a.eventTime>=?2 AND a.b64Protection IS NOT NULL"
*   
* @ejb.finder
*   description="findProtectedLogEventsByTime"
*   signature="java.util.Collection findProtectedLogEventsByTime(long eventTime)"
*   query="SELECT OBJECT(a) from ProtectedLogDataBean a WHERE a.eventTime=?1"
*   
* @jboss.method-attributes
*   pattern = "get*"
*   read-only = "true"
*   
* @jboss.method-attributes
*   pattern = "find*"
*   read-only = "true"
*   
* @jonas.jdbc-mapping
*   jndi-name="${datasource.jndi-name}"
* @deprecated
*/
public abstract class ProtectedLogDataBean extends BaseEntityBean {

    /**
     * @ejb.create-method view-type="local"
     */
	public String ejbCreate(int adminType, String admindata, int caid, int module, long eventTime, String username, String certificateSerialNumber,
			String certificateIssuerDN, int eventId, String eventComment, ProtectedLogEventIdentifier eventIdentifier, String nodeIP, ProtectedLogEventIdentifier[] linkedInEventIdentifiers,
			byte[] linkedInEventsHash, String currentHashAlgorithm, int protectionKeyIdentifier, String protectionKeyAlgorithm, byte[] protection) throws CreateException {
		setPk(GUIDGenerator.generateGUID(this));
	    setAdminType(adminType);
	    setAdminData(admindata);
	    setCaId(caid);
	    setModule(module);
	    setEventTime(eventTime);
	    setUsername(StringTools.strip(username));
	    setCertificateSerialNumber(certificateSerialNumber);
	    setCertificateIssuerDN(certificateIssuerDN);
	    setEventId(eventId);
	    setEventComment(eventComment);
	    setNodeGUID(eventIdentifier.getNodeGUID());
	    setCounter(eventIdentifier.getCounter());
	    setNodeIP(nodeIP);
	    setLinkedInEventIdentifiers(linkedInEventIdentifiers);
	    setLinkedInEventsHash(linkedInEventsHash);
	    setCurrentHashAlgorithm(currentHashAlgorithm);
	    setProtectionKeyIdentifier(protectionKeyIdentifier);
	    setProtectionKeyAlgorithm(protectionKeyAlgorithm);
	    setProtection(protection);
        setRowVersion(0);
		return null;
	}

	public void ejbPostCreate(int adminType, String admindata, int caid, int module, long eventTime, String username, String certificateSerialNumber,
			String certificateIssuerDN, int eventId, String eventComment, ProtectedLogEventIdentifier eventIdentifier, String nodeIP, ProtectedLogEventIdentifier[] linkedInEventIdentifiers,
			byte[] linkedInEventsHash, String currentHashAlgorithm, int protectionKeyIdentifier, String protectionKeyAlgorithm, byte[] protection) {
	}
	   
    /**
     * @ejb.pk-field
     * @ejb.persistence column-name="pk"
     * @ejb.interface-method
     */
    public abstract String getPk();
    public abstract void setPk(String pk);

    /**
     * @ejb.persistence column-name="rowVersion"
     */
    public abstract int getRowVersion();
    public abstract void setRowVersion(int rowVersion);

    /**
     * @ejb.persistence column-name="adminType"
     * @ejb.interface-method
     */
    public abstract int getAdminType();
    public abstract void setAdminType(int adminType);

    /**
     * @ejb.persistence column-name="adminData"
     * @ejb.interface-method
     */
    public abstract String getAdminData();
    public abstract void setAdminData(String admindata);
    
    /**
     * @ejb.persistence column-name="cAId"
     * @ejb.interface-method
     */
    public abstract int getCaId();
    public abstract void setCaId(int caid);

    /**
     * @ejb.persistence column-name="module"
     * @ejb.interface-method
     */
    public abstract int getModule();
    public abstract void setModule(int module);

    /**
     * @ejb.persistence column-name="eventTime"
     * @ejb.interface-method
     */
    public abstract long getEventTime();
    public abstract void setEventTime(long eventTime);

    /**
     * @ejb.persistence column-name="username"
     * @ejb.interface-method
     */
    public abstract String getUsername();
    /** username must be called 'stripped' using StringTools.strip()  */
    public abstract void setUsername(String username);

    /**
     * @ejb.persistence column-name="certificateSerialNumber"
     * @ejb.interface-method view-type="local"
     */
    public abstract String getCertificateSerialNumber();
    public abstract void setCertificateSerialNumber(String certificateSerialNumber);

    /**
     * @ejb.persistence column-name="certificateIssuerDN"
     * @ejb.interface-method view-type="local"
     */
    public abstract String getCertificateIssuerDN();
    public abstract void setCertificateIssuerDN(String certificateIssuerDN);
    
    /**
     * @ejb.persistence column-name="eventId"
     * @ejb.interface-method view-type="local"
     */
    public abstract int getEventId();
    public abstract void setEventId(int eventId);

    /**
     * @ejb.persistence jdbc-type="LONGVARCHAR" column-name="eventComment"
     * @ejb.interface-method view-type="local"
     */
    public abstract String getEventComment();
    public abstract void setEventComment(String eventComment);

    /**
     * @ejb.persistence column-name="nodeGUID"
     * @ejb.interface-method
     */
    public abstract int getNodeGUID();
    public abstract void setNodeGUID(int nodeGUID);

    /**
     * @ejb.persistence column-name="counter"
     * @ejb.interface-method
     */
    public abstract long getCounter();
    public abstract void setCounter(long counter);
    
    /**
     * @ejb.persistence column-name="nodeIP"
     * @ejb.interface-method
     */
    public abstract String getNodeIP();
    public abstract void setNodeIP(String nodeIP);
    
    /**
     * @ejb.persistence jdbc-type="LONGVARCHAR" column-name="b64LinkedInEventIdentifiers"
     * @ejb.interface-method
     */
    public abstract String getB64LinkedInEventIdentifiers();
    public abstract void setB64LinkedInEventIdentifiers(String b64LinkedInEventIdentifiers);
    
    /** @ejb.interface-method */
    public ProtectedLogEventIdentifier[] getLinkedInEventIdentifiers() {
    	String b64LinkedInEventIdentifiers = getB64LinkedInEventIdentifiers();
    	ProtectedLogEventIdentifier[] protectedLogEventIdentifiers = null;
    	if (b64LinkedInEventIdentifiers != null) {
        	String[] b64LinkedInEventIdentifierArray = b64LinkedInEventIdentifiers.split(";");
        	protectedLogEventIdentifiers = new ProtectedLogEventIdentifier[b64LinkedInEventIdentifierArray.length];
    		for (int i=0; i<b64LinkedInEventIdentifierArray.length; i++) {
    			protectedLogEventIdentifiers[i] = new ProtectedLogEventIdentifier(b64LinkedInEventIdentifierArray[i]);
    		}
    	}
    	return protectedLogEventIdentifiers;
    }
    /** @ejb.interface-method */
    public void setLinkedInEventIdentifiers(ProtectedLogEventIdentifier[] linkedInEventIdentifiers) {
    	if (linkedInEventIdentifiers != null) {
    		String b64LinkedInEventIdentifiers = null;
        	for (int i=0; i<linkedInEventIdentifiers.length; i++) {
        		if (b64LinkedInEventIdentifiers == null) {
        			b64LinkedInEventIdentifiers = linkedInEventIdentifiers[i].getAsBase64EncodedString();
        		} else {
        			b64LinkedInEventIdentifiers += ";" + linkedInEventIdentifiers[i].getAsBase64EncodedString();
        		}
        	}
        	setB64LinkedInEventIdentifiers(b64LinkedInEventIdentifiers);
    	} else {
    		setB64LinkedInEventIdentifiers(null);
    	}
    }

    /**
     * @ejb.persistence column-name="b64LinkedInEventsHash"
     * @ejb.interface-method
     */
    public abstract String getB64LinkedInEventsHash();
    public abstract void setB64LinkedInEventsHash(String b64LinkedInEventsHash);
    
    /** @ejb.interface-method */
    public byte[] getLinkedInEventsHash() {
    	String b64LinkedInEventsHash = getB64LinkedInEventsHash();
    	if (b64LinkedInEventsHash != null) {
        	return Base64.decode(b64LinkedInEventsHash.getBytes());
    	}
    	return null;
    }
    /** @ejb.interface-method */
    public void setLinkedInEventsHash(byte[] linkedInEventsHash) {
    	if (linkedInEventsHash != null) {
        	setB64LinkedInEventsHash(new String(Base64.encode(linkedInEventsHash, false)));
    	} else {
        	setB64LinkedInEventsHash(null);
    	}
    }

    /**
     * @ejb.persistence column-name="currentHashAlgorithm"
     * @ejb.interface-method
     */
    public abstract String getCurrentHashAlgorithm();
    public abstract void setCurrentHashAlgorithm(String currentHashAlgorithm);

    /**
     * @ejb.persistence column-name="protectionKeyIdentifier"
     * @ejb.interface-method
     */
    public abstract int getProtectionKeyIdentifier();
    public abstract void setProtectionKeyIdentifier(int protectionKeyIdentifier);

    /**
     * @ejb.persistence column-name="protectionKeyAlgorithm"
     * @ejb.interface-method
     */
    public abstract String getProtectionKeyAlgorithm();
    public abstract void setProtectionKeyAlgorithm(String protectionKeyAlgorithm);

    /**
     * @ejb.persistence jdbc-type="LONGVARCHAR" column-name="b64Protection"
     * @ejb.interface-method
     */
    public abstract String getB64Protection();
    public abstract void setB64Protection(String protection);

    /** @ejb.interface-method */
    public byte[] getProtection() {
    	String b64Protection = getB64Protection();
    	if (b64Protection != null) {
        	return Base64.decode(b64Protection.getBytes());
    	}
    	return null;
    }
    /** @ejb.interface-method */
    public void setProtection(byte[] protection) {
    	if (protection != null) {
        	setB64Protection(new String(Base64.encode(protection, false)));
    	} else {
        	setB64Protection(null);
    	}
    }
    
    /** @ejb.interface-method */
    public ProtectedLogEventRow toProtectedLogEventRow() {
    	return new ProtectedLogEventRow(getAdminType(), getAdminData(), getCaId(), getModule(), getEventTime(),
    			getUsername(), getCertificateSerialNumber(), getCertificateIssuerDN(), getEventId(), getEventComment(),
    			new ProtectedLogEventIdentifier(getNodeGUID(), getCounter()), getNodeIP(), getLinkedInEventIdentifiers(), getLinkedInEventsHash(),
    			getCurrentHashAlgorithm(), getProtectionKeyIdentifier(), getProtectionKeyAlgorithm(), getProtection());
    }
    
}
