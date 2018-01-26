/*
 *  Copyright 2018 original author or authors.
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

package org.springframework.data.spanner.core.mapping;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import org.springframework.data.mapping.model.FieldNamingStrategy;
import org.springframework.data.mapping.model.PropertyNameFieldNamingStrategy;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Chengyuan Zhao
 */
@RunWith(SpringRunner.class)
public class SpannerMappingContextTests {

	@Test
	public void testNullSetFieldNamingStrategy() {
		SpannerMappingContext context = new SpannerMappingContext();

		context.setFieldNamingStrategy(null);
		Assert.assertEquals(PropertyNameFieldNamingStrategy.INSTANCE,
				context.getFieldNamingStrategy());
	}

	@Test
	public void testSetFieldNamingStrategy() {
		SpannerMappingContext context = new SpannerMappingContext();
		FieldNamingStrategy strat = Mockito.mock(FieldNamingStrategy.class);
		context.setFieldNamingStrategy(strat);
		Assert.assertSame(strat, context.getFieldNamingStrategy());
	}
}