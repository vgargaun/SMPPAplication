package com.unifun.smpp.repo;

import com.unifun.smpp.model.ServerConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerConfigRepository extends JpaRepository<ServerConfig, Long> {
}
