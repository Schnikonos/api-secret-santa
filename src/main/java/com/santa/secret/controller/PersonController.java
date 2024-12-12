package com.santa.secret.controller;

import com.santa.secret.model.*;
import com.santa.secret.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
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

    @DeleteMapping(path = "people/{id}")
    public void deletePeople(@PathVariable long id) {
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
    public void deleteSanta(@PathVariable long santaId) {
        log.info("delete-santa");
        personService.deleteSanta(santaId);
    }

    @GetMapping(path = "santa/{santaId}")
    public Santa getSanta(@PathVariable long santaId) {
        log.info("get-santa");
        return personService.getSanta(santaId);
    }

    @GetMapping(path = "santa/{santaId}/run")
    public List<SantaRun> getRunList(@PathVariable long santaId) {
        log.info("get-run-list");
        return personService.getRunList(santaId);
    }

    @GetMapping(path = "santa/{santaId}/run/{runId}")
    public SantaRun getRun(@PathVariable long santaId, @PathVariable long runId) {
        log.info("get-run");
        return personService.getRun(runId);
    }

    @DeleteMapping(path = "santa/{santaId}/run/{runId}")
    public void deleteRun(@PathVariable long santaId, @PathVariable long runId) {
        log.info("delete-run");
        personService.deleteSantaRun(runId);
    }

    @PostMapping(path = "compute/{santaId}")
    public ComputeReply compute(@PathVariable long santaId, @RequestBody SantaRun santaRun) {
        return personService.compute(santaId, santaRun);
    }

    @PostMapping(path = "mail")
    public MailReply sendMail(@RequestBody SantaRun santaRun) {
        return personService.sendMail(santaRun);
    }
}
