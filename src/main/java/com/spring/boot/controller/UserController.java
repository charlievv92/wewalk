package com.spring.boot.controller;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.spring.boot.dto.PrincipalDetails;
import com.spring.boot.dto.SellerRequestForm;
import com.spring.boot.model.AttachmentType;
import com.spring.boot.model.SellerRequest;
import com.spring.boot.model.SiteUser;
import com.spring.boot.model.UserFiles;
import com.spring.boot.service.FileService;
import com.spring.boot.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@RequestMapping("/user/mypage")
public class UserController {
	
	private final UserService userService;
	private final FileService fileService;

	//USER로 접근할 수 있는 페이지 관리할 예정
	@PreAuthorize("isAuthenticated")
	@GetMapping("/requestSeller")
	public String requestSeller(SellerRequestForm sellerRequestForm, 
			@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
		
		String userName = principalDetails.getUsername();
		
		model.addAttribute("userName", userName);
		
		return "request_seller";
		
	}
	
	@PreAuthorize("isAuthenticated")
	@PostMapping("/requestSeller")
	public String requestSeller(@Valid SellerRequestForm sellerRequestForm, BindingResult bindResult, 
			@AuthenticationPrincipal PrincipalDetails principalDetails, RedirectAttributes redirectAttributes) {
		
		//입력값 검증
		if(bindResult.hasErrors()) {
			return "request_seller";
		}
				
		//입력값 DB에 넣으면서 검증(서버사이드)
		try {
			// 현재 로그인한 사용자 정보 가져오기
			SiteUser currentUser = principalDetails.getUser();
			
			// 첨부파일 저장
			Map<AttachmentType, List<MultipartFile>> multipartFiles = sellerRequestForm.getAttachmentTypeListMap();
			List<UserFiles> savedFiles = fileService.saveAttachments(multipartFiles, currentUser);

			LocalDateTime requestTime = LocalDateTime.now();
			boolean isProcessed = false;
			
			// SellerRequest 저장
	        SellerRequest sellerRequest = userService.saveSellerRequest(currentUser, 
	        		sellerRequestForm.getIntro(), requestTime, isProcessed);
			
		} catch (DateTimeException e) {
		    // 유효하지 않은 날짜
			e.printStackTrace();
			bindResult.reject("requestFailed", "유효하지 않은 날짜입니다");
			return "request_seller";
			
		} catch (DataIntegrityViolationException e) {
			
			e.printStackTrace();
			bindResult.reject("requestFailed", "이미 등록된 사용자입니다");
			
			return "request_seller";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			bindResult.reject("requestFailed", e.getMessage());
			
			return "request_seller";
			
		}
		// DB 저장 성공 시 메시지 설정
	    redirectAttributes.addFlashAttribute("message", "판매자 요청이 완료되었습니다");
	    
		return "redirect:/user/mypage";
	}
	
}
