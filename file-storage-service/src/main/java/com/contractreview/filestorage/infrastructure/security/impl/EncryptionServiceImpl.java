package com.contractreview.filestorage.infrastructure.security.impl;

import com.contractreview.filestorage.domain.service.EncryptionService;
import com.contractreview.filestorage.infrastructure.security.EncryptionProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 加密服务实现
 * 
 * @author ContractReview Team
 */
@Service
@RequiredArgsConstructor
public class EncryptionServiceImpl implements EncryptionService {

    private final EncryptionProvider encryptionProvider;

    @Override
    public byte[] encrypt(byte[] data, String publicKey) {
        return encryptionProvider.encrypt(data, publicKey);
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, String publicKey) {
        return encryptionProvider.decrypt(encryptedData, publicKey);
    }

    @Override
    public boolean validatePublicKey(String publicKey) {
        return encryptionProvider.validateKey(publicKey);
    }
}