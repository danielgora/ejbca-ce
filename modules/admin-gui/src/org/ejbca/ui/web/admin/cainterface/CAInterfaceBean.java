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
 
package org.ejbca.ui.web.admin.cainterface;

import java.io.Serializable;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.bouncycastle.util.encoders.Base64;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authorization.AuthorizationDeniedException;
import org.cesecore.authorization.control.AccessControlSessionLocal;
import org.cesecore.authorization.rules.AccessRuleManagementSessionLocal;
import org.cesecore.authorization.user.AccessUserAspectManagerSessionLocal;
import org.cesecore.certificates.ca.CADoesntExistsException;
import org.cesecore.certificates.ca.CAInfo;
import org.cesecore.certificates.ca.CAOfflineException;
import org.cesecore.certificates.ca.CaSession;
import org.cesecore.certificates.certificate.CertificateCreateSessionLocal;
import org.cesecore.certificates.certificate.CertificateInfo;
import org.cesecore.certificates.certificate.CertificateStatus;
import org.cesecore.certificates.certificate.CertificateStoreSession;
import org.cesecore.certificates.certificateprofile.CertificateProfile;
import org.cesecore.certificates.certificateprofile.CertificateProfileConstants;
import org.cesecore.certificates.certificateprofile.CertificateProfileDoesNotExistException;
import org.cesecore.certificates.certificateprofile.CertificateProfileExistsException;
import org.cesecore.certificates.certificateprofile.CertificateProfileSession;
import org.cesecore.certificates.crl.CRLInfo;
import org.cesecore.certificates.crl.CrlCreateSession;
import org.cesecore.certificates.crl.CrlStoreSession;
import org.cesecore.certificates.crl.RevokedCertInfo;
import org.cesecore.certificates.endentity.ExtendedInformation;
import org.cesecore.keys.token.CryptoTokenOfflineException;
import org.cesecore.util.CertTools;
import org.ejbca.core.ejb.ca.caadmin.CAAdminSession;
import org.ejbca.core.ejb.ca.publisher.PublisherQueueSession;
import org.ejbca.core.ejb.ca.publisher.PublisherSessionLocal;
import org.ejbca.core.ejb.ca.revoke.RevocationSessionLocal;
import org.ejbca.core.ejb.ca.sign.SignSession;
import org.ejbca.core.ejb.ca.store.CertReqHistorySession;
import org.ejbca.core.ejb.config.GlobalConfigurationSession;
import org.ejbca.core.ejb.hardtoken.HardTokenSession;
import org.ejbca.core.ejb.ra.UserAdminSessionLocal;
import org.ejbca.core.ejb.ra.raadmin.EndEntityProfileSession;
import org.ejbca.core.ejb.services.ServiceSessionLocal;
import org.ejbca.core.model.SecConst;
import org.ejbca.core.model.ca.store.CertReqHistory;
import org.ejbca.core.model.util.EjbLocalHelper;
import org.ejbca.ui.web.CertificateView;
import org.ejbca.ui.web.RequestHelper;
import org.ejbca.ui.web.RevokedInfoView;
import org.ejbca.ui.web.admin.cainterface.exception.ExcessiveResultsException;
import org.ejbca.ui.web.admin.configuration.EjbcaWebBean;
import org.ejbca.ui.web.admin.configuration.InformationMemory;

/**
 * A class used as an interface between CA jsp pages and CA ejbca functions.
 *
 * @version $Id$
 */
public class CAInterfaceBean implements Serializable {

	private static final long serialVersionUID = 2L;

    public static final int CATOKENTYPE_P12          = 1;
    public static final int CATOKENTYPE_HSM          = 2;
	public static final int CATOKENTYPE_NULL         = 3;

	private EjbLocalHelper ejb = new EjbLocalHelper();

