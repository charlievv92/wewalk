package com.spring.boot.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private SiteUser user;

	@Column(name = "type", length = 30, nullable = false)
	private String type;

	@Column(name = "receivername", length = 30, nullable = false)
	private String receiverName;

	@Column(name = "phone", length = 50, nullable = false)
	private String phone;

	@Column(name = "address01", length = 100, nullable = false)
	private String address01;

	@Column(name = "address02", length = 200, nullable = false)
	private String address02;

	@Column(name = "zipcode", length = 30, nullable = false)
	private String zipcode;

}
