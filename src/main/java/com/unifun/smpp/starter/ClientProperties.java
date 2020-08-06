package com.unifun.smpp.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import javax.validation.constraints.NotNull;

@Data
@ConfigurationProperties("smpp.transmitter")
public class ClientProperties {
    @NotNull(message = "Host can't be null")
    private String host;
    @NotNull(message = "Port can't be null")
    private int port;
    @NotNull(message = "Name can't be null")
    private String name;
    @NotNull(message = "System ID can't be null")
    private String systemId;
    private int waitBindTimeout;
    private int requestTimeout;
}
