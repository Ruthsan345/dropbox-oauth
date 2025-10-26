package com.dropbox.demo.service;


import com.dropbox.demo.dao.TokenData;

import java.util.Map;

public interface DropBoxService {


    Map<String, Object> getTeamInfo();

    Map<String, Object> getMemberList();

    void setTokens(TokenData tokenData);
}
