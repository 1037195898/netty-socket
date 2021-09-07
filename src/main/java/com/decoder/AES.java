package com.decoder;

import com.decoder.Rijndael.AESMode;

public class AES {
	
//	private final String encoding = "UTF-8";
	public static byte[] KEY = "0123456789ABCDEF".getBytes();
	public static byte[] IV = "FEDCBA9876543210".getBytes();
	private Rijndael cbc_aes_pkcs5;
	
	public AES() {
		cbc_aes_pkcs5 = new Rijndael(KEY, IV, AESMode.CBC_PKCS5);
	}
	
	/**
	 * 解密
	 * @param by
	 * @return 
	 */
	public byte[] decrypt(byte[] by) {
		return cbc_aes_pkcs5.decryptHex(new String(by));
	}

	/**
	 * 加密
	 * @param bytes
	 * @return 
	 */
	public String encrypt(byte[] bytes) {
		return cbc_aes_pkcs5.encryptToHex(bytes);
	}
	
}
