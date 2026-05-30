package com.example.URLShortner.services;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.example.URLShortner.dto.RequestUrl;
import com.example.URLShortner.dto.UrlStatsResponse;
import com.example.URLShortner.repository.UrlRepository;
import com.example.URLShortner.entity.Url;
import com.example.URLShortner.exception.UrlInvalidException;
import com.example.URLShortner.exception.UrlNotFoundException;

@Service
public class UrlServiceImpl implements UrlService {
    private final UrlRepository urlRepository;

    public UrlServiceImpl(UrlRepository urlRepository)
    {
        this.urlRepository=urlRepository;
    }

    @Override
    public String shortenUrl(RequestUrl request)
    {
        if(!isValid(request.getLongUrl()))
            throw new UrlInvalidException("Invalid URL");

        Optional<Url> existing=urlRepository.findByLongUrl(request.getLongUrl());

        if(existing.isPresent())
            return existing.get().getShortCode();
            

        String code;
        do{
            code=generateShortCode();
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
    public String getLongUrl(String shortCode)
    {
        Url url=urlRepository.findByShortCode(shortCode)
                            .orElseThrow(()->new UrlNotFoundException("URL not found"));
        url.setClickCount(url.getClickCount()+1);
        urlRepository.save(url);
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

    private String generateShortCode() {

        String chars =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        Random random = new Random();

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }

    private boolean isValid(String longUrl)
    {   
        try {
            URI.create(longUrl).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
