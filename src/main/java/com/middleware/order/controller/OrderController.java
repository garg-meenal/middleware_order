package com.middleware.order.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.middleware.order.model.Order;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

//THIS CONTROLLER CLASS IS TO TEST SAVE AND UPDATE DATA IN JSON FUNCTIONALITIES

@RestController
@RequestMapping("/order")
public class OrderController {
	
	@Autowired
	ObjectMapper objectMapper;
	
	@Autowired
	ResourceLoader resourceLoader;
	
	@Value("${rabbitmq.place.order.queue.name}")
    private String queue;
	
	@Value("${rabbitmq.place.order.exchange.name}")
    private String exchange;

	 
	@GetMapping
	public List<Order> getFileData() {
		try {
			Resource resource = resourceLoader.getResource("classpath:products.json");
			TypeReference<List<Order>> typeReference = new TypeReference<List<Order>>(){};
			return objectMapper.readValue(resource.getInputStream(),typeReference);
		} catch (IOException e) {
			return null;
		}
	}
	
	@PutMapping
	public String updateData(@RequestBody Order order) {
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
	
	@PostMapping
	public String saveData(@RequestBody Order order){
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
}
