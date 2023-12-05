package com.spring.boot.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.spring.boot.model.OrderList;
import com.spring.boot.model.SiteUser;

public interface OrderListRepository extends JpaRepository<OrderList, Long>, JpaSpecificationExecutor<OrderList> {

	List<OrderList> findByUser(SiteUser user);

	List<OrderList> findOrderByUser(SiteUser user);

	// 판매량 내림차순으로 productno 반환
	@Query("SELECT o.productno FROM OrderList o GROUP BY o.productno ORDER BY SUM(o.count) DESC")
	List<Long> findTopNSellingProductnos(Pageable pageable);

	// 주어진 카테고리에 해당하는 제품의 판매량 내림차순으로 productno 반환
	@Query("SELECT o.productno FROM OrderList o WHERE o.productno IN :productIds GROUP BY o.productno ORDER BY COUNT(o) DESC")
	List<Long> findTopNSellingProducts(@Param("productIds") List<Long> productIds, Pageable pageable);

	// 판매량 상위 20개의 productno 리스트로 재구매율이 높은(재구매 3회 이상) 상품의 productno와 구매한 userId 반환
	@Query("SELECT o.productno, o.user.id FROM OrderList o WHERE o.productno IN :productno GROUP BY o.productno, o.user.id HAVING COUNT(o) >= 3")
	List<Object[]> findProductsBoughtMoreThan3TimesBySameUser(@Param("productno") List<Long> productno);
}
