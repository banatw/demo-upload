package com.repository;

import com.entity.Picture;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PictureRepo extends PagingAndSortingRepository<Picture,Integer> {
}
