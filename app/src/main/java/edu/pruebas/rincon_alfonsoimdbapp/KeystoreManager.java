package edu.pruebas.rincon_alfonsoimdbapp;

import android.util.Base64;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

public class KeystoreManager {

    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS = "MyKeyAlias";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH = 128;

    public KeystoreManager() throws Exception {
        createKeyIfNeeded();
    }

    private void createKeyIfNeeded() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", ANDROID_KEYSTORE);
            KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build();
            keyGenerator.init(keySpec);
            keyGenerator.generateKey();
        }
    }

    /**
     * Cifra el texto plano y devuelve una cadena en Base64 que contiene el IV (generado automáticamente)
     * y el ciphertext concatenados.
     */
    public String encrypt(String plainText) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        SecretKey key = (SecretKey) keyStore.getKey(KEY_ALIAS, null);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        // Inicializamos sin proporcionar un IV, para que la infraestructura genere uno aleatorio
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] iv = cipher.getIV(); // Obtenemos el IV generado

        byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));

        // Combinar IV y ciphertext para almacenarlos juntos
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);
        byte[] combined = byteBuffer.array();

        return Base64.encodeToString(combined, Base64.DEFAULT);
    }

    /**
     * Descifra la cadena cifrada (en Base64) y devuelve el texto plano.
     */
    public String decrypt(String encryptedText) throws Exception {
        byte[] combined = Base64.decode(encryptedText, Base64.DEFAULT);
        ByteBuffer byteBuffer = ByteBuffer.wrap(combined);
        // Se asume que el IV tiene el tamaño que usa el proveedor (generalmente 12 bytes para GCM)
        byte[] iv = new byte[12];
        byteBuffer.get(iv);
        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        SecretKey key = (SecretKey) keyStore.getKey(KEY_ALIAS, null);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] plainBytes = cipher.doFinal(cipherText);
        return new String(plainBytes, "UTF-8");
    }
}
