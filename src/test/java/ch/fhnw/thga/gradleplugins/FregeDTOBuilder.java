package ch.fhnw.thga.gradleplugins;

public class FregeDTOBuilder implements Builder {
    private String version = "";
    private String release = "";
    private String compilerDownloadDir = "";
    private String mainSourceDir = "";
    private String outputDir = "";
    private String mainModule = "";

    @Override
    public Builder version(String version) {
        this.version = version;
        return this;

    }

    @Override
    public Builder release(String release) {
        this.release = release;
        return this;

    }

    @Override
    public Builder compilerDownloadDir(String downloadDir) {
        this.compilerDownloadDir = downloadDir;
        return this;

    }

    @Override
    public Builder mainSourceDir(String mainSourceDir) {
        this.mainSourceDir = mainSourceDir;
        return this;

    }

    @Override
    public Builder outputDir(String outputDir) {
        this.outputDir = outputDir;
        return this;
    }

    @Override
    public Builder mainModule(String mainModule) {
        this.mainModule = mainModule;
        return this;
    }

    public FregeDTO build() {
        return new FregeDTO(version, release, compilerDownloadDir, mainSourceDir, outputDir, mainModule);
    }
}
