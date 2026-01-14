package com.contractreview.filestorage.domain.service;

/**
 * 加密服务接口
 * 
 * @author ContractReview Team
 */
public interface EncryptionService {

    /**
     * 加密数据
     */
    byte[] encrypt(byte[] data, String publicKey);

    /**
     * 解密数据
     */
    byte[] decrypt(byte[] encryptedData, String publicKey);

    /**
     * 验证公钥格式
     */
    boolean validatePublicKey(String publicKey);
}