/*************************************************************************
 *                                                                       *
 *  EJBCA - Proprietary Modules: Enterprise Certificate Authority        *
 *                                                                       *
 *  Copyright (c), PrimeKey Solutions AB. All rights reserved.           *
 *  The use of the Proprietary Modules are subject to specific           * 
 *  commercial license terms.                                            *
 *                                                                       *
 *************************************************************************/
package org.ejbca.ssh.certificates;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.encoders.Hex;
import org.cesecore.certificates.ca.CAOfflineException;
import org.cesecore.certificates.ca.IllegalNameException;
import org.cesecore.certificates.ca.IllegalValidityException;
import org.cesecore.certificates.ca.InvalidAlgorithmException;
import org.cesecore.certificates.ca.ssh.SshCa;
import org.cesecore.certificates.certificate.CertificateConstants;
import org.cesecore.certificates.certificate.CertificateCreateException;
import org.cesecore.certificates.certificate.IllegalKeyException;
import org.cesecore.certificates.certificate.certextensions.CertificateExtensionException;
import org.cesecore.certificates.certificate.ssh.SshCertificateReader;
import org.cesecore.certificates.certificate.ssh.SshCertificateType;
import org.cesecore.certificates.certificate.ssh.SshExtension;
import org.cesecore.certificates.certificate.ssh.SshKeyException;
import org.cesecore.certificates.certificate.ssh.SshKeyFactory;
import org.cesecore.certificates.certificate.ssh.SshPublicKey;
import org.cesecore.certificates.certificateprofile.CertificateProfile;
import org.cesecore.certificates.certificateprofile.CertificateProfileConstants;
import org.cesecore.certificates.endentity.EndEntityConstants;
import org.cesecore.certificates.endentity.EndEntityInformation;
import org.cesecore.certificates.endentity.EndEntityType;
import org.cesecore.certificates.endentity.EndEntityTypes;
import org.cesecore.certificates.util.AlgorithmConstants;
import org.cesecore.certificates.util.AlgorithmTools;
import org.cesecore.keys.token.CryptoToken;
import org.cesecore.keys.token.CryptoTokenOfflineException;
import org.cesecore.keys.util.KeyTools;
import org.cesecore.util.CryptoProviderTools;
import org.ejbca.core.protocol.ssh.SshRequestMessage;
import org.ejbca.ssh.certificate.SshEcCertificate;
import org.ejbca.ssh.certificate.SshRsaCertificate;
import org.ejbca.ssh.certificate.signature.ec.EcCertificateSigner;
import org.ejbca.ssh.certificate.signature.ec.EcSigningAlgorithm;
import org.ejbca.ssh.certificate.signature.rsa.RsaCertificateSigner;
import org.ejbca.ssh.certificate.signature.rsa.RsaSigningAlgorithms;
import org.ejbca.ssh.keys.ec.SshEcKeyPair;
import org.ejbca.ssh.keys.ec.SshEcPublicKey;
import org.ejbca.ssh.keys.rsa.SshRsaKeyPair;
import org.ejbca.ssh.keys.rsa.SshRsaPublicKey;
import org.ejbca.ssh.util.SshCaTestUtils;
import org.ejbca.ssh.util.SshTestUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * SSH Certificate tests.
 */
public class SshCertificateUnitTest {

    /*
     * These are generated with the following commands:
     * 
     * To generate the CA and user keys
     * $ ssh-keygen -b 2048 -t rsa -f ca_key 
     * To generate the certificate: 
     * $ ssh-keygen -s ca_key -I ejbca -n ejbca0,ejbca1 -V +52w user_key.pub
     */
    
    private static final String SAMPLE_EC_P384_USER_CERT = "ecdsa-sha2-nistp384-cert-v01@openssh.com AAAAKGVjZHNhLXNoYTItbmlzdHAzODQtY2VydC12MDFAb3BlbnNzaC5jb20AAAAgkemEsC1/b2p3c9inkCkDu0lUJSjvQpPHCk8vjM5HbCwAAAAIbmlzdHAzODQAAABhBEhwk8QN5sLpxVZWrTWRr66RO0JUKnji6Ewi/vV5eFFVR7y0DnlrX1QbSKUPSOwaFWzupW9hPJqDmuQlb03amTI+4UgqAQHCfjJwRJsTSQxeGehJkr5jNnO2uqPtNHloGQAAAAAAAAAAAAAAAQAAAAVlamJjYQAAABQAAAAGZWpiY2EwAAAABmVqYmNhMQAAAABe1NvwAAAAAGC0vkAAAAAAAAAAggAAABVwZXJtaXQtWDExLWZvcndhcmRpbmcAAAAAAAAAF3Blcm1pdC1hZ2VudC1mb3J3YXJkaW5nAAAAAAAAABZwZXJtaXQtcG9ydC1mb3J3YXJkaW5nAAAAAAAAAApwZXJtaXQtcHR5AAAAAAAAAA5wZXJtaXQtdXNlci1yYwAAAAAAAAAAAAAAiAAAABNlY2RzYS1zaGEyLW5pc3RwMzg0AAAACG5pc3RwMzg0AAAAYQRIcJPEDebC6cVWVq01ka+ukTtCVCp44uhMIv71eXhRVUe8tA55a19UG0ilD0jsGhVs7qVvYTyag5rkJW9N2pkyPuFIKgEBwn4ycESbE0kMXhnoSZK+YzZztrqj7TR5aBkAAACDAAAAE2VjZHNhLXNoYTItbmlzdHAzODQAAABoAAAAMFDHrr4+NzOWQRA75pLuM+5mwM6XkhuHHW9BLbbNvTNcZY66Nmwmj7RHYxG3ONMPlQAAADBoes4FN8bO96qfFq0V6ow8F0SPN85O4ZlYb3by268+75P1ukTY+p1dAfKIF5pslTM=";

