package com.decoder

import com.entity.Input
import com.entity.Output
import com.util.PoolUtils.getObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.min

/**
 * 提供AES的所有核心加密解密。
 *
 * 使用 `AES/CBC/NoPadding` 模式时，解密后的原文后会有多余的空格字符需要去除。
 *
 * **该类是线程安全的。**
 *
 * @since 2.0
 * 根据给定的私密密钥、IV向量和默认的 `AES/CBC/PKCS5Padding` 填充方式，构造一个新的 `Rijndael`。
 * @param key  私密密钥。
 * @param iv   IV向量。
 * @param mode 模式与填充方式。 默认 AESMode.CBC_PKCS5 AES/CBC/PKCS5Padding
 */
@OptIn(ExperimentalStdlibApi::class, ExperimentalEncodingApi::class)
class Rijndael(key: ByteArray, iv: ByteArray? = null, private val mode: AESMode = AESMode.CBC_PKCS5) {

    private val keySpec: SecretKeySpec
    private val ivSpec: IvParameterSpec?

    private var enc: Cipher? = null
    private var dec: Cipher? = null

    init {
        validateKey(key)
        val aesKey = key.copyOfRange(0, min(key.size, KEY_LENGTH))
        this.keySpec = SecretKeySpec(aesKey, ALGORITHM)
        this.ivSpec = iv?.let {
            validateIV(iv)
            val aesIV = it.copyOfRange(0, min(iv.size, IV_LENGTH))
            IvParameterSpec(aesIV)
        }
        initEncryptionCipher()
        initDecryptionCipher()
    }

    /**
     * 校验密钥格式。
     */
    private fun validateKey(key: ByteArray) {
        require(key.size >= KEY_LENGTH) {
            // AES 密钥长度无效
            "Invalid AES key length: ${key.size} bytes"
        }
    }

    /**
     * 校验向量格式。
     */
    private fun validateIV(iv: ByteArray) {
        require(iv.size >= IV_LENGTH) {
            "Wrong IV length: must be $IV_LENGTH bytes long"
        }
    }

    /**
     * 返回明文加密后的最大长度。
     *
     * @param sourceLen 明文长度。
     * @return 明文加密后的最大长度。
     */
    fun getCihperLength(sourceLen: Int): Int {
        var base = 0
        if (!mode.value.endsWith("NoPadding")) {
            base = 16
        }
        val pad = sourceLen % 16
        if (pad == 0) {
            return sourceLen + base
        }
        return sourceLen - pad + base
    }

    /**
     * 对指定的数据进行 `AES` 算法加密。如果加密发生错误，则返回长度为 0 的 `byte` 数组。
     *
     *
     * 处理 `data` 缓冲区中的字节以及可能在上一次 `update`
     * 操作中已缓存的任何输入字节，其中应用了填充（如果请求）。结果存储在新缓冲区中。
     *
     *
     * 结束时，此方法将把此类中的 `cipher` 加密对象重置为上一次调用 `init`
     * 初始化得到的状态。即重置该对象，可供加密或解密（取决于调用 init 时指定的操作模式）更多的数据。
     *
     * @param data 要加密的数据。
     * @return 加密后的数据。
     */
    fun encrypt(data: ByteArray): ByteArray {
        if (data.isEmpty()) {
            return byteArrayOf()
        }
        val input = if (mode.value.endsWith("NoPadding")) {
            padding(data)
        } else {
            data
        }
        try {
            return enc!!.doFinal(input)
        } catch (ex: Exception) {
            writeEncryptLogger(ex)
        }
        return byteArrayOf()
    }

