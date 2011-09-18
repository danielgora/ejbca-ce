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

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.ejbca.core.model.AlgorithmConstants;
import org.ejbca.core.model.ca.certextensions.CertificateExtensionFactory;
import org.ejbca.core.model.ca.certificateprofiles.CertificateProfile;
import org.ejbca.core.model.ca.certificateprofiles.EndUserCertificateProfile;
import org.ejbca.core.model.ra.raadmin.EndEntityProfile;
import org.ejbca.core.protocol.ws.client.gen.CertificateResponse;
import org.ejbca.core.protocol.ws.client.gen.ExtendedInformationWS;
import org.ejbca.core.protocol.ws.client.gen.UserDataVOWS;
import org.ejbca.core.protocol.ws.common.CertificateHelper;
import org.ejbca.util.Base64;
import org.ejbca.util.CertTools;
import org.ejbca.util.keystore.KeyTools;

/**
 * 
 * 
 * @author Lars Silvén
 * @version $Id$
 */
public class CertificateExtensionTest extends CommonEjbcaWS {

	private static final Logger log = Logger.getLogger(CertificateExtensionTest.class);
	private static final String CERTIFICATE_PROFILE = "certExtension";
	private static final String TEST_USER = "certExtension";
	private static final String END_ENTITY_PROFILE = "endEntityProfile";

	public void test00SetupAccessRights() {
		try {
			super.setupAccessRights();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}
	public void test1() throws Exception {
		setUpAdmin();
		{
			Properties props = new Properties();
			props.put("id1.oid", "1.2.3.4");
			props.put("id1.classpath", "org.ejbca.core.model.ca.certextensions.BasicCertificateExtension");
			props.put("id1.displayname", "TESTEXTENSION");
			props.put("id1.used", "TRUE");
			props.put("id1.translatable", "FALSE");
			props.put("id1.critical", "TRUE");		
			props.put("id1.property.encoding", "DERPRINTABLESTRING");
			props.put("id1.property.value", "Test 123");
			CertificateExtensionFactory.getInstance(props);
		}
		if (this.certificateProfileSession.getCertificateProfileId(intAdmin, CERTIFICATE_PROFILE) != 0) {
			this.certificateProfileSession.removeCertificateProfile(intAdmin, CERTIFICATE_PROFILE);
		}
		final int certProfID; {
			final CertificateProfile profile = new EndUserCertificateProfile();
			final List<Integer> usedCertificateExtensions = new LinkedList<Integer>();
			usedCertificateExtensions.add(new Integer(1));
			profile.setUsedCertificateExtensions(usedCertificateExtensions);
			this.certificateProfileSession.addCertificateProfile(intAdmin, CERTIFICATE_PROFILE, profile);
			certProfID = this.certificateProfileSession.getCertificateProfileId(intAdmin, CERTIFICATE_PROFILE);
		}
		if ( this.endEntityProfileSession.getEndEntityProfile(intAdmin, END_ENTITY_PROFILE)!=null ) {
			this.endEntityProfileSession.removeEndEntityProfile(intAdmin, END_ENTITY_PROFILE);
		}
		{
			final EndEntityProfile profile = new EndEntityProfile(true);
	        profile.setValue(EndEntityProfile.AVAILCERTPROFILES, 0, Integer.toString(certProfID));
			this.endEntityProfileSession.addEndEntityProfile(intAdmin, END_ENTITY_PROFILE, profile);
		}

		final UserDataVOWS userData = new UserDataVOWS(TEST_USER, PASSWORD, true, "C=SE, CN=cert extension test",
				getAdminCAName(), null, "foo@anatom.se", UserDataVOWS.STATUS_NEW,
				UserDataVOWS.TOKEN_TYPE_USERGENERATED, END_ENTITY_PROFILE, CERTIFICATE_PROFILE, null);
		{
			final List<ExtendedInformationWS> lei = new LinkedList<ExtendedInformationWS>();
			final ExtendedInformationWS ei = new ExtendedInformationWS();
			ei.setName("1.2.3");
			ei.setValue("1234567890abcdef");
			lei.add(ei);
			userData.setExtendedInformation(lei);
		}
		setUpAdmin();
		this.ejbcaraws.editUser(userData);

		final KeyPair keys = KeyTools.genKeys("1024", AlgorithmConstants.KEYALGORITHM_RSA);
		final PKCS10CertificationRequest pkcs10 = new PKCS10CertificationRequest("SHA1WithRSA", CertTools.stringToBcX509Name("CN=NOUSED"), keys.getPublic(),
				new DERSet(), keys.getPrivate());

		final CertificateResponse certenv = this.ejbcaraws.pkcs10Request(TEST_USER, PASSWORD, new String(Base64.encode(pkcs10.getEncoded())), null,
				CertificateHelper.RESPONSETYPE_CERTIFICATE);

		assertNotNull(certenv);
		assertTrue(certenv.getResponseType().equals(CertificateHelper.RESPONSETYPE_CERTIFICATE));
		final X509Certificate cert = (X509Certificate) CertificateHelper.getCertificate(certenv.getData());

		CertificateExtensionFactory.getInstance(null);
	}
	public void test99cleanUpAdmins() {
		try {
			this.certificateProfileSession.removeCertificateProfile(intAdmin, CERTIFICATE_PROFILE);
		} catch (Throwable e) {
			// do nothing
		}
		try {
			this.endEntityProfileSession.removeEndEntityProfile(intAdmin, END_ENTITY_PROFILE);
		} catch (Throwable e) {
			// do nothing
		}
		try {
			this.userAdminSession.deleteUser(intAdmin, TEST_USER);
		} catch (Throwable e) {
			// do nothing
		}
		try {
			super.cleanUpAdmins();
		} catch (Throwable e) {
			// do nothing
		}
	}
}
