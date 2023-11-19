package io.wispforest.owo.serialization.impl.edm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

public class EdmIo {

    public static void encode(DataOutput output, EdmElement<?> data) throws IOException {
        output.writeByte(data.type().ordinal());
        encodeElementData(output, data);
    }

    public static EdmElement<?> decode(DataInput input) throws IOException {
        return decodeElementData(input, input.readByte());
    }

    public static void encodeElementData(DataOutput output, EdmElement<?> data) throws IOException {
        switch (data.type()) {
            case BYTE -> output.writeByte(data.<Byte>cast());
            case SHORT -> output.writeShort(data.<Short>cast());
            case INT -> output.writeInt(data.cast());
            case LONG -> output.writeLong(data.cast());
            case FLOAT -> output.writeFloat(data.cast());
            case DOUBLE -> output.writeDouble(data.cast());
            case BOOLEAN -> output.writeBoolean(data.cast());
            case STRING -> output.writeUTF(data.cast());
            case BYTES -> {
                output.writeInt((data.<byte[]>cast()).length);
                output.write(data.cast());
            }
            case OPTIONAL -> {
                var optional = data.<Optional<EdmElement<?>>>cast();

                output.writeBoolean(optional.isPresent());
                if (optional.isPresent()) encodeElementData(output, optional.get());
            }
            case SEQUENCE -> {
                var list = data.<List<EdmElement<?>>>cast();

                output.writeInt(list.size());
                if (!list.isEmpty()) {
                    output.writeByte(list.get(0).type().ordinal());
                    for (var element : list) {
                        encodeElementData(output, element);
                    }
                }
            }
            case MAP -> {
                var map = data.<Map<String, EdmElement<?>>>cast();

                output.writeInt(map.size());
                for (var entry : map.entrySet()) {
                    output.writeUTF(entry.getKey());

                    output.writeByte(entry.getValue().type().ordinal());
                    encodeElementData(output, entry.getValue());
                }
            }
        }
    }

    private static EdmElement<?> decodeElementData(DataInput input, byte type) throws IOException {
        return switch (EdmElement.Type.values()[type]) {
            case BYTE -> EdmElement.wrapByte(input.readByte());
            case SHORT -> EdmElement.wrapShort(input.readShort());
            case INT -> EdmElement.wrapInt(input.readInt());
            case LONG -> EdmElement.wrapLong(input.readLong());
            case FLOAT -> EdmElement.wrapFloat(input.readFloat());
            case DOUBLE -> EdmElement.wrapDouble(input.readDouble());
            case BOOLEAN -> EdmElement.wrapBoolean(input.readBoolean());
            case STRING -> EdmElement.wrapString(input.readUTF());
            case BYTES -> {
                var result = new byte[input.readInt()];
                input.readFully(result);

                yield EdmElement.wrapBytes(result);
            }
            case OPTIONAL -> {
                if (input.readByte() != 0) {
                    yield EdmElement.wrapOptional(Optional.of(decodeElementData(input, input.readByte())));
                } else {
                    yield EdmElement.wrapOptional(Optional.empty());
                }
            }
            case SEQUENCE -> {
                var length = input.readInt();
                if (length != 0) {
                    var result = new ArrayList<EdmElement<?>>(length);
                    var listType = input.readByte();

                    for (int i = 0; i < length; i++) {
                        result.add(decodeElementData(input, listType));
                    }

                    yield EdmElement.wrapSequence(result);
                } else {
                    yield EdmElement.wrapSequence(List.of());
                }
            }
            case MAP -> {
                var length = input.readInt();
                var result = new HashMap<String, EdmElement<?>>(length);

                for (int i = 0; i < length; i++) {
                    result.put(
                            input.readUTF(),
                            decodeElementData(input, input.readByte())
                    );
                }

                yield EdmElement.wrapMap(result);
            }
        };
    }

}
