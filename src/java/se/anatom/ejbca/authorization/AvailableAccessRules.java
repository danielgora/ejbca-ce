package se.anatom.ejbca.authorization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import se.anatom.ejbca.log.Admin;
import se.anatom.ejbca.ra.raadmin.GlobalConfiguration;
import se.anatom.ejbca.ra.raadmin.IRaAdminSessionLocal;

/**
 * 
 *
 * @version $Id: AvailableAccessRules.java,v 1.5 2004-02-11 10:43:15 herrvendil Exp $
 */
public class AvailableAccessRules {
        
        // Available end entity profile authorization rules.
    public static final String VIEW_RIGHTS    = "/view_end_entity";
    public static final String EDIT_RIGHTS    = "/edit_end_entity";
    public static final String CREATE_RIGHTS  = "/create_end_entity";
    public static final String DELETE_RIGHTS  = "/delete_end_entity";
    public static final String REVOKE_RIGHTS  = "/revoke_end_entity";
    public static final String HISTORY_RIGHTS = "/view_end_entity_history";
    

    public static final String  HARDTOKEN_RIGHTS               = "/view_hardtoken";

    public static final String  KEYRECOVERY_RIGHTS             = "/keyrecovery";    
    
        // Endings used in profile authorizxation.
    public static final String[]  ENDENTITYPROFILE_ENDINGS = {VIEW_RIGHTS,EDIT_RIGHTS,CREATE_RIGHTS,DELETE_RIGHTS,REVOKE_RIGHTS,HISTORY_RIGHTS};
    
        // Name of end entity profile prefix directory in authorization module.
    public static final String    ENDENTITYPROFILEPREFIX          = "/endentityprofilesrules/";


        // Name of ca prefix directory in access rules.
    public static final String    CAPREFIX          = "/ca/";
    
    public static final String REGULAR_CAFUNCTIONALTY                                 = "/ca_functionality";
    public static final String REGULAR_CABASICFUNCTIONS                             = "/ca_functionality/basic_functions";    
    public static final String REGULAR_VIEWCERTIFICATE                                 = "/ca_functionality/view_certificate";    
    public static final String REGULAR_CREATECRL                                           = "/ca_functionality/create_crl";    
    public static final String REGULAR_EDITCERTIFICATEPROFILES                    = "/ca_functionality/edit_certificate_profiles";
    public static final String REGULAR_CREATECERTIFICATE                              = "/ca_functionality/create_certificate";
    public static final String REGULAR_STORECERTIFICATE                               = "/ca_functionality/store_certificate";    
    public static final String REGULAR_RAFUNCTIONALITY                                 = "/ra_functionality";
    public static final String REGULAR_EDITENDENTITYPROFILES                       = "/ra_functionality/edit_end_entity_profiles";    
    public static final String REGULAR_VIEWENDENTITY                                     = "/ra_functionality/view_end_entity";    
    public static final String REGULAR_CREATEENDENTITY                                 = "/ra_functionality/create_end_entity";
    public static final String REGULAR_EDITENDENTITY                                      = "/ra_functionality/edit_end_entity";
    public static final String REGULAR_DELETEENDENTITY                                  = "/ra_functionality/delete_end_entity";
    public static final String REGULAR_REVOKEENDENTITY                                 = "/ra_functionality/revoke_end_entity";    
    public static final String REGULAR_VIEWENDENTITYHISTORY                        = "/ra_functionality/view_end_entity_history";
    public static final String REGULAR_LOGFUNCTIONALITY                                = "/log_functionality"; 
    public static final String REGULAR_VIEWLOG                                                = "/log_functionality/view_log"; 
    public static final String REGULAR_LOGCONFIGURATION                              = "/log_functionality/edit_log_configuration"; 
    public static final String REGULAR_SYSTEMCONFIGURATION                         = "/system_functionality";
    public static final String REGULAR_EDITADMINISTRATORPRIVILEDGES           = "/system_functionality/edit_administrator_privileges";
    
