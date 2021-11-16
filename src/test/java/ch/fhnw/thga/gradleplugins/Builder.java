package ch.fhnw.thga.gradleplugins;

public interface Builder {
    Builder version(String version);

    Builder release(String release);

    Builder compilerDownloadDir(String downloadDir);

    Builder mainSourceDir(String mainSourceDir);

    Builder outputDir(String outputDir);

    Builder mainModule(String mainModule);

    Builder compilerFlags(String compilerFlags);

    FregeDTO build();
}