    /**
     * 对指定的数据进行 `AES` 算法解密。如果解密发生错误，则返回长度为 0 的 `byte` 数组。
     *
     *
     * 处理 `data` 缓冲区中的字节以及可能在上一次 `update`
     * 操作中已缓存的任何输入字节，其中应用了填充（如果请求）。结果存储在新缓冲区中。
     *
     *
     * 结束时，此方法将把此类中的 `cipher` 加密对象重置为上一次调用 `init`
     * 初始化得到的状态。即重置该对象，可供加密或解密（取决于调用 init 时指定的操作模式）更多的数据。
     *
     * @param data 要解密的数据。
     * @return 解密后的数据。
     */
    fun decrypt(data: ByteArray?): ByteArray {
        try {
            val result = dec!!.doFinal(data)
            if (mode.value.endsWith("NoPadding")) {
                var idx = result.size
                var i = result.size
                while (--i >= 0) {
                    if (result[i] == 0.toByte()) {
                        idx = i
                    } else {
                        break
                    }
                }
                result.copyOfRange(0, min(idx, result.size))
            }
            return result
        } catch (ex: Exception) {
            writeDecryptLogger(ex)
        }
        return byteArrayOf()
    }

    /**
     * 使用 `AES` 算法对指定的数据进行加密操作。如果加密发生错误，则返回长度为 0 的 `byte` 数组。
     *
     *
     * 处理 data 缓冲区中从 `offset` 开始（包含）的前 `length` 个字节以及可能在上一次
     * `update` 操作过程中已缓存的任何输入字节，其中应用了填充（如果需要）。结果存储在新缓冲区中。
     *
     *
     * 结束时，此方法将把此类中的 `cipher` 加密对象重置为上一次调用 `init`
     * 初始化得到的状态。即重置该对象，可供加密或解密（取决于调用 init 时指定的操作模式）更多的数据。
     *
     * @param data   输入缓冲区。
     * @param offset `data` 中输入开始位置的偏移量。
     * @param length 输入长度。
     * @return 存储结果的新缓冲区，即加密后的数据。
     */
    fun encrypt(data: ByteArray?, offset: Int, length: Int): ByteArray {
        try {
            return enc!!.doFinal(data, offset, length)
        } catch (ex: Exception) {
            writeEncryptLogger(ex)
        }
        return byteArrayOf()
    }

    /**
     * 使用 `AES` 算法对指定的数据进行解密操作。如果解密发生错误，则返回长度为 0 的 `byte` 数组。
     *
     *
     * 处理 data 缓冲区中从 `offset` 开始（包含）的前 `length` 个字节以及可能在上一次
     * `update` 操作过程中已缓存的任何输入字节，其中应用了填充（如果需要）。结果存储在新缓冲区中。
     *
     *
     * 结束时，此方法将把此类中的 `cipher` 加密对象重置为上一次调用 `init`
     * 初始化得到的状态。即重置该对象，可供加密或解密（取决于调用 init 时指定的操作模式）更多的数据。
     *
     * @param data   输入缓冲区。
     * @param offset `data` 中输入开始位置的偏移量。
     * @param length 输入长度。
     * @return 存储结果的新缓冲区，即解密后的数据。
     */
    fun decrypt(data: ByteArray?, offset: Int, length: Int): ByteArray {
        try {
            return dec!!.doFinal(data, offset, length)
        } catch (ex: Exception) {
            writeDecryptLogger(ex)
        }
        return byteArrayOf()
    }

    /**
     * 使用 `AES` 算法对指定的数据进行加密操作。如果加密发生错误，则返回 0。
     *
     *
     * 处理 `inputData` 缓冲区中从 `inputOffset` 开始（包含）的前 `inputLen`
     * 个字节以及可能在上一次 `update` 操作过程中已缓存的任何输入字节，其中应用了填充（如果需要）。结果存储在
     * `outputData` 缓冲区中从 `outputOffset`（包含）开始的位置。
     *
     *
     * 如果 `outputData` 缓冲区太小无法保存该结果，则返回 0。
     *
     * @param inputData    要加密的数据。
     * @param inputOffset  `inputData` 中输入开始位置的偏移量。
     * @param inputLen     输入长度。
     * @param outputData   保存结果的缓冲区。
     * @param outputOffset `outputData` 中存储结果的位置的偏移量。
     * @return `outputData` 中存储的字节数。
     */
    fun encrypt(
        inputData: ByteArray?, inputOffset: Int, inputLen: Int,
        outputData: ByteArray?, outputOffset: Int
    ): Int {
        try {
            return enc!!.doFinal(
                inputData, inputOffset, inputLen, outputData,
                outputOffset
            )
        } catch (ex: Exception) {
            writeEncryptLogger(ex)
        }
        return 0
    }