    private static final String SAMPLE_RSA_2048_USER_CERT = "ssh-rsa-cert-v01@openssh.com AAAAHHNzaC1yc2EtY2VydC12MDFAb3BlbnNzaC5jb20AAAAg5XCeFvniKnNbRZaclB83kTyF8zITCde4uRrv4mqesn4AAAADAQABAAABAQDiFqTBdKOWPeBeP1PiKSVy8ilfNChu5/6Z3iXu3Rdtg5ozu98IoAl4MtlklDdUDzvFkB+VPD/9gqHPKK8fTOhqgUPGoiCeZP3Ktr6NR53xd1QPQDeBvOMiYkPqQXziCQiVyL1WFzN616szrxsJ1Ni7WCHXcMTKOMruLv4es8FfB03wGDbBKVzMwo0JuZCicGg2pg/o8n9BPlzW6CvjUkmvUO3ycGKibPPFkiDgyuYynIbkMcmdShhY3XSOGB4UPeA3U4OiH6Z+09K9LqogWIcjxJeK0tObORd9QnQ4k1ba+/bbEfnFIQwLyzqXXPsPQ0Ud9upxVHD1lezRNE1DAIr9AAAAAAAAAAAAAAABAAAABWVqYmNhAAAAFAAAAAZlamJjYTAAAAAGZWpiY2ExAAAAAF7Q0HgAAAAAYLCytwAAAAAAAACCAAAAFXBlcm1pdC1YMTEtZm9yd2FyZGluZwAAAAAAAAAXcGVybWl0LWFnZW50LWZvcndhcmRpbmcAAAAAAAAAFnBlcm1pdC1wb3J0LWZvcndhcmRpbmcAAAAAAAAACnBlcm1pdC1wdHkAAAAAAAAADnBlcm1pdC11c2VyLXJjAAAAAAAAAAAAAAEXAAAAB3NzaC1yc2EAAAADAQABAAABAQC8ajEtOvKxSPYSRL+A1y7Ye0baYHaR65KkzaT3U5XugHKHusvVPsDlRfSl598TsMjPQhbJt0O1SefMvXCbqdj776PWok5I1ScnLKJWRKzreeslEJZdcKTOUoT9Y5sg/LxC3xXwhIz+yLm8SbvQ7yQvPMmmlg5ldwccC8/0cua/25Vrjm0JhRjgxny65s2bNkClXXLmtevhvlQ7rXMQhpGmg5th156Ny/BUac7CQPEnDkRkhfsH8zKuh0NX19Y/93bwLsI7z+zP7CJJ11C0CpZl5yi2/8vqZUkufjRu/TH78EnLdCE/bkKcn0yyahG5BTh9dInrSclgCSiEPOYojvzjAAABFAAAAAxyc2Etc2hhMi0yNTYAAAEAj4/d9pKGGReo7B1ZbVBtHD9ftBfa5fjyPnuM8qbEMtYWgbx/MlCqVf2CL6VfJe2lvsg4ZZWvHyq0XBucFQqVHMekHJ71CymAs5/boGF4efLo56Ck6FF7tqM4dYmcO0aWRRQt3DyC24bLUZ4ZcdkyAgEm4EbTj9nPyvzbR57VsP/p+6WlszrGAHPwBlZ6my0g3cwPJSxwdV0USfMUIWooMIXLS7ocVZ7a8y+HF6qC2FGYIXYSgJuBaG5jlfXOojfKwA4tzwdy3ZziHxW50WDRkPrw6ZnXeRantJfMOhJ7ol9NSt1WcdkiAHqzDbEUpOz9UF0aSwxtIQYI7ONgl29a7w==";

    private SshRsaCertificate sshRsaCertificate;
    private SshEcCertificate sshEcCertificate;

    private static KeyPair keysRSA1024 = null;
    private static KeyPair keysRSA2048 = null;
    private static KeyPair keysEC256 = null;
    private static KeyPair keysEC384 = null;
    private static KeyPair keysEC521 = null;
    private static CryptoToken cryptoTokenRSA1024;
    private static CryptoToken cryptoTokenEC256;
    private static CryptoToken cryptoTokenEC384;
    
    @BeforeClass
    public static void beforeClass() throws InvalidAlgorithmParameterException, CryptoTokenOfflineException {
        CryptoProviderTools.installBCProviderIfNotAvailable();
        keysRSA1024 = KeyTools.genKeys("1024", AlgorithmConstants.KEYALGORITHM_RSA);
        keysRSA2048 = KeyTools.genKeys("2048", AlgorithmConstants.KEYALGORITHM_RSA);
        keysEC256 = KeyTools.genKeys("secp256r1", AlgorithmConstants.KEYALGORITHM_EC);
        keysEC384 = KeyTools.genKeys("secp384r1", AlgorithmConstants.KEYALGORITHM_EC);
        keysEC521 = KeyTools.genKeys("secp521r1", AlgorithmConstants.KEYALGORITHM_EC);
        cryptoTokenRSA1024 = SshCaTestUtils.getNewCryptoTokenSignOnly("1024");
        cryptoTokenEC256 = SshCaTestUtils.getNewCryptoTokenSignOnly("secp256r1");
        cryptoTokenEC384 = SshCaTestUtils.getNewCryptoTokenSignOnly("secp384r1");
    }

