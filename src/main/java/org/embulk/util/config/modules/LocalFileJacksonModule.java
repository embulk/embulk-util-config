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
        public void serialize(LocalFile value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();
            jgen.writeFieldName("base64");
            jgen.writeBinary(value.getContent());
            jgen.writeEndObject();
        }
    }

    private static class LocalFileDeserializer extends JsonDeserializer<LocalFile> {
        @Override
        public LocalFile deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonToken t = jp.getCurrentToken();
            if (t == JsonToken.START_OBJECT) {
                t = jp.nextToken();
            }

            switch (t) {
                case VALUE_NULL:
                    return null;

                case FIELD_NAME: {
                    LocalFile result;

                    String keyName = jp.getCurrentName();
                    if ("content".equals(keyName)) {
                        jp.nextToken();
                        result = LocalFile.ofContent(jp.getValueAsString());
                    } else if ("base64".equals(keyName)) {
                        jp.nextToken();
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        jp.readBinaryValue(ctxt.getBase64Variant(), out);
                        result = LocalFile.ofContent(out.toByteArray());
                    } else {
                        throw ctxt.mappingException("Unknown key '" + keyName + "' to deserialize LocalFile");
                    }

                    t = jp.nextToken();
                    if (t != JsonToken.END_OBJECT) {
                        throw ctxt.mappingException("Unexpected extra map keys to LocalFile");
                    }
                    return result;
                }

                case END_OBJECT:
                case START_ARRAY:
                case END_ARRAY:
                    throw ctxt.mappingException("Attempted unexpected map or array to LocalFile");

                case VALUE_EMBEDDED_OBJECT: {
                    Object obj = jp.getEmbeddedObject();
                    if (obj == null) {
                        return null;
                    }
                    if (LocalFile.class.isAssignableFrom(obj.getClass())) {
                        return (LocalFile) obj;
                    }
                    throw ctxt.mappingException("Don't know how to convert embedded Object of type " + obj.getClass().getName() + " into LocalFile");
                }

                default:
                    return LocalFile.of(jp.getValueAsString());
            }
        }
    }
}
