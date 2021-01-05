package net.ketone.accrptgen.config.mongo;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class StackTraceElementArrayCodec implements Codec<StackTraceElement[]> {

    @Override
    public StackTraceElement[] decode(BsonReader bsonReader, DecoderContext decoderContext) {
        bsonReader.readString();
        return new StackTraceElement[0];
    }

    @Override
    public void encode(BsonWriter bsonWriter, StackTraceElement[] aClass, EncoderContext encoderContext) {
        bsonWriter.writeString(aClass.toString());
    }

    @Override
    public Class<StackTraceElement[]> getEncoderClass() {
        return StackTraceElement[].class;
    }

}