    @Test
    public void rsaCertificate() throws InvalidAlgorithmParameterException, CertificateException,
            SshKeyException, InvalidKeyException, SignatureException {
        // Init sshRsaCertificate and signatureKeys
        initSshRsaCertificateAndItsKeyPair();
        byte[] signature = new RsaCertificateSigner(RsaSigningAlgorithms.SHA1).signPayload(sshRsaCertificate.encodeCertificateBody(),
                keysRSA2048.getPublic(), keysRSA2048.getPrivate(), BouncyCastleProvider.PROVIDER_NAME);
        assertEquals("Signature was the wrong size, cannot continue", 271, signature.length);
        sshRsaCertificate.setSignature(signature);
        assertTrue("SSH Certificate did not verify correctly", sshRsaCertificate.verify());
        byte[] exportedCert = sshRsaCertificate.encodeForExport();

        initiateKeyFactory("ssh-rsa", SshRsaPublicKey.class);

        SshRsaCertificate importedRsaCertificate = new SshRsaCertificate();
        importedRsaCertificate.init(exportedCert);
        assertTrue("SSH Certificate did not verify correctly", importedRsaCertificate.verify());
    }

    @Test
    public void rsaSha256Certificate() throws InvalidAlgorithmParameterException, CertificateException,
            InvalidKeyException, SignatureException {
        // Init sshRsaCertificate and signatureKeys
        initSshRsaCertificateAndItsKeyPair();
        byte[] signature = new RsaCertificateSigner(RsaSigningAlgorithms.SHA256).signPayload(sshRsaCertificate.encodeCertificateBody(),
                keysRSA2048.getPublic(), keysRSA2048.getPrivate(), BouncyCastleProvider.PROVIDER_NAME);
        assertEquals("Signature was the wrong size, cannot continue", 276, signature.length);
        sshRsaCertificate.setSignature(signature);
        assertTrue("SSH Certificate did not verify correctly", sshRsaCertificate.verify());
    }

    @Test
    public void rsaSha512Certificate() throws InvalidAlgorithmParameterException, CertificateException,
            InvalidKeyException, SignatureException {
        // Init sshRsaCertificate and signatureKeys
        initSshRsaCertificateAndItsKeyPair();
        byte[] signature = new RsaCertificateSigner(RsaSigningAlgorithms.SHA512).signPayload(sshRsaCertificate.encodeCertificateBody(),
                keysRSA2048.getPublic(), keysRSA2048.getPrivate(), BouncyCastleProvider.PROVIDER_NAME);
        assertEquals("Signature was the wrong size, cannot continue", 276, signature.length);
        sshRsaCertificate.setSignature(signature);
        assertTrue("SSH Certificate did not verify correctly", sshRsaCertificate.verify());
    }

    @Test
    public void ecP256Certificate() throws InvalidKeySpecException, InvalidAlgorithmParameterException,
            CertificateException, InvalidKeyException, SignatureException {
        // Init sshEcCertificate and signatureKeys
        initSshEcCertificateAndItsKeyPair(keysEC256, SshEcPublicKey.NISTP256, "secp256r1", false);
        byte[] signature = new EcCertificateSigner(EcSigningAlgorithm.SHA256).signPayload(sshEcCertificate.encodeCertificateBody(),
                keysEC256.getPublic(), keysEC256.getPrivate(), BouncyCastleProvider.PROVIDER_NAME);
        sshEcCertificate.setSignature(signature);
        assertTrue("SSH Certificate did not verify correctly", sshEcCertificate.verify());
    }

    @Test
    public void ecP256CertificateWithCustomExtension() throws InvalidKeySpecException, InvalidAlgorithmParameterException,
            CertificateException, InvalidKeyException, SignatureException {
        // Init sshEcCertificate and signatureKeys
        initSshEcCertificateAndItsKeyPair(keysEC256, SshEcPublicKey.NISTP256, "secp256r1", true);
        byte[] signature = new EcCertificateSigner(EcSigningAlgorithm.SHA256).signPayload(sshEcCertificate.encodeCertificateBody(),
                keysEC256.getPublic(), keysEC256.getPrivate(), BouncyCastleProvider.PROVIDER_NAME);
        sshEcCertificate.setSignature(signature);
        assertTrue("SSH Certificate did not verify correctly", sshEcCertificate.verify());
    }

    @Test
    public void ecP384Certificate() throws InvalidKeySpecException, InvalidAlgorithmParameterException,
            CertificateException, InvalidKeyException, SignatureException, SshKeyException {
        // Init sshCertificate and signatureKeys
        initSshEcCertificateAndItsKeyPair(keysEC384, SshEcPublicKey.NISTP384, "secp384r1", false, "127.0.0.1", "192.168.0.1");
        byte[] signature = new EcCertificateSigner(EcSigningAlgorithm.SHA384).signPayload(sshEcCertificate.encodeCertificateBody(),
                keysEC384.getPublic(), keysEC384.getPrivate(), BouncyCastleProvider.PROVIDER_NAME);
        sshEcCertificate.setSignature(signature);
        assertTrue("SSH Certificate did not verify correctly", sshEcCertificate.verify());
        byte[] exportedCert = sshEcCertificate.encodeForExport();
        initiateKeyFactory("ecdsa-sha2-nistp384", SshEcPublicKey.class);
        SshEcCertificate importedEcCertificate = new SshEcCertificate();
        importedEcCertificate.init(exportedCert);
        assertTrue("SSH Certificate did not verify correctly", importedEcCertificate.verify());
    }

