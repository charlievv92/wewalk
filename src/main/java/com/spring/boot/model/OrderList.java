package com.spring.boot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class OrderList {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long Id;

	private String orderNo;

	// Product랑 ManyToOne으로 묶으면 활용 범위가 넓어질듯
	private Long productno;

	private Long sellerid;

	@ManyToOne
	private SiteUser user;

	private int count;

	private int price;

	private String payment;

	private String name;

	private String tel;

	private String addr;

	private String zip;

	private String addr_detail;

	private String request;
}
