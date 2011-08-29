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

package org.ejbca.core.protocol.cmp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERGeneralizedTime;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.ocsp.RevokedInfo;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.ReasonFlags;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.X509KeyUsage;
import org.cesecore.core.ejb.authorization.AdminEntitySessionRemote;
import org.cesecore.core.ejb.authorization.AdminGroupSessionRemote;
import org.cesecore.core.ejb.ca.store.CertificateProfileSession;
import org.cesecore.core.ejb.ra.raadmin.EndEntityProfileSession;
import org.ejbca.config.CmpConfiguration;
import org.ejbca.core.EjbcaException;
import org.ejbca.core.ejb.authorization.AuthorizationSession;
import org.ejbca.core.ejb.ca.caadmin.CAAdminSessionRemote;
import org.ejbca.core.ejb.ca.caadmin.CaSession;
import org.ejbca.core.ejb.ca.caadmin.CaSessionRemote;
import org.ejbca.core.ejb.ca.sign.SignSessionRemote;
import org.ejbca.core.ejb.ca.store.CertificateStoreSession;
import org.ejbca.core.ejb.config.ConfigurationSessionBean;
import org.ejbca.core.ejb.config.ConfigurationSessionRemote;
import org.ejbca.core.ejb.ra.UserAdminSessionRemote;
import org.ejbca.core.model.AlgorithmConstants;
import org.ejbca.core.model.SecConst;
import org.ejbca.core.model.approval.ApprovalException;
import org.ejbca.core.model.approval.WaitingForApprovalException;
import org.ejbca.core.model.authorization.AdminEntity;
import org.ejbca.core.model.authorization.AdminGroup;
import org.ejbca.core.model.authorization.AuthorizationDeniedException;
import org.ejbca.core.model.ca.caadmin.CAInfo;
import org.ejbca.core.model.ca.crl.RevokedCertInfo;
import org.ejbca.core.model.log.Admin;
import org.ejbca.core.model.ra.NotFoundException;
import org.ejbca.core.model.ra.UserDataConstants;
import org.ejbca.core.model.ra.UserDataVO;
import org.ejbca.core.model.ra.raadmin.UserDoesntFullfillEndEntityProfile;
import org.ejbca.core.protocol.cmp.authentication.EndEntityCertificateAuthenticationModule;
import org.ejbca.core.protocol.cmp.authentication.HMACAuthenticationModule;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.ui.cli.batch.BatchMakeP12;
import org.ejbca.util.CertTools;
import org.ejbca.util.CryptoProviderTools;
import org.ejbca.util.InterfaceCache;
import org.ejbca.util.keystore.KeyTools;
import org.hibernate.ObjectNotFoundException;

import com.novosec.pkix.asn1.cmp.ErrorMsgContent;
import com.novosec.pkix.asn1.cmp.PKIBody;
import com.novosec.pkix.asn1.cmp.PKIHeader;
import com.novosec.pkix.asn1.cmp.PKIMessage;
import com.novosec.pkix.asn1.crmf.AttributeTypeAndValue;
import com.novosec.pkix.asn1.crmf.CRMFObjectIdentifiers;
import com.novosec.pkix.asn1.crmf.CertReqMessages;
import com.novosec.pkix.asn1.crmf.CertReqMsg;
import com.novosec.pkix.asn1.crmf.CertRequest;
import com.novosec.pkix.asn1.crmf.CertTemplate;
import com.novosec.pkix.asn1.crmf.OptionalValidity;
import com.novosec.pkix.asn1.crmf.POPOSigningKey;
import com.novosec.pkix.asn1.crmf.ProofOfPossession;

/**
 * This will test the different cmp authentication modules.
 * 
 * @version $Id$
 *
 */
public class AuthenticationModulesTest extends CmpTestCase {

    private static final Logger log = Logger.getLogger(AuthenticationModulesTest.class);
	
