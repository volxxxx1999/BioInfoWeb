package com.ahau.controller;

import com.ahau.common.Code;
import com.ahau.common.Result;
import com.ahau.domain.DraftParam;
import com.ahau.domain.gapFill.GapContigs;
import com.ahau.domain.gapFill.GapParam;
import com.ahau.domain.telo.TeloParam;
import com.ahau.service.impl.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Vector;

@RestController
@RequestMapping("/blast/exec")
public class TrainController {

    // 调用trainService
    @Autowired
    private TrainService trainService;

    /**
     * @Description: 对应前端DraftBlast的训练请求
     * @Param: HttpServletRequest request, @RequestBody DraftParam draftParam
     * @Return: Result
     */
    @PostMapping("/draftBlast")
    public Result draftBlast(HttpServletRequest request, @RequestBody DraftParam draftParam) {
        System.out.println("=========TrainController--->AssembleBlast:参数指令开始训练，读取Session中文件属性===========");

        // 1. 从Session获取数据 获取训练的文件名称
        HttpSession session = request.getSession();
        String RefGenomeUrl = (String) session.getAttribute("RefGenome_Url");
        String HiFiUrl = (String) session.getAttribute("HiFi_Url");

        System.out.println("------》用户上传的文件名，转存时已加上UUID：");
        System.out.println(RefGenomeUrl);
        System.out.println(HiFiUrl);
        System.out.println("------》用户输入的参数列表");
        System.out.println(draftParam.toString());

        // 2. 接受返回数据
        Vector<String> trainResult = trainService.trainDraft(RefGenomeUrl, HiFiUrl, draftParam);

        // 3. 把生成的结果存储在Session中 该部分会检验每一个打印语句
        Boolean err = trainService.setSession(request, trainResult);
        if (!err) {
            return new Result(Code.TRAIN_ERR, "Unexpected error occurred", null);
        } else {
            return new Result(Code.TRAIN_OK, "success", null);
        }
    }


    /**
     * @Description: module2 gapfill训练
     * @Param: request 参数列表
     * @Return: Result
     */
    @PostMapping("/fillBlast")
    public Result fillBlast(HttpServletRequest request, @RequestBody GapParam gapParam) {
        System.out.println("=========TrainController---->gapFillBlast ：参数指令开始训练，读取Session中文件属性===========");

        // 1. 从Session获取数据 获取训练的文件名称
        HttpSession session = request.getSession();
        String fillGenomeUrl = (String) session.getAttribute("fillGenome_Url");
        ArrayList<GapContigs> fillContigsUrl = (ArrayList<GapContigs>) session.getAttribute("fillContig_Url");

        System.out.println("------》用户上传的文件名，转存时已加上UUID：");
        System.out.println(fillGenomeUrl);
        System.out.println(fillContigsUrl);
        System.out.println("------》用户输入的参数列表");
        System.out.println(gapParam.toString());

        // 2. 接受返回数据
        Vector<String> trainResult = trainService.trainGapFill(fillGenomeUrl, fillContigsUrl, gapParam);

        // 3. 把生成的结果存储在Session中 该部分会检验每一个打印语句
        Boolean err = trainService.fillSetSession(request, trainResult);
        if (!err) {
            return new Result(Code.TRAIN_ERR, "Unexpected error occurred", null);
        } else {
            return new Result(Code.TRAIN_OK, "success", null);
        }
    }


    /**
     * @Description: teloBlast训练 module3
     * @Param: 参数列表teloParam HTTPServletRequest
     * @Return: Result
     */
    @PostMapping("/teloBlast")
    public Result teloBlast(HttpServletRequest request, @RequestBody TeloParam teloParam) {
        System.out.println("=========TrainController---->TeloBlast ：参数指令开始训练，读取Session中文件属性===========");

        // 1. 从Session获取数据 获取训练的文件名称
        HttpSession session = request.getSession();
        String teloGenomeUrl = (String) session.getAttribute("teloGenome_Url");

        System.out.println("------》用户上传的文件名，转存时已加上UUID：");
        System.out.println(teloGenomeUrl);

        System.out.println("------》用户输入的参数列表");
        System.out.println(teloParam.toString());

        // 2. 接受返回数据
        Vector<String> trainResult = trainService.trainTelo(teloGenomeUrl, teloParam);

        // 3. 把生成的结果存储在Session中 该部分会检验每一个打印语句
        Boolean err = trainService.teloSetSession(request, trainResult);

        if (!err) {
            return new Result(Code.TRAIN_ERR, "Unexpected error occurred", null);
        } else {
            return new Result(Code.TRAIN_OK, "success", null);
        }
    }


}
