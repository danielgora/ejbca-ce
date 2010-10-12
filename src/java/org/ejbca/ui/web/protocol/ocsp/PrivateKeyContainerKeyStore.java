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

package org.ejbca.ui.web.protocol.ocsp;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;



/**
 * Implementation for java KeyStores. Could be SW or P11.
 * 
 * @author primelars
 * @version  $Id$
 */
class PrivateKeyContainerKeyStore implements PrivateKeyContainer {
    /**
     * 
     */
    final StandAloneSession standAloneSession;
    /**
     * Alias of the in the {@link KeyStore} for this key.
     */
    final String alias;
    /**
     * Certificate for this OCSP signing key.
     */
    X509Certificate certificate;
    /**
     * The OCSP signing key.
     */
    PrivateKey privateKey;
    /**
     * Object that runs a thread that is renewing a specified period before the certificate expires.
     */
    private KeyRenewer keyRenewer;
    /**
     * Key store holding this key.
     */
    KeyStore keyStore;
    /**
     * True if the key is updating. {@link #getKey()} is halted when true.
     */
    private boolean isUpdatingKey;
    /**
     * Name of the provider to be used for signing.
     */
    final String providerName;
    /**
     * Name of file where a newly generated key should be stored. Only used by SW keystores.
     */
    final String fileName;
    /**
     * Nr of users using the key.
     */
    private int nrOfusers = 0;
    /**
     * Constructs the key reference.
     * @param a sets {@link #alias}
     * @param pw sets {@link #password}
     * @param _keyStore sets {@link #keyStore}
     * @param cert sets {@link #certificate}
     * @param _providerName sets {@link #providerName}
     * @param _fileName Only used SW keystores.
     * @param standAloneSession TODO
     * @throws Exception
     */
    PrivateKeyContainerKeyStore( StandAloneSession standAloneSession, String a, char pw[], KeyStore _keyStore, X509Certificate cert, String _providerName, String _fileName) throws Exception {
        this.standAloneSession = standAloneSession;
        this.alias = a;
        this.certificate = cert;
        this.keyStore = _keyStore;
        this.providerName = _providerName;
        this.fileName = _fileName;
        set(pw);
    }
    /* (non-Javadoc)
     * @see org.ejbca.ui.web.protocol.OCSPServletStandAloneSession.PrivateKeyContainer#init(java.util.List, int)
     */
    public void init(List<X509Certificate> caChain, int caid) {
        destroy();
        if ( !this.standAloneSession.doKeyRenewal() ) {
            return;
        }
        if ( this.fileName!=null && this.standAloneSession.mStorePassword==null ) {
            StandAloneSession.m_log.error("Not possible to renew keys whith no stored keystore password for certificate with DN: "+caChain.get(0).getSubjectDN());
            return;
        }
        this.keyRenewer = new KeyRenewer(this, caChain, caid);
    }
    /**
     * Sets the private key.
     * @param pw The key password.
     * @throws Exception
     */
    private void set(char pw[]) throws Exception {
        this.privateKey = this.keyStore!=null ? (PrivateKey)this.keyStore.getKey(this.alias, pw) : null;
    }
    /* (non-Javadoc)
     * @see org.ejbca.ui.web.protocol.OCSPServletStandAloneSession.PrivateKeyContainer#set(java.security.KeyStore)
     */
    public void set(KeyStore _keyStore) throws Exception {
        this.keyStore = _keyStore;
        if ( this.fileName!=null && this.standAloneSession.mKeyPassword==null ) {
            throw new Exception("Key password must be configured when reloading SW keystore.");
        }
        set(this.standAloneSession.mKeyPassword.toCharArray());
    }
    /* (non-Javadoc)
     * @see org.ejbca.ui.web.protocol.OCSPServletStandAloneSession.PrivateKeyContainer#clear()
     */
    public void clear() {
        this.privateKey = null;
    }
    /* (non-Javadoc)
     * @see org.ejbca.ui.web.protocol.OCSPServletStandAloneSession.PrivateKeyContainer#getKey()
     */
    public PrivateKey getKey() throws Exception {
        synchronized(this) {
            while( this.isUpdatingKey ) {
                this.wait();
            }
        }
        if ( this.privateKey==null ) {
            return null;
        }
        synchronized(this.privateKey) {
            this.nrOfusers++;
            return this.privateKey;
        }
    }
    /* (non-Javadoc)
     * @see org.ejbca.ui.web.protocol.OCSPServletStandAloneSession.PrivateKeyContainer#releaseKey()
     */
    public void releaseKey() {
        if ( this.privateKey==null ) {
            StandAloneSession.m_log.warn("This should never ever happen. But if it does things may work afterwards anyway.");
            this.nrOfusers--;
            return;
        }
        synchronized(this.privateKey) {
            this.nrOfusers--;
            if ( this.nrOfusers<1 ) {
                this.privateKey.notifyAll();
            }
        }
    }
    /* (non-Javadoc)
     * @see org.ejbca.ui.web.protocol.OCSPServletStandAloneSession.PrivateKeyContainer#isOK()
     */
    public boolean isOK() {
        // SW checked when initialized
        return this.privateKey!=null;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PrivateKeyContainerKeyStore for key with alias "+this.alias+'.';
    }
    /* (non-Javadoc)
     * @see org.ejbca.ui.web.protocol.OCSPServletStandAloneSession.PrivateKeyContainer#getCertificate()
     */
    public X509Certificate getCertificate() {
        return this.certificate;
    }
    /* (non-Javadoc)
     * @see org.ejbca.ui.web.protocol.OCSPServletStandAloneSession.PrivateKeyContainer#destroy()
     */
    public void destroy() {
        if ( this.keyRenewer!=null ) {
            this.keyRenewer.shutdown();
        }
    }
    /**
     * Notify that key-generation has ended.
     */
    synchronized void keyGenerationFinished() {
        this.isUpdatingKey = false;
        this.notifyAll();
    }
    /**
     * Wait until key is not used. Used before key generation is started.
     */
    void waitUntilKeyIsNotUsed() {
        synchronized(this) {
            this.isUpdatingKey = true;
        }
        if ( this.privateKey==null ) {
            StandAloneSession.m_log.warn("This should never ever happen. But if it does things may work afterwards anyway.");
            if ( this.nrOfusers>0 ) {
                throw new Error("No private key in '"+this.toString()+"'. Still used by "+this.nrOfusers+" users.");
            }
            return;
        }
        synchronized(this.privateKey) {
            while( this.nrOfusers>0 ) {
                try {
                    this.privateKey.wait();
                } catch (InterruptedException e) {
                    new Error(e); // should never happen.
                }
            }
        }
    }
}