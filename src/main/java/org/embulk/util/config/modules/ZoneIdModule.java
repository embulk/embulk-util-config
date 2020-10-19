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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ZoneIdModule extends SimpleModule {
    private ZoneIdModule(final boolean usesLegacyNames) {
        this.addSerializer(ZoneId.class, new ZoneIdSerializer());
        this.addDeserializer(ZoneId.class, new ZoneIdDeserializer(usesLegacyNames));
    }

    public ZoneIdModule() {
        this(false);
    }

    public static ZoneIdModule withLegacyNames() {
        return new ZoneIdModule(true);
    }

    private static class ZoneIdSerializer extends JsonSerializer<ZoneId> {
        @Override
        public void serialize(
                final ZoneId value,
                final JsonGenerator jsonGenerator,
                final SerializerProvider provider)
                throws IOException {
            jsonGenerator.writeString(value.getId());
        }
    }

    private static class ZoneIdDeserializer extends FromStringDeserializer<ZoneId> {
        public ZoneIdDeserializer(final boolean usesLegacyNames) {
            super(ZoneId.class);
            this.usesLegacyNames = usesLegacyNames;
        }

        @Override
        protected ZoneId _deserialize(final String value, final DeserializationContext context)
                throws JsonMappingException {
            final Optional<String> suggestion = LegacyZones.getSuggestion(value);
            if (suggestion.isPresent()) {
                if (this.usesLegacyNames) {
                    logger.warn(suggestion.get());
                } else {
                    throw JsonMappingException.from(context.getParser(), suggestion.get());
                }
            }

            final Optional<ZoneId> alternative = LegacyZones.getAlternative(value);
            if (alternative.isPresent()) {
                logger.warn("\"{}\" is recognized as \"{}\" to be compatible with the legacy style.", value, alternative.get());
                return alternative.get();
            }

            try {
                return ZoneId.of(value);
            } catch (final DateTimeException ex) {
                throw JsonMappingException.from(
                        context.getParser(),
                        String.format("\"%s\" is not recognized as a timezone name.", value),
                        ex);
            }
        }

        private final boolean usesLegacyNames;
    }

    private static final Logger logger = LoggerFactory.getLogger(ZoneIdModule.class);
}