        // Standard Regular Access Rules
    private  final  String[] STANDARDREGULARACCESSRULES = {REGULAR_CAFUNCTIONALTY, 
                                                           REGULAR_CABASICFUNCTIONS,
                                                           REGULAR_VIEWCERTIFICATE, 
                                                           REGULAR_CREATECRL,
                                                           REGULAR_EDITCERTIFICATEPROFILES,
                                                           REGULAR_CREATECERTIFICATE,
                                                           REGULAR_STORECERTIFICATE,
                                                           REGULAR_RAFUNCTIONALITY, 
                                                           REGULAR_EDITENDENTITYPROFILES,
                                                           REGULAR_VIEWENDENTITY,
                                                           REGULAR_CREATEENDENTITY, 
                                                           REGULAR_EDITENDENTITY, 
                                                           REGULAR_DELETEENDENTITY,
                                                           REGULAR_REVOKEENDENTITY,
                                                           REGULAR_VIEWENDENTITYHISTORY,
                                                           REGULAR_LOGFUNCTIONALITY,
                                                           REGULAR_VIEWLOG,
                                                           REGULAR_LOGCONFIGURATION,
                                                           REGULAR_SYSTEMCONFIGURATION,
                                                           REGULAR_EDITADMINISTRATORPRIVILEDGES};
                                                       
        // Role Access Rules
    public static final  String[] ROLEACCESSRULES =       {  "/public_web_user",
                                                             "/administrator",
                                                             "/super_administrator"};
                                                       
    
    
    public static final String[] VIEWLOGACCESSRULES =   { "/log_functionality/view_log/ca_entries",
                                                          "/log_functionality/view_log/ra_entries",
                                                          "/log_functionality/view_log/log_entries",
                                                          "/log_functionality/view_log/publicweb_entries",
                                                          "/log_functionality/view_log/adminweb_entries",
                                                          "/log_functionality/view_log/hardtoken_entries",
                                                          "/log_functionality/view_log/keyrecovery_entries",
                                                          "/log_functionality/view_log/authorization_entries"};
    
                                                        
        // Hard Token specific accessrules used in authorization module.
    public static final String[] HARDTOKENACCESSRULES    = {"/hardtoken_functionality",
                                                            "/hardtoken_functionality/edit_hardtoken_issuers",
		                                                    "/hardtoken_functionality/edit_hardtoken_profiles",     
                                                            "/hardtoken_functionality/issue_hardtokens",
                                                            "/hardtoken_functionality/issue_hardtoken_administrators"};
                                                            

                                                        
                                                        
    /** Creates a new instance of AvailableAccessRules */
    public AvailableAccessRules(Admin admin, Authorizer authorizer, IRaAdminSessionLocal raadminsession, String[] customaccessrules) throws NamingException, CreateException {   
      // Initialize
      this.raadminsession = raadminsession;  
      this.authorizer = authorizer;
      
      // Get Global Configuration
      GlobalConfiguration globalconfiguration = raadminsession.loadGlobalConfiguration(admin);
      enableendentityprofilelimitations = globalconfiguration.getEnableEndEntityProfileLimitations();
      usehardtokenissuing = globalconfiguration.getIssueHardwareTokens();
      usekeyrecovery = globalconfiguration.getEnableKeyRecovery();        
      
      // Is Admin SuperAdministrator.
      try{
        issuperadministrator = authorizer.isAuthorizedNoLog(admin, "/super_administrator");
      }catch(AuthorizationDeniedException e){
        issuperadministrator=false;
      }
      // Get End Entity Profiles
      endentityprofiles = raadminsession.getEndEntityProfileIdToNameMap(admin);
      
      // Get CA:s
      authorizedcaids = new HashSet();
      authorizedcaids.addAll(authorizer.getAuthorizedCAIds(admin));
      
      this.customaccessrules= customaccessrules;
    }
    
    // Public methods 
    /** Returns all the accessrules and subaccessrules from the given subresource */
    public Collection getAvailableAccessRules(Admin admin){
      ArrayList accessrules = new ArrayList();
      
      
      insertAvailableRoleAccessRules(accessrules);
      
      insertAvailableRegularAccessRules(admin, accessrules);
      
      if(enableendentityprofilelimitations) 
        insertAvailableEndEntityProfileAccessRules(admin, accessrules);

      insertAvailableCAAccessRules(accessrules);
      
      insertCustomAccessRules(admin, accessrules);
      
      
      return accessrules;
    }
   
    // Private methods
    /**
     * Method that adds all authorized role based access rules.
     */    
    private void insertAvailableRoleAccessRules(ArrayList accessrules){
        
      accessrules.add(ROLEACCESSRULES[0]);
      accessrules.add(ROLEACCESSRULES[1]); 
        
      if(issuperadministrator)  
        accessrules.add(ROLEACCESSRULES[2]);
      
    }

    /**
     * Method that adds all regular access rules.
     */    
    
