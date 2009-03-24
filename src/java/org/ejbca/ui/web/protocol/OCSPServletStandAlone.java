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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.KeyStore.PasswordProtection;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.EJBException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.ejbca.core.ejb.ServiceLocator;
import org.ejbca.core.ejb.ca.store.ICertificateStoreOnlyDataSessionLocal;
import org.ejbca.core.ejb.ca.store.ICertificateStoreOnlyDataSessionLocalHome;
import org.ejbca.core.model.InternalResources;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.ExtendedCAServiceNotActiveException;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.ExtendedCAServiceRequestException;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.IllegalExtendedCAServiceRequestException;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.OCSPCAServiceRequest;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.OCSPCAServiceResponse;
import org.ejbca.core.model.ca.crl.RevokedCertInfo;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.protocol.ocsp.CertificateCache;
import org.ejbca.core.protocol.ocsp.CertificateCacheStandalone;
import org.ejbca.core.protocol.ocsp.OCSPUtil;
import org.ejbca.ui.web.pub.cluster.ExtOCSPHealthCheck;
import org.ejbca.util.CertTools;
import org.ejbca.util.keystore.KeyTools;

/** 
 * Servlet implementing server side of the Online Certificate Status Protocol (OCSP)
 * For a detailed description of OCSP refer to RFC2560.
 * 
 * @web.servlet name = "OCSP"
 *              display-name = "OCSPServletStandAlone"
 *              description="Answers OCSP requests"
 *              load-on-startup = "1"
 *
 * @web.servlet-mapping url-pattern = "/ocsp"
 * @web.servlet-mapping url-pattern = "/ocsp/*"
 *
 * @web.servlet-init-param description="Directory name of the soft keystores. The signing keys will be fetched from all files in this directory. Valid formats of the files are JKS and PKCS12 (p12)."
 *   name="softKeyDirectoryName"
 *   value="${ocsp.keys.dir}"
 *
 * @web.servlet-init-param description="The password for the all the soft keys of the OCSP responder."
 *   name="keyPassword"
 *   value="${ocsp.keys.keyPassword}"
 *
 * @web.servlet-init-param description="The password to all soft keystores."
 *   name="storePassword"
 *   value="${ocsp.keys.storePassword}"
 *
 * @web.servlet-init-param description="The password for all keys stored on card."
 *   name="cardPassword"
 *   value="${ocsp.keys.cardPassword}"
 *
 * @web.servlet-init-param description="The class that implements card signing of the OCSP response."
 *   name="hardTokenClassName"
 *   value="${ocsp.hardToken.className}"
 *
 * @web.servlet-init-param description="P11 shared library path name."
 *   name="sharedLibrary"
 *   value="${ocsp.p11.sharedLibrary}"
 *
 * @web.servlet-init-param description="P11 password."
 *   name="p11password"
 *   value="${ocsp.p11.p11password}"
 *
 * @web.servlet-init-param description="P11 slot number."
 *   name="slot"
 *   value="${ocsp.p11.slot}"
 *
 * @web.resource-ref
 *  name="${datasource.jndi-name-prefix}${datasource.jndi-name}"
 *  type="javax.sql.DataSource"
 *  auth="Container"
 *  
 * @web.ejb-local-ref
 *  name="ejb/CertificateStoreOnlyDataSessionLocal"
 *  type="Session"
 *  link="CertificateStoreOnlyDataSession"
 *  home="org.ejbca.core.ejb.ca.store.ICertificateStoreOnlyDataSessionLocalHome"
 *  local="org.ejbca.core.ejb.ca.store.ICertificateStoreOnlyDataSessionLocal"
 *
 * @author Lars Silven PrimeKey
 * @version  $Id$
 */
public class OCSPServletStandAlone extends OCSPServletBase implements IHealtChecker {

    /**
     * 
     */
    private static final long serialVersionUID = -7093480682721604160L;
    static final private Logger m_log = Logger.getLogger(OCSPServletStandAlone.class);
    /** Internal localization of logs and errors */
    private static final InternalResources intres = InternalResources.getInstance();

    private String mKeystoreDirectoryName;
    private String mKeyPassword;
    private String mStorePassword;
    private CardKeys mCardTokenObject;
	private final Map mSignEntity;
	private final Map mNewSignEntity;
    private ICertificateStoreOnlyDataSessionLocal m_certStore = null;
    private String mSlot;
    private String mSharedLibrary;
    private String mP11Password;
    private boolean mIsIndex;

