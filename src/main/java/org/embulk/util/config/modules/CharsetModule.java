/*
 * Copyright 2014 The Embulk project
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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.nio.charset.Charset;

public final class CharsetModule extends SimpleModule {
    public CharsetModule() {
        this.addSerializer(Charset.class, new CharsetSerializer());
        this.addDeserializer(Charset.class, new CharsetDeserializer());
    }

    private static class CharsetSerializer extends JsonSerializer<Charset> {
        @Override
        public void serialize(final Charset value, final JsonGenerator jsonGenerator, final SerializerProvider provider)
                throws IOException {
            jsonGenerator.writeString(value.name());
        }
    }

    private static class CharsetDeserializer extends FromStringDeserializer<Charset> {
        public CharsetDeserializer() {
            super(Charset.class);
        }

        @Override
        protected Charset _deserialize(final String value, final DeserializationContext context) throws JsonMappingException {
            try {
                return Charset.forName(value);
            } catch (final UnsupportedOperationException ex) {
                throw new JsonMappingException(String.format("Unknown charset '%s'", value));
            }
        }
    }
}
