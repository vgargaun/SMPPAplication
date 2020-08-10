package com.unifun.smpp.repo;

import com.unifun.smpp.model.MessageInput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<MessageInput, Long> {
}
