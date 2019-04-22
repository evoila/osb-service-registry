package de.evoila.osb.service.registry.util;

import de.evoila.osb.service.registry.properties.ServiceRegistryBean;
import de.evoila.osb.service.registry.web.request.services.BaseRequestService;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Note that the spring library to en- and decrypt depends on the JCE extension for JDKs / JREs prior:
 * <ul>
 *     <li>Java 9</li>
 *     <li>Java 8u161</li>
 *     <li>Java 7u171</li>
 *     <li>Java 6u181</li>
 * </ul>
 */
@Service
public class Cryptor {

    private ServiceRegistryBean props;

    public Cryptor(ServiceRegistryBean props) {
        this.props = props;
        BaseRequestService.setCryptor(this);
    }

    public boolean isInitialized() {
        return props != null && props.getEncryptionKey() != null;
    }

    public String getNewSalt() {
        return KeyGenerators.string().generateKey();
    }

    public String getBasicAuthEncoded(String username, String password) {
        return Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
    }

    public String encrypt(String salt, String value) {
        if (salt == null || salt.isEmpty() || value == null || value.isEmpty()) return "";
        TextEncryptor encryptor = Encryptors.text(new String(props.getEncryptionKey()), salt);
        String encrypted = encryptor.encrypt(value);
        return encrypted;
    }

    public String decrypt(String salt, String encrypted) {
        if (salt == null || salt.isEmpty() || encrypted == null || encrypted.isEmpty()) return "";
        TextEncryptor encryptor = Encryptors.text(new String(props.getEncryptionKey()), salt);
        String decrypted = encryptor.decrypt(encrypted);

        return decrypted;
    }
}
