package com.netease.pangu.game.util;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.MessageBuffer;
import org.msgpack.value.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangc on 2017/2/24.
 */
public class MessagePackUtil {
    public static byte[] pack(byte[] bytes) {
        MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
        try {
            packer.packRawStringHeader(bytes.length);
            packer.writePayload(bytes);
            packer.close();
            MessageBuffer buffer = packer.toMessageBuffer();
            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String upack(byte[] bytes) {
        MessageUnpacker unpacker = null;
        try {
            unpacker = MessagePack.newDefaultUnpacker(bytes);
            Value value = unpacker.unpackValue();
            unpacker.close();
            return value.asStringValue().asString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("1", 1);
        map.put("2", "123");
        map.put("3", "12");
        String jsonStr = JsonUtil.toJson(map);
        byte[] bytes = MessagePackUtil.pack(jsonStr.getBytes());
        System.out.println(MessagePackUtil.upack(bytes));
    }
}
