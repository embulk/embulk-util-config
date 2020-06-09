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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Retrieves actual user configuration values from JSON, and builds a {@link java.util.Map} of {@link java.lang.Object}s backing behind a {@link Task}.
 */
final class TaskObjectsRetriever {
    private TaskObjectsRetriever(
            final Class<? extends Task> taskInterface,
            final Map<String, List<TaskField>> taskFieldsFromJsonFieldName,
            final boolean useDefault,
            final ObjectMapper nestedObjectMapper) {
        this.taskInterface = taskInterface;
        this.taskFieldsFromJsonFieldName = taskFieldsFromJsonFieldName;
        this.useDefault = useDefault;
        this.nestedObjectMapper = nestedObjectMapper;
    }

    static final TaskObjectsRetriever forConfig(
            final Class<? extends Task> taskInterface,
            final ObjectMapper nestedObjectMapper) {
        return new TaskObjectsRetriever(
                taskInterface,
                mapTaskFieldsFromJsonFieldName(taskInterface, taskField -> taskField.getFieldNameMappedFromConfigSourceJson()),
                true,
                nestedObjectMapper);
    }

    static final TaskObjectsRetriever forTask(
            final Class<? extends Task> taskInterface,
            final ObjectMapper nestedObjectMapper) {
        return new TaskObjectsRetriever(
                taskInterface,
                mapTaskFieldsFromJsonFieldName(taskInterface, taskField -> taskField.getFieldNameMappedFromTaskSourceJson()),
                false,
                nestedObjectMapper);
    }

    /**
     * Builds a {@link java.util.Map} of {@link java.lang.Object}s backing behind a {@link Task}, from {@link com.fasterxml.jackson.core.JsonParser}.
     *
     * <p>For example, assume a {@code ConfigSource} JSON {@code { "config": "1234" }} is given for the {@code PluginTask} below.
     *
     * <pre><code> interface PluginTask extends Task {
     *    {@literal @}Config("config")
     *     String getConfigAsString();
     *
     *    {@literal @}Config("config")
     *     int getConfigAsInteger();
     *
     *    {@literal @}Config("someone")
     *    {@literal @}ConfigDefault("any")
     *     String getSomeone();
     * }</code></pre>
     *
     * <p>For that case, the built {@link java.util.Map} is like {@code { "ConfigAsString": "1234", "ConfigAsInteger": 1234, "Someone": "any"}}.
     *
     * @param parser  {@link com.fasterxml.jackson.core.JsonParser} as a source of the backing {@link java.lang.Object}s
     * @return {@link java.util.Map} of backing {@link java.lang.Object}s
     * @throws IOException  hoge
     */
    final ConcurrentHashMap<String, Object> buildTaskBackingObjects(final JsonParser parser) throws IOException {
        final HashMap<TaskField, String> unfilledTaskFields = new HashMap<>();
        for (final Map.Entry<String, List<TaskField>> sourceJsonKeyAndTaskFields : this.taskFieldsFromJsonFieldName.entrySet()) {
            final String sourceJsonKey = sourceJsonKeyAndTaskFields.getKey();
            for (final TaskField taskField : sourceJsonKeyAndTaskFields.getValue()) {
                unfilledTaskFields.put(taskField, sourceJsonKey);
            }
        }

        final ArrayList<NullPointerException> nullExceptions = new ArrayList<>();
        final ConcurrentHashMap<String, Object> taskBackingObjects =
                buildTaskBackingObjectsOnlyAvailableInJson(parser, unfilledTaskFields, nullExceptions);

        // Set default values.
        for (final Map.Entry<TaskField, String> unfilledTaskFieldAndKey : unfilledTaskFields.entrySet()) {
            final TaskField unfilledTaskField = unfilledTaskFieldAndKey.getKey();
            final String key = unfilledTaskFieldAndKey.getValue();

            // @ConfigDefault works (only) in Configs.
            if (this.useDefault && unfilledTaskField.getDefaultValueInJsonString().isPresent()) {
                final Object value = this.nestedObjectMapper.readValue(
                        unfilledTaskField.getDefaultValueInJsonString().get(),
                        new GenericTypeReference(unfilledTaskField.getReturnType()));
                if (value == null) {
                    nullExceptions.add(new NullPointerException(
                            "Setting null to a task field is not allowed: " + key + ". "
                            + unfilledTaskField.getGetterMethod().getName()
                            + "() has to use java.util.Optional<T> to represent null."));
                } else {
                    taskBackingObjects.put(unfilledTaskField.getName(), value);
                }
            } else {
                // required field
                throw JsonMappingException.from(parser, "Field '" + key + "' is required but not set.");
            }
        }

        if (!nullExceptions.isEmpty()) {
            final JsonMappingException ex = new JsonMappingException("Setting null to a task field is not allowed.");
            for (final NullPointerException inner : nullExceptions) {
                ex.addSuppressed(inner);
            }
            throw ex;
        }

        return taskBackingObjects;
    }

