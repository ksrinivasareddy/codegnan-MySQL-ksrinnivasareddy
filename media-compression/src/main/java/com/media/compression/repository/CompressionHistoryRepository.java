package com.media.compression.repository;

import com.media.compression.entity.CompressionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompressionHistoryRepository extends JpaRepository<CompressionHistory, Long> {
}