    @Test
    public void ecP521Certificate() throws InvalidKeySpecException, InvalidAlgorithmParameterException,
            CertificateException, InvalidKeyException, SignatureException {
        // Init sshCertificate and signatureKeys
        initSshEcCertificateAndItsKeyPair(keysEC521, SshEcPublicKey.NISTP521, "secp521r1", false);
        byte[] signature = new EcCertificateSigner(EcSigningAlgorithm.SHA512).signPayload(sshEcCertificate.encodeCertificateBody(),
                keysEC521.getPublic(), keysEC521.getPrivate(), BouncyCastleProvider.PROVIDER_NAME);
        sshEcCertificate.setSignature(signature);
        assertTrue("SSH Certificate did not verify correctly", sshEcCertificate.verify());
    }

    /** Creates a CA with RSA keys and signature algorithm SHA1WithRSA, and creates a certificate, with RSA user keys, and verifies it 
     * @throws IOException if signature bytes from generated certificate can not be parsed correctly
     */ 
    @Test
    public void rsaSha1CARSAUser() throws InvalidAlgorithmParameterException, CryptoTokenOfflineException, InvalidAlgorithmException, OperatorCreationException, CertificateException, CAOfflineException, IllegalValidityException, IllegalNameException, CertificateCreateException, SignatureException, IllegalKeyException, CertificateExtensionException, SshKeyException, InvalidKeyException, IOException {
        // Create a standalone test CA
        final SshCa ca = SshCaTestUtils.createTestCA(cryptoTokenRSA1024, "rsaSha1CA", AlgorithmConstants.SIGALG_SHA1_WITH_RSA);
        
        // Issue a certificate from this standalone test CA
        final CertificateProfile cp = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
        cp.setType(CertificateConstants.CERTTYPE_SSH);
        final EndEntityInformation user = new EndEntityInformation("sshUnitTest", "CN=sshUnitTest", 666, null, null, new EndEntityType(EndEntityTypes.ENDUSER), 0, 0, EndEntityConstants.TOKEN_USERGEN, null);
        final Date notBefore = null, notAfter = null;
        final List<String> principals = Arrays.asList("ejbca0", "ejbca1");
        final SshRequestMessage sshRequestMessage = new SshRequestMessage(keysRSA1024.getPublic(), "keyID1", principals, null, null, "my comment");
        final Certificate cert = ca.generateCertificate(cryptoTokenRSA1024, user, sshRequestMessage, keysRSA1024.getPublic(), 0, notBefore, notAfter, cp, null, null, null);
        // Verify the certificate
        initiateKeyFactory("ssh-rsa", SshRsaPublicKey.class); // CA key is an RSA key
        SshRsaCertificate rsaCertificate = new SshRsaCertificate();
        rsaCertificate.init(cert.getEncoded());
        // Check that the algorithms is SHA-1 and CA key RSA1024
        assertEquals("CA key did not have the correct encoding algorithm.", AlgorithmConstants.KEYALGORITHM_RSA,
                rsaCertificate.getPublicKey().getAlgorithm());
        assertEquals("CA key was not RSA 1024", "1024", AlgorithmTools.getKeySpecification(rsaCertificate.getPublicKey()));
        final byte[] signatureBytes = rsaCertificate.getSignature();
        try (final SshCertificateReader signatureReader = new SshCertificateReader(signatureBytes)) {
            final String signaturePrefix = signatureReader.readString();
            assertEquals("Incorrect signature prefix", "ssh-rsa", signaturePrefix);            
        }
        assertTrue("SSH Certificate did not verify correctly", rsaCertificate.verify());
    }