    /**
     * 使用 `AES` 算法对指定的数据进行解密操作。如果解密发生错误，则返回 0。
     *
     *
     * 处理 `inputData` 缓冲区中从 `inputOffset` 开始（包含）的前 `inputLen`
     * 个字节以及可能在上一次 `update` 操作过程中已缓存的任何输入字节，其中应用了填充（如果需要）。结果存储在
     * `outputData` 缓冲区中从 `outputOffset`（包含）开始的位置。
     *
     *
     * 如果 `outputData` 缓冲区太小无法保存该结果，则返回 0。
     *
     * @param inputData    要解密的数据。
     * @param inputOffset  `inputData` 中输入开始位置的偏移量。
     * @param inputLen     输入长度。
     * @param outputData   保存结果的缓冲区。
     * @param outputOffset `outputData` 中存储结果的位置的偏移量。
     * @return `outputData` 中存储的字节数。
     */
    fun decrypt(
        inputData: ByteArray?, inputOffset: Int, inputLen: Int,
        outputData: ByteArray?, outputOffset: Int
    ): Int {
        try {
            return dec!!.doFinal(
                inputData, inputOffset, inputLen, outputData,
                outputOffset
            )
        } catch (ex: Exception) {
            writeDecryptLogger(ex)
        }
        return 0
    }

    /**
     * 加密给定的数据，并返回密文转换成16进制的字符串。如果加密失败，则返回 `null`。
     *
     * @param data 要加密的数据。
     * @return 返回加密密文转换成16进制的字符串。
     */
    fun encryptToHex(data: ByteArray): String {
        val bytes = encrypt(data)
        if (bytes.isEmpty()) {
            return ""
        }
        return bytes.toHexString()
    }

    /**
     * 解密给定的16进制字符串，返回明文数据。如果解密失败则返回空的`byte`数组。
     *
     *
     * 该方法用于解密 [.encryptToHex] 方法加密后的16进制字符串。
     *
     * @param hexStr 16进制字符串表示的密文。
     * @return 返回明文数据。
     */
    fun decryptHex(hexStr: String?): ByteArray {
        if (hexStr.isNullOrBlank()) {
            return byteArrayOf()
        }
        val bytes = runCatching {
            hexStr.hexToByteArray()
        }.onFailure {
            writeDecryptLogger(it)
            byteArrayOf()
        }.getOrThrow()
        return decrypt(bytes)
    }

    /**
     * 使用指定的字符编码，加密给定的字符串，返回密文数据。如果加密失败则返回空的 `byte` 数组。
     *
     * @param input    要加密的字符串。
     * @param encoding 字符编码。
     * @return 返回密文数据。
     */
    fun encryptString(input: String, encoding: Charset = Charsets.ISO_8859_1): ByteArray {
        if (input.isBlank()) {
            return byteArrayOf()
        }
        val data = runCatching {
            input.toByteArray(encoding)
        }.onFailure {
            writeEncryptLogger(it)
            byteArrayOf()
        }
        return encrypt(data.getOrThrow())
    }

    /**
     * 使用指定的字符编码对给定的数据进行解密，还原成原字符串。 如果
     * `encoding == null || "".equals(encoding)`，则字符编码默认为
     * `ISO-889-1`
     *
     *
     * 如果 `data` 为`null` 或空数组，或者解密失败，则返回 `null`。 该方法主要用于解密由
     * [.encryptString] 加密后的数据。
     *
     * @param data     要解密的数据。
     * @param encoding 字符编码。
     * @return 解密后的原文字符串。
     */
    fun decryptToString(data: ByteArray, encoding: Charset = Charsets.ISO_8859_1): String {
        if (data.isEmpty()) {
            return ""
        }
        val result = decrypt(data)
        if (result.isEmpty()) {
            return ""
        }
        return try {
            String(result, encoding)
        } catch (ex: UnsupportedEncodingException) {
            ""
        }
    }

