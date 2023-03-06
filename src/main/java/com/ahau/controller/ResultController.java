package com.ahau.controller;

import com.ahau.common.Code;
import com.ahau.common.Result;
import com.ahau.service.impl.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@RestController
@RequestMapping("/quarTeT/search")
public class ResultController {

    @Autowired
    private ResultService resultService;

    @GetMapping("/resultSearch/{taskID}")
    public Result resultSearch(HttpServletRequest request, @PathVariable String taskID) { // taskID: Telo_uuid
        System.out.println("==========ResultController:resultSearch");
        // 1. 把TaskID设置进Session中 查看TaskID是否存在
        Boolean exist = resultService.setTaskID(request, taskID);
        if(!exist){
            return new Result(Code.SEARCH_ERR, "No item Found!");
        }
        // 2. 从TaskID获取类别
        String taskType = taskID.split("_")[0];
        // 3. 根据不同的类别 跳转到不同的页面
        switch(taskType){
            case "Assemble":
                return new Result(Code.ASSEMBLE_SEARCH_OK, "success");
            case "GapFill":
                return new Result(Code.GAPFILL_SEARCH_OK, "success");
            case "Telo":
                return new Result(Code.TELO_SEARCH_OK, "success");
            case "Centro":
                return new Result(Code.CENTRO_SEARCH_OK, "success");
        }
        return null;
    }


    /**
     * @Description: 根据Demo号返回不同的结果
     * @Param: demoNum
     * @Return: Result
     */
    @GetMapping("/goDemo/{demoNum}")
    public Result getInstance(HttpServletRequest request, @PathVariable String demoNum) {
        System.out.println("===》ResultController:getInstance");
        // 1. 设置taskID
        String taskID = "instance/" + demoNum + "/";
        System.out.println("======》taskID:\t" + taskID);
        HttpSession session = request.getSession();
        session.setAttribute("TaskID", taskID);
        switch (demoNum) {
            case "demo1":
                return new Result(Code.ASSEMBLE_SEARCH_OK, "success");
            case "demo2":
                return new Result(Code.GAPFILL_SEARCH_OK, "success");
            case "demo3":
                return new Result(Code.TELO_SEARCH_OK, "success");
            case "demo4":
                return new Result(Code.CENTRO_SEARCH_OK, "success");
        }
        return null;
    }

}