    /** Creates a CA with RSA keys and signature algorithm SHA256WithRSA, and creates a user certificate with RSA user keys, and verifies it 
     * @throws IOException if signature bytes from generated certificate can not be parsed correctly
     */ 
    @Test
    public void rsaSha256CARSAUser() throws InvalidAlgorithmParameterException, CryptoTokenOfflineException, InvalidAlgorithmException, OperatorCreationException, CertificateException, CAOfflineException, IllegalValidityException, IllegalNameException, CertificateCreateException, SignatureException, IllegalKeyException, CertificateExtensionException, SshKeyException, InvalidKeyException, IOException {
        // Create a standalone test CA
        final SshCa ca = SshCaTestUtils.createTestCA(cryptoTokenRSA1024, "rsaSha256CA", AlgorithmConstants.SIGALG_SHA256_WITH_RSA);
        
        // Issue a certificate from this standalone test CA
        final CertificateProfile cp = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
        cp.setType(CertificateConstants.CERTTYPE_SSH);
        final EndEntityInformation user = new EndEntityInformation("sshUnitTest", "CN=sshUnitTest", 666, null, null, new EndEntityType(EndEntityTypes.ENDUSER), 0, 0, EndEntityConstants.TOKEN_USERGEN, null);
        final Date notBefore = null, notAfter = null;
        final List<String> principals = Arrays.asList("ejbca0", "ejbca1");
        final SshRequestMessage sshRequestMessage = new SshRequestMessage(keysRSA1024.getPublic(), "keyID1", principals, null, null, "my comment");
        final Certificate cert = ca.generateCertificate(cryptoTokenRSA1024, user, sshRequestMessage, keysRSA1024.getPublic(), 0, notBefore, notAfter, cp, null, null, null);
        // Verify the certificate
        initiateKeyFactory("ssh-rsa", SshRsaPublicKey.class); // CA key is an RSA key
        SshRsaCertificate rsaCertificate = new SshRsaCertificate();
        rsaCertificate.init(cert.getEncoded());
        // Check that the algorithms is SHA-256 and CA key RSA1024
        assertEquals("CA key did not have the correct encoding algorithm.", AlgorithmConstants.KEYALGORITHM_RSA,
                rsaCertificate.getPublicKey().getAlgorithm());
        assertEquals("User key was not RSA 1024", "1024", AlgorithmTools.getKeySpecification(rsaCertificate.getPublicKey()));
        final byte[] signatureBytes = rsaCertificate.getSignature();
        try (final SshCertificateReader signatureReader = new SshCertificateReader(signatureBytes)) {
            final String signaturePrefix = signatureReader.readString();
            assertEquals("Incorrect signature prefix", "rsa-sha2-256", signaturePrefix);            
        }
        assertTrue("SSH Certificate did not verify correctly", rsaCertificate.verify());
    }

    /** Creates a CA with RSA keys and signature algorithm SHA512WithRSA, and creates a user certificate with RSA user keys, and verifies it 
     * @throws IOException if signature bytes from generated certificate can not be parsed correctly
     */ 
    @Test
    public void rsaSha512CARSAUser() throws InvalidAlgorithmParameterException, CryptoTokenOfflineException, InvalidAlgorithmException, OperatorCreationException, CertificateException, CAOfflineException, IllegalValidityException, IllegalNameException, CertificateCreateException, SignatureException, IllegalKeyException, CertificateExtensionException, SshKeyException, InvalidKeyException, IOException {
        // Create a standalone test CA
        final SshCa ca = SshCaTestUtils.createTestCA(cryptoTokenRSA1024, "rsaSha512CA", AlgorithmConstants.SIGALG_SHA512_WITH_RSA);
        
        // Issue a certificate from this standalone test CA
        final CertificateProfile cp = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
        cp.setType(CertificateConstants.CERTTYPE_SSH);
        final EndEntityInformation user = new EndEntityInformation("sshUnitTest", "CN=sshUnitTest", 666, null, null, new EndEntityType(EndEntityTypes.ENDUSER), 0, 0, EndEntityConstants.TOKEN_USERGEN, null);
        final Date notBefore = null, notAfter = null;
        final List<String> principals = Arrays.asList("ejbca0", "ejbca1");
        final SshRequestMessage sshRequestMessage = new SshRequestMessage(keysRSA2048.getPublic(), "keyID1", principals, null, null, "my comment");
        final Certificate cert = ca.generateCertificate(cryptoTokenRSA1024, user, sshRequestMessage, keysRSA2048.getPublic(), 0, notBefore, notAfter, cp, null, null, null);
        // Verify the certificate
        initiateKeyFactory("ssh-rsa", SshRsaPublicKey.class); // CA key is an RSA key
        SshRsaCertificate rsaCertificate = new SshRsaCertificate();
        rsaCertificate.init(cert.getEncoded());
        // Check that the algorithms is SHA-256 and CA key RSA1024
        assertEquals("CA key did not have the correct encoding algorithm.", AlgorithmConstants.KEYALGORITHM_RSA,
                rsaCertificate.getPublicKey().getAlgorithm());
        assertEquals("User key was not RSA 2048", "2048", AlgorithmTools.getKeySpecification(rsaCertificate.getPublicKey()));
        final byte[] signatureBytes = rsaCertificate.getSignature();
        try (final SshCertificateReader signatureReader = new SshCertificateReader(signatureBytes)) {
            final String signaturePrefix = signatureReader.readString();
            assertEquals("Incorrect signature prefix", "rsa-sha2-512", signaturePrefix);            
        }
        assertTrue("SSH Certificate did not verify correctly", rsaCertificate.verify());
    }

