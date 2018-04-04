package com.repository;

import com.entity.User;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends PagingAndSortingRepository<User,String> {
}
