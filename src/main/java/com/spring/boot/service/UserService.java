package com.spring.boot.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.spring.boot.config.DataNotFoundException;
import com.spring.boot.dao.OrderListRepository;
import com.spring.boot.dao.SellerRequestRepository;
import com.spring.boot.dao.UserRepository;
import com.spring.boot.model.OrderList;
import com.spring.boot.model.SellerRequest;
import com.spring.boot.model.SiteUser;
import com.spring.boot.model.UserRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	
	private final UserRepository userRepository;
	private final SellerRequestRepository sellerRequestRepository;
	private final OrderListRepository orderListRepository;
	
	//BCrypt해시 함수 호출
	private final PasswordEncoder passwordEncoder;
	
	//id 생성 메소드
	public SiteUser create(UserRole role, String email, String password, String userName,  
							String name, LocalDate birthDate, String postcode, String address, 
							String detailAddress, String tel, String picture, boolean seller, 
							boolean isActivated, String grade, Integer paymoney, Integer point) {
		
		SiteUser user = SiteUser.builder()
								.role(role)
								.email(email)
								.password(passwordEncoder.encode(password))
								.userName(userName)
								.name(name)
								.createdDate(LocalDateTime.now())
								.birthDate(birthDate)
								.postcode(postcode)
								.address(address)
								.detailAddress(detailAddress)
								.tel(tel)
								.picture(picture)
								.seller(seller)
								.isActivated(isActivated)
								.grade(grade)
								.paymoney(paymoney)
								.point(point)
								.build();
		
		//회원정보 db에 저장
		userRepository.save(user);
		
		return user;
	}
	
	public SiteUser getUser(Long id) {
		
		Optional<SiteUser> op = userRepository.findById(id);
		
		if(op.isPresent()) {
						
			return op.get();
			
		}else {
			
			throw new DataNotFoundException("데이터가 없습니다");
			
		}
		
	}
	
	//이메일로 불러오기
	public SiteUser getUserByEmail(String email) {
		
		Optional<SiteUser> siteUser = 
				userRepository.findByEmail(email);
		
		if(siteUser.isPresent()) {
			return siteUser.get();
		}else {
			throw new DataNotFoundException("User not found!");
		}
		
	}
	

	//userName으로 불러오기
	public SiteUser getUserByUserName(String userName) {
	
		Optional<SiteUser> siteUser = 
				userRepository.findByUserName(userName);
			

		if(siteUser.isPresent()) {
			return siteUser.get();
		}else {
			throw new DataNotFoundException("User not found!");
		}

		
	}
	
	//사용자 정보 수정
	public void modify(SiteUser siteUser, String email, String userName, 
			String postcode, String address, String detailAddress, String tel) {
		
		siteUser.setEmail(email);
		siteUser.setUserName(userName);
		siteUser.setPostcode(postcode);
		siteUser.setAddress(address);
		siteUser.setDetailAddress(detailAddress);
		siteUser.setTel(tel);
		siteUser.setModifyDate(LocalDateTime.now());
		
		userRepository.save(siteUser);
		
	}
	
	//소셜 로그인 유저 회원가입(DB에 있는 정보 업데이트)
  	public void oauthSignup(SiteUser siteUser, UserRole role, String name, 
  			LocalDate birthDate, LocalDateTime createdDate, 
  			String postcode, String address, String detailAddress, String tel, 
  			boolean isActivated, String grade, Integer point, Integer paymoney) {
  		
  		siteUser.setRole(role);
  		siteUser.setName(name);
  		siteUser.setBirthDate(birthDate);
  		siteUser.setCreatedDate(createdDate);
  		siteUser.setPostcode(postcode);
  		siteUser.setAddress(address);
  		siteUser.setDetailAddress(detailAddress);
  		siteUser.setTel(tel);
  		siteUser.setActivated(isActivated);
  		siteUser.setGrade(grade);
  		siteUser.setPoint(point);
  		siteUser.setPaymoney(paymoney);
  		
  		userRepository.save(siteUser);
  		
  	}

	public boolean existsByUserName(String userName) {
		Optional<SiteUser> siteUser = 
				userRepository.findByUserName(userName);
		
		if(siteUser.isPresent()) {
			return true;
		}else {
			return false;
		}
	}
	
	//판매자 등록 요청 리스트 저장
	public SellerRequest saveSellerRequest(SiteUser requestUser, String intro, 
			LocalDateTime requestTime, boolean isProcessed) {
		
		SellerRequest sellerRequest = SellerRequest.builder()
								.requestUser(requestUser)
								.intro(intro)
								.requestTime(requestTime)
								.isProcessed(isProcessed)
								.build();
		
		//판매자 등록 요청 테이블에 저장
		sellerRequestRepository.save(sellerRequest);
		
		return sellerRequest;
	}
	
	//id로 판매자 요청 내용 불러오기
	public SellerRequest getSellerRequest(Long id) {
		
		Optional<SellerRequest> sellerRequest = 
				sellerRequestRepository.findById(id);
		
		if(sellerRequest.isPresent()) {
			return sellerRequest.get();
		}else {
			throw new DataNotFoundException("Data not found!");
		}
		
	}
	
	//논리적 삭제
	//비활성화 또는 활성화 모두 가능하게 함
	public void reactivate(SiteUser siteUser) {
		
		System.out.println(siteUser + "활성화");

		//물리적 삭제는 기한을 두고 진행할 수 있게 추후 업데이트 예정
		//		adminRepository.delete(siteUser);
		
		siteUser.setActivated(true);
		
		userRepository.save(siteUser);
	}
	
	/* security를 이용한 비밀번호 일치여부 확인 메소드
	 public boolean checkPassword(String username, String inputPassword) {
	    try {
	        // 사용자로부터 입력받은 비밀번호를 이용한 인증 요청 객체 생성
	        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, inputPassword);
	
	        // 인증 시도
	        authenticationManager.authenticate(token);
	        
	        return true; // 인증 성공 = 비밀번호 일치
	    } catch (BadCredentialsException e) {
	        return false; // 비밀번호 불일치
	    }
	}
	  
	 */

	
	//비밀번호 업데이트
	public void updatePassword(SiteUser user, String newPassword) {
		
        // 새로운 비밀번호를 암호화하여 설정합니다.
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        // 사용자 정보를 업데이트합니다.
        userRepository.save(user);
    }
	
	//회원 비활성화
	public void saveUser(SiteUser user) {
        userRepository.save(user);
    }
	 
	 //수정
	 public void updateUser(SiteUser user) {
	        userRepository.save(user);
	    }
	 
	 //거래후 등급,적립금,페이머니 조정
	 public int updateAfterOrder(SiteUser user, int point, int payMoney, int amount, int accumulate) {
		 
		 if(!user.getGrade().equals("P")) {
			 List<OrderList> lists = orderListRepository.findByUser(user);
			 
			 int purchaseAmount = 0;
			 
			 for(OrderList orderlist : lists) {
				 purchaseAmount+=orderlist.getPrice();
			 }
			 
			 if(purchaseAmount>=500000) {
				 user.setGrade("P");
			 }else if(purchaseAmount>=300000) {
				 user.setGrade("G");
			 }else if(purchaseAmount>=100000) {
				 user.setGrade("S");
			 }
			 
		 }
		 
		 int allAmount = amount + point + payMoney;
		 int getPoint = allAmount * accumulate / 100; //적립금액
		 
		 user.setPoint(user.getPoint()-point+getPoint);
		 user.setPaymoney(user.getPaymoney()-payMoney);
		 userRepository.save(user);
		 
		 return getPoint;
	 }

	public SiteUser getUser(String name, String tel, String email) {
		SiteUser siteUser = 
				userRepository.findByNameAndTelAndEmail(name, tel, email);
		
		if(siteUser != null) {			
			return siteUser;
		}
		
		return null;
	}
	 
	 
	 
}