    /** Creates a CA with EC keys and signature algorithm SHA256WithECDSA, and creates a certificate, with EC user keys, and verifies it 
     * @throws IOException if signature bytes from generated certificate can not be parsed correctly
     */ 
    @Test
    public void ecSha256CAECUser() throws InvalidAlgorithmParameterException, CryptoTokenOfflineException, InvalidAlgorithmException, OperatorCreationException, CertificateException, CAOfflineException, IllegalValidityException, IllegalNameException, CertificateCreateException, SignatureException, IllegalKeyException, CertificateExtensionException, SshKeyException, InvalidKeyException, IOException {
        // Create a standalone test CA
        final SshCa ca = SshCaTestUtils.createTestCA(cryptoTokenEC256, "ecSha256CA", AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA);
        
        // Issue a certificate from this standalone test CA
        final CertificateProfile cp = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
        cp.setType(CertificateConstants.CERTTYPE_SSH);
        final EndEntityInformation user = new EndEntityInformation("sshUnitTest", "CN=sshUnitTest", 666, null, null, new EndEntityType(EndEntityTypes.ENDUSER), 0, 0, EndEntityConstants.TOKEN_USERGEN, null);
        final Date notBefore = null, notAfter = null;
        final List<String> principals = Arrays.asList("ejbca0", "ejbca1");
        final SshRequestMessage sshRequestMessage = new SshRequestMessage(keysEC256.getPublic(), "keyID1", principals, null, null, "my comment");
        final Certificate cert = ca.generateCertificate(cryptoTokenEC256, user, sshRequestMessage, keysEC256.getPublic(), 0, notBefore, notAfter, cp, null, null, null);
        // Verify the certificate
        initiateKeyFactory("ecdsa-sha2-nistp256", SshEcPublicKey.class);
        SshEcCertificate ecCertificate = new SshEcCertificate();
        ecCertificate.init(cert.getEncoded());
        assertTrue("SSH Certificate did not verify correctly", ecCertificate.verify());        
        // Check that the algorithms is SHA-1 and CA key RSA2048
        assertEquals("CA key did not have the correct encoding algorithm.", AlgorithmConstants.KEYALGORITHM_EC,
                ecCertificate.getPublicKey().getAlgorithm());
        assertEquals("User key was not EC P256", "secp256r1", AlgorithmTools.getKeySpecification(ecCertificate.getPublicKey()));
        final byte[] signatureBytes = ecCertificate.getSignature();
        try (final SshCertificateReader signatureReader = new SshCertificateReader(signatureBytes)) {
            final String signaturePrefix = signatureReader.readString();
            assertEquals("Incorrect signature prefix", "ecdsa-sha2-nistp256", signaturePrefix);            
        }
        assertTrue("SSH Certificate did not verify correctly", ecCertificate.verify());
    }

    /** Creates a CA with EC keys and signature algorithm SHA284WithECDSA, and creates a certificate, with EC user keys, and verifies it 
     * @throws IOException if signature bytes from generated certificate can not be parsed correctly
     */ 
    @Test
    public void ecSha384CAECUser() throws InvalidAlgorithmParameterException, CryptoTokenOfflineException, InvalidAlgorithmException, OperatorCreationException, CertificateException, CAOfflineException, IllegalValidityException, IllegalNameException, CertificateCreateException, SignatureException, IllegalKeyException, CertificateExtensionException, SshKeyException, InvalidKeyException, IOException {
        // Create a standalone test CA
        final SshCa ca = SshCaTestUtils.createTestCA(cryptoTokenEC384, "ecSha256CA", AlgorithmConstants.SIGALG_SHA384_WITH_ECDSA);
        
        // Issue a certificate from this standalone test CA
        final CertificateProfile cp = new CertificateProfile(CertificateProfileConstants.CERTPROFILE_FIXED_ENDUSER);
        cp.setType(CertificateConstants.CERTTYPE_SSH);
        final EndEntityInformation user = new EndEntityInformation("sshUnitTest", "CN=sshUnitTest", 666, null, null, new EndEntityType(EndEntityTypes.ENDUSER), 0, 0, EndEntityConstants.TOKEN_USERGEN, null);
        final Date notBefore = null, notAfter = null;
        final List<String> principals = Arrays.asList("ejbca0", "ejbca1");
        final SshRequestMessage sshRequestMessage = new SshRequestMessage(keysEC256.getPublic(), "keyID1", principals, null, null, "my comment");
        final Certificate cert = ca.generateCertificate(cryptoTokenEC384, user, sshRequestMessage, keysEC256.getPublic(), 0, notBefore, notAfter, cp, null, null, null);
        // Verify the certificate
        initiateKeyFactory("ecdsa-sha2-nistp384", SshEcPublicKey.class);
        SshEcCertificate ecCertificate = new SshEcCertificate();
        ecCertificate.init(cert.getEncoded());
        assertTrue("SSH Certificate did not verify correctly", ecCertificate.verify());        
        // Check that the algorithms is SHA-1 and CA key RSA2048
        assertEquals("CA key did not have the correct encoding algorithm.", AlgorithmConstants.KEYALGORITHM_EC,
                ecCertificate.getPublicKey().getAlgorithm());
        assertEquals("User key was not EC P256", "secp256r1", AlgorithmTools.getKeySpecification(ecCertificate.getPublicKey()));
        final byte[] signatureBytes = ecCertificate.getSignature();
        try (final SshCertificateReader signatureReader = new SshCertificateReader(signatureBytes)) {
            final String signaturePrefix = signatureReader.readString();
            assertEquals("Incorrect signature prefix", "ecdsa-sha2-nistp384", signaturePrefix);            
        }
        assertTrue("SSH Certificate did not verify correctly", ecCertificate.verify());
    }

    private void initiateKeyFactory(final String implementation, Class<? extends SshPublicKey> publicKeyImplementation) {
        // Since the key factory is loaded by the service loader, let's load it here for the sake of the test
        Map<String, Class<? extends SshPublicKey>> sshKeyImplementations = new HashMap<>();
        sshKeyImplementations.put(implementation, publicKeyImplementation);
        Field sshKeyImplementationsField;
        try {
            sshKeyImplementationsField = SshKeyFactory.class.getDeclaredField("sshKeyImplementations");
            sshKeyImplementationsField.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            sshKeyImplementationsField.set(SshKeyFactory.INSTANCE, sshKeyImplementations);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException("Test cannot continue", e);
        }
    }

