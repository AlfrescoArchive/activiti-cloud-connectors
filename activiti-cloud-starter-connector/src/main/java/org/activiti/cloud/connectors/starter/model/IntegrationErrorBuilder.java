/*
 * Copyright 2018 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.cloud.connectors.starter.model;

import java.util.Objects;

import org.activiti.cloud.api.process.model.IntegrationError;
import org.activiti.cloud.api.process.model.IntegrationRequest;
import org.activiti.cloud.api.process.model.impl.IntegrationErrorImpl;
import org.activiti.cloud.connectors.starter.configuration.ConnectorProperties;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

public class IntegrationErrorBuilder {

    private IntegrationRequest requestEvent;
    private IntegrationRequest integrationRequest;
    private ConnectorProperties connectorProperties;
    private IntegrationErrorImpl integrationResult;
    private Exception error;

    private IntegrationErrorBuilder(IntegrationRequest integrationRequest, 
                                    ConnectorProperties connectorProperties) {
        this.integrationRequest = integrationRequest;
        this.connectorProperties = connectorProperties;
    }

    private IntegrationErrorBuilder(IntegrationRequest integrationRequest, 
                                    ConnectorProperties connectorProperties, 
                                    Exception error) {
        this.requestEvent = integrationRequest;
        
        this.integrationResult = new IntegrationErrorImpl(integrationRequest, 
                                                          integrationRequest.getIntegrationContext(), 
                                                          error);
        if(connectorProperties != null) {
            this.integrationResult.setAppName(connectorProperties.getAppName());
            this.integrationResult.setAppVersion(connectorProperties.getAppVersion());
            this.integrationResult.setServiceFullName(connectorProperties.getServiceFullName());
            this.integrationResult.setServiceType(connectorProperties.getServiceType());
            this.integrationResult.setServiceVersion(connectorProperties.getServiceVersion());
            this.integrationResult.setServiceName(connectorProperties.getServiceName());
        }

    }

    public static IntegrationErrorBuilder errorFor(IntegrationRequest integrationRequest, 
                                                   ConnectorProperties connectorProperties) {
        return new IntegrationErrorBuilder(integrationRequest,
                                           connectorProperties);
    }

    public IntegrationErrorBuilder withError(Exception error) {
        this.error = error;
        
        return this;
    }

    public IntegrationError build() {
        Objects.requireNonNull(error);
        
        return new IntegrationErrorImpl(integrationRequest, 
                                        integrationRequest.getIntegrationContext(), 
                                        error);
    }

    public Message<IntegrationError> buildMessage() {
        return getMessageBuilder().build();
    }

    public MessageBuilder<IntegrationError> getMessageBuilder() {
        IntegrationError integrationError = build();
        
        return MessageBuilder.withPayload(integrationError).setHeader("targetService",
                                                                       requestEvent.getServiceFullName());
    }
}
