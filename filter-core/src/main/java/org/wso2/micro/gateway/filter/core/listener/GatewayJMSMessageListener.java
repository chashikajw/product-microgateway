/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.micro.gateway.filter.core.listener;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.micro.gateway.filter.core.common.ReferenceHolder;
import org.wso2.micro.gateway.filter.core.constants.APIConstants;
import org.wso2.micro.gateway.filter.core.constants.APIConstants.EventType;
import org.wso2.micro.gateway.filter.core.constants.APIConstants.PolicyType;
import org.wso2.micro.gateway.filter.core.constants.APIStatus;
import org.wso2.micro.gateway.filter.core.listener.events.*;

import javax.jms.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class GatewayJMSMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(GatewayJMSMessageListener.class);
    private final boolean debugEnabled = log.isDebugEnabled();

    public void onMessage(Message message) {

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                Topic jmsDestination = (Topic) message.getJMSDestination();
                if (message instanceof MapMessage) {
                    MapMessage mapMessage = (MapMessage) message;
                    Map<String, Object> map = new HashMap<String, Object>();
                    Enumeration enumeration = mapMessage.getMapNames();
                    while (enumeration.hasMoreElements()) {
                        String key = (String) enumeration.nextElement();
                        map.put(key, mapMessage.getObject(key));
                    }
                    if (APIConstants.TopicNames.TOPIC_NOTIFICATION.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (map.get(APIConstants.EVENT_TYPE) != null) {
                            /*
                             * This message contains notification
                             * eventType - type of the event
                             * timestamp - system time of the event published
                             * event - event data
                             */
                            if (debugEnabled) {
                                log.debug("Event received from the topic of " + jmsDestination.getTopicName());
                            }
                            handleNotificationMessage((String) map.get(APIConstants.EVENT_TYPE),
                                    (Long) map.get(APIConstants.EVENT_TIMESTAMP),
                                    (String) map.get(APIConstants.EVENT_PAYLOAD));
                        }
                    }

                } else {
                    log.warn("Event dropped due to unsupported message type " + message.getClass());
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException e) {
            log.error("JMSException occurred when processing the received message ", e);
        }
    }

    private void handleNotificationMessage(String eventType, long timestamp, String encodedEvent) {

        byte[] eventDecoded = Base64.decodeBase64(encodedEvent.getBytes());
        String eventJson = new String(eventDecoded);

        if (EventType.APPLICATION_CREATE.toString().equals(eventType)
                || EventType.APPLICATION_UPDATE.toString().equals(eventType)) {
            ApplicationEvent event = new Gson().fromJson(eventJson, ApplicationEvent.class);
            ReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateApplication(event);
        } else if (EventType.SUBSCRIPTIONS_CREATE.toString().equals(eventType)
                || EventType.SUBSCRIPTIONS_UPDATE.toString().equals(eventType)) {
            SubscriptionEvent event = new Gson().fromJson(eventJson, SubscriptionEvent.class);
            ReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateSubscription(event);
        } else if (EventType.API_UPDATE.toString().equals(eventType)) {
            APIEvent event = new Gson().fromJson(eventJson, APIEvent.class);
             ReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateAPI(event);
        } else if (EventType.API_LIFECYCLE_CHANGE.toString().equals(eventType)) {
            APIEvent event = new Gson().fromJson(eventJson, APIEvent.class);
            if (APIStatus.CREATED.toString().equals(event.getApiStatus())
                    || APIStatus.RETIRED.toString().equals(event.getApiStatus())) {
                 ReferenceHolder.getInstance().getKeyManagerDataService().removeAPI(event);
            } else {
                 ReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateAPI(event);
            }
        } else if (EventType.APPLICATION_REGISTRATION_CREATE.toString().equals(eventType)) {
            ApplicationRegistrationEvent event = new Gson().fromJson(eventJson, ApplicationRegistrationEvent.class);
             ReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateApplicationKeyMapping(event);
        } else if (EventType.API_DELETE.toString().equals(eventType)) {
            APIEvent event = new Gson().fromJson(eventJson, APIEvent.class);
             ReferenceHolder.getInstance().getKeyManagerDataService().removeAPI(event);
        } else if (EventType.SUBSCRIPTIONS_DELETE.toString().equals(eventType)) {
            SubscriptionEvent event = new Gson().fromJson(eventJson, SubscriptionEvent.class);
             ReferenceHolder.getInstance().getKeyManagerDataService().removeSubscription(event);
        } else if (EventType.APPLICATION_DELETE.toString().equals(eventType)) {
            ApplicationEvent event = new Gson().fromJson(eventJson, ApplicationEvent.class);
             ReferenceHolder.getInstance().getKeyManagerDataService().removeApplication(event);
        } else {
            PolicyEvent event = new Gson().fromJson(eventJson, PolicyEvent.class);
            boolean updatePolicy = false;
            boolean deletePolicy = false;
            if (EventType.POLICY_CREATE.toString().equals(eventType)
                    || EventType.POLICY_UPDATE.toString().equals(eventType)) {
                updatePolicy = true;
            } else if (EventType.POLICY_DELETE.toString().equals(eventType)) {
                deletePolicy = true;
            }
            if (event.getPolicyType() == APIConstants.PolicyType.API) {
                APIPolicyEvent policyEvent = new Gson().fromJson(eventJson, APIPolicyEvent.class);
                if (updatePolicy) {
                     ReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateAPIPolicy(policyEvent);
                } else if (deletePolicy) {
                     ReferenceHolder.getInstance().getKeyManagerDataService().removeAPIPolicy(policyEvent);
                }
            } else if (event.getPolicyType() == PolicyType.SUBSCRIPTION) {
                SubscriptionPolicyEvent policyEvent = new Gson().fromJson(eventJson, SubscriptionPolicyEvent.class);
                if (updatePolicy) {
                     ReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateSubscriptionPolicy(policyEvent);
                } else if (deletePolicy) {
                     ReferenceHolder.getInstance().getKeyManagerDataService().removeSubscriptionPolicy(policyEvent);
                }
            } else if (event.getPolicyType() == PolicyType.APPLICATION) {
                ApplicationPolicyEvent policyEvent = new Gson().fromJson(eventJson, ApplicationPolicyEvent.class);
                if (updatePolicy) {
                     ReferenceHolder.getInstance().getKeyManagerDataService().addOrUpdateApplicationPolicy(policyEvent);
                } else if (deletePolicy) {
                     ReferenceHolder.getInstance().getKeyManagerDataService().removeApplicationPolicy(policyEvent);
                }
            }
        }
    }
}