    @Test
    public void ecCertificateWithIncorrectAlgorithm() throws InvalidKeySpecException, InvalidAlgorithmParameterException,
            CertificateException, SignatureException {
        SshEcKeyPair sshEcKeyPair = new SshEcKeyPair(SshEcPublicKey.NISTP521);
        KeyPair signatureKeys = KeyTools.genKeys("secp521r1", AlgorithmConstants.KEYALGORITHM_ECDSA);

        SshEcCertificate sshCertificate = new SshEcCertificate(sshEcKeyPair.getPublicKey(), "deadbeef".getBytes(), //nonce
                "1337", //Serial number
                SshCertificateType.USER, //Certificate type
                "foo", //Key ID
                new HashSet<>(Arrays.asList("ejbca", "ejbca1")), //Principals
                new Date(System.currentTimeMillis() / 1000), //validAfter
                new Date(System.currentTimeMillis() / 1000 + (60 * 60)), //validBefore
                SshTestUtils.getDefaultCriticalOptionsMap(), //Critical Options
                SshTestUtils.getAllSshExtensionsMap(), //Extensions
                new SshEcPublicKey((ECPublicKey) signatureKeys.getPublic()), "A comment.", null);
        // Siging algorithm is set to SHA384, which should fail with P521
        try {
            new EcCertificateSigner(EcSigningAlgorithm.SHA384).signPayload(sshCertificate.encodeCertificateBody(), signatureKeys.getPublic(),
                    signatureKeys.getPrivate(), BouncyCastleProvider.PROVIDER_NAME);
            fail("Creating the certificate signer with an incorrect signing algorithm should have failed.");
        } catch (InvalidKeyException e) {
            //NOPMD As expected
        }
    }

    @Test
    public void readSampleEcP384Certificate() throws CertificateEncodingException, SshKeyException, SecurityException,
            IllegalArgumentException, InvalidKeyException, SignatureException {
        initiateKeyFactory("ecdsa-sha2-nistp384", SshEcPublicKey.class);
        SshEcCertificate sshEcCertificate = new SshEcCertificate();
        sshEcCertificate.init(SAMPLE_EC_P384_USER_CERT.getBytes());
        assertTrue("Read SSH certificate did not verify", sshEcCertificate.verify());
        byte[] exportedCertificate = sshEcCertificate.encodeForExport();
        assertEquals("Exported certificate did not match original", SAMPLE_EC_P384_USER_CERT, new String(exportedCertificate));
        SshEcCertificate importedEcCertificate = new SshEcCertificate();
        importedEcCertificate.init(exportedCertificate);
        assertTrue("SSH Certificate did not verify correctly", importedEcCertificate.verify());
    }

    @Test
    public void readSampleRSA2048Certificate() throws CertificateEncodingException, SshKeyException, SecurityException,
            IllegalArgumentException, InvalidKeyException, SignatureException {
        initiateKeyFactory("ssh-rsa", SshRsaPublicKey.class);
        SshRsaCertificate sshRsaCertificate = new SshRsaCertificate();
        sshRsaCertificate.init(SAMPLE_RSA_2048_USER_CERT.getBytes());
        assertTrue("Read SSH certificate did not verify", sshRsaCertificate.verify());
    }

    public static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private Map<String, byte[]> getExtensionsMap(final boolean withCustomExtension) {
        final Map<String, byte[]> extensions = SshTestUtils.getSshExtensionsMapWithExclusions(SshExtension.NO_PRESENCE_REQUIRED);
        if(withCustomExtension) {
            extensions.put("customExtension", "customValue".getBytes());
        }
        return extensions;
    }

    private void initSshRsaCertificateAndItsKeyPair() throws InvalidAlgorithmParameterException {
        final SshRsaKeyPair sshRsaKeyPair = new SshRsaKeyPair(2048);
        sshRsaCertificate = new SshRsaCertificate(sshRsaKeyPair.getPublicKey(), "deadbeef".getBytes(), //nonce
                "1337", //Serial number
                SshCertificateType.USER, //Certificate type
                "foo", //Key ID
                new HashSet<>(Arrays.asList("ejbca", "ejbca1")), //Principals
                new Date(System.currentTimeMillis()), //validAfter
                new Date(System.currentTimeMillis() + (60L * 60L * 1000L)), //validBefore
                SshTestUtils.getDefaultCriticalOptionsMap(), //Critical Options
                getExtensionsMap(false), //Extensions
                new SshRsaPublicKey((RSAPublicKey) keysRSA2048.getPublic()), "A comment", null);
    }

    private void initSshEcCertificateAndItsKeyPair(
            final KeyPair signatureKeys, final String curveName, final String keySpec, final boolean withCustomExtension
    ) throws InvalidKeySpecException, InvalidAlgorithmParameterException {
        initSshEcCertificateAndItsKeyPair(signatureKeys, curveName, keySpec, withCustomExtension, "127.0.0.1");
    }

    private void initSshEcCertificateAndItsKeyPair(
            final KeyPair signatureKeys, final String curveName, final String keySpec, final boolean withCustomExtension, final String... sourceAddress
    ) throws InvalidKeySpecException, InvalidAlgorithmParameterException {
        SshEcKeyPair sshEcKeyPair = new SshEcKeyPair(curveName);
        sshEcCertificate = new SshEcCertificate(sshEcKeyPair.getPublicKey(), "deadbeef".getBytes(), //nonce
                "1337", //Serial number
                SshCertificateType.USER, //Certificate type
                "foo", //Key ID
                new HashSet<>(Arrays.asList("ejbca", "ejbca1")), //Principals
                new Date(System.currentTimeMillis()), //validAfter
                new Date(System.currentTimeMillis() + (60L * 60L * 1000L)), //validBefore
                SshTestUtils.getCriticalOptionsMap(sourceAddress), //Critical Options
                getExtensionsMap(withCustomExtension), //Extensions
                new SshEcPublicKey((ECPublicKey) signatureKeys.getPublic()), "A comment.", null);
    }
    
