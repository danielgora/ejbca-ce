/*************************************************************************
 *                                                                       *
 *  EJBCA Community: The OpenSource Certificate Authority                *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/
package org.ejbca.webtest.scenario;

import java.io.IOException;

import org.cesecore.authentication.oauth.OAuthKeyInfo.OAuthProviderType;
import org.ejbca.webtest.WebTestBase;
import org.ejbca.webtest.helper.OauthProvidersHelper;
import org.ejbca.webtest.helper.SystemConfigurationHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.WebDriver;

/**
 * Adds, edits, views and removes a Trusted OAuth Provider.
 *  <br/>
 * Reference: <a href="https://jira.primekey.se/browse/ECAQA-301">ECAQA-301</a>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EcaQa301_ManageOAuthProvider extends WebTestBase {

    private static final OAuthProviderType TYPE = OAuthProviderType.TYPE_AZURE;
    private static final String LABEL = "ECAQA-301";
    private static final String KEYID = "ecaqa-301";
    private static final String KEYID_EDITED = "edited_keyid";
    private static final String SKEWLIMIT = "40000";
    private static final String SKEWLIMIT_EDITED = "60000";
    private static final String TENANT = "tenant";
    private static final String SCOPE = "scope";
    private static final String CLIENT = "client";
    private static final String SECRET = "secret";

    private static OauthProvidersHelper oauthProvidersHelper;
    private static SystemConfigurationHelper sysConfigHelper;
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void init() {
        beforeClass(true, null);
        WebDriver webDriver = getWebDriver();
        oauthProvidersHelper = new OauthProvidersHelper(webDriver);
        sysConfigHelper = new SystemConfigurationHelper(webDriver);
    }

    @AfterClass
    public static void exit() {
        removeOauthProviders(KEYID, KEYID_EDITED);
        afterClass();
    }

    @Test
    public void stepA_addOauthProvider() throws IOException {
        goToProvidersConfigurationPage();
        oauthProvidersHelper.startAddingProvider();
        
        oauthProvidersHelper.selectProviderType(TYPE);
        oauthProvidersHelper.fillProviderNameField(LABEL);
        oauthProvidersHelper.fillPublicKeyField(oauthProvidersHelper.createPublicKeyFile(folder, OauthProvidersHelper.PUBLIC_KEY));
        oauthProvidersHelper.fillSkewLimitField(SKEWLIMIT);
        oauthProvidersHelper.fillTenantField(TENANT);
        oauthProvidersHelper.fillScopeField(SCOPE);
        oauthProvidersHelper.fillClientField(CLIENT);
        oauthProvidersHelper.fillClientSecretField(SECRET);
        oauthProvidersHelper.pressAddOauthProviderButton();

        oauthProvidersHelper.assertElementExistsInTable(KEYID);
    }
    
    @Test
    public void stepB_editOauthProvider() {
        oauthProvidersHelper.pressEditOauthProviderButton(KEYID);
        
        oauthProvidersHelper.fillKeyIdField(KEYID_EDITED);
        oauthProvidersHelper.fillSkewLimitField(SKEWLIMIT_EDITED);
        oauthProvidersHelper.pressSaveOauthProviderButton();
        
        oauthProvidersHelper.assertElementExistsInTable(KEYID_EDITED);
    }
    
    @Test
    public void stepC_viewOauthProvider() {
        oauthProvidersHelper.pressViewOauthProviderButton(KEYID_EDITED);
        
        oauthProvidersHelper.assertKeyIdentifierText("edited_keyid");
        oauthProvidersHelper.assertCurrentPublicKeyFingerprintText("NdKo3LkMXyL7hAVfdHveLaJrPAFHvhTIJGrvPx+jhps=");
        oauthProvidersHelper.assertSkewLimitText("60000");
        oauthProvidersHelper.pressBackButton();
        
        oauthProvidersHelper.assertElementExistsInTable(KEYID_EDITED);
    }
    
    public void stepD_deleteOauthProvider() {
        oauthProvidersHelper.pressRemoveOauthProviderButton(KEYID_EDITED);
        
        oauthProvidersHelper.assertElementDoesNotExistInTable(KEYID_EDITED);
    }

    private void goToProvidersConfigurationPage() {
        sysConfigHelper.openPage(getAdminWebUrl());
        sysConfigHelper.openTab(SystemConfigurationHelper.SysConfigTabs.OAUTHPROVIDERS);
    }
}
