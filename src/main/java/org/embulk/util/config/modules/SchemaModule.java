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

package org.embulk.util.config.modules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import org.embulk.spi.Column;
import org.embulk.spi.Schema;

public final class SchemaModule extends SimpleModule {
    public SchemaModule() {
        this.addSerializer(Schema.class, new SchemaSerializer());
        this.addDeserializer(Schema.class, new SchemaDeserializer());
    }

    private static class SchemaSerializer extends JsonSerializer<Schema> {
        @Override
        public void serialize(
                final Schema value,
                final JsonGenerator jsonGenerator,
                final SerializerProvider provider)
                throws IOException {
            final ArrayNode array = OBJECT_MAPPER.createArrayNode();
            for (final Column column : value.getColumns()) {
                final ObjectNode object = OBJECT_MAPPER.createObjectNode();
                object.put("index", column.getIndex());
                object.put("name", column.getName());
                object.put("type", column.getType().getName());
                array.add(object);
            }
            jsonGenerator.writeTree(array);
        }
    }

    private static class SchemaDeserializer extends StdNodeBasedDeserializer<Schema> {
        protected SchemaDeserializer() {
            super(Schema.class);
        }

        @Override
        public Schema convert(
                final JsonNode root,
                final DeserializationContext context)
                throws JsonProcessingException {
            if (root == null || !root.isArray()) {
                throw JsonMappingException.from(context.getParser(), "Schema expects a JSON Array node.");
            }
            final ArrayNode array = (ArrayNode) root;

            final ArrayList<Column> builder = new ArrayList<>();
            for (final JsonNode element : (Iterable<JsonNode>) () -> array.elements()) {
                builder.add(ColumnModule.convertJsonNodeToColumn(element, context.getParser()));
            }

            return new Schema(Collections.unmodifiableList(builder));
        }
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
}
