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
 
package se.anatom.ejbca;

import java.rmi.RemoteException;

import se.anatom.ejbca.log.Admin;


/**
 * JobRunner session wraps around any class and is a general session bean that can be used to
 * launch a specified job.
 *
 * @version $Id: IJobRunnerSession.java,v 1.9 2004-04-16 07:39:01 anatom Exp $
 */
public interface IJobRunnerSession  {
    /**
     * Runs the job
     *
     * @param admin administrator running the job
     *
     * @throws RemoteException error
     */
    public void run(Admin admin,String issuerdn) throws RemoteException;
}
