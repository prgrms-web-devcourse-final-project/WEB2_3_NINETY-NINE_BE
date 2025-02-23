package com.example.onculture.domain.event.service;

import com.example.onculture.domain.event.model.PopupStorePost;
import com.example.onculture.domain.event.repository.PopupStorePostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PopupStorePostService {

    private final PopupStorePostRepository repository;

    public PopupStorePostService(PopupStorePostRepository repository) {
        this.repository = repository;
    }

    public List<PopupStorePost> listAll() {
        return repository.findAll();
    }

    public List<PopupStorePost> searchByLocation(String keyword) {
        return repository.findByLocationContaining(keyword);
    }
}
