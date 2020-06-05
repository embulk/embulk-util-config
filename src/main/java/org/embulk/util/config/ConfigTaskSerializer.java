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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

/**
 * Serializes a {@link org.embulk.util.config.Task} instance into a JSON object.
 */
final class ConfigTaskSerializer extends JsonSerializer<Task> {
    ConfigTaskSerializer(final ObjectMapper nestedObjectMapper) {
        this.nestedObjectMapper = nestedObjectMapper;
    }

    @Override
    public void serialize(
            final Task value,
            final JsonGenerator jsonGenerator,
            final SerializerProvider provider)
            throws IOException {
        final ObjectNode objectNode = value.toObjectNode();
        this.nestedObjectMapper.writeTree(jsonGenerator, objectNode);
    }

    private final ObjectMapper nestedObjectMapper;
}