    public OCSPServletStandAlone() {
        super();
        mSignEntity = new ConcurrentHashMap();
        mNewSignEntity = new HashMap();
    }
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            mSharedLibrary = config.getInitParameter("sharedLibrary");
            if ( mSharedLibrary!=null && mSharedLibrary.length()>0 ) {
                final String slot = config.getInitParameter("slot");
                final char firstChar = slot!=null && slot.length()>0 ? slot.charAt(0) : '\0';
                if ( firstChar=='i'||firstChar=='I' ) {
                    mSlot = slot.substring(1);
                    mIsIndex = true;
                } else {
                    mSlot = slot;
                    mIsIndex = false;
                }
                mP11Password = config.getInitParameter("p11password");
            } else {
                mSlot = null;
            }
			m_log.debug("sharedLibrary is: "+mSharedLibrary);

            mKeyPassword = config.getInitParameter("keyPassword");
            if ( mKeyPassword==null || mKeyPassword.length()==0 ) {
                throw new ServletException("no keystore password given");
            }
            mStorePassword = config.getInitParameter("storePassword");
            if ( mCardTokenObject==null ) {
                final String hardTokenClassName = config.getInitParameter("hardTokenClassName");
                if ( hardTokenClassName!=null && hardTokenClassName.length()>0 ) {
                    String sCardPassword = config.getInitParameter("cardPassword");
                    sCardPassword = sCardPassword!=null ? sCardPassword.trim() : null;
                    if ( sCardPassword!=null && sCardPassword.length()>0 ) {
                        try {
                            mCardTokenObject = (CardKeys)OCSPServletStandAlone.class.getClassLoader().loadClass(hardTokenClassName).newInstance();
                            mCardTokenObject.autenticate(sCardPassword);
                        } catch( ClassNotFoundException e) {
                    		String iMsg = intres.getLocalizedMessage("ocsp.classnotfound", hardTokenClassName);
                            m_log.info(iMsg);
                        }
                    } else {
                		String iMsg = intres.getLocalizedMessage("ocsp.nocardpwd");
                        m_log.info(iMsg);
                    }
                } else {
            		String iMsg = intres.getLocalizedMessage("ocsp.nohwsigningclass");
            		m_log.info(iMsg);
                }
            }
            if ( mStorePassword==null || mStorePassword.length()==0 )
                mStorePassword = mKeyPassword;
            
            mKeystoreDirectoryName = config.getInitParameter("softKeyDirectoryName");
            m_log.debug("softKeyDirectoryName is: "+mKeystoreDirectoryName);
            if ( mKeystoreDirectoryName!=null && mKeystoreDirectoryName.length()>0 ) {
                ExtOCSPHealthCheck.setHealtChecker(this);
            } else {
        		String errMsg = intres.getLocalizedMessage("ocsp.errornovalidkeys");
            	throw new ServletException(errMsg);
            }
            
