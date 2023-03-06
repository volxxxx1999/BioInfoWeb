package com.ahau.controller;

import com.ahau.common.Code;
import com.ahau.common.Result;
import com.ahau.domain.assemble.DraftParam;
import com.ahau.domain.centro.CentroParam;
import com.ahau.domain.combination.AGParam;
import com.ahau.domain.gapFill.GapParam;
import com.ahau.domain.telo.TeloParam;
import com.ahau.service.impl.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Vector;

@RestController
@RequestMapping("/quarTeT/exec")
public class TrainController {

    // 调用trainService
    @Autowired
    private TrainService trainService;


    /**
     * @Description: 对应前端DraftBlast的训练请求
     * @Param: HttpServletRequest request
     * @Param: DraftParam draftParam
     * @Return: Result
     */
    @PostMapping("/assembleBlast")
    public Result assembleBlast(HttpServletRequest request, @RequestBody DraftParam draftParam) {
        System.out.println("===》TrainController--->AssembleBlast:参数指令开始训练，读取Session中文件属性......");
        // 定义高级目录
        String catalogue = "Assemble/";
        // 0. 创建TaskID属性和目录,是该任务脚本的运行目录（运行目录是相对idea项目的路径）
        File taskDir = trainService.initTask(request, catalogue); // taskDir: ../../bioRepository/user_dir/upload/Assemble/uuid/
        // 1. 判断是否选择在线基因组，获取基因组路径
        String onlineGenome = draftParam.getOnlineGenome();
        System.out.println(onlineGenome.isEmpty());
        String assembleGenome = trainService.getGenome(onlineGenome, request, "AssembleGenome");
        // 2. 运行
        Vector<String> trainResult = trainService.assembleTrain(request, draftParam, assembleGenome, taskDir);
        // 3. 把生成的结果存储在Session中 该部分会检验每一个打印语句
        Boolean err = trainService.assembleSetSession(request, trainResult, catalogue);
        // 4. 发邮件给用户
        return trainService.sendEmail(request, err, catalogue);
    }


    /**
     * @Description: Assemble+GapFiller的训练
     * @Param:
     * @Return:
     */
    @PostMapping("/agBlast")
    public Result agBlast(HttpServletRequest request, @RequestBody AGParam agParam){
        System.out.println("===》TrainController--->Assemble + GapFiller:参数指令开始训练，读取Session中文件属性......");
        // 定义高级目录
        String catalogue = "AG/";
        // 0. 初始工作目录
        File taskDir = trainService.initTask(request, catalogue);
        // 1. 判断是否选择在线基因组，获取基因组路径
        String onlineGenome = agParam.getOnlineGenome();
        // 还是在Assemble模块，所以attribute是一样的
        String genome = trainService.getGenome(onlineGenome, request, "AssembleGenome");
        return trainService.agTrain(request, agParam, genome, taskDir, catalogue);
    }


    /**
     * @Description: module2 gapFill训练
     * @Param: HttpServletRequest request
     * @Param: GapParam gapParam
     * @Return: Result
     */
    @PostMapping("/fillBlast")
    public Result fillBlast(HttpServletRequest request, @RequestBody GapParam gapParam) {
        System.out.println("===》TrainController---->gapFillBlast ：参数指令开始训练，读取Session中文件属性......");
        // 定义高级目录
        String catalogue = "GapFill/";
        // 0. 创建TaskID属性和目录,是该任务脚本的运行目录（运行目录是相对idea项目的路径）
        File taskDir = trainService.initTask(request, catalogue); // taskDir: ../../bioRepository/user_dir/upload/GapFill/uuid/
        // 1. 判断是否选择在线基因组，获取基因组路径
        String onlineGenome = gapParam.getOnlineGenome();
        String genome = trainService.getGenome(onlineGenome, request, "GapFillGenome");
        // 2. 运行
        Vector<String> trainResult = trainService.trainGapFill(request, gapParam, genome, taskDir);
        // 2. 把生成的结果存储在Session中 该部分会检验每一个打印语句
        Boolean err = trainService.fillSetSession(request, trainResult, catalogue);
        // 3. 发邮件给用户
        return trainService.sendEmail(request, err, catalogue);
    }


    /**
     * @Description: teloBlast训练
     * @Param: HttpServletRequest
     * @Param: TeloParam
     * @Return: Result
     */
    @PostMapping("/teloBlast")
    public Result teloBlast(HttpServletRequest request, @RequestBody TeloParam teloParam) {
        System.out.println("===》TrainController---->TeloBlast ：参数指令开始训练，读取Session中文件属性......");
        // 1. 接受返回数据
        String catalogue = "Telo/";
        Vector<String> trainResult = trainService.trainTelo(request, teloParam, catalogue);
        // 2. 把生成的结果存储在Session中 该部分会检验每一个打印语句
        Boolean err = trainService.teloSetSession(request, trainResult, catalogue);
        // 3. 发邮件给用户
        return trainService.sendEmail(request, err, catalogue);
    }


    /**
     * @Description: Centro Module的blast
     * @Param: HttpServletRequest
     * @Param: CentroParam
     * @Return: Result
     */
    @PostMapping("/centroBlast")
    public Result centroBlast(HttpServletRequest request, @RequestBody CentroParam centroParam) {
        System.out.println("===》TrainController---->CentroBlast ：参数指令开始训练，读取Session中文件属性......");
        // 1. 接受返回数据
        String catalogue = "Centro/";
        Vector<String> trainResult = trainService.trainCentro(request, centroParam, catalogue);
        // 2. 把生成的结果存储在Session中 该部分会检验每一个打印语句
        Boolean err = trainService.centroSetSession(request, trainResult, catalogue);
        // 3. 发邮件给用户
        return trainService.sendEmail(request, err, catalogue);
    }
}
