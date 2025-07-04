package com.ito.collector.adapter;

import com.ito.collector.domain.ItsmData;
import com.ito.collector.repository.ItsmViewRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ItsmDbAdapter {

    private final ItsmViewRepository repository;

    public ItsmDbAdapter(ItsmViewRepository repository) {
        this.repository = repository;
    }

    public List<ItsmData> fetch() {
        return repository.findAll();
    }
}
