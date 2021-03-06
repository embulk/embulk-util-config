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
import org.embulk.spi.time.Timestamp;

@Deprecated
public final class TimestampModule extends SimpleModule {
    public TimestampModule() {
        this.addSerializer(Timestamp.class, new TimestampSerializer());
        this.addDeserializer(Timestamp.class, new TimestampDeserializer());
    }

    private static class TimestampSerializer extends JsonSerializer<Timestamp> {
        @Override
        public void serialize(final Timestamp value, final JsonGenerator jsonGenerator, final SerializerProvider provider)
                throws IOException {
            jsonGenerator.writeString(value.toString());
        }
    }

    private static class TimestampDeserializer extends FromStringDeserializer<Timestamp> {
        public TimestampDeserializer() {
            super(Timestamp.class);
        }

        @Override
        protected Timestamp _deserialize(final String value, final DeserializationContext context) throws JsonMappingException {
            if (value == null) {
                throw new JsonMappingException("TimestampDeserializer#_deserialize received null unexpectedly.");
            }
            try {
                return Timestamp.ofString(value);
            } catch (final NumberFormatException ex) {
                throw new JsonMappingException("Invalid format as a Timestamp value: '" + value + "'", ex);
            } catch (final IllegalStateException ex) {
                throw new JsonMappingException("Unexpected failure in parsing: '" + value + "'", ex);
            }
        }
    }
}
