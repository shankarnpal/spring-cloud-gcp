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

package org.springframework.cloud.gcp.core;

import java.io.ByteArrayInputStream;

import com.google.api.gax.core.CredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author João André Martins
 */
public class DefaultPcfConfiguration implements PcfConfiguration {

	private static final Log LOGGER = LogFactory.getLog(DefaultPcfConfiguration.class);

	private JsonObject configurationJsonObject;

	public DefaultPcfConfiguration(String jsonConfiguration) {
		this.configurationJsonObject = new JsonParser().parse(jsonConfiguration).getAsJsonObject();
	}

	@Override
	public CredentialsProvider getStorageCredentialsProvider() {
		if (this.configurationJsonObject.has("google-storage")) {
			JsonElement googleStorage = this.configurationJsonObject.get("google-storage");
			if (googleStorage.isJsonArray()	&& ((JsonArray) googleStorage).size() > 0) {
				JsonElement jsonCredentialsElement = ((JsonArray) googleStorage).get(0);
				if (jsonCredentialsElement.isJsonObject()
						&& ((JsonObject) jsonCredentialsElement).has("PrivateKeyData")) {
					JsonObject jsonCredentials = (JsonObject) jsonCredentialsElement;
					LOGGER.info("Pivotal Cloud Foundry credentials for "
							+ "Google Cloud Storage found: " + jsonCredentials.get("Name"));
					return () -> GoogleCredentials.fromStream(new ByteArrayInputStream(
							jsonCredentials.get("PrivateKeyData").getAsString().getBytes()));
				}
			}
		}

		return null;
	}

	@Override
	public CredentialsProvider getPubSubCredentialsProvider() {
		return null;
	}

	@Override
	public CredentialsProvider getCloudSqlCredentialsProvider() {
		return null;
	}
}
