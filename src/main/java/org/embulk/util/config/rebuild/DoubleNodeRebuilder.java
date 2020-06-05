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

import com.fasterxml.jackson.databind.node.DoubleNode;

final class DoubleNodeRebuilder {
    private DoubleNodeRebuilder() {
        // No instantiation.
    }

    static DoubleNode rebuild(final Object from) {
        final double doubleValue = Util.getThroughGetter(
                from,
                "com.fasterxml.jackson.databind.node.DoubleNode",
                "doubleValue",
                Double.class,
                DoubleNodeRebuilder.class);

        return new DoubleNode(doubleValue);
    }
}
