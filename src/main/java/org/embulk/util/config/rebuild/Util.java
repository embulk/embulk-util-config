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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class Util {
    private Util() {
        // No instantiation.
    }

    static JsonNode rebuildPerType(final Class clazz, final Object node, final ObjectMapper mapper) {
        if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.ArrayNode")) {
            return ArrayNodeRebuilder.rebuild(node, mapper);
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.BigIntegerNode")) {
            return BigIntegerNodeRebuilder.rebuild(node);
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.BinaryNode")) {
            throw new UnsupportedOperationException("com.fasterxml.jackson.databind.node.BinaryNode is not supported.");
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.BooleanNode")) {
            return BooleanNodeRebuilder.rebuild(node);
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.DecimalNode")) {
            return DecimalNodeRebuilder.rebuild(node);
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.DoubleNode")) {
            return DoubleNodeRebuilder.rebuild(node);
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.FloatNode")) {
            return FloatNodeRebuilder.rebuild(node);
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.IntNode")) {
            return IntNodeRebuilder.rebuild(node);
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.LongNode")) {
            return LongNodeRebuilder.rebuild(node);
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.MissingNode")) {
            throw new IllegalStateException("com.fasterxml.jackson.databind.node.MissingNode should not be there.");
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.NullNode")) {
            return NullNodeRebuilder.rebuild(node);
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.ObjectNode")) {
            return ObjectNodeRebuilder.rebuild(node, mapper);
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.POJONode")) {
            throw new UnsupportedOperationException("com.fasterxml.jackson.databind.node.POJONode is not supported.");
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.ShortNode")) {
            return ShortNodeRebuilder.rebuild(node);
        } else if (isSubclassOf(clazz, "com.fasterxml.jackson.databind.node.TextNode")) {
            return TextNodeRebuilder.rebuild(node);
        } else {
            throw new IllegalStateException("Unknown Jackson JsonNode type: " + clazz.toString());
        }
    }

    static Class<?> getClassUnder(
            final Object from,
            final String expectedAncestor,
            final Class<?> callerClass) {
        if (from == null) {
            throw new NullPointerException(callerClass.getSimpleName() + " received null.");
        }

        final Class<?> fromClass = from.getClass();
        if (!isSubclassOf(fromClass, expectedAncestor)) {
            throw new ClassCastException("Expected " + expectedAncestor + " is actually not " + expectedAncestor + ".");
        }
        return fromClass;
    }

    static <T> T getThroughGetter(
            final Object from,
            final String expectedAncestor,
            final String getterMethodName,
            final Class<T> expectedResultClass,
            final Class<?> callerClass) {
        final Class<?> fromClass = getClassUnder(from, expectedAncestor, callerClass);

        final Method getterMethod;
        try {
            getterMethod = fromClass.getMethod(getterMethodName);
        } catch (final NoSuchMethodException ex) {
            throw new NoSuchMethodError(
                    fromClass.getCanonicalName() + " (" + expectedAncestor + ") does not have " + getterMethodName + "().");
        }

        final Object object;
        try {
            object = getterMethod.invoke(from);
        } catch (final InvocationTargetException ex) {
            final Throwable targetException = ex.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            }
            if (targetException instanceof Error) {
                throw (Error) targetException;
            }
            throw new IllegalStateException(
                    fromClass.getCanonicalName() + "#" + getterMethodName + " threw unexpected Exception.", targetException);
        } catch (final IllegalAccessException ex) {
            throw new IllegalStateException(
                    fromClass.getCanonicalName() + "#" + getterMethodName + " is not accessible.", ex);
        }

        if (object == null) {
            throw new NullPointerException(fromClass.getCanonicalName() + "#" + getterMethodName + "() returned null.");
        }
        if (!expectedResultClass.isInstance(object)) {
            throw new ClassCastException(
                    fromClass.getCanonicalName() + "#" + getterMethodName + "() did not return "
                    + expectedResultClass.getCanonicalName() + ", but: "
                    + object.getClass().getCanonicalName());
        }

        return expectedResultClass.cast(object);
    }

    private static boolean isSubclassOf(final Class<?> target, final String expectedAncestor) {
        for (Class<?> klazz = target; klazz != null; klazz = klazz.getSuperclass()) {
            if (klazz.getCanonicalName().equals(expectedAncestor)) {
                return true;
            }
        }
        return false;
    }
}