    private ConcurrentHashMap<String, Object> buildTaskBackingObjectsOnlyAvailableInJson(
            final JsonParser parser,
            final HashMap<TaskField, String> unfilledTaskFields,
            final ArrayList<NullPointerException> nullExceptions)
            throws IOException {
        final ConcurrentHashMap<String, Object> taskBackingObjects = new ConcurrentHashMap<>();

        final String firstKey;
        if (JsonToken.START_OBJECT == parser.getCurrentToken()) {
            final JsonToken dummy = parser.nextToken();
            firstKey = parser.getCurrentName();
        } else {
            firstKey = parser.nextFieldName();
        }

        for (String key = firstKey; key != null; key = parser.nextFieldName()) {
            final JsonToken dummy = parser.nextToken();  // Skip the next token to get the value.

            final List<TaskField> taskFieldsForKey = this.taskFieldsFromJsonFieldName.get(key);
            if (taskFieldsForKey.isEmpty()) {
                parser.skipChildren();
                continue;
            }

            final JsonNode children = this.nestedObjectMapper.readValue(parser, JsonNode.class);
            for (final TaskField taskField : taskFieldsForKey) {
                final Object value = this.nestedObjectMapper.convertValue(
                        children, new GenericTypeReference(taskField.getReturnType()));
                if (value == null) {
                    nullExceptions.add(new NullPointerException(
                            "Setting null to a task field is not allowed: " + key + ". "
                            + taskField.getGetterMethod().getName()
                            + "() has to use java.util.Optional<T> to represent null."));
                } else {
                    taskBackingObjects.put(taskField.getName(), value);
                }

                if (!unfilledTaskFields.remove(taskField, key)) {
                    throw new JsonMappingException(String.format(
                            "FATAL: Expected to be a bug in embulk-util-config."
                            + " Mapping \"%s: (%s) %s\" might have already been processed, or not in %s.",
                            key,
                            taskField.getReturnType().toString(),
                            taskField.getName(),
                            this.taskInterface.toString()));
                }
            }
        }

        return taskBackingObjects;
    }

    private static Map<String, List<TaskField>> mapTaskFieldsFromJsonFieldName(
            final Class<? extends Task> taskInterface,
            final Function<TaskField, String> getFieldNameMappedFromSourceJson) {
        final ConcurrentHashMap<String, List<TaskField>> taskFieldsFromJsonFieldName = new ConcurrentHashMap<>();

        for (final Method method : taskInterface.getMethods()) {
            final TaskField taskField = TaskField.of(method);
            if (taskField == null) {
                continue;
            }

            final String keyExpectedInSourceJson = getFieldNameMappedFromSourceJson.apply(taskField);
            if (keyExpectedInSourceJson == null) {
                continue;
            }

            taskFieldsFromJsonFieldName.compute(keyExpectedInSourceJson, (key, oldValue) -> {
                if (oldValue == null) {
                    final ArrayList<TaskField> newValue = new ArrayList<>();
                    newValue.add(taskField);
                    return newValue;
                } else {
                    oldValue.add(taskField);
                    return oldValue;
                }
            });
        }
        taskFieldsFromJsonFieldName.replaceAll((key, value) -> Collections.unmodifiableList(value));

        return Collections.unmodifiableMap(taskFieldsFromJsonFieldName);
    }

    private final Class<? extends Task> taskInterface;
    private final Map<String, List<TaskField>> taskFieldsFromJsonFieldName;
    private final boolean useDefault;
    private final ObjectMapper nestedObjectMapper;
}