    /**
     * 使用指定的字符编码，加密给定的字符串，并返回密文转换成16进制的字符串。如果加密失败，则返回 `null`。
     *
     *
     * 如果 `encoding == null || "".equals(encoding)`，则字符编码为
     * `ISO-8859-1`。
     *
     * @param input    要加密的字符串。
     * @param encoding 字符编码。
     * @return 返回加密后的密文16进制字符串。
     */
    fun encryptStringToHex(input: String, encoding: Charset = Charsets.ISO_8859_1): String {
        val bytes = encryptString(input, encoding)
        if (bytes.isEmpty()) {
            return ""
        }
        return bytes.toHexString()
    }

    /**
     * 解密给定的16进制字符串，返回明文字符串。如果解密失败则返回 `null`。
     *
     *
     * 该方法用于解密 [.encryptStringToHex] 方法加密后的16进制字符串密文。
     *
     * @param hexStr   16进制字符串表示的密文。
     * @param encoding 字符编码。
     * @return 返回明文字符串。
     */
    fun decryptHexToString(hexStr: String, encoding: Charset = Charsets.ISO_8859_1): String {
        val bytes = decryptHex(hexStr)
        if (bytes.isEmpty()) {
            return ""
        }
        return try {
            String(bytes, encoding)
        } catch (ex: UnsupportedEncodingException) {
            ""
        }
    }

    /**
     * 加密给定的数据，将密文数据进行 `base64` 编码。如果给定的数据为空或加密失败，则返回 `null`。
     *
     * @param data 要加密的数据。
     * @return 返回AES加密后的密文的 `base64` 编码。
     */
    fun encryptToBase64(data: ByteArray): String {
        val bytes = encrypt(data)
        if (bytes.isEmpty()) {
            return ""
        }
        return Base64.encode(bytes)
    }

    /**
     * 将给定的 `base64` 编码格式字符串还原成 `byte` 数组， 然后对该数据进行 `AES`
     * 解密。如果解密失败，则返回空的`byte`数组。
     *
     *
     * 该方法用于解密 [.encryptToBase64] 方法加密后的字符串。
     *
     * @param base64Str base64格式字符串。
     * @return 返回解密后的数据。
     */
    fun decryptBase64(base64Str: String): ByteArray {
        val base64Bytes = Base64.decode(base64Str)
        if (base64Bytes.isEmpty()) {
            return byteArrayOf()
        }
        return decrypt(base64Bytes)
    }

    /**
     * 使用指定的字符编码加密给定的字符串。
     *
     *
     * 该方法先将明文字符串根据指定字符编码转换成 `byte[]`，然后调用
     * [.encryptToBase64] 进行数据加密。 如果
     * `encoding == null || "".equals(encoding)`，则字符编码默认为
     * `ISO-8859-1`。
     *
     * @param input    要加密的字符串。
     * @param encoding 字符编码。
     * @return 返回加密后的密文字符串。
     */
    fun encryptStringToBase64(input: String, encoding: Charset = Charsets.ISO_8859_1): String {
        val bytes = encryptString(input, encoding)
        if (bytes.isEmpty()) {
            return ""
        }
        return Base64.encode(bytes)
    }

