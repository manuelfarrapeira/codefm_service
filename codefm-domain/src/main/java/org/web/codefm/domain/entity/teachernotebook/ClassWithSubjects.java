package org.web.codefm.domain.entity.teachernotebook;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class ClassWithSubjects {
    private Class classData;
    private List<SubjectClassDetail> subjects;
}

