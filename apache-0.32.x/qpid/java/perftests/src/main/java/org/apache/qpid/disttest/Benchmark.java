/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.qpid.disttest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.qpid.disttest.controller.Controller;
import org.apache.qpid.disttest.controller.ResultsForAllTests;
import org.apache.qpid.disttest.controller.config.Config;
import org.apache.qpid.disttest.controller.config.ConfigReader;
import org.apache.qpid.disttest.controller.config.TestConfig;
import org.apache.qpid.disttest.jms.ControllerJmsDelegate;
import org.apache.qpid.disttest.results.BenchmarkResultWriter;
import org.apache.qpid.disttest.results.ResultsWriter;
import org.apache.qpid.disttest.results.aggregation.Aggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs a performance benchmark using the performance test suite.  The performance test
 * script (Benchmark.js) is normally found on the classpath.
 *
 * Typical command line usage:
 *
 * java -cp ".:./lib/*"  -Dqpid.disttest.duration=1000 -Dqpid.disttest.messageSize=2048  -Dqpid.dest_syntax=BURL
 *     org.apache.qpid.disttest.Benchmark
 *        report-message-totals=false jndi-config=etc/perftests-jndi.properties
 *
 * The classpath must contain the JMS client and the performance test JAR.
 *
 */
public class Benchmark
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Benchmark.class);

    private static final String REPORT_MESSAGE_TOTALS = "report-message-totals";
    private static final String JNDI_CONFIG_PROP = "jndi-config";
    private static final String JNDI_CONFIG_DEFAULT = "jndi.properties";
    private static final String TEST_CONFIG_PROP = "test-config";

    @SuppressWarnings("serial")
    private Map<String,String> _cliOptions = new HashMap<String, String>()
    {{
        put(JNDI_CONFIG_PROP, JNDI_CONFIG_DEFAULT);
        put(TEST_CONFIG_PROP, "/Benchmark.js");
        put(REPORT_MESSAGE_TOTALS, "false");
    }};

    private final Aggregator _aggregator = new Aggregator();

    public static void main(String[] args) throws Exception
    {
        Benchmark benchmark = new Benchmark();
        benchmark.parseArgumentsIntoConfig(args);
        benchmark.doBenchMark();
    }

    private void parseArgumentsIntoConfig(String[] args)
    {
        ArgumentParser argumentParser = new ArgumentParser();
        argumentParser.parseArgumentsIntoConfig(getCliOptions(), args);
        if (_cliOptions.containsKey("-h"))
        {
            printHelp(null);
        }
    }

    private void printHelp(String message)
    {
        if (message != null)
        {
            System.out.println(message);
            System.out.println();
        }
        System.out.println("Usage:");
        System.out.println("java -cp \"<classpath>\" -Dqpid.disttest.duration=<test duration in milliseconds> -Dqpid.disttest.messageSize=<message size in bytes> org.apache.qpid.disttest.Benchmark [-h] [report-message-totals=<false|true>] [jndi-config=<path/to/jndi.properties>] [test-config=<path/to/test/configuration>]");
        System.out.println("    -h                     prints this help");
        System.out.println("    report-message-totals       optional flag to report total payload. Default is false");
        System.out.println("    jndi-config                 path to jndi properties. Default is jndi.properties");
        System.out.println("    test-config                 path to test configuration. If not set, defaults to a built in bench mark test script.  Alternative testscript(s) can be run by setting this option to a directory or file.  If the former, all testscripts within the directory are executed.");
        System.out.println();
        System.out.println("Supported JVM settings:");
        System.out.println("    qpid.disttest.duration      overridden test duration in milliseconds");
        System.out.println("    qpid.disttest.messageSize   overridden message size in bytes");
        System.exit(0);
    }

    private Context getContext()
    {
        String jndiConfig = getJndiConfig();
        if (jndiConfig == null)
        {
            printHelp("JNDI configuration is not provided");
        }

        try
        {
            final Properties properties = loadProperties(jndiConfig);
            return new InitialContext(properties);
        }
        catch (Exception e)
        {
            throw new DistributedTestException("Exception while loading JNDI properties from '" + jndiConfig + "'", e);
        }
    }

    private Properties loadProperties(String jndiConfig) throws IOException, FileNotFoundException
    {
        final Properties properties = new Properties();
        InputStream inStream = getClass().getResourceAsStream(jndiConfig);
        if (inStream == null)
        {
            if (!new File(jndiConfig).exists())
            {
                printHelp("Cannot find " + jndiConfig);
            }
            inStream = new FileInputStream(jndiConfig);
        }

        try
        {
            properties.load(inStream);
        }
        finally
        {
            if (inStream != null)
            {
                inStream.close();
            }
        }
        return properties;
    }

    private void doBenchMark() throws Exception
    {
        Context context = getContext();
        ControllerJmsDelegate jmsDelegate = new ControllerJmsDelegate(context);

        try
        {
            runTests(jmsDelegate, context);
        }
        finally
        {
            jmsDelegate.closeConnections();
        }
    }

    private String getJndiConfig()
    {
        return getCliOptions().get(JNDI_CONFIG_PROP);
    }

    private boolean getReportMessageTotals()
    {
        return Boolean.parseBoolean(getCliOptions().get(REPORT_MESSAGE_TOTALS));
    }

    private Map<String,String> getCliOptions()
    {
        return _cliOptions;
    }
    private void runTests(ControllerJmsDelegate jmsDelegate, Context context)
    {
        Controller controller = new Controller(jmsDelegate, DistributedTestConstants.REGISTRATION_TIMEOUT, DistributedTestConstants.COMMAND_RESPONSE_TIMEOUT);

        String testConfigPath = getCliOptions().get(TEST_CONFIG_PROP);
        Config testConfig = buildTestConfigFrom(testConfigPath);
        createClients(testConfig, context);
        controller.setConfig(testConfig);
        try
        {
            List<ResultsForAllTests> results = new ArrayList<ResultsForAllTests>();

            controller.awaitClientRegistrations();

            ResultsForAllTests testResult = runTest(controller, testConfig, testConfigPath);
            results.add(testResult);
        }
        catch(Exception e)
        {
            LOGGER.error("Problem running test", e);
        }
        finally
        {
            controller.stopAllRegisteredClients();
        }
    }

    private ResultsForAllTests runTest(Controller controller, Config testConfig, String testConfigFile)
    {
        ResultsWriter _resultsWriter = new BenchmarkResultWriter(getReportMessageTotals());
        ResultsForAllTests rawResultsForAllTests = controller.runAllTests();
        ResultsForAllTests resultsForAllTests = _aggregator.aggregateResults(rawResultsForAllTests);

        _resultsWriter.writeResults(resultsForAllTests, testConfigFile);

        return resultsForAllTests;
    }

    private void createClients(Config testConfig, Context context)
    {
        int maxNumberOfClients = testConfig.getTotalNumberOfClients();

        //we must create the required test clients, running in single-jvm mode
        for (int i = 1; i <= maxNumberOfClients; i++)
        {
            ClientRunner clientRunner = new ClientRunner();
            clientRunner.runClients(context);
        }
    }

    private Config buildTestConfigFrom(String testConfigFile)
    {
        ConfigReader configReader = new ConfigReader();
        Config testConfig;
        InputStream configStream = null;
        try
        {
            configStream = getClass().getResourceAsStream(testConfigFile);
            if (configStream != null)
            {
                testConfig = configReader.readConfig(new InputStreamReader(configStream), testConfigFile.endsWith(".js"));
            }
            else
            {
                ConfigFileHelper configFileHelper = new ConfigFileHelper();
                List<String> files = configFileHelper.getTestConfigFiles(testConfigFile);
                List<TestConfig> tests = new ArrayList<TestConfig>();
                for (String file : files)
                {
                    Config config = configReader.getConfigFromFile(file);
                    tests.addAll(config.getTestConfigs());
                }
                testConfig = new Config(tests);
            }
        }
        catch (IOException e)
        {
            throw new DistributedTestException("Exception while loading test config from '"
                    + testConfigFile + "'. Tried both classpath and filesystem", e);
        }
        finally
        {
            if (configStream != null)
            {
                try
                {
                    configStream.close();
                }
                catch (IOException e)
                {
                }
            }
        }

        return testConfig;
    }
}
