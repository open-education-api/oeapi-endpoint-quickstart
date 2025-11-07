package oeapi.controller;

import oeapi.controller.requestparameters.oeapiNewsFeedRequestParam;
import java.util.Map;

import oeapi.model.NewsFeed;
import oeapi.service.NewsFeedService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author itziar.urrutia
 */
@RestController
@RequestMapping("/news-feeds")
public class NewsFeedController extends oeapiController<NewsFeed> {

    @Autowired
    private NewsFeedService service;

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@ModelAttribute oeapiNewsFeedRequestParam requestParam) {
        Map.Entry<String, String> filter = requestParam.getFilter();
        return this.getAll(filter, requestParam.toPageable(), service);

    }
}
