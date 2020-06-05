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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.embulk.config.DataSource;

final class DataSourceSerializer<T extends DataSource> extends JsonSerializer<T> {
    DataSourceSerializer(final ObjectMapper nestedObjectMapper) {
        this.nestedObjectMapper = nestedObjectMapper;
    }

    @Override
    public void serialize(
            final T value,
            final JsonGenerator jsonGenerator,
            final SerializerProvider serializerProvider)
            throws IOException {
        if (value == null) {
            throw new JsonGenerationException(new NullPointerException(
                    "DataSourceSerializer#serialize accepts only non-null value."));
        }
        final String valueJsonStringified = Compat.toJson(value);  // TODO: DataSource#toJson
        if (valueJsonStringified == null) {
            throw new JsonGenerationException(new NullPointerException(
                    "DataSourceSerializer#serialize accepts only valid DataSource."));
        }
        final JsonNode valueJsonNode = this.nestedObjectMapper.readValue(valueJsonStringified, JsonNode.class);
        if (!valueJsonNode.isObject()) {
            throw new JsonGenerationException(new ClassCastException(
                    "DataSourceSerializer#serialize accepts only valid JSON object."));
        }
        ((ObjectNode) valueJsonNode).serialize(jsonGenerator, serializerProvider);
    }

    private final ObjectMapper nestedObjectMapper;
}
