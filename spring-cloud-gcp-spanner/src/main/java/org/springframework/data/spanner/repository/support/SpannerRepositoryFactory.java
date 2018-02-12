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

package org.springframework.data.spanner.repository.support;

import java.util.Optional;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.spanner.core.SpannerOperations;
import org.springframework.data.spanner.core.mapping.BasicSpannerPersistentEntity;
import org.springframework.data.spanner.core.mapping.SpannerMappingContext;
import org.springframework.data.spanner.core.mapping.SpannerPersistentEntity;
import org.springframework.data.spanner.repository.query.SpannerQueryLookupStrategy;

/**
 * @author Ray Tsang
 * @author Chengyuan Zhao
 */
public class SpannerRepositoryFactory extends RepositoryFactorySupport {
	private final SpannerOperations operations;
	private final SpannerMappingContext mappingContext;

	public SpannerRepositoryFactory(SpannerOperations operations) {
		this.operations = operations;
		this.mappingContext = operations.getMappingContext();
	}

	@Override
	public <T, ID> EntityInformation<T, ID> getEntityInformation(Class<T> domainClass) {
		SpannerPersistentEntity<?> entity = this.mappingContext
				.getPersistentEntity(domainClass);

		if (entity == null) {
			throw new MappingException(String.format(
					"Could not lookup mapping metadata for domain class %s!",
					domainClass.getName()));
		}

		BasicSpannerPersistentEntity<T> persistentEntity = (BasicSpannerPersistentEntity<T>) this.mappingContext
				.getPersistentEntity(domainClass);
		return new MappingSpannerEntityInformation(persistentEntity);
	}

	@Override
	protected Object getTargetRepository(RepositoryInformation information) {
		SpannerEntityInformation<?, ?> entityInformation = (SpannerEntityInformation) getEntityInformation(
				information.getDomainType());
		return getTargetRepositoryViaReflection(information, entityInformation,
				this.operations);
	}

	@Override
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(Key key,
			EvaluationContextProvider evaluationContextProvider) {
		return Optional.of(new SpannerQueryLookupStrategy(key, evaluationContextProvider, this.operations));
	}

	@Override
	protected Class<?> getRepositoryBaseClass(RepositoryMetadata repositoryMetadata) {
		return SimpleSpannerRepository.class;
	}
}
