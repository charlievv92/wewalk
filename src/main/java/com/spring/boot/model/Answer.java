package com.spring.boot.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Answer {
	// wewalk
	// 문의답변
	@Id // primaryKey.
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer Id;// 답변글 번호

	@ManyToOne
	private Question question;

	@ManyToOne
	private SiteUser user;// 리뷰작성자 =판매자 >> fk 여부 고민중

	private String content;

	private LocalDateTime date;

}
