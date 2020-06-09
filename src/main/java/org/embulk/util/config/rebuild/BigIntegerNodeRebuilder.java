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

import com.fasterxml.jackson.databind.node.BigIntegerNode;
import java.math.BigInteger;

final class BigIntegerNodeRebuilder {
    private BigIntegerNodeRebuilder() {
        // No instantiation.
    }

    static BigIntegerNode rebuild(final Object from) {
        final BigInteger bigIntegerValue = Util.getThroughGetter(
                from,
                "com.fasterxml.jackson.databind.node.BigIntegerNode",
                "bigIntegerValue",
                BigInteger.class,
                BigIntegerNodeRebuilder.class);

        return new BigIntegerNode(bigIntegerValue);
    }
}
