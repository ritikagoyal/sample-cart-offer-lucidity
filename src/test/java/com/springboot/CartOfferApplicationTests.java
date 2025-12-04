package com.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.controller.ApplyOfferRequest;
import com.springboot.controller.ApplyOfferResponse;
import com.springboot.controller.OfferRequest;
import com.springboot.controller.SegmentResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CartOfferApplicationTests {
	private ObjectMapper mapper = new ObjectMapper();
	private static final String BASE_URL = "http://localhost:9001";
	private static final String APPLY_OFFER_URL = BASE_URL + "/api/v1/cart/apply_offer";

	@Test
	public void checkFlatXForOneSegment() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(1,"FLATX",10,segments);
		boolean result = addOffer(offerRequest);
		Assert.assertEquals(result,true);
	}

	@Test
	public void flatXOfferP1SegmentBasicDiscount() throws Exception {
		List<String> segments = new ArrayList<>();
		segments.add("p1");
		OfferRequest offerRequest = new OfferRequest(1,"FLATX",10,segments);
		addOffer(offerRequest);
		int finalCartValue = applyOfferAndGetCartValue(200, 1, 1);
		Assert.assertEquals("FLATX Rs.10 off should reduce cart from 200 to 190", 190, finalCartValue);
	}

	@Test
	public void flatXOfferP2SegmentBasicDiscount() throws Exception {
		OfferRequest offerRequest = new OfferRequest(2,"FLATX",50,Arrays.asList("p2"));
		addOffer(offerRequest);
		int finalCartValue = applyOfferAndGetCartValue(500, 2, 2);
		Assert.assertEquals("FLATX Rs.50 off should reduce cart from 500 to 450", 450, finalCartValue);
	}

	@Test
	public void flatXOfferP3SegmentBasicDiscount() throws Exception {
		OfferRequest offerRequest = new OfferRequest(3,"FLATX",100,Arrays.asList("p3"));
		addOffer(offerRequest);
		int finalCartValue = applyOfferAndGetCartValue(1000, 3, 3);
		Assert.assertEquals("FLATX Rs.100 off should reduce cart from 1000 to 900", 900, finalCartValue);
	}

	@Test
	public void flatPercentOfferP1Segment10PercentOff() throws Exception {
		OfferRequest offerRequest = new OfferRequest(4,"FLAT%",10,Arrays.asList("p1"));
		addOffer(offerRequest);
		int finalCartValue = applyOfferAndGetCartValue(200, 1, 4);
		Assert.assertEquals("FLAT% 10% off should reduce cart from 200 to 180", 180, finalCartValue);
	}

	@Test
	public void flatPercentOfferP2Segment20PercentOff() throws Exception {
		OfferRequest offerRequest = new OfferRequest(5, "FLAT%", 20, Arrays.asList("p2"));
		addOffer(offerRequest);
		int finalCartValue = applyOfferAndGetCartValue(1000, 2, 5);
		Assert.assertEquals("FLAT% 20% off should reduce cart from 1000 to 800", 800, finalCartValue);
	}

	@Test
	public void flatPercentOfferP3Segment50PercentOff() throws Exception {
		OfferRequest offerRequest = new OfferRequest(6, "FLAT%", 50, Arrays.asList("p3"));
		addOffer(offerRequest);
		int finalCartValue = applyOfferAndGetCartValue(500, 3, 6);
		Assert.assertEquals("FLAT% 50% off should reduce cart from 500 to 250", 250, finalCartValue);
	}

	@Test
	public void offerForMultipleSegments() throws Exception {
		OfferRequest offerRequest = new OfferRequest(7, "FLATX", 30, Arrays.asList("p1", "p2"));
		addOffer(offerRequest);
		int cartValueP1 = applyOfferAndGetCartValue(300, 1, 7);
		Assert.assertEquals("P1 user should get Rs.30 off", 270, cartValueP1);
	}

	@Test
	public void offerForMultipleSegmentsP2UserGetsDiscount() throws Exception {
		OfferRequest offerRequest = new OfferRequest(8, "FLATX", 30, Arrays.asList("p1", "p2"));
		addOffer(offerRequest);
		int cartValueP2 = applyOfferAndGetCartValue(300, 2, 8);
		Assert.assertEquals("P2 user should get Rs.30 off", 270, cartValueP2);
	}

	@Test
	public void noOfferForRestaurant() throws Exception {
		int finalCartValue = applyOfferAndGetCartValue(200, 1, 99);
		Assert.assertEquals("No offer should result in unchanged cart value", 200, finalCartValue);
	}

	@Test
	public void userSegmentNotInOfferSegments() throws Exception {
		OfferRequest offerRequest = new OfferRequest(10, "FLATX", 50, Arrays.asList("p1"));
		addOffer(offerRequest);
		int finalCartValue = applyOfferAndGetCartValue(500, 2, 10);
		Assert.assertEquals("User from non-eligible segment should not get discount", 500, finalCartValue);
	}

	@Test
	public void multipleOffersDifferentSegments1() throws Exception {
		OfferRequest offerRequest1 = new OfferRequest(11, "FLATX", 50, Arrays.asList("p1"));
		addOffer(offerRequest1);
		OfferRequest offerRequest2 = new OfferRequest(11, "FLATX", 100, Arrays.asList("p2"));
		addOffer(offerRequest2);
		int finalCartValue = applyOfferAndGetCartValue(500, 1, 11);
		Assert.assertEquals("P1 user should get Rs.50 off", 450, finalCartValue);
	}

	@Test
	public void multipleOffersDifferentSegments() throws Exception {
		OfferRequest offerRequest1 = new OfferRequest(12, "FLATX", 50, Arrays.asList("p1"));
		addOffer(offerRequest1);
		OfferRequest offerRequest2 = new OfferRequest(12, "FLATX", 100, Arrays.asList("p2"));
		addOffer(offerRequest2);
		int finalCartValue = applyOfferAndGetCartValue(500, 2, 12);
		Assert.assertEquals("P2 user should get Rs.100 off", 400, finalCartValue);
	}

	@Test
	public void zeroCartValue() throws Exception {
		OfferRequest offerRequest1 = new OfferRequest(13, "FLATX", 10, Arrays.asList("p1"));
		addOffer(offerRequest1);
		int finalCartValue = applyOfferAndGetCartValue(0, 1, 13);
		Assert.assertEquals("Zero cart with Rs.10 off results in -10", -10, finalCartValue);
	}

	@Test
	public void discountGreaterThanCartValue() throws Exception {
		OfferRequest offerRequest1 = new OfferRequest(14, "FLATX", 100, Arrays.asList("p1"));
		addOffer(offerRequest1);
		int finalCartValue = applyOfferAndGetCartValue(50, 1, 14);
		Assert.assertEquals("Rs.100 off on Rs.50 cart results in -50", -50, finalCartValue);
	}

	@Test
	public void hundredPercentOff() throws Exception {
		OfferRequest offerRequest1 = new OfferRequest(15, "FLAT%", 100, Arrays.asList("p1"));
		addOffer(offerRequest1);
		int finalCartValue = applyOfferAndGetCartValue(500, 1, 15);
		Assert.assertEquals("100% off should make cart value 0", 0, finalCartValue);
	}

	@Test
	public void zeroDiscountFlatX() throws Exception {
		OfferRequest offerRequest1 = new OfferRequest(16, "FLATX", 0, Arrays.asList("p1"));
		addOffer(offerRequest1);
		int finalCartValue = applyOfferAndGetCartValue(200, 1, 16);
		Assert.assertEquals("Rs.0 discount should not change cart value", 200, finalCartValue);
	}

	@Test
	public void zeroPercentDiscount() throws Exception {
		OfferRequest offerRequest1 = new OfferRequest(17, "FLAT%", 0, Arrays.asList("p1"));
		addOffer(offerRequest1);
		int finalCartValue = applyOfferAndGetCartValue(200, 1, 17);
		Assert.assertEquals("0% discount should not change cart value", 200, finalCartValue);
	}

	@Test
	public void largeCartValueFlatPercent() throws Exception {
		OfferRequest offerRequest1 = new OfferRequest(19, "FLAT%", 25, Arrays.asList("p2"));
		addOffer(offerRequest1);
		int finalCartValue = applyOfferAndGetCartValue(10000, 2, 19);
		Assert.assertEquals("Large cart value with 25% off should be 7500", 7500, finalCartValue);
	}

	@Test
	public void smallCartValue_SmallDiscount() throws Exception {
		OfferRequest offerRequest1 = new OfferRequest(20, "FLATX", 5, Arrays.asList("p1"));
		addOffer(offerRequest1);
		int finalCartValue = applyOfferAndGetCartValue(50, 1, 20);
		Assert.assertEquals("Small cart with Rs.5 off should be 45", 45, finalCartValue);
	}

	@Test
	public void mixedOffersFlatXAndPercentP1GetsFlatX() throws Exception {
		OfferRequest offerRequest1 = new OfferRequest(21, "FLATX", 100, Arrays.asList("p1"));
		addOffer(offerRequest1);
		OfferRequest offerRequest = new OfferRequest(21, "FLAT%", 20, Arrays.asList("p2"));
		addOffer(offerRequest);
		int finalCartValue = applyOfferAndGetCartValue(1000, 1, 21);
		Assert.assertEquals("P1 user should get FLATX Rs.100 off", 900, finalCartValue);
	}

	@Test
	public void mixedOffersFlatXAndPercentP2GetsPercent() throws Exception {
		OfferRequest offerRequest1 = new OfferRequest(22, "FLATX", 100, Arrays.asList("p1"));
		addOffer(offerRequest1);
		OfferRequest offerRequest = new OfferRequest(22, "FLAT%", 20, Arrays.asList("p2"));
		addOffer(offerRequest);
		int finalCartValue = applyOfferAndGetCartValue(1000, 2, 22);
		Assert.assertEquals("P2 user should get 20% off = Rs.200 discount", 800, finalCartValue);
	}

	@Test
	public void offerForAllSegments() throws Exception {
		OfferRequest offerRequest1 = new OfferRequest(23, "FLATX", 20, Arrays.asList("p1", "p2", "p3"));
		addOffer(offerRequest1);
		int cartP1 = applyOfferAndGetCartValue(200, 1, 23);
		int cartP2 = applyOfferAndGetCartValue(200, 2, 23);
		int cartP3 = applyOfferAndGetCartValue(200, 3, 23);
		Assert.assertEquals("P1 user should get discount", 180, cartP1);
		Assert.assertEquals("P2 user should get discount", 180, cartP2);
		Assert.assertEquals("P3 user should get discount", 180, cartP3);
	}

	@Test
	public void firstMatchingOfferWins() throws Exception {
		OfferRequest offerRequest1 = new OfferRequest(28, "FLATX", 50, Arrays.asList("p1"));
		addOffer(offerRequest1);
		OfferRequest offerRequest = new OfferRequest(28, "FLATX", 100, Arrays.asList("p1"));
		addOffer(offerRequest);
		int finalCartValue = applyOfferAndGetCartValue(500, 1, 28);
		Assert.assertEquals("First matching offer should apply", 450, finalCartValue);
	}

	public boolean addOffer(OfferRequest offerRequest) throws Exception {
		String urlString = "http://localhost:9001/api/v1/offer";
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");

		ObjectMapper mapper = new ObjectMapper();

		String POST_PARAMS = mapper.writeValueAsString(offerRequest);
		OutputStream os = con.getOutputStream();
		os.write(POST_PARAMS.getBytes());
		os.flush();
		os.close();
		int responseCode = con.getResponseCode();
		System.out.println("POST Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { //success
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			// print result
			System.out.println(response.toString());
		} else {
			System.out.println("POST request did not work.");
		}
		return true;
	}

	private int applyOfferAndGetCartValue(int cartValue, int userId, int restaurantId) throws Exception {
		ApplyOfferRequest request = new ApplyOfferRequest();
		request.setCart_value(cartValue);
		request.setUser_id(userId);
		request.setRestaurant_id(restaurantId);

		URL url = new URL(APPLY_OFFER_URL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "application/json");

		String jsonBody = mapper.writeValueAsString(request);
		OutputStream os = con.getOutputStream();
		os.write(jsonBody.getBytes());
		os.flush();
		os.close();

		int responseCode = con.getResponseCode();

		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			System.out.println("Response is " + response);
			ApplyOfferResponse applyOfferResponse = mapper.readValue(response.toString(), ApplyOfferResponse.class);
			System.out.println("Applied offer: " + request + " -> Final cart: " + applyOfferResponse.getCart_value());
			return applyOfferResponse.getCart_value();
		}

		throw new Exception("Failed to apply offer, response code: " + responseCode);
	}
}