    /**
     * 将给定的 `base64` 编码格式字符串还原成 `byte` 数组， 然后对该数据进行 `AES`
     * 解密，返回明文字符串。如果解密失败，则返回 `null`。
     *
     *
     * 该方法是对 [.decryptBase64] 的補充。用于对
     * [.encryptStringToBase64] 方法加密后的 字符串解密。
     *
     * <pre>
     * String plain = "I am a good boy.";
     * String encoding = CharEncoding.UTF_8;
     * String enc_plain = encryptToBase64(plain, encoding);
     * String dec_plain = decryptToBase64(enc_plain, encoding);
     * dec_plain = "I am a good boy."
    </pre> *
     *
     * @param base64Str 要解密的经过 `base64` 编码的密文。
     * @param encoding  字符编码。
     * @return 返回解密后的字符串。
     */
    fun decryptBase64ToString(base64Str: String, encoding: Charset = Charsets.ISO_8859_1): String {
        val bytes = decryptBase64(base64Str)
        if (bytes.isEmpty()) {
            return ""
        }
        return try {
            String(bytes, encoding)
        } catch (ex: UnsupportedEncodingException) {
            ""
        }
    }

    private fun writeEncryptLogger(cause: Throwable) {
        LOGGER.error("AES decrypt failed. Cause: ${cause.message}")
    }

    private fun writeDecryptLogger(cause: Throwable) {
        LOGGER.error("AES decrypt failed. Cause: ${cause.message}")
    }

    private fun padding(bytes: ByteArray): ByteArray {
        val len = bytes.size
        if (len == 0) return bytes
        val pad: Int
        if (len < 16) {
            pad = 16 - len
            val pads = createSequence(0.toByte(), pad, 0.toByte())

            return bytes + pads
        } else {
            pad = len % 16
            if (pad != 0) {
                val pads = createSequence(0.toByte(), 16 - pad, 0.toByte())
                return bytes + pads
            }
            return bytes
        }
    }

    private fun initEncryptionCipher() {
        if (enc == null) {
            try {
                enc = Cipher.getInstance(mode.value)
                if (ivSpec == null) {
                    enc?.init(Cipher.ENCRYPT_MODE, keySpec)
                } else {
                    enc?.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
                }
            } catch (ex: NoSuchAlgorithmException) {
                LOGGER.error("initEncrypt: The current platform no such algorithm - ${ALGORITHM}, Cause: ${ex.message}")
            } catch (ex: NoSuchPaddingException) {
                LOGGER.error("initEncrypt: The current platform no such padding - ${mode.value}, Cause: ${ex.message}")
            } catch (ex: InvalidKeyException) {
                LOGGER.error("initEncrypt: Invalid key - ${keySpec.format} Cause: ${ex.message}")
            } catch (ex: InvalidAlgorithmParameterException) {
                LOGGER.error("initEncrypt: Invalid algorithm parameter - ${(ivSpec?.toString() ?: "encrypt iv parameter.")} Cause: ${ex.message}")
            }
        }
    }

