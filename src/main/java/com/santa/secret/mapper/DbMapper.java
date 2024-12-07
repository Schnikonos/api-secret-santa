package com.santa.secret.mapper;

import com.santa.secret.model.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DbMapper {
    List<People> getPeopleList();
    void insertPeople(People people);
    void updatePeople(People people);
    void deletePeople(Long id);

    List<Santa> getSantaList();
    Santa getSanta(Long id);
    Santa getLastSanta();
    void insertSanta(Santa santa);
    void updateSanta(Santa santa);
    void deleteSanta(Long id);

    List<SantaRun> selectRunList(Long idSanta);
    SantaRun selectRun(Long id);
    void insertRun(Long idSea, SantaRun r);
    void updateRun(SantaRun santaRun);
    void deleteRun(Long id);

    void insertRunPeople(Long idRun, SantaRunPeople rp);
    void updateRunPeople(SantaRunPeople rp);
    void insertExclusion(Long idRunPeople, SantaRunExclusion e);
}
