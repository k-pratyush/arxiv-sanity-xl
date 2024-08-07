package com.pratyush.core.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.pratyush.core.model.Document;
import com.pratyush.core.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/sanity/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("")
    public List<Document> getQueryResults(@RequestParam String query, @RequestParam String method) {
        return new ArrayList<Document>() {};
    }
}
