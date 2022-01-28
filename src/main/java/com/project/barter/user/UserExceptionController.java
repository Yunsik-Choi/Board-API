package com.project.barter.user;

import com.project.barter.user.exception.CustomBindingException;
import com.project.barter.user.exception.LoginIdAlreadyExistsException;
import com.project.barter.user.exception.UserLoginUnavailableException;
import com.project.barter.user.exception.UserNotExistsException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.project.barter.user")
public class UserExceptionController {

    @ExceptionHandler(CustomBindingException.class)
    public ResponseEntity bindingException(CustomBindingException exception){
        BindingResult bindingResult = exception.getBindingResult();
        return ResponseEntity.badRequest().body(bindingResult.getAllErrors()
                .stream().map(i -> i.getDefaultMessage()).toArray());
    }

    @ExceptionHandler(UserNotExistsException.class)
    public ResponseEntity notExistsUser(UserNotExistsException exception){
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(LoginIdAlreadyExistsException.class)
    public ResponseEntity loginIdAlreadyExists(LoginIdAlreadyExistsException exception){
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(UserLoginUnavailableException.class)
    public ResponseEntity userLoginUnavailable(){
        return ResponseEntity.badRequest().build();
    }

}