	private String username;
	private String userDN;
	private String issuerDN;
	private byte[] nonce;
	private byte[] transid;
	private int caid;
	private Certificate cacert;
	
    private Admin admin;
    private CAAdminSessionRemote caAdminSession = InterfaceCache.getCAAdminSession();
    private CaSessionRemote caSession = InterfaceCache.getCaSession();
    private UserAdminSessionRemote userAdminSession = InterfaceCache.getUserAdminSession();
    private SignSessionRemote signSession = InterfaceCache.getSignSession();
    private EndEntityProfileSession eeProfileSession = InterfaceCache.getEndEntityProfileSession();
    private ConfigurationSessionRemote confSession = InterfaceCache.getConfigurationSession();
    private CertificateStoreSession certSession = InterfaceCache.getCertificateStoreSession();
    private AuthorizationSession authorizationSession = InterfaceCache.getAuthorizationSession();
    private AdminGroupSessionRemote adminGroupSession = InterfaceCache.getAdminGroupSession();
    private AdminEntitySessionRemote adminEntitySession = InterfaceCache.getAdminEntitySession();
    
	public AuthenticationModulesTest(String arg0) {
		super(arg0);

		admin = new Admin(Admin.TYPE_RA_USER);
		
		username = "authModuleTestUser";
	    userDN = "CN="+username+", O=PrimeKey Solutions AB, C=SE, UID=foo123";
	    issuerDN = "CN=AdminCA1,O=EJBCA Sample,C=SE";
	    nonce = CmpMessageHelper.createSenderNonce();
	    transid = CmpMessageHelper.createSenderNonce();

        CryptoProviderTools.installBCProvider();
        setCAID();
        setCaCert();
        
		confSession.backupConfiguration();
	}

	public void test01HMACModule() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, IOException, InvalidAlgorithmParameterException {

		KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);
		
		PKIMessage msg = genCertReq(issuerDN, userDN, keys, cacert, nonce, transid, false, null, null, null, null);	
		assertNotNull("Generating CrmfRequest failed." + msg);
        PKIMessage req = protectPKIMessage(msg, false, "foo123", "mykeyid", 567);
        assertNotNull("Protecting PKIMessage with HMACPbe failed." + req);

