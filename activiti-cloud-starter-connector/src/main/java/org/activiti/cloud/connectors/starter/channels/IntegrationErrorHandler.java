package org.activiti.cloud.connectors.starter.channels;

import org.springframework.messaging.support.ErrorMessage;

public interface IntegrationErrorHandler {

    void handleError(ErrorMessage errorMessage);

} 