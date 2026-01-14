package com.contractreview.filestorage.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES加密提供者实现
 * 
 * @author ContractReview Team
 */
@Component
@Slf4j
public class AESEncryptionProvider implements EncryptionProvider {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM_NAME = "AES-256-CBC";
    private static final int KEY_LENGTH = 32; // 256 bits
    private static final int IV_LENGTH = 16; // 128 bits

    @Override
    public byte[] encrypt(byte[] data, String key) {
        try {
            if (!validateKey(key)) {
                throw new IllegalArgumentException("无效的加密密钥格式");
            }

            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            // 生成随机IV
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encryptedData = cipher.doFinal(data);

            // 将IV和加密数据合并
            byte[] result = new byte[IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, result, 0, IV_LENGTH);
            System.arraycopy(encryptedData, 0, result, IV_LENGTH, encryptedData.length);

            log.debug("数据加密成功，原始大小: {}, 加密后大小: {}", data.length, result.length);
            return result;

        } catch (Exception e) {
            log.error("数据加密失败", e);
            throw new RuntimeException("数据加密失败", e);
        }
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, String key) {
        try {
            if (!validateKey(key)) {
                throw new IllegalArgumentException("无效的解密密钥格式");
            }

            if (encryptedData.length < IV_LENGTH) {
                throw new IllegalArgumentException("加密数据格式错误");
            }

            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);

            // 提取IV和加密数据
            byte[] iv = Arrays.copyOfRange(encryptedData, 0, IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(encryptedData, IV_LENGTH, encryptedData.length);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decryptedData = cipher.doFinal(cipherText);

            log.debug("数据解密成功，加密大小: {}, 解密后大小: {}", encryptedData.length, decryptedData.length);
            return decryptedData;

        } catch (Exception e) {
            log.error("数据解密失败", e);
            throw new RuntimeException("数据解密失败", e);
        }
    }

    @Override
    public boolean validateKey(String key) {
        if (StringUtils.isBlank(key)) {
            return false;
        }

        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            return keyBytes.length == KEY_LENGTH;
        } catch (IllegalArgumentException e) {
            log.warn("密钥格式验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }
}