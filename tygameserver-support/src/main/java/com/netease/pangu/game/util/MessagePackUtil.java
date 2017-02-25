package com.netease.pangu.game.util;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.MessageBuffer;
import org.msgpack.value.Value;

import java.io.IOException;

/**
 * Created by huangc on 2017/2/24.
 */
public class MessagePackUtil {
    public static String packToHexString(byte[] bytes){
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.writePayload(bytes);
            packer.flush();
            MessageBuffer buffer = packer.toMessageBuffer();
            return buffer.toHexString(0, buffer.size());
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                packer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String upackFromHexString(String hexString){
        MessageUnpacker unpacker = null;
        try {
            byte[] bytes = Hex.decodeHex(hexString.toCharArray());
            unpacker = MessagePack.newDefaultUnpacker(bytes);
            Value value = unpacker.unpackValue();
            return value.asMapValue().toJson();
        } catch (DecoderException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(unpacker != null){
                try {
                    unpacker.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String str = "87A3696E7401A5666C6F6174CB3FE0000000000000A7626F6F6C65616EC3A46E756C6CC0A6737472696E67A7666F6F20626172A5617272617992A3666F6FA3626172A66F626A65637482A3666F6F01A362617ACB3FE0000000000000";
        System.out.println(str);
        String result = MessagePackUtil.upackFromHexString(str);
        System.out.println(result);
    }
}
