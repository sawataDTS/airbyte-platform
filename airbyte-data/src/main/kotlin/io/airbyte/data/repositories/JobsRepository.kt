/*
 * Copyright (c) 2020-2025 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.data.repositories

import io.airbyte.data.repositories.entities.Job
import io.airbyte.db.instance.jobs.jooq.generated.enums.JobConfigType
import io.airbyte.db.instance.jobs.jooq.generated.enums.JobStatus
import io.micronaut.data.annotation.Query
import io.micronaut.data.jdbc.annotation.JdbcRepository
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.repository.PageableRepository
import java.time.OffsetDateTime

@JdbcRepository(dialect = Dialect.POSTGRES, dataSource = "config")
interface JobsRepository : PageableRepository<Job, Long> {
  /**
   * Counts the number of failed jobs since the last successful job for a given scope.
   * If there are no successful jobs, it counts all failed jobs for that scope.
   *
   * @param scope The scope associated with the connection (UUID as String).
   * @return The count of failed jobs since the last successful job.
   */
  @Query(
    """
    SELECT COUNT(*)
    FROM jobs
    WHERE scope = :scope
      AND status = 'failed'
      AND (created_at > (
          SELECT MAX(created_at)
          FROM jobs
          WHERE scope = :scope
            AND status = 'succeeded'
      ) OR NOT EXISTS (
          SELECT 1
          FROM jobs
          WHERE scope = :scope
            AND status = 'succeeded'
      ))
    """,
  )
  fun countFailedJobsSinceLastSuccessForScope(scope: String): Int

  @Query(
    """
    SELECT *
    FROM jobs
    WHERE scope = :scope
      AND status = 'succeeded'
    ORDER BY created_at ASC
    LIMIT 1
    """,
  )
  fun firstSuccessfulJobForScope(scope: String): Job?

  @Query(
    """
    SELECT *
    FROM jobs
    WHERE scope = :scope
      AND status = 'succeeded'
    ORDER BY created_at DESC
    LIMIT 1
    """,
  )
  fun lastSuccessfulJobForScope(scope: String): Job?

  @Query(
    """
    SELECT *
    FROM jobs
    WHERE scope = :scope
      AND created_at < (
          SELECT created_at
          FROM jobs
          WHERE id = :jobId
      )
      AND status = :status
    ORDER BY created_at DESC
    LIMIT 1
    """,
  )
  fun getPriorJobWithStatusForScopeAndJobId(
    scope: String,
    jobId: Long,
    status: JobStatus,
  ): Job?

  @Query(
    """
    SELECT *
    FROM (
      SELECT *,
       ROW_NUMBER() OVER (PARTITION BY scope ORDER BY created_at DESC) AS rn
      FROM jobs
      WHERE scope IN (:scopes)
        AND created_at >= :createdAtStart
    ) j
    WHERE j.rn = 1
    ORDER BY created_at DESC
    LIMIT :limit OFFSET :offset
  """,
    nativeQuery = true,
  )
  fun findLatestJobPerScope(
    configTypes: Set<JobConfigType>,
    scopes: Set<String>,
    createdAtStart: OffsetDateTime,
    limit: Int,
    offset: Int,
  ): List<Job>
}
