package com.spring.boot.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.spring.boot.config.DataNotFoundException;
import com.spring.boot.dao.ProductRepository;
import com.spring.boot.dao.UserRepository;
import com.spring.boot.dto.ItemDataForm;
import com.spring.boot.dto.OrderResultForm;
import com.spring.boot.dto.PageRequestDTO;
import com.spring.boot.dto.PageResultDTO;
import com.spring.boot.dto.ProductDTO;
import com.spring.boot.dto.ProductForm;
import com.spring.boot.model.OrderList;
import com.spring.boot.model.Product;
import com.spring.boot.model.SiteUser;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Service
public class ProductService {
	
	@Autowired
	private final ProductRepository productRepository; //서비스와 레포 연결
	private final UserRepository userRepository;
	
	//최신글부터 전체셀렉
	public Page<Product> getTotalLists(Pageable pageable){
		
		//최신글부터 전체셀렉
		List<Sort.Order> sorts = new ArrayList<Sort.Order>();
		sorts.add(Sort.Order.desc("date"));
		
		//pageable 페이징 추가
		pageable = PageRequest.of(pageable.getPageNumber()<=0 ? 0: pageable.getPageNumber()-1,
				pageable.getPageSize(),Sort.by(sorts));
		
		return productRepository.findAll(pageable);//pageable 돌려주기
	}
	
	
	//판매자 id 로 판매자의 상품 전체리스트 셀렉	
	public List<Product> getListsById(SiteUser siteUser){
		long userid =  siteUser.getId();
		List<Product> product= productRepository.findByUserId(userid);
		
		
		return product;
	}
	
	//상품번호만으로 상품에대한 detail 조회하기 
	public Product getProductDetailByNo(long productNo){
		
		Optional<Product> product= productRepository.findById(productNo);
		
		
		if(product.isPresent()) {
			return product.get();
		}else {
			throw new DataNotFoundException("상세정보가 존재하지 않아요!");
		}
		
	}
	
	//소연언니
	@Transactional
    public void registerProduct(ProductForm productForm, Product product, Long userId) {
       
		product.setPname(productForm.getPname());
		product.setContent(productForm.getContent());
		product.setPrice(productForm.getPrice());
		product.setCategory(productForm.getCategory());
		product.setStock(productForm.getStock());
		product.setSelling(true);
		
        // 사용자 정보 설정 (글 작성시 product 테이블에 사용자 id 값도 넣기)
        SiteUser user = new SiteUser();
        user.setId(userId);
        product.setUser(user);
        
        // 상품 등록
        productRepository.save(product);
    }
	
	//상품 출력
	//public List<Product> getAllProducts() {
        // 모든 상품을 가져오는 로직
    //    return productRepository.findAll();
    //}
	
	//Sale 출력
	public int getSaleCount(Long id) {
	    List<Product> saleProduct = productRepository.findSaleProductsByUserId(id);
	    return saleProduct.size();
	}
	
	//Soldout 출력
	public int getSoldoutCount() {
		List<Product> soldoutProduct = productRepository.findByStockEquals(0);
		return soldoutProduct.size();
	}
	
	public Page<Product> getProductsPaged(Long userId, int pageNum, int itemsPerPage) {
        Pageable pageable = PageRequest.of(pageNum - 1, itemsPerPage);
        // 사용자 ID를 사용하여 해당 사용자가 등록한 상품만 가져오도록 변경
        return productRepository.findByUserId(userId, pageable);
    }
	
	public Page<Product> getSaleProductsPaged(Long userId,int pageNum, int itemsPerPage) {
	    Pageable pageable = PageRequest.of(pageNum - 1, itemsPerPage);
	    return productRepository.findByUserIdAndStockGreaterThan(userId, 0, pageable);
	}

	public Page<Product> getSoldoutProductsPaged(Long userId, int pageNum, int itemsPerPage) {
	    Pageable pageable = PageRequest.of(pageNum - 1, itemsPerPage);
	    return productRepository.findByUserIdAndStockEquals(userId, 0, pageable);
	}

