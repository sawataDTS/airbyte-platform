/*
 * Copyright (c) 2020-2025 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.server.apis.publicapi.filters

/**
 * Base filter class for listing resources.
 */
open class BaseFilter(
  val createdAtStart: java.time.OffsetDateTime?,
  val createdAtEnd: java.time.OffsetDateTime?,
  val updatedAtStart: java.time.OffsetDateTime?,
  val updatedAtEnd: java.time.OffsetDateTime?,
  val limit: Int? = 20,
  val offset: Int? = 0,
)
