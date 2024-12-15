package com.santa.secret.mapper;

import com.santa.secret.model.MailTemplate;
import com.santa.secret.model.People;
import com.santa.secret.model.PeopleGroup;
import com.santa.secret.model.Santa;
import com.santa.secret.model.SantaRun;
import com.santa.secret.model.SantaRunExclusion;
import com.santa.secret.model.SantaRunPeople;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DbMapper {
    List<People> getPeopleList();
    List<PeopleGroup> getPeopleGroupList();
    void insertPeople(People people);
    void updatePeople(People people);
    void deletePeople(Long id);
    void insertPeopleGroup(PeopleGroup peopleGroup);
    void updatePeopleGroup(PeopleGroup peopleGroup);
    void insertPeopleGroupMapping(long idPeo, long idPeg);
    void clearPeopleGroup(long idPeo);
    void deletePeopleGroup(long idPeg);
    void purgePeopleGroup();

    List<MailTemplate> getTemplates();
    void insertTemplate(MailTemplate mailTemplate);
    void updateTemplate(MailTemplate mailTemplate);
    void deleteTemplate(Long id);

    List<Santa> getSantaList();
    Santa getSanta(Long id);
    Santa getLastSanta();
    void insertSanta(Santa santa, Long idTemplate);
    void updateSanta(Santa santa, Long idTemplate);
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
