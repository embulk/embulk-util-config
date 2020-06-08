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
import java.io.UncheckedIOException;
import org.embulk.config.TaskSource;

/**
 * Maps {@code org.embulk.config.TaskSource} into a task-defining interface that inherits {@link Task}.
 *
 * <p>It performs the same job with Embulk core's {@code ConfigSource#loadTask} on plugin's side.
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
 *     public ConfigDiff transaction(ConfigSource config, InputPlugin.Control control) {
 *         // ...
 *     }
 *
 *     public TaskReport run(TaskSource taskSource, Schema schema, int taskIndex, PageOutput output) {
 *         final org.embulk.util.config.TaskMapper taskMapper = this.configMapperFactory.createTaskMapper();
 *         final PluginTask task = taskMapper.map(taskSource, PluginTask.class);
 *         // ...
 *     }
 *
 *     // ...
 *
 *     private org.embulk.util.config.ConfigMapperFactory configMapperFactory;
 * }}</pre>
 */
public final class TaskMapper {
    TaskMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Maps {@code org.embulk.config.TaskSource} into a task-defining interface that inherits {@link Task}.
     *
     * @param <T>  the task-defining interface
     * @param task  {@code org.embulk.config.TaskSource} to map from
     * @param taskType  {@link java.lang.Class} of the task-defining interface
     * @return a mapped task instance
     */
    public <T extends Task> T map(final TaskSource task, final Class<T> taskType) {
        final ObjectNode objectNode;
        try {
            objectNode = Compat.rebuildObjectNode(task);
        } catch (final IOException ex) {
            // It should happen only from DataSource#toJson(), not from rebuilding ObjectNode.
            throw new UncheckedIOException("org.embulk.config.TaskSource#toJson() returned an invalid JSON.", ex);
        }

        final T value;
        try {
            value = this.objectMapper.readValue(objectNode.traverse(), taskType);
        } catch (final JsonMappingException ex) {
            throw new UncheckedIOException("Failed to map a JSON value into some object.", ex);
        } catch (final JsonParseException ex) {
            throw new UncheckedIOException("Unexpected failure in parsing ObjectNode rebuilt from org.embulk.config.TaskSource.", ex);
        } catch (final JsonProcessingException ex) {
            throw new UncheckedIOException("Unexpected failure in processing ObjectNode rebuilt from org.embulk.config.TaskSource.", ex);
        } catch (final IOException ex) {
            throw new UncheckedIOException("Unexpected I/O error in ObjectNode rebuilt from org.embulk.config.TaskSource.", ex);
        }
        return value;
    }

    private final ObjectMapper objectMapper;
}
