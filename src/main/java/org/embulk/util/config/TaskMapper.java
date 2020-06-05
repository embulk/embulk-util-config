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
        final ObjectNode objectNode = Compat.rebuildObjectNode(task);

        final T value;
        try {
            value = this.objectMapper.readValue(objectNode.traverse(), taskType);
        } catch (final IOException ex) {  // JsonParseException and JsonMappingException are IOException.
            // For tasks, not a ConfigException.
            throw new UncheckedIOException(ex);
        }
        return value;
    }

    private final ObjectMapper objectMapper;
}
