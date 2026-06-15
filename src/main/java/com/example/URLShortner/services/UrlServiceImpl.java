package com.example.URLShortner.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import com.example.URLShortner.dto.RequestUrl;
import com.example.URLShortner.dto.UrlStatsResponse;
import com.example.URLShortner.repository.UrlRepository;
import com.example.URLShortner.entity.Url;
import com.example.URLShortner.exception.UrlInvalidException;
import com.example.URLShortner.exception.UrlNotFoundException;
import com.example.URLShortner.utils.Base62Encoder;
import com.example.URLShortner.utils.UrlValidator;

@Service
public class UrlServiceImpl implements UrlService {
    private final UrlRepository urlRepository;
    private final Base62Encoder base62Encoder;
    private final UrlValidator  urlValidator;

    public UrlServiceImpl(UrlRepository urlRepository,Base62Encoder base62Encoder,UrlValidator urlValidator)
    {
        this.urlRepository=urlRepository;
        this.base62Encoder=base62Encoder;
        this.urlValidator=urlValidator;
    }

    @Override
    public String shortenUrl(RequestUrl request)
    {
        if(!urlValidator.isValid(request.getLongUrl()))
            throw new UrlInvalidException("Invalid URL");

        Optional<Url> existing=urlRepository.findByLongUrl(request.getLongUrl());

        if(existing.isPresent())
            return existing.get().getShortCode();
            

        String code;
        do{
            code=base62Encoder.generate();
        }while(urlRepository.existsByShortCode(code));

        Url url=new Url();
        url.setLongUrl(request.getLongUrl());
        url.setShortCode(code);
        url.setCreatedAt(LocalDateTime.now());
        url.setClickCount(0L);
        url.setExpiresAt(LocalDateTime.now().plusDays(30));

        urlRepository.save(url);
        return code;
    }

    @Override
    @Cacheable(value = "urls", key = "#shortCode")    //value->cache bucket name(here urls),key specifies how to identify a cached entry within that cache.(If multiple argument tell how to combine them to form cache key)
    public String getLongUrl(String shortCode)
    {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException("URL not found"));
        return url.getLongUrl();
    }

    @Override
    public UrlStatsResponse getStats(String code)
    {
        Url url=urlRepository.findByShortCode(code)
                            .orElseThrow(()->new UrlNotFoundException("URL not found"));
        return new UrlStatsResponse(
            url.getShortCode(),
            url.getLongUrl(),
            url.getClickCount(),
            url.getCreatedAt(),
            url.getExpiresAt()
        );
    }

    @Override
    public void deleteUrl(String shortCode) {
        urlRepository.deleteByShortCode(shortCode);
    }

    @Override
    public void incrementClickCount(String shortCode) {
        urlRepository.incrementClickCount(shortCode); 
    }
}