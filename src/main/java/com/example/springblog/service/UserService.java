package com.example.springblog.service;

import com.example.springblog.model.RoleType;
import com.example.springblog.model.User;
import com.example.springblog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 스프링이 컴포넌트 스캔을 통해 Bean에 등록을 해줌. IoC를 해줌
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Integer 회원가입(User user) {
        try {
            userRepository.save(user);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("UserService :회원가입() : " + e.getMessage());
        }
        return -1;
    }
}
