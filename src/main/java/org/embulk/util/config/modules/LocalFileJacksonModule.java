/*
 * Copyright 2015 The Embulk project
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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.embulk.util.config.units.LocalFile;

public final class LocalFileJacksonModule extends SimpleModule {
    public LocalFileJacksonModule() {
        this.addSerializer(LocalFile.class, new LocalFileSerializer());
        this.addDeserializer(LocalFile.class, new LocalFileDeserializer());
    }

    private static class LocalFileSerializer extends JsonSerializer<LocalFile> {
        @Override
        public void serialize(
                final LocalFile value,
                final JsonGenerator jsonGenerator,
                final SerializerProvider provider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeFieldName("base64");
            jsonGenerator.writeBinary(value.getContent());
            jsonGenerator.writeEndObject();
        }
    }

    private static class LocalFileDeserializer extends JsonDeserializer<LocalFile> {
        @Override
        public LocalFile deserialize(final JsonParser jsonParser, final DeserializationContext context) throws IOException {
            final JsonToken currentToken = jsonParser.getCurrentToken();
            final JsonToken startingToken;
            if (currentToken == JsonToken.START_OBJECT) {
                startingToken = jsonParser.nextToken();
            } else {
                startingToken = currentToken;
            }

            switch (startingToken) {
                case VALUE_NULL:
                    return null;

                case FIELD_NAME: {
                    final String keyName = jsonParser.getCurrentName();

                    final LocalFile result;
                    if ("content".equals(keyName)) {
                        jsonParser.nextToken();
                        result = LocalFile.ofContent(jsonParser.getValueAsString());
                    } else if ("base64".equals(keyName)) {
                        jsonParser.nextToken();
                        final ByteArrayOutputStream out = new ByteArrayOutputStream();
                        jsonParser.readBinaryValue(context.getBase64Variant(), out);
                        result = LocalFile.ofContent(out.toByteArray());
                    } else {
                        throw context.mappingException("Unknown key '" + keyName + "' to deserialize LocalFile");
                    }

                    final JsonToken nextToken = jsonParser.nextToken();
                    if (nextToken != JsonToken.END_OBJECT) {
                        throw context.mappingException("Unexpected extra map keys to LocalFile");
                    }
                    return result;
                }

                case END_OBJECT:
                case START_ARRAY:
                case END_ARRAY:
                    throw context.mappingException("Attempted unexpected map or array to LocalFile");

                case VALUE_EMBEDDED_OBJECT: {
                    final Object embeddedObject = jsonParser.getEmbeddedObject();
                    if (embeddedObject == null) {
                        return null;
                    }
                    if (LocalFile.class.isAssignableFrom(embeddedObject.getClass())) {
                        return (LocalFile) embeddedObject;
                    }
                    throw context.mappingException(
                            "Don't know how to convert embedded Object of type " + embeddedObject.getClass().getName() + " into LocalFile");
                }

                default:
                    return LocalFile.of(jsonParser.getValueAsString());
            }
        }
    }
}
