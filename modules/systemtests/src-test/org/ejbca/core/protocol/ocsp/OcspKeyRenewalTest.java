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
package org.ejbca.core.protocol.ocsp;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.log4j.Logger;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authorization.AuthorizationDeniedException;
import org.cesecore.certificates.ca.CADoesntExistsException;
import org.cesecore.certificates.ca.CAInfo;
import org.cesecore.certificates.ca.CaSessionRemote;
import org.cesecore.certificates.ca.X509CA;
import org.cesecore.certificates.certificate.CertificateStoreSessionRemote;
import org.cesecore.certificates.certificate.InternalCertificateStoreSessionRemote;
import org.cesecore.certificates.ocsp.OcspResponseGeneratorTestSessionRemote;
import org.cesecore.config.OcspConfiguration;
import org.cesecore.configuration.CesecoreConfigurationProxySessionRemote;
import org.cesecore.keys.token.CryptoTokenManagementSessionRemote;
import org.cesecore.keys.token.CryptoTokenOfflineException;
import org.cesecore.keys.token.CryptoTokenTestUtils;
import org.cesecore.mock.authentication.tokens.TestAlwaysAllowLocalAuthenticationToken;
import org.cesecore.roles.management.RoleInitializationSessionRemote;
import org.cesecore.roles.management.RoleManagementSessionRemote;
import org.cesecore.util.CertTools;
import org.cesecore.util.EjbRemoteHelper;
import org.ejbca.core.ejb.keybind.InternalKeyBinding;
import org.ejbca.core.ejb.keybind.InternalKeyBindingMgmtSessionRemote;
import org.ejbca.core.ejb.keybind.InternalKeyBindingStatus;
import org.ejbca.core.ejb.keybind.impl.AuthenticationKeyBinding;
import org.ejbca.core.ejb.keybind.impl.OcspKeyBinding;
import org.ejbca.core.protocol.ocsp.standalone.OcspKeyRenewalProxySessionRemote;
import org.ejbca.util.TraceLogMethodsRule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * @version $Id$
 *
 */
public class OcspKeyRenewalTest {

    private static final String CA_DN = "CN=OcspDefaultTestCA";
    private static final String TESTCLASSNAME = OcspKeyRenewalTest.class.getSimpleName();
    private static final AuthenticationToken authenticationToken = new TestAlwaysAllowLocalAuthenticationToken(TESTCLASSNAME);
    private static final Logger log = Logger.getLogger(OcspKeyRenewalTest.class);

    private static CesecoreConfigurationProxySessionRemote cesecoreConfigurationProxySession = EjbRemoteHelper.INSTANCE
            .getRemoteSession(CesecoreConfigurationProxySessionRemote.class, EjbRemoteHelper.MODULE_TEST);
    private OcspKeyRenewalProxySessionRemote ocspKeyRenewalProxySession = EjbRemoteHelper.INSTANCE
            .getRemoteSession(OcspKeyRenewalProxySessionRemote.class, EjbRemoteHelper.MODULE_TEST);
    private OcspResponseGeneratorTestSessionRemote standaloneOcspResponseGeneratorTestSession = EjbRemoteHelper.INSTANCE
        .getRemoteSession(OcspResponseGeneratorTestSessionRemote.class, EjbRemoteHelper.MODULE_TEST);
    private static final OcspResponseGeneratorTestSessionRemote ocspResponseGeneratorTestSession = EjbRemoteHelper.INSTANCE
            .getRemoteSession(OcspResponseGeneratorTestSessionRemote.class, EjbRemoteHelper.MODULE_TEST);
    private static final InternalCertificateStoreSessionRemote internalCertificateStoreSession = EjbRemoteHelper.INSTANCE
            .getRemoteSession(InternalCertificateStoreSessionRemote.class, EjbRemoteHelper.MODULE_TEST);       
    private static final InternalKeyBindingMgmtSessionRemote internalKeyBindingMgmtSession = EjbRemoteHelper.INSTANCE
            .getRemoteSession(InternalKeyBindingMgmtSessionRemote.class);
    private static final CryptoTokenManagementSessionRemote cryptoTokenManagementSession = EjbRemoteHelper.INSTANCE
            .getRemoteSession(CryptoTokenManagementSessionRemote.class);
    private static final CertificateStoreSessionRemote certificateStoreSession = EjbRemoteHelper.INSTANCE
            .getRemoteSession(CertificateStoreSessionRemote.class);       
    private static final CaSessionRemote caSession = EjbRemoteHelper.INSTANCE.getRemoteSession(CaSessionRemote.class);
    private static final RoleManagementSessionRemote roleManagementSession = EjbRemoteHelper.INSTANCE.getRemoteSession(RoleManagementSessionRemote.class);

    @Rule
    public TestRule traceLogMethodsRule = new TraceLogMethodsRule();

