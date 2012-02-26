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

package org.ejbca.ui.cli.ca;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

import org.bouncycastle.asn1.x509.ReasonFlags;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authentication.tokens.UsernamePrincipal;
import org.cesecore.certificates.ca.CAInfo;
import org.cesecore.certificates.ca.CaSessionRemote;
import org.cesecore.certificates.certificate.CertificateInfo;
import org.cesecore.certificates.certificate.CertificateStoreSessionRemote;
import org.cesecore.certificates.certificate.InternalCertificateStoreSessionRemote;
import org.cesecore.certificates.crl.CrlStoreSessionRemote;
import org.cesecore.certificates.crl.RevokedCertInfo;
import org.cesecore.certificates.endentity.EndEntityConstants;
import org.cesecore.certificates.endentity.EndEntityInformation;
import org.cesecore.jndi.JndiHelper;
import org.cesecore.mock.authentication.tokens.TestAlwaysAllowLocalAuthenticationToken;
import org.cesecore.util.CertTools;
import org.ejbca.core.ejb.ra.CertificateRequestSessionRemote;
import org.ejbca.core.ejb.ra.UserAdminSessionRemote;
import org.ejbca.core.model.SecConst;
import org.ejbca.util.InterfaceCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * System test class for CaImportCRLCommand
 * 
 * @version $Id$
 */
public class CaImportCRLCommandTest {

    private static final String CA_NAME = "4711CRL";
    private static final String CRL_FILENAME = "4711CRL.crl";
    private static final String CA_DN = "CN=CLI Test CA 4711CRL,O=EJBCA,C=SE";
    private static final String[] CAINIT_ARGS = { "init", CA_NAME, "\""+CA_DN+"\"", "soft", "foo123", "1024", "RSA",
            "365", "null", "SHA1WithRSA" };
    private static final String[] CACREATECRL_ARGS = { "createcrl", CA_NAME };
    private static final String[] CAGETCRL_ARGS = { "getcrl", CA_NAME, CRL_FILENAME };
    private static final String[] CAIMPORTCRL_STRICT_ARGS = { "importcrl", CA_NAME, CRL_FILENAME, "STRICT" };
    private static final String[] CAIMPORTCRL_LENIENT_ARGS = { "importcrl", CA_NAME, CRL_FILENAME, "LENIENT" };
    private static final String[] CAIMPORTCRL_ADAPTIVE_ARGS = { "importcrl", CA_NAME, CRL_FILENAME, "ADAPTIVE" };

    private CaInitCommand caInitCommand;
    private CaCreateCrlCommand caCreateCrlCommand;
    private CaGetCrlCommand caGetCrlCommand;
    private CaImportCRLCommand caImportCrlCommand;
    
    private AuthenticationToken admin = new TestAlwaysAllowLocalAuthenticationToken(new UsernamePrincipal("CaImportCRLCommandTest"));

    private CaSessionRemote caSession = InterfaceCache.getCaSession();
    private InternalCertificateStoreSessionRemote internalCertStoreSession = JndiHelper.getRemoteSession(InternalCertificateStoreSessionRemote.class);
    private CrlStoreSessionRemote crlSession = InterfaceCache.getCrlStoreSession();
    private CertificateRequestSessionRemote certReqSession = InterfaceCache.getCertficateRequestSession();
    private CertificateStoreSessionRemote certStoreSession = InterfaceCache.getCertificateStoreSession();
    private UserAdminSessionRemote userAdminSession = InterfaceCache.getUserAdminSession();

    @Before
    public void setUp() throws Exception {
        caInitCommand = new CaInitCommand();
        caCreateCrlCommand = new CaCreateCrlCommand();
        caGetCrlCommand = new CaGetCrlCommand();
        caImportCrlCommand = new CaImportCRLCommand();
        
        try {
            File f = new File(CRL_FILENAME);
            if (f.exists()) {
                f.delete();
            }
            caSession.removeCA(admin, caInitCommand.getCAInfo(admin, CA_NAME).getCAId());
        } catch (Exception e) {
            // Ignore.

        }
    }

    @After
    public void tearDown() throws Exception {
        File f = new File(CRL_FILENAME);
        f.deleteOnExit();
    }

