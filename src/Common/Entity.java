package Common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.nio.serialization.DataSerializable;

public abstract class Entity implements DataSerializable {


    public Entity() {
    }

    public static <T extends Entity> T from(String json, Class<T> ofClass)  {
        try {
            return new ObjectMapper().readValue(json, ofClass);
        } catch (JsonProcessingException e) {
            Util.Logger.Log(Util.Logger.WARN, "Could not convert json to object", e);
            e.printStackTrace();

            return null;
        }
    }
}
