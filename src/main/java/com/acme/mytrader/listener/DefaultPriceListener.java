package com.acme.mytrader.listener;

import com.acme.mytrader.execution.ExecutionService;
import com.acme.mytrader.price.PriceListener;
import com.db.SecurityPrices;
import lombok.Getter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
public class DefaultPriceListener implements PriceListener, PropertyChangeListener {

	public enum INSTRUCTION_TYPE {
		BUY,
		SELL
	}

	private ExecutionService executionService;
	private Optional<Map<String, //IBM
				Optional<Map<INSTRUCTION_TYPE, //buy
					Optional<Map<Double, Integer>> //@ 55.0, 100 lots
						>>>> executionInstructions;
	private SecurityPrices securityPrices;

	public DefaultPriceListener(ExecutionService executionService) {
		this.executionService = executionService;
		this.executionInstructions = Optional.of(new HashMap<>());
		this.securityPrices = SecurityPrices.getInstance(this);
	}
	public void addInstruction(String security, INSTRUCTION_TYPE instruction, double price, int quantity) {
		Optional<Map<INSTRUCTION_TYPE, Optional<Map<Double, Integer>>>> securityInstructions;
		Optional<Map<Double, Integer>> instructionLimitAndQuantity;

		securityInstructions = executionInstructions.orElseGet(HashMap::new).getOrDefault(security, Optional.of(new HashMap<>()));
		instructionLimitAndQuantity = securityInstructions.orElseGet(HashMap::new).getOrDefault(instruction, Optional.of(new HashMap<>()));

		instructionLimitAndQuantity.orElseGet(HashMap::new).put(price, quantity);
		securityInstructions.orElse(new HashMap<>()).put(instruction, instructionLimitAndQuantity);
		executionInstructions.orElse(new HashMap<>()).put(security, securityInstructions);
	}

	@Override
	public void priceUpdate(String security, double price) {
		securityPrices.update(security, price);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		 executionInstructions
			.flatMap(security -> security.get(event.getPropertyName()))
			.flatMap(instruction -> {
				instruction.forEach((instructionType, value) ->
					value.flatMap(limitAndQuantity -> {
						limitAndQuantity.forEach((limit, quantity) -> {
							if (limit > (Double)event.getNewValue() && instructionType.equals(INSTRUCTION_TYPE.BUY))
								executionService.buy(event.getPropertyName(), (double) event.getNewValue(), quantity);
						});
						return Optional.empty();
					})
				);
				return Optional.empty();
			});
	}
}
