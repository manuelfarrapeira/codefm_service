package org.web.codefm.api.mapper;

import org.mapstruct.Mapper;
import org.web.codefm.domain.entity.teachernotebook.Exercise;
import org.web.codefm.model.ExerciseDTO;
import org.web.codefm.model.QuarterExercisesDTO;
import org.web.codefm.model.SubjectExercisesDTO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ExerciseDTOMapper {

    ExerciseDTO toDTO(Exercise exercise);

    default List<QuarterExercisesDTO> toGroupedDTOList(List<Exercise> exercises) {
        Map<Integer, Map<Integer, List<Exercise>>> grouped = exercises.stream()
                .collect(Collectors.groupingBy(
                        Exercise::getQuarter,
                        LinkedHashMap::new,
                        Collectors.groupingBy(
                                Exercise::getSubjectId,
                                LinkedHashMap::new,
                                Collectors.toList()
                        )
                ));

        List<QuarterExercisesDTO> result = new ArrayList<>();

        grouped.forEach((quarter, subjectMap) -> {
            QuarterExercisesDTO quarterDTO = new QuarterExercisesDTO();
            quarterDTO.setQuarter(quarter);

            List<SubjectExercisesDTO> subjectList = new ArrayList<>();

            subjectMap.forEach((subjectId, exerciseList) -> {
                SubjectExercisesDTO subjectDTO = new SubjectExercisesDTO();
                subjectDTO.setSubjectId(subjectId);
                subjectDTO.setSubjectName(exerciseList.get(0).getSubjectName());
                subjectDTO.setExercises(exerciseList.stream().map(this::toDTO).toList());
                subjectList.add(subjectDTO);
            });

            quarterDTO.setSubjects(subjectList);
            result.add(quarterDTO);
        });

        return result;
    }
}

