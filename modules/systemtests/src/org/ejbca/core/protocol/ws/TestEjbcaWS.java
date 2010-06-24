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
package org.ejbca.core.protocol.ws; 

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.List;

import org.ejbca.core.model.AlgorithmConstants;
import org.ejbca.core.model.authorization.AccessRulesConstants;
import org.ejbca.core.protocol.ws.client.gen.IllegalQueryException_Exception;
import org.ejbca.core.protocol.ws.client.gen.KeyStore;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.client.gen.UserMatch;
import org.ejbca.core.protocol.ws.common.KeyStoreHelper;

/**
 * To run you must have the file tmp/bin/junit/jndi.properties
 * 
 * @version $Id$
 */
public class TestEjbcaWS extends CommonEjbcaWSTest {
	
	public void test00SetupAccessRights() throws Exception{
		super.test00SetupAccessRights();
	}
	
	
	public void test01EditUser() throws Exception{
		test01EditUser(true);
	}

	public void test02findUser() throws Exception{
		test02findUser(true);
	}			

	public void test03_1GeneratePkcs10() throws Exception{
		test03GeneratePkcs10(true);
	}

	public void test03_2GenerateCrmf() throws Exception{
		test03GenerateCrmf(true);
	}

	public void test03_3GenerateSpkac() throws Exception{
		test03GenerateSpkac(true);
	}

    public void test03_4GeneratePkcs10Request() throws Exception {
    	test19GeneratePkcs10Request(true);  
    }

    public void test03_5CertificateRequest() throws Exception{
		test03CertificateRequest(true);
	}

	public void test03_6EnforcementOfUniquePublicKeys() throws Exception{
		test03EnforcementOfUniquePublicKeys(true);
	}

	public void test03_6EnforcementOfUniqueSubjectDN() throws Exception{
		test03EnforcementOfUniqueSubjectDN(true);
	}

    public void test04GeneratePkcs12() throws Exception{
		test04GeneratePkcs12(true);
	}
	
	public void test05findCerts() throws Exception{
	  test05findCerts(true);	
	}
	
	public void test06revokeCert() throws Exception{
		test06revokeCert(true);
	}

	public void test07revokeToken() throws Exception{
		test07revokeToken(true);
	}
	
	public void test08checkRevokeStatus() throws Exception{
	   test08checkRevokeStatus(true);
	}
	
	
	public void test09UTF8() throws Exception{
		test09UTF8(true);
	}
	
	public void test10revokeUser() throws Exception{
		test10revokeUser(true);
	}
	
    public void test11IsAuthorized() throws Exception{
    	setUpAdmin();
    	
		// This is a superadmin keystore, improve in the future
		assertTrue(ejbcaraws.isAuthorized(AccessRulesConstants.ROLE_SUPERADMINISTRATOR));
	}

 
  
    public void test13genTokenCertificates() throws Exception{
    	test12genTokenCertificates(true,false);
    }
    
    
    public void test14getExistsHardToken() throws Exception{
    	test13getExistsHardToken(true);
    }
  

    public void test15getHardTokenData() throws Exception{
    	test14getHardTokenData(true, false);
    }
    
    
    public void test16getHardTokenDatas() throws Exception{
       test15getHardTokenDatas(true);
    }

    public void test17CustomLog() throws Exception{
      test16CustomLog(true);
    }

    public void test18GetCertificate() throws Exception{
      test17GetCertificate(true);
    }
    
    public void test19RevocationApprovals() throws Exception {
    	test18RevocationApprovals(true);  
    }

    public void test20KeyRecoverNewest() throws Exception {
    	test20KeyRecover(true);  
    }

    public void test21GetAvailableCAs() throws Exception {
    	test21GetAvailableCAs(true);  
    }

    public void test22GetAuthorizedEndEntityProfiles() throws Exception {
    	test22GetAuthorizedEndEntityProfiles(true);  
    }
    public void test23GetAvailableCertificateProfiles() throws Exception {
    	test23GetAvailableCertificateProfiles(true);  
    }
    public void test24GetAvailableCAsInProfile() throws Exception {
    	test24GetAvailableCAsInProfile(true);  
    }

