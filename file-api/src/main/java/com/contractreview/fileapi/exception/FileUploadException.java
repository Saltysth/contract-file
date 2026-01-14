package com.contractreview.fileapi.exception;

/**
 * 文件上传异常
 * 
 * @author ContractReview Team
 * @version 1.0.0
 */
public class FileUploadException extends FileStorageException {
    
    public FileUploadException(String message) {
        super("FILE_UPLOAD_ERROR", message);
    }
    
    public FileUploadException(String message, Throwable cause) {
        super("FILE_UPLOAD_ERROR", message, cause);
    }
}