    // Private fields
	private AccessControlSessionLocal authorizationsession;
    private AccessRuleManagementSessionLocal accessRuleManagementSession;
    private AccessUserAspectManagerSessionLocal accessUserAspectManagerSession;
	private CAAdminSession caadminsession;
	private CaSession caSession;
	private CertificateCreateSessionLocal certcreatesession;
	private CertificateProfileSession certificateProfileSession;
    private CertificateStoreSession certificatesession;
    private CertReqHistorySession certreqhistorysession;  
    private CrlStoreSession crlStoreSession;
    private CrlCreateSession crlCreateSession;
    private EndEntityProfileSession endEntityProfileSession;
    private GlobalConfigurationSession globalconfigurationsession;
    private HardTokenSession hardtokensession;
    private PublisherQueueSession publisherqueuesession;
    private PublisherSessionLocal publishersession;
    private RevocationSessionLocal revocationSession;
    private ServiceSessionLocal serviceSession;
    private SignSession signsession;
    private UserAdminSessionLocal userAdminSession;

    private CertificateProfileDataHandler certificateprofiles;
    private CADataHandler cadatahandler;
    private PublisherDataHandler publisherdatahandler;
    
    private boolean initialized;
    private AuthenticationToken administrator;
    private InformationMemory informationmemory;
    private CAInfo cainfo;
    /** The certification request in binary format */
    transient private byte[] request;
    private Certificate processedcert;
    private CertificateProfile tempCertProfile = null;
    private boolean isUniqueIndex;
	
	/** Creates a new instance of CaInterfaceBean */
    public CAInterfaceBean() { }

    // Public methods
    public void initialize(EjbcaWebBean ejbcawebbean) {
        if(!initialized){
            this.accessRuleManagementSession = ejb.getAccessRuleManagementSession();
            this.accessUserAspectManagerSession = ejb.getAccessUserAspectSession();
          caSession = ejb.getCaSession();
          certificatesession = ejb.getCertificateStoreSession();
          certreqhistorysession = ejb.getCertReqHistorySession();
          crlStoreSession = ejb.getCrlStoreSession();
          crlCreateSession = ejb.getCrlCreateSession();
          caadminsession = ejb.getCaAdminSession();
          authorizationsession = ejb.getAccessControlSession();
          userAdminSession = ejb.getUserAdminSession();
          globalconfigurationsession = ejb.getGlobalConfigurationSession();               
          signsession = ejb.getSignSession();
          certcreatesession = ejb.getCertificateCreateSession();
          hardtokensession = ejb.getHardTokenSession();               
          publishersession = ejb.getPublisherSession();               
          publisherqueuesession = ejb.getPublisherQueueSession();
          certificateProfileSession = ejb.getCertificateProfileSession();
          endEntityProfileSession = ejb.getEndEntityProfileSession(); 
          revocationSession = ejb.getRevocationSession();
          serviceSession = ejb.getServiceSession();
          this.informationmemory = ejbcawebbean.getInformationMemory();
          this.administrator = ejbcawebbean.getAdminObject();
            
          certificateprofiles = new CertificateProfileDataHandler(administrator, authorizationsession, caSession, certificateProfileSession, informationmemory);;
            cadatahandler = new CADataHandler(administrator, accessRuleManagementSession, accessUserAspectManagerSession,caadminsession, caSession, endEntityProfileSession, userAdminSession, globalconfigurationsession,
                    certificateProfileSession, revocationSession, ejbcawebbean);
          publisherdatahandler = new PublisherDataHandler(administrator, publishersession, authorizationsession, caadminsession, certificateProfileSession,  informationmemory);
          isUniqueIndex = certcreatesession.isUniqueCertificateSerialNumberIndex();
          initialized =true;
        }
    }

    public void initialize(HttpServletRequest request, EjbcaWebBean ejbcawebbean) {
    	initialize(ejbcawebbean);
    }

    public CertificateView[] getCACertificates(int caid) {
    	try {
    		final Collection<Certificate> chain = signsession.getCertificateChain(administrator, caid);
    		final CertificateView[] returnval = new CertificateView[chain.size()];
    		final Iterator<Certificate> iter = chain.iterator();
    		int i=0;
    		while(iter.hasNext()){
    			final Certificate next = iter.next();  
    			RevokedInfoView revokedinfo = null;
    			CertificateStatus revinfo = certificatesession.getStatus(CertTools.getIssuerDN(next), CertTools.getSerialNumber(next));
    			if(revinfo != null && revinfo.revocationReason != RevokedCertInfo.NOT_REVOKED) {
    				revokedinfo = new RevokedInfoView(revinfo, CertTools.getSerialNumber(next));
    			}
    			returnval[i] = new CertificateView(next, revokedinfo, null);
    			i++;
    		}
    		return returnval;
    	} catch (AuthorizationDeniedException e) {
    		throw new RuntimeException(e);
    	}
    }
    
