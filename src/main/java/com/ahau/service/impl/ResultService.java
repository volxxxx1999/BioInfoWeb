package com.ahau.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;


@Service
public class ResultService {

    @Value("${pathConfig.rwRootPath}")  // ../../bioRepository/user_dir/
    private String rwRootPath;

    /**
     * @Description: 查看该TaskID是否真实存在 并设置Session
     * @Param:
     * @Return:
     */
    public Boolean setTaskID(HttpServletRequest request, String taskID) {  // taskID: telo_uuid
        // 0 设置Session
        HttpSession session = request.getSession();
        // 1 获取相对路径 查看检索路径是否存在
        taskID = taskID.replace("_", "/") + "/";
        session.setAttribute("TaskID", taskID);
        String taskDir = rwRootPath + taskID;
        File taskFile = new File(taskDir);
        File[] genFiles = taskFile.listFiles();
        return taskFile.exists() && genFiles != null;
    }
}
