package com.dropbox.demo.controller;

import com.dropbox.demo.service.DropBoxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dropbox")
public class DropboxController {

    @Autowired
    private DropBoxService dropboxService;

    @GetMapping("/team-info")
    public Map<String, Object> getTeamInfo() {
        return dropboxService.getTeamInfo();
    }

    @GetMapping("/member-list")
    public Map<String, Object> getMemberDetails(){
        return dropboxService.getMemberList();
    }
}
