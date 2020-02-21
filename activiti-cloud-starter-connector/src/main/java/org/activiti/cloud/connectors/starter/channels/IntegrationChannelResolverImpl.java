package org.activiti.cloud.connectors.starter.channels;

import org.activiti.cloud.api.process.model.IntegrationRequest;
import org.activiti.cloud.connectors.starter.configuration.ConnectorProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binding.BinderAwareChannelResolver;
import org.springframework.messaging.MessageChannel;

public class IntegrationChannelResolverImpl implements IntegrationChannelResolver {

    @Value("${ACT_INT_RES_CONSUMER:}")
    private String resultDestinationOverride;

    private final BinderAwareChannelResolver resolver;

    private final ConnectorProperties connectorProperties;

    @Autowired
    public IntegrationChannelResolverImpl(BinderAwareChannelResolver resolver, 
                                          ConnectorProperties connectorProperties) {
        this.resolver = resolver;
        this.connectorProperties = connectorProperties;
    }
    
    @Override
    public MessageChannel resolveDestination(IntegrationRequest event) {
        String destination = (resultDestinationOverride == null || resultDestinationOverride.isEmpty())
                ? "integrationResult" + connectorProperties.getMqDestinationSeparator() + event.getServiceFullName() : resultDestinationOverride;
                
        return resolver.resolveDestination(destination);
    }
    

}
