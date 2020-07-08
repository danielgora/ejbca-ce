/*************************************************************************
 *                                                                       *
 *  EJBCA - Proprietary Modules: Enterprise Certificate Authority        *
 *                                                                       *
 *  Copyright (c), PrimeKey Solutions AB. All rights reserved.           *
 *  The use of the Proprietary Modules are subject to specific           * 
 *  commercial license terms.                                            *
 *                                                                       *
 *************************************************************************/
package org.ejbca.ssh.keys.ec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

import org.cesecore.certificates.certificate.ssh.SshKeyException;
import org.cesecore.util.Base64;
import org.cesecore.util.CryptoProviderTools;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * SSH EC Keys tests.
 *
 * @version $Id$
 */
public class SshEcKeysUnitTest {

    //EC keys produced by OpenSSH
    private static final String P384_KEY_BODY = "AAAAE2VjZHNhLXNoYTItbmlzdHAzODQAAAAIbmlzdHAzODQAAABhBNhgxk6D6poojS4qxRNVe6l/eWHGI35tMUJQ/oyOL4Be+NVOjzYOL4iQvays0x7AJEA1gezrbetQcruhGTOGEaSK72eFYMARZOf3JFVz0VpdpC84DiU0/4pQlnXKwRtwfw==";
    private static final String P256_KEY_BODY = "AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBOLCdH4MCabYjOjFsgMz8wfbrqVCiMjuOGw1AxbVrpZZ/CD7o1JngufaQNYyTz8Li7JV4SqQxTcTy13JHYf82Ro=";
    private static final String P521_KEY_BODY = "AAAAE2VjZHNhLXNoYTItbmlzdHA1MjEAAAAIbmlzdHA1MjEAAACFBABKU9MAY42Gu1N4OHWx/Wi7CZbZ8tDHB7Y9dBpIr6mWe3O8ICNKP3ppRH4jtTi8A+oxwIQqEzfcbbAN/qJ6OiXnnQHVzMGNMR+JV3yRv9MQIaLKnF6gUZZ5ZHMBvqh963W/wd/onCClsytuAqmDxhFV3MIrWi/HNkrG/IrQHM/ePwO8Mw==";

    @BeforeClass
    public static void beforeClass() {
        CryptoProviderTools.installBCProviderIfNotAvailable();
    }

    /**
     * Reads an P384 Public Key produced by OpenSSH
     */
    @Test
    public void readP384PublicKey() throws InvalidKeySpecException, SshKeyException {
        byte[] decoded = Base64.decode(P384_KEY_BODY.getBytes());
        SshEcPublicKey sshEcPublicKey = new SshEcPublicKey(decoded);
        ECPublicKey ecPublicKey = (ECPublicKey) sshEcPublicKey.getPublicKey();
        assertEquals("Incorrect algorithm", "EC", ecPublicKey.getAlgorithm());
    }

    /**
     * Reads an P384 Public Key produced by OpenSSH
     */
    @Test
    public void readP256PublicKey() throws InvalidKeySpecException, SshKeyException {
        byte[] decoded = Base64.decode(P256_KEY_BODY.getBytes());
        SshEcPublicKey sshEcPublicKey = new SshEcPublicKey(decoded);
        ECPublicKey ecPublicKey = (ECPublicKey) sshEcPublicKey.getPublicKey();
        assertEquals("Incorrect algorithm", "EC", ecPublicKey.getAlgorithm());
    }

    /**
     * Reads an P521 Public Key produced by OpenSSH
     */
    @Test
    public void readP521PublicKey() throws InvalidKeySpecException, SshKeyException {
        byte[] decoded = Base64.decode(P521_KEY_BODY.getBytes());
        SshEcPublicKey sshEcPublicKey = new SshEcPublicKey(decoded);
        ECPublicKey ecPublicKey = (ECPublicKey) sshEcPublicKey.getPublicKey();
        assertEquals("Incorrect algorithm", "EC", ecPublicKey.getAlgorithm());
    }

    @Test
    public void ecKeysExport() throws IOException, InvalidKeySpecException {
        SshEcKeyPair sshEcKeyPair = new SshEcKeyPair("nistp384");
        final String commment = "P384 Keypair generated by EJBCA";
        String exportedPublicKey = new String(sshEcKeyPair.getPublicKey().encodeForExport(commment));
        assertTrue("Exported public key did not contain EC prefix.", exportedPublicKey.startsWith(SshEcPublicKey.ENCODING_ALGORITHM_BASE));
        assertTrue("Exported public key did not end with comment.", exportedPublicKey.endsWith(commment));
    }
}
