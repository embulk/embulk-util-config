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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.Validator;

final class ConfigTaskDeserializer<T extends Task> extends JsonDeserializer<T> {
    ConfigTaskDeserializer(
            final Class<T> taskInterface,
            final TaskObjectsRetriever taskObjectsRetriever,
            final ObjectMapper nestedObjectMapper,
            final Validator validator) {
        this.taskInterface = taskInterface;
        this.taskObjectsRetriever = taskObjectsRetriever;
        this.nestedObjectMapper = nestedObjectMapper;
        this.validator = validator;
    }

    @Override
    public final T deserialize(final JsonParser jsonParser, final DeserializationContext context) throws IOException {
        final ConcurrentHashMap<String, Object> internalObjects = this.taskObjectsRetriever.buildTaskBackingObjects(jsonParser);
        return castToT(Proxy.newProxyInstance(
                this.taskInterface.getClassLoader(),
                new Class<?>[] { this.taskInterface },
                new TaskInvocationHandler(this.taskInterface, internalObjects, this.nestedObjectMapper, this.validator)));
    }

    @SuppressWarnings("unchecked")
    private T castToT(final Object proxy) {
        return (T) proxy;
    }

    private final Class<? extends Task> taskInterface;
    private final TaskObjectsRetriever taskObjectsRetriever;
    private final ObjectMapper nestedObjectMapper;
    private final Validator validator;
}