        HMACAuthenticationModule hmac = new HMACAuthenticationModule("foo123");
        hmac.setCaInfo(caAdminSession.getCAInfo(admin, caid));
        hmac.setSession(admin, userAdminSession);
		boolean res = hmac.verify(req);
		assertTrue("Verifying the message authenticity using HMAC failed.", res);
		assertNotNull("HMAC returned null password." + hmac.getAuthenticationString());
		assertEquals("HMAC returned the wrong password", "foo123", hmac.getAuthenticationString());
	}
	
	public void test02EEModule() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException, IOException, InvalidAlgorithmParameterException,
	EjbcaException, javax.ejb.ObjectNotFoundException, AuthorizationDeniedException, UserDoesntFullfillEndEntityProfile, WaitingForApprovalException, Exception {

		KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);

		PKIMessage msg = genCertReq(issuerDN, userDN, keys, cacert, nonce, transid, false, null, null, null, null);	
		assertNotNull("Generating CrmfRequest failed." + msg);

		AlgorithmIdentifier pAlg = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha1WithRSAEncryption);
		msg.getHeader().setProtectionAlg(pAlg);
		msg.getHeader().setSenderKID(new DEROctetString(nonce));

		createUser("cmpTestAdmin", "CN=cmpTestAdmin,C=SE", "foo123");
		KeyPair admkeys = KeyTools.genKeys("1024", "RSA");
		Certificate admCert = signSession.createCertificate(admin, "cmpTestAdmin", "foo123", admkeys.getPublic());
		Admin adm = new Admin(admCert, "cmpTestAdmin", "cmpTestAdmin@primekey.se");
		setupAccessRights(adm);
		addExtraCert(msg, admCert);
		signPKIMessage(msg, admkeys);
		assertNotNull(msg);

		EndEntityCertificateAuthenticationModule eemodule = new EndEntityCertificateAuthenticationModule(caAdminSession.getCAInfo(admin, caid).getName());
		eemodule.setSession(admin, caAdminSession, certSession, authorizationSession, eeProfileSession);
		boolean res = eemodule.verify(msg);
		assertTrue("Verifying the message authenticity using EndEntityCertificate failed.", res);
		assertNotNull("EndEntityCertificate authentication module returned null password." + eemodule.getAuthenticationString());
		//Should be a random generated password
		assertNotSame("EndEntityCertificate authentication module returned the wrong password", "foo123", eemodule.getAuthenticationString());
	}
	
	public void test03HMACCrmfReq() throws Exception {
		assertFalse("Configurations have not been backed up before starting testing.", confSession.backupConfiguration());
		
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, CmpConfiguration.AUTHMODULE_HMAC);
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, CmpConfiguration.AUTHMODULE_HMAC));
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, "foo123");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, "foo123"));		
		confSession.updateProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra"));
		
		KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);
		
		PKIMessage msg = genCertReq(issuerDN, userDN, keys, cacert, nonce, transid, false, null, null, null, null);
		assertNotNull("Generating CrmfRequest failed." + msg);
        PKIMessage req = protectPKIMessage(msg, false, "foo123", "mykeyid", 567);
        assertNotNull("Protecting PKIMessage with HMACPbe failed.");
        
        final ByteArrayOutputStream bao = new ByteArrayOutputStream();
        final DEROutputStream out = new DEROutputStream(bao);
        out.writeObject(req);
        final byte[] ba = bao.toByteArray();
        // Send request and receive response
        final byte[] resp = sendCmpHttp(ba, 200);        
        checkCmpResponseGeneral(resp, issuerDN, userDN, cacert, req.getHeader().getSenderNonce().getOctets(), req.getHeader().getTransactionID().getOctets(), false, null);
        Certificate cert1 = checkCmpCertRepMessage(userDN, cacert, resp, req.getBody().getIr().getCertReqMsg(0).getCertReq().getCertReqId().getValue().intValue());
        assertNotNull("Crmf request did not return a certificate", cert1);
	}

	
	public void test04HMACRevReq() throws Exception {
		assertFalse("Configurations have not been backed up before starting testing.", confSession.backupConfiguration());
		
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, CmpConfiguration.AUTHMODULE_HMAC);
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, CmpConfiguration.AUTHMODULE_HMAC));
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, "foo123");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, "foo123"));		
		confSession.updateProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra"));
		
		Collection<Certificate> certs = certSession.findCertificatesBySubjectAndIssuer(admin, userDN, issuerDN);
		log.debug("Found " + certs.size() + " certificates for userDN \"" + userDN + "\"");
		Certificate cert = null, tmp=null;
		Iterator<Certificate> itr = certs.iterator();
		while(itr.hasNext()) {
			tmp = itr.next();
			if(!certSession.isRevoked(issuerDN, CertTools.getSerialNumber(tmp))) {
				cert = tmp;
				break;
			}
		}
		assertNotNull("Could not find a suitable certificate to revoke.", cert);
		
		
		PKIMessage msg = genRevReq(issuerDN, userDN, CertTools.getSerialNumber(cert), cacert, nonce, transid, false);
		assertNotNull("Generating RevocationRequest failed." + msg);
        PKIMessage req = protectPKIMessage(msg, false, "foo123", "mykeyid", 567);
        assertNotNull("Protecting PKIMessage with HMACPbe failed." + req);
        
        final ByteArrayOutputStream bao = new ByteArrayOutputStream();
        final DEROutputStream out = new DEROutputStream(bao);
        out.writeObject(req);
        final byte[] ba = bao.toByteArray();
        // Send request and receive response
        final byte[] resp = sendCmpHttp(ba, 200);        
        checkCmpResponseGeneral(resp, issuerDN, userDN, cacert, req.getHeader().getSenderNonce().getOctets(), req.getHeader().getTransactionID().getOctets(), false, null);
        int revStatus = checkRevokeStatus(issuerDN, CertTools.getSerialNumber(cert));
        assertNotSame("Revocation request failed to revoke the certificate", RevokedCertInfo.NOT_REVOKED, revStatus);
	}
	
	public void test04EECrmfReq() throws NoSuchAlgorithmException, EjbcaException, IOException, Exception  {
		assertFalse("Configurations have not been backed up before starting testing.", confSession.backupConfiguration());
		
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, CmpConfiguration.AUTHMODULE_ENDENTITY_CERTIFICATE);
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, CmpConfiguration.AUTHMODULE_ENDENTITY_CERTIFICATE));
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, "AdminCA1");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, "AdminCA1"));
		confSession.updateProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra"));

		KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);
		
		PKIMessage msg = genCertReq(issuerDN, userDN, keys, cacert, nonce, transid, false, null, null, null, null);	
		assertNotNull("Generating CrmfRequest failed." + msg);
		
		AlgorithmIdentifier pAlg = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha1WithRSAEncryption);
		msg.getHeader().setProtectionAlg(pAlg);		 
		msg.getHeader().setSenderKID(new DEROctetString(nonce));

		createUser("cmpTestAdmin", "CN=cmpTestAdmin,C=SE", "foo123");
		KeyPair admkeys = KeyTools.genKeys("1024", "RSA");
		Certificate admCert = signSession.createCertificate(admin, "cmpTestAdmin", "foo123", admkeys.getPublic());
		Admin adm = new Admin(admCert, "cmpTestAdmin", "cmpTestAdmin@primekey.se");
		setupAccessRights(adm);
		addExtraCert(msg, admCert);
		signPKIMessage(msg, admkeys);
		assertNotNull(msg);
		
        final ByteArrayOutputStream bao = new ByteArrayOutputStream();
        final DEROutputStream out = new DEROutputStream(bao);
        out.writeObject(msg);
        final byte[] ba = bao.toByteArray();
        // Send request and receive response
        final byte[] resp = sendCmpHttp(ba, 200);        
        checkCmpResponseGeneral(resp, issuerDN, userDN, cacert, msg.getHeader().getSenderNonce().getOctets(), msg.getHeader().getTransactionID().getOctets(), false, null);
        Certificate cert2 = checkCmpCertRepMessage(userDN, cacert, resp, msg.getBody().getIr().getCertReqMsg(0).getCertReq().getCertReqId().getValue().intValue());
        assertNotNull("CrmfRequest did not return a certificate", cert2);
	}
	
	public void test05EERevReq() throws NoSuchAlgorithmException, EjbcaException, IOException, Exception  {
		assertFalse("Configurations have not been backed up before starting testing.", confSession.backupConfiguration());
		
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, CmpConfiguration.AUTHMODULE_ENDENTITY_CERTIFICATE);
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, CmpConfiguration.AUTHMODULE_ENDENTITY_CERTIFICATE));
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, "AdminCA1");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, "AdminCA1"));
		confSession.updateProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra"));

		Collection<Certificate> certs = certSession.findCertificatesBySubjectAndIssuer(admin, userDN, issuerDN);
		log.debug("Found " + certs.size() + " certificates for userDN \"" + userDN + "\"");
		Certificate cert = null, tmp=null;
		Iterator<Certificate> itr = certs.iterator();
		while(itr.hasNext()) {
			tmp = itr.next();
			if(!certSession.isRevoked(issuerDN, CertTools.getSerialNumber(tmp))) {
				cert = tmp;
				break;
			}
		}
		assertNotNull("Could not find a suitable certificate to revoke.", cert);
		
		PKIMessage msg = genRevReq(issuerDN, userDN, CertTools.getSerialNumber(cert), cacert, nonce, transid, false);	
		assertNotNull("Generating CrmfRequest failed." + msg);
		
		AlgorithmIdentifier pAlg = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha1WithRSAEncryption);
		msg.getHeader().setProtectionAlg(pAlg);		 
		msg.getHeader().setSenderKID(new DEROctetString(nonce));

		createUser("cmpTestAdmin", "CN=cmpTestAdmin,C=SE", "foo123");
		KeyPair admkeys = KeyTools.genKeys("1024", "RSA");
		Certificate admCert = signSession.createCertificate(admin, "cmpTestAdmin", "foo123", admkeys.getPublic());
		Admin adm = new Admin(admCert, "cmpTestAdmin", "cmpTestAdmin@primekey.se");
		setupAccessRights(adm);
		addExtraCert(msg, admCert);
		signPKIMessage(msg, admkeys);
		assertNotNull(msg);
		
        final ByteArrayOutputStream bao = new ByteArrayOutputStream();
        final DEROutputStream out = new DEROutputStream(bao);
        out.writeObject(msg);
        final byte[] ba = bao.toByteArray();
        // Send request and receive response
        final byte[] resp = sendCmpHttp(ba, 200);        
        checkCmpResponseGeneral(resp, issuerDN, userDN, cacert, msg.getHeader().getSenderNonce().getOctets(), msg.getHeader().getTransactionID().getOctets(), false, null);
        int revStatus = checkRevokeStatus(issuerDN, CertTools.getSerialNumber(cert));
        assertNotSame("Revocation request failed to revoke the certificate", RevokedCertInfo.NOT_REVOKED, revStatus);
	}

	public void test06EECrmfReqMultipleAuthModules() throws NoSuchAlgorithmException, EjbcaException, IOException, Exception  {
		assertFalse("Configurations have not been backed up before starting testing.", confSession.backupConfiguration());
		
		String modules = CmpConfiguration.AUTHMODULE_HMAC + ";" + CmpConfiguration.AUTHMODULE_ENDENTITY_CERTIFICATE;
		String parameters = "foo123" + ";" + "AdminCA1";
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, modules);
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, modules));
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, parameters);
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, parameters));
		confSession.updateProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra"));

		KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);
		
		PKIMessage msg = genCertReq(issuerDN, userDN, keys, cacert, nonce, transid, false, null, null, null, null);	
		assertNotNull("Generating CrmfRequest failed." + msg);
		
		AlgorithmIdentifier pAlg = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha1WithRSAEncryption);
		msg.getHeader().setProtectionAlg(pAlg);		 
		msg.getHeader().setSenderKID(new DEROctetString(nonce));

		createUser("cmpTestAdmin", "CN=cmpTestAdmin,C=SE", "foo123");
		KeyPair admkeys = KeyTools.genKeys("1024", "RSA");
		Certificate admCert = signSession.createCertificate(admin, "cmpTestAdmin", "foo123", admkeys.getPublic());
		Admin adm = new Admin(admCert, "cmpTestAdmin", "cmpTestAdmin@primekey.se");
		setupAccessRights(adm);
		addExtraCert(msg, admCert);
		signPKIMessage(msg, admkeys);
		assertNotNull(msg);
		
        final ByteArrayOutputStream bao = new ByteArrayOutputStream();
        final DEROutputStream out = new DEROutputStream(bao);
        out.writeObject(msg);
        final byte[] ba = bao.toByteArray();
        // Send request and receive response
        final byte[] resp = sendCmpHttp(ba, 200);        
        checkCmpResponseGeneral(resp, issuerDN, userDN, cacert, msg.getHeader().getSenderNonce().getOctets(), msg.getHeader().getTransactionID().getOctets(), false, null);
        Certificate cert2 = checkCmpCertRepMessage(userDN, cacert, resp, msg.getBody().getIr().getCertReqMsg(0).getCertReq().getCertReqId().getValue().intValue());
        assertNotNull("CrmfRequest did not return a certificate", cert2);
	}

	public void test07HMACCrmfReqMultipleAuthenticationModules() throws Exception {
		assertFalse("Configurations have not been backed up before starting testing.", confSession.backupConfiguration());
		
		String modules = CmpConfiguration.AUTHMODULE_REG_TOKEN_PWD + ";" + CmpConfiguration.AUTHMODULE_ENDENTITY_CERTIFICATE + ";" + CmpConfiguration.AUTHMODULE_HMAC;
		String parameters = "-;AdminCA1;foo123hmac";
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, modules);
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, modules));
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, parameters);
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, parameters));		
		confSession.updateProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra"));
		
		KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);
		
		PKIMessage msg = genCertReq(issuerDN, userDN, keys, cacert, nonce, transid, false, null, null, null, null);
		assertNotNull("Generating CrmfRequest failed." + msg);
        PKIMessage req = protectPKIMessage(msg, false, "foo123hmac", "mykeyid", 567);
        assertNotNull("Protecting PKIMessage with HMACPbe failed.");
        
        final ByteArrayOutputStream bao = new ByteArrayOutputStream();
        final DEROutputStream out = new DEROutputStream(bao);
        out.writeObject(req);
        final byte[] ba = bao.toByteArray();
        // Send request and receive response
        final byte[] resp = sendCmpHttp(ba, 200);        
        checkCmpResponseGeneral(resp, issuerDN, userDN, cacert, req.getHeader().getSenderNonce().getOctets(), req.getHeader().getTransactionID().getOctets(), false, null);
        Certificate cert1 = checkCmpCertRepMessage(userDN, cacert, resp, req.getBody().getIr().getCertReqMsg(0).getCertReq().getCertReqId().getValue().intValue());
        assertNotNull("Crmf request did not return a certificate", cert1);
	}

	public void test08HMACCrmfReqWrongAuthenticationModule() throws Exception {
		assertFalse("Configurations have not been backed up before starting testing.", confSession.backupConfiguration());
		
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, CmpConfiguration.AUTHMODULE_DN_PART_PWD);
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, CmpConfiguration.AUTHMODULE_DN_PART_PWD));
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, "UID");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, "UID"));		
		confSession.updateProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra"));
		
		KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);
		
		PKIMessage msg = genCertReq(issuerDN, userDN, keys, cacert, nonce, transid, false, null, null, null, null);
		assertNotNull("Generating CrmfRequest failed." + msg);
        PKIMessage req = protectPKIMessage(msg, false, "foo123hmac", "mykeyid", 567);
        assertNotNull("Protecting PKIMessage with HMACPbe failed.");
        
        final ByteArrayOutputStream bao = new ByteArrayOutputStream();
        final DEROutputStream out = new DEROutputStream(bao);
        out.writeObject(req);
        final byte[] ba = bao.toByteArray();
        // Send request and receive response
        final byte[] resp = sendCmpHttp(ba, 200);   
        checkCmpResponseGeneral(resp, issuerDN, userDN, cacert, req.getHeader().getSenderNonce().getOctets(), req.getHeader().getTransactionID().getOctets(), false, null);
        
        PKIMessage respObject = PKIMessage.getInstance(new ASN1InputStream(new ByteArrayInputStream(resp)).readObject());
        assertNotNull(respObject);

        PKIBody body = respObject.getBody();
        assertEquals(23, body.getTagNo());
        String errMsg = body.getError().getPKIStatus().getStatusString().getString(0).getString();
        assertEquals("Unrecognized authentication modules", errMsg);
	}
	
	public void test09EECrmfReqUnauthorizedAdmin() throws NoSuchAlgorithmException, EjbcaException, IOException, Exception  {
		assertFalse("Configurations have not been backed up before starting testing.", confSession.backupConfiguration());
		
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, CmpConfiguration.AUTHMODULE_ENDENTITY_CERTIFICATE);
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONMODULE, CmpConfiguration.AUTHMODULE_ENDENTITY_CERTIFICATE));
		confSession.updateProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, "AdminCA1");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_AUTHENTICATIONPARAMETERS, "AdminCA1"));
		confSession.updateProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra");
		assertTrue("The CMP Authentication module was not configured correctly.", confSession.verifyProperty(CmpConfiguration.CONFIG_OPERATIONMODE, "ra"));

		KeyPair keys = KeyTools.genKeys("512", AlgorithmConstants.KEYALGORITHM_RSA);
		
		PKIMessage msg = genCertReq(issuerDN, userDN, keys, cacert, nonce, transid, false, null, null, null, null);	
		assertNotNull("Generating CrmfRequest failed." + msg);
		
		AlgorithmIdentifier pAlg = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha1WithRSAEncryption);
		msg.getHeader().setProtectionAlg(pAlg);		 
		msg.getHeader().setSenderKID(new DEROctetString(nonce));

		String adminName ="cmpTestUnauthorizedAdmin"; 
		createUser(adminName , "CN=" + adminName + ",C=SE", "foo123");
		KeyPair admkeys = KeyTools.genKeys("1024", "RSA");
		Certificate admCert = signSession.createCertificate(admin, adminName, "foo123", admkeys.getPublic());
		Admin adm = new Admin(admCert, adminName, "cmpTestAdmin@primekey.se");
		addExtraCert(msg, admCert);
		signPKIMessage(msg, admkeys);
		assertNotNull(msg);
		
        final ByteArrayOutputStream bao = new ByteArrayOutputStream();
        final DEROutputStream out = new DEROutputStream(bao);
        out.writeObject(msg);
        final byte[] ba = bao.toByteArray();
        // Send request and receive response
        final byte[] resp = sendCmpHttp(ba, 200);        
        checkCmpResponseGeneral(resp, issuerDN, userDN, cacert, msg.getHeader().getSenderNonce().getOctets(), msg.getHeader().getTransactionID().getOctets(), false, null);

        PKIMessage respObject = PKIMessage.getInstance(new ASN1InputStream(new ByteArrayInputStream(resp)).readObject());
        assertNotNull(respObject);

        PKIBody body = respObject.getBody();
        assertEquals(23, body.getTagNo());
        String errMsg = body.getError().getPKIStatus().getStatusString().getString(0).getString();
        assertEquals("\"CN=cmpTestUnauthorizedAdmin,C=SE\" is not an authorized administrator.", errMsg);
	}
	
	
	
	
	public void test99RestoreConf() {
		assertTrue("Restoring configuration faild.", confSession.restoreConfiguration());
		try {
			userAdminSession.revokeAndDeleteUser(admin, username, ReasonFlags.keyCompromise);
			userAdminSession.revokeAndDeleteUser(admin, "cmpTestUnauthorizedAdmin", ReasonFlags.keyCompromise);
		} catch(Exception e){}
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	private void setCAID() {
		// Try to use AdminCA1 if it exists
		final CAInfo adminca1 = caAdminSession.getCAInfo(admin, "AdminCA1");

		if (adminca1 == null) {
			final Collection<Integer> caids;

			caids = caSession.getAvailableCAs(admin);
			final Iterator<Integer> iter = caids.iterator();
			int tmp = 0;
			while (iter.hasNext()) {
				tmp = iter.next().intValue();
				if(tmp != 0)	break;
			}
			caid = tmp;
		} else {
			caid = adminca1.getCAId();
		}
		if (caid == 0) {
			assertTrue("No active CA! Must have at least one active CA to run tests!", false);
		}
	}
	
	private void setCaCert() {
		final CAInfo cainfo;

		cainfo = caAdminSession.getCAInfo(admin, caid);

		Collection<Certificate> certs = cainfo.getCertificateChain();
		if (certs.size() > 0) {
			Iterator<Certificate> certiter = certs.iterator();
			Certificate cert = certiter.next();
			String subject = CertTools.getSubjectDN(cert);
			if (StringUtils.equals(subject, cainfo.getSubjectDN())) {
				// Make sure we have a BC certificate
				try {
					cacert = (X509Certificate) CertTools.getCertfromByteArray(cert.getEncoded());
				} catch (Exception e) {
					throw new Error(e);
				}
			} else {
				cacert = null;
			}
		} else {
			log.error("NO CACERT for caid " + caid);
			cacert = null;
		}
	}
	
	private void addExtraCert(PKIMessage msg, Certificate cert) throws CertificateEncodingException, IOException{
		ByteArrayInputStream    bIn = new ByteArrayInputStream(cert.getEncoded());
		ASN1InputStream         dIn = new ASN1InputStream(bIn);
		ASN1Sequence extraCertSeq = (ASN1Sequence)dIn.readObject();
		X509CertificateStructure extraCert = new X509CertificateStructure(ASN1Sequence.getInstance(extraCertSeq));
		msg.addExtraCert(extraCert);
	}
	
	private void signPKIMessage(PKIMessage msg, KeyPair keys) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException {
		final Signature sig = Signature.getInstance(PKCSObjectIdentifiers.sha1WithRSAEncryption.getId(), "BC");
		sig.initSign(keys.getPrivate());
		sig.update(msg.getProtectedBytes());
		byte[] eeSignature = sig.sign();			
		msg.setProtection(new DERBitString(eeSignature));	
	}

    private UserDataVO createUser(String username, String subjectDN, String password) throws AuthorizationDeniedException, UserDoesntFullfillEndEntityProfile, 
    			WaitingForApprovalException, EjbcaException, Exception {

    	UserDataVO user = new UserDataVO(username, subjectDN, caid, null, username+"@primekey.se", SecConst.USER_ENDUSER, SecConst.EMPTY_ENDENTITYPROFILE,
        SecConst.CERTPROFILE_FIXED_ENDUSER, SecConst.TOKEN_SOFT_PEM, 0, null);
    	user.setPassword(password);
    	try {
    		userAdminSession.addUser(admin, user, false);
    		// usersession.addUser(admin,"cmptest","foo123",userDN,null,"cmptest@primekey.se",false,SecConst.EMPTY_ENDENTITYPROFILE,SecConst.CERTPROFILE_FIXED_ENDUSER,SecConst.USER_ENDUSER,SecConst.TOKEN_SOFT_PEM,0,caid);
    		log.debug("created user: " + username);
    	} catch (Exception e) {
    		log.debug("User " + username + " already exists. Setting the user status to NEW");
    		userAdminSession.changeUser(admin, user, false);
    		userAdminSession.setUserStatus(admin, username, UserDataConstants.STATUS_NEW);
    		log.debug("Reset status to NEW");
    	}

    	return user;

    }
    
    private void setupAccessRights(Admin adm) throws Exception {
        
    	boolean adminExists = false;
    	AdminGroup admingroup = adminGroupSession.getAdminGroup(adm, AdminGroup.TEMPSUPERADMINGROUP);
    	Iterator<AdminEntity> iter = admingroup.getAdminEntities().iterator();
    	while (iter.hasNext()) {
    		AdminEntity adminEntity = iter.next();
    		if (adminEntity.getMatchValue().equals(adm.getUsername())) {
    			adminExists = true;
            }
    	}

    	if (!adminExists) {
    		List<AdminEntity> list = new ArrayList<AdminEntity>();
    		list.add(new AdminEntity(AdminEntity.WITH_COMMONNAME, AdminEntity.TYPE_EQUALCASE, adm.getUsername(), caid));
    		adminEntitySession.addAdminEntities(adm, AdminGroup.TEMPSUPERADMINGROUP, list);
    		authorizationSession.forceRuleUpdate(adm);
    	}
    	
    	BatchMakeP12 batch = new BatchMakeP12();
    	batch.setMainStoreDir("p12");
    	batch.createAllNew();
    }

}
