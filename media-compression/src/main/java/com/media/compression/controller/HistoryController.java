package com.media.compression.controller;

import com.media.compression.entity.CompressionHistory;
import com.media.compression.repository.CompressionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history/api")
@CrossOrigin("*")
public class HistoryController {

    @Autowired
    private CompressionHistoryRepository historyRepo;

    @GetMapping
    public List<CompressionHistory> getAll() {
        return historyRepo.findAll();
    }
}
