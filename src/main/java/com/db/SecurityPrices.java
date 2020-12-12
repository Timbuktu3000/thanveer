package com.db;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

@Getter
public class SecurityPrices {

	private static Logger logger = LoggerFactory.getLogger(SecurityPrices.class);

	private static SecurityPrices securityPrices;
	private List<PropertyChangeListener> securityPriceListeners;
	private final Map<String, Double> securityPriceValues;

	private SecurityPrices() {
		securityPriceListeners = new ArrayList<>();
		securityPriceValues = new HashMap<>();
	}
	private void notifyListeners(String security, double prevPrice, double newPrice) {
		securityPriceListeners.forEach(
			securityListener -> securityListener.propertyChange(
				new PropertyChangeEvent(securityListener, security, prevPrice, newPrice))
		);
	}

	public static SecurityPrices getInstance(PropertyChangeListener listener) {
		if (securityPrices==null) {
			securityPrices = new SecurityPrices();
		}
		securityPrices.securityPriceListeners.add(listener);
		return securityPrices;
	}
	public void update(String security, double price) {
		try {
			double previousValue = securityPriceValues.getOrDefault(Objects.requireNonNull(security).toUpperCase(), 0.0);
			securityPriceValues.put(Objects.requireNonNull(security).toUpperCase(), price);
			notifyListeners(security, previousValue, price);
		} catch (NullPointerException npe) {
			logger.warn("SecurityPrices.update :: Skipping null update input values");
		}
	}
}
