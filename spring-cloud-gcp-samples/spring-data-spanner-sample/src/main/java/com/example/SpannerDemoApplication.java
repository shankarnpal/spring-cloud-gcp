/*
 *  Copyright 2017 original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.spanner.core.SpannerTemplate;
import org.springframework.data.spanner.repository.config.EnableSpannerRepositories;

/**
 * @author Ray Tsang
 * @author Chengyuan Zhao
 */
@SpringBootApplication
@EnableSpannerRepositories
public class SpannerDemoApplication implements CommandLineRunner {
	@Autowired
	SpannerTemplate spannerTemplate;

	@Autowired
	TradeRepository tradeRepository;

	@Autowired
	TraderRepository traderRepository;

	public static void main(String[] args) {
		SpringApplication.run(SpannerDemoApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		// Just for testing
		// Not the best example of an ID
		// Also, not having composite keys yet
		final String traderId = UUID.randomUUID().toString();
		this.spannerTemplate.transaction(ctx -> {
			Trader trader = new Trader();
			trader.id = traderId;
			trader.name = "Name";

			ctx.insert(trader);
			for (int i = 0; i < 5; i++) {
				String tradeId = UUID.randomUUID().toString();

				Trade t = new Trade();
				t.id = tradeId;
				t.symbol = "ABCD";
				t.action = "BUY";
				t.traderId = traderId;
				t.price = 100.0;

				ctx.insert(t);
			}
		});

		try {
			System.out.println(traderRepository.findTraderByName("Ray"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.spannerTemplate.findAll(Trade.class).stream().forEach(System.out::println);

		long count = this.tradeRepository.count();
		System.out.println("There are " + count + " records");

		Trader trader = this.traderRepository.findById(traderId).get();
		System.out.println(trader);
		this.tradeRepository.deleteAll();
		count = this.tradeRepository.count();
		System.out.println("Deleted, There are " + count + " records");
	}
}
