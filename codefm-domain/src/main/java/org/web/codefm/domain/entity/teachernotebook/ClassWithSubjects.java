package org.web.codefm.domain.entity.teachernotebook;

import lombok.Builder;
import lombok.Data;
import lombok.Generated;

import java.util.List;

@Data
@Builder
@Generated
public class ClassWithSubjects {
    private Class classData;
    private List<Subject> subjects;
}

