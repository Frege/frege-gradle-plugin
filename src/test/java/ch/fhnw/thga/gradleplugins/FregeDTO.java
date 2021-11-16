package ch.fhnw.thga.gradleplugins;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FregeDTO {
    public final String version;
    public final String release;
    public final String compilerDownloadDir;
    public final String mainSourceDir;
    public final String outputDir;
    public final String mainModule;
    public final String compilerFlags;

    public FregeDTO(String version, String release, String compilerDownloadDir, String mainSourceDir,
            String outputDir, String mainModule, String compilerFlags) {
        this.version = version;
        this.release = release;
        this.compilerDownloadDir = compilerDownloadDir;
        this.mainSourceDir = mainSourceDir;
        this.outputDir = outputDir;
        this.mainModule = mainModule;
        this.compilerFlags = compilerFlags;
    }

    public String getVersion() {
        return version;
    }

    public String getRelease() {
        return release;
    }

    public String getCompilerDownloadDir() {
        return compilerDownloadDir;
    }

    public String getMainSourceDir() {
        return mainSourceDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public String getMainModule() {
        return mainModule;
    }

    public String getCompilerFlags() {
        return compilerFlags;
    }

    private String getFieldValue(Field field) {
        try {
            return field.get(this).toString();
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }
    }

    private Field getField(String fieldName) {
        try {
            return FregeDTO.class.getField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(
                    String.format("Field %s not found in class %s", e.getMessage(), FregeDTO.class.getName()),
                    e.getCause());
        }
    }

    private boolean isEmpty(Field field) {
        return getFieldValue(field).isEmpty();
    }

    private String toKeyValuePairs(Field field) {
        return String.format("%s = %s", field.getName(), getFieldValue(field));
    }

    private boolean isGetterProperty(Method method) {
        return method.getName().startsWith("get") && method.getReturnType().getName().contains("Property");
    }

    private String stripGetPrefixAndDecapitalize(String s) {
        return Character.toLowerCase(s.charAt(3)) + s.substring(4);
    }

    public String toBuildFile() {
        Stream<Method> methods = Arrays.stream(FregeExtension.class.getMethods());
        Stream<Field> fields = methods.filter(m -> isGetterProperty(m))
                .map(m -> stripGetPrefixAndDecapitalize(m.getName())).map(name -> getField(name));
        return fields.filter(f -> !isEmpty(f)).map(f -> toKeyValuePairs(f)).collect(Collectors.joining("\n  "));
    }
}
