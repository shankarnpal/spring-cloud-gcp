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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.KeySet;

import org.springframework.data.spanner.core.SpannerOperations;
import org.springframework.data.spanner.repository.SpannerRepository;

/**
 * @author Ray Tsang
 */
public class SimpleSpannerRepository<T, ID extends Serializable>
		implements SpannerRepository<T, ID> {
	private final SpannerEntityInformation<T, ID> entityInformation;
	private final SpannerOperations spannerOperations;

	public SimpleSpannerRepository(SpannerEntityInformation<T, ID> entityInformation,
			SpannerOperations spannerOperations) {
		this.entityInformation = entityInformation;
		this.spannerOperations = spannerOperations;
	}

	@Override
	public SpannerOperations getSpannerOperations() {
		return this.spannerOperations;
	}

	@Override
	public <S extends T> S save(S entity) {
		this.spannerOperations.upsert(entity);
		return entity;
	}

	@Override
	public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
		List<S> result = new ArrayList<>();
		for (S entity : entities) {
			save(entity);
		}
		return result;
	}

	@Override
	public Optional<T> findById(ID id) {
		return Optional.of(this.spannerOperations
				.find(this.entityInformation.getJavaType(), Key.of(id)));
	}

	@Override
	public boolean existsById(ID id) {
		return findById(id).isPresent();
	}

	@Override
	public Iterable<T> findAll() {
		return this.spannerOperations.findAll(this.entityInformation.getJavaType());
	}

	@Override
	public Iterable<T> findAllById(Iterable<ID> iterable) {
		KeySet.Builder builder = KeySet.newBuilder();
		for (ID id : iterable) {
			builder.addKey(Key.of(id));
		}

		return this.spannerOperations.find(this.entityInformation.getJavaType(),
				builder.build());
	}

	@Override
	public long count() {
		return this.spannerOperations.count(this.entityInformation.getJavaType());
	}

	@Override
	public void deleteById(ID id) {
		this.spannerOperations.delete(this.entityInformation.getJavaType(), Key.of(id));
	}

	@Override
	public void delete(T t) {
		this.spannerOperations.delete(t);
	}

	@Override
	public void deleteAll(Iterable<? extends T> entities) {
		this.spannerOperations.delete(this.entityInformation.getJavaType(), entities);
	}

	@Override
	public void deleteAll() {
		this.spannerOperations.delete(this.entityInformation.getJavaType(), KeySet.all());
	}
}
