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

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.Map;
import org.embulk.spi.type.Type;
import org.embulk.spi.type.Types;

public class TypeDeserializer extends FromStringDeserializer<Type> {
    public TypeDeserializer() {
        super(Type.class);
    }

    @Override
    protected Type _deserialize(String value, DeserializationContext context) throws IOException {
        Type t = stringToTypeMap.get(value);
        if (t == null) {
            throw new JsonMappingException(
                    String.format("Unknown type name '%s'. Supported types are: %s",
                                  value,
                                  Joiner.on(", ").join(stringToTypeMap.keySet())));
        }
        return t;
    }

    static {
        ImmutableMap.Builder<String, Type> builder = ImmutableMap.builder();
        builder.put(Types.BOOLEAN.getName(), Types.BOOLEAN);
        builder.put(Types.LONG.getName(), Types.LONG);
        builder.put(Types.DOUBLE.getName(), Types.DOUBLE);
        builder.put(Types.STRING.getName(), Types.STRING);
        builder.put(Types.TIMESTAMP.getName(), Types.TIMESTAMP);
        builder.put(Types.JSON.getName(), Types.JSON);
        stringToTypeMap = builder.build();
    }

    private static final Map<String, Type> stringToTypeMap;
}
