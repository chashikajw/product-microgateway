/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2am.micro.gw.tests.common;


import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2am.micro.gw.tests.common.model.API;
import org.wso2am.micro.gw.tests.common.model.ApplicationDTO;
import org.wso2am.micro.gw.tests.common.model.SubscribedApiDTO;
import org.wso2am.micro.gw.tests.context.MgwServerInstance;
import org.wso2am.micro.gw.tests.context.MicroGWTestException;
import org.wso2am.micro.gw.tests.util.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

/**
 * Base test class for mgw test cases.
 */
public class BaseTestCase {

    protected MgwServerInstance microGWServer;
    private static int maxRetryCount=10;
    private static int retryIntervalMillis = 3000;
    private static final Logger log = LoggerFactory.getLogger(BaseTestCase.class);

    /**
     * start the mgw docker environment and mock backend.
     *
     * @throws MicroGWTestException
     * @throws IOException
     */
    public void startMGW() throws MicroGWTestException, IOException, InterruptedException {
        microGWServer = new MgwServerInstance();
        microGWServer.startMGW();
    }

    /**
     * start the mgw docker environment and mock backend.
     *
     * @param confPath external conf.toml file location
     * @throws MicroGWTestException
     * @throws IOException
     */
    public void startMGW(String confPath) throws MicroGWTestException, IOException, InterruptedException {
        microGWServer = new MgwServerInstance(confPath);
        microGWServer.startMGW();
    }

    /**
     * start the mgw docker environment and mock backend.
     *
     * @param confPath   external conf.toml file location
     * @param tlsEnabled true if the tls based backend server is required additionally
     * @throws MicroGWTestException
     * @throws IOException
     */
    public void startMGW(String confPath, boolean tlsEnabled) throws MicroGWTestException, IOException,
            InterruptedException {
        microGWServer = new MgwServerInstance(confPath, tlsEnabled);
        microGWServer.startMGW();
    }

    /**
     * stop the mgw docker environment.
     */
    public void stopMGW() {
        microGWServer.stopMGW();

    }

    public static String getImportAPIServiceURLHttps(String servicePath) throws MalformedURLException {
        return new URL(new URL("https://localhost:" + TestConstant.ADAPTER_IMPORT_API_PORT), servicePath)
                .toString();
    }

    public static String getServiceURLHttps(String servicePath) throws MalformedURLException {
        return new URL(new URL("https://localhost:" + TestConstant.GATEWAY_LISTENER_HTTPS_PORT), servicePath)
                .toString();
    }

    public static String getMockServiceURLHttp(String servicePath) throws MalformedURLException {
        return new URL(new URL("http://localhost:" + TestConstant.MOCK_SERVER_PORT), servicePath).toString();
    }

    public static HttpResponse retryPostRequestUntilDeployed(String requestUrl, Map<String, String> headers,
            String body) throws IOException, InterruptedException {
        HttpResponse response;
        int retryCount = 0;
        do {
            log.info("Trying request with url : " + requestUrl);
            response = HttpsClientRequest.doPost(requestUrl, body, headers);
            retryCount++;
        } while (response != null && response.getResponseCode() == 404 && response.getResponseMessage()
                .contains("Not Found") && retry(retryCount));
        return response;
    }

    public static HttpResponse retryGetRequestUntilDeployed(String requestUrl, Map<String, String> headers)
            throws IOException, InterruptedException {
        HttpResponse response;
        int retryCount = 0;
        do {
            log.info("Trying request with url : " + requestUrl);
            response = HttpsClientRequest.doGet(requestUrl, headers);
            retryCount++;
        } while (response != null && response.getResponseCode() == 404 && response.getResponseMessage()
                .contains("Not Found") && retry(retryCount));
        return response;
    }

    private static boolean retry(int retryCount) throws InterruptedException {
        if(retryCount >= maxRetryCount) {
            log.info("Retrying of the request is finished");
            return false;
        }
        Thread.sleep(retryIntervalMillis);
        return true;
    }

    /**
     * get a jwt token.
     *
     * @param api            api
     * @param applicationDTO application dto
     * @param tier           tier
     * @param keyType        keytype
     * @param validityPeriod validityPeriod
     * @throws Exception
     */
    public static String getJWT(API api, ApplicationDTO applicationDTO, String tier, String keyType,
                                int validityPeriod, String scopes) throws Exception {
        SubscribedApiDTO subscribedApiDTO = new SubscribedApiDTO();
        subscribedApiDTO.setContext(api.getContext() + "/" + api.getVersion());
        subscribedApiDTO.setName(api.getName());
        subscribedApiDTO.setVersion(api.getVersion());
        subscribedApiDTO.setPublisher("admin");

        subscribedApiDTO.setSubscriptionTier(tier);
        subscribedApiDTO.setSubscriberTenantDomain("carbon.super");

        JSONObject jwtTokenInfo = new JSONObject();
        jwtTokenInfo.put("subscribedAPIs", new JSONArray(Arrays.asList(subscribedApiDTO)));
        return TokenUtil.getBasicJWT(applicationDTO, jwtTokenInfo, keyType, validityPeriod, scopes);
    }
}
