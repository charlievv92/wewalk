package com.spring.boot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.spring.boot.dto.AddressForm;
import com.spring.boot.dto.PrincipalDetails;
import com.spring.boot.model.Pay;
import com.spring.boot.model.Point;
import com.spring.boot.model.Product;
import com.spring.boot.model.Review;
import com.spring.boot.model.Address;
import com.spring.boot.model.OrderList;
import com.spring.boot.model.SiteUser;
import com.spring.boot.dto.PrincipalDetails;
import com.spring.boot.dto.ProductForm;
import com.spring.boot.service.PayService;
import com.spring.boot.service.PointService;
import com.spring.boot.service.ProductService;
import com.spring.boot.service.ReviewService;
import com.spring.boot.service.AddressService;
import com.spring.boot.service.MembershipService;
import com.spring.boot.service.OrderListService;
import com.spring.boot.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequestMapping("/user")
@Controller
public class MyPageController {

    private static final List<MultipartFile> imagePaths = null;
    
    private final PayService payService;
    private final UserService userService;
    private final AddressService addressService;
    private final ProductService productService;
    private final MembershipService membershipService;
    private final PointService pointService;
    private final OrderListService orderListService;
    private final ReviewService reviewService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public MyPageController(PayService payService, UserService userService, AddressService shippingService, ProductService productService, MembershipService membershipService,
    		PointService pointService, OrderListService orderListService ,ReviewService reviewService ) {
        this.payService = payService;
        this.userService = userService;
        this.addressService = shippingService;
        this.productService = productService;
        this.membershipService = membershipService;
        this.pointService = pointService;
        this.orderListService = orderListService;
        this.reviewService = reviewService;
    }

    @GetMapping("/mypage")
    public String myPage(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam(name = "pageNum", defaultValue = "1") int pageNum) {
    	
        if (principalDetails != null) {
        	
        	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
            model.addAttribute("user", user);
            
            int itemsPerPage = 9; // 페이지당 항목 수
            
            //5개 페이지씩 묶음(소수점 없으면 동작 제대로 안함)
            int tempEnd = (int)((Math.ceil(pageNum / 5.0))* 5);
            
            //첫 번째 번호는 tempEnd - 4(1, 6, 11, ...)
            int start = tempEnd - 4;
            
            Page<Product> productPage = productService.getProductsPaged(user.getId(), pageNum, itemsPerPage);
            List<Product> product = productPage.getContent();

            model.addAttribute("product", product);
            
            int totalItemCount = (int) productService.getTotalItemCount(principalDetails.getId());
            int totalPages = (int) Math.ceil((double) totalItemCount / itemsPerPage);
            
            int end;
            
            if(totalPages > tempEnd) {
            	end = tempEnd;
            }else {
            	end = totalPages;
            }
            
            List<Integer> pageList = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());

            String listUrl = "/user/mypage/"; 
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("currentPage", pageNum);
            model.addAttribute("pageList", pageList);
            model.addAttribute("listUrl", listUrl); // listUrl 변수를 모델에 추가
        	
        	return "myPage";

        }
        
