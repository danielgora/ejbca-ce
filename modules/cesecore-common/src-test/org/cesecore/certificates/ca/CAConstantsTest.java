/*************************************************************************
 *                                                                       *
 *  CESeCore: CE Security Core                                           *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

package org.cesecore.certificates.ca;

import org.cesecore.certificates.util.AlgorithmConstants;
import org.cesecore.keys.util.KeyTools;
import org.cesecore.util.CryptoProviderTools;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class CAConstantsTest {

    @Test
    public void testGetPreSignKeys() {
        CryptoProviderTools.installBCProviderIfNotAvailable();
        assertNotNull("Should find presign DSA key",
                CAConstants.getPreSignPublicKey(
                        AlgorithmConstants.SIGALG_SHA256_WITH_DSA,
                        KeyTools.getKeyPairFromPEM(CAConstants.PRESIGN_VALIDATION_KEY_DSA_PRIV).getPublic()));
        assertNotNull("Should find presign RSA key",
                CAConstants.getPreSignPublicKey(
                        AlgorithmConstants.SIGALG_SHA256_WITH_RSA,
                        KeyTools.getKeyPairFromPEM(CAConstants.PRESIGN_VALIDATION_KEY_RSA_PRIV).getPublic()));
        assertNotNull("Should find presign secp256r1 key",
                CAConstants.getPreSignPublicKey(
                        AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA,
                        KeyTools.getKeyPairFromPEM(CAConstants.PRESIGN_VALIDATION_KEY_EC_SECP256R1_PRIV).getPublic()));
        assertNotNull("Should find presign secp384r1 key",
                CAConstants.getPreSignPublicKey(
                        AlgorithmConstants.SIGALG_SHA256_WITH_ECDSA,
                        KeyTools.getKeyPairFromPEM(CAConstants.PRESIGN_VALIDATION_KEY_EC_SECP384R1_PRIV).getPublic()));
        assertNotNull("Should find presign Ed25519 key",
                CAConstants.getPreSignPublicKey(
                        AlgorithmConstants.KEYALGORITHM_ED25519,
                        KeyTools.getKeyPairFromPEM(CAConstants.PRESIGN_VALIDATION_KEY_ED25519_PRIV).getPublic()));
        assertNotNull("Should find presign Ed448 key",
                CAConstants.getPreSignPublicKey(
                        AlgorithmConstants.KEYALGORITHM_ED448,
                        KeyTools.getKeyPairFromPEM(CAConstants.PRESIGN_VALIDATION_KEY_ED448_PRIV).getPublic()));
    }
}
