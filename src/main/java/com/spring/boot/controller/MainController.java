package com.spring.boot.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.spring.boot.dto.PageRequestDTO;
import com.spring.boot.dto.PrincipalDetails;
import com.spring.boot.model.Product;
import com.spring.boot.model.SiteUser;
import com.spring.boot.service.OrderListService;
import com.spring.boot.service.ProductService;
import com.spring.boot.service.UserService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/wewalk")
@RequiredArgsConstructor
@Controller
public class MainController {

	private final UserService userService;
	private final ProductService productService;
	private final OrderListService orderListService;
	private final SpringTemplateEngine templateEngine;

	@GetMapping("/main")
	public String mainPage(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {

		// 판매량 상위 3개
		Pageable topN = PageRequest.of(0, 3);

		//로그인 한 유저가 관심가질만한 상품 추천
		if (principalDetails != null) {
			SiteUser user = userService.getUserByUserName(principalDetails.getUsername());

			String userInterest = user.getInterest1();

			// interest가 일치하는 상품 중 판매량이 높은 순으로 3개 가져오기
			List<Product> productsFoundByInterest = productService.getProductsByCategory(userInterest);

			List<Long> top3SellingProductnosFoundByInterest = orderListService
					.getTopNSellingProductnosFoundByCategory(productsFoundByInterest, topN);

			List<Product> top3SellingProductsFoundByInterest = productService
					.getTopNSellingProducts(top3SellingProductnosFoundByInterest);

			model.addAttribute("top3products", top3SellingProductsFoundByInterest);
			model.addAttribute("user", user);

		}

		// 최근등록상품8개
		List<Product> recentlyProducts = productService.getTop8NewestProducts();
		model.addAttribute("recentlyProducts", recentlyProducts);

		// 판매량 상위 8개
		topN = PageRequest.of(0, 8);

		// 주문내역에서 groupby를 통해 가장 많이 판매된 상품의 productno를 8개 가져옴
		List<Long> top8SellingProductnos = orderListService.getTopNSellingProductnos(topN);

		// 프로덕트에서 검색해서 가장 많이 팔린 상품 8개 리스트 들고 감
		List<Product> productListTop8 = productService.getTopNSellingProducts(top8SellingProductnos);
		model.addAttribute("productListTop8", productListTop8);

		return "mainPage";
	}

	// 메인에서 검색시이곳으로 keyword가 검색어
	@GetMapping("/search")
	public String search(@RequestParam(value = "sort", required = false) String sort,
			@ModelAttribute PageRequestDTO pageRequestDTO, Model model,
			@AuthenticationPrincipal PrincipalDetails principalDetails) {

		String sortText = "";
		if (principalDetails != null) {
			SiteUser user = userService.getUserByUserName(principalDetails.getUsername());

			model.addAttribute("user", user);
		}

		model.addAttribute("paging", productService.getSearchList(pageRequestDTO, sort));

		if (sort != null && !sort.isEmpty()) {
			if (sort.equals("newest")) {
				sortText = "신상품순";
			} else if (sort.equals("priceAsc")) {
				sortText = "낮은 가격순";
			} else if (sort.equals("priceDesc")) {
				sortText = "높은 가격순";
			} else if (sort.equals("category")) {
				sortText = "카테고리순";
			}

			model.addAttribute("sort", sort);
			model.addAttribute("sortText", sortText);
		} else {
			model.addAttribute("sort", "newest");
			model.addAttribute("sortText", "신상품순");
		}

		if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
			model.addAttribute("keyword", pageRequestDTO.getKeyword());
		}

		return "search";

	}

	// 베스트 상품 페이지
	@GetMapping("/searchBest")
	public String searchBest(@RequestParam(value = "sort", required = false) String sort,
			@ModelAttribute PageRequestDTO pageRequestDTO, Model model,
			@AuthenticationPrincipal PrincipalDetails principalDetails) {

		String sortText = "";

		if (principalDetails != null) {
			SiteUser user = userService.getUserByUserName(principalDetails.getUsername());

			model.addAttribute("user", user);
		}

		// 판매량 상위 20개
		Pageable top20 = PageRequest.of(0, 20);

		List<Long> top20SellingProductnos = orderListService.getTopNSellingProductnos(top20);

		// 판매량 상위 20개 상품 중 2회 이상 재구매한 유저가 있는 상품
		List<Long> productnosTop20SellingNBoughtMoreThan3TimesBySameUser = orderListService
				.getProductnosBoughtMoreThan3TimesBySameUser(top20SellingProductnos);

		model.addAttribute("paging", 
				productService.getBestProducts(
						productnosTop20SellingNBoughtMoreThan3TimesBySameUser, pageRequestDTO, sort)
		);
		
		if (sort != null && !sort.isEmpty()) {
			if (sort.equals("newest")) {
				sortText = "신상품순";
			} else if (sort.equals("priceAsc")) {
				sortText = "낮은 가격순";
			} else if (sort.equals("priceDesc")) {
				sortText = "높은 가격순";
			} else if (sort.equals("category")) {
				sortText = "카테고리순";
			}

			model.addAttribute("sort", sort);
			model.addAttribute("sortText", sortText);
		} else {
			model.addAttribute("sort", "newest");
			model.addAttribute("sortText", "신상품순");
		}

		return "searchBest";

	}

}