    public void test25CreateCRL() throws Exception {
    	test25CreateCRL(true);  
    }

    public void test26_1CvcRequestRSA() throws Exception {
    	test26CvcRequest(true, "CN=WSCVCA,C=SE", "WSTESTCVCA", "CN=WSDVCA,C=SE", "WSTESTDVCA", CA1_WSTESTUSER1CVCRSA, "1024", AlgorithmConstants.KEYALGORITHM_RSA, AlgorithmConstants.SIGALG_SHA256_WITH_RSA_AND_MGF1);
    }

    public void test26_2CleanCvcRequestRSA() throws Exception {
        // Remove the CAs
        deleteCVCCA("CN=WSCVCA,C=SE", "CN=WSDVCA,C=SE");        
    }

    public void test26_3CvcRequestECDSA() throws Exception {
    	test26CvcRequest(true, "CN=WSCVCAEC,C=SE", "WSTESTCVCAEC", "CN=WSDVCAEC,C=SE", "WSTESTDVCAEC", CA2_WSTESTUSER1CVCEC, "secp256r1", AlgorithmConstants.KEYALGORITHM_ECDSA, AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA);
    }

    public void test26_4CleanCvcRequestECDSA() throws Exception {
        // Remove the CAs
        deleteCVCCA("CN=WSCVCAEC,C=SE", "CN=WSDVCAEC,C=SE");        
    }


    public void test27EjbcaVersion() throws Exception {
    	test27EjbcaVersion(true);  
    }

    public void test28getLastCertChain() throws Exception {
    	test28getLastCertChain(true);  
    }

    public void test29ErrorOnEditUser() throws Exception {
    	test29ErrorOnEditUser(true);  
    }

    public void test30ErrorOnGeneratePkcs10() throws Exception {
    	test30ErrorOnGeneratePkcs10(true);  
    }

    public void test31ErrorOnGeneratePkcs12() throws Exception {
    	test31ErrorOnGeneratePkcs12(true);  
    }
    
    public void test32OperationOnNonexistingCA() throws Exception {
        test32OperationOnNonexistingCA(true);  
    }

    public void test33checkQueueLength() throws Exception {
		test33checkQueueLength(true);
	}

	public void test34CaRenewCertRequest() throws Exception{
		super.test34CaRenewCertRequest(true);
	}

	public void test35CleanUpCACertRequest() throws Exception{
		super.test35CleanUpCACertRequest(true);
	}
	
	/** Simulate a simple SQL injection by sending the illegal char "'". */
	public void testEvilFind01() throws Exception {
		setUpAdmin();
		UserMatch usermatch = new UserMatch();
	    usermatch.setMatchwith(org.ejbca.util.query.UserMatch.MATCH_WITH_USERNAME);
	    usermatch.setMatchtype(org.ejbca.util.query.UserMatch.MATCH_TYPE_EQUALS);
	    usermatch.setMatchvalue("A' OR '1=1");
	    try {
			List<UserDataVOWS> userdatas = ejbcaraws.findUser(usermatch);
			fail("SQL injection did not cause an error! " + userdatas.size());
	    } catch (IllegalQueryException_Exception e) {
	    	// NOPMD, this should be thrown and we ignore it because we fail if it is not thrown
	    } 
	}

	/** Use single transaction method for requesting KeyStore with special characters in the certificate SubjectDN. */
	public void testCertificateRequestWithSpecialChars01() throws Exception{
		setUpAdmin();
		long rnd = new SecureRandom().nextLong();
		testCertificateRequestWithSpecialChars("CN=test" + rnd + ", O=foo\\+bar\\\"\\,, C=SE", "CN=test" + rnd + ",O=foo\\+bar\\\"\\,,C=SE");
	}
	
	/** Use single transaction method for requesting KeyStore with special characters in the certificate SubjectDN. */
	public void testCertificateRequestWithSpecialChars02() throws Exception{
		setUpAdmin();
		long rnd = new SecureRandom().nextLong();
		testCertificateRequestWithSpecialChars("CN=test" + rnd + ", O=foo;bar\\;123, C=SE", "CN=test" + rnd + ",O=foo/bar\\;123,C=SE");
	}
	
