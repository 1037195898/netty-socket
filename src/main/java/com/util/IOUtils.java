package com.util;

import com.decoder.AES;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class IOUtils {


    /**
     * 获取对象保存的 aes
     *
     * @param channel 渠道
     * @return AES
     */
    public static AES getAes(Channel channel) {
        AttributeKey<AES> attributeKey = AttributeKey.valueOf("key_" + channel.id());
        AES aes;
        if (!channel.hasAttr(attributeKey)) {
            aes = new AES();
            channel.attr(attributeKey).set(aes);
        } else {
            aes = channel.attr(attributeKey).get();
        }
        return aes;
    }

}
