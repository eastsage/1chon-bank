package com.woowahanbank.backend.domain.customer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.woowahanbank.backend.domain.customer.dto.LoanerDto;
import com.woowahanbank.backend.domain.customer.service.CustomerService;
import com.woowahanbank.backend.global.auth.security.CustomUserDetails;
import com.woowahanbank.backend.global.response.BaseResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@Api(tags = {"Loaner API"})
@RequestMapping("/api/loaner")
public class LoanerController {
	private final CustomerService<LoanerDto> customerService;

	@ApiOperation(value = "대출 상품 가입 신청")
	@ApiResponse(code = 200, message = "대출 상품 가입 신청 성공")
	@PostMapping
	public ResponseEntity<?> apply(@AuthenticationPrincipal CustomUserDetails customUser,
		@RequestBody LoanerDto loanerDto) {
		loanerDto.setUserId(customUser.getUser().getId());
		try {
			customerService.apply(loanerDto);
			return BaseResponse.ok(HttpStatus.OK, "대출 상품 가입 신청 성공");
		} catch (Exception e) {
			return BaseResponse.fail("대출 상품 가입 신청 실패", 400);
		}
	}

	@ApiOperation(value = "모든 대출 상품 비허가 고객 조회")
	@ApiResponse(code = 200, message = "우리 가족 모든 대출 상품 비허가 고객 목록")
	@GetMapping("/disallowList")
	public ResponseEntity<?> getDisallow(@AuthenticationPrincipal CustomUserDetails customUser) {
		List<LoanerDto> disallowList = customerService.getDisallow(customUser);
		return BaseResponse.okWithData(HttpStatus.OK, "우리 가족 모든 대출 상품 비허가 고객 목록", disallowList);
	}

	@ApiOperation(value = "특정 대출 상품 비허가 고객 조회")
	@ApiImplicitParam(name = "productId", value = "상품Id", required = true, dataType = "Long", paramType = "path")
	@ApiResponse(code = 200, message = "특정 대출 상품 비허가 고객")
	@GetMapping("/disallowCustommer/{productId}")
	public ResponseEntity<?> getDisallow(@PathVariable Long productId) {
		List<LoanerDto> disallowList = customerService.getDisallowProducts(productId);
		return BaseResponse.okWithData(HttpStatus.OK, "특정 대출 상품 비허가 고객", disallowList);
	}

	@ApiOperation(value = "비허가 대출 고객 승인")
	@ApiImplicitParam(name = "loanerId", value = "대출 고객 Id", required = true, dataType = "Long", paramType = "path")
	@ApiResponse(code = 200, message = "대출 고객 승인")
	@PutMapping("/allow/{loanerId}")
	public ResponseEntity<?> allowProduct(@AuthenticationPrincipal CustomUserDetails customUser,
		@PathVariable Long loanerId) {
		if (customUser.getUser().getRoles().equals("ROLE_CHILD"))
			return BaseResponse.fail("자녀는 허락 할 수 없습니다.", 400);
		try {
			customerService.allow(loanerId, customUser.getUser());
			return BaseResponse.ok(HttpStatus.OK, "대출 고객 승인");
		} catch (Exception e) {
			return BaseResponse.fail("대출 고객 승인 실패", 400);
		}
	}

	@ApiOperation(value = "비허가 대출 고객 거절")
	@ApiImplicitParam(name = "loanerId", value = "대출 고객 Id", required = true, dataType = "Long", paramType = "path")
	@ApiResponse(code = 200, message = "대출 고객 거절")
	@PutMapping("/refuse/{loanerId}")
	public ResponseEntity<?> refuseProduct(@AuthenticationPrincipal CustomUserDetails customUser,
		@PathVariable Long loanerId) {
		if (customUser.getUser().getRoles().equals("ROLE_CHILD"))
			return BaseResponse.fail("자녀는 허락 할 수 없습니다.", 400);
		try {
			customerService.refuse(loanerId, customUser.getUser());
			return BaseResponse.ok(HttpStatus.OK, "대출 고객 거절");
		} catch (Exception e) {
			return BaseResponse.fail("대출 고객 거절 실패", 400);
		}
	}

	@ApiOperation(value = "닉네임으로 대출 상품 조회")
	@ApiImplicitParam(name = "nickname", value = "닉네임", required = true, dataType = "String", paramType = "path")
	@ApiResponse(code = 200, message = "~의 대출 상품 목록")
	@GetMapping("/Custommer/{nickname}")
	public ResponseEntity<?> getDisallow(@PathVariable String nickname) {
		List<LoanerDto> disallowList = customerService.getProductsByNickname(nickname);
		return BaseResponse.okWithData(HttpStatus.OK, nickname + "의 대출 상품 목록", disallowList);
	}

}
