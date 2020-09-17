package com.unifun.smpp.model;

import lombok.*;

import javax.persistence.*;

@RequiredArgsConstructor
@Setter
@Getter
@Entity
public class ServerConfig {

    @Id
    private long id;
    private int port;
    private String host;
    private String name;
    private  String password;
    private String systemId;
    private String bindType;
    private String TON;
    private String NPI;
    private int tpc;
}
