package com.middleware.order.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.middleware.order.model.Order;
import com.middleware.order.proto.Order.OrderRequest;

@Component
public class FileUtils {
	
	@Autowired
	ResourceLoader resourceLoader;
	
	@Autowired
	ObjectMapper objectMapper;
	
	public String saveData(OrderRequest request) {
		
		Order order = populateOrderData(request);
		Resource resource = resourceLoader.getResource("classpath:products.json");
		TypeReference<List<Order>> typeReference = new TypeReference<List<Order>>(){};
		List<Order> orders = new ArrayList<>();
		try {
			try {
				orders = objectMapper.readValue(resource.getInputStream(),typeReference);
			}catch(MismatchedInputException e) {
				orders = new ArrayList<>();
			}
			
			Optional<Order> dataFromFile = orders
					.stream()
					.filter(arg -> arg.getProductId() == order.getProductId())
					.findFirst();
			if(dataFromFile.isPresent()){ 
				return "Product order already exists"; 
			}
			 
            orders.add(order);
    		objectMapper.writeValue(resource.getFile(), orders);
    		return "Product order placed successfully";
        } catch (IOException e){
            e.printStackTrace();
            return "Place order operation failed";
        }
	}
	
	public String updateData(OrderRequest request) {
		Order order = populateOrderData(request);
		Resource resource = resourceLoader.getResource("classpath:products.json");
		
		TypeReference<List<Order>> typeReference = new TypeReference<List<Order>>(){};
		List<Order> orders = new ArrayList<>();
		try {
            orders = objectMapper.readValue(resource.getInputStream(),typeReference);
            Optional<Order> dataFromFile = orders.stream().filter(arg -> arg.getProductId() == order.getProductId()).findFirst();
            if(dataFromFile.isPresent()) {
            	int index = orders.indexOf(dataFromFile.get());
            	orders.set(index, order);
            	objectMapper.writeValue(resource.getFile(), orders);
            	return "Product order updated successfully";
            }else {
            	return "Product does not exists";
            }	
        } catch(MismatchedInputException e) {
        	return "Product does not exists";
        } 
		catch (IOException e){
            e.printStackTrace();
            return "Update order operation failed";
        }
	}
	
	private Order populateOrderData(OrderRequest request) {
		Order order = new Order();
		order.setId(request.getProductId());
		order.setProductId(request.getProductId());
		order.setProductName(request.getProductName());
		order.setProductColor(request.getProductColor());
		order.setProductDescription(request.getProductDescription());
		order.setProductPrice(request.getProductPrice());
		return order;
	}

}
