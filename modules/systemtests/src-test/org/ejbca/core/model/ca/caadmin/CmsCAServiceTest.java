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

package org.ejbca.core.model.ca.caadmin;

import static org.junit.Assert.*;

import java.security.cert.CertStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.security.auth.x500.X500Principal;

import org.apache.log4j.Logger;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.cesecore.authentication.tokens.AlwaysAllowLocalAuthenticationToken;
import org.cesecore.authentication.tokens.AuthenticationToken;
import org.cesecore.authentication.tokens.UsernamePrincipal;
import org.cesecore.certificates.ca.CAInfo;
import org.cesecore.certificates.ca.CaSessionRemote;
import org.cesecore.certificates.ca.extendedservices.ExtendedCAServiceInfo;
import org.cesecore.certificates.ca.extendedservices.ExtendedCAServiceNotActiveException;
import org.cesecore.util.CryptoProviderTools;
import org.ejbca.core.ejb.ca.CaTestCase;
import org.ejbca.core.ejb.ca.caadmin.CAAdminSessionRemote;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.CmsCAServiceInfo;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.CmsCAServiceRequest;
import org.ejbca.core.model.ca.caadmin.extendedcaservices.CmsCAServiceResponse;
import org.ejbca.util.InterfaceCache;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the CertTools class.
 * 
 * @version $Id$
 */
public class CmsCAServiceTest extends CaTestCase {
    private static Logger log = Logger.getLogger(CmsCAServiceTest.class);

    private byte[] doc = "foo123".getBytes();

    private final AuthenticationToken admin = new AlwaysAllowLocalAuthenticationToken(new UsernamePrincipal("SYSTEMTEST"));

    private CAAdminSessionRemote caAdminSession = InterfaceCache.getCAAdminSession();
    private CaSessionRemote caSession = InterfaceCache.getCaSession();

    @BeforeClass
    public static void beforeClass() {
        // Install BouncyCastle provider
        CryptoProviderTools.installBCProvider();

    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void test01CmsCAServiceNotActive() throws Exception {
        CmsCAServiceRequest request = new CmsCAServiceRequest(doc, CmsCAServiceRequest.MODE_SIGN);
        // First try a request when the service is not active
        boolean active = true;
        try {
            caAdminSession.extendedService(admin, getTestCAId(), request);
        } catch (ExtendedCAServiceNotActiveException e) {
            active = false;
        }
        // By default the CA service is not active
        assertTrue(!active);
    }

    /**
	 */
    @Test
    public void test02ActivateCmsCAService() throws Exception {
        // Activate the CMS service in the CA
        CAInfo cainfo = caSession.getCAInfo(admin, "TEST");
        ArrayList<ExtendedCAServiceInfo> newlist = new ArrayList<ExtendedCAServiceInfo>();
        newlist.add(new CmsCAServiceInfo(ExtendedCAServiceInfo.STATUS_ACTIVE, false));
        cainfo.setExtendedCAServiceInfos(newlist);
        caAdminSession.editCA(admin, cainfo);
    }

    /**
	 */
    @Test
    public void test03CmsCAServiceActive() throws Exception {
        CmsCAServiceRequest request = new CmsCAServiceRequest(doc, CmsCAServiceRequest.MODE_SIGN);
        CmsCAServiceResponse resp = null;
        // Try the request again
        boolean active = true;
        try {
            resp = (CmsCAServiceResponse) caAdminSession.extendedService(admin, getTestCAId(), request);
        } catch (ExtendedCAServiceNotActiveException e) {
            active = false;
        }
        // By default the CA service is not active
        assertTrue(active);

        assertNotNull(resp);
        byte[] respdoc = resp.getCmsDocument();
        assertNotNull(resp);
        CMSSignedData csd = new CMSSignedData(respdoc);
        SignerInformationStore infoStore = csd.getSignerInfos();
        Collection<SignerInformation> signers = infoStore.getSigners();
        Iterator<SignerInformation> iter = signers.iterator();
        if (iter.hasNext()) {
            SignerInformation si = iter.next();
            assertNotNull(si);
            // log.info("Digest alg is: "+si.getDigestAlgOID());
            assertEquals(CMSSignedGenerator.DIGEST_SHA1, si.getDigestAlgOID());
            SignerId sid = si.getSID();
            // log.info(sid.toString());
            X500Principal issuer = sid.getIssuer();
            assertNotNull(issuer);
            assertEquals("CN=TEST", issuer.getName());
        }
        CertStore store = csd.getCertificatesAndCRLs("Collection", "BC");
        Collection certs = store.getCertificates(null);
        assertEquals(2, certs.size());

        CMSProcessable cp = csd.getSignedContent();
        Object o = cp.getContent();
        byte[] ob = (byte[]) o;
        assertEquals(new String(doc), new String(ob));
    }

    /**
	 */
    @Test
    public void test03CmsCAEncryptDecrypt() throws Exception {
        CmsCAServiceRequest request = new CmsCAServiceRequest(doc, CmsCAServiceRequest.MODE_ENCRYPT);
        CmsCAServiceResponse resp = null;
        // Try the request again
        boolean active = true;
        try {
            resp = (CmsCAServiceResponse) caAdminSession.extendedService(admin, getTestCAId(), request);
        } catch (ExtendedCAServiceNotActiveException e) {
            active = false;
        }
        // By default the CA service is not active
        assertTrue(active);

        assertNotNull(resp);
        byte[] respdoc = resp.getCmsDocument();
        assertNotNull(respdoc);

        assertFalse(Arrays.equals(respdoc, doc));

        request = new CmsCAServiceRequest(respdoc, CmsCAServiceRequest.MODE_DECRYPT);

        // Try the request again
        active = true;
        try {
            resp = (CmsCAServiceResponse) caAdminSession.extendedService(admin, getTestCAId(), request);
        } catch (ExtendedCAServiceNotActiveException e) {
            active = false;
        }
        // By default the CA service is not active
        assertTrue(active);

        assertNotNull(resp);
        respdoc = resp.getCmsDocument();
        assertNotNull(respdoc);

        assertTrue(Arrays.equals(respdoc, doc));
    }

    /**
	 */
    @Test
    public void test04DeActivateCmsCAService() throws Exception {
        // Deactivate the CMS service in the CA
        CAInfo cainfo = caSession.getCAInfo(admin, "TEST");
        ArrayList<ExtendedCAServiceInfo> newlist = new ArrayList<ExtendedCAServiceInfo>();
        newlist.add(new CmsCAServiceInfo(ExtendedCAServiceInfo.STATUS_INACTIVE, false));
        cainfo.setExtendedCAServiceInfos(newlist);
        caAdminSession.editCA(admin, cainfo);
    }

}
