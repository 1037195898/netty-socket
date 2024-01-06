package com.decoder

class AES {

    private val cbc_aes_pkcs5 = Rijndael(KEY, IV, AESMode.CBC_PKCS5)

    /**
     * 解密
     * @param by
     * @return
     */
    fun decrypt(by: ByteArray): ByteArray {
        return cbc_aes_pkcs5.decryptHex(by.decodeToString())
    }

    /**
     * 加密
     * @param bytes
     * @return
     */
    fun encrypt(bytes: ByteArray): String {
        return cbc_aes_pkcs5.encryptToHex(bytes)
    }

    companion object {
        //	private final String encoding = "UTF-8"
        var KEY: ByteArray = "0123456789ABCDEF".toByteArray()
        var IV: ByteArray = "FEDCBA9876543210".toByteArray()
    }

}
