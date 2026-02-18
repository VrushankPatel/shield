package com.shield.module.newsletter.repository;

import com.shield.module.newsletter.entity.NewsletterEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterRepository extends JpaRepository<NewsletterEntity, UUID> {

    Page<NewsletterEntity> findAllByDeletedFalse(Pageable pageable);

    Optional<NewsletterEntity> findByIdAndDeletedFalse(UUID id);

    Page<NewsletterEntity> findAllByYearAndDeletedFalse(int year, Pageable pageable);
}
