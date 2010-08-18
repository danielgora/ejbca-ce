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

package org.ejbca.core.model.log;

import java.io.Serializable;
import java.security.cert.Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.FinderException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ejbca.config.OldLogConfiguration;
import org.ejbca.config.ProtectConfiguration;
import org.ejbca.core.ejb.JNDINames;
import org.ejbca.core.ejb.ServiceLocator;
import org.ejbca.core.ejb.log.LogConfigurationDataLocal;
import org.ejbca.core.ejb.log.LogConfigurationDataLocalHome;
import org.ejbca.core.ejb.log.LogEntryDataLocalHome;
import org.ejbca.core.ejb.protect.TableProtectSessionLocal;
import org.ejbca.core.ejb.protect.TableProtectSessionLocalHome;
import org.ejbca.core.model.InternalResources;
import org.ejbca.core.model.protect.TableVerifyResult;
import org.ejbca.core.model.util.EjbRemoteHelper;
import org.ejbca.util.CertTools;
import org.ejbca.util.JDBCUtil;
import org.ejbca.util.query.IllegalQueryException;
import org.ejbca.util.query.Query;

/**
 * Implements a log device using the old logging system, implements the Singleton pattern.
 * @version $Id$
 */
public class OldLogDevice implements ILogDevice, Serializable {
	
	public final static String DEFAULT_DEVICE_NAME = "OldLogDevice";
	
	/** Internal localization of logs and errors */
	private static final InternalResources intres = InternalResources.getInstance();

	private static final Logger log = Logger.getLogger(OldLogDevice.class);
	
    /** The come interface of the protection session bean */
    private TableProtectSessionLocalHome protecthome;
    /** The home interface of  LogEntryData entity bean */
    private LogEntryDataLocalHome logentryhome;
    /** The remote interface of the LogConfigurationData entity bean */
    private LogConfigurationDataLocal logconfigurationdata;
    /** The home interface of  LogConfigurationData entity bean */
    private LogConfigurationDataLocalHome logconfigurationhome;


	/**
	 * A handle to the unique Singleton instance.
	 */
	private static ILogDevice instance;

    /** Columns in the database used in select */
    private final String LOGENTRYDATA_TABLE = "LogEntryData";
    private final String LOGENTRYDATA_COL = "id, adminType, adminData, cAId, module, time, username, certificateSNR, event";
    private final String LOGENTRYDATA_TIMECOL = "time";
    private final String LOGENTRYDATA_COL_COMMENT = "logComment";

    private String deviceName = null;

    /** If signing of logs is enabled of not, default not */
    private boolean logsigning = OldLogConfiguration.getLogSigning() || ProtectConfiguration.getLogProtectionEnabled();

    /**
	 * Initializes
	 */
	protected OldLogDevice(String name) throws Exception {
		resetDevice(name);
	}

	/**
	 * @see org.ejbca.core.model.log.ILogDevice
	 */
	public void resetDevice(String name) {
		deviceName = name;
        logconfigurationhome = (LogConfigurationDataLocalHome) ServiceLocator.getInstance().getLocalHome(LogConfigurationDataLocalHome.COMP_NAME);
        logentryhome = (LogEntryDataLocalHome) ServiceLocator.getInstance().getLocalHome(LogEntryDataLocalHome.COMP_NAME);
        if (logsigning) {
        	protecthome = (TableProtectSessionLocalHome) ServiceLocator.getInstance().getLocalHome(TableProtectSessionLocalHome.COMP_NAME);
        }
	}

	/**
	 * Creates (if needed) the log device and returns the object.
	 *
	 * @param prop Arguments needed for the eventual creation of the object
	 * @return An instance of the log device.
	 */
	public static synchronized ILogDevice instance(String name) throws Exception {
		if (instance == null) {
			instance = new OldLogDevice(name);
		}
		return instance;
	}
	
    /**
     * Log everything in the database using the log entity bean
     */
	public void log(Admin admin, int caid, int module, Date time, String username, Certificate certificate, int event, String comment, Exception exception) {
		if (exception != null) {
			comment += ", Exception: " + exception.getMessage();
		}
		boolean successfulLog = false;
    	int tries = 0;
    	do{
    		try {
    			String uid = null;
    			if (certificate != null) {
    				uid = CertTools.getSerialNumberAsString(certificate) + "," + CertTools.getIssuerDN(certificate);        		
    			}
    			String admindata = admin.getAdminData();
    			if((event == LogConstants.EVENT_INFO_ADMINISTRATORLOGGEDIN) && (comment.contains("external CA"))){
    				admindata += " : CertDN : \"" + CertTools.getSubjectDN(admin.getAdminInformation().getX509Certificate()) + "\""; 
    			}
    			
    			Integer id = getAndIncrementRowCount();
    			logentryhome.create(id, admin.getAdminType(), admindata, caid, module, time, username, uid, event, comment);
    			if (logsigning) {
    				LogEntry le = new LogEntry(id.intValue(), admin.getAdminType(), admindata, caid, module, time, username, uid, event, comment);
    				TableProtectSessionLocal protect = protecthome.create();
    				protect.protect(le);
    			}
    			successfulLog = true;
    		} catch (Throwable e) {
    			tries++;
    			if(tries == 3){
        			// We are losing a db audit entry in this case.
    				String msg = intres.getLocalizedMessage("log.errormissingentry");            	
    				log.error(msg,e);
    			}else{
    				String msg = intres.getLocalizedMessage("log.warningduplicatekey");            	
    				log.warn(msg);
    			}
    			
    		}
    	}while(!successfulLog && tries < 3);
    }

