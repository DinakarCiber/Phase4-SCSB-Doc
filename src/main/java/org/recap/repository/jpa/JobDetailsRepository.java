package org.recap.repository.jpa;

import org.recap.model.jpa.JobEntity;

/**
 * Created by rajeshbabuk on 4/4/17.
 */
public interface JobDetailsRepository extends BaseRepository<JobEntity> {

    /**
     * Finds job entity by using job name.
     *
     * @param jobName the job name
     * @return the job entity
     */
    JobEntity findByJobName(String jobName);
}
