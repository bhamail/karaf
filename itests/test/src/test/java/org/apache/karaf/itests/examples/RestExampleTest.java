/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.karaf.itests.examples;

import org.apache.karaf.itests.KarafTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static org.junit.Assert.assertEquals;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class RestExampleTest extends KarafTestSupport {

    private void setup() throws Exception {
        addFeaturesRepository("mvn:org.apache.karaf.examples/karaf-rest-example-features/" + System.getProperty("karaf.version") + "/xml");
    }

    private void verify() throws Exception {
        String location = "http://localhost:" + getHttpPort() + "/cxf/booking/";
        executeCommand("booking:add --url " + location + " 1 Foo TST001");

        String output = executeCommand("booking:list --url " + location);
        System.out.println(output);
        assertContains("TST001", output);
    }

    @Test
    public void testWithHttpClient() throws Exception {
        setup();

        installAndAssertFeature("karaf-rest-example-provider");

        installAndAssertFeature("karaf-rest-example-client-http");

        verify();
    }

    @Test
    public void testWithCxfClient() throws Exception {
        setup();

        installAndAssertFeature("karaf-rest-example-provider");

        installAndAssertFeature("karaf-rest-example-client-cxf");

        verify();
    }

    @Test
    public void testWhiteboard() throws Exception {
        setup();

        installAndAssertFeature("karaf-rest-example-whiteboard");

        String output = executeCommand("http:list");
        while (!output.contains("Deployed")) {
            Thread.sleep(500);
            output = executeCommand("http:list");
        }
        System.out.println(output);

        URL url = new URL("http://localhost:" + getHttpPort() + "/booking");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        StringBuilder buffer = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }

        output = buffer.toString();
        assertEquals("[]", output);
    }

}