	public long getTotalItemCount(Long userId) {
	    return productRepository.countByUserId(userId);
	}
    
    public Product getProductById(Long id) {
        // 상품 ID를 사용하여 데이터베이스에서 상품 가져오기
        return productRepository.findById(id).orElse(null);
    }
    
    @Transactional
    public void updateProduct(Long id, ProductForm productForm) {
        Product existingProduct = productRepository.findById(id).orElse(null);

        if (existingProduct != null) {
        	existingProduct.setPname(productForm.getPname());
        	existingProduct.setContent(productForm.getContent());
        	existingProduct.setPrice(productForm.getPrice());
        	existingProduct.setCategory(productForm.getCategory());
        	existingProduct.setStock(productForm.getStock());

            // 이미지 업로드 및 이미지 경로 가져오기
            List<String> imagePaths = uploadImages(productForm.getImages());

            // 이미지 경로 업데이트
            existingProduct.setImage(imagePaths.size() >= 1 ? imagePaths.get(0) : null);
            existingProduct.setImage1(imagePaths.size() >= 2 ? imagePaths.get(1) : null);
            existingProduct.setImage2(imagePaths.size() >= 3 ? imagePaths.get(2) : null);
            existingProduct.setImage3(imagePaths.size() >= 4 ? imagePaths.get(3) : null);

            productRepository.save(existingProduct);
        }
    }

