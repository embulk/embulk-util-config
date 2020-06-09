/*
 * Copyright 2020 The Embulk project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.embulk.util.config;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.embulk.config.ConfigException;
import org.embulk.config.ConfigSource;

/**
 * Maps {@code org.embulk.config.ConfigSource} into a task-defining interface that inherits {@link Task}.
 *
 * <p>It performs the same job with Embulk core's {@code ConfigSource#loadConfig} on plugin's side.
 *
 * <pre>{@code public class ExampleInputPlugin implements InputPlugin {
 *     public ExampleInputPlugin() {
 *         this.configMapperFactory = org.embulk.util.config.ConfigMapperFactory.withDefault();
 *     }
 *
 *     public interface PluginTask extends org.embulk.util.config.Task {
 *         // ...
 *     }
 *
 *     public ConfigDiff transaction(final ConfigSource config, final InputPlugin.Control control) {
 *         final org.embulk.util.config.ConfigMapper configMapper = this.configMapperFactory.createConfigMapper();
 *         final PluginTask task = configMapper.map(config, PluginTask.class);
 *         // ...
 *     }
 *
 *     // ...
 *
 *     private org.embulk.util.config.ConfigMapperFactory configMapperFactory;
 * }}</pre>
 */
public final class ConfigMapper {
    ConfigMapper(final ObjectMapper objectMapper, final Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    /**
     * Maps {@code org.embulk.config.ConfigSource} into a task-defining interface that inherits {@link Task}.
     *
     * @param <T>  the task-defining interface
     * @param config  {@code org.embulk.config.ConfigSource} to map from
     * @param taskType  {@link java.lang.Class} of the task-defining interface
     * @return a mapped task instance
     */
    public <T extends Task> T map(final ConfigSource config, final Class<T> taskType) {
        final ObjectNode objectNode;
        try {
            objectNode = Compat.rebuildObjectNode(config);
        } catch (final IOException ex) {
            // It should happen only from DataSource#toJson(), not from rebuilding ObjectNode.
            throw new ConfigException("org.embulk.config.ConfigSource#toJson() returned an invalid JSON.", ex);
        } catch (final RuntimeException ex) {
            throw new ConfigException("Unexpected failure in reinterpreting ObjectNode from org.embulk.config.ConfigSource.", ex);
        }

        final T value;
        try {
            value = this.objectMapper.readValue(objectNode.traverse(), taskType);
        } catch (final JsonMappingException ex) {
            throw new ConfigException("Failed to map a JSON value into some object.", ex);
        } catch (final JsonParseException ex) {
            throw new ConfigException("Unexpected failure in parsing ObjectNode rebuilt from org.embulk.config.ConfigSource.", ex);
        } catch (final JsonProcessingException ex) {
            throw new ConfigException("Unexpected failure in processing ObjectNode rebuilt from org.embulk.config.ConfigSource.", ex);
        } catch (final IOException ex) {
            throw new ConfigException("Unexpected I/O error in ObjectNode rebuilt from org.embulk.config.ConfigSource.", ex);
        } catch (final RuntimeException ex) {
            throw new ConfigException("Unexpected failure in ObjectNode rebuilt from org.embulk.config.ConfigSource.", ex);
        }

        if (this.validator != null) {
            final Set<ConstraintViolation<T>> violations = this.validator.validate(value);
            if (!violations.isEmpty()) {
                throw new TaskValidationException(violations);
            }
        }

        return value;
    }

    private final ObjectMapper objectMapper;
    private final Validator validator;
}
