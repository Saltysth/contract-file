package com.contractreview.filestorage.domain.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Bucket名称校验器测试
 * 
 * @author ContractReview Team
 * @version 1.0.0
 */
class BucketNameValidatorTest {

    @Test
    void testValidBucketNames() {
        // 测试有效的bucket名称
        String[] validNames = {
            "my-bucket",
            "test123",
            "bucket-name-123",
            "abc",
            "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6a7b8c9d0e1f"  // 63个字符
        };
        
        for (String name : validNames) {
            BucketNameValidator.ValidationResult result = BucketNameValidator.validate(name);
            assertTrue(result.isValid(), "应该是有效的bucket名称: " + name);
            assertEquals(name.toLowerCase(), result.getNormalizedName());
        }
    }

    @Test
    void testInvalidBucketNames() {
        // 测试无效的bucket名称
        String[] invalidNames = {
            "TestBucket",           // 包含大写字母
            "test_bucket",          // 包含下划线
            "test..bucket",         // 包含连续点号
            "test--bucket",         // 包含连续连字符
            "-testbucket",          // 以连字符开头
            "testbucket-",          // 以连字符结尾
            "ab",                   // 长度太短
            "192.168.1.1",         // IP地址格式
            "xn--test",             // 以xn--开头
            "test-s3alias",         // 以-s3alias结尾
            "test--ol-s3",          // 以--ol-s3结尾
            "",                     // 空字符串
            "a".repeat(64)          // 长度超过63个字符
        };
        
        for (String name : invalidNames) {
            BucketNameValidator.ValidationResult result = BucketNameValidator.validate(name);
            assertFalse(result.isValid(), "应该是无效的bucket名称: " + name + ", 错误信息: " + result.getMessage());
            assertNull(result.getNormalizedName());
        }
    }

    @Test
    void testNullAndEmptyBucketNames() {
        // 测试null和空字符串
        BucketNameValidator.ValidationResult nullResult = BucketNameValidator.validate(null);
        assertFalse(nullResult.isValid());
        assertTrue(nullResult.getMessage().contains("不能为空"));

        BucketNameValidator.ValidationResult emptyResult = BucketNameValidator.validate("");
        assertFalse(emptyResult.isValid());
        assertTrue(emptyResult.getMessage().contains("不能为空"));

        BucketNameValidator.ValidationResult spaceResult = BucketNameValidator.validate("   ");
        assertFalse(spaceResult.isValid());
        assertTrue(spaceResult.getMessage().contains("不能为空"));
    }

    @Test
    void testBucketNameNormalization() {
        // 测试名称标准化（转小写）
        String mixedCaseName = "MyBucket123";
        BucketNameValidator.ValidationResult result = BucketNameValidator.validate(mixedCaseName);
        
        // 这个应该失败，因为包含大写字母
        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("小写字母"));
    }

    @Test
    void testSpecificS3Rules() {
        // 测试特定的S3规则
        
        // IP地址格式
        assertFalse(BucketNameValidator.validate("192.168.1.1").isValid());
        assertFalse(BucketNameValidator.validate("10.0.0.1").isValid());
        
        // xn--前缀
        assertFalse(BucketNameValidator.validate("xn--example").isValid());
        
        // 特殊后缀
        assertFalse(BucketNameValidator.validate("my-bucket-s3alias").isValid());
        assertFalse(BucketNameValidator.validate("my-bucket--ol-s3").isValid());
        
        // 连续连字符
        assertFalse(BucketNameValidator.validate("my--bucket").isValid());
        assertFalse(BucketNameValidator.validate("bucket--name--test").isValid());
    }

    @Test
    void testBoundaryConditions() {
        // 测试边界条件
        
        // 最短有效长度（3个字符）
        assertTrue(BucketNameValidator.validate("abc").isValid());
        assertFalse(BucketNameValidator.validate("ab").isValid());
        
        // 最长有效长度（63个字符）
        String maxLengthName = "a" + "b".repeat(61) + "c"; // 63个字符
        assertTrue(BucketNameValidator.validate(maxLengthName).isValid());
        
        String tooLongName = "a" + "b".repeat(62) + "c"; // 64个字符
        assertFalse(BucketNameValidator.validate(tooLongName).isValid());
    }
}