    private List<String> uploadImages(List<MultipartFile> images) {
        List<String> imagePaths = new ArrayList<>();
        
        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                String imagePath = saveImage(image);
                if (imagePath != null) {
                    imagePaths.add(imagePath);
                }
            }
        }
        return imagePaths;
    }
    
    public String saveImage(MultipartFile image) {
        String uploadDirectory = "src/main/resources/static/product";

        String originalFilename = image.getOriginalFilename();

        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename;

        try {
            Files.createDirectories(Paths.get(uploadDirectory));
            Files.copy(image.getInputStream(), Paths.get(uploadDirectory, uniqueFileName), StandardCopyOption.REPLACE_EXISTING);
            return uniqueFileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	
	//희주님 꺼 

	public Product getProductByIdnormal(Long id) {

		Product product = productRepository.findById(id).get();
		return product;
	}
	
	
	public void updateProductStock(List<ItemDataForm> list) {
		
		for(ItemDataForm productData : list) {
			Long id = productData.getId();
			
			Product product = productRepository.findById(id).get();
			int newstock = product.getStock()-productData.getCount();
			product.setStock(newstock);
			
			productRepository.save(product);
		}
		
	}
	
	public List<OrderResultForm> getResultForm(List<OrderList> orderLists){
		List<OrderResultForm> lists = new ArrayList<OrderResultForm>();
		
		for (OrderList OrderListData : orderLists) {
			OrderResultForm orderResultForm = new OrderResultForm();
			SiteUser seller = userRepository.findById(OrderListData.getSellerid()).get();
			Product product = productRepository.findById(OrderListData.getProductno()).get();
			orderResultForm.setOrderNo(OrderListData.getOrderNo());
			orderResultForm.setProductName(product.getPname());
			orderResultForm.setImage(product.getImage());
			orderResultForm.setSellerId(seller.getUserName());
			orderResultForm.setCount(OrderListData.getCount());
			orderResultForm.setPrice(OrderListData.getPrice());
			
			lists.add(orderResultForm);
		}
		
		return lists;
	}
	
	//검색기능을 포함한 상품 리스트(검색 방식에 따라 sorting 다르게 적용 & 카테고리 또는 제품명으로 검색)
	public PageResultDTO<ProductDTO, Product> getSearchList(PageRequestDTO requestDTO, String sort) {
	    
		Pageable pageable;
		
		if("priceAsc".equals(sort)) { //낮은 가격 순			
			pageable = requestDTO.getPageable(Sort.by("price").ascending());
		}else if("priceDesc".equals(sort)){ //높은 가격 순		
			pageable = requestDTO.getPageable(Sort.by("price").descending());
		}else if("category".equals(sort)) { //카테고리순
			pageable = requestDTO.getPageable(Sort.by("category").ascending());
		}else { //기본값은 신상품순
			pageable = requestDTO.getPageable(Sort.by("date").descending());
		}
	    
		//Specification을 활용한 검색
	    Specification<Product> spec = ProductSpecification.isGreaterThanZero();
	    	  
	    spec = spec.and(ProductSpecification.hasKeyword(requestDTO.getKeyword()));

	    Page<Product> result = productRepository.findAll(spec, pageable);
	    
	    Function<Product, ProductDTO> fn = (entity -> entityToDto(entity));
	    
	    return new PageResultDTO<>(result, fn);
	}
	
	//베스트 상품 리스트(검색 방식에 따라 sorting 다르게 적용 & 카테고리 또는 제품명으로 검색)
	public PageResultDTO<ProductDTO, Product> getBestProducts(List<Long> productIdList, 
															PageRequestDTO requestDTO, String sort) {
	    
		Pageable pageable;
		
		if("priceAsc".equals(sort)) { //낮은 가격 순			
			pageable = requestDTO.getPageable(Sort.by("price").ascending());
		}else if("priceDesc".equals(sort)){ //높은 가격 순		
			pageable = requestDTO.getPageable(Sort.by("price").descending());
		}else if("category".equals(sort)) { //카테고리순
			pageable = requestDTO.getPageable(Sort.by("category").ascending());
		}else { //기본값은 신상품순
			pageable = requestDTO.getPageable(Sort.by("date").descending());
		}
	    
		//productIdList에 있는 Id값과 일치하는 데이터 찾기
	    Page<Product> result = productRepository.findByIdIn(productIdList, pageable);
	    
	    Function<Product, ProductDTO> fn = (entity -> entityToDto(entity));
	    
	    return new PageResultDTO<>(result, fn);
	}
	
	// 가장 최근 등록한 8개 제품 데이터 가져오기
	public List<Product> getTop8NewestProducts() {
	    
		return productRepository.findTop8ByOrderByDateDesc();
		
	}
	
	//관심사랑 일치하는 제품 데이터 가져오기
	public List<Product> getProductsByCategory(String userInterest) {
	    
		return productRepository.findAllByCategory(userInterest);
		
	}
	
	// 받아온 idList로 판매량이 높은 상품 데이터 가져오기
	public List<Product> getTopNSellingProducts(List<Long> productIdList) {
	    
	    // 제품 ID에 해당하는 Product 엔터티 리스트 조회
	    List<Product> products = productRepository.findByIdIn(productIdList);
	    
	    // 받아온 idList의 순서대로 Product 엔터티 리스트 정렬
	    products.sort(Comparator.comparingInt(p -> productIdList.indexOf(p.getId())));
	    
	    return products;
	}
	
	public Product dtoToEntity(ProductDTO dto){
		Product entity = Product.builder()
                .id(dto.getId())
                .category(dto.getCategory())
                .pname(dto.getPname())
                .content(dto.getContent())
                .price(dto.getPrice())
                .date(dto.getDate())
                .stock(dto.getStock())
                .selling(dto.isSelling())
                .image(dto.getImage())
                .image1(dto.getImage1())
                .image2(dto.getImage2())
                .image3(dto.getImage3())
                .build();
        return entity;
    }
	
	public ProductDTO entityToDto(Product entity){

		ProductDTO dto = ProductDTO.builder()
				.id(entity.getId())
                .category(entity.getCategory())
                .pname(entity.getPname())
                .content(entity.getContent())
                .price(entity.getPrice())
                .date(entity.getDate())
                .stock(entity.getStock())
                .selling(entity.isSelling())
                .image(entity.getImage())
                .image1(entity.getImage1())
                .image2(entity.getImage2())
                .image3(entity.getImage3())
                .build();

        return dto;
    }
}
	
	
	

