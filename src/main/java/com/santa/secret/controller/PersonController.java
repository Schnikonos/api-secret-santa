package com.santa.secret.controller;

import com.santa.secret.model.ComputeReply;
import com.santa.secret.model.ImportPersonReply;
import com.santa.secret.model.People;
import com.santa.secret.model.PeopleGroup;
import com.santa.secret.model.Santa;
import com.santa.secret.model.SantaRun;
import com.santa.secret.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "person")
@CrossOrigin(origins={"http://localhost:3000"})
public class PersonController {
    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping(path = "people")
    public List<People> getPeople() {
        log.info("get-people");
        return personService.getPeopleList();
    }

    @PostMapping(path = "people")
    public People addPeople(@RequestBody People people) {
        log.info("add-people");
        if (people.getId() == null) {
            return personService.insertPeople(people);
        } else {
            return personService.updatePeople(people);
        }
    }

    @GetMapping(path = "people-group")
    public List<PeopleGroup> getPeopleGroup() {
        log.info("get-people-group");
        return personService.getPeopleGroupList();
    }

    @PostMapping(path = "people-group")
    public PeopleGroup addPeopleGroup(@RequestBody PeopleGroup peopleGroup) {
        log.info("add-people-group");
        if (peopleGroup.getId() == null) {
            return personService.insertPeopleGroup(peopleGroup);
        } else {
            return personService.updatePeopleGroup(peopleGroup);
        }
    }

    @DeleteMapping(path = "people-group/{id}")
    public void deletePeopleGroup(@PathVariable("id") Long id) {
        log.info("delete-people-group");
        personService.deletePeopleGroup(id);
    }

    @DeleteMapping(path = "people/{id}")
    public void deletePeople(@PathVariable("id") long id) {
        log.info("delete-people");
        personService.deletePeople(id);
    }

    @GetMapping(path = "santa")
    public List<Santa> getSanta() {
        log.info("get-santa");
        return personService.getSantaList();
    }

    @GetMapping(path = "last-santa")
    public Santa getLastSanta() {
        log.info("get-last-santa");
        return personService.getLastSanta();
    }

    @PostMapping(path = "santa")
    public Santa insertSanta(@RequestBody Santa santa) {
        log.info("insert-santa");
        if (santa.getId() == null) {
            return personService.insertSanta(santa);
        } else {
            return personService.updateSanta(santa);
        }
    }

    @DeleteMapping(path = "santa/{santaId}")
    public void deleteSanta(@PathVariable("santaId") long santaId) {
        log.info("delete-santa");
        personService.deleteSanta(santaId);
    }

    @GetMapping(path = "santa/{santaId}")
    public Santa getSanta(@PathVariable("santaId") long santaId) {
        log.info("get-santa");
        return personService.getSanta(santaId);
    }

    @GetMapping(path = "santa/{santaId}/run")
    public List<SantaRun> getRunList(@PathVariable("santaId") long santaId) {
        log.info("get-run-list");
        return personService.getRunList(santaId);
    }

    @GetMapping(path = "santa/{santaId}/run/{runId}")
    public SantaRun getRun(@PathVariable("santaId") long santaId, @PathVariable("runId") long runId) {
        log.info("get-run");
        return personService.getRun(runId);
    }

    @DeleteMapping(path = "santa/{santaId}/run/{runId}")
    public void deleteRun(@PathVariable("santaId") long santaId, @PathVariable("runId") long runId) {
        log.info("delete-run");
        personService.deleteSantaRun(runId);
    }

    @PostMapping(path = "compute/{santaId}")
    public ComputeReply compute(@PathVariable("santaId") long santaId, @RequestBody SantaRun santaRun) {
        return personService.compute(santaId, santaRun);
    }

    @PostMapping(path = "import-people")
    public ImportPersonReply importPeople(@RequestBody List<People> peopleList) {
        log.info("import-people");
        return personService.importPeopleList(peopleList);
    }
}
