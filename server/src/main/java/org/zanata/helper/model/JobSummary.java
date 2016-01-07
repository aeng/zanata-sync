package org.zanata.helper.model;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JobSummary implements Serializable {
    private Long id;
    private String name;
    private String description;
    private JobStatus syncToRepoJobStatus;
    private JobStatus syncToTransServerJobStatus;
}
