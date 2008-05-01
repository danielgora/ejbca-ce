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
 
package org.ejbca.ui.cli;

import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;

import org.ejbca.core.model.ca.caadmin.CAInfo;
import org.ejbca.util.CertTools;



/**
 * Gets and prints info about the CA.
 *
 * @version $Id$
 */
public class CaInfoCommand extends BaseCaAdminCommand {
    /**
     * Creates a new instance of CaInfoCommand
     *
     * @param args command line arguments
     */
    public CaInfoCommand(String[] args) {
        super(args);
    }

    /**
     * Runs the command
     *
     * @throws IllegalAdminCommandException Error in command args
     * @throws ErrorAdminCommandException Error running command
     */
    public void execute() throws IllegalAdminCommandException, ErrorAdminCommandException {
        if (args.length < 2) {
           String msg = "Usage: CA info <caname>";               
           throw new IllegalAdminCommandException(msg);
        }
        try {            
            String caname = args[1];
            ArrayList chain = new ArrayList(getCertChain(caname));
            CAInfo cainfo = getCAInfo(caname);
                                    
            getOutputStream().println("CA name: " + caname);
            getOutputStream().println("CA ID: " + cainfo.getCAId());
            getOutputStream().println("CA CRL Expiration Period: " + cainfo.getCRLPeriod());
            getOutputStream().println("CA CRL Issue Interval: " + cainfo.getCRLIssueInterval());
            getOutputStream().println("CA Description: " + cainfo.getDescription());
            getOutputStream().println("\n");
            
            if (chain.size() < 2)
              getOutputStream().println("This is a Root CA.");
            else
              getOutputStream().println("This is a subordinate CA.");
              
              getOutputStream().println("Size of chain: " + chain.size());
            if (chain.size() > 0) {
                X509Certificate rootcert = (X509Certificate)chain.get(chain.size()-1);
                getOutputStream().println("Root CA DN: "+CertTools.getSubjectDN(rootcert));
                getOutputStream().println("Root CA id: "+CertTools.getSubjectDN(rootcert).hashCode());
                getOutputStream().println("Certificate valid from: "+rootcert.getNotBefore().toString());
                getOutputStream().println("Certificate valid to: "+rootcert.getNotAfter().toString());
                getOutputStream().println("Root CA keysize: "+((RSAPublicKey)rootcert.getPublicKey()).getModulus().bitLength());            
                for(int i = chain.size()-2; i>=0; i--){                                          
                    X509Certificate cacert = (X509Certificate)chain.get(i);
                    getOutputStream().println("CA DN: "+CertTools.getSubjectDN(cacert));
                    getOutputStream().println("Certificate valid from: "+cacert.getNotBefore().toString());
                    getOutputStream().println("Certificate valid to: "+cacert.getNotAfter().toString());
                    getOutputStream().println("CA keysize: "+((RSAPublicKey)cacert.getPublicKey()).getModulus().bitLength());

                }                
            }
        } catch (Exception e) {
            throw new ErrorAdminCommandException(e);
        }
    } // execute
}