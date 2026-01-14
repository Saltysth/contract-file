package com.contractreview.filestorage.infrastructure.security;

/**
 * 加密提供者接口
 * 
 * @author ContractReview Team
 */
public interface EncryptionProvider {

    /**
     * 加密数据
     */
    byte[] encrypt(byte[] data, String key);

    /**
     * 解密数据
     */
    byte[] decrypt(byte[] encryptedData, String key);

    /**
     * 验证密钥格式
     */
    boolean validateKey(String key);

    /**
     * 获取算法名称
     */
    String getAlgorithmName();
}