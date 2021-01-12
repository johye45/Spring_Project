package com.koreait.petshop.controller.payment;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;


import com.koreait.petshop.exception.CartException;
import com.koreait.petshop.exception.LoginRequiredException;
import com.koreait.petshop.model.common.MessageData;
import com.koreait.petshop.model.domain.Cart;
import com.koreait.petshop.model.domain.Member;
import com.koreait.petshop.model.domain.OrderSummary;
import com.koreait.petshop.model.domain.Receiver;
import com.koreait.petshop.model.payment.service.PaymentService;
import com.koreait.petshop.model.product.service.TopCategoryService;
//�������� ���� �׷��µ� ��Ŭ��� �������� �Ǿ����� , Ȯ���غ����� jsp !!!
@Controller
public class PaymentController {
      private static final Logger logger=LoggerFactory.getLogger(PaymentController.class);
      @Autowired
      private PaymentService paymentService;
      
      @Autowired
      private TopCategoryService topCategoryService;
      
      //īƮ ��û 
      @RequestMapping(value="/shop/cart/list", method=RequestMethod.GET )
      public ModelAndView cart(HttpServletRequest request) {
    	  HttpSession session = request.getSession();
    	  Member member = (Member)session.getAttribute("member");
    	  List topList = topCategoryService.selectAll();
    	  List cartList = paymentService.selectCartList(member.getMember_id());
    	  
         ModelAndView mav = new ModelAndView();
         mav.addObject("topList", topList);
         mav.addObject("cartList", cartList);
         mav.setViewName("shop/cart/cart_list");
         return mav;
      }
      
      //��ٱ��Ͽ� ��ǰ ��� ��û 
		/*
		 * @RequestMapping(value="/shop/cart/regist", method=RequestMethod.POST)
		 * 
		 * @ResponseBody public MessageData registCart(Cart cart, HttpSession session) {
		 * if(session.getAttribute("member")==null) { throw new
		 * LoginRequiredException("�α����� �ʿ��� �����Դϴ�."); }
		 * 
		 * Member member = (Member)session.getAttribute("member");
		 * 
		 * logger.debug("product_id "+cart.getProduct_id());
		 * logger.debug("quantity "+cart.getQuantity());
		 * cart.setMember_id(member.getMember_id()); paymentService.insert(cart);
		 * 
		 * MessageData messageData = new MessageData(); messageData.setResultCode(1);
		 * messageData.setMsg("��ٱ��Ͽ� ��ǰ�� �����ϴ�"); messageData.setUrl("/shop/cart/list");
		 * 
		 * return messageData; }
		 */
      
      //��ٱ��� ���� 
      @RequestMapping(value="/shop/cart/del", method=RequestMethod.GET)
      public String delCart(HttpSession session){
         //��ٱ��ϸ� �����ϱ� ���ؼ���, �α����� ȸ���� ����..
         if(session.getAttribute("member")==null) {
            throw new LoginRequiredException("�α��� ���� ���ּ���");
         }
         paymentService.delete((Member)session.getAttribute("member"));
         return "redirect:/shop/cart/list";
      }
      
      //��ٱ��� ���� 
      @RequestMapping(value="/shop/cart/edit", method=RequestMethod.POST)
      public ModelAndView editCart(@RequestParam("cart_id") int[] cartArray, @RequestParam("quantity") int[] qArray) {
         //�Ѱܹ��� cart_id,  quantity  �Ķ���� ���
         logger.debug("cartArray length "+cartArray.length);
         List cartList = new ArrayList();
         for(int i=0;i<cartArray.length;i++) {
            Cart cart  = new Cart();
            cart.setCart_id(cartArray[i]);
            cart.setQuantity(qArray[i]);
            cartList.add(cart);
         }
         paymentService.update(cartList);
         
         //�����Ǿ����� 
         MessageData messageData = new MessageData();
         messageData.setResultCode(1);
         messageData.setMsg("��ٱ��ϰ� �����Ǿ����ϴ�");
         messageData.setUrl("/shop/cart/list");
         //�ƴϸ� �����޼���
         ModelAndView mav = new ModelAndView();
         mav.addObject("messageData", messageData);
         mav.setViewName("shop/error/message");
         return mav;
      }
      
      //üũ�ƿ� ������ ��û 
      @GetMapping("/shop/payment/form")
      public String payForm(Model model, HttpSession session) {
         List topList = topCategoryService.selectAll();
         model.addAttribute("topList", topList); //ModelAndView������ Model�� ���..
         
         //�������� �������� 
         List paymethodList = paymentService.selectPaymethodList();
         model.addAttribute("paymethodList", paymethodList);
         
         //��ٱ��� ������ �������� 
         Member member =(Member)session.getAttribute("member");
         List cartList = paymentService.selectCartList(member.getMember_id());
         model.addAttribute("cartList", cartList);
         
         return "shop/payment/checkout";
      }
      
      //������û ó��
      @PostMapping("/shop/payment/regist")
      public String pay(HttpSession session, OrderSummary orderSummary, Receiver receiver) {
         logger.debug("���� ��� �̸� "+receiver.getReceiver_name());
         logger.debug("���� ��� ����ó "+receiver.getReceiver_phone());
         logger.debug("���� ��� �ּ� "+receiver.getReceiver_addr());
         logger.debug("��������� "+orderSummary.getPaymethod_id());
         logger.debug("total_price "+orderSummary.getTotal_price());
         logger.debug("total_pay "+orderSummary.getTotal_price());
         Member member=(Member)session.getAttribute("member");
         orderSummary.setMember_id(member.getMember_id()); //ȸ�� pk
         paymentService.registOrder(orderSummary, receiver);
         return "";
      }
      
      //��ٱ��Ͽ� ���õ� ����ó�� �ڵ鷯
      @ExceptionHandler(CartException.class)
      @ResponseBody
      public MessageData handleException(CartException e) {
         logger.debug("�ڵ鷯 ������ "+ e.getMessage());
         MessageData messageData = new MessageData();
         messageData.setResultCode(0);
         messageData.setMsg(e.getMessage());   
         return messageData;
      }
      
}