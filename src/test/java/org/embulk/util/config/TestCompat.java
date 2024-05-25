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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

public class TestCompat {
    @Test
    public void testToJson() throws IOException {
        final ObjectNode node = SIMPLE_MAPPER.createObjectNode();
        node.put("foo", "bar");
        final DataSourceImpl impl = new DataSourceImpl(node, SIMPLE_MAPPER);
        assertEquals("{\"foo\":\"bar\"}", Compat.toJson(impl));
    }

    @Test
    public void testToMap() throws IOException {
        final ObjectNode node = SIMPLE_MAPPER.createObjectNode();
        node.put("foo", "bar");
        final DataSourceImpl impl = new DataSourceImpl(node, SIMPLE_MAPPER);

        final LinkedHashMap<String, Object> expected = new LinkedHashMap<>();
        expected.put("foo", "bar");
        assertEquals(expected, Compat.toMap(impl));
    }

    private static final ObjectMapper SIMPLE_MAPPER = new ObjectMapper();
}
