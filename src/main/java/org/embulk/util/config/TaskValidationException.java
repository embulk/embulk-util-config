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

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.embulk.config.ConfigException;

/**
 * {@link java.lang.Exception} that represents violation(s) with constraints of {@link javax.validation.Validator}.
 */
public class TaskValidationException extends ConfigException {
    <T> TaskValidationException(final Set<ConstraintViolation<T>> violations) {
        super("Configuration violates constraints validated in task definition.",
              new ConstraintViolationException(violations));
    }
}