    /** Tests signing with a custom signature provider */
    @Test
    public void signatureProviderTest() throws InvalidAlgorithmParameterException, CertificateException,
            SshKeyException, InvalidKeyException, SignatureException {
        try {
            Security.addProvider(new SshJUnitTestProvider());
            KeyPair rsaKeys = KeyTools.genKeys(Integer.toString(1024), AlgorithmConstants.KEYALGORITHM_RSA);
            byte[] rsaSignature = new RsaCertificateSigner(RsaSigningAlgorithms.SHA256).signPayload("mypayloadrsa".getBytes(),
                    rsaKeys.getPublic(), rsaKeys.getPrivate(), SshJUnitTestProvider.NAME);
            // My dummy provider creates a fixed signature value based on the signatureAlgorithm.hashCode
            assertEquals("My provider should give predictable RSA signature", "0000000c7273612d736861322d3235360000000830060204ef4b1b73", Hex.toHexString(rsaSignature));
            KeyPair ecKeys = KeyTools.genKeys("secp256r1", AlgorithmConstants.KEYALGORITHM_EC);
            byte[] ecSignature = new EcCertificateSigner(EcSigningAlgorithm.SHA256).signPayload("mypayloadec".getBytes(),
                    ecKeys.getPublic(), ecKeys.getPrivate(), SshJUnitTestProvider.NAME);
            // My dummy provider creates a fixed signature value based on the signatureAlgorithm.hashCode
            assertEquals("My provider should give predictable EC signature", "0000001365636473612d736861322d6e6973747032353600000008000000044833a8c7", Hex.toHexString(ecSignature));
        } finally {
            Security.removeProvider(SshJUnitTestProvider.NAME);
        }
    }

    // Dummy signature provided used by signatureProviderTest, to verify that a specified provider is used to sign SSH certificates
    protected class SshJUnitTestProvider extends Provider {
        private static final long serialVersionUID = 1L;
        private static final String NAME = "SshJUnitTestProvider";
        private static final String info = "SSH JUnit Test Provider";
        public SshJUnitTestProvider() {
            super(NAME, 0.0, info);
            putService(new MySigningService(this, "Signature", "SHA256withRSA", MySignature.class.getName()));
            putService(new MySigningService(this, "Signature", "SHA256withECDSA", MySignature.class.getName()));
        }
    }
    private static class MyService extends Service {
        private static final Class<?>[] paramTypes = {Provider.class, String.class};

        MyService(Provider provider, String type, String algorithm,
                String className) {
            super(provider, type, algorithm, className, null, null);
        }        
        @Override
        public Object newInstance(Object param) throws NoSuchAlgorithmException {
            try {
                // get the Class object for the implementation class
                Class<?> clazz;
                Provider provider = getProvider();
                ClassLoader loader = provider.getClass().getClassLoader();
                if (loader == null) {
                    clazz = Class.forName(getClassName());
                } else {
                    clazz = loader.loadClass(getClassName());
                }
                // fetch the (Provider, String) constructor
                Constructor<?> cons = clazz.getConstructor(paramTypes);
                // invoke constructor and return the SPI object
                return cons.newInstance(provider, getAlgorithm());
            } catch (ReflectiveOperationException |  IllegalArgumentException | SecurityException e) {
                throw new NoSuchAlgorithmException("Could not instantiate service", e);
            }
        }
    }
    private static class MySigningService extends MyService {
        MySigningService(Provider provider, String type, String algorithm,
                String className) {
            super(provider, type, algorithm, className);
        }
    }
    private static class MySignature extends SignatureSpi {
        private String algorithm;
        @SuppressWarnings("unused")
        public MySignature(Provider provider, String algorithm) {
            super();
            this.algorithm = algorithm;
        }
        @Override
        protected void engineInitSign(PrivateKey arg0) throws InvalidKeyException {
        }
        @Override
        protected byte[] engineSign() throws SignatureException {
            // Fake a signature that can be decoded as ASN.1, which is needed for EC signatures
            ASN1EncodableVector vec = new ASN1EncodableVector();
            vec.add(new ASN1Integer(this.algorithm.hashCode()));
            ASN1Sequence seq = new DERSequence(vec);
            try {
                return seq.getEncoded();
            } catch (IOException e) {
                throw new SignatureException(e);
            }
        }
        @Override
        protected Object engineGetParameter(String arg0) throws InvalidParameterException {
            return null;
        }
        @Override
        protected void engineInitVerify(PublicKey arg0) throws InvalidKeyException {
        }
        @Override
        protected void engineSetParameter(String arg0, Object arg1) throws InvalidParameterException {
        }
        @Override
        protected void engineUpdate(byte arg0) throws SignatureException {
        }
        @Override
        protected void engineUpdate(byte[] arg0, int arg1, int arg2) throws SignatureException {
        }
        @Override
        protected boolean engineVerify(byte[] arg0) throws SignatureException {
            return false;
        }
    }

}
