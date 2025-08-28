package com.solsolhey.campus;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampusService {

    private final CampusRepository campusRepository;

    public List<Campus> getCampusList() {
        return campusRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }
}