    /**
     * Test trivial happy path for execute, i.e, create an ordinary CA.
     * 
     * @throws Exception on serious error
     */
    @Test
    public void testCreateAndImportCRL() throws Exception {
        String fingerprint = null;
        final String testUsername = "4711CRLUSER";
        try {
            caInitCommand.execute(CAINIT_ARGS);
            assertNotNull("Test CA was not created.", caSession.getCAInfo(admin, CA_NAME));
            CAInfo cainfo = caSession.getCAInfo(admin, CA_NAME);
            int no = crlSession.getLastCRLNumber(cainfo.getSubjectDN(), false);
            System.out.println(no);
            caCreateCrlCommand.execute(CACREATECRL_ARGS);
            int noafter = crlSession.getLastCRLNumber(cainfo.getSubjectDN(), false);
            System.out.println(noafter);
            assertEquals("A new CRL was not created", no+1, noafter);
            File f = new File(CRL_FILENAME);
            assertFalse("CRL file already exists.", f.exists());
            caGetCrlCommand.execute(CAGETCRL_ARGS);
            assertTrue("Get CRL command failed, no file exists.", f.exists());
            // Now create a certificate that we can play with and run the commands
            EndEntityInformation userdata = new EndEntityInformation(testUsername, "CN=4711CRLUSER", cainfo.getCAId(), null, null, SecConst.USER_ENDUSER, SecConst.EMPTY_ENDENTITYPROFILE,
                    SecConst.CERTPROFILE_FIXED_ENDUSER, SecConst.TOKEN_SOFT_PEM, 0, null);
            userdata.setPassword("foo123");
            userdata.setStatus(EndEntityConstants.STATUS_NEW);
            byte[] p12 = certReqSession.processSoftTokenReq(admin, userdata, null, "512", "RSA", true);
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new ByteArrayInputStream(p12), userdata.getPassword().toCharArray());
            assertNotNull(keyStore);
            Enumeration<String> aliases = keyStore.aliases();
            String alias = aliases.nextElement();
            Certificate cert = keyStore.getCertificate(alias);
            if (CertTools.isSelfSigned(cert)) {
                // Ignore the CA cert and get another one
                alias = aliases.nextElement();
                cert = keyStore.getCertificate(alias);
            }
            assertEquals("CertTools.getSubjectDN: " + CertTools.getSubjectDN(cert) + " userdata.getDN:" + userdata.getDN(),
                    CertTools.getSubjectDN(cert), userdata.getDN());
            fingerprint = CertTools.getFingerprintAsString(cert);
            CertificateInfo info = certStoreSession.getCertificateInfo(fingerprint);
            assertEquals("Cert should not be revoked", info.getStatus(), SecConst.CERT_ACTIVE);
            caImportCrlCommand.execute(CAIMPORTCRL_STRICT_ARGS);
            caImportCrlCommand.execute(CAIMPORTCRL_LENIENT_ARGS);
            caImportCrlCommand.execute(CAIMPORTCRL_ADAPTIVE_ARGS);
            // Nothing should have happened to the certificate
            info = certStoreSession.getCertificateInfo(fingerprint);
            assertEquals("Cert should not be revoked", info.getStatus(), SecConst.CERT_ACTIVE);
            // Now revoke the certificate, create a new CRL and import it, nothing should happen still
            certStoreSession.setRevokeStatus(admin, cert, RevokedCertInfo.REVOCATION_REASON_CERTIFICATEHOLD, null);
            caCreateCrlCommand.execute(CACREATECRL_ARGS);
            caGetCrlCommand.execute(CAGETCRL_ARGS);
            caImportCrlCommand.execute(CAIMPORTCRL_STRICT_ARGS);
            caImportCrlCommand.execute(CAIMPORTCRL_LENIENT_ARGS);
            caImportCrlCommand.execute(CAIMPORTCRL_ADAPTIVE_ARGS);
            info = certStoreSession.getCertificateInfo(fingerprint);
            assertEquals("Cert should be revoked", info.getStatus(), SecConst.CERT_REVOKED);
            assertEquals("Revocation reasonn should be on hold", info.getRevocationReason(), RevokedCertInfo.REVOCATION_REASON_CERTIFICATEHOLD);
            // Now unrevoke the certificate and import the CRL, it should be revoked again
            certStoreSession.setRevokeStatus(admin, cert, RevokedCertInfo.NOT_REVOKED, null);
            info = certStoreSession.getCertificateInfo(fingerprint);
            assertEquals("Cert should not be revoked", info.getStatus(), SecConst.CERT_ACTIVE);
            // Strict will do it
            caImportCrlCommand.execute(CAIMPORTCRL_STRICT_ARGS);
            info = certStoreSession.getCertificateInfo(fingerprint);
            assertEquals("Cert should be revoked", info.getStatus(), SecConst.CERT_REVOKED);
            assertEquals("Revocation reason should be on hold", info.getRevocationReason(), RevokedCertInfo.REVOCATION_REASON_CERTIFICATEHOLD);
            // Now delete the certificate from the database an import the CRL, using ADAPTIVE a new certificate is created
            Certificate cert2 = certStoreSession.findCertificateByIssuerAndSerno(CertTools.getIssuerDN(cert), CertTools.getSerialNumber(cert));
            assertNotNull("Certificate should exist", cert2);
            internalCertStoreSession.removeCertificate(fingerprint);
            cert2 = certStoreSession.findCertificateByIssuerAndSerno(CertTools.getIssuerDN(cert), CertTools.getSerialNumber(cert));
            assertNull("Certificate should not exist", cert2);
            caImportCrlCommand.execute(CAIMPORTCRL_STRICT_ARGS);
            // Strict should not do anything because the cert does not exist
            cert2 = certStoreSession.findCertificateByIssuerAndSerno(CertTools.getIssuerDN(cert), CertTools.getSerialNumber(cert));
            assertNull("Certificate should not exist", cert2);
            // Lenient should not do anything because the cert does not exist
            caImportCrlCommand.execute(CAIMPORTCRL_LENIENT_ARGS);
            cert2 = certStoreSession.findCertificateByIssuerAndSerno(CertTools.getIssuerDN(cert), CertTools.getSerialNumber(cert));
            assertNull("Certificate should not exist", cert2);
            // Adaptive should do something because a dummy cert will be created
            caImportCrlCommand.execute(CAIMPORTCRL_ADAPTIVE_ARGS);
            cert2 = certStoreSession.findCertificateByIssuerAndSerno(CertTools.getIssuerDN(cert), CertTools.getSerialNumber(cert));
            assertNotNull("Certificate should exist", cert2);
            fingerprint = CertTools.getFingerprintAsString(cert2);
        } finally {
            caSession.removeCA(admin, caSession.getCAInfo(admin, CA_NAME).getCAId());
            userAdminSession.revokeAndDeleteUser(admin, testUsername, ReasonFlags.unused);
            internalCertStoreSession.removeCertificate(fingerprint);
        }
    }
}
