package com.santa.secret.service;

import com.santa.secret.mapper.DbMapper;
import com.santa.secret.model.ComputeReply;
import com.santa.secret.model.MailTemplate;
import com.santa.secret.model.People;
import com.santa.secret.model.PeopleGroup;
import com.santa.secret.model.Santa;
import com.santa.secret.model.SantaRun;
import com.santa.secret.model.SantaRunExclusion;
import com.santa.secret.model.SantaRunPeople;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PersonService {
    private final DbMapper dbMapper;
    private final EmailService emailService;
    private List<MailTemplate> templates;
    private MailTemplate defaultTemplate;

    @Autowired
    public PersonService(DbMapper dbMapper, EmailService emailService) {
        this.dbMapper = dbMapper;
        this.emailService = emailService;

        dbMapper.purgePeopleGroup();

    }

    public ComputeReply compute(Long idSanta, SantaRun santaRun) {
        ComputeReply res = new ComputeReply();

        List<SantaRunPeople> peopleList = santaRun.getPeopleList();
        peopleList.forEach(SantaRunPeople::compute);

        Map<Long, Long> currentAssociations = new HashMap<>();
        peopleList.forEach(p -> currentAssociations.put(p.getIdPeople(), p.getIdPeopleTo()));

        boolean isOk = computeLoop(res, peopleList, false);
        if (isOk) {
            SantaRun newSantaRun = insertSantaRun(idSanta, santaRun);
            fillSantaRunInfo(newSantaRun);
            res.setSantaRun(newSantaRun);
            res.setOk(true);
            return res;
        }

        // reset and try again
        log.info("No result found... Best Effort: we try while allowing to give a gift to the one that gave us a gift");
        res.setNbChanged(0);
        peopleList.forEach(p -> p.setIdPeopleTo(currentAssociations.get(p.getIdPeople())));
        isOk = computeLoop(res, peopleList, true);
        if (isOk) {
            SantaRun newSantaRun = insertSantaRun(idSanta, santaRun);
            fillSantaRunInfo(newSantaRun);
            res.setSantaRun(newSantaRun);
            res.setOk(true);
            res.setAllowSameFromTo(true);
            return res;
        }

        fillSantaRunInfo(santaRun);
        res.setSantaRun(santaRun);
        res.setOk(false);
        return res;
    }

    private boolean computeLoop(ComputeReply res, List<SantaRunPeople> peopleList, boolean allowSameFromTo) {
        while (true) {
            boolean isOk = computeRun(peopleList, allowSameFromTo);
            if (isOk) {
                return true;
            }
            if (peopleList.stream().noneMatch(p -> p.getIdPeopleTo() != null && !p.isLocked())) {
                return false;
            }
            Map<Long, Long> scoreMap = computePeopleListScore(peopleList);
            peopleList.stream().filter(p -> p.getIdPeopleTo() != null && !p.isLocked())
                    .min(Comparator.comparing(p -> scoreMap.get(p.getIdPeople())))
                    .ifPresent(p -> p.setIdPeopleTo(null)); // purpose is to add more people in the loop, starting with the ones with the less restrictions
            res.setNbChanged(res.getNbChanged() + 1);
        }
    }

    /**
     * The goal is to find the people most compatible with the current active ones. The lower the score, the higher the compatibility
     */
    private Map<Long, Long> computePeopleListScore(List<SantaRunPeople> peopleList) {
        Map<Long, Long> scores = new HashMap<>();

        List<SantaRunPeople> activePeopleList = peopleList.stream().filter(p -> p.getIdPeopleTo() == null).toList();
        Set<Long> idActivePeople = activePeopleList.stream().map(SantaRunPeople::getIdPeople).collect(Collectors.toSet());
        Map<Long, Long> exclusionsCount = new HashMap<>();
        for (SantaRunPeople p : peopleList) {
            p.getExcludedIds().forEach(e -> exclusionsCount.put(e, exclusionsCount.getOrDefault(e, 0L) + 1)); // we count the number of time each person has been excluded by different people
        }

        for (SantaRunPeople p : peopleList) {
            long exclusionMatch = p.getExcludedIds().stream().filter(idActivePeople::contains).count(); // we count the number of active people that are part of the exclusion
            scores.put(p.getIdPeople(), exclusionMatch + exclusionsCount.getOrDefault(p.getIdPeople(), 0L));
        }
        return scores;
    }

    private boolean computeRun(List<SantaRunPeople> peopleList, boolean allowSameFromTo) {
        Set<Long> usedIds = peopleList.stream().map(SantaRunPeople::getIdPeopleTo).filter(Objects::nonNull).collect(Collectors.toSet()); // we collect people that already receive gifts
        List<Long> availableIds = peopleList.stream().map(SantaRunPeople::getIdPeople).filter(idPeople -> !usedIds.contains(idPeople)).toList(); // we compute the list of people not receiving gifts

        Map<Long, Long> toFrom = new HashMap<>();
        peopleList.forEach(p -> toFrom.put(p.getIdPeopleTo(), p.getId()));

        return fillSecretSanta(peopleList, 0, availableIds, new HashSet<>(), toFrom, allowSameFromTo);
    }

    private SantaRun fillSantaRunInfo(SantaRun santaRun) {
        Map<Long, SantaRunPeople> runPeopleList = new HashMap<>();
        Map<Long, People> peopleMap = new HashMap<>();

        santaRun.getPeopleList().forEach(p -> runPeopleList.put(p.getIdPeople(), p));
        getPeopleList().forEach(p -> peopleMap.put(p.getId(), p));

        runPeopleList.values().forEach(p -> {
            p.setPeople(peopleMap.get(p.getIdPeople()));
            if (p.getIdPeopleTo() != null) {
                p.setPeopleTo(peopleMap.get(p.getIdPeopleTo()));
                SantaRunPeople to = runPeopleList.get(p.getIdPeopleTo());
                to.setIdPeopleFrom(p.getIdPeople());
                to.setPeopleFrom(p.getPeople());
            }
        });
        return santaRun;
    }

    private boolean fillSecretSanta(List<SantaRunPeople> peopleList, int position, List<Long> availableIds, Set<Long> alreadyTriedIds, Map<Long, Long> toFrom, boolean allowSameFromTo) {
        if (position >= peopleList.size()) {
            return true; // Victory !
        }

        SantaRunPeople people = peopleList.get(position);
        if (people.getIdPeopleTo() != null) {
            return fillSecretSanta(peopleList, position + 1, availableIds, new HashSet<>(), toFrom, allowSameFromTo);
        }

        if (availableIds.isEmpty()) {
            return false;
        }

        Long id = getAvailableId(people, availableIds, alreadyTriedIds, toFrom, allowSameFromTo);
        if (id == null) {
            return false;
        }

        people.setIdPeopleTo(id);
        people.setMailSent(false);
        Map<Long, Long> toFromNew = new HashMap<>(toFrom);
        toFromNew.put(id, people.getIdPeopleTo());
        List<Long> remainingIds = availableIds.stream().filter(i -> !id.equals(i)).toList();
        boolean isOk = fillSecretSanta(peopleList, position + 1, remainingIds, new HashSet<>(), toFromNew, allowSameFromTo);

        if (isOk) {
            return true; // all good !
        }

        // we need to try another id as this one didn't work
        people.setIdPeopleTo(null);
        alreadyTriedIds.add(id);
        return fillSecretSanta(peopleList, position, availableIds, alreadyTriedIds, toFrom, allowSameFromTo);
    }

    private Long getAvailableId(SantaRunPeople people, List<Long> availableIds, Set<Long> alreadyTriedIds, Map<Long, Long> toFrom, boolean allowSameFromTo) {
        List<Long> tempAvailableIds = new ArrayList<>(availableIds);
        Collections.shuffle(tempAvailableIds);
        for (Long id : tempAvailableIds) {
            if (id.equals(people.getIdPeople())  // we don't offer a gift to ourself
                    || people.getExcludedIds().contains(id)  // we don't offer a gift to an excluded person
                    || alreadyTriedIds.contains(id)  // Id already tried without success
                    || (!allowSameFromTo && people.getIdPeople().equals(toFrom.get(id)))) {  // we don't offer a gift to the one the gave us a gift
                continue;
            }
            return id;
        }
        return null;
    }

    public List<People> getPeopleList() {
        return dbMapper.getPeopleList();
    }

    public People insertPeople(People people) {
        dbMapper.insertPeople(people.sanitize());
        people.getGroups().forEach(g -> insertGroupMapping(people, g));
        return people;
    }

    private void insertGroupMapping(People people, PeopleGroup group) {
        dbMapper.insertPeopleGroupMapping(people.getId(), group.getId());
    }

    public People updatePeople(People people) {
        dbMapper.updatePeople(people.sanitize());
        dbMapper.clearPeopleGroup(people.getId());
        people.getGroups().forEach(g -> insertGroupMapping(people, g));
        return people;
    }

    public List<PeopleGroup> getPeopleGroupList() {
        return dbMapper.getPeopleGroupList();
    }

    public PeopleGroup insertPeopleGroup(PeopleGroup peopleGroup) {
        dbMapper.insertPeopleGroup(peopleGroup);
        return peopleGroup;
    }

    public PeopleGroup updatePeopleGroup(PeopleGroup peopleGroup) {
        dbMapper.updatePeopleGroup(peopleGroup);
        return peopleGroup;
    }

    public void deletePeopleGroup(long id) {
        dbMapper.deletePeopleGroup(id);
    }

    public void deletePeople(long id) {
        dbMapper.deletePeople(id);
    }

    public List<Santa> getSantaList() {
        return dbMapper.getSantaList();
    }

    public Santa getSanta(Long id) {
        return dbMapper.getSanta(id);
    }

    public Santa getLastSanta() {
        return dbMapper.getLastSanta();
    }

    public Santa insertSanta(Santa santa) {
        dbMapper.insertSanta(santa.sanitize(), null);
        return santa;
    }

    public Santa updateSanta(Santa santa) {
        dbMapper.updateSanta(santa.sanitize(), null);
        return santa;
    }

    public void deleteSanta(Long idSanta) {
        dbMapper.deleteSanta(idSanta);
    }

    public List<SantaRun> getRunList(Long idSanta) {
        return dbMapper.selectRunList(idSanta);
    }

    public SantaRun getRun(Long idRun) {
        SantaRun santaRun = dbMapper.selectRun(idRun);
        fillSantaRunInfo(santaRun);
        return santaRun;
    }

    public SantaRun insertSantaRun(Long idSanta, SantaRun santaRun) {
        dbMapper.insertRun(idSanta, santaRun);
        santaRun.getPeopleList().forEach(p -> {
            insertRunPeople(santaRun.getId(), p);
        });
        return santaRun;
    }

    public SantaRun updateSantaRun(SantaRun santaRun) {
        dbMapper.updateRun(santaRun);
        return santaRun;
    }

    public void deleteSantaRun(Long id) {
        dbMapper.deleteRun(id);
    }

    public void insertRunPeople(Long idRun, SantaRunPeople santaRunPeople) {
        dbMapper.insertRunPeople(idRun, santaRunPeople);
        santaRunPeople.getExclusions().forEach(e -> {
            insertExclusion(santaRunPeople.getId(), e);
        });
    }

    public void insertExclusion(Long idRunPeople, SantaRunExclusion santaRunExclusion) {
        dbMapper.insertExclusion(idRunPeople, santaRunExclusion);
    }
}