	/**
	 * @see org.ejbca.core.model.log.ILogDevice
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * @see org.ejbca.core.model.log.ILogDevice
	 */
	public byte[] export(Admin admin, Query query, String viewlogprivileges, String capriviledges, ILogExporter logexporter, int maxResults) throws IllegalQueryException, Exception {
		byte[] ret = null;
		if (query != null) {
			Collection logentries = query(query, viewlogprivileges, capriviledges, maxResults);
			if (log.isDebugEnabled()) {
				log.debug("Found "+logentries.size()+" entries when exporting");    		
			}
			logexporter.setEntries(logentries);
			ret = logexporter.export(admin);
		}
		return ret;
	}

	/**
	 * Method to execute a customized query on the log db data. The parameter query should be a legal Query object.
	 *
	 * @param query a number of statements compiled by query class to a SQL 'WHERE'-clause statement.
	 * @param viewlogprivileges is a SQL query string returned by a LogAuthorization object.
	 * @param maxResults Maximum size of Collection
	 * @return a collection of LogEntry.
	 * @throws IllegalQueryException when query parameters internal rules isn't fulfilled.
	 * @see org.ejbca.util.query.Query
	 */
	public Collection query(Query query, String viewlogprivileges, String capriviledges, int maxResults) throws IllegalQueryException {
		log.trace(">query()");
		if (capriviledges == null || capriviledges.length() == 0 || !query.isLegalQuery()) {
			throw new IllegalQueryException();
		}

		Connection con = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// Construct SQL query.
			con = JDBCUtil.getDBConnection(JNDINames.DATASOURCE);
			String sql = "select "+LOGENTRYDATA_COL+", "+LOGENTRYDATA_COL_COMMENT+" from "+LOGENTRYDATA_TABLE+" where ( "
				+ query.getQueryString() + ") and (" + capriviledges + ")";
			if (StringUtils.isNotEmpty(viewlogprivileges)) {
				sql += " and (" + viewlogprivileges + ")";
			}
			sql += " order by "+LOGENTRYDATA_TIMECOL+" desc";
			if (log.isDebugEnabled()) {
				log.debug("Query: "+sql);
			}
			ps = con.prepareStatement(sql);
			//ps.setFetchDirection(ResultSet.FETCH_REVERSE);
			ps.setFetchSize(maxResults + 1);
			// Execute query.
			rs = ps.executeQuery();
			// Assemble result.
			ArrayList returnval = new ArrayList();
			while (rs.next() && returnval.size() <= maxResults) {
				LogEntry data = new LogEntry(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4), rs.getInt(5), new Date(rs.getLong(6)), rs.getString(7), 
						rs.getString(8), rs.getInt(9), rs.getString(10));
				if (logsigning) {
					TableProtectSessionLocal protect = protecthome.create();
					TableVerifyResult res = protect.verify(data);
					data.setVerifyResult(res.getResultConstant());
				}
				returnval.add(data);
			}
			return returnval;

		} catch (Exception e) {
			throw new EJBException(e);
		} finally {
			JDBCUtil.close(con, ps, rs);
		}
	} // query

    private Integer getAndIncrementRowCount() {
        if (this.logconfigurationdata == null) {
            try {
                logconfigurationdata = logconfigurationhome.findByPrimaryKey(new Integer(0));
            } catch (FinderException e) {
                try {
                    LogConfiguration logconfiguration = new LogConfiguration();
                    this.logconfigurationdata = logconfigurationhome.create(new Integer(0), logconfiguration);
                } catch (CreateException f) {
                    throw new EJBException(f);
                }
            }
        }

        return this.logconfigurationdata.getAndIncrementRowCount();
    }

	/**
	 * @see org.ejbca.core.model.log.ILogDevice
	 */
	public void destructor() {
		// No action needed
	}

	/**
	 * @see org.ejbca.core.model.log.ILogDevice
	 */
	public boolean getAllowConfigurableEvents() {
		return true;
	}
    

}
