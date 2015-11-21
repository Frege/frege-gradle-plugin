package frege.gradle.plugins;

import org.gradle.util.VersionNumber;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FregeJarFile {
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("(frege(?:-all)?)-(\\d.*?)(-indy)?.jar");
    private final File file;
    private final Matcher matcher;
    private String version;

    private FregeJarFile(File file, Matcher matcher) {
        this.file = file;
        this.matcher = matcher;
    }


    public static FregeJarFile parse(File file) {
        Matcher matcher = FILE_NAME_PATTERN.matcher(file.getName());
        return matcher.matches() ? new FregeJarFile(file, matcher) : null;
    }

    public String getDependencyNotation() {
        return "org.frege-lang:frege:" + getVersion();

    }

    public VersionNumber getVersion() {
        return VersionNumber.parse(matcher.group(2));
    }
}
