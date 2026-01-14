package com.contractreview.fileapi.exception;

/**
 * 文件存储异常
 * 
 * @author ContractReview Team
 * @version 1.0.0
 */
public class FileStorageException extends RuntimeException {
    
    private final String errorCode;
    
    public FileStorageException(String message) {
        super(message);
        this.errorCode = "FILE_STORAGE_ERROR";
    }
    
    public FileStorageException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "FILE_STORAGE_ERROR";
    }
    
    public FileStorageException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}