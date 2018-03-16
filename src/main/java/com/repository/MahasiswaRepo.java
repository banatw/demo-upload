package com.repository;

import com.entity.Mahasiswa;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MahasiswaRepo extends PagingAndSortingRepository<Mahasiswa,Integer> {
}
