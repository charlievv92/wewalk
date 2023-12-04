package com.spring.boot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebImgConfig implements WebMvcConfigurer{
	
	//로컬 서버 C:// 에 저장된 img 파일을 연결해줌 
	//imgfile.path=C:\Users\pinke\OneDrive\문서\itwillfinal\files
	@Value("${review.img}")//config에 웹소스로 핸들링해주는 값을 받음 >> config에있다 reviewimg.properties 
	private String reviewImg;
	@Value("${img.upload.directory}")
    private String imgUploadDirectory;
	
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		registry.addResourceHandler("/image/**")//위의주소로 src요청시
		.addResourceLocations("file:///C:/teamproject/files/images/");
		registry.addResourceHandler("/general/**")//위의주소로 src요청시
		.addResourceLocations("file:///C:/teamproject/files/generals/");

		//registry.addResourceHandler("/imgfile/**")//위의주소로 src요청시
		//.addResourceLocations("file:///C:/Users/pinke/OneDrive/문서/itwillfinal/files/");
		
		registry.addResourceHandler("src/main/resources/static/reviewImg/**")//위의주소로 src요청시
		.addResourceLocations(reviewImg);


		//.addResourceLocations(imgSavePath);//밑의 로컬주소로 연결
		//file:///C:/Users/pinke/OneDrive/문서/itwillfinal/files/
		
		registry.addResourceHandler("src/main/resources/static/product/**")
        .addResourceLocations(imgUploadDirectory);
	}

}
