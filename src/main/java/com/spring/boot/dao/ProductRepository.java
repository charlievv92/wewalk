package com.spring.boot.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.spring.boot.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

	// 주의: 여기 레포에서 하나라도 안돌아가면 에러남

	// 전체상품 찾는 메소드
	Page<Product> findAll(Pageable pageable);//
	// pageable 을 돌려줘야함 대신

	@EntityGraph(value = "Product.withAll", type = EntityGraph.EntityGraphType.LOAD)
	Page<Product> findAll(Specification<Product> spec, Pageable pageable);

	// 판매자id 로 product상품List 찾는 메소드
	Page<Product> findByUserId(long userId, Pageable pageable);
	// 매개변수id 랑 pageable 둘다 줘야하나????????
	// 이거아직 안씀

	// productno로 상품 detail 찾는 메소드
	Optional<Product> findById(long productNo);
	// Question findBySubjectAndContent(String subject, String
	// content);//두개의조건으로셀렉하기

	@Query("SELECT p FROM Product p WHERE p.user.id = :userId AND p.stock > 0")
	List<Product> findSaleProductsByUserId(@Param("userId") Long userId);

	// 검색-카테고리검색
	List<Product> findByCategoryLike(String category);

	List<Product> findAllByCategory(String category);

	// 소연
	Page<Product> findByUserId(Long userId, Pageable pageable);

	Page<Product> findByUserIdAndStockGreaterThan(Long userId, int stock, Pageable pageable);

	Page<Product> findByUserIdAndStockEquals(Long userId, int stock, Pageable pageable);

	List<Product> findByStockGreaterThan(int stock);

	List<Product> findByStockEquals(int stock);

	// 판매량 순으로 검색(n개)
	List<Product> findByIdIn(List<Long> topNSellingProducts);

	// 판매량 순으로 검색(n개)
	Page<Product> findByIdIn(List<Long> productIdList, Pageable pageable);

	// 가장 최근에 등록한 상품 8개
	List<Product> findTop8ByOrderByDateDesc();

	long countByUserId(Long userId);

	// productlist-상품id 로 관련된 판매자siteuser의 다른상품추천하기
	List<Product> findByUserId(long siteuser);

}