    private fun initDecryptionCipher() {
        if (dec == null) {
            try {
                dec = Cipher.getInstance(mode.value)
                if (ivSpec == null) {
                    dec?.init(Cipher.DECRYPT_MODE, keySpec)
                } else {
                    dec?.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
                }
            } catch (ex: NoSuchAlgorithmException) {
                LOGGER.error("initDecrypt: The current platform no such algorithm - $ALGORITHM, Cause: ${ex.message}")
            } catch (ex: NoSuchPaddingException) {
                LOGGER.error("initDecrypt: The current platform no such padding - ${mode.value}, Cause: ${ex.message}")
            } catch (ex: InvalidKeyException) {
                LOGGER.error("initDecrypt: Invalid key - ${keySpec.format} Cause: ${ex.message}")
            } catch (ex: InvalidAlgorithmParameterException) {
                LOGGER.error("initDecrypt: Invalid algorithm parameter - ${ivSpec?.toString() ?: "decrypt iv parameter."} Cause: ${ex.message}")
            }
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(Rijndael::class.java)

        /** aes 缓存模式  */
        private val AES_CACHES = mutableMapOf<String, Rijndael>()

        /**
         * 算法名称 － `AES`。
         */
        const val ALGORITHM: String = "AES"

        /**
         * 默认的128位时的密钥长度。
         */
        const val KEY_LENGTH: Int = 16

        /**
         * 默认的128位时的向量长度。
         */
        const val IV_LENGTH: Int = KEY_LENGTH

        /**
         * 根据给定的私密密钥、IV向量和指定的填充方式，获得一个 `Rijndael`。
         *
         * @param key  私密密钥。
         * @param iv   IV向量。
         * @param mode 模式与填充方式。
         * @return 返回给定的私密密钥、IV向量和指定的填充方式的 `Rijndael`。
         */
        fun getInstance(key: ByteArray, iv: ByteArray?, mode: AESMode = AESMode.CBC_PKCS5): Rijndael {
            val cacheKey =
                arrayOf(key.toHexString(), iv?.toHexString() ?: "iv", mode.value.replace("/", "-")).joinToString("_")
            return AES_CACHES.getOrPut(cacheKey) { Rijndael(key, iv, mode) }
        }

        /**
         *
         * 根据给定的私密密钥和默认的 {@code AES/ECB/PKCS5Padding}填充方式，构造一个新的 `Rijndael`。
         * @param key  私密密钥。
         * @param mode 模式与填充方式。
         */
        fun getInstance(key: ByteArray, mode: AESMode = AESMode.ECB_PKCS5): Rijndael {
            val cacheKey = arrayOf(key.toHexString(), mode.value.replace("/", "-")).joinToString("_")
            return AES_CACHES.getOrPut(cacheKey) { Rijndael(key, mode = mode) }
        }

        private fun createSequence(start: Byte, length: Int, step: Byte): ByteArray {
            if (step.toInt() == 0) {
                return ByteArray(length) { start }
            }
            var start1 = start
            var step1 = step
            val result = ByteArray(length)
            for (i in 0 until length) {
                result[i] = start1
                val next = start1.toInt() + step1.toInt()
                if (next < Byte.MIN_VALUE || next > Byte.MAX_VALUE) {
                    // 正序
                    if (step1 > 0) {
                        start1 = Byte.MAX_VALUE
                        step1 = 0
                    } else {
                        start1 = Byte.MIN_VALUE
                        step1 = 0
                    }
                } else {
                    start1 = next.toByte()
                }
            }
            return result
        }

    }
}

/**
 * JCE支持的AES模式和填充方式枚举。
 */
enum class AESMode(
    /**
     * 返回AES算法模式和填充方式。
     */
    val value: String
) {
    /**
     * AES/CFB/NoPadding。
     */
    CFB_NO("AES/CFB/NoPadding"),

    /**
     * AES/CFB/PKCS5Padding。
     */
    CFB_PKCS5("AES/CFB/PKCS5Padding"),

    /**
     * AES/CFB/ISO10126Padding。
     */
    CFB_ISO10126("AES/CFB/ISO10126Padding"),

    /**
     * AES/CBC/NoPadding。
     */
    CBC_NO("AES/CBC/NoPadding"),

    /**
     * AES/CBC/PKCS5Padding。
     */
    CBC_PKCS5("AES/CBC/PKCS5Padding"),

    /**
     * AES/CBC/ISO10126Padding。
     */
    CBC_ISO10126("AES/CBC/ISO10126Padding"),

    /**
     * AES/ECB/NoPadding。
     */
    ECB_NO("AES/ECB/NoPadding"),

    /**
     * AES/ECB/PKCS5Padding。
     */
    ECB_PKCS5("AES/ECB/PKCS5Padding"),

    /**
     * AES/ECB/ISO10126Padding。
     */
    ECB_ISO10126("AES/ECB/ISO10126Padding")


}

fun main(args: Array<String>) {
    //		System.setProperty("rootDir", "E:\\WorkSpace\\Idea\\Java\\GameSocketServer");

    val encoding = Charsets.UTF_8
    val plain = "`1234567890~!@#$%^&*()_+|{}:\"<>?[];',./'\\\n\r\t中国人"
    val plainBytes = plain.toByteArray(encoding)
    println(String.format("原文字节数组长度：%s", plainBytes.size))
    val key = "0123456789ABCDEF".toByteArray()
    val iv = "FEDCBA9876543210".toByteArray()
    val ecb_aes = Rijndael(key, mode = AESMode.ECB_PKCS5)

    println("ECB+++++++++++++++++++++++++++++++++ Base64 ++++++++++++++++++++++++++++++++++++")
    val b64Str = ecb_aes.encryptStringToBase64(plain, encoding)
    val b64Plain = ecb_aes.decryptBase64ToString(b64Str, encoding)
    println(String.format("原文：%s", plain))
    println(String.format("密文：%s", b64Str))
    println(String.format("解密：%s", b64Plain))
    println("")

    println("ECB+++++++++++++++++++++++++++++++++ Hex(16) +++++++++++++++++++++++++++++++++++")
    val hexStr = ecb_aes.encryptStringToHex(plain, encoding)
    val hexPlain = ecb_aes.decryptHexToString(hexStr, encoding)
    println(String.format("原文：%s", plain))
    println(String.format("密文：%s", hexStr))
    println(String.format("解密：%s", hexPlain))
    println("")
    val cbc_aes = Rijndael(key, iv, AESMode.CBC_NO)

    println("CBC+++++++++++++++++++++++++++++++++ Base64 ++++++++++++++++++++++++++++++++++++")
    val b64Str2 = cbc_aes.encryptStringToBase64(plain, encoding)
    val b64Plain2 = cbc_aes.decryptBase64ToString(b64Str2, encoding)
    println(String.format("原文：%s", plain))
    println(String.format("密文：%s", b64Str2))
    println(String.format("解密：%s", b64Plain2))
    println("")

    println("CBC+++++++++++++++++++++++++++++++++ Hex(16) +++++++++++++++++++++++++++++++++++")
    val hexStr2 = cbc_aes.encryptStringToHex(plain, encoding)
    val hexPlain2 = cbc_aes.decryptHexToString(hexStr2, encoding)
    println(String.format("原文：%s", plain))
    println(String.format("密文：%s", hexStr2))
    println(String.format("解密：%s", hexPlain2))
    println("")

    val cbc_aes_pkcs5 = Rijndael(key, iv, AESMode.CBC_PKCS5)
    println("CBC+CBC_PKCS5+++++++++++++++++++++++ Base64 ++++++++++++++++++++++++++++++++++++")
    val b64Str3 = cbc_aes_pkcs5.encryptStringToBase64(plain, encoding)
    val b64Plain3 = cbc_aes_pkcs5.decryptBase64ToString(b64Str3, encoding)
    println(String.format("原文：%s", plain))
    println(String.format("密文：%s", b64Str3))
    println(String.format("解密：%s", b64Plain3))
    println("")

    println("CBC+CBC_PKCS5+++++++++++++++++++++++ Hex(16) +++++++++++++++++++++++++++++++++++")
    val hexStr3 = cbc_aes_pkcs5.encryptStringToHex(plain, encoding)
    val hexPlain3 = cbc_aes_pkcs5.decryptHexToString(hexStr3, encoding)
    println(String.format("原文：%s", plain))
    println(String.format("密文：%s", hexStr3))
    println(String.format("解密：%s", hexPlain3))
    println("")

    println("CBC+CBC_PKCS5+++++++++++++++++++++++ byte Hex(16) +++++++++++++++++++++++++++++++++++")
    val output = getObject(Output::class.java)
    output.writeLong(1515)
    output.writeUTF("我来了")
    var bytes = output.toByteArray()
    println(bytes.size)
    for (i in bytes.indices) {
        println(bytes[i])
    }

    //        System.out.println(cbc_aes_pkcs5.encrypt(bytes).length);
    val str = cbc_aes_pkcs5.encryptToHex(bytes)
    println(String.format("密文：%s", str))

    bytes = cbc_aes_pkcs5.decryptHex(str)
    val input = Input(bytes)
    val id = input.readLong()
    val name = input.readUTF()
    println(String.format("解密：%s%s", id, name))
}