    /**
     * Method that returns a HashMap connecting available CAIds (Integer) to CA Names (String).
     */ 
    public Map<Integer, String>  getCAIdToNameMap(){
    	return informationmemory.getCAIdToNameMap();      
    }

    /**
     * Return the name of the CA based on its ID
     * @param caId the CA ID
     * @return the name of the CA or null if it does not exists.
     */
    public String getName(Integer caId) {
        return (String)informationmemory.getCAIdToNameMap().get(caId);
    }

    public Collection<Integer> getAuthorizedCAs(){
      return informationmemory.getAuthorizedCAIds();
    }  

    public TreeMap<String, Integer> getEditCertificateProfileNames() {
      return informationmemory.getEditCertificateProfileNames();
    }

    /** Returns the profile name from id proxied */
    public String getCertificateProfileName(int profileid) {
    	return this.informationmemory.getCertificateProfileNameProxy().getCertificateProfileName(profileid);
    }
    
    public int getCertificateProfileId(String profilename) {
    	return certificateprofiles.getCertificateProfileId(profilename);
    }

    public CertificateProfile getCertificateProfile(String name) throws AuthorizationDeniedException {
    	return certificateprofiles.getCertificateProfile(name);
    }

    public CertificateProfile getCertificateProfile(int id) throws AuthorizationDeniedException {
    	return certificateprofiles.getCertificateProfile(id);
    }

    public void addCertificateProfile(String name) throws CertificateProfileExistsException, AuthorizationDeniedException {
       CertificateProfile profile = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
       profile.setAvailableCAs(informationmemory.getAuthorizedCAIds());
       certificateprofiles.addCertificateProfile(name, profile);
    }

    public void changeCertificateProfile(String name, CertificateProfile profile) throws Exception {
       certificateprofiles.changeCertificateProfile(name, profile);
    }
    
    /**
     * Returns a {@link List} of service names using the given certificate profile
     * 
     * @param certificateProfileName the name of the profile to look for.
     * @return a {@link List} of service names using the given certificate profile
     * @throws CertificateProfileDoesNotExistException if sought certificate profile was not found.
     */
    public List<String> getServicesUsingCertificateProfile(final String certificateProfileName) throws CertificateProfileDoesNotExistException {
        Integer certificateProfileId = certificateProfileSession.getCertificateProfileId(certificateProfileName);
        if (certificateProfileId == 0) {
            throw new CertificateProfileDoesNotExistException(certificateProfileName + " was not found.");
        } else {
            return serviceSession.getServicesUsingCertificateProfile(certificateProfileId);
        }
    }
    
    /**
     * Returns a count of all end entities using a certain certificate profile of the 
     * SecConst.CERTTYPE_ENDENTITY type. 
     * 
     * @param certificateProfileName the name of the certificate profile
     * @return the number of end entities found
     */
    public long countEndEntitiesUsingCertificateProfile(final String certificateProfileName) {
        int certificateprofileid = certificateProfileSession.getCertificateProfileId(certificateProfileName);
        CertificateProfile certprofile = this.certificateProfileSession.getCertificateProfile(certificateProfileName);
        if (certprofile.getType() == SecConst.CERTTYPE_ENDENTITY) {
            return countEndEntitiesUsingCertificateProfile(certificateprofileid);
        } else {
            return 0;
        }       
    }
    
