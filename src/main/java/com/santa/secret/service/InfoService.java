package com.santa.secret.service;

import com.santa.secret.model.AppInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InfoService {
    private final String version;


    public InfoService(@Value("${app.version}") String version) {
        this.version = version;
    }

    public AppInfo appInfo() {
        AppInfo appInfo = new AppInfo();
        appInfo.setVersion(this.version);
        return appInfo;
    }
}
