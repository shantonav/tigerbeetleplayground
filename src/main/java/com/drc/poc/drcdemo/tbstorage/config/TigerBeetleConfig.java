package com.drc.poc.drcdemo.tbstorage.config;

import com.tigerbeetle.Client;
import com.tigerbeetle.UInt128;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class TigerBeetleConfig {

    @Bean
    public Client tigerbeetleClient(@Value("${dcrdemo.tigerbeetle.replica.cluster-id:0}") long clusterId,
                                    @Value("${dcrdemo.tigerbeetle.replica.addresses:3000}") String[] replicaAddresses) {
        log.info("Configuring tigerbeetle replica cluster: {}, addresses {}", clusterId, String.join(", ", replicaAddresses));

        return new Client(UInt128.asBytes(clusterId), replicaAddresses);
    }
}


