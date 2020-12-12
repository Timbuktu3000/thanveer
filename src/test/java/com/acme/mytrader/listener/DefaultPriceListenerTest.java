package com.acme.mytrader.listener;

import com.acme.mytrader.execution.ExecutionService;
import com.acme.mytrader.price.PriceSource;
import com.db.SecurityPrices;
import org.hamcrest.collection.IsMapContaining;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.FieldSetter;

public class DefaultPriceListenerTest {

	@Mock
	PriceSource priceSource;
	@Mock
	ExecutionService executionService;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_GivenAPriceListener_WhenAStockPriceChangeOccurs_ThenUpdateStockPrice() {
		DefaultPriceListener priceListener = new DefaultPriceListener(executionService);

		priceSource.addPriceListener(priceListener);
		priceListener.priceUpdate("IBM", 50.0); //PriceSource.class updates the listener
		Assert.assertThat(priceListener.getSecurityPrices().getSecurityPriceValues(), IsMapContaining.hasKey("IBM"));

		priceListener.priceUpdate("IBM", 51.0);
		Assert.assertThat(priceListener.getSecurityPrices().getSecurityPriceValues(), IsMapContaining.hasKey("IBM"));
		Assert.assertThat(priceListener.getSecurityPrices().getSecurityPriceValues().get("IBM"), IsEqual.equalTo(51.0));
	}

	@Test
	public void test_GivenAStock_WhenStockIsMonitored_AndPriceChangesOccurs_ThenNotifyPriceListener() throws NoSuchFieldException {
		DefaultPriceListener priceListener = Mockito.mock(DefaultPriceListener.class);
		FieldSetter.setField(priceListener, DefaultPriceListener.class.getDeclaredField("executionService"), executionService);
		FieldSetter.setField(priceListener, DefaultPriceListener.class.getDeclaredField("securityPrices"), SecurityPrices.getInstance(priceListener));
		Mockito.doCallRealMethod().when(priceListener).priceUpdate(Mockito.anyString(), Mockito.anyDouble());

		priceSource.addPriceListener(priceListener);
		priceListener.priceUpdate("IBM", 60.0);

		Mockito.verifyNoMoreInteractions(executionService);
		//Mockito.verify(priceListener, Mockito.atLeastOnce()).propertyChange(Mockito.any());
	}

	@Test
	public void test_GivenAStock_WhenABuyInstrIsReceived_AndQuantityIsSpecified_AndPriceIsBelowTriggerLevel_ThenBuyStock() {
		DefaultPriceListener priceListener = new DefaultPriceListener(executionService);

		priceSource.addPriceListener(priceListener);
		priceListener.priceUpdate("IBM", 60.0);
		Mockito.verify(executionService, Mockito.never()).buy(Mockito.anyString(), Mockito.anyDouble(), Mockito.anyInt());

		priceListener.addInstruction("IBM", DefaultPriceListener.INSTRUCTION_TYPE.BUY, 55.0, 100);
		Assert.assertTrue(priceListener.getExecutionInstructions().isPresent());
		Assert.assertTrue(priceListener.getExecutionInstructions().get().get("IBM").isPresent());
		Assert.assertTrue(priceListener.getExecutionInstructions().get().get("IBM").get().containsKey(DefaultPriceListener.INSTRUCTION_TYPE.BUY));
		Assert.assertTrue(priceListener.getExecutionInstructions().get().get("IBM").get().get(DefaultPriceListener.INSTRUCTION_TYPE.BUY).isPresent());
		Assert.assertTrue(priceListener.getExecutionInstructions().get().get("IBM").get().get(DefaultPriceListener.INSTRUCTION_TYPE.BUY).get().containsKey(55.0));
		Assert.assertEquals(100, (int) priceListener.getExecutionInstructions().get().get("IBM").get().get(DefaultPriceListener.INSTRUCTION_TYPE.BUY).get().get(55.0));

		priceListener.priceUpdate("IBM", 54.0);
		Mockito.verifyNoMoreInteractions(executionService);
		//Mockito.verify(executionService, Mockito.atLeastOnce()).buy(Mockito.anyString(), Mockito.anyDouble(), Mockito.anyInt());
	}
}
