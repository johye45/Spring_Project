package com.koreait.petshop.model.common;

import lombok.Data;

//json을 비동기방식으로 전송할때, result, msg, url을 정의한다
@Data
public class MessageData {
	private int resultCode; //실패 성공 여부 코드
	private String msg;//클라이언트가 보게될 메시지
	private String url;//새롭게 됴청 페이지가 있다면 
}
