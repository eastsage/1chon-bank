package com.woowahanbank.backend.global.auth.controller;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.woowahanbank.backend.domain.user.domain.User;
import com.woowahanbank.backend.domain.user.dto.JoinDto;
import com.woowahanbank.backend.domain.user.repository.UserRepository;
import com.woowahanbank.backend.domain.user.service.UserService;
import com.woowahanbank.backend.global.auth.jwt.JwtPayloadDto;
import com.woowahanbank.backend.global.response.BaseResponse;
import com.woowahanbank.backend.global.util.JwtTokenUtil;
import com.woowahanbank.backend.global.util.OidcUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/users/login/kakao")
public class KakaoOidcController {
	private final OidcUtil oidcUtil;
	private final UserService userService;
	private final RedisTemplate<String, String> template;
	private final UserRepository userRepository;

	@PostMapping
	public ResponseEntity<?> oidcLogin(@RequestHeader("Authorization") String idToken) {
		JwtPayloadDto memberData;
		try {
			memberData = oidcUtil.decodeIdToken(idToken, "kakao");
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		log.info("kakao login data = {}", memberData);
		log.info("kakao picture = {}", memberData.getPicture());
		Optional<User> user = userRepository.findByUserId(memberData.getSub());
		JoinDto newMember;
		if (user.isEmpty()) {
			newMember = JoinDto.builder()
				.userId(memberData.getSub())
				// .photo(memberData.getPicture())
				.score(500)
				.money(0)
				.build();
			userService.userRegister(newMember);
			user = userRepository.findByUserId(memberData.getSub());
		}
		// if (optionalMember.get().getDelete()) {
		// 	return BaseResponse.fail("login fail", 400);
		// }
		String userId = user.get().getUserId();

		// 유효한 패스워드가 맞는 경우, 로그인 성공으로 응답.(액세스 토큰을 포함하여 응답값 전달)
		Map<String, String> tokens = new LinkedHashMap<>();
		tokens.put("access-token", JwtTokenUtil.getAccessToken(userId));
		tokens.put("refresh-token", JwtTokenUtil.getRefreshToken(userId));

		log.info("access-token = {}", tokens.get("access-token"));
		//Redis에 20일 동안 저장
		template.opsForValue().set("refresh " + userId, tokens.get("refresh-token"), Duration.ofDays(20));

		return BaseResponse.okWithData(HttpStatus.OK, "login Success", tokens);
	}
}
