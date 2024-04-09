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

package org.embulk.util.config.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import javax.validation.Validation;
import javax.validation.constraints.Max;
import org.apache.bval.jsr303.ApacheValidationProvider;
import org.embulk.test.EmbulkTestRuntime;
import org.embulk.util.config.Config;
import org.embulk.util.config.ConfigDefault;
import org.embulk.util.config.ConfigMapper;
import org.embulk.util.config.ConfigMapperFactory;
import org.embulk.util.config.Task;
import org.embulk.util.config.TaskMapper;
import org.embulk.util.config.TaskValidationException;
import org.junit.Rule;
import org.junit.Test;

public class TestConfigMapper {
    @Rule
    public EmbulkTestRuntime runtime = new EmbulkTestRuntime();

    @Test
    public void testSimple() {
        final ConfigMapperFactory configMapperFactory = ConfigMapperFactory.withDefault();

        final org.embulk.config.ConfigSource legacyConfig = org.embulk.spi.Exec.newConfigSource();
        legacyConfig.set("boolean", true);
        legacyConfig.set("int", 3);
        legacyConfig.set("double", 0.2);
        legacyConfig.set("long", Long.MAX_VALUE);
        legacyConfig.set("string", "sf");
        legacyConfig.set("optional2", "baz");
        legacyConfig.set("unexpected", "something");

        final ConfigMapper configMapper = configMapperFactory.createConfigMapper();
        final TypeFieldsTask taskFromConfig = configMapper.map(legacyConfig, TypeFieldsTask.class);
        assertEquals(true, taskFromConfig.getBoolean());
        assertEquals(3, taskFromConfig.getInt());
        assertEquals(0.2, taskFromConfig.getDouble(), 0.00001);
        assertEquals(Long.MAX_VALUE, taskFromConfig.getLong());
        assertEquals("sf", taskFromConfig.getString());
        assertFalse(taskFromConfig.getOptional1().isPresent());
        assertTrue(taskFromConfig.getOptional2().isPresent());
        assertEquals("baz", taskFromConfig.getOptional2().get());
        assertEquals(null, taskFromConfig.getExtra());

        taskFromConfig.setExtra("foobar");

        final org.embulk.config.TaskSource newTask = taskFromConfig.toTaskSource();
        System.out.println(newTask);

        final TaskMapper taskMapper = configMapperFactory.createTaskMapper();
        final TypeFieldsTask taskFromTask = taskMapper.map(newTask, TypeFieldsTask.class);
        assertEquals(true, taskFromTask.getBoolean());
        assertEquals(3, taskFromTask.getInt());
        assertEquals(0.2, taskFromTask.getDouble(), 0.00001);
        assertEquals(Long.MAX_VALUE, taskFromTask.getLong());
        assertEquals("sf", taskFromTask.getString());
        assertFalse(taskFromTask.getOptional1().isPresent());
        assertTrue(taskFromTask.getOptional2().isPresent());
        assertEquals("baz", taskFromTask.getOptional2().get());
        assertEquals("foobar", taskFromTask.getExtra());
    }

    @Test
    public void testDuplication() {
        final ConfigMapperFactory configMapperFactory = ConfigMapperFactory.withDefault();

        final org.embulk.config.ConfigSource legacyConfig = org.embulk.spi.Exec.newConfigSource();
        legacyConfig.set("duplicated_number", "12049");

        final ConfigMapper configMapper = configMapperFactory.createConfigMapper();
        final DuplicatedTask taskFromConfig = configMapper.map(legacyConfig, DuplicatedTask.class);
        assertEquals(12049, taskFromConfig.getDuplicatedInInteger());
        assertEquals("12049", taskFromConfig.getDuplicatedInString());
        assertEquals(12049.0, taskFromConfig.getDuplicatedInDouble(), 0.00001);

        final org.embulk.config.TaskSource newTask = taskFromConfig.toTaskSource();
        System.out.println(newTask);

        final TaskMapper taskMapper = configMapperFactory.createTaskMapper();
        final DuplicatedTask taskFromTask = taskMapper.map(newTask, DuplicatedTask.class);
        assertEquals(12049, taskFromTask.getDuplicatedInInteger());
        assertEquals("12049", taskFromTask.getDuplicatedInString());
        assertEquals(12049.0, taskFromTask.getDuplicatedInDouble(), 0.00001);
    }

    @Test
    public void testValidation() {
        // Here we do not attempt to test all of the JSR 303's validation rules.
        // This is only to make sure the violation details will be propagated properly
        // downstream to the caller side through TaskValidationException's message.
        final ConfigMapper configMapper = ConfigMapperFactory.builder()
                .addDefaultModules()
                .withValidator(Validation.byProvider(ApacheValidationProvider.class)
                        .configure()
                        .buildValidatorFactory()
                        .getValidator())
                .build()
                .createConfigMapper();
        final org.embulk.config.ConfigSource maxCapViolatedConfig =
                org.embulk.spi.Exec.newConfigSource().set("number", 101);

        assertTrue("The exception has to at least contains the property name, the constraint and actual values.",
                assertThrows(TaskValidationException.class,
                        () -> configMapper.map(maxCapViolatedConfig, ToBeValidatedTask.class)
                ).getMessage().matches("(?i).*number.*100.*101.*"));
    }

    private static interface TypeFieldsTask extends Task {
        @Config("boolean")
        boolean getBoolean();

        @Config("double")
        double getDouble();

        @Config("int")
        int getInt();

        @Config("long")
        long getLong();

        @Config("string")
        String getString();

        @Config("optional1")
        @ConfigDefault("null")
        Optional<String> getOptional1();

        @Config("optional2")
        @ConfigDefault("null")
        Optional<String> getOptional2();

        String getExtra();

        void setExtra(String extra);
    }

    private static interface DuplicationParentTask extends Task {
        @Config("duplicated_number")
        public int getDuplicatedInInteger();
    }

    private static interface DuplicatedTask extends DuplicationParentTask {
        @Config("duplicated_number")
        public String getDuplicatedInString();

        @Config("duplicated_number")
        public double getDuplicatedInDouble();
    }

    private interface ToBeValidatedTask extends Task {
        @Config("number")
        @Max(100)
        Integer getNumber();
    }
}
