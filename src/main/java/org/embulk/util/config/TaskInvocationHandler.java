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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.embulk.config.TaskSource;

/**
 * An {@link java.lang.reflect.InvocationHandler} implementation to emulate plugin's {@link org.embulk.util.config.Task} behavior dynamically with {@link java.lang.reflect.Proxy}.
 */
final class TaskInvocationHandler implements InvocationHandler {
    TaskInvocationHandler(
            final Class<? extends Task> taskInterface,
            final Map<String, Object> taskBackingObjects,
            final ObjectMapper objectMapper,
            final Validator validator) {
        this.taskInterface = taskInterface;
        this.taskBackingObjects = new ConcurrentHashMap<>(taskBackingObjects);
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        final String methodName = method.getName();

        switch (methodName) {
            case "validate":
                Tasks.assertParameters(method, 0);
                if (this.validator != null) {
                    final Set<ConstraintViolation<Object>> violations = this.validator.validate(proxy);
                    if (!violations.isEmpty()) {
                        throw new TaskValidationException(violations);
                    }
                }
                return proxy;

            case "dump":
            case "toTaskSource":
                Tasks.assertParameters(method, 0);
                return invokeToTaskSource();

            case "toObjectNode":
                Tasks.assertParameters(method, 0);
                return invokeToObjectNode();

            case "toString":
                Tasks.assertParameters(method, 0);
                return invokeToString();

            case "hashCode":
                Tasks.assertParameters(method, 0);
                return invokeHashCode();

            case "equals":
                Tasks.assertParameters(method, 1);
                if (args[0] instanceof Proxy) {
                    final Object otherHandler = Proxy.getInvocationHandler(args[0]);
                    return this.invokeEquals(otherHandler);
                }
                return false;

            default:
                if (methodName.startsWith("get")) {
                    return this.invokeGetter(proxy, method);
                } else if (methodName.startsWith("set")) {
                    Tasks.assertParameters(method, 1);
                    this.invokeSetter(method, args[0]);
                    return this;
                } else {
                    throw new IllegalArgumentException(String.format("Undefined method '%s'", methodName));
                }
        }
    }

    private Object invokeGetter(final Object proxy, final Method method) {
        final String methodName = method.getName();
        final Optional<String> fieldName = Tasks.getFieldNameFromGetter(methodName);
        if (!fieldName.isPresent()) {
            throw new IllegalArgumentException("Tried to run a getter " + methodName + ", but the method name is invalid.");
        }

        if (method.isDefault() && !this.taskBackingObjects.containsKey(fieldName.get())) {
            // If and only if the method has default implementation, and @Config is not annotated there,
            // it is tried to call the default implementation directly without proxying.
            //
            // methodWithDefaultImpl.invoke(proxy) without this hack would cause infinite recursive calls.
            //
            // See hints:
            // https://rmannibucau.wordpress.com/2014/03/27/java-8-default-interface-methods-and-jdk-dynamic-proxies/
            // https://stackoverflow.com/questions/22614746/how-do-i-invoke-java-8-default-methods-reflectively
            //
            // This hack is required to support `org.joda.time.DateTimeZone` in some Tasks, for example
            // TimestampParser.Task and TimestampParser.TimestampColumnOption.
            //
            // TODO: Remove the hack once a cleaner way is found, or Joda-Time is finally removed.
            // https://github.com/embulk/embulk/issues/890
            if (CONSTRUCTOR_MethodHandles_Lookup != null) {
                synchronized (CONSTRUCTOR_MethodHandles_Lookup) {
                    boolean hasSetAccessible = false;
                    try {
                        CONSTRUCTOR_MethodHandles_Lookup.setAccessible(true);
                        hasSetAccessible = true;
                    } catch (SecurityException ex) {
                        // Skip handling default implementation in case of errors.
                    }

                    if (hasSetAccessible) {
                        try {
                            return CONSTRUCTOR_MethodHandles_Lookup
                                    .newInstance(
                                        method.getDeclaringClass(),
                                        MethodHandles.Lookup.PUBLIC
                                                | MethodHandles.Lookup.PRIVATE
                                                | MethodHandles.Lookup.PROTECTED
                                                | MethodHandles.Lookup.PACKAGE)
                                    .unreflectSpecial(method, method.getDeclaringClass())
                                    .bindTo(proxy)
                                    .invokeWithArguments();
                        } catch (Throwable ex) {
                            // Skip handling default implementation in case of errors.
                        } finally {
                            CONSTRUCTOR_MethodHandles_Lookup.setAccessible(false);
                        }
                    }
                }
            }
        }
        Tasks.assertParameters(method, 0);
        return this.taskBackingObjects.get(fieldName.get());
    }

    private void invokeSetter(final Method method, final Object arg) {
        final String methodName = method.getName();
        final Optional<String> fieldName = Tasks.getFieldNameFromSetter(methodName);
        if (!fieldName.isPresent()) {
            throw new IllegalArgumentException("Tried to run a setter " + methodName + ", but the method name is invalid.");
        }

        if (arg == null) {
            this.taskBackingObjects.remove(fieldName.get());
        } else {
            this.taskBackingObjects.put(fieldName.get(), arg);
        }
    }

    private ObjectNode invokeToObjectNode() {
        final ObjectNode objectNode = this.objectMapper.createObjectNode();
        for (final Map.Entry<String, Object> pair : this.taskBackingObjects.entrySet()) {
            objectNode.set(pair.getKey(), this.objectMapper.valueToTree(pair.getValue()));
        }
        return objectNode;
    }

    private TaskSource invokeToTaskSource() {
        return (TaskSource) (new DataSourceImpl(this.invokeToObjectNode(), this.objectMapper));
    }

    private String invokeToString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(this.taskInterface.getName());
        builder.append(this.taskBackingObjects);
        return builder.toString();
    }

    private int invokeHashCode() {
        return this.taskBackingObjects.hashCode();
    }

    private boolean invokeEquals(final Object otherObject) {
        if (!(otherObject instanceof TaskInvocationHandler)) {
            return false;
        }
        final TaskInvocationHandler other = (TaskInvocationHandler) otherObject;
        return this.taskBackingObjects.equals(other.taskBackingObjects);
    }

    static {
        Constructor<MethodHandles.Lookup> constructorMethodHandlesLookupTemporary = null;
        try {
            constructorMethodHandlesLookupTemporary =
                    MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
        } catch (final NoSuchMethodException | SecurityException ex) {
            constructorMethodHandlesLookupTemporary = null;
        } finally {
            CONSTRUCTOR_MethodHandles_Lookup = constructorMethodHandlesLookupTemporary;
        }
    }

    private final Class<? extends Task> taskInterface;

    /**
     * Objects backing behind a {@link Task} that are wrapped by getter (and setter) methods.
     */
    private final ConcurrentHashMap<String, Object> taskBackingObjects;

    private final ObjectMapper objectMapper;
    private final Validator validator;

    private static final Constructor<MethodHandles.Lookup> CONSTRUCTOR_MethodHandles_Lookup;
}
