package com.project.barter.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.barter.user.dto.UserLogin;
import com.project.barter.user.dto.UserPost;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/join")
    public ResponseEntity join(@Validated @RequestBody UserPost userPost){
        if(userRepository.findUserByUserId(userPost.getUserId()).isPresent()){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().body(userRepository.save(objectMapper.convertValue(userPost,User.class)));
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody UserLogin userLogin, HttpServletRequest request){
        Optional<User> loginRequestUser =
                userRepository.findUserByUserIdAndPassword(userLogin.getUserId(), userLogin.getPassword());
        if(loginRequestUser.isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        HttpSession session = request.getSession();
        session.setAttribute("loginUser", userLogin.getUserId());
        return ResponseEntity.ok().body(userRepository.findById(loginRequestUser.get().getId()));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity findById(@PathVariable Long id){
        Optional<User> findUser = userRepository.findById(id);
        if(findUser.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(findUser.get());
    }
}
