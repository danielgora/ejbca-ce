package org.ejbca.core.protocol.cmp.authentication;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.cesecore.core.ejb.ra.raadmin.EndEntityProfileSession;
import org.ejbca.config.CmpConfiguration;
import org.ejbca.core.ejb.authorization.AuthorizationSession;
import org.ejbca.core.ejb.ca.caadmin.CAAdminSession;
import org.ejbca.core.ejb.ca.store.CertificateStoreSession;
import org.ejbca.core.ejb.ra.UserAdminSession;
import org.ejbca.core.model.ca.caadmin.CAInfo;
import org.ejbca.core.model.log.Admin;

import com.novosec.pkix.asn1.cmp.PKIMessage;

public class VerifyPKIMessage {
	
	private static final Logger log = Logger.getLogger(VerifyPKIMessage.class);
	
	private CAInfo cainfo;
	private ICMPAuthenticationModule authModule;
	
	private Admin admin;
	private CAAdminSession caAdminSession;
	private UserAdminSession userAdminSession;
	private CertificateStoreSession certificateStoreSession;
	private AuthorizationSession authorizationSessoin;
	private EndEntityProfileSession eeProfileSession;
	
	private String errMsg;

	public VerifyPKIMessage() {
		this.cainfo = null;
		this.authModule = null;
		
		this.admin = null;
		this.caAdminSession = null;
		this.userAdminSession = null;
		this.certificateStoreSession = null;
		this.authorizationSessoin = null;
		this.eeProfileSession = null;
		
		this.errMsg = null;
	}
	
	public String getErrorMessage() {
		return this.errMsg;
	}
	
	public VerifyPKIMessage(CAInfo cainfo, Admin admin, CAAdminSession caSession, UserAdminSession userSession, CertificateStoreSession certSession,
				AuthorizationSession authSession, EndEntityProfileSession eeprofSession) {
		this.cainfo = cainfo;
		this.authModule = null;
		
		this.admin = admin;
		this.caAdminSession = caSession;
		this.userAdminSession = userSession;
		this.certificateStoreSession = certSession;
		this.authorizationSessoin = authSession;
		this.eeProfileSession = eeprofSession;
	}
	
	public ICMPAuthenticationModule getAuthenticationModule() {
		return this.authModule;
	}
	
	public boolean verify(PKIMessage msg) {
		String authModules = CmpConfiguration.getAuthenticationModule();
		String authparameters = CmpConfiguration.getAuthenticationParameters();
		String modules[] = authModules.split(";");
		String params[] = authparameters.split(";");
		
		ICMPAuthenticationModule module = null;
		int i=0;
		while(i<modules.length) {
			if(log.isDebugEnabled()) {
				log.debug("Trying to verify the message authentication by using \"" + modules[i] + "\" authentication module.");
			}
			module = getAuthModule(modules[i], params[i], msg);
			if((module != null) && module.verify(msg)) {
				this.authModule = module;
				return true;
			}
			if((module != null) && (module.getErrorMessage() != null)) {
				errMsg = module.getErrorMessage();
			}
			i++;
		}
		return false;
		
	}
	
	
	private ICMPAuthenticationModule getAuthModule(String module, String parameter, PKIMessage pkimsg) {
		if(StringUtils.equals(module, CmpConfiguration.AUTHMODULE_HMAC)) {
			HMACAuthenticationModule hmacmodule = new HMACAuthenticationModule(parameter);
			hmacmodule.setSession(this.admin, this.userAdminSession);
			hmacmodule.setCaInfo(this.cainfo);
			return hmacmodule;
		} else if(StringUtils.equals(module, CmpConfiguration.AUTHMODULE_ENDENTITY_CERTIFICATE)) {
			EndEntityCertificateAuthenticationModule eemodule = new EndEntityCertificateAuthenticationModule(parameter);
			eemodule.setSession(this.admin, this.caAdminSession, this.certificateStoreSession, this.authorizationSessoin, this.eeProfileSession);
			return eemodule;
		}
		return null;
	}

}
