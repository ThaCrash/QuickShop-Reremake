package org.maxgamer.quickshop;

import lombok.Cleanup;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;

@Getter
public class BuildInfo {
    private final String buildId;
    private final String buildTag;
    private final String buildUrl;
    private final String gitCommit;
    private final String gitBranch;
    private final String pomVersion;
    private final String pomGruopId;
    private final String pomArtifactId;

    public BuildInfo(@NotNull QuickShop plugin) {
        @Cleanup
        InputStream inputStream = plugin.getResource("BUILDINFO");
        if (inputStream == null) {
            buildId = "Unknown";
            buildTag = "Unknown";
            buildUrl = "Unknown";
            gitCommit = "Custom Build";
            gitBranch = "Unknown";
            pomVersion = "Unknown";
            pomGruopId = "Unknown";
            pomArtifactId = "Unknown";
            return;
        }
        YamlConfiguration buildInfo = YamlConfiguration.loadConfiguration(new InputStreamReader(inputStream));
        buildId = buildInfo.getString("build-id", "Unknown");
        buildTag = buildInfo.getString("build-tag", "Unknown");
        buildUrl = buildInfo.getString("build-url", "Unknown");
        gitCommit = buildInfo.getString("git-commit", "Invalid");
        gitBranch = buildInfo.getString("git-branch", "Unknown");
        pomVersion = buildInfo.getString("pom-version", "Unknown");
        pomGruopId = buildInfo.getString("pom-groupid", "Unknown");
        pomArtifactId = buildInfo.getString("pom-artifactid", "Unknown");
    }
}