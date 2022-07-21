package com.util;

import java.io.ByteArrayOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZlibUtil {

	/**
	 * 用zlib压缩
	 * @param data
	 * @throws Exception
	 */
	public static byte[] compress(byte[] data) throws Exception {
		byte[] output;
		Deflater deflater = new Deflater();
		deflater.reset();
		deflater.setInput(data);
		deflater.finish();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
		try {
			byte[] buf = new byte[1024];
			while (!deflater.finished()) {
				int i = deflater.deflate(buf);
				bos.write(buf, 0, i);
			}
			output = bos.toByteArray();
		} finally {
			bos.close();
		}
		deflater.end();
		return output;
	}

	/**
	 * 解压缩
	 * @param data 需要解压缩的字节数组
	 * @throws Exception
	 */
	public static byte[] decompress(byte[] data) throws Exception {
		byte[] output;
		Inflater inflater = new Inflater();
		inflater.reset();
		inflater.setInput(data);
		ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
		try {
			byte[] buf = new byte[1024];
			while (!inflater.finished()) {
				int i = inflater.inflate(buf);
				o.write(buf, 0, i);
			}
			output = o.toByteArray();
		} finally {
			o.close();
		}
		inflater.end();
		return output;
	}

}
