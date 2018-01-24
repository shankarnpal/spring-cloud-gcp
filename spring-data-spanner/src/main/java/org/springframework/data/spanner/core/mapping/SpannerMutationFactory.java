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

package org.springframework.data.spanner.core.mapping;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.cloud.Date;
import com.google.cloud.Timestamp;
import com.google.cloud.spanner.Key;
import com.google.cloud.spanner.KeySet;
import com.google.cloud.spanner.Mutation;
import com.google.cloud.spanner.ValueBinder;

import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.PropertyHandler;

/**
 * Factory class that creates Spanner mutation operation objects.
 *
 * @author Ray Tsang
 * @author Chengyuan Zhao
 */
public class SpannerMutationFactory {
	private final SpannerMappingContext mappingContext;

	/**
	 * Constructor
	 * @param mappingContext The mapping context that will track the entities' metadata as it changes
	 * via mutation operations.
	 */
	public SpannerMutationFactory(SpannerMappingContext mappingContext) {
		this.mappingContext = mappingContext;
	}

	/**
	 * Store's a single object in Spanner.
	 * @param object The object to store.
	 * @param <T> The object's type.
	 * @return The mutation operation which will store the object.
	 */
	public <T> Mutation insert(T object) {
		return createMutation(Mutation.Op.INSERT, object);
	}

	/**
	 * Updates or inserts a single object in Spanner. The columns' values corresponding to the
	 * object's fields are treated according to Mutation.Op.INSERT_OR_UPDATE.
	 * @param object The object to update or newly insert.
	 * @param <T> The object's type.
	 * @return The mutation operation to perform the action.
	 */
	public <T> Mutation upsert(T object) {
		return createMutation(Mutation.Op.INSERT_OR_UPDATE, object);
	}

	/**
	 * Replaces a single object in Spanner. The columns' values corresponding to the object's fields
	 * are treated according to Mutation.Op.REPLACE.
	 * @param object The object to store.
	 * @param <T> The object's type.
	 * @return The mutation operation to perform this action.
	 */
	public <T> Mutation replace(T object) {
		return createMutation(Mutation.Op.REPLACE, object);
	}

	/**
	 * Updates a single object in Spanner. The columns' values corresponding to the
	 * object's fields are treated according to Mutation.Op.UPDATE.
	 * @param object The object to update.
	 * @param <T> The object's type.
	 * @return The mutation operation to perform the action.
	 */
	public <T> Mutation update(T object, String... properties) {
		return createMutation(Mutation.Op.UPDATE, object, properties);
	}

	/**
	 * Deletes several objects from Spanner.
	 * @param entityClass The type of the objects to delete.
	 * @param entities A list of objects to delete. Each object can be a subtype of entityClass.
	 * @param <T> The type of object to delete.
	 * @return The delete mutation.
	 */
	public <T> Mutation delete(Class<T> entityClass, Iterable<? extends T> entities) {
		final BasicSpannerPersistentEntity<?> persistentEntity = this.mappingContext
				.getPersistentEntity(entityClass);
		KeySet.Builder builder = KeySet.newBuilder();
		for (T entity : entities) {
			final PersistentPropertyAccessor accessor = persistentEntity
					.getPropertyAccessor(entity);
			SpannerPersistentProperty idProperty = persistentEntity.getIdProperty();
			Object value = accessor.getProperty(idProperty);
			builder.addKey(Key.of(value));
		}
		return Mutation.delete(persistentEntity.tableName(), builder.build());
	}

	/**
	 * Deletes a single object from Spanner.
	 * @param object The object to delete.
	 * @param <T> The type of the object to delete.
	 * @return The delete mutation.
	 */
	public <T> Mutation delete(T object) {
		final Class<?> entityType = object.getClass();
		final BasicSpannerPersistentEntity<?> persistentEntity = this.mappingContext
				.getPersistentEntity(object.getClass());
		final PersistentPropertyAccessor accessor = persistentEntity
				.getPropertyAccessor(object);

		final SpannerPersistentProperty idProperty = persistentEntity.getIdProperty();
		Class<?> propertyType = idProperty.getType();
		Object value = accessor.getProperty(idProperty);
		Key key = Key.of(value);

		final Mutation mutation = Mutation.delete(persistentEntity.tableName(), key);
		return mutation;
	}

	protected <T> Mutation createMutation(Mutation.Op op, T object, String... properties) {
		final Set<String> includeProperties = new HashSet<>(Arrays.asList(properties));
		final Class<?> entityType = object.getClass();
		final BasicSpannerPersistentEntity<?> persistentEntity = this.mappingContext
				.getPersistentEntity(object.getClass());
		final Mutation.WriteBuilder writeBuilder = writeBuilder(op,
				persistentEntity.tableName());
		final PersistentPropertyAccessor accessor = persistentEntity
				.getPropertyAccessor(object);
		persistentEntity
				.doWithProperties(new PropertyHandler<SpannerPersistentProperty>() {
					@Override
					public void doWithPersistentProperty(
							SpannerPersistentProperty spannerPersistentProperty) {
						if (!spannerPersistentProperty.isIdProperty()
								&& op == Mutation.Op.UPDATE && !includeProperties
										.contains(spannerPersistentProperty.getName())) {
							return;
						}
						Object value = accessor.getProperty(spannerPersistentProperty);
						Class<?> propertyType = spannerPersistentProperty.getType();
						ValueBinder<Mutation.WriteBuilder> set = writeBuilder
								.set(spannerPersistentProperty.getColumnName());
						if (String.class.isAssignableFrom(propertyType)) {
							set.to((String) value);
						}
						else if (Boolean.class.isAssignableFrom(propertyType)) {
							set.to((Boolean) value);
						}
						else if (Date.class.isAssignableFrom(propertyType)) {
							set.to((Date) value);
						}
						else if (Double.class.isAssignableFrom(propertyType)) {
							set.to((Double) value);
						}
						else if (Long.class.isAssignableFrom(propertyType)) {
							set.to((Long) value);
						}
						else if (Timestamp.class.isAssignableFrom(propertyType)) {
							set.to((Timestamp) value);
						}
						else {
							throw new SpannerDataException(
									String.format("Unsupported mapping for type: %s",
											value.getClass()));
						}
					}
				});
		return writeBuilder.build();
	}

	protected Mutation.WriteBuilder writeBuilder(Mutation.Op op, String tableName) {
		Mutation.WriteBuilder builder = null;
		switch (op) {
		case INSERT:
			builder = Mutation.newInsertBuilder(tableName);
			break;
		case INSERT_OR_UPDATE:
			builder = Mutation.newInsertOrUpdateBuilder(tableName);
			break;
		case UPDATE:
			builder = Mutation.newUpdateBuilder(tableName);
			break;
		case REPLACE:
			builder = Mutation.newReplaceBuilder(tableName);
			break;
		}
		if (builder == null) {
			throw new IllegalArgumentException("Unknown Mutation Operation: " + op);
		}
		return builder;
	}
}
