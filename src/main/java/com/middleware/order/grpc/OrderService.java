package com.middleware.order.grpc;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.middleware.order.event.producer.EventProducer;
import com.middleware.order.proto.Order.OrderRequest;
import com.middleware.order.proto.Order.OrderResponse;
import com.middleware.order.proto.OrderServiceGrpc.OrderServiceImplBase;
import com.middleware.order.util.FileUtils;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class OrderService extends OrderServiceImplBase{
	
	@Value("${rabbitmq.place.order.exchange.name}")
    private String placeOrderExchange;
	
	@Value("${rabbitmq.place.order.queue.name}")
    private String placeOrderQueue;
	
	@Value("${rabbitmq.update.order.exchange.name}")
    private String updateOrderExchange;
	
	@Value("${rabbitmq.update.order.queue.name}")
    private String updateOrderQueue;

	@Value("${rabbitmq.routing.key}")
    private String routingKey;
	
	@Autowired
	FileUtils fileUtils;
	
	@Autowired
	EventProducer eventProducer;
	
	@Override
	public void placeOrder(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
		System.out.println("Request received from client:\n" + request);
		
		//save in json
		String status = fileUtils.saveData(request);
		
        String message = new StringBuilder().append(status)
        	.append(" for product id - ")
            .append(request.getProductId())
            .toString();

        OrderResponse response = OrderResponse.newBuilder()
            .setMessage(message)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        
        //send notification
        eventProducer.publishEvent(placeOrderExchange, "fanout", routingKey, message);
	}
	
	@Override
	public void updateOrder(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
		System.out.println("Request received from client:\n" + request);

		//save in json
		String status = fileUtils.updateData(request);
		
        String message = new StringBuilder().append(status)
        	.append(" for product id - ")
            .append(request.getProductId())
            .toString();

        OrderResponse response = OrderResponse.newBuilder()
            .setMessage(message)
            .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        
        //send notification
      //send notification
        eventProducer.publishEvent(updateOrderExchange, "topic", routingKey, message);
	}

}
