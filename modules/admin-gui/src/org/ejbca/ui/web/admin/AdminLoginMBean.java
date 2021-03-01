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
package org.ejbca.ui.web.admin;

import java.io.IOException;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cesecore.authentication.oauth.OAuthGrantResponseInfo;
import org.cesecore.authentication.oauth.OAuthKeyInfo;
import org.cesecore.authentication.oauth.OauthRequestHelper;
import org.ejbca.config.WebConfiguration;
import org.ejbca.ui.web.jsf.configuration.EjbcaWebBean;
import org.ejbca.util.HttpTools;

/**
 * Bean used to display a login page.
 */
@ManagedBean
@SessionScoped
public class AdminLoginMBean extends BaseManagedBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(AdminLoginMBean.class);

    private EjbcaWebBean ejbcaWebBean;

    private List<Throwable> throwables = null;
    private String errorMessage;
    private Collection<OAuthKeyInfoGui> oauthKeys = null;
    /** A random identifier used to link requests, to avoid CSRF attacks. */
    private String stateInSession = null;
    private String oauthClicked = null;

    public class OAuthKeyInfoGui{
        String label;

        public OAuthKeyInfoGui(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    /**
     * @return the general error which occurred
     */
    public String getError() {
        return ejbcaWebBean.getText("AUTHORIZATIONDENIED");
    }

    /**
     * @return please login message
     */
    public String getPleaseLogin() {
        return ejbcaWebBean.getText("PLEASE_LOGIN");
    }

    /**
     * @return error message generated by application exceptions
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * without access to template, we have to fetch the CSS manually
     *
     * @return path to admin web CSS file
     **/
    public String getCssFile() {
        try {
            return ejbcaWebBean.getBaseUrl() + "/" + ejbcaWebBean.getCssFile();
        } catch (Exception e) {
            // This happens when EjbcaWebBeanImpl fails to initialize.
            // That is already logged in EjbcaWebBeanImpl.getText, so log at debug level here.
            final String msg = "Caught exception when trying to get stylesheet URL, most likely EjbcaWebBean failed to initialized";
            if (log.isTraceEnabled()) {
                log.debug(msg, e);
            } else {
                log.debug(msg);
            }
            return "exception_in_getCssFile";
        }
    }

    /**
     * Invoked when login.xhtml is rendered. Show errors and possible login links.
     */
    @SuppressWarnings("unchecked")
    public void onLoginPageLoad() throws Exception {
        ejbcaWebBean = getEjbcaErrorWebBean();
        HttpServletRequest servletRequest = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        ejbcaWebBean.initialize_errorpage(servletRequest);
        final Map<String, Object> requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
        final Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        final String authCode = params.get("code");
        final String state = params.get("state");
        final String error = params.get("error");
        // Render error caught by CaExceptionHandlerFactory
        if (throwables == null) {
            throwables = (List<Throwable>) requestMap.get(CaExceptionHandlerFactory.REQUESTMAP_KEY);
            requestMap.remove(CaExceptionHandlerFactory.REQUESTMAP_KEY);
        }
        if (CollectionUtils.isNotEmpty(throwables)) {
            for (final Throwable throwable : throwables) {
                if (log.isDebugEnabled()) {
                    log.debug("Error occurred.", throwable);
                }
                errorMessage = ejbcaWebBean.getText("CAUSE") + ": " + throwable.getMessage();
            }
        }

        if (error != null) {
            log.info("Server reported user authentication failure: " + error.replaceAll("[^a-zA-Z0-9_]", "")); // sanitize untrusted parameter
            if (verifyStateParameter(state)) {
                errorMessage = params.getOrDefault("error_description", "");
            } else {
                log.info("Received 'error' parameter without valid 'state' parameter.");
                errorMessage = "Internal error.";
            }
        }
        else if (StringUtils.isNotEmpty(authCode)) {
            requestTokenUsingCode(servletRequest, params);
        } else {
            log.debug("Generating randomized 'state' string.");
            final byte[] stateBytes = new byte[32];
            new SecureRandom().nextBytes(stateBytes);
            stateInSession = Base64.encodeBase64URLSafeString(stateBytes);
            initOauthProviders();
            log.debug("Showing login links.");
        }
    }

    private void requestTokenUsingCode(HttpServletRequest servletRequest, Map<String, String> params) throws IOException {
        log.debug("Received authorization code. Requesting token from authorization server.");
        final String authCode = params.get("code");
        final String state = params.get("state");
        if (verifyStateParameter(state)) {
            OAuthKeyInfo oAuthKeyInfo = ejbcaWebBean.getOAuthConfiguration().getOauthKeyByLabel(oauthClicked);
            if (oAuthKeyInfo != null) {
                final OAuthGrantResponseInfo token = OauthRequestHelper.sendTokenRequest(oAuthKeyInfo, authCode,
                        getRedirectUri());
                if (token.compareTokenType(HttpTools.AUTHORIZATION_SCHEME_BEARER)) {
                    servletRequest.getSession(true).setAttribute("ejbca.bearer.token", token);
                    FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
                } else {
                    log.info("Received OAuth token of unsupported type '" + token.getTokenType() + "'");
                    errorMessage = "Internal error.";
                }
            } else {
                log.info("Received provider identifier does not correspond to existing oauth providers. Key indentifier = " + oauthClicked);
                errorMessage = "Internal error";
            }
        } else {
            log.info("Received 'code' parameter without valid 'state' parameter.");
            errorMessage = "Internal error.";
        }
    }

    private String getRedirectUri() {
        return ejbcaWebBean.getGlobalConfiguration().getBaseUrl(
                "https",
                WebConfiguration.getHostName(),
                WebConfiguration.getPublicHttpsPort()
        ) + ejbcaWebBean.getGlobalConfiguration().getAdminWebPath();
    }

    private boolean verifyStateParameter(final String state) {
        return stateInSession != null && stateInSession.equals(state);
    }

    /**
     * Returns a list of OAuth Keys containing url.
     *
     * @return a list of OAuth Keys containing url
     */
    public Collection<OAuthKeyInfoGui> getAllOauthKeys() {
        if (oauthKeys == null) {
            initOauthProviders();
        }
        return oauthKeys;
    }

    private void initOauthProviders() {
        oauthKeys = new ArrayList<>();
        ejbcaWebBean.reloadGlobalConfiguration();
        Collection<OAuthKeyInfo> oAuthKeyInfos = ejbcaWebBean.getOAuthConfiguration().getOauthKeys().values();
        if (!oAuthKeyInfos.isEmpty()) {
            for (OAuthKeyInfo oauthKeyInfo : oAuthKeyInfos) {
                if (StringUtils.isNotEmpty(oauthKeyInfo.getUrl())) {
                    oauthKeys.add(new OAuthKeyInfoGui(oauthKeyInfo.getLabel()));
                }
            }
        }
    }

    private String getOauthLoginUrl(OAuthKeyInfo oauthKeyInfo) {
        String url = oauthKeyInfo.getUrl();
        if (StringUtils.isNotEmpty(oauthKeyInfo.getRealm())) {
            url = new StringBuilder()
                    .append(oauthKeyInfo.getUrl()).append("/realms/")
                    .append(oauthKeyInfo.getRealm())
                    .append("/protocol/openid-connect/auth").toString();
        }
        return addParametersToUrl(oauthKeyInfo, url);
    }

    private String addParametersToUrl(OAuthKeyInfo oauthKeyInfo, String url) {
        UriBuilder uriBuilder = UriBuilder.fromUri(url);
        if (StringUtils.isNotEmpty(oauthKeyInfo.getClient())) {
            uriBuilder
                    .queryParam("client_id", oauthKeyInfo.getClient());
        }
        uriBuilder
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", getRedirectUri())
                .queryParam("state", stateInSession);
        return uriBuilder.build().toString();
    }

    public void clickLoginLink(String keyLabel) throws IOException {
        final OAuthKeyInfo oAuthKeyInfo = ejbcaWebBean.getOAuthConfiguration().getOauthKeyByLabel(keyLabel);
        if (oAuthKeyInfo != null) {
            oauthClicked = keyLabel;
            String url = getOauthLoginUrl(oAuthKeyInfo);
            FacesContext.getCurrentInstance().getExternalContext().redirect(url);
        } else {
            log.info("Trusted provider info not found for keyId =" + keyLabel);
        }
    }

}