    /**
     * Returns a count of all end entities using a certain certificate profile 
     * 
     * @param certificateprofileid the ID of the certificate profile
     * @return the number of end entities found
     */
    private long countEndEntitiesUsingCertificateProfile(final int certificateprofileid) {
        return userAdminSession.countEndEntitiesUsingCertificateProfile(certificateprofileid);
    }
  
    
    /**
     * Check if certificate profile is in use by any end entity
     * 
     * @param certificateProfileName the name of the sought profile
     * @return a list of end entity names using the sought profile
     * @throws CertificateProfileDoesNotExistException if sought certificate profile was not found.
     * @throws ExcessiveResultsException on a query returning +100 results
     */
    public List<String> getEndEntitiesUsingCertificateProfile(final String certificateProfileName) throws CertificateProfileDoesNotExistException, ExcessiveResultsException {
        int certificateprofileid = certificateProfileSession.getCertificateProfileId(certificateProfileName);
        CertificateProfile certprofile = this.certificateProfileSession.getCertificateProfile(certificateProfileName);
        if (certprofile == null) {
            throw new CertificateProfileDoesNotExistException(certificateProfileName + " was not found.");
        } else {
            if ((certprofile.getType() == SecConst.CERTTYPE_ENDENTITY) || (certprofile.getType() == SecConst.CERTTYPE_SUBCA)) {
                if(countEndEntitiesUsingCertificateProfile(certificateprofileid) < 100) {
                    return userAdminSession.findByCertificateProfileId(certificateprofileid);
                } else {
                    throw new ExcessiveResultsException("Excessive amount of end entities (+100) encountered.");
                }
            } else {
                return new ArrayList<String>();
            }
        }
    }

    /**
     * Check if certificate profile is in use by any end entity profile
     * 
     * @param certificateProfileName the name of the sought profile
     * @return a list of end entity profile names using the sought profile
     * @throws CertificateProfileDoesNotExistException if sought certificate profile was not found.
     */
    public List<String> getEndEntityProfilesUsingCertificateProfile(final String certificateProfileName) throws CertificateProfileDoesNotExistException {
        int certificateprofileid = certificateProfileSession.getCertificateProfileId(certificateProfileName);
        CertificateProfile certprofile = this.certificateProfileSession.getCertificateProfile(certificateProfileName); 
        if (certprofile == null) {
            throw new CertificateProfileDoesNotExistException(certificateProfileName + " was not found.");
        } else {
            if ((certprofile.getType() == SecConst.CERTTYPE_ENDENTITY) || (certprofile.getType() == SecConst.CERTTYPE_SUBCA)) {
                return endEntityProfileSession.getEndEntityProfilesUsingCertificateProfile(certificateprofileid);
            } else {
                return new ArrayList<String>();
            }
        }
    }
    
    /**
     * Check if certificate profile is in use by any hard token profile
     * 
     * @param certificateProfileName the name of the sought profile
     * @return a list of hard token profile names using the sought profile
     * @throws CertificateProfileDoesNotExistException if sought certificate profile was not found.
     */
    public List<String> getHardTokenTokensUsingCertificateProfile(final String certificateProfileName) throws CertificateProfileDoesNotExistException {
        int certificateprofileid = certificateProfileSession.getCertificateProfileId(certificateProfileName);
        CertificateProfile certprofile = this.certificateProfileSession.getCertificateProfile(certificateProfileName);
        if (certprofile == null) {
            throw new CertificateProfileDoesNotExistException(certificateProfileName + " was not found.");
        } else {
            if (certprofile.getType() == SecConst.CERTTYPE_ENDENTITY) {
                return hardtokensession.getHardTokenProfileUsingCertificateProfile(certificateprofileid);
            } else {
                return new ArrayList<String>();
            }
        }
    }
    
    /**
     * Check if certificate profile is in use by any CA
     * 
     * @param certificateProfileName the name of the sought profile
     * @return a list of CA names using the sought profile
     * @throws CertificateProfileDoesNotExistException if sought certificate profile was not found.
     */
    public List<String> getCaUsingCertificateProfile(final String certificateProfileName) throws CertificateProfileDoesNotExistException {
        int certificateprofileid = certificateProfileSession.getCertificateProfileId(certificateProfileName);  
        CertificateProfile certprofile = this.certificateProfileSession.getCertificateProfile(certificateProfileName);
        if (certprofile == null) {
            throw new CertificateProfileDoesNotExistException(certificateProfileName + " was not found.");
        } else {
            if (certprofile.getType() != SecConst.CERTTYPE_ENDENTITY) {
                return caadminsession.getCAsUsingCertificateProfile(certificateprofileid);
            } else {
                return new ArrayList<String>();
            }
        }
    }
    
    public void removeCertificateProfile(String certificateProfileName) throws Exception {
        certificateprofiles.removeCertificateProfile(certificateProfileName);
    }

    public void renameCertificateProfile(String oldname, String newname) throws CertificateProfileExistsException, AuthorizationDeniedException {
    	certificateprofiles.renameCertificateProfile(oldname, newname);
    }

