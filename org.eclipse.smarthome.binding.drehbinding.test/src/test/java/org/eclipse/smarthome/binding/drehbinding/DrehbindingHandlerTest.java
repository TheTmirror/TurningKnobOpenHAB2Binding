/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.binding.drehbinding;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.eclipse.smarthome.binding.drehbinding.deprecated.RESTServiceImpl;
import org.eclipse.smarthome.binding.drehbinding.handler.DrehbindingHandler;
import org.eclipse.smarthome.binding.drehbinding.internal.REST.implementation.RESTIOServiceImpl;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

/**
 * Test cases for {@link DrehbindingHandler}. The tests provide mocks for supporting entities using Mockito.
 *
 * @author Tristan - Initial contribution
 */
public class DrehbindingHandlerTest extends JavaTest {

    private ThingHandler handler;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Thing thing;

    @Before
    public void setUp() {
        initMocks(this);
        handler = new DrehbindingHandler(thing, new RESTIOServiceImpl(new RESTServiceImpl()));
        handler.setCallback(callback);
    }

    @Test
    public void initializeShouldCallTheCallback() {
        // mock getConfiguration to prevent NPEs
        when(thing.getConfiguration()).thenReturn(new Configuration());

        // we expect the handler#initialize method to call the callback during execution and
        // pass it the thing and a ThingStatusInfo object containing the ThingStatus of the thing.
        handler.initialize();

        // the argument captor will capture the argument of type ThingStatusInfo given to the
        // callback#statusUpdated method.
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);

        // verify the interaction with the callback and capture the ThingStatusInfo argument:
        waitForAssert(() -> {
            verify(callback, times(2)).statusUpdated(eq(thing), statusInfoCaptor.capture());
        });

        // assert that the (temporary) UNKNOWN status was given first:
        assertThat(statusInfoCaptor.getAllValues().get(0).getStatus(), is(ThingStatus.UNKNOWN));

        // assert that ONLINE status was given later:
        assertThat(statusInfoCaptor.getAllValues().get(1).getStatus(), is(ThingStatus.ONLINE));

        // See the documentation at
        // https://www.eclipse.org/smarthome/documentation/development/testing.html#assertions
        // to see when to use Hamcrest assertions (assertThat) or JUnit assertions.
    }

}
