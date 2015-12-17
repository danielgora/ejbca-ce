/*************************************************************************
 *                                                                       *
 *  CESeCore: CE Security Core                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.cesecore.keys.token;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.SecretKey;

import org.apache.log4j.Logger;

/**
 * Wrapper of a Java KeyStore to speed up key operations by caching key references.
 * 
 * Aliases and certificates are read when the class is initiated from the underlying key store.
 * PrivateKey and SecretKey objects are read and cached when the key specific protection is available (when used).
 * 
 * @version $Id: CachingKeyStoreWrapper.java 20890 2015-03-12 14:12:12Z mikekushner $
 */
public class CachingKeyStoreWrapper {
    
    /** Similar to Java's KeyStore.Entry */
    private class KeyStoreMapEntry {
        Key key;
        Certificate[] certificateChain;
    }
    
    private static final Logger log = Logger.getLogger(CachingKeyStoreWrapper.class);
    private final ReentrantLock updateLock = new ReentrantLock(false);
    private final KeyStore keyStore;
    private final boolean cachingEnabled;
    private HashMap<String, KeyStoreMapEntry> keyStoreCache = new HashMap<String, KeyStoreMapEntry>();
    
    /**
     * Wrap the key store object with optional caching of all entries.
     * 
     * @param keyStore the key store to wrap
     * @param cachingEnabled true will cache a list of all aliases, certificates and lazily cache private and secret keys when accessed
     * @throws KeyStoreException if the underlying key store cannot be accessed
     */
    public CachingKeyStoreWrapper(final KeyStore keyStore, final boolean cachingEnabled) throws KeyStoreException {
        this.keyStore = keyStore;
        this.cachingEnabled = cachingEnabled;
        if (log.isDebugEnabled()) {
            log.debug("cachingEnabled: " + cachingEnabled);
        }
        if (cachingEnabled) {
            // Load the whole public KeyStore content (aliases and certificate) into the cache
            final Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                if (log.isDebugEnabled()) {
                    log.debug("KeyStore has alias: " + alias);
                }
                final KeyStoreMapEntry keyStoreMapEntry = new KeyStoreMapEntry();
                // Try to load a certificate chain for a PrivateKey
                keyStoreMapEntry.certificateChain = keyStore.getCertificateChain(alias);
                if (keyStoreMapEntry.certificateChain==null) {
                    // See if there is a TrustedCertificateEntry instead
                    final Certificate certificate = keyStore.getCertificate(alias);
                    if (certificate!=null) {
                        keyStoreMapEntry.certificateChain = new Certificate[] { certificate };
                    }
                }
                this.keyStoreCache.put(fixBadUTF8(alias), keyStoreMapEntry);
            }
        }
    }

    /** @see java.security.KeyStore#getCertificate(String) */
    public Certificate getCertificate(final String alias) throws KeyStoreException {
        if (cachingEnabled) {
            final KeyStoreMapEntry keyStoreMapEntry = keyStoreCache.get(alias);
            if (keyStoreMapEntry==null) {
                return null;
            }
            if (keyStoreMapEntry.certificateChain==null || keyStoreMapEntry.certificateChain.length==0) {
                return null;
            }
            return keyStoreMapEntry.certificateChain[0];
        } else {
            return keyStore.getCertificate(alias);
        }
    }

    /** @see java.security.KeyStore#setCertificateEntry(String, Certificate) */
    public void setCertificateEntry(final String alias, final Certificate certificate) throws KeyStoreException {
        // Update the TrustedCertificateEntry in the real key store
        keyStore.setCertificateEntry(alias, certificate);
        if (cachingEnabled) {
            updateLock.lock();
            try {
                final HashMap<String, KeyStoreMapEntry> clone = new HashMap<String, KeyStoreMapEntry>(keyStoreCache);
                KeyStoreMapEntry keyStoreMapEntry = clone.get(alias);
                if (keyStoreMapEntry==null) {
                    keyStoreMapEntry = new KeyStoreMapEntry();
                }
                keyStoreMapEntry.certificateChain = new Certificate[] { certificate };
                clone.put(alias, keyStoreMapEntry);
                keyStoreCache = clone;
            } finally {
                updateLock.unlock();
            }
            if (log.isDebugEnabled()) {
                log.debug("Updated certificate entry in cache for alias: " + alias);
            }
        }
    }

    /** @see java.security.KeyStore#aliases() */
    public Enumeration<String> aliases() throws KeyStoreException {
        if (cachingEnabled) {
            return new Vector<String>(keyStoreCache.keySet()).elements();
        } else {
            return keyStore.aliases();
        }
    }

    /** @see java.security.KeyStore#store(OutputStream, char[]) */
    public void store(final OutputStream outputStream, final char[] password) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        keyStore.store(outputStream, password);
    }

    /** @see java.security.KeyStore#setKeyEntry(String, Key, char[], Certificate[]) */
    public void setKeyEntry(final String alias, final Key key, final char[] password, final Certificate[] chain) throws KeyStoreException {
        // Removal of old key is only needed for sun-p11 with none ASCII chars in the alias.
        // But it makes no harm to always do it and it should be fast.
        // If not done the entry will not be stored correctly in the p11 KeyStore.
        this.keyStore.deleteEntry(makeBadUTF8(alias));
        keyStore.setKeyEntry(alias, key, password, chain);
        if (cachingEnabled) {
            final KeyStoreMapEntry keyStoreMapEntry = new KeyStoreMapEntry();
            keyStoreMapEntry.certificateChain = chain;
            keyStoreMapEntry.key = key;
            updateLock.lock();
            try {
                final HashMap<String, KeyStoreMapEntry> clone = new HashMap<String, KeyStoreMapEntry>(keyStoreCache);
                clone.put(alias, keyStoreMapEntry);
                keyStoreCache = clone;
            } finally {
                updateLock.unlock();
            }
            if (log.isDebugEnabled()) {
                log.debug("Updated key entry in cache for alias: " + alias);
            }
        }
    }

    private static boolean isSunP11( final KeyStore keyStore ) {
        return keyStore.getProvider().getName().indexOf("SunPKCS11")==0;
    }

    /**
     * The Sun p11 implementation of the {@link KeyStore} returns aliases that
     * are badly encoded. This method fix this encoding.
     * @param orig a badly encoded alias.
     * @return a correct encoded alias.
     */
    private String fixBadUTF8(final String orig) {
        if ( !isSunP11(this.keyStore) ) {
            return orig;
        }
        try {
            final byte bvIn[] = orig.getBytes("UTF-16BE");
            final byte bvOut[] = new byte[bvIn.length/2];
            for ( int i=1; i<bvIn.length; i += 2) {
                bvOut[i/2] = (byte)(bvIn[i]&0xff);
            }
            return new String(bvOut, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-16BE and UTF-8 must be implemented for all JREs.");
        }
    }

    /**
     * When deleting a key in the sun p11 implementation of {@link KeyStore} the
     * alias {@link String} must be altered due to an implementation bug.
     * @param orig the alias
     * @return modified alias to suit the sun p11.
     */
    private String makeBadUTF8(final String orig) {
        if ( !isSunP11(this.keyStore) ) {
            return orig;
        }
        try {
            final byte bvIn[] = orig.getBytes("UTF-8");
            final byte bvOut[] = new byte[bvIn.length*2];
            for ( int i=0; i<bvIn.length; i += 1) {
                bvOut[i*2] = 0;
                bvOut[i*2+1] = (byte)(bvIn[i]&0xff);
            }
            return new String(bvOut, "UTF-16BE");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-16BE and UTF-8 must be implemented for all JREs.");
        }
    }

    /** @see java.security.KeyStore#deleteEntry(String) */
    public void deleteEntry(final String alias) throws KeyStoreException {
        this.keyStore.deleteEntry(makeBadUTF8(alias));
        if (cachingEnabled) {
            updateLock.lock();
            try {
                final HashMap<String, KeyStoreMapEntry> clone = new HashMap<String, KeyStoreMapEntry>(keyStoreCache);
                clone.remove(alias);
                keyStoreCache = clone;
            } finally {
                updateLock.unlock();
            }
            if (log.isDebugEnabled()) {
                log.debug("Removed entry from cache for alias: " + alias);
            }
        }
    }

    /** @see java.security.KeyStore#getEntry(String, ProtectionParameter) */
    public Entry getEntry(final String alias, final ProtectionParameter protParam) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {
        if (cachingEnabled) {
            final KeyStoreMapEntry keyStoreMapEntry = keyStoreCache.get(alias);
            if (keyStoreMapEntry==null) {
                return null;
            }
            if (keyStoreMapEntry.key instanceof PrivateKey) {
                return new PrivateKeyEntry((PrivateKey)keyStoreMapEntry.key, keyStoreMapEntry.certificateChain);
            }
            if (keyStoreMapEntry.key instanceof SecretKey) {
                return new SecretKeyEntry((SecretKey)keyStoreMapEntry.key);
            }
            if (keyStoreMapEntry.certificateChain!=null && keyStoreMapEntry.certificateChain.length>0) {
                return new TrustedCertificateEntry(keyStoreMapEntry.certificateChain[0]);
            }
            return null;
        } else {
            return keyStore.getEntry(alias, protParam);
        }
    }

    /** @see java.security.KeyStore#setEntry(String, Entry, ProtectionParameter) */
    public void setEntry(final String alias, final Entry entry, final ProtectionParameter protParam) throws KeyStoreException {
        keyStore.setEntry(alias, entry, protParam);
        if (cachingEnabled) {
            final KeyStoreMapEntry keyStoreMapEntry = new KeyStoreMapEntry();
            if (entry instanceof PrivateKeyEntry) {
                final PrivateKeyEntry privateKeyEntry = ((PrivateKeyEntry)entry);
                keyStoreMapEntry.certificateChain = privateKeyEntry.getCertificateChain();
                keyStoreMapEntry.key = privateKeyEntry.getPrivateKey();
            } else if (entry instanceof SecretKeyEntry) {
                keyStoreMapEntry.certificateChain = null;
                keyStoreMapEntry.key = ((SecretKeyEntry)entry).getSecretKey();
            } else {
                keyStoreMapEntry.certificateChain = new Certificate[] { ((TrustedCertificateEntry)entry).getTrustedCertificate() };
                keyStoreMapEntry.key = null;
            }
            updateLock.lock();
            try {
                final HashMap<String, KeyStoreMapEntry> clone = new HashMap<String, KeyStoreMapEntry>(keyStoreCache);
                clone.put(alias, keyStoreMapEntry);
                keyStoreCache = clone;
            } finally {
                updateLock.unlock();
            }
        }
    }

    /** @see java.security.KeyStore#getProvider() */
    public Provider getProvider() {
        return keyStore.getProvider();
    }

    /** @see java.security.KeyStore#getKey(String, char[]) */
    public Key getKey(final String alias, final char[] password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        if (cachingEnabled) {
            final KeyStoreMapEntry keyStoreMapEntry = keyStoreCache.get(alias);
            if (keyStoreMapEntry==null) {
                // If the alias exists it has a KeyStoreMapEntry. No need to query the key store here.
                return null;
            }
            if (keyStoreMapEntry.key==null) {
                updateLock.lock();
                try {
                    // Check if another thread has retrieved the key while we waited for the lock
                    if (keyStoreMapEntry.key==null) {
                        final Key key = keyStore.getKey(alias, password);
                        keyStoreMapEntry.key = key;
                    }
                } finally {
                    updateLock.unlock();
                }
                if (log.isDebugEnabled()) {
                    log.debug("Caching key for alias: " + alias);
                }
            }
            return keyStoreMapEntry.key;
        } else {
            return keyStore.getKey(alias, password);
        }
    }
}
