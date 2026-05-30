package com.example.URLShortner.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.URLShortner.entity.Url;

public interface UrlRepository extends JpaRepository<Url,Long> {
    Optional<Url> findByShortCode(String shortCode);
    
    Optional<Url> findByLongUrl(String longUrl);

    boolean existsByShortCode(String shortCode);
}
