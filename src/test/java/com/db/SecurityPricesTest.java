package com.db;

import com.acme.mytrader.execution.ExecutionService;
import com.acme.mytrader.listener.DefaultPriceListener;
import com.acme.mytrader.price.PriceSource;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.FieldSetter;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class SecurityPricesTest {

	@Mock
	PriceSource priceSource;
	@Mock
	ExecutionService executionService;
	@Mock
	SecurityPrices securityPrices;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_GivenAnUpdateToASecurityPrice_WhenTheUpdateIsApplied_ThenNotifyPriceListeners() throws NoSuchFieldException {
		DefaultPriceListener priceListener = Mockito.mock(DefaultPriceListener.class);
		List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();
		listeners.add(priceListener);

		FieldSetter.setField(securityPrices, SecurityPrices.class.getDeclaredField("securityPriceListeners"), listeners);
		FieldSetter.setField(priceListener, DefaultPriceListener.class.getDeclaredField("executionService"), executionService);
		FieldSetter.setField(priceListener, DefaultPriceListener.class.getDeclaredField("securityPrices"), securityPrices);
		Mockito.doCallRealMethod().when(priceListener).priceUpdate(Mockito.anyString(), Mockito.anyDouble());

		priceSource.addPriceListener(priceListener);
		priceListener.priceUpdate("IBM", 50.0); //Price source updates the listener

		Mockito.verify(securityPrices, Mockito.atLeastOnce()).update(Mockito.anyString(), Mockito.anyDouble());
	}
}