    private void insertAvailableRegularAccessRules(Admin admin, ArrayList accessrules) {
       
      // Insert Standard Access Rules.
      for(int i=0; i < STANDARDREGULARACCESSRULES.length; i++){
         addAuthorizedAccessRule(admin, STANDARDREGULARACCESSRULES[i], accessrules);
      }
      for(int i=0; i < VIEWLOGACCESSRULES.length; i++){
         addAuthorizedAccessRule(admin, VIEWLOGACCESSRULES[i], accessrules);
      }      
      
        
      if(usehardtokenissuing){
        for(int i=0; i < HARDTOKENACCESSRULES.length;i++){
           accessrules.add(HARDTOKENACCESSRULES[i]);           
        }
        addAuthorizedAccessRule(admin, "/ra_functionality" + HARDTOKEN_RIGHTS, accessrules);        
      }
        
      if(usekeyrecovery)
         addAuthorizedAccessRule(admin, "/ra_functionality" + KEYRECOVERY_RIGHTS, accessrules);         
      
    }
    
    
    /**
     * Method that adds all authorized access rules conserning end entity profiles.
     */
    private void insertAvailableEndEntityProfileAccessRules(Admin admin, ArrayList accessrules){
        
        // Add most basic rule if authorized to it.
		try{
		  authorizer.isAuthorizedNoLog(admin, "/endentityprofilesrules");  
		  accessrules.add("/endentityprofilesrules");
		}catch(AuthorizationDeniedException e){
          //  Add it to superadministrator anyway
				 if(issuperadministrator)
				   accessrules.add("/endentityprofilesrules");
		}
		
        
        // Add all authorized End Entity Profiles                    
        Iterator iter = raadminsession.getAuthorizedEndEntityProfileIds(admin).iterator();
        while(iter.hasNext()){
            // Check if profiles available CAs is a subset of administrators authorized CAs
            int profileid = ((Integer) iter.next()).intValue();
            
              // Administrator is authorized to this End Entity Profile, add it.
                try{
                  authorizer.isAuthorizedNoLog(admin, ENDENTITYPROFILEPREFIX + profileid);  
                  addEndEntityProfile( profileid, accessrules);
                }catch(AuthorizationDeniedException e){}
            
        }
    }
    
    /** 
     * Help Method for insertAvailableEndEntityProfileAccessRules.
     */
    private void addEndEntityProfile(int profileid, ArrayList accessrules){
      accessrules.add(ENDENTITYPROFILEPREFIX + profileid);      
      for(int j=0;j < ENDENTITYPROFILE_ENDINGS.length; j++){     
        accessrules.add(ENDENTITYPROFILEPREFIX + profileid +ENDENTITYPROFILE_ENDINGS[j]);  
      }         
      if(usehardtokenissuing) 
        accessrules.add(ENDENTITYPROFILEPREFIX + profileid + HARDTOKEN_RIGHTS);     
      if(usekeyrecovery) 
        accessrules.add(ENDENTITYPROFILEPREFIX + profileid + KEYRECOVERY_RIGHTS);           
    }
      
    /**
     * Method that adds all authorized CA access rules.
     */
    private void insertAvailableCAAccessRules(ArrayList accessrules){
      // Add All Authorized CAs  
      Iterator iter = authorizedcaids.iterator();
      while(iter.hasNext()){
        accessrules.add(CAPREFIX + ((Integer) iter.next()).intValue());  
      }
    }
    
    /**
     * Method that adds the custom available access rules.
     */
    private void insertCustomAccessRules(Admin admin, ArrayList accessrules){
      for(int i=0; i < customaccessrules.length; i++){
        if(!customaccessrules[i].trim().equals(""))  
          addAuthorizedAccessRule(admin, customaccessrules[i].trim(), accessrules);    
      } 
    }
    
    /**
     * Method that checks if administrator himself is authorized to access rule, and if so adds it to list.
     */    
    private void addAuthorizedAccessRule(Admin admin, String accessrule, ArrayList accessrules){
      try{
        authorizer.isAuthorizedNoLog(admin, accessrule);
        accessrules.add(accessrule);
      }catch(AuthorizationDeniedException e){
      }
    }
    
   
    // Private fields
    private Authorizer authorizer;
    private IRaAdminSessionLocal raadminsession;
    private boolean issuperadministrator;
    private boolean enableendentityprofilelimitations;
    private boolean usehardtokenissuing;
    private boolean usekeyrecovery;
    private HashMap endentityprofiles;
    private HashSet authorizedcaids;
    private String[] customaccessrules;
    
   
}
