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

package org.embulk.util.config.rebuild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ShortNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

public class TestObjectNodeRebuilder {
    @Test
    public void testEmpty() {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = mapper.createObjectNode();
        final ObjectNode rebuilt = ObjectNodeRebuilder.rebuild(root, mapper);
        assertEquals(root, rebuilt);
    }

    @Test
    public void testAllTypes() {
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode root = mapper.createObjectNode();

        final ArrayNode array = mapper.createArrayNode();
        final ArrayNode arrayInArray1 = mapper.createArrayNode();
        final ArrayNode arrayInArray2 = mapper.createArrayNode();
        final ArrayNode arrayInArray3 = mapper.createArrayNode();
        final ObjectNode objectInArray = mapper.createObjectNode();

        arrayInArray3.add(new TextNode("innerinner"));
        arrayInArray2.add(arrayInArray3);
        arrayInArray1.add(arrayInArray2);
        objectInArray.set("inner", new TextNode("secret"));
        array.add(arrayInArray1);
        array.add(new BigIntegerNode(new BigInteger("829048219048920184920849120")));
        array.add(new DecimalNode(new BigDecimal("1389832908310941.14890482910")));
        array.add(new DoubleNode((double) 94041.192980));
        array.add(new FloatNode((float) 813.8148));
        array.add(new IntNode(9018411));
        array.add(new LongNode(91084908910248L));
        array.add(NullNode.instance);
        array.add(objectInArray);
        array.add(new ShortNode((short) 9414));
        array.add(new TextNode("Foobar"));

        final ObjectNode object = mapper.createObjectNode();
        final ObjectNode objectInObject1 = mapper.createObjectNode();
        final ObjectNode objectInObject2 = mapper.createObjectNode();
        final ObjectNode objectInObject3 = mapper.createObjectNode();
        final ArrayNode arrayInObject = mapper.createArrayNode();

        objectInObject3.set("in3", new TextNode("something"));
        objectInObject2.set("in2", objectInObject3);
        objectInObject1.set("in1", objectInObject2);
        arrayInObject.add(new TextNode("someone"));
        object.set("array2", arrayInObject);
        object.set("bigInteger2", new BigIntegerNode(new BigInteger("57157912918902184902")));
        object.set("decimal2", new DecimalNode(new BigDecimal("38930890412412.421984084")));
        object.set("double2", new DoubleNode((double) 49041.192980));
        object.set("float2", new FloatNode((float) 133.8148));
        object.set("int2", new IntNode(7018411));
        object.set("long2", new LongNode(61084908910248L));
        object.set("null2", NullNode.instance);
        object.set("object2", objectInObject1);
        object.set("short2", new ShortNode((short) 4717));
        object.set("text", new TextNode("fooBar"));

        root.set("array", array);
        root.set("bigInteger", new BigIntegerNode(new BigInteger("49814812489084910849083218975287")));
        root.set("decimal", new DecimalNode(new BigDecimal("8903278643891443291.418904819078953")));
        root.set("double", new DoubleNode((double) 19041.421980));
        root.set("float", new FloatNode((float) 913.1290));
        root.set("int", new IntNode(1234567));
        root.set("long", new LongNode(1234567890123L));
        root.set("null", NullNode.instance);
        root.set("object", object);
        root.set("short", new ShortNode((short) 123));
        root.set("text", new TextNode("foobarbaz"));

        final ObjectNode rebuilt = ObjectNodeRebuilder.rebuild(root, mapper);
        assertEquals(root, rebuilt);
    }
}
