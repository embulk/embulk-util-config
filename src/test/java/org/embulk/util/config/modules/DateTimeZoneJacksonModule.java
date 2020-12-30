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

/**
 * Legacy SerDe of Joda-Time {@code DataTimeZone} and String in {@code embulk-core}.
 *
 * <p>It is copied from {@code embulk-core} v0.10.18's {@code org.embulk.spi.time.DateTimeZoneJacksonModule}, and modified a bit.
 */
final class DateTimeZoneJacksonModule extends SimpleModule {
    DateTimeZoneJacksonModule() {
        this.addSerializer(org.joda.time.DateTimeZone.class, new DateTimeZoneSerializer());
        this.addDeserializer(org.joda.time.DateTimeZone.class, new DateTimeZoneDeserializer());
    }

    private static class DateTimeZoneSerializer extends JsonSerializer<org.joda.time.DateTimeZone> {
        @Override
        public void serialize(org.joda.time.DateTimeZone value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException {
            jgen.writeString(value.getID());
        }
    }

    private static class DateTimeZoneDeserializer extends FromStringDeserializer<org.joda.time.DateTimeZone> {
        public DateTimeZoneDeserializer() {
            super(org.joda.time.DateTimeZone.class);
        }

        @Override
        protected org.joda.time.DateTimeZone _deserialize(String value, DeserializationContext context)
                throws JsonMappingException {
            org.joda.time.DateTimeZone parsed = JodaDateTimeZones.parseJodaDateTimeZone(value);
            if (parsed == null) {
                // TODO include link to a document to the message for the list of supported time zones
                throw new JsonMappingException(String.format("Unknown time zone name '%s'", value));
            }
            return parsed;
        }
    }
}
