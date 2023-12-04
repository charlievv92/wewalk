package com.spring.boot.controller;

import java.time.DateTimeException;
import java.time.LocalDate;

import javax.validation.Valid;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring.boot.dto.AdminCreateForm;
import com.spring.boot.dto.PageRequestDTO;
import com.spring.boot.dto.PrincipalDetails;
import com.spring.boot.model.SellerRequest;
import com.spring.boot.model.SiteUser;
import com.spring.boot.model.UserRole;
import com.spring.boot.service.AdminService;
import com.spring.boot.service.UserService;

import lombok.RequiredArgsConstructor;

//Admin 페이지용 controller 추가 예정
@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {

	//User 및 Seller 관리를 위한 CRUD 예정
	//오버로딩된 생성자로 의존성 주입(DI)
	private final AdminService adminService;
	private final UserService userService;
	
	//검색 기능이 포함된 리스트
	@RequestMapping("/userList")
	public String list(Model model, @PageableDefault Pageable pageable,
			@ModelAttribute PageRequestDTO pageRequestDTO,
			@AuthenticationPrincipal PrincipalDetails principalDetails) {
		
		String userName = principalDetails.getUsername();
		
	    model.addAttribute("paging", adminService.getList(pageRequestDTO));
	    model.addAttribute("userName", userName);
	    
	    return "user_list2";
	}
	
	@RequestMapping("/sellerRequestList")
	public String sellerRequestList(Model model, @PageableDefault Pageable pageable, 
			@AuthenticationPrincipal PrincipalDetails principalDetails) {
		
		Page<SellerRequest> paging = adminService.getSellerRequestLists(pageable);
		
	    String userName = principalDetails.getUsername();

		model.addAttribute("paging", paging);
		model.addAttribute("userName", userName);
		
		return "seller_request_list";
		
	}
	
	@PreAuthorize("isAuthenticated")
	@GetMapping("/approveSellerRequest/{id}")
	public String approveSellerRequest(@PathVariable("id") Long id) {
	
		//판매자 요청 데이터 받기
		SellerRequest request = userService.getSellerRequest(id);
		
		//받아온 판매자 요청으로 요청자 받기
		SiteUser requestUser =userService.getUser(request.getRequestUser().getId());
		
		//요청 승인 처리하고 db에 저장
		adminService.approveRequest(requestUser, request);
		
		return "redirect:/admin/sellerRequestList";
		
	}
	
	@PreAuthorize("isAuthenticated")
	@GetMapping("/denySellerRequest/{id}")
	public String denySellerRequest(@PathVariable("id") Long id) {
	
		//판매자 요청 데이터 받기
		SellerRequest request = userService.getSellerRequest(id);
		
		//요청 승인 처리하고 db에 저장
		adminService.denyRequest(request);
		
		return "redirect:/admin/sellerRequestList";
		
	}
	
	@GetMapping("/createUser")
	public String createUser(AdminCreateForm adminCreateForm, Model model, 
			@AuthenticationPrincipal PrincipalDetails principalDetails) {
		
		adminCreateForm.setName("테스터");
	    adminCreateForm.setPostcode("00000");
	    adminCreateForm.setAddress("삼원타워");
	    adminCreateForm.setDetailAddress("4층 6강의실");
	    adminCreateForm.setTel("00000000000");
	    adminCreateForm.setSeller(true);
	    adminCreateForm.setBirthYear("2023");
	    adminCreateForm.setBirthMonth("10");
	    adminCreateForm.setBirthDay("31");
	    
	    String userName = principalDetails.getUsername();
	    
	    model.addAttribute("adminCreateForm", adminCreateForm);
		model.addAttribute("userName", userName);
	    
	    return "createUser_form";
		
	}
	
	@PostMapping("/createUser")
	public String createUser(@Valid AdminCreateForm adminCreateForm, BindingResult bindResult) {
		
		//입력값 검증
		if(bindResult.hasErrors()) {
			bindResult.reject("signupFailed", "유효성 검증에 실패했습니다");
			return "createUser_form";
		}
		
		//입력값 DB에 넣으면서 검증(서버사이드)
		try {
			
			//날짜 검증 및 날짜 형식으로 변환
			int year = Integer.parseInt(adminCreateForm.getBirthYear());
		    int month = Integer.parseInt(adminCreateForm.getBirthMonth());
		    int day = Integer.parseInt(adminCreateForm.getBirthDay());

		    LocalDate birthDate = LocalDate.of(year, month, day);
		    
		    UserRole role = UserRole.ADMIN;
		    Integer point = 999999999;
		    Resource resource = new ClassPathResource("static/images/flower-8173829_640.jpg");
		    String picture = "/images/flower-8173829_640.jpg";
//				    String intro = "For test";
			//String password = "test";
			
		    //UserRole을 지정해서 넣어줘야 하고 거기에 추가로 UserCreateForm과 UserService, SiteUser에서의 데이터 입력 순서를 맞춰줘야 함
			adminService.create(role, adminCreateForm.getEmail(), adminCreateForm.getPassword1(), adminCreateForm.getUserName(), 
					adminCreateForm.getName(), birthDate, adminCreateForm.getPostcode(),
					adminCreateForm.getAddress(), adminCreateForm.getDetailAddress(), adminCreateForm.getTel(), adminCreateForm.isSeller(), 
					picture, point);
		
		} catch (DateTimeException e) {
		    // 유효하지 않은 날짜
			e.printStackTrace();
			bindResult.reject("signupFailed", "유효하지 않은 날짜입니다");
			return "createUser_form";
			
		} catch (DataIntegrityViolationException e) {
			
			e.printStackTrace();
			bindResult.reject("signupFailed", "이미 등록된 사용자입니다");
			
			return "createUser_form";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			bindResult.reject("signupFailed", e.getMessage());
			
			return "createUser_form";
			
		}
		return "redirect:/admin/userList";
	}
	
	@PreAuthorize("isAuthenticated")
	@GetMapping("/modifyUser/{id}")
	public String modifyUser(AdminCreateForm adminCreateForm, 
			@PathVariable("id") Long id, Model model) {
		
		SiteUser siteUser = userService.getUser(id);
		String year = Integer.toString(siteUser.getBirthDate().getYear());
		String month = Integer.toString(siteUser.getBirthDate().getMonth().getValue());
		String day = Integer.toString(siteUser.getBirthDate().getDayOfMonth());
		
		adminCreateForm.setEmail(siteUser.getEmail());
		adminCreateForm.setUserName(siteUser.getUserName());
		adminCreateForm.setName(siteUser.getName());
	    adminCreateForm.setPostcode(siteUser.getPostcode());
	    adminCreateForm.setAddress(siteUser.getAddress());
	    adminCreateForm.setDetailAddress(siteUser.getDetailAddress());
	    adminCreateForm.setTel(siteUser.getTel());
	    adminCreateForm.setSeller(siteUser.isSeller());
	    adminCreateForm.setBirthYear(year);
	    adminCreateForm.setBirthMonth(month);
	    adminCreateForm.setBirthDay(day);
	    
	    //url에 따라 양식 변경하려면 add해주면 됨
	    model.addAttribute("currentUrl", "/modifyUser");

		return "createUser_form";
		
	}
	
	@PreAuthorize("isAuthenticated")
	@PostMapping("/modifyUser/{id}")
	public String modifyUser(@Valid AdminCreateForm adminCreateForm, BindingResult bindResult,
			@PathVariable("id") Long id) {
	
		if(bindResult.hasErrors()) {
			return "createUser_form";
		}

		SiteUser siteUser = userService.getUser(id);
		
		//검사한 결과값들을 넣어서 수정
		adminService.modify(siteUser, adminCreateForm.getEmail(), adminCreateForm.getPassword1(), adminCreateForm.getUserName(), 
				adminCreateForm.getName(), adminCreateForm.getPostcode(), adminCreateForm.getAddress(), adminCreateForm.getDetailAddress(), 
				adminCreateForm.getTel(), adminCreateForm.isSeller(), adminCreateForm.getBirthYear(), adminCreateForm.getBirthMonth(), adminCreateForm.getBirthDay());
		
		return "redirect:/admin/userList";
		
	}
	
	//동작은 잘 하지만 페이징 처리에서 isActivated 값이 변경 안됨
	@PreAuthorize("isAuthenticated")
	@GetMapping("/deactivateOrReactivate/{userName}")
	public String deactivateOrReactivate(@PathVariable("userName") String userName) {
	
		//질문글 데이터 받아옴
		SiteUser siteUser = userService.getUserByUserName(userName);
		
		adminService.deactivateOrReactivate(siteUser);
		
		return "redirect:/admin/userList";
		
	}
	
//	@PreAuthorize("isAuthenticated")
//	@PutMapping("/deactivateOrReactivate/{userName}")
//	@ResponseBody
//	public Map<String, Object> deactivateOrReactivate(@PathVariable("userName") String userName) {
//	    Map<String, Object> response = new HashMap<>();
//
//	    SiteUser siteUser = userService.getUserByUserName(userName);
//	    adminService.deactivateOrReactivate(siteUser);
//
//	    response.put("status", "success");
//	    response.put("message", "User activation status changed successfully.");
//
//	    return response;
//	}
	
}
