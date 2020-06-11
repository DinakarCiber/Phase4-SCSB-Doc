package org.recap.model.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by angelind on 2/5/17.
 */
@Entity
@Table(name = "JOB_PARAM_T", schema = "RECAP", catalog = "")
@AttributeOverride(name = "id", column = @Column(name = "RECORD_NUM"))
@Getter
@Setter
public class JobParamEntity extends  AbstractEntity<Integer> {
    @Column(name = "JOB_NAME")
    private String jobName;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "RECORD_NUM")
    private List<JobParamDataEntity> jobParamDataEntities = new ArrayList<>();

    /**
     * Add all.
     *
     * @param jobParamDataEntities the job param data entities
     */
    public void addAll(List<JobParamDataEntity> jobParamDataEntities) {
        if(null == getJobParamDataEntities()){
            this.jobParamDataEntities = new ArrayList<>();
        }
        this.jobParamDataEntities.addAll(jobParamDataEntities);
    }
}
