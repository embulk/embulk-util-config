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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

public class TestTaskObjectsRetriever {
    @Test
    public void testForConfig() throws Exception {
        final ObjectNode node = MAPPER.createObjectNode();
        node.put("boolean", true);
        node.put("double", 1240.8042);
        node.put("int", 9134);
        node.put("long", 9480124L);
        node.put("string", "value");
        node.put("optional2", "foobar");
        node.put("unexpected", "some mysterious value");
        final JsonParser parser = node.traverse();

        // TaskObjectsRetriever#buildTaskBackingObjects() expects JsonParser whose "current token" is the first token.
        //
        // JsonDeserializer#deserialize passes JsonParser whose "current token" is already the first token
        // although JsonNode#traverse returns JsonParser whose "current token" is still null, and which needs nextToken().
        parser.nextToken();

        final TaskObjectsRetriever retriever = TaskObjectsRetriever.forConfig(TypeFieldsTask.class, MAPPER);
        final ConcurrentHashMap<String, Object> actual = retriever.buildTaskBackingObjects(parser);

        final ConcurrentHashMap<String, Object> expected = new ConcurrentHashMap<>();
        expected.put("TaskBoolean", true);
        expected.put("TaskDouble", 1240.8042);
        expected.put("TaskInt", 9134);
        expected.put("TaskLong", 9480124L);
        expected.put("TaskString", "value");
        expected.put("TaskOptional1", Optional.<String>empty());
        expected.put("TaskOptional2", Optional.<String>of("foobar"));

        assertEquals(expected, actual);
    }

    private static interface TypeFieldsTask extends Task {
        @Config("boolean")
        boolean getTaskBoolean();

        @Config("double")
        double getTaskDouble();

        @Config("int")
        int getTaskInt();

        @Config("long")
        long getTaskLong();

        @Config("string")
        String getTaskString();

        @Config("optional1")
        @ConfigDefault("null")
        Optional<String> getTaskOptional1();

        @Config("optional2")
        @ConfigDefault("null")
        Optional<String> getTaskOptional2();

        String getTaskExtra();

        void setTaskExtra(String extra);
    }

    private static final ObjectMapper MAPPER = (new ObjectMapper()).registerModule(new Jdk8Module());
}
