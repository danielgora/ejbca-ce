
package org.ejbca.core.protocol.ws.client.gen;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.0_01-b59-fcs
 * Generated source version: 2.0
 * 
 */
@WebFault(name = "UserDoesntFullfillEndEntityProfile", targetNamespace = "http://ws.protocol.core.ejbca.org/")
public class UserDoesntFullfillEndEntityProfile_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private UserDoesntFullfillEndEntityProfile faultInfo;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public UserDoesntFullfillEndEntityProfile_Exception(String message, UserDoesntFullfillEndEntityProfile faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @param message
     * @param faultInfo
     * @param cause
     */
    public UserDoesntFullfillEndEntityProfile_Exception(String message, UserDoesntFullfillEndEntityProfile faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * 
     * @return
     *     returns fault bean: org.ejbca.core.protocol.ws.client.gen.UserDoesntFullfillEndEntityProfile
     */
    public UserDoesntFullfillEndEntityProfile getFaultInfo() {
        return faultInfo;
    }

}