	/** Use single transaction method for requesting KeyStore with special characters in the certificate SubjectDN. */
	public void testCertificateRequestWithSpecialChars03() throws Exception{
		setUpAdmin();
		long rnd = new SecureRandom().nextLong();
		testCertificateRequestWithSpecialChars("CN=test" + rnd + ", O=foo+bar\\+123, C=SE", "CN=test" + rnd + ",O=foo\\+bar\\+123,C=SE");
	}
	
	/** Use single transaction method for requesting KeyStore with special characters in the certificate SubjectDN. */
	public void testCertificateRequestWithSpecialChars04() throws Exception{
		setUpAdmin();
		long rnd = new SecureRandom().nextLong();
		testCertificateRequestWithSpecialChars("CN=test" + rnd + ", O=foo\\=bar, C=SE", "CN=test" + rnd + ",O=foo\\=bar,C=SE");
	}
	
	/** Use single transaction method for requesting KeyStore with special characters in the certificate SubjectDN. */
	public void testCertificateRequestWithSpecialChars05() throws Exception{
		setUpAdmin();
		long rnd = new SecureRandom().nextLong();
		testCertificateRequestWithSpecialChars("CN=test" + rnd + ", O=\"foo=bar, C=SE\"", "CN=test" + rnd + ",O=foo\\=bar\\, C\\=SE");
	}
	
	/** Use single transaction method for requesting KeyStore with special characters in the certificate SubjectDN. */
	public void testCertificateRequestWithSpecialChars06() throws Exception{
		setUpAdmin();
		long rnd = new SecureRandom().nextLong();
		testCertificateRequestWithSpecialChars("CN=test" + rnd + ", O=\"foo+b\\+ar, C=SE\"", "CN=test" + rnd + ",O=foo\\+b\\\\\\+ar\\, C\\=SE");
	}
	
	/** Use single transaction method for requesting KeyStore with special characters in the certificate SubjectDN. */
	public void testCertificateRequestWithSpecialChars07() throws Exception{
		setUpAdmin();
		long rnd = new SecureRandom().nextLong();
		testCertificateRequestWithSpecialChars("CN=test" + rnd + ", O=\\\"foo+b\\+ar\\, C=SE\\\"", "CN=test" + rnd + ",O=\\\"foo\\+b\\+ar\\, C\\=SE\\\"");
	}
	
    public void test99cleanUpAdmins() throws Exception {
    	super.test99cleanUpAdmins();
    }

	private void testCertificateRequestWithSpecialChars(String requestedSubjectDN, String expectedSubjectDN) throws Exception {
		String userName = "wsSpecialChars" + new SecureRandom().nextLong();
		final UserDataVOWS userData = new UserDataVOWS();
		userData.setUsername(userName);
		userData.setPassword("foo123");
		userData.setClearPwd(true);
		userData.setSubjectDN(requestedSubjectDN);
		userData.setCaName(getAdminCAName());
		userData.setEmail(null);
		userData.setSubjectAltName(null);
		userData.setStatus(UserDataVOWS.STATUS_NEW);
		userData.setTokenType(UserDataVOWS.TOKEN_TYPE_P12);
		userData.setEndEntityProfileName("EMPTY");
		userData.setCertificateProfileName("ENDUSER");

        KeyStore ksenv = ejbcaraws.softTokenRequest(userData,null,"1024", AlgorithmConstants.KEYALGORITHM_RSA);
        java.security.KeyStore keyStore = KeyStoreHelper.getKeyStore(ksenv.getKeystoreData(),"PKCS12","foo123");
        assertNotNull(keyStore);
        Enumeration en = keyStore.aliases();
        String alias = (String) en.nextElement();
        X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
        
        String resultingSubjectDN = cert.getSubjectDN().toString();
        assertEquals(requestedSubjectDN + " was transformed into " + resultingSubjectDN + " (not the expected " + expectedSubjectDN + ")", expectedSubjectDN, resultingSubjectDN);
	}
}

