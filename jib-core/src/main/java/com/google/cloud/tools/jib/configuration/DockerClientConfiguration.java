package com.google.cloud.tools.jib.configuration;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public class DockerClientConfiguration {

    /** Builder for instantiating a {@link DockerClientConfiguration}. */
    public static class Builder {

        @Nullable private ImmutableMap<String, String> environmentMap;

        /**
         * Sets the container's environment variables, mapping variable name to value.
         *
         * @param environmentMap the map
         * @return this
         */
        public Builder setEnvironment(@Nullable Map<String, String> environmentMap) {
            if (environmentMap == null) {
                this.environmentMap = null;
            } else {
                Preconditions.checkArgument(!Iterables.any(environmentMap.keySet(), Objects::isNull));
                Preconditions.checkArgument(!Iterables.any(environmentMap.values(), Objects::isNull));
                this.environmentMap = ImmutableMap.copyOf(environmentMap);
            }
            return this;
        }

        /**
         * Builds the {@link DockerClientConfiguration}.
         *
         * @return the corresponding {@link DockerClientConfiguration}
         */
        public DockerClientConfiguration build() {
            return new DockerClientConfiguration(environmentMap);
        }

        private Builder() {}
    }

    /**
     * Constructs a builder for a {@link DockerClientConfiguration}.
     *
     * @return the builder
     */
    public static DockerClientConfiguration.Builder builder() {
        return new DockerClientConfiguration.Builder();
    }

    @Nullable private final ImmutableMap<String, String> environmentMap;

    private DockerClientConfiguration (
            @Nullable ImmutableMap<String, String> environmentMap) {
        this.environmentMap = environmentMap;
    }

    @Nullable
    public ImmutableMap<String, String> getEnvironmentMap() {
        return environmentMap;
    }

    @Override
    @VisibleForTesting
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DockerClientConfiguration)) {
            return false;
        }
        DockerClientConfiguration otherDockerClientConfiguration = (DockerClientConfiguration) other;
        return Objects.equals(environmentMap, otherDockerClientConfiguration.environmentMap);
    }

    @Override
    @VisibleForTesting
    public int hashCode() {
        return Objects.hash(environmentMap);
    }

}
