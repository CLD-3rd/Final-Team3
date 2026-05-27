package com.matchFit.user.service;

import com.matchFit.user.dto.request.EditMyPageRequest;
import com.matchFit.user.dto.response.MyPageResponse;
import com.matchFit.user.entity.User;
import com.matchFit.user.exception.UserNotFoundException;
import com.matchFit.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;

    public MyPageResponse getMyPage(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        return new MyPageResponse(
                user.getEmail(),
                user.getNickname(),
                user.getTown(),
                user.getAge(),
                user.getSports()
        );
    }

    @Transactional
    public MyPageResponse editMyPage(String email, EditMyPageRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        user.setNickname(req.getNickName());
        user.setAge(req.getAge());
        user.setSports(req.getSports());

        return new MyPageResponse(
                user.getEmail(),
                user.getNickname(),
                user.getTown(),
                user.getAge(),
                user.getSports()
        );
    }
}