    		// Load OCSP responders private keys into cache in init to speed things up for the first request
            loadPrivateKeys(m_adm);	
            
        } catch( ServletException e ) {
            throw e;
        } catch (Exception e) {
    		String errMsg = intres.getLocalizedMessage("ocsp.errorinitialize");
            m_log.error(errMsg, e);
            throw new ServletException(e);
        }
        
    }
    
    /**
     * Returns the certificate data only session bean
     */
    private synchronized ICertificateStoreOnlyDataSessionLocal getStoreSessionOnlyData(){
    	if(m_certStore == null){	
    		try {
                ServiceLocator locator = ServiceLocator.getInstance();
                ICertificateStoreOnlyDataSessionLocalHome castorehome =
                    (ICertificateStoreOnlyDataSessionLocalHome)locator.getLocalHome(ICertificateStoreOnlyDataSessionLocalHome.COMP_NAME);
                m_certStore = castorehome.create();
    		}catch(Exception e){
    			throw new EJBException(e);      	  	    	  	
    		}
    	}
    	return m_certStore;
    }

    private X509Certificate[] getCertificateChain(X509Certificate cert, Admin adm) {
    	String issuerDN = CertTools.getIssuerDN(cert);
        RevokedCertInfo revokedInfo = isRevoked(adm, issuerDN, CertTools.getSerialNumber(cert));
        if ( revokedInfo==null ) {
    		String wMsg = intres.getLocalizedMessage("ocsp.signcertnotindb", CertTools.getSerialNumberAsString(cert), issuerDN);
            m_log.warn(wMsg);
            return null;
        }
        if ( revokedInfo.getReason()!=RevokedCertInfo.NOT_REVOKED ) {
    		String wMsg = intres.getLocalizedMessage("ocsp.signcertrevoked", CertTools.getSerialNumberAsString(cert), issuerDN);
            m_log.warn(wMsg);
            return null;
        }
        X509Certificate chain[] = null;
        final List list = new ArrayList();
        X509Certificate current = cert;
        while( true ) {
        	list.add(current);
        	if ( CertTools.isSelfSigned(current) ) {
        		chain = (X509Certificate[])list.toArray(new X509Certificate[0]);
        		break;
        	}
        	// Is there a CA certificate?
        	X509Certificate target = m_caCertCache.findLatestBySubjectDN(CertTools.getIssuerDN(current));
        	if (target != null) {
    			current = target;
        	} else {
        		break;        		
        	}
        }
        if ( chain==null ) {
    		String wMsg = intres.getLocalizedMessage("ocsp.signcerthasnochain", CertTools.getSerialNumberAsString(cert), issuerDN);
        	m_log.warn(wMsg);
        }
        return chain;
    }
    private boolean loadFromP11HSM(Admin adm) throws Exception {
    	m_log.trace(">loadFromP11HSM");
        if ( this.mSharedLibrary==null || this.mSharedLibrary.length()<1 ) {
        	m_log.trace("<loadFromP11HSM: no shared library");
            return false;        	
        }
        final P11ProviderHandler providerHandler = new P11ProviderHandler();
        final PasswordProtection pwp = providerHandler.getPwd();
        loadFromKeyStore(adm, providerHandler.getKeyStore(pwp), null, this.mSharedLibrary, providerHandler);
        pwp.destroy();
    	m_log.trace("<loadFromP11HSM");
        return true;
    }
    private boolean loadFromSWKeyStore(Admin adm, String fileName) {
    	m_log.trace(">loadFromSWKeyStore");
    	boolean ret = false;
        try {
            KeyStore keyStore;
            try {
                keyStore = KeyStore.getInstance("JKS");
                keyStore.load(new FileInputStream(fileName), mStorePassword.toCharArray());
            } catch( IOException e ) {
                keyStore = KeyStore.getInstance("PKCS12", "BC");
                keyStore.load(new FileInputStream(fileName), mStorePassword.toCharArray());
            }
            loadFromKeyStore(adm, keyStore, mKeyPassword, fileName, new SWProviderHandler());
            ret = true;
        } catch( Exception e ) {
            m_log.debug("Unable to load key file "+fileName+". Exception: "+e.getMessage());
        }
    	m_log.trace("<loadFromSWKeyStore");
        return ret;
    }
    private boolean signTest(PrivateKey privateKey, PublicKey publicKey, String alias, String providerName) throws Exception {
        final String sigAlgName = "SHA1withRSA";
        final byte signInput[] = "Lillan gick på vägen ut.".getBytes();
        final byte signBA[];
        final boolean result;{
            Signature signature = Signature.getInstance(sigAlgName, providerName);
            signature.initSign( privateKey );
            signature.update( signInput );
            signBA = signature.sign();
        }
        {
            Signature signature = Signature.getInstance(sigAlgName);
            signature.initVerify(publicKey);
            signature.update(signInput);
            result = signature.verify(signBA);
            m_log.debug("Signature test of key "+alias+
                        ": signature length " + signBA.length +
                        "; first byte " + Integer.toHexString(0xff&signBA[0]) +
                        "; verifying " + result);
        }
        return result;
    }
    private void loadFromKeyStore(Admin adm, KeyStore keyStore, String keyPassword,
                                  String errorComment, ProviderHandler providerHandler) throws KeyStoreException {
        final Enumeration eAlias = keyStore.aliases();
        while( eAlias.hasMoreElements() ) {
            final String alias = (String)eAlias.nextElement();
            try {
                final X509Certificate cert = (X509Certificate)keyStore.getCertificate(alias);
                if (m_log.isDebugEnabled()) {
                    m_log.debug("Trying to load signing keys for signer with subjectDN (EJBCA ordering) '"+CertTools.getSubjectDN(cert)+"', keystore alias '"+alias+"'");                	
                }
                final PrivateKeyFactory pkf = new PrivateKeyFactoryKeyStore(alias, keyPassword!=null ? keyPassword.toCharArray() : null, keyStore);
                if ( pkf.getKey()!=null && cert!=null && signTest(pkf.getKey(), cert.getPublicKey(), errorComment, providerHandler.getProviderName()) ) {
                    putSignEntity(pkf, cert, adm, providerHandler);
                } else {
                    if (m_log.isDebugEnabled()) {
                    	m_log.debug("Not adding a signEntity for: "+CertTools.getSubjectDN(cert));
                    }
                }
            } catch (Exception e) {
                String errMsg = intres.getLocalizedMessage("ocsp.errorgetalias", alias, errorComment);
                m_log.error(errMsg, e);
            }
        }
    }
    private boolean putSignEntity( PrivateKeyFactory keyFactory, X509Certificate cert, Admin adm, ProviderHandler providerHandler ) {
        if ( keyFactory==null || cert==null )
            return false;
        providerHandler.addKeyFactory(keyFactory);
        X509Certificate[] chain = getCertificateChain(cert, adm);
        if ( chain!=null ) {
            int caid = getCaid(chain[1]);
            SigningEntity oldSigningEntity = (SigningEntity)mSignEntity.get(new Integer(caid));
            if ( oldSigningEntity!=null && !oldSigningEntity.getCertificateChain().equals(chain) ) {
                String wMsg = intres.getLocalizedMessage("ocsp.newsigningkey", chain[1].getSubjectDN(), chain[0].getSubjectDN());
                m_log.warn(wMsg);
            }
            mNewSignEntity.put( new Integer(caid), new SigningEntity(chain, keyFactory, providerHandler) );
            m_log.debug("CA with ID "+caid+" now has a OCSP signing key.");
        }
        return true;
    }
    public String healthCheck() {
    	StringWriter sw = new StringWriter();
    	PrintWriter pw = new PrintWriter(sw);
        try {
        	loadPrivateKeys(m_adm);
            Iterator i = mSignEntity.values().iterator();
	    	while ( i.hasNext() ) {
	    		SigningEntity signingEntity = (SigningEntity)i.next();
	    		if ( !signingEntity.isOK() ) {
                    pw.println();
            		String errMsg = intres.getLocalizedMessage("ocsp.errorocspkeynotusable", signingEntity.getCertificateChain()[1].getSubjectDN(), signingEntity.getCertificateChain()[0].getSerialNumber().toString(16));
	    			pw.print(errMsg);
	    			m_log.error(errMsg);
	    		}
	    	}
		} catch (Exception e) {
    		String errMsg = intres.getLocalizedMessage("ocsp.errorloadsigningcerts");
            m_log.error(errMsg, e);
			pw.print(errMsg + ": "+e.getMessage());
		}
    	pw.flush();
    	return sw.toString();
    }
    private interface PrivateKeyFactory {
        /**
         * @return the key
         * @throws Exception
         */
        PrivateKey getKey() throws Exception;
        /**
         * @param keyStore sets key from keystore
         * @throws Exception
         */
        void set(KeyStore keyStore) throws Exception;
        /**
         * removes key
         */
        void clear();
		/**
		 * @return is key OK to use.
		 */
		boolean isOK();
    }
    private class PrivateKeyFactoryKeyStore implements PrivateKeyFactory {
        final private char password[];
        final private String alias;
        private PrivateKey privateKey;
        PrivateKeyFactoryKeyStore( String a, char pw[], KeyStore keyStore) throws Exception {
            this.alias = a;
            this.password = pw;
            set(keyStore);
        }
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.PrivateKeyFactory#set(java.security.KeyStore)
         */
        public void set(KeyStore keyStore) throws Exception {
            this.privateKey = keyStore!=null ? (PrivateKey)keyStore.getKey(this.alias, this.password) : null;
        }
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.PrivateKeyFactory#clear()
         */
        public void clear() {
            this.privateKey = null;
        }
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.PrivateKeyFactory#getKey()
         */
        public PrivateKey getKey() throws Exception {
            return this.privateKey;
        }
		/* (non-Javadoc)
		 * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.PrivateKeyFactory#isOK()
		 */
		public boolean isOK() {
			// SW checked when initialized
			return this.privateKey!=null;
		}
    }
    private class PrivateKeyFactoryCard implements PrivateKeyFactory {
        final private RSAPublicKey publicKey;
        PrivateKeyFactoryCard( RSAPublicKey key) {
            this.publicKey = key;
        }
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.PrivateKeyFactory#getKey()
         */
        public PrivateKey getKey() throws Exception {
            return OCSPServletStandAlone.this.mCardTokenObject.getPrivateKey(this.publicKey);
        }
		/* (non-Javadoc)
		 * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.PrivateKeyFactory#isOK()
		 */
		public boolean isOK() {
			return OCSPServletStandAlone.this.mCardTokenObject.isOK(this.publicKey);
		}
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.PrivateKeyFactory#clear()
         */
        public void clear() {
            // not used by cards.
        }
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.PrivateKeyFactory#set(java.security.KeyStore)
         */
        public void set(KeyStore keyStore) throws Exception {
            // not used by cards.
        }
    }
    private boolean putSignEntityCard( Object obj, Admin adm ) {
        if ( obj!=null && obj instanceof X509Certificate ) {
            X509Certificate cert = (X509Certificate)obj;
            PrivateKeyFactory keyFactory = new PrivateKeyFactoryCard((RSAPublicKey)cert.getPublicKey());
            putSignEntity( keyFactory, cert, adm, new CardProviderHandler() );
            m_log.debug("HW key added. Serial number: "+cert.getSerialNumber().toString(0x10));
            return true;
        }
        return false;
    }
    private void loadFromKeyCards(Admin adm, String fileName) {
    	m_log.trace(">loadFromKeyCards");
        final CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (java.security.cert.CertificateException e) {
            throw new Error(e);
        }
        String fileType = null;
        try {// read certs from PKCS#7 file
            final Collection c = cf.generateCertificates(new FileInputStream(fileName));
            if ( c!=null && !c.isEmpty() ) {
                Iterator i = c.iterator();
                while (i.hasNext()) {
                    if ( putSignEntityCard(i.next(), adm) )
                        fileType = "PKCS#7";
                }
            }
        } catch( Exception e) {
        }
        if ( fileType==null ) {
            try {// read concatenated certificate in PEM format
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
                while (bis.available() > 0) {
                    if ( putSignEntityCard(cf.generateCertificate(bis), adm) )
                        fileType="PEM";
                }
            } catch(Exception e){
            }
        }
        if ( fileType!=null ) {
            m_log.debug("Certificate(s) found in file "+fileName+" of "+fileType+".");
        } else {
            m_log.debug("File "+fileName+" has no cert.");
        }
    	m_log.trace("<loadFromKeyCards");
    }
    
    protected void loadPrivateKeys(Admin adm) throws Exception {
    	m_log.trace(">loadPrivateKeys");
    	// We will only load private keys if the cache time has run out
		if ( (mSignEntity != null) && (mSignEntity.size() > 0) && (mKeysValidTo > new Date().getTime()) ) {
	    	m_log.trace("<loadPrivateKeys: using cache");
			return;
		}
        mNewSignEntity.clear();
        loadFromP11HSM(adm);
        final File dir = mKeystoreDirectoryName!=null ? new File(mKeystoreDirectoryName) : null;
        if ( dir!=null && dir.isDirectory() ) {
            final File files[] = dir.listFiles();
            if ( files!=null && files.length>0 ) {
                for ( int i=0; i<files.length; i++ ) {
                    final String fileName = files[i].getCanonicalPath();
                    if ( !loadFromSWKeyStore(adm, fileName) )
                        loadFromKeyCards(adm, fileName);
                }
            } else {
            	m_log.debug("No files in directory: " + dir.getCanonicalPath());            	
                if ( mNewSignEntity.size()<1 ) {
                    throw new ServletException("No files in soft key directory: " + dir.getCanonicalPath());            	
                }
            }
        } else {
        	m_log.debug((dir != null ? dir.getCanonicalPath() : "null") + " is not a directory.");
            if ( mNewSignEntity.size()<1 ) {
                throw new ServletException((dir != null ? dir.getCanonicalPath() : "null") + " is not a directory.");
            }
        }
        // No P11 keys, there are files, but none are valid keys or certs for cards
        if ( mNewSignEntity.size()<1 ) {
        	String dirStr = (dir != null ? dir.getCanonicalPath() : "null");
            throw new ServletException("No valid keys in directory " + dirStr+", or in PKCS#11 keystore.");        	
        }
        // Replace old signEntity references with new ones or null if they no longer exist
        Iterator iterator = mSignEntity.keySet().iterator();
        while (iterator.hasNext()) {
        	Object key = iterator.next();
        	if (mNewSignEntity.get(key) != null) {
            	mSignEntity.put(key, mNewSignEntity.get(key));
        	} else {
        		mSignEntity.remove(key);
        	}
        }
        // Replace existing signEntity references and add new ones. (Yes, we have some overlap here..)
        iterator = mNewSignEntity.keySet().iterator();
        while (iterator.hasNext()) {
        	Object key = iterator.next();
        	mSignEntity.put(key, mNewSignEntity.get(key));
        }
        
        m_log.debug("We have keys, returning");
        
        // Update cache time
    	// If m_valid_time == 0 we set reload time to Long.MAX_VALUE, which should be forever, so the cache is never refreshed
        mKeysValidTo = m_valid_time>0 ? new Date().getTime()+m_valid_time : Long.MAX_VALUE;
    	m_log.trace("<loadPrivateKeys");
    }
    
    private interface ProviderHandler {
        /**
         * @return name of the provider if an provider is available otherwise null
         */
        String getProviderName();
        /**
         * @param keyFactory to be updated at reload
         */
        void addKeyFactory(PrivateKeyFactory keyFactory);
        /**
         * start a threads that tryes to reload the provider until it is none
         */
        void reload();
    }
    private class CardProviderHandler implements ProviderHandler {
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.ProviderHandler#getProviderName()
         */
        public String getProviderName() {
            return "PrimeKey";
        }
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.ProviderHandler#reload()
         */
        public void reload() {
            // not needed to reload.
        }
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.ProviderHandler#addKeyFactory(org.ejbca.ui.web.protocol.OCSPServletStandAlone.PrivateKeyFactory)
         */
        public void addKeyFactory(PrivateKeyFactory keyFactory) {
            // do nothing
        }
    }
    private class SWProviderHandler implements ProviderHandler {
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.ProviderHandler#getProviderName()
         */
        public String getProviderName() {
            return "BC";
        }
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.ProviderHandler#reload()
         */
        public void reload() {
            // no use reloading a SW provider
        }
        public void addKeyFactory(PrivateKeyFactory keyFactory) {
            // do nothing
            
        }
    }
    private class P11ProviderHandler implements ProviderHandler {
        private String name;
        private boolean isOK;
        final Set sKeyFacrory = new HashSet();
        P11ProviderHandler() throws IOException {
            addProvider();
            this.isOK = true;
        }
        private void addProvider() throws IOException {
            Provider provider = KeyTools.getP11Provider(OCSPServletStandAlone.this.mSlot,
                                                        OCSPServletStandAlone.this.mSharedLibrary,
                                                        OCSPServletStandAlone.this.mIsIndex, null);
            Security.addProvider( provider );
            this.name = provider.getName();
        }
        public KeyStore getKeyStore(PasswordProtection pwp) throws Exception {
            final KeyStore.Builder builder = KeyStore.Builder.newInstance("PKCS11",
                                                                          Security.getProvider(this.name),
                                                                          pwp);
            final KeyStore keyStore = builder.getKeyStore();
            m_log.debug("Loading key from slot '"+OCSPServletStandAlone.this.mSlot+"' using pin.");
            keyStore.load(null, null);
            return keyStore;
        }
        public PasswordProtection getPwd() {
            return new PasswordProtection( (OCSPServletStandAlone.this.mP11Password!=null && OCSPServletStandAlone.this.mP11Password.length()>0)? OCSPServletStandAlone.this.mP11Password.toCharArray():null );
        }
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.ProviderHandler#getProviderName()
         */
        public String getProviderName() {
            return this.isOK ? this.name : null;
        }
        private class Reloader implements Runnable {
            Reloader() {
                // nothing done
            }
            /* (non-Javadoc)
             * @see java.lang.Runnable#run()
             */
            public void run() {
                boolean isNotWorking = true;
                while ( isNotWorking ) {
                    Security.removeProvider(P11ProviderHandler.this.name);
                    {
                        final Iterator i = P11ProviderHandler.this.sKeyFacrory.iterator();
                        while ( i.hasNext() )
                            ((PrivateKeyFactory)i.next()).clear();
                    }
                    for (int i=0; i<5; i++) {
                        try {
                            synchronized(this) {
                                wait(100);
                            }
                        } catch (InterruptedException e1) {
                            throw new Error(e1);
                        }
                        System.runFinalization();
                    }
                    try {
                        addProvider();
                        final PasswordProtection pwp = getPwd();
                        KeyStore keyStore = getKeyStore(pwp);
                        pwp.destroy();
                        {
                            final Iterator i = P11ProviderHandler.this.sKeyFacrory.iterator();
                            while ( i.hasNext() )
                                ((PrivateKeyFactory)i.next()).set(keyStore);
                        }
                        isNotWorking = false;
                    } catch (Exception e) {
                        // do nothing
                    }
                }
                P11ProviderHandler.this.isOK  = true;
            }
        }
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.ProviderHandler#reload()
         */
        public synchronized void reload() {
            if ( this.isOK ) {
                this.isOK = false;
                new Thread(new Reloader()).start();
            }
        }
        /* (non-Javadoc)
         * @see org.ejbca.ui.web.protocol.OCSPServletStandAlone.ProviderHandler#addKeyFactory(org.ejbca.ui.web.protocol.OCSPServletStandAlone.PrivateKeyFactory)
         */
        public void addKeyFactory(PrivateKeyFactory keyFactory) {
            this.sKeyFacrory.add(keyFactory);
        }
    }
    private class SigningEntity {
        final private X509Certificate mChain[];
        final private PrivateKeyFactory mKeyFactory;
        final private ProviderHandler providerHandler;
        SigningEntity(X509Certificate c[], PrivateKeyFactory f, ProviderHandler ph) {
            this.mChain = c;
            this.mKeyFactory = f;
            this.providerHandler = ph;
        }
        OCSPCAServiceResponse sign( OCSPCAServiceRequest request) throws ExtendedCAServiceRequestException, IllegalExtendedCAServiceRequestException {
        	PrivateKey privKey;
            final String hsmErrorString = "HSM not functional";
			try {
				privKey = this.mKeyFactory.getKey();
                if ( privKey==null )
                    throw new ExtendedCAServiceRequestException(hsmErrorString);
            } catch (ExtendedCAServiceRequestException e) {
                this.providerHandler.reload();
                throw e;
            } catch (Exception e) {
                throw new ExtendedCAServiceRequestException(e);
            }
        	if ( this.providerHandler.getProviderName()==null )
                throw new ExtendedCAServiceRequestException(hsmErrorString);
            try {
                return OCSPUtil.createOCSPCAServiceResponse(request, privKey, this.providerHandler.getProviderName(), this.mChain);
            } catch( ExtendedCAServiceRequestException e) {
                this.providerHandler.reload();
                throw e;
            } catch( IllegalExtendedCAServiceRequestException e ) {
                throw e;
            } catch( Throwable e ) {
                this.providerHandler.reload();
                final ExtendedCAServiceRequestException e1 = new ExtendedCAServiceRequestException(hsmErrorString);
                e1.initCause(e);
                throw e1;
            }
        }
        boolean isOK() {
        	try {
				return this.mKeyFactory.isOK();
			} catch (Exception e) {
				m_log.info("Exception thrown when accessing the private key: ", e);
				return false;
			}
        }
        X509Certificate[] getCertificateChain() {
        	return this.mChain;
        }
    }

    protected Certificate findCertificateByIssuerAndSerno(Admin adm, String issuer, BigInteger serno) {
        return getStoreSessionOnlyData().findCertificateByIssuerAndSerno(adm, issuer, serno);
    }
    
    protected OCSPCAServiceResponse extendedService(Admin adm, int caid, OCSPCAServiceRequest request) throws ExtendedCAServiceRequestException,
                                                                                                    ExtendedCAServiceNotActiveException, IllegalExtendedCAServiceRequestException {
        SigningEntity se =(SigningEntity)mSignEntity.get(new Integer(caid));
        if ( se!=null ) {
            return se.sign(request);            
        }
        throw new ExtendedCAServiceNotActiveException("No ocsp signing key for caid "+caid);
    }

    protected RevokedCertInfo isRevoked(Admin adm, String name, BigInteger serialNumber) {
        return getStoreSessionOnlyData().isRevoked(adm, name, serialNumber);
    }
    
	protected CertificateCache createCertificateCache(Properties prop) {
		return new CertificateCacheStandalone(prop);
	}

}
