
package org.ejbca.core.protocol.ws.client.gen;

import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;


/**
 * This class was generated by the JAXWS SI.
 * JAX-WS RI 2.0_01-b59-fcs
 * Generated source version: 2.0
 * 
 */
@WebService(name = "EjbcaWS", targetNamespace = "http://ws.protocol.core.ejbca.org/")
public interface EjbcaWS {


    /**
     * 
     * @param arg1
     * @param arg0
     * @return
     *     returns org.ejbca.core.protocol.ws.client.gen.Certificate
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getCertificate", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetCertificate")
    @ResponseWrapper(localName = "getCertificateResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetCertificateResponse")
    public Certificate getCertificate(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1)
        throws AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception
    ;

    /**
     * 
     * @return
     *     returns java.util.List<org.ejbca.core.protocol.ws.client.gen.NameAndId>
     * @throws AuthorizationDeniedException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getAvailableCAs", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetAvailableCAs")
    @ResponseWrapper(localName = "getAvailableCAsResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetAvailableCAsResponse")
    public List<NameAndId> getAvailableCAs()
        throws AuthorizationDeniedException_Exception, EjbcaException_Exception
    ;

    /**
     * 
     * @param arg0
     * @return
     *     returns int
     * @throws ApprovalRequestExpiredException_Exception
     * @throws EjbcaException_Exception
     * @throws ApprovalException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "isApproved", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.IsApproved")
    @ResponseWrapper(localName = "isApprovedResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.IsApprovedResponse")
    public int isApproved(
        @WebParam(name = "arg0", targetNamespace = "")
        int arg0)
        throws ApprovalException_Exception, ApprovalRequestExpiredException_Exception, EjbcaException_Exception
    ;

    /**
     * 
     * @param arg0
     * @return
     *     returns boolean
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "isAuthorized", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.IsAuthorized")
    @ResponseWrapper(localName = "isAuthorizedResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.IsAuthorizedResponse")
    public boolean isAuthorized(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0)
        throws EjbcaException_Exception
    ;

    /**
     * 
     * @param arg0
     * @return
     *     returns java.util.List<org.ejbca.core.protocol.ws.client.gen.UserDataVOWS>
     * @throws AuthorizationDeniedException_Exception
     * @throws IllegalQueryException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "findUser", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.FindUser")
    @ResponseWrapper(localName = "findUserResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.FindUserResponse")
    public List<UserDataVOWS> findUser(
        @WebParam(name = "arg0", targetNamespace = "")
        UserMatch arg0)
        throws AuthorizationDeniedException_Exception, EjbcaException_Exception, IllegalQueryException_Exception
    ;

    /**
     * 
     * @param arg0
     * @return
     *     returns boolean
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "existsHardToken", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.ExistsHardToken")
    @ResponseWrapper(localName = "existsHardTokenResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.ExistsHardTokenResponse")
    public boolean existsHardToken(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0)
        throws EjbcaException_Exception
    ;

    /**
     * 
     * @param arg2
     * @param arg1
     * @param arg0
     * @throws AuthorizationDeniedException_Exception
     * @throws WaitingForApprovalException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws AlreadyRevokedException_Exception
     * @throws NotFoundException_Exception
     * @throws EjbcaException_Exception
     * @throws ApprovalException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "revokeUser", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.RevokeUser")
    @ResponseWrapper(localName = "revokeUserResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.RevokeUserResponse")
    public void revokeUser(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        int arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        boolean arg2)
        throws AlreadyRevokedException_Exception, ApprovalException_Exception, AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, NotFoundException_Exception, WaitingForApprovalException_Exception
    ;

    /**
     * 
     * @param arg2
     * @param arg1
     * @param arg0
     * @throws WaitingForApprovalException_Exception
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws AlreadyRevokedException_Exception
     * @throws NotFoundException_Exception
     * @throws EjbcaException_Exception
     * @throws ApprovalException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "revokeCert", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.RevokeCert")
    @ResponseWrapper(localName = "revokeCertResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.RevokeCertResponse")
    public void revokeCert(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        int arg2)
        throws AlreadyRevokedException_Exception, ApprovalException_Exception, AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, NotFoundException_Exception, WaitingForApprovalException_Exception
    ;

    /**
     * 
     * @param arg0
     * @throws ApprovalRequestExpiredException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws EjbcaException_Exception
     * @throws ApprovalException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "createCRL", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CreateCRL")
    @ResponseWrapper(localName = "createCRLResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CreateCRLResponse")
    public void createCRL(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0)
        throws ApprovalException_Exception, ApprovalRequestExpiredException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception
    ;

    /**
     * 
     * @param arg0
     * @return
     *     returns int
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getPublisherQueueLength", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetPublisherQueueLength")
    @ResponseWrapper(localName = "getPublisherQueueLengthResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetPublisherQueueLengthResponse")
    public int getPublisherQueueLength(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0)
        throws EjbcaException_Exception
    ;

    /**
     * 
     * @param arg0
     * @throws AuthorizationDeniedException_Exception
     * @throws WaitingForApprovalException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws UserDoesntFullfillEndEntityProfile_Exception
     * @throws EjbcaException_Exception
     * @throws ApprovalException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "editUser", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.EditUser")
    @ResponseWrapper(localName = "editUserResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.EditUserResponse")
    public void editUser(
        @WebParam(name = "arg0", targetNamespace = "")
        UserDataVOWS arg0)
        throws ApprovalException_Exception, AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, UserDoesntFullfillEndEntityProfile_Exception, WaitingForApprovalException_Exception
    ;

    /**
     * 
     * @param arg1
     * @param arg0
     * @return
     *     returns java.util.List<org.ejbca.core.protocol.ws.client.gen.Certificate>
     * @throws AuthorizationDeniedException_Exception
     * @throws NotFoundException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "findCerts", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.FindCerts")
    @ResponseWrapper(localName = "findCertsResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.FindCertsResponse")
    public List<Certificate> findCerts(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        boolean arg1)
        throws AuthorizationDeniedException_Exception, EjbcaException_Exception, NotFoundException_Exception
    ;

    /**
     * 
     * @param arg0
     * @return
     *     returns java.util.List<org.ejbca.core.protocol.ws.client.gen.Certificate>
     * @throws AuthorizationDeniedException_Exception
     * @throws NotFoundException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getLastCertChain", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetLastCertChain")
    @ResponseWrapper(localName = "getLastCertChainResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetLastCertChainResponse")
    public List<Certificate> getLastCertChain(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0)
        throws AuthorizationDeniedException_Exception, EjbcaException_Exception, NotFoundException_Exception
    ;

    /**
     * 
     * @param arg4
     * @param arg3
     * @param arg2
     * @param arg1
     * @param arg0
     * @return
     *     returns org.ejbca.core.protocol.ws.client.gen.CertificateResponse
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws NotFoundException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "crmfRequest", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CrmfRequest")
    @ResponseWrapper(localName = "crmfRequestResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CrmfRequestResponse")
    public CertificateResponse crmfRequest(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        String arg2,
        @WebParam(name = "arg3", targetNamespace = "")
        String arg3,
        @WebParam(name = "arg4", targetNamespace = "")
        String arg4)
        throws AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, NotFoundException_Exception
    ;

    /**
     * 
     * @param arg4
     * @param arg3
     * @param arg2
     * @param arg1
     * @param arg0
     * @return
     *     returns org.ejbca.core.protocol.ws.client.gen.CertificateResponse
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws NotFoundException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "spkacRequest", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.SpkacRequest")
    @ResponseWrapper(localName = "spkacRequestResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.SpkacRequestResponse")
    public CertificateResponse spkacRequest(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        String arg2,
        @WebParam(name = "arg3", targetNamespace = "")
        String arg3,
        @WebParam(name = "arg4", targetNamespace = "")
        String arg4)
        throws AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, NotFoundException_Exception
    ;

    /**
     * 
     * @param arg2
     * @param arg1
     * @param arg0
     * @return
     *     returns java.util.List<org.ejbca.core.protocol.ws.client.gen.Certificate>
     * @throws WaitingForApprovalException_Exception
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws UserDoesntFullfillEndEntityProfile_Exception
     * @throws CertificateExpiredException_Exception
     * @throws NotFoundException_Exception
     * @throws EjbcaException_Exception
     * @throws SignRequestException_Exception
     * @throws ApprovalException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "cvcRequest", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CvcRequest")
    @ResponseWrapper(localName = "cvcRequestResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CvcRequestResponse")
    public List<Certificate> cvcRequest(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        String arg2)
        throws ApprovalException_Exception, AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, CertificateExpiredException_Exception, EjbcaException_Exception, NotFoundException_Exception, SignRequestException_Exception, UserDoesntFullfillEndEntityProfile_Exception, WaitingForApprovalException_Exception
    ;

    /**
     * 
     * @param arg5
     * @param arg4
     * @param arg3
     * @param arg2
     * @param arg1
     * @param arg0
     * @return
     *     returns byte[]
     * @throws WaitingForApprovalException_Exception
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws EjbcaException_Exception
     * @throws ApprovalException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "caRenewCertRequest", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CaRenewCertRequest")
    @ResponseWrapper(localName = "caRenewCertRequestResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CaRenewCertRequestResponse")
    public byte[] caRenewCertRequest(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        List<byte[]> arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        boolean arg2,
        @WebParam(name = "arg3", targetNamespace = "")
        boolean arg3,
        @WebParam(name = "arg4", targetNamespace = "")
        boolean arg4,
        @WebParam(name = "arg5", targetNamespace = "")
        String arg5)
        throws ApprovalException_Exception, AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, WaitingForApprovalException_Exception
    ;

    /**
     * 
     * @param arg3
     * @param arg2
     * @param arg1
     * @param arg0
     * @throws AuthorizationDeniedException_Exception
     * @throws WaitingForApprovalException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws EjbcaException_Exception
     * @throws ApprovalException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "caCertResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CaCertResponse")
    @ResponseWrapper(localName = "caCertResponseResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CaCertResponseResponse")
    public void caCertResponse(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        byte[] arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        List<byte[]> arg2,
        @WebParam(name = "arg3", targetNamespace = "")
        String arg3)
        throws ApprovalException_Exception, AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, WaitingForApprovalException_Exception
    ;

    /**
     * 
     * @param arg4
     * @param arg3
     * @param arg2
     * @param arg1
     * @param arg0
     * @return
     *     returns org.ejbca.core.protocol.ws.client.gen.CertificateResponse
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws NotFoundException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "pkcs10Request", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.Pkcs10Request")
    @ResponseWrapper(localName = "pkcs10RequestResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.Pkcs10RequestResponse")
    public CertificateResponse pkcs10Request(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        String arg2,
        @WebParam(name = "arg3", targetNamespace = "")
        String arg3,
        @WebParam(name = "arg4", targetNamespace = "")
        String arg4)
        throws AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, NotFoundException_Exception
    ;

    /**
     * 
     * @param arg4
     * @param arg3
     * @param arg2
     * @param arg1
     * @param arg0
     * @return
     *     returns org.ejbca.core.protocol.ws.client.gen.KeyStore
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws NotFoundException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "pkcs12Req", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.Pkcs12Req")
    @ResponseWrapper(localName = "pkcs12ReqResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.Pkcs12ReqResponse")
    public KeyStore pkcs12Req(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        String arg2,
        @WebParam(name = "arg3", targetNamespace = "")
        String arg3,
        @WebParam(name = "arg4", targetNamespace = "")
        String arg4)
        throws AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, NotFoundException_Exception
    ;

    /**
     * 
     * @param arg0
     * @throws WaitingForApprovalException_Exception
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws NotFoundException_Exception
     * @throws EjbcaException_Exception
     * @throws ApprovalException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "keyRecoverNewest", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.KeyRecoverNewest")
    @ResponseWrapper(localName = "keyRecoverNewestResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.KeyRecoverNewestResponse")
    public void keyRecoverNewest(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0)
        throws ApprovalException_Exception, AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, NotFoundException_Exception, WaitingForApprovalException_Exception
    ;

    /**
     * 
     * @param arg1
     * @param arg0
     * @throws WaitingForApprovalException_Exception
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws AlreadyRevokedException_Exception
     * @throws NotFoundException_Exception
     * @throws EjbcaException_Exception
     * @throws ApprovalException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "revokeToken", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.RevokeToken")
    @ResponseWrapper(localName = "revokeTokenResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.RevokeTokenResponse")
    public void revokeToken(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        int arg1)
        throws AlreadyRevokedException_Exception, ApprovalException_Exception, AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, NotFoundException_Exception, WaitingForApprovalException_Exception
    ;

    /**
     * 
     * @param arg1
     * @param arg0
     * @return
     *     returns org.ejbca.core.protocol.ws.client.gen.RevokeStatus
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "checkRevokationStatus", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CheckRevokationStatus")
    @ResponseWrapper(localName = "checkRevokationStatusResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CheckRevokationStatusResponse")
    public RevokeStatus checkRevokationStatus(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1)
        throws AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception
    ;

    /**
     * 
     * @param arg1
     * @param arg0
     * @return
     *     returns java.util.List<org.ejbca.core.protocol.ws.client.gen.UserDataSourceVOWS>
     * @throws AuthorizationDeniedException_Exception
     * @throws EjbcaException_Exception
     * @throws UserDataSourceException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "fetchUserData", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.FetchUserData")
    @ResponseWrapper(localName = "fetchUserDataResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.FetchUserDataResponse")
    public List<UserDataSourceVOWS> fetchUserData(
        @WebParam(name = "arg0", targetNamespace = "")
        List<String> arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1)
        throws AuthorizationDeniedException_Exception, EjbcaException_Exception, UserDataSourceException_Exception
    ;

    /**
     * 
     * @param arg4
     * @param arg3
     * @param arg2
     * @param arg1
     * @param arg0
     * @return
     *     returns java.util.List<org.ejbca.core.protocol.ws.client.gen.TokenCertificateResponseWS>
     * @throws ApprovalRequestExpiredException_Exception
     * @throws AuthorizationDeniedException_Exception
     * @throws WaitingForApprovalException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws UserDoesntFullfillEndEntityProfile_Exception
     * @throws ApprovalRequestExecutionException_Exception
     * @throws HardTokenExistsException_Exception
     * @throws EjbcaException_Exception
     * @throws ApprovalException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "genTokenCertificates", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GenTokenCertificates")
    @ResponseWrapper(localName = "genTokenCertificatesResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GenTokenCertificatesResponse")
    public List<TokenCertificateResponseWS> genTokenCertificates(
        @WebParam(name = "arg0", targetNamespace = "")
        UserDataVOWS arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        List<TokenCertificateRequestWS> arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        HardTokenDataWS arg2,
        @WebParam(name = "arg3", targetNamespace = "")
        boolean arg3,
        @WebParam(name = "arg4", targetNamespace = "")
        boolean arg4)
        throws ApprovalException_Exception, ApprovalRequestExecutionException_Exception, ApprovalRequestExpiredException_Exception, AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, HardTokenExistsException_Exception, UserDoesntFullfillEndEntityProfile_Exception, WaitingForApprovalException_Exception
    ;

    /**
     * 
     * @param arg2
     * @param arg1
     * @param arg0
     * @return
     *     returns org.ejbca.core.protocol.ws.client.gen.HardTokenDataWS
     * @throws ApprovalRequestExpiredException_Exception
     * @throws AuthorizationDeniedException_Exception
     * @throws WaitingForApprovalException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws ApprovalRequestExecutionException_Exception
     * @throws HardTokenDoesntExistsException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getHardTokenData", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetHardTokenData")
    @ResponseWrapper(localName = "getHardTokenDataResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetHardTokenDataResponse")
    public HardTokenDataWS getHardTokenData(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        boolean arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        boolean arg2)
        throws ApprovalRequestExecutionException_Exception, ApprovalRequestExpiredException_Exception, AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, HardTokenDoesntExistsException_Exception, WaitingForApprovalException_Exception
    ;

    /**
     * 
     * @param arg2
     * @param arg1
     * @param arg0
     * @return
     *     returns java.util.List<org.ejbca.core.protocol.ws.client.gen.HardTokenDataWS>
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getHardTokenDatas", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetHardTokenDatas")
    @ResponseWrapper(localName = "getHardTokenDatasResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetHardTokenDatasResponse")
    public List<HardTokenDataWS> getHardTokenDatas(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        boolean arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        boolean arg2)
        throws AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception
    ;

    /**
     * 
     * @param arg1
     * @param arg0
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws PublisherException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "republishCertificate", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.RepublishCertificate")
    @ResponseWrapper(localName = "republishCertificateResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.RepublishCertificateResponse")
    public void republishCertificate(
        @WebParam(name = "arg0", targetNamespace = "")
        String arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1)
        throws AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception, PublisherException_Exception
    ;

    /**
     * 
     * @param arg5
     * @param arg4
     * @param arg3
     * @param arg2
     * @param arg1
     * @param arg0
     * @throws AuthorizationDeniedException_Exception
     * @throws CADoesntExistsException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @RequestWrapper(localName = "customLog", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CustomLog")
    @ResponseWrapper(localName = "customLogResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.CustomLogResponse")
    public void customLog(
        @WebParam(name = "arg0", targetNamespace = "")
        int arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        String arg2,
        @WebParam(name = "arg3", targetNamespace = "")
        String arg3,
        @WebParam(name = "arg4", targetNamespace = "")
        Certificate arg4,
        @WebParam(name = "arg5", targetNamespace = "")
        String arg5)
        throws AuthorizationDeniedException_Exception, CADoesntExistsException_Exception, EjbcaException_Exception
    ;

    /**
     * 
     * @param arg2
     * @param arg1
     * @param arg0
     * @return
     *     returns boolean
     * @throws AuthorizationDeniedException_Exception
     * @throws EjbcaException_Exception
     * @throws MultipleMatchException_Exception
     * @throws UserDataSourceException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "deleteUserDataFromSource", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.DeleteUserDataFromSource")
    @ResponseWrapper(localName = "deleteUserDataFromSourceResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.DeleteUserDataFromSourceResponse")
    public boolean deleteUserDataFromSource(
        @WebParam(name = "arg0", targetNamespace = "")
        List<String> arg0,
        @WebParam(name = "arg1", targetNamespace = "")
        String arg1,
        @WebParam(name = "arg2", targetNamespace = "")
        boolean arg2)
        throws AuthorizationDeniedException_Exception, EjbcaException_Exception, MultipleMatchException_Exception, UserDataSourceException_Exception
    ;

    /**
     * 
     * @return
     *     returns java.util.List<org.ejbca.core.protocol.ws.client.gen.NameAndId>
     * @throws AuthorizationDeniedException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getAuthorizedEndEntityProfiles", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetAuthorizedEndEntityProfiles")
    @ResponseWrapper(localName = "getAuthorizedEndEntityProfilesResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetAuthorizedEndEntityProfilesResponse")
    public List<NameAndId> getAuthorizedEndEntityProfiles()
        throws AuthorizationDeniedException_Exception, EjbcaException_Exception
    ;

    /**
     * 
     * @param arg0
     * @return
     *     returns java.util.List<org.ejbca.core.protocol.ws.client.gen.NameAndId>
     * @throws AuthorizationDeniedException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getAvailableCertificateProfiles", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetAvailableCertificateProfiles")
    @ResponseWrapper(localName = "getAvailableCertificateProfilesResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetAvailableCertificateProfilesResponse")
    public List<NameAndId> getAvailableCertificateProfiles(
        @WebParam(name = "arg0", targetNamespace = "")
        int arg0)
        throws AuthorizationDeniedException_Exception, EjbcaException_Exception
    ;

    /**
     * 
     * @param arg0
     * @return
     *     returns java.util.List<org.ejbca.core.protocol.ws.client.gen.NameAndId>
     * @throws AuthorizationDeniedException_Exception
     * @throws EjbcaException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getAvailableCAsInProfile", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetAvailableCAsInProfile")
    @ResponseWrapper(localName = "getAvailableCAsInProfileResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetAvailableCAsInProfileResponse")
    public List<NameAndId> getAvailableCAsInProfile(
        @WebParam(name = "arg0", targetNamespace = "")
        int arg0)
        throws AuthorizationDeniedException_Exception, EjbcaException_Exception
    ;

    /**
     * 
     * @return
     *     returns java.lang.String
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "getEjbcaVersion", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetEjbcaVersion")
    @ResponseWrapper(localName = "getEjbcaVersionResponse", targetNamespace = "http://ws.protocol.core.ejbca.org/", className = "org.ejbca.core.protocol.ws.client.gen.GetEjbcaVersionResponse")
    public String getEjbcaVersion();

}
