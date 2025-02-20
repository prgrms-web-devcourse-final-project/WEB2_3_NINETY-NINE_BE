package com.example.onculture.user;

import com.example.onculture.domain.user.controller.UserController;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.domain.user.service.UserService;
import com.example.onculture.global.response.SuccessResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)     // Mockito를 사용하기 위해 JUnit 5에서 MockitoExtension을 사용
public class UserControllerTest {

    @Mock   // 가짜 객체 생성
    private UserService userService;    // UserService를 모킹

    @InjectMocks    // 가짜 환경을 집어 넣는 것
    private UserController userController;      // UserController에 UserService의 목 객체를 주입

    private MockMvc mvc;    // 컨트롤러를 실제로 실행하여 HTTP 요청과 응답을 테스트

    @BeforeEach     // MockMvc를 초기화
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @DisplayName("회원가입 API")
    @Test
    public void testSignup() throws Exception {
        // given - 회원가입 요청 DTO 설정
        SignupRequestDTO request = new SignupRequestDTO();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setNickname("testUser");

        // when - userService.save() 메서드가 호출되었을 때 반환할 값 설정
        // 문제 발생 :
        // SignupRequestDTO 객체가 두 번 이상 생성되어, 서로 다른 객체로 인식되고 있기 때문입니다.
        // 실제 save() 메서드 호출에서 전달된 request 객체의 메모리 주소와, Mockito가 스텁할 때 설정한 request 객체의 메모리 주소가 다르기 때문에 Mockito가 두 호출을 서로 다른 인수로 판단
        // 해결 방안 : any()를 사용하여 해당 메서드 호출 시 어떤 SignupRequestDTO 객체가 들어와도 상관없다고 설정
        when(userService.save(any(SignupRequestDTO.class))).thenReturn(1L);  // any()를 사용하여 매개변수 일치

        // then - 실제 API 호출 (POST /signup)
        MvcResult result = mvc.perform(post("/api/signup")
                        .contentType("application/json")
                        .content("{ \"email\": \"test@example.com\", \"password\": \"password123\", \"nickname\": \"testUser\" }"))
                .andExpect(status().isCreated())  // 상태 코드가 201(Created)이어야 함
                .andExpect(jsonPath("$.message").value("회원가입에 성공하였습니다.")) // 메시지 검증
                .andReturn();
    }
}
