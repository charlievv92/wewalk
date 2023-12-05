package com.spring.boot.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.spring.boot.dao.OrderListRepository;
import com.spring.boot.dao.ProductRepository;
import com.spring.boot.dao.UserRepository;
import com.spring.boot.dto.ItemDataForm;
import com.spring.boot.dto.PaymentDataForm;
import com.spring.boot.model.OrderList;
import com.spring.boot.model.Product;
import com.spring.boot.model.SiteUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderListService {

	@Autowired
	private final OrderListRepository orderListRepository;
	private final ProductRepository productRepository;
	private final UserRepository userRepository;

	public List<OrderList> saveOrderHistory(PaymentDataForm paymentDataForm, SiteUser user) {

		List<ItemDataForm> list = paymentDataForm.getItemIds();
		List<OrderList> orderLists = new ArrayList<OrderList>();

		for (ItemDataForm productData : list) {

			OrderList orderlist = new OrderList();
			SiteUser seller = productRepository.findById(productData.getId()).get().getUser();

			orderlist.setOrderNo(paymentDataForm.getMerchant_uid());
			orderlist.setProductno(productData.getId());
			orderlist.setSellerid(seller.getId());
			orderlist.setUser(user);
			orderlist.setCount(productData.getCount());
			orderlist.setPrice(productData.getPrice() + 3000);// 택배비3000
			orderlist.setPayment(paymentDataForm.getPay_method());
			orderlist.setName(paymentDataForm.getBuyer_name());
			orderlist.setTel(paymentDataForm.getBuyer_tel());
			orderlist.setAddr(paymentDataForm.getBuyer_addr());
			orderlist.setZip(paymentDataForm.getBuyer_postcode());
			orderlist.setAddr_detail(paymentDataForm.getBuyer_addr_detail());
			orderlist.setRequest(paymentDataForm.getRequest());

			orderListRepository.save(orderlist);
			orderLists.add(orderlist);
		}

		return orderLists;

	}

	// 판매량 상위 n개의 productno 데이터 가져오기
	public List<Long> getTopNSellingProductnos(Pageable pageable) {

		// 몇개의 데이터 가져올지 변수로 입력
		Pageable topCount = pageable;

		// 판매량 상위 n개의 상품 id 리스트에 담기
		List<Long> topNSellingProductIdList = orderListRepository.findTopNSellingProductnos(topCount);

		return topNSellingProductIdList;
	}

	// 판매량 상위 n개의 productno 데이터 가져오기
	public List<Long> getTopNSellingProductnosFoundByCategory(
			List<Product> productsFoundByCategory, Pageable pageable) {

		// 몇개의 데이터 가져올지 변수로 입력
		Pageable topCount = pageable;

		//Product 클래스를 참조하여 리스트에 카테고리와 일치하는 상품의 id들을 담기
		List<Long> productIds = productsFoundByCategory
				.stream()
				.map(Product::getId)
				.collect(Collectors.toList());

		// 꺼내온 productId 상품 중 판매량 상위 n개의 상품 id 리스트에 담기
		List<Long> topNSellingProductIdList = orderListRepository.findTopNSellingProducts(productIds, topCount);

		return topNSellingProductIdList;
	}

	// 판매량 상위 n개의 상품 중 재구매가 3회 이상 일어난 상품 productno 리스트에 담기
	public List<Long> getProductnosBoughtMoreThan3TimesBySameUser(List<Long> topNSellingProductnos) {

		List<Object[]> findProductnosBoughtMoreThan3TimesBySameUser = orderListRepository
				.findProductsBoughtMoreThan3TimesBySameUser(topNSellingProductnos);

		List<Long> productIdList = findProductnosBoughtMoreThan3TimesBySameUser
				.stream().map(result -> (Long) result[0])
				.collect(Collectors.toList());

		return productIdList;

	}

	// 페이징
	public List<OrderList> findOrderByUserId(Long userId) {

		SiteUser user = userRepository.findById(userId).get();

		return orderListRepository.findOrderByUser(user);
	}

}
