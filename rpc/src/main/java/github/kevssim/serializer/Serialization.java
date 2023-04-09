package github.kevssim.serializer;

import java.io.IOException;

public interface Serialization {
    public byte[] serialize(Object obj) throws IOException;

    public <T> T deserialize(byte[] bytes, Class<T> clz) throws IOException, ClassNotFoundException;
}