        return "login";
    }
    
    @GetMapping("/mypage/sale")
    public String myPageSale(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam(name = "pageNum", defaultValue = "1") int pageNum) {

    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
        model.addAttribute("user", user);

        int itemsPerPage = 9;
        Page<Product> saleProductPage = productService.getSaleProductsPaged(user.getId(), pageNum, itemsPerPage);
        List<Product> saleProduct = saleProductPage.getContent();

        int totalItemCount = (int) productService.getSaleCount(user.getId()); // Sale 상품의 총 개수
        int totalPages = (int) Math.ceil((double) totalItemCount / itemsPerPage);

        List<Integer> pageList = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());

        model.addAttribute("product", saleProduct);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("pageList", pageList);
        
        return "sale";

    }
    
    @GetMapping("/mypage/soldout")
    public String myPageSoldout(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam(name = "pageNum", defaultValue = "1") int pageNum) {

    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
        model.addAttribute("user", user);

        int itemsPerPage = 9;
        Page<Product> soldProductPage = productService.getSoldoutProductsPaged(user.getId(), pageNum, itemsPerPage);
        List<Product> soldProduct = soldProductPage.getContent();

        int totalItemCount = (int) productService.getSoldoutCount(); 
        int totalPages = (int) Math.ceil((double) totalItemCount / itemsPerPage);

        List<Integer> pageList = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());

        model.addAttribute("product", soldProduct);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("pageList", pageList);

        return "soldout";

    }
    
    /*
    @GetMapping("/mypage/wishlist")
    public String myWishList(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam(name = "pageNum", defaultValue = "1") int pageNum) {

    	//찜한 상품 목록
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
    	model.addAttribute("user", user);
    	
    	
    	return "wishList";
    }
    */
    @GetMapping("/mypage/seller")
    public String myWishList(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam(name = "pageNum", defaultValue = "1") int pageNum) {

    	//찜한 상품 목록
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
    	model.addAttribute("user", user);
    	
    	
    	return "seller";
    }
    

	@GetMapping("/mypage/change")
    public String change(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {

		SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
    	model.addAttribute("user", user);
		
    	//회원정보 수정 하기 전 비밀 번호 확인
        return "change";
        
    }
	
	@PostMapping("/mypage/checkPassword")
	public String checkPassword(@RequestParam("userPassword") String enteredPassword, @AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
	    
		SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
	    
	    if (passwordEncoder.matches(enteredPassword, user.getPassword())) {
	        // 비밀번호가 일치하면 원하는 페이지로 리디렉션
	        return "redirect:/user/mypage/change/changeInfo";
	        
	    } else {
	    	
	        // 비밀번호가 일치하지 않을 때, 사용자에게 알림을 표시
	    	model.addAttribute("errorMessage", "비밀번호가 올바르지 않습니다. 다시 시도하세요.");
	        return "redirect:/user/mypage/change";
	    }
	}
	
    @GetMapping("/mypage/change/changeInfo")
    public String changeInfo(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
       
    	//회원정보 수정탭
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
    	
    	model.addAttribute("birthYear", user.getBirthDate().getYear());
    	model.addAttribute("birthMonth", user.getBirthDate().getMonth().getValue());
    	model.addAttribute("birthDay", user.getBirthDate().getDayOfMonth());
    	
    	model.addAttribute("user", user);
    	
        return "changeInfo";
        
    }
    
    @PostMapping("/mypage/change/changeInfo")
    public String changeInfo(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam String originalPassword, @RequestParam String newPassword) {
        
    	// 현재 사용자 정보 가져오기
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());

        if (isPasswordValid(newPassword)) {
        	
            // 현재 비밀번호와 입력한 비밀번호를 비교
            if (passwordEncoder.matches(originalPassword, user.getPassword())) {
                // 새 비밀번호를 해싱하여 저장
                String encodedNewPassword = passwordEncoder.encode(newPassword);
                user.setPassword(encodedNewPassword);

                // SiteUser 업데이트
                userService.updatePassword(user, newPassword);

                return "redirect:/user/mypage/changeSuccess";
                
            } else {
                
                return "redirect:/user/mypage/change/changeInfo"; 
            }
        } else {

            return "redirect:/user/mypage/change/changeInfo";
        }
    }
    
    @GetMapping("/mypage/changeSuccess")
    public String changeSuccess(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
    	
    	// 현재 사용자 정보 가져오기
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
    	model.addAttribute("user", user);
    	
    	return "changeSuccess";
    	
    	
    }
    	

    // 비밀번호 변경 조건을 확인하는 메서드
    private boolean isPasswordValid(String newPassword) {
        // 비밀번호의 조건을 여기에 추가합니다. 예를 들어, 길이, 숫자, 특수 문자 등
        if (newPassword.length() >= 6 && newPassword.matches(".*\\d.*") && newPassword.matches(".*[~!@#$%^&*()_+|<>?:{}].*")) {
            return true;
        }
        return false;
    }	
  
    
    @GetMapping("/mypage/shipping")
    public String shipping(Model model, HttpServletRequest request, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
    	// 배송지 관리 페이지
    	// 사용자의 배송지 목록을 가져오는 서비스 또는 메서드 호출
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());

        long userId = user.getId();
    	
        List<Address> addressList = addressService.getAddressListByUserId(userId);

        model.addAttribute("user", user);
        model.addAttribute("addressList", addressList);
    	
        return "shipping";
    }
    
    @PostMapping("/mypage/shipping")
    public String shipping(@ModelAttribute("addressForm") @Valid AddressForm addressForm, Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
    	

    	return "shipping";
    	
    }
    
    
    @PostMapping("/mypage/shipping/delete/{id}")
    public String deleteShipping(@PathVariable("id") Long id, Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
    	
        // 주소 삭제 로직을 구현
        boolean deleted = addressService.deleteAddress(id);

        if (deleted) {
            // 삭제가 성공하면 배송지 목록을 다시 가져옵니다.

        	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());

            long userId = user.getId();
        	
            List<Address> addressList = addressService.getAddressListByUserId(userId);
            
            model.addAttribute("user", user);
            model.addAttribute("addressList", addressList);
    
            
        } else {
            // 삭제에 실패한 경우 오류 메시지를 처리합니다.
            model.addAttribute("error", "주소 삭제에 실패했습니다.");
        }
        
        return "redirect:/user/mypage/shipping";
    }
 

    @GetMapping("/mypage/shipping_popup")
    public String shippingPopup(Model model, HttpServletRequest request) {
    	
    	return "shipping_popup";
    	
    }
    
    @PostMapping("/mypage/shipping_popup")
    public String shippingPopup(@ModelAttribute("addressForm") @Valid AddressForm addressForm, Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
    	
    	//배송지 정보 관리
        // ShippingForm에서 Shipping 엔티티로 데이터 복사
    	Address address = new Address();
    	address.setReceiverName(addressForm.getReceivername());
    	address.setType(addressForm.getType());
    	address.setPhone(addressForm.getPhone());
    	address.setZipcode(addressForm.getZipcode());
    	address.setAddress01(addressForm.getAddress01());
    	address.setAddress02(addressForm.getAddress02());

        // 현재 로그인한 사용자 정보 가져오기
        SiteUser user = userService.getUserByUserName(principalDetails.getUsername());

        // Shipping 엔티티에 현재 로그인한 사용자 설정
        address.setUser(user);
        
        // Shipping 정보를 서비스를 통해 저장
        addressService.saveAddress(address);
        
        /* 창이 달라서 새로고침 기능 불가능
        boolean success = shippingService.saveShipping(shipping); // 성공 여부 확인

        if (success) {
            // 배송지 추가 후, 사용자 ID를 사용하여 새로운 배송지 목록을 가져옵니다.
            long userId = user.getId();
            List<Shipping> shippingList = shippingService.getShippingListByUserId(userId); // 오름차순 정렬
            model.addAttribute("shippingList", shippingList);
        }
        */
        
        //배송지 등록 성공 페이지
        return "shipping_success";
    	
    }
    	
    @GetMapping("/mypage/myshop/add")
    public String myShopAdd(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        //물건 등록 페이지
    	//현재 사용자의 user_id 가져오기
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
        long userId = user.getId();
        
        model.addAttribute("productForm", new ProductForm());
        model.addAttribute("user", user);
        model.addAttribute("userId", userId);
        
        return "myshop_add";
    }
  

    @PostMapping("/mypage/myshop/add")
    public String myShopCreate(@ModelAttribute("ProductForm") @Valid ProductForm productForm, Model model, @AuthenticationPrincipal PrincipalDetails principalDetails, BindingResult bindingResult) {
       
    	
    	// 상품을 등록하기 위한 POST 요청 처리
        if (bindingResult.hasErrors()) {
            // 입력 데이터의 유효성 검사 실패 시, 다시 폼을 보여줌
            return "myshop_add";
        }
        
        // 파일 업로드 및 경로 저장
        List<String> imagePaths = new ArrayList<>();
        
        for (MultipartFile image : productForm.getImages()) {
            if (image != null && !image.isEmpty()) {
                
                String imagePath = saveImage(image);
                if (imagePath != null) {
                    imagePaths.add(imagePath);
                }
            }
        }
                
        // Product 엔티티에 이미지 경로 저장
        Product product = new Product();
        product.setImage(imagePaths.size() >= 1 ? imagePaths.get(0) : null);
        product.setImage1(imagePaths.size() >= 2 ? imagePaths.get(1) : null);
        product.setImage2(imagePaths.size() >= 3 ? imagePaths.get(2) : null);
        product.setImage3(imagePaths.size() >= 4 ? imagePaths.get(3) : null);

        // 상품 등록 로직
        //현재 사용자의 user_id 가져오기
        SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
        long userId = user.getId();
        productService.registerProduct(productForm, product, userId);

        return "redirect:/user/mypage";
                    

    }     
   
    private String saveImage(MultipartFile image) {
        // 이미지를 저장할 디렉토리 경로 설정 (application.properties에 설정한 경로 사용)
    	
    	String uploadDirectory = "src/main/resources/static/product";

        // 업로드한 이미지 파일의 원래 파일 이름
        String originalFilename = image.getOriginalFilename();

        // 이미지 파일 이름에 UUID 추가하여 고유한 이름 생성
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename;

        //Path imagePath = Paths.get(uploadDirectory, uniqueFileName);
        //Path imagePath = Paths.get(uniqueFileName);
        
        try {
            // 이미지를 저장할 디렉토리가 없으면 생성
            Files.createDirectories(Paths.get(uploadDirectory));

         // 서버에 이미지 파일 저장 (경로를 생략하여 저장)
            Files.copy(image.getInputStream(), Paths.get(uploadDirectory, uniqueFileName), StandardCopyOption.REPLACE_EXISTING);

            // 저장된 이미지 파일의 경로를 반환
            return uniqueFileName;
            
        } catch (IOException e) {
            e.printStackTrace();
            // 이미지 저장 실패 시 예외 처리 필요
            return null;
        }
    }
    
    @GetMapping("/mypage/myshop/edit/{id}")
    public String myShopEdit(@PathVariable Long id, Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
    	
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
    	model.addAttribute("user", user);
    	
        // Principal 객체를 통해 현재 사용자의 ID를 가져오기
        Long userId = principalDetails.getId();

        // id를 기반으로 수정할 상품을 가져옵니다.
        Product editProduct = productService.getProductById(id);

        // 상품이 없는 경우 또는 권한이 없는 경우에 대한 처리
        if (editProduct == null || !userId.equals(editProduct.getUser().getId())) {
            return "redirect:/user/mypage"; // 에러 페이지로 리디렉션 또는 다른 처리

        }

        // 사용자가 상품을 수정할 수 있으므로 정보를 모델에 추가
        model.addAttribute("editProduct", editProduct);
        
        return "myshop_edit";
    }
    
    
    @PostMapping("/mypage/myshop/edit/{id}")
    public String myShopEdit(@PathVariable Long id, ProductForm productForm) {

        // 상품 정보 업데이트를 수행하는 서비스 호출
        productService.updateProduct(id, productForm);
        
        return "redirect:/user/mypage/sale";
        
    }

    
    @GetMapping("/mypage/mybuyhistory")
    public String myBuyHistory(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails,
    		@PageableDefault Pageable pageable,
    		@RequestParam(name = "page", defaultValue = "1") int pageNum) {

    	//구매 내역 페이지
    	//사용자 정보 가져오기
        SiteUser user = userService.getUserByUserName(principalDetails.getUsername());

        model.addAttribute("user", user);
        
        //사용자 ID 가져오기
        Long userId = user.getId();
        
        int itemsPerPage = 5;
        
        //거래 목록 가져오기
        List<OrderList> orderList = orderListService.findOrderByUserId(userId);
        
        int totalItemCount = orderList.size();
        int totalPages = (int) Math.ceil((double) totalItemCount / itemsPerPage);
        List<Integer> pageList = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());
        
        int startIndex = (pageNum - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItemCount);
        List<OrderList> orderAll = orderList.subList(startIndex, endIndex);
        
        
        
        //은별 id 로 리뷰목록 가져오기 
        Page<Review> paging = reviewService.getReview(user,pageable);
		
		model.addAttribute("paging",paging);
        
        model.addAttribute("orderList", orderAll);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("pageList", pageList); 
        model.addAttribute("listUrl", "/user/mypage/mybuyhistory/");
        
        return "myBuyHistory";
    }

       
    @GetMapping("/mypage/pay")
    public String Pay (Model model, HttpServletRequest request, @AuthenticationPrincipal PrincipalDetails principalDetails) {
    	
    	//pay 충전 페이지
    	//사용자 정보 user에 담아서 model에 추가
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
    	model.addAttribute("user", user);
    	
        return "pay";
    }
    
    @PostMapping("/mypage/pay")
    public String pay(Model model, HttpServletRequest request, @AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam("paymoney") Integer paymoney) {

        // pay 충전 페이지
        try {
        	
        	// 사용자 정보 가져오기
        	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());

        	model.addAttribute("user", user);
        	
            Pay pay = new Pay();
            
            pay.setUser(user);
            pay.setPayMoney(paymoney);
            pay.setType("충전");
            pay.setPayDate(new Date());

            // 결제 정보를 데이터베이스에 저장
            payService.save(pay);
            
            //사용자의 paymoney 업데이트
            user.setPaymoney(user.getPaymoney() + paymoney);

            
        } catch (Exception e) {
            // 결제 실패 시 화면에 메시지 전달
            e.printStackTrace();
        }
        return "pay";
    }

    @GetMapping("/mypage/paylist")
    public String PayList (Model model, @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(name = "page", defaultValue = "1") int pageNum) {
    	
    	//pay 내역 페이지
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
        model.addAttribute("user", user);
        
        Long userId = user.getId();
        
        int itemsPerPage = 5; 
        
        // 전체 포인트 내역을 가져오기
        List<Pay> payList = payService.findPaysByUserId(userId);

        // 페이지네이션 계산
        int totalItemCount = payList.size();
        int totalPages = (int) Math.ceil((double) totalItemCount / itemsPerPage);

        // 페이지 목록 생성
        List<Integer> pageList = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());

        // 페이지에 해당하는 포인트 내역 가져오기
        int startIndex = (pageNum - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItemCount);
        List<Pay> payAll = payList.subList(startIndex, endIndex);

        model.addAttribute("payList", payAll);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("pageList", pageList); // 페이지 목록 추가
        model.addAttribute("listUrl", "/user/mypage/paylist/");
        
    	
        return "payList";
    }
    
    @GetMapping("/mypage/point")
    public String point(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(name = "page", defaultValue = "1") int pageNum) {
    	
        // 사용자 정보 가져오기
        SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
        model.addAttribute("user", user);

        // 사용자 ID 가져오기
        Long userId = user.getId();

        int itemsPerPage = 5; 
        
        // 전체 포인트 내역을 가져오기
        List<Point> pointList = pointService.findPointsByUserId(userId);

        // 페이지네이션 계산
        int totalItemCount = pointList.size();
        int totalPages = (int) Math.ceil((double) totalItemCount / itemsPerPage);

        // 페이지 목록 생성
        List<Integer> pageList = IntStream.rangeClosed(1, totalPages).boxed().collect(Collectors.toList());

        // 페이지에 해당하는 포인트 내역 가져오기
        int startIndex = (pageNum - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItemCount);
        List<Point> pointAll = pointList.subList(startIndex, endIndex);

        model.addAttribute("pointList", pointAll);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", pageNum);
        model.addAttribute("pageList", pageList); // 페이지 목록 추가
        model.addAttribute("listUrl", "/user/mypage/point/");

        return "point";
    }
    
    
    @GetMapping("/mypage/membership")
    public String membership(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
    	
    	//membership 가입 페이지
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
    	model.addAttribute("user", user);
    	
    	return "membership";
    }
    
    @PostMapping("/mypage/membership")
    public ResponseEntity<String> membership(
    	    @RequestBody String data,
    	    @AuthenticationPrincipal PrincipalDetails principalDetails) {
    	    try {
    	        SiteUser user = userService.getUserByUserName(principalDetails.getUsername());

    	        if (user == null) {
    	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
    	        }

    	        if ("success".equals(data)) {
    	            // "success" 문자열을 받았을 때 사용자 정보 업데이트

    	        	LocalDate currentDate = LocalDate.now(); // 현재 날짜
    	            LocalDate membershipEndDate = currentDate.plus(30, ChronoUnit.DAYS); // 30일 후 날짜 계산
    	        	
    	        	user.setPaymoney(user.getPaymoney() - 5000); // 5000원 차감
    	            user.setMembership(true);
    	            user.setMembershipEndDate(membershipEndDate);

    	            SiteUser updatedUser = membershipService.save(user);

    	            return ResponseEntity.ok("멤버쉽 가입을 성공했습니다. 기간은 30일입니다.");
    	            
    	        } else {
    	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("멤버쉽 가입에 실패하였습니다. 잠시 후 다시 시도해주세요.");
    	        }
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("멤버쉽 가입에 실패하였습니다. 잠시 후 다시 시도해주세요.");
    	    }
    	}
    
    @GetMapping("/mypage/grade")
    public String grade(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
    	
    	//구매등급 안내 페이지
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
    	model.addAttribute("user", user);
    	
    	return "grade";
    }
    
    @GetMapping("/mypage/myInterest")
    public String myInterest(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
    	
    	//나의 관심 분야 설정하기
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
    	model.addAttribute("user", user);
    	
    	return "myInterest";
    }
    
    @PostMapping("/mypage/myInterest")
    public String myInterestSave(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam(value = "interests", required = false) List<String> interests) {
    	
    	//나의 관심 분야 설정하기
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
    	model.addAttribute("user", user);
    	
    	// 선택한 관심 분야 수를 확인하고 최대 3개까지 저장
        int maxInterests = Math.min(interests.size(), 3);
        // 기존 값을 초기화하고 새로운 값을 할당
        user.setInterest1(null);
        user.setInterest2(null);
        user.setInterest3(null);

    	if (interests != null) {
    		
            for (int i = 0; i < maxInterests; i++) {
                switch (i) {
                    case 0:
                        user.setInterest1(interests.get(0));
                        break;
                    case 1:
                        user.setInterest2(interests.get(1));
                        break;
                    case 2:
                        user.setInterest3(interests.get(2));
                        break;
                }
            }
        }

        userService.updateUser(user);
    	
        return "redirect:/user/mypage/myInterest?s=true";
    }
    
    @GetMapping("/mypage/withdraw")
    public String withdraw(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
    	
    	//회원 탈퇴 페이지
    	//사용자 정보 가져오기 (비밀번호 일치 할 경우 탈퇴 처리를 위해)
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());
    	model.addAttribute("user", user);
    
    	return "withdraw";
    }
    
    @PostMapping("/mypage/withdraw")
    public String withDraw(@RequestParam("userPassword") String enteredPassword, @AuthenticationPrincipal PrincipalDetails principalDetails, Model model, HttpServletRequest request, HttpServletResponse response) {
    	
    	SiteUser user = userService.getUserByUserName(principalDetails.getUsername());

        if (passwordEncoder.matches(enteredPassword, user.getPassword())) {
        	
            // 비밀번호가 일치할 경우 사용자 비활성화
        	user.setActivated(false);
        	
            userService.saveUser(user);
            
            // 로그아웃 처리
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.setInvalidateHttpSession(true); // 현재 세션 무효화
            logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());

            return "redirect:/user/mypage/withdraw_success";
            
        } else {
        	
            model.addAttribute("errorMessage", "비밀번호가 올바르지 않습니다. 다시 시도하세요.");
            
            return "redirect:/user/mypage/withdraw";
        }
    }
    
}

   
