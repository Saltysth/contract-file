package com.contractreview.filestorage.domain.model.valueobject;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;

/**
 * 加密元数据值对象
 * 
 * @author ContractReview Team
 */
@Value
public class EncryptionMetadata {
    
    private static final String DEFAULT_ALGORITHM = "AES-256-CBC";

    Boolean isEncrypted;
    String encryptionAlgorithm;

    private EncryptionMetadata(Boolean isEncrypted, String encryptionAlgorithm) {
        this.isEncrypted = isEncrypted != null ? isEncrypted : false;
        this.encryptionAlgorithm = this.isEncrypted ? 
            (StringUtils.isNotBlank(encryptionAlgorithm) ? encryptionAlgorithm : DEFAULT_ALGORITHM) : null;
    }

    /**
     * 创建未加密的元数据
     */
    public static EncryptionMetadata unencrypted() {
        return new EncryptionMetadata(false, null);
    }

    /**
     * 创建加密的元数据
     */
    public static EncryptionMetadata encrypted() {
        return new EncryptionMetadata(true, DEFAULT_ALGORITHM);
    }

    /**
     * 创建指定算法的加密元数据
     */
    public static EncryptionMetadata encrypted(String algorithm) {
        return new EncryptionMetadata(true, algorithm);
    }

    /**
     * 从现有数据创建
     */
    public static EncryptionMetadata of(Boolean isEncrypted, String encryptionAlgorithm) {
        return new EncryptionMetadata(isEncrypted, encryptionAlgorithm);
    }

    /**
     * 是否支持的加密算法
     */
    public boolean isSupportedAlgorithm() {
        return DEFAULT_ALGORITHM.equals(encryptionAlgorithm);
    }

    /**
     * 获取算法简称
     */
    public String getAlgorithmShortName() {
        if (!isEncrypted || StringUtils.isBlank(encryptionAlgorithm)) {
            return null;
        }
        return encryptionAlgorithm.replace("-", "").toLowerCase();
    }
}