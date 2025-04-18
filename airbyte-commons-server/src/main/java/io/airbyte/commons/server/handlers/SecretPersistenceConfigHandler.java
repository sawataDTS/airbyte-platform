/*
 * Copyright (c) 2020-2025 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.commons.server.handlers;

import io.airbyte.commons.enums.Enums;
import io.airbyte.commons.json.Jsons;
import io.airbyte.config.ScopeType;
import io.airbyte.config.SecretPersistenceConfig;
import io.airbyte.config.secrets.SecretCoordinate.AirbyteManagedSecretCoordinate;
import io.airbyte.config.secrets.SecretsRepositoryWriter;
import jakarta.inject.Singleton;
import java.util.UUID;

@Singleton
public class SecretPersistenceConfigHandler {

  private final SecretsRepositoryWriter secretsRepositoryWriter;

  public SecretPersistenceConfigHandler(final SecretsRepositoryWriter secretsRepositoryWriter) {
    this.secretsRepositoryWriter = secretsRepositoryWriter;
  }

  @SuppressWarnings("LineLength")
  public io.airbyte.api.model.generated.SecretPersistenceConfig buildSecretPersistenceConfigResponse(
                                                                                                     final SecretPersistenceConfig secretPersistenceConfig) {
    return new io.airbyte.api.model.generated.SecretPersistenceConfig()
        .secretPersistenceType(
            Enums.toEnum(secretPersistenceConfig.getSecretPersistenceType().value(), io.airbyte.api.model.generated.SecretPersistenceType.class)
                .orElseThrow())
        ._configuration(Jsons.jsonNode(secretPersistenceConfig.getConfiguration()))
        .scopeType(Enums.toEnum(secretPersistenceConfig.getScopeType().value(), io.airbyte.api.model.generated.ScopeType.class).orElseThrow())
        .scopeId(secretPersistenceConfig.getScopeId());
  }

  public String writeToEnvironmentSecretPersistence(final AirbyteManagedSecretCoordinate secretCoordinate, final String payload) {
    secretsRepositoryWriter.store(secretCoordinate, payload, null);
    return secretCoordinate.getFullCoordinate();
  }

  public AirbyteManagedSecretCoordinate buildRsmCoordinate(final ScopeType scope, final UUID scopeId) {
    return new AirbyteManagedSecretCoordinate(
        String.format("rsm_%s_", scope.name()),
        scopeId,
        AirbyteManagedSecretCoordinate.DEFAULT_VERSION,
        UUID::randomUUID);
  }

}