    private static X509CA x509ca;
    private static int cryptoTokenId;
    private static int ocspKeyBindingId;
    private static X509Certificate ocspSigningCertificate;
    private static X509Certificate caCertificate;
    private static int authenticationKeyBindingId;
    private static X509Certificate clientSSLCertificate;
    private static int managementCaId = 0;

    @BeforeClass
    public static void beforeClass() throws Exception {
        cleanup();
        ocspResponseGeneratorTestSession.reloadOcspSigningCache();
        
        x509ca = CryptoTokenTestUtils.createTestCA(authenticationToken, CA_DN);
        log.debug("OCSP CA Id: " + x509ca.getCAId() + " CA SubjectDN: " + x509ca.getSubjectDN());
        cryptoTokenId = CryptoTokenTestUtils.createCryptoToken(authenticationToken, TESTCLASSNAME);
        ocspKeyBindingId = OcspTestUtils.createInternalKeyBinding(authenticationToken, cryptoTokenId, OcspKeyBinding.IMPLEMENTATION_ALIAS,
                TESTCLASSNAME + "-ocsp");
        ocspSigningCertificate = OcspTestUtils.createOcspSigningCertificate(authenticationToken, ocspKeyBindingId, x509ca.getCAId());
        OcspTestUtils.updateInternalKeyBindingCertificate(authenticationToken, ocspKeyBindingId);
        OcspTestUtils.setInternalKeyBindingStatus(authenticationToken, ocspKeyBindingId, InternalKeyBindingStatus.ACTIVE);
        caCertificate = ProtocolOcspHttpStandaloneTest.createCaCertificate(authenticationToken, x509ca.getCACertificate());
        // Ensure that the new ocsp signing certificate is picked up by the cache
        ocspResponseGeneratorTestSession.reloadOcspSigningCache();
        // Reuse the same CryptoToken for the client SSL authentication
        authenticationKeyBindingId = OcspTestUtils.createInternalKeyBinding(authenticationToken, cryptoTokenId, AuthenticationKeyBinding.IMPLEMENTATION_ALIAS,
                TESTCLASSNAME + "-ssl");
        // We need to issue the SSL certificate from an issuer trusted by the server (AdminCA1/ManagementCA)
        try {
            managementCaId = caSession.getCAInfo(authenticationToken, "AdminCA1").getCAId();
        } catch (CADoesntExistsException e) {
            try {
                managementCaId = caSession.getCAInfo(authenticationToken, "ManagementCA").getCAId();
            } catch (CADoesntExistsException e2) {
                // Test relying on SSL will fail
            }
        }
        log.debug("SSL CA Id: " + managementCaId);
        if (managementCaId != 0) {
            clientSSLCertificate = OcspTestUtils.createClientSSLCertificate(authenticationToken, authenticationKeyBindingId, managementCaId);
            OcspTestUtils.updateInternalKeyBindingCertificate(authenticationToken, authenticationKeyBindingId);
            OcspTestUtils.setInternalKeyBindingStatus(authenticationToken, authenticationKeyBindingId, InternalKeyBindingStatus.ACTIVE);
            // Add authorization rules for this client SSL certificate
            final RoleInitializationSessionRemote roleInitSession = EjbRemoteHelper.INSTANCE.getRemoteSession(RoleInitializationSessionRemote.class, EjbRemoteHelper.MODULE_TEST);
            roleInitSession.initializeAccessWithCert(authenticationToken, TESTCLASSNAME, clientSSLCertificate);
        }
        // Set re-keying URL to our local instance
        cesecoreConfigurationProxySession.setConfigurationValue(OcspConfiguration.REKEYING_WSURL, "https://localhost:8443/ejbca/ejbcaws/ejbcaws");
    }
    
    @AfterClass
    public static void afterClass() throws Exception {
        cleanup();
    }
    
    private static void cleanup() throws Exception {
        try {
            roleManagementSession.remove(authenticationToken, TESTCLASSNAME);
        } catch (Exception e) {
            //Ignore any failures.
        }
        try {
            internalCertificateStoreSession.removeCertificate(ocspSigningCertificate);
        } catch (Exception e) {
            //Ignore any failures.
        }
        try {
            internalCertificateStoreSession.removeCertificate(caCertificate);
        } catch (Exception e) {
            //Ignore any failures.
        }
        try {
            internalCertificateStoreSession.removeCertificate(clientSSLCertificate);
        } catch (Exception e) {
            //Ignore any failures.
        }
        
        // Delete KeyBindings
        cleanupKeyBinding(TESTCLASSNAME + "-ssl"); // authentication key binding
        cleanupKeyBinding(TESTCLASSNAME + "-ocsp"); // ocsp key binding
        
        // Delete CryptoToken
        try {
            while (true) {
                Integer id = cryptoTokenManagementSession.getIdFromName(TESTCLASSNAME); 
                if (id == null) break;
                cryptoTokenManagementSession.deleteCryptoToken(authenticationToken, id);
            }
        } catch (Exception e) {
            //Ignore any failures.
        }
        
        // Delete CA
        try {
            while (true) {
                CAInfo info = caSession.getCAInfo(authenticationToken, CA_DN);
                caSession.removeCA(authenticationToken, info.getCAId());
            }
        } catch (Exception e) {
            // Get out of loop and ignore
        }
        
        // Ensure that the removed signing certificate is removed from the cache
        ocspResponseGeneratorTestSession.reloadOcspSigningCache();
    }
    
