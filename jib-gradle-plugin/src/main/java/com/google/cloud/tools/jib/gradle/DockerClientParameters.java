/*
 * Copyright 2018 Google LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.tools.jib.gradle;

import com.google.cloud.tools.jib.plugins.common.ConfigurationPropertyValidator;
import com.google.cloud.tools.jib.plugins.common.PropertyNames;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import static com.google.cloud.tools.jib.docker.DockerClient.DEFAULT_DOCKER_CLIENT;

/**
 * A bean that configures properties of the container run from the image. This is configurable with
 * Groovy closures and can be validated when used as a task input.
 */
public class DockerClientParameters {

    private Path executable = Paths.get(DEFAULT_DOCKER_CLIENT);
    private Map<String, String> environment = Collections.emptyMap();

    @Input
    @Optional
    public Map<String, String> getEnvironment() {
        if (System.getProperty(PropertyNames.DOCKER_CLIENT_ENVIRONMENT) != null) {
            return ConfigurationPropertyValidator.parseMapProperty(
                    System.getProperty(PropertyNames.DOCKER_CLIENT_ENVIRONMENT));
        }
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    @Input
    @Optional
    public Path getExecutable() {
        if (System.getProperty(PropertyNames.DOCKER_CLIENT_EXECUTABLE) != null) {
            return Paths.get(System.getProperty(PropertyNames.DOCKER_CLIENT_EXECUTABLE));
        }
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = Paths.get(executable);
    }


}
