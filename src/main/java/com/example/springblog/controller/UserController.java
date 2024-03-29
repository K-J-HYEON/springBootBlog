package com.example.springblog.controller;

import com.example.springblog.config.auth.PrincipalDetail;
import com.example.springblog.model.KakaoProfile;
import com.example.springblog.model.OAuthToken;
import com.example.springblog.model.User;
import com.example.springblog.service.BoardService;
import com.example.springblog.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;


// 인증이 안된 사용자들이 출입할 수 있는 경로
// /auth/** 허용
// 그리고 그냥 주소가 / 이면 index.html 허용
// static 이하에 있는 /js/** , /css/**, /image/** 허용

@Controller
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Value("${root.key}")
    private String rootKey;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private BoardService boardService;


    @GetMapping("/auth/joinForm")
    public String joinForm() {
        return "user/joinForm";
    }


    @GetMapping("/auth/loginForm")
    public String loginForm() {
        return "user/loginForm";
    }


    // 카카오 이 부분 수정
    @GetMapping("/auth/kakao/callback")
    public @ResponseBody String kakaoCallback(String code) { // Data를 리턴해주는 컨트롤러 함수


        // POST 방식으로 key=value 데이터를 요청(카카오쪽으로)
        // Retrofit2
        // OkHttp
        // RestTemplate


        // HttpHeader 오브젝트 생성
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");



        // HttpBody 오브젝트 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", "a1b1baa7f918f916ce1685d64b4bb30f");
        params.add("redirect_uri", "http://localhost:8080/auth/kakao/callback");
        params.add("code", code);


        // HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(params, headers);


        // Http 요청하기 - Post방식으로 - 그리고 response 변수의 응답 받음.
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // Gson Json Simple, ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        OAuthToken oAuthToken = null;
        try {
            oAuthToken = objectMapper.readValue(response.getBody(), OAuthToken.class);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("카카오 엑세스 토큰 : "+oAuthToken.getAccess_token());

        RestTemplate rt2 = new RestTemplate();


        // HttpHeader 오브젝트 생성
        HttpHeaders headers2 = new HttpHeaders();
        headers2.add("Authorization", "Bearer "+ oAuthToken.getAccess_token());
        headers2.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");


        // HttpHeader와 HttpBody를 하나의 오브젝트에 담기
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest2 =
                new HttpEntity<>(headers2);


        // Http 요청하기 - Post방식으로 - 그리고 response 변수의 응답 받음.
        ResponseEntity<String> response2 = rt2.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest2,
                String.class
        );

        ObjectMapper objectMapper2 = new ObjectMapper();
        KakaoProfile kakaoProfile = null;
        try {
            kakaoProfile = objectMapper2.readValue(response2.getBody(), KakaoProfile.class);
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        // User 오브젝트 : username, password, email
        System.out.println("카카오 아이디(번호): " + kakaoProfile.getId());
        System.out.println("카카오 이메일 : "+kakaoProfile.getKakao_account().getEmail());

        System.out.println("블로그서버 유저네임 : "+kakaoProfile.getKakao_account().getEmail()+"_"+kakaoProfile.getId());
        System.out.println("블로그서버 이메일 : "+kakaoProfile.getKakao_account().getEmail());
        // UUID란 중복되지 않는 어떤 특정 값을 만들어 내는 알고리
        UUID garbagePassword = UUID.randomUUID();
        System.out.println("블로그서버 패스워드 : " + rootKey);

        User kakaoUser = User.builder()
                .username(kakaoProfile.getKakao_account().getEmail() + "_" + kakaoProfile.getId())
                .password(rootKey)
                .email(kakaoProfile.getKakao_account().getEmail())
                .oauth("kakao")
                .build();
        System.out.println("1111111111111");
        // 가입자 혹은 비가입자 체크 해서 처리
        System.out.println(kakaoUser.getUsername());
        User originUser = userService.회원찾기(kakaoUser.getUsername());

        System.out.println("2222222222");
        if (originUser.getUsername() == null) {
            System.out.println("기존 회원이 아닙니다.");
            userService.회원가입(kakaoUser);
        }


        // 로그인 처리
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(kakaoUser.getUsername(), kakaoUser.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return "redirect:/";
    }


    @GetMapping("/user/updateForm")
    public String updateForm(Model model, @AuthenticationPrincipal PrincipalDetail principalDetail) {
        model.addAttribute("user", principalDetail.getUser());
        return "/user/updateForm";
    }


    // 작성글 보기
    @GetMapping("/user/myBoard")
    public String index(Model model, @AuthenticationPrincipal PrincipalDetail principalDetail) {
        model.addAttribute("user", principalDetail.getUser());
        model.addAttribute("boards", boardService.작성글목록(principalDetail.getUser()));
        return "/user/myBoard";
    }
}
