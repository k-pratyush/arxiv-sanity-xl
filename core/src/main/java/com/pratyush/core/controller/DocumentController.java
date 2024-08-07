package com.pratyush.core.controller;

import com.pratyush.core.service.DocumentService;
import com.pratyush.core.model.Document;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/sanity")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("")
    public List<Document> getPapers(@RequestParam(required = false) Integer numPapers) {
        if(numPapers != null && numPapers > 0) {
            return documentService.getTopNPapers(numPapers);
        } else {
            return documentService.getAllPapers();
        }
    }

}