    private static void cleanupKeyBinding(String keybindingName) {
        try {
            // There can be more than one key binding if the test has failed multiple times
            while (true) {
                Integer keybindingId = internalKeyBindingMgmtSession.getIdFromName(keybindingName);
                if (keybindingId == null) break;
                internalKeyBindingMgmtSession.deleteInternalKeyBinding(authenticationToken, keybindingId);
            }
        } catch (Exception e) {
            //Ignore any failures.
        }
    }
    
    @Test
    public void testKeyRenewal() throws Exception {
        assertNotEquals("This test cannot run without a ManagementCA that issued the localhost SSL certificate.", 0, managementCaId);
        // Make renewal call through our proxy
        ocspKeyRenewalProxySession.renewKeyStores("CN=ocspTestSigner");
        // Check that back-end InternalKeyBinding has been updated
        final String oldFingerprint = CertTools.getFingerprintAsString(ocspSigningCertificate);
        InternalKeyBinding ocspKeyBindingInfo = internalKeyBindingMgmtSession.getInternalKeyBindingInfo(authenticationToken, ocspKeyBindingId);
        final String newFingerprint = ocspKeyBindingInfo.getCertificateId();
        final Certificate newOcspCertificate = certificateStoreSession.findCertificateByFingerprint(newFingerprint);
        assertNotEquals("The same certificate was returned after the renewal process. Key renewal failed", newFingerprint, oldFingerprint);
        // Check that OcspSigningCache has been updated
        final List<X509Certificate> cachedOcspCertificates = standaloneOcspResponseGeneratorTestSession.getCacheOcspCertificates();
        assertNotEquals("No OCSP certificates in cache after renewal!", 0, cachedOcspCertificates);
        boolean newCertificateExistsInCache = false;
        for (final X509Certificate cachedCertificate : cachedOcspCertificates) {
            if (CertTools.getFingerprintAsString(cachedCertificate).equals(newFingerprint)) {
                newCertificateExistsInCache = true;
                break;
            }
        }
        assertTrue("Renewed certificate does not exist in OCSP cache.", newCertificateExistsInCache);
        //Make sure that the new certificate is signed by the CA certificate
        try {
            newOcspCertificate.verify(caCertificate.getPublicKey());
        } catch (SignatureException e) {
            fail("The new signing certificate was not signed correctly.");
        }
    }
    
    @Test
    public void testAutomaticKeyRenewal() throws InvalidKeyException, KeyStoreException, CryptoTokenOfflineException, AuthorizationDeniedException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, InterruptedException {
        if (managementCaId == 0) {
            throw new Error("This test cannot run without a ManagementCA that issued the localhost SSL certificate.");
        }
        ocspKeyRenewalProxySession.setTimerToFireInOneSecond();
        //Race condition, the WS object takes about two years to materialize
        Thread.sleep(3000);
        //Timer should have fired, and we should see some new stuff.

        // Check that back-end InternalKeyBinding has been updated
        final String oldFingerprint = CertTools.getFingerprintAsString(ocspSigningCertificate);
        InternalKeyBinding ocspKeyBindingInfo = internalKeyBindingMgmtSession.getInternalKeyBindingInfo(authenticationToken, ocspKeyBindingId);
        final String newFingerprint = ocspKeyBindingInfo.getCertificateId();
        final Certificate newOcspCertificate = certificateStoreSession.findCertificateByFingerprint(newFingerprint);
        assertNotEquals("The same certificate was returned after the renewal process. Key renewal failed", newFingerprint, oldFingerprint);
        // Check that OcspSigningCache has been updated
        final List<X509Certificate> cachedOcspCertificates = standaloneOcspResponseGeneratorTestSession.getCacheOcspCertificates();
        assertNotEquals("No OCSP certificates in cache after renewal!", 0, cachedOcspCertificates);
        boolean newCertificateExistsInCache = false;
        for (final X509Certificate cachedCertificate : cachedOcspCertificates) {
            if (CertTools.getFingerprintAsString(cachedCertificate).equals(newFingerprint)) {
                newCertificateExistsInCache = true;
                break;
            }
        }
        assertTrue("Renewed certificate does not exist in OCSP cache.", newCertificateExistsInCache);
        //Make sure that the new certificate is signed by the CA certificate
        try {
            newOcspCertificate.verify(caCertificate.getPublicKey());
        } catch (SignatureException e) {
            fail("The new signing certificate was not signed correctly.");
        }
    }
    
}
