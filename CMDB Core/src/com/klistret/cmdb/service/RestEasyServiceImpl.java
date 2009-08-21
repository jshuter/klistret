package com.klistret.cmdb.service;

public class RestEasyServiceImpl implements RestEasyService {

	public String getHello() {
		// TODO Auto-generated method stub
		return "hello";
	}

	public Bubble getBubble() {
		Bubble bubble = new Bubble();
		bubble.setWater("wet");
		bubble.setPlastic("acme");

		return bubble;
	}
}
