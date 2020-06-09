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

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.Deserializers;
import javax.validation.Validator;

final class ConfigTaskDeserializers extends Deserializers.Base {
    private ConfigTaskDeserializers(
            final boolean forConfig,
            final ObjectMapper nestedObjectMapper,
            final Validator validator) {
        this.forConfig = forConfig;
        this.nestedObjectMapper = nestedObjectMapper;
        this.validator = validator;
    }

    static ConfigTaskDeserializers forConfig(final ObjectMapper nestedObjectMapper, final Validator validator) {
        return new ConfigTaskDeserializers(true, nestedObjectMapper, validator);
    }

    static ConfigTaskDeserializers forTask(final ObjectMapper nestedObjectMapper, final Validator validator) {
        return new ConfigTaskDeserializers(false, nestedObjectMapper, validator);
    }

    @Override
    public JsonDeserializer<?> findBeanDeserializer(
            final JavaType type,
            final DeserializationConfig config,
            final BeanDescription beanDescription)
            throws JsonMappingException {
        final Class<?> rawClass = type.getRawClass();
        if (Task.class.isAssignableFrom(rawClass)) {
            final Class<? extends Task> taskInterface = castToTask(rawClass);

            final TaskObjectsRetriever taskObjectsRetriever;
            if (this.forConfig) {
                taskObjectsRetriever = TaskObjectsRetriever.forConfig(taskInterface, this.nestedObjectMapper);
            } else {
                taskObjectsRetriever = TaskObjectsRetriever.forTask(taskInterface, this.nestedObjectMapper);
            }

            return new ConfigTaskDeserializer<>(
                    taskInterface,
                    taskObjectsRetriever,
                    this.nestedObjectMapper,
                    this.validator);
        }
        return super.findBeanDeserializer(type, config, beanDescription);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Task> castToTask(final Class<?> rawClass) {
        return (Class<? extends Task>) rawClass;
    }

    private final boolean forConfig;
    private final ObjectMapper nestedObjectMapper;
    private final Validator validator;
}
