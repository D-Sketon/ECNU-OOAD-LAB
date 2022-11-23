package gizmoball.ui.file;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gizmoball.engine.physics.PhysicsBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class PersistentUtil {

    public static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
    }

    /**
     * 包装List&lt;PhysicsBody&rt;，否则直接反序列化List不会包含PhysicsBody的class信息
     */
    private static class Wrapper {
        List<PhysicsBody> bodies;
    }

    private static final Wrapper WRAPPER = new Wrapper();

    public static String toJsonString(List<PhysicsBody> physicsBodies) throws JsonProcessingException {
        WRAPPER.bodies = physicsBodies;
        return mapper.writeValueAsString(WRAPPER);
    }

    public static List<PhysicsBody> fromJsonString(String json) throws IOException {
        Wrapper wrapper = mapper.readValue(json, Wrapper.class);
        return wrapper.bodies;
    }

    public static void write(String json, File file) {
        try {
            Files.write(file.toPath(), json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("write file error", e);
        }
    }

    public static void write(String json, String file) {
        write(json, new File(file));
    }

    public static String readFromFile(String path) throws IOException {
        return IOUtils.toString(Files.newInputStream(Paths.get(path)), "UTF-8");
    }

    public static String readFromFile(File file) throws IOException {
        return IOUtils.toString(Files.newInputStream(file.toPath()), "UTF-8");
    }

}
