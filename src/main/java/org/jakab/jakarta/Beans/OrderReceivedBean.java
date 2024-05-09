package org.jakab.jakarta.Beans;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.jakab.jakarta.domain.MenuItem;
import org.jakab.jakarta.domain.Order;
import org.jakab.jakarta.service.OrderService;
import org.jakab.jakarta.service.SessionHandlerFactory;
import org.jakab.jakarta.service.SessionHandlerService;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;


@Named("orderReceived")
@RequestScoped
public class OrderReceivedBean {

    private Map<Integer, String> itemQuantities;

    @Inject
    private OrderService service;

    @Inject
    private SessionHandlerService handler;

    @PostConstruct
    public void init() {
        handler = SessionHandlerFactory.getHandler();
        itemQuantities = new HashMap<>();
    }



    public String placeOrder() {
        // Convert itemQuantities to JSON format
        String items = convertToJson(itemQuantities); // Use a method to convert Integer keys to Long
        System.out.println("Order JSON: " + items);

        try {
            // Make an HTTP POST request to the microservice endpoint
            URL url = new URL("http://localhost:9001/orders");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = items.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Get the response code (optional)
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Successfully placed order in microservice
            } else {
                // Handle unsuccessful response
            }
        } catch (Exception e) {
            // Handle exception
        }

        // Proceed with your existing logic
        Order order = service.placeOrder(itemQuantities);
        handler.newOrder(order);

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute("orderId", order.getId());

        return "thankYou?faces-redirect=true";
    }

    public String convertToJson(Map<Integer, String> itemQuantities) {
        Map<String, Map<Integer, String>> jsonMap = new HashMap<>();
        Map<Integer, String> filteredItems = new HashMap<>();

        // Filter items with quantity > 0
        for (Map.Entry<Integer, String> entry : itemQuantities.entrySet()) {
            Integer itemId = entry.getKey();
            String quantity = entry.getValue();
            if (!quantity.isEmpty() && Integer.parseInt(quantity) > 0) {
                filteredItems.put(itemId, quantity);
            }
        }

        jsonMap.put("items", filteredItems);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(jsonMap);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }





    public Map<Integer, String> getItemQuantities() {
        return itemQuantities;
    }

    public void setItemQuantities(Map<Integer, String> itemQuantities) {
        this.itemQuantities = itemQuantities;
    }
}