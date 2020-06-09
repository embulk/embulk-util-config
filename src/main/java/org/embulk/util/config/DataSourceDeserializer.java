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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.embulk.config.DataSource;

// TODO T extends DataSource super DataSourceImpl
final class DataSourceDeserializer<T extends DataSource> extends JsonDeserializer<T> {
    DataSourceDeserializer(final ObjectMapper nestedObjectMapper) {
        this.nestedObjectMapper = nestedObjectMapper;
        this.treeObjectMapper = new ObjectMapper();
    }

    @Override
    public T deserialize(final JsonParser jsonParser, final DeserializationContext context) throws IOException {
        final JsonNode json = treeObjectMapper.readTree(jsonParser);
        if (!json.isObject()) {
            throw new JsonMappingException("Expected object to deserialize DataSource", jsonParser.getCurrentLocation());
        }
        return castToT(new DataSourceImpl((ObjectNode) json, this.nestedObjectMapper));
    }

    @SuppressWarnings("unchecked")
    private T castToT(final DataSourceImpl data) {
        return (T) data;
    }

    private final ObjectMapper nestedObjectMapper;
    private final ObjectMapper treeObjectMapper;
}
