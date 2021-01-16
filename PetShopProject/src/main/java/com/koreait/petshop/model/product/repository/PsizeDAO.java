package com.koreait.petshop.model.product.repository;

import java.util.List;

import com.koreait.petshop.model.domain.Psize;

public interface PsizeDAO {
	public List selectAll();	//�׳� ��� ������ ��������
	public List selectById(int product_id); //fk�� �Ҽӵ� ��� ������ ��������
	public Psize select(int psize_id);
	public void insert(Psize psize);
	public void update(Psize psize);
	public void delete(int product_id);
}