    public void cloneCertificateProfile(String originalname, String newname) throws CertificateProfileExistsException, AuthorizationDeniedException {
    	certificateprofiles.cloneCertificateProfile(originalname, newname);
    }    
      
    public void createCRL(String issuerdn) throws CryptoTokenOfflineException, CAOfflineException  {      
        try {
            crlCreateSession.forceCRL(administrator, issuerdn.hashCode());
        } catch (CADoesntExistsException e) {
            throw new RuntimeException(e);
        } catch (AuthorizationDeniedException e) {
            throw new RuntimeException(e);
		}
    }

    public void createDeltaCRL(String issuerdn) throws CryptoTokenOfflineException, CAOfflineException {      
        try {
            crlCreateSession.forceDeltaCRL(administrator, issuerdn.hashCode());
        } catch (CADoesntExistsException e) {
            throw new RuntimeException(e);
        } catch (AuthorizationDeniedException e) {
            throw new RuntimeException(e);
		}
    }

    public int getLastCRLNumber(String  issuerdn) {
    	return crlStoreSession.getLastCRLNumber(issuerdn, false);      
    }

    /**
     * @param caInfo of the CA that has issued the CRL.
     * @param deltaCRL false for complete CRL info, true for delta CRLInfo
     * @return CRLInfo of last CRL by CA or null if no CRL exists.
     */
	public CRLInfo getLastCRLInfo(CAInfo caInfo, boolean deltaCRL) {
		final String issuerdn;// use issuer DN from CA certificate. Might differ from DN in CAInfo.
		{
			final Collection<Certificate> certs = caInfo.getCertificateChain();
			final Certificate cacert = !certs.isEmpty() ? (Certificate)certs.iterator().next(): null;
			issuerdn = cacert!=null ? CertTools.getSubjectDN(cacert) : null;
		}
		return crlStoreSession.getLastCRLInfo(issuerdn, deltaCRL);          
	}

    /* Returns certificate profiles as a CertificateProfiles object */
    public CertificateProfileDataHandler getCertificateProfileDataHandler(){
      return certificateprofiles;
    }
    
    public HashMap<Integer, String> getAvailablePublishers() {
      return publishersession.getPublisherIdToNameMap();
    }
    
    public int getPublisherQueueLength(int publisherId) {
    	return publisherqueuesession.getPendingEntriesCountForPublisher(publisherId);
    }
    
    public int[] getPublisherQueueLength(int publisherId, int[] intervalLower, int[] intervalUpper) {
    	return publisherqueuesession.getPendingEntriesCountForPublisherInIntervals(publisherId, intervalLower, intervalUpper);
    }
    
    public PublisherDataHandler getPublisherDataHandler() {    
    	return this.publisherdatahandler;
    }
    
    public CADataHandler getCADataHandler(){
      return cadatahandler;   
    }
    
    public CAInfoView getCAInfo(String name) throws Exception{
      return cadatahandler.getCAInfo(name);   
    }

    public CAInfoView getCAInfo(int caid) throws Exception{
      return cadatahandler.getCAInfo(caid);   
    }    
    
    public void saveRequestInfo(CAInfo cainfo){
    	this.cainfo = cainfo;
    }
    
    public CAInfo getRequestInfo(){
    	return this.cainfo;
    }
    
	public void saveRequestData(byte[] request){
		this.request = request;
	}
    
	public byte[] getRequestData(){
		return this.request;
	}    
	
	public String getRequestDataAsString() throws Exception{
		String returnval = null;	
		if(request != null ){
			returnval = RequestHelper.BEGIN_CERTIFICATE_REQUEST_WITH_NL
			+ new String(Base64.encode(request))
			+ RequestHelper.END_CERTIFICATE_REQUEST_WITH_NL;  
		}      
		return returnval;
	}
    
	public void saveProcessedCertificate(Certificate cert){
		this.processedcert =cert;
	}

	public Certificate getProcessedCertificate(){
		return this.processedcert;
	}    

	public String getProcessedCertificateAsString() throws Exception{
		String returnval = null;	
		if(request != null ){
			byte[] b64cert = Base64.encode(this.processedcert.getEncoded());
			returnval = RequestHelper.BEGIN_CERTIFICATE_WITH_NL;
			returnval += new String(b64cert);
			returnval += RequestHelper.END_CERTIFICATE_WITH_NL;  	    
		}      
		return returnval;
	}

