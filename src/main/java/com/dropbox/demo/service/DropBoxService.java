package com.dropbox.demo.service;


import java.util.Map;

public interface DropBoxService {


    Map<String, Object> getTeamInfo();

    void setAccessToken(String accessToken);

    Map<String, Object> getMemberList();
}
