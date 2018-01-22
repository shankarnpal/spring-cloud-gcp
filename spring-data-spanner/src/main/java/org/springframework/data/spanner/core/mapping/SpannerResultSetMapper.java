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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.cloud.spanner.ResultSet;

import org.springframework.beans.BeanUtils;

/**
 * @author Ray Tsang
 */
public class SpannerResultSetMapper {
	private final SpannerStructObjectMapper objectMapper;

	public SpannerResultSetMapper(SpannerStructObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public <T> void map(ResultSet resultSet, Class<T> entityClass, List<T> target) {
		while (resultSet.next()) {
			T object = BeanUtils.instantiate(entityClass);
			this.objectMapper.map(resultSet.getCurrentRowAsStruct(), object);
			target.add(object);
		}
	}

	public <T> List<T> mapToUnmodifiableList(ResultSet resultSet, Class<T> entityClass) {
		ArrayList<T> result = new ArrayList<T>();
		this.map(resultSet, entityClass, result);
		return Collections.unmodifiableList(result);
	}

}
