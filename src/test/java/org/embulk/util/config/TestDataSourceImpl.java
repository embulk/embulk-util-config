/*
 * Copyright 2022 The Embulk project
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Map;
import org.embulk.config.ConfigException;
import org.junit.jupiter.api.Test;

public class TestDataSourceImpl {
    @Test
    public void testGetList() {
        final DataSourceImpl impl = createTestNode();
        final List<?> list = impl.get(List.class, "array");
        assertEquals(4, list.size());
        assertTrue(list.get(0) instanceof String);
        assertEquals("hoge", list.get(0));
        assertTrue(list.get(1) instanceof String);
        assertEquals("fuga", list.get(1));
        assertTrue(list.get(2) instanceof Map);
        assertTrue(list.get(3) instanceof List);
    }

    @Test
    public void testGetObject() {
        final DataSourceImpl impl = createTestNode();
        final DataSourceImpl object = impl.getNested("object");
        assertTrue(object.has("key1"));
        assertTrue(object.has("key2"));
        assertEquals("value1", object.get(String.class, "key1"));
        assertEquals("value2", object.get(String.class, "key2"));
    }

    @Test
    public void testFailToGetNestedAsList() {
        final DataSourceImpl impl = createTestNode();
        try {
            impl.get(List.class, "object");
        } catch (final ConfigException ex) {
            assertTrue(ex.getCause() instanceof com.fasterxml.jackson.core.JsonProcessingException);
            return;
        }
        fail("ConfigException should be thrown by getting a String value as a List.");
    }

    @Test
    public void testFailToGetStringAsList() {
        final DataSourceImpl impl = createTestNode();
        try {
            impl.get(List.class, "string");
        } catch (final ConfigException ex) {
            assertTrue(ex.getCause() instanceof com.fasterxml.jackson.core.JsonProcessingException);
            return;
        }
        fail("ConfigException should be thrown by getting a String value as a List.");
    }

    @Test
    public void testFailToGetListAsNested() {
        final DataSourceImpl impl = createTestNode();
        try {
            impl.getNested("array");
        } catch (final ConfigException ex) {
            assertEquals("Attribute array must be an object.", ex.getMessage());
            return;
        }
        fail("ConfigException should be thrown by getting a String value as a List.");
    }

    @Test
    public void testFailToGetStringAsNested() {
        final DataSourceImpl impl = createTestNode();
        try {
            impl.getNested("string");
        } catch (final ConfigException ex) {
            assertEquals("Attribute string must be an object.", ex.getMessage());
            return;
        }
        fail("ConfigException should be thrown by getting a String value as a List.");
    }

    private static DataSourceImpl createTestNode() {
        final ObjectNode root = SIMPLE_MAPPER.createObjectNode();
        root.put("string", "foo");
        root.put("boolean", true);
        root.put("int", 12);
        root.put("double", 42914.1420);
        final ArrayNode array = SIMPLE_MAPPER.createArrayNode();
        array.add("hoge");
        array.add("fuga");
        final ObjectNode objectUnderList = SIMPLE_MAPPER.createObjectNode();
        objectUnderList.put("subkey1", "something");
        array.add(objectUnderList);
        final ArrayNode arrayUnderList = SIMPLE_MAPPER.createArrayNode();
        arrayUnderList.add("somewhat");
        array.add(arrayUnderList);
        root.put("array", array);
        final ObjectNode object = SIMPLE_MAPPER.createObjectNode();
        object.put("key1", "value1");
        object.put("key2", "value2");
        root.put("object", object);
        return new DataSourceImpl(root, SIMPLE_MAPPER);
    }

    private static final ObjectMapper SIMPLE_MAPPER = new ObjectMapper();
}
