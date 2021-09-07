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
		byte[] output = new byte[0];
		Deflater compresser = new Deflater();
		compresser.reset();
		compresser.setInput(data);
		compresser.finish();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
		try {
			byte[] buf = new byte[1024];
			while (!compresser.finished()) {
				int i = compresser.deflate(buf);
				bos.write(buf, 0, i);
			}
			output = bos.toByteArray();
		} finally {
			bos.close();
		}
		compresser.end();
		return output;
	}

	/**
	 * 解压缩
	 * @param data 需要解压缩的字节数组
	 * @throws Exception
	 */
	public static byte[] decompress(byte[] data) throws Exception {
		byte[] output = new byte[0];
		Inflater decompresser = new Inflater();
		decompresser.reset();
		decompresser.setInput(data);
		ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
		try {
			byte[] buf = new byte[1024];
			while (!decompresser.finished()) {
				int i = decompresser.inflate(buf);
				o.write(buf, 0, i);
			}
			output = o.toByteArray();
		} finally {
			o.close();
		}
		decompresser.end();
		return output;
	}

}
