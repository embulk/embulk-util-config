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

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Utility methods for the tweaks on {@link org.embulk.util.config.Task}s.
 */
final class Tasks {
    private Tasks() {
        // No instantiation.
    }

    static Optional<String> getFieldNameFromGetter(final String methodName) {
        if (methodName.startsWith("get")) {
            return Optional.of(methodName.substring(3));
        }
        return Optional.empty();
    }

    static Optional<String> getFieldNameFromSetter(final String methodName) {
        if (methodName.startsWith("set")) {
            return Optional.of(methodName.substring(3));
        }
        return Optional.empty();
    }

    static void assertParameters(final Method method, final int expectedNumberOfParameters) {
        final Class<?>[] actualParameterTypes = method.getParameterTypes();

        if (expectedNumberOfParameters != actualParameterTypes.length) {
            throw new IllegalArgumentException(String.format(
                    "Method '%s' is expected to receive %d parameter(s), but actually got %d parameter(s).",
                    method.getName(),
                    expectedNumberOfParameters,
                    actualParameterTypes.length));
        }
    }
}