	public String republish(CertificateView certificatedata) throws AuthorizationDeniedException {
		String returnval = "CERTREPUBLISHFAILED";
		int certificateProfileId = CertificateProfileConstants.CERTPROFILE_NO_PROFILE;
		String username = null;
		String password = null;
		String dn = null;
		ExtendedInformation ei = null;
		final Certificate certificate = certificatedata.getCertificate();
		final CertReqHistory certreqhist = certreqhistorysession.retrieveCertReqHistory(administrator, CertTools.getSerialNumber(certificate), CertTools.getIssuerDN(certificate));
		if (certreqhist != null) {
			// First try to look up all info using the Certificate Request History from when the certificate was issued
			// We need this since the certificate subjectDN might be a subset of the subjectDN in the template
			certificateProfileId = certreqhist.getUserDataVO().getCertificateProfileId();
			username = certreqhist.getUserDataVO().getUsername();
			password = certreqhist.getUserDataVO().getPassword();
			dn = certreqhist.getUserDataVO().getCertificateDN();
			ei = certreqhist.getUserDataVO().getExtendedinformation();
		}
		final CertificateInfo certinfo = certificatesession.getCertificateInfo(CertTools.getFingerprintAsString(certificate));
		if (certinfo != null) {
			// If we are missing Certificate Request History for this certificate, we can at least recover some of this info
			if (certificateProfileId == CertificateProfileConstants.CERTPROFILE_NO_PROFILE) {
				certificateProfileId = certinfo.getCertificateProfileId();
			}
			if (username == null) {
				username = certinfo.getUsername();
			}
			if (dn == null) {
				dn = certinfo.getSubjectDN();
			}
		}
		if (certificateProfileId == CertificateProfileConstants.CERTPROFILE_NO_PROFILE) {
			// If there is no cert req history and the cert profile was not defined in the CertificateData row, so we can't do anything about it..
			returnval = "CERTREQREPUBLISHFAILED";
		} else {
			final CertificateProfile certprofile = certificateProfileSession.getCertificateProfile(certificateProfileId);
			if (certprofile != null) {
				if (certprofile.getPublisherList().size() > 0) {
					if (publishersession.storeCertificate(administrator, certprofile.getPublisherList(), certificatedata.getCertificate(), username, password, dn,
							certinfo.getCAFingerprint(), certinfo.getStatus() , certinfo.getType(), certinfo.getRevocationDate().getTime(), certinfo.getRevocationReason(),
							certinfo.getTag(), certificateProfileId, certinfo.getUpdateTime().getTime(), ei)) {
						returnval = "CERTREPUBLISHEDSUCCESS";
					}
				} else {
					returnval = "NOPUBLISHERSDEFINED";
				}
			} else {
				returnval = "CERTPROFILENOTFOUND";
			}
		}
		return returnval; 
	}

	/** Class used to sort CertReq History by users modfifytime, with latest first*/
	private class CertReqUserCreateComparator implements Comparator<CertReqHistory> {
		@Override
		public int compare(CertReqHistory o1, CertReqHistory o2) {
			return 0 - (o1.getUserDataVO().getTimeModified().compareTo(o2.getUserDataVO().getTimeModified()));
		}
	}

	/**
	 * Returns a List of CertReqHistUserData from the certreqhist database in an collection sorted by timestamp.
	 */
	public List<CertReqHistory> getCertReqUserDatas(String username){
		List<CertReqHistory> history = this.certreqhistorysession.retrieveCertReqHistory(administrator, username);
		// Sort it by timestamp, newest first;
		Collections.sort(history, new CertReqUserCreateComparator());
		return history;
	}

	/**
	 *  Help functions used by edit certificate profile pages used to temporary
	 *  save a profile so things can be canceled later.
	 */
	public CertificateProfile getTempCertificateProfile(){
		return this.tempCertProfile;
	}

	public void setTempCertificateProfile(CertificateProfile profile){
		this.tempCertProfile = profile;
	}

	/** @return true if serial number unique indexing is supported by DB. */
	public boolean isUniqueIndexForSerialNumber() {
		return this.isUniqueIndex;
	}
}
