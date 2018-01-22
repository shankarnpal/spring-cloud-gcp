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
package org.springframework.data.spanner.core;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.Options;
import com.google.cloud.spanner.ResultSet;
import com.google.cloud.spanner.Statement;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.TransactionRunner;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.spanner.core.mapping.BasicSpannerPersistentEntity;
import org.springframework.data.spanner.core.mapping.SpannerMappingContext;
import org.springframework.data.spanner.core.mapping.SpannerMutationFactory;
import org.springframework.data.spanner.core.mapping.SpannerStructObjectMapper;

/**
 * @author Ray Tsang
 */
public class SpannerTemplate implements SpannerOperations, ApplicationContextAware {
	private final DatabaseClient databaseClient;
	private final SpannerMappingContext mappingContext;
	private final SpannerStructObjectMapper objectMapper;
	private final SpannerMutationFactory mutationFactory;
	private final SpannerReadContextTemplate readContextTemplate;
	private ApplicationContext applicationContext;

	public SpannerTemplate(DatabaseClient databaseClient,
			SpannerMappingContext mappingContext) {
		this.databaseClient = databaseClient;
		this.mappingContext = mappingContext;

		this.objectMapper = new SpannerStructObjectMapper(mappingContext);
		this.mutationFactory = new SpannerMutationFactory(mappingContext);
		this.readContextTemplate = new SpannerReadContextTemplate(mappingContext,
				this.objectMapper);
	}

	@Override
	public SpannerMappingContext getMappingContext() {
		return this.mappingContext;
	}

	public DatabaseClient getDatabaseClient() {
		return this.databaseClient;
	}

	@Override
	public <T> T find(Class<T> entityClass, Key key) {
		return this.readContextTemplate.find(this.databaseClient.singleUse(), entityClass,
				key);
	}

	@Override
	public <T> List<T> find(Class<T> entityClass, KeySet keys,
			Options.ReadOption... options) {
		return this.readContextTemplate.find(this.databaseClient.singleUse(), entityClass,
				keys, options);
	}

	@Override
	public <T> List<T> find(Class<T> entityClass, Statement statement,
			Options.QueryOption... options) {
		return this.readContextTemplate.find(this.databaseClient.singleUse(), entityClass,
				statement, options);
	}

	@Override
	public <T> List<T> findAll(Class<T> entityClass, Options.ReadOption... options) {
		BasicSpannerPersistentEntity<?> persistentEntity = this.mappingContext
				.getPersistentEntity(entityClass);
		return this.readContextTemplate.find(this.databaseClient.singleUse(), entityClass,
				KeySet.all(), options);
	}

	@Override
	public void insert(Object object) {
		Mutation mutation = this.mutationFactory.insert(object);
		this.databaseClient.write(Arrays.asList(mutation));
	}

	@Override
	public void update(Object object, String... properties) {
		Mutation mutation = this.mutationFactory.update(object);
		this.databaseClient.write(Arrays.asList(mutation));
	}

	@Override
	public void upsert(Object object) {
		Mutation mutation = this.mutationFactory.upsert(object);
		this.databaseClient.write(Arrays.asList(mutation));
	}

	@Override
	public <T> void delete(Class<T> entityClass, Key key) {
		BasicSpannerPersistentEntity<?> persistentEntity = this.mappingContext
				.getPersistentEntity(entityClass);
		String tableName = persistentEntity.tableName();
		Mutation mutation = Mutation.delete(tableName, key);
		this.databaseClient.write(Arrays.asList(mutation));
	}

	@Override
	public void delete(Object entity) {
		Mutation mutation = this.mutationFactory.delete(entity);
		this.databaseClient.write(Arrays.asList(mutation));
	}

	@Override
	public <T> void delete(Class<T> entityClass, Iterable<? extends T> entities) {
		Mutation mutation = this.mutationFactory.delete(entityClass, entities);
		this.databaseClient.write(Arrays.asList(mutation));
	}

	@Override
	public <T> void delete(Class<T> entityClass, KeySet keys) {
		BasicSpannerPersistentEntity<?> persistentEntity = this.mappingContext
				.getPersistentEntity(entityClass);
		String tableName = persistentEntity.tableName();
		Mutation delete = Mutation.delete(tableName, keys);
		this.databaseClient.write(Arrays.asList(delete));
	}

	@Override
	public <T> long count(Class<T> entityClass) {
		BasicSpannerPersistentEntity<?> persistentEntity = this.mappingContext
				.getPersistentEntity(entityClass);
		ResultSet resultSet = this.databaseClient.singleUse().executeQuery(Statement.of(
				String.format("select count(*) from %s", persistentEntity.tableName())));
		resultSet.next();
		return resultSet.getLong(0);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void transaction(final Consumer<SpannerTransactionContext> unitOfWork) {
		final SpannerMappingContext mappingContext = this.mappingContext;
		final SpannerMutationFactory mutationFactory = this.mutationFactory;
		final SpannerReadContextTemplate readContextTemplate = this.readContextTemplate;
		this.databaseClient.readWriteTransaction()
				.run(new TransactionRunner.TransactionCallable<Void>() {
					@Nullable
					@Override
					public Void run(TransactionContext transactionContext)
							throws Exception {
						SpannerTransactionContext ctx = new SpannerTransactionContext(
								transactionContext, readContextTemplate, mappingContext,
								mutationFactory);
						unitOfWork.accept(ctx);
						return null;
					}
				});
	}
}
