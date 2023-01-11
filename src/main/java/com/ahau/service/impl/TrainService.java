package com.ahau.service.impl;


import com.ahau.common.Code;
import com.ahau.domain.DraftParam;
import com.ahau.domain.ProcessWarning;
import com.ahau.domain.centro.CentroParam;
import com.ahau.domain.gapFill.GapContigs;
import com.ahau.domain.gapFill.GapParam;
import com.ahau.domain.telo.TeloParam;
import com.ahau.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

@Service
public class TrainService {

    // 服务器中 使用参数设置方式 训练脚本的位置
    @Value("${bio.draftPath}")
    private String draftExePath;
    // 服务器中 用户上传文件的目录
    @Value("${bio.uploadPath}")
    private String uploadPath;
    // 训练脚本的语言
    @Value("${bio.exeMethod}")
    private String exeMethod;

    /**
     * @Description: 普通方法：调用进程执行命令 返回cmd打印结果
     * @Param: String cmd
     * @Return: Vector<String> execResult
     */

    public Vector<String> train(String cmd) {
        System.out.println("=========TrainService -> train 通用调用进程执行命令===========");
        // 1. 创建进程对象
        Process process;
        // 2. 存储命令行打印的读取结果
        Vector<String> execResult = new Vector<>();
        try {
            // 3. 使用Runtime.getRuntime()创建一个本地进程
            process = Runtime.getRuntime().exec(cmd);
            // 5. 定义脚本的输出
            String result = null;
            // 6. cmd返回流 BufferedInputStream：字节缓冲流， 需要提供process返回连接到子进程正常输出的输入流
            BufferedInputStream in = new BufferedInputStream(process.getInputStream());
            // 7. 字符流转换字节流 BufferedReader：从字符输入流中读取文本，缓冲字符； InputStreamReader:从字节流到字符流的桥梁
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            // 【注意】如果你要输出中文字符，在这里需要给字符输入流加一个指定charset字符集，我这里把注释掉了，你可以自己选择
            //  BufferedReader br1 = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            // 8. 进行读取和输出
            String lineStr = null;
            while ((lineStr = br.readLine()) != null) {
                result = lineStr;
                execResult.add(lineStr);
            }
            // 关闭输入流
            br.close();
            in.close();
            // 4. 如有必要，使当前线程等待，直到此Process对象表示的进程终止。
            process.waitFor();
        } catch (Exception e) {
            throw new BusinessException("Fail to generate the result, please check the format of your file", Code.TRAIN_ERR);
        }
        // 9. 输出这个String Vector
        System.out.println("------》 打印cmd Result结果地址：");
        for (String s : execResult) {
            System.out.println(s);
        }
        return execResult;
    }

    /**
     * @Description: DraftBlast的trainService
     * @Param: String RefGenomeUrl, String HiFiUrl, DraftParam param
     * @Return: Vector<String> execResult
     */
    public Vector<String> trainDraft(String RefGenomeUrl, String HiFiUrl, DraftParam param) {
        System.out.println("=========TrainService - trainDraft 参数的处理+命令的拼接===========");

        // 1 获取两个必要的训练FASTA文件 和 训练语言
       /* RefGenomeUrl = uploadPath + RefGenomeUrl;
        HiFiUrl = uploadPath + HiFiUrl;*/
        String exe = exeMethod;

        // 2 获取前端的参数类中的各个值
        String minAlign = param.getMinAlign().toString();
        String minContig = param.getMinContig().toString();
        String minIdentity = param.getMinIdentity().toString();
        String prefix = param.getPrefix();
        String aligner = param.getAligner();
        Boolean plot = param.getPlot();

        // 3 plot是store_true属性 需要特殊处理下
        String plotArg;
        if (plot) {
            plotArg = "--plot";
        } else {
            plotArg = "";
        }

        // 4 prefix传入到脚本中，是最终出来文件的前缀 脚本添加不了UUID 我来处理
        prefix = prefix + "_" + UUID.randomUUID().toString();
        System.out.println("------》new UUID prefix:" + prefix);
        // 5 拼接cmd指令
        String cmd = exe + " " +
                draftExePath + " " +
                "-r=" + RefGenomeUrl + " " +
                "-q=" + HiFiUrl + " " +
                "-a=" + aligner + " " +
                "-c=" + minContig + " " +
                "-l=" + minAlign + " " +
                "-i=" + minIdentity + " " +
                "-p=" + prefix + " " +
                plotArg;
        System.out.println("------》调用cmd的语句：");
        System.out.println("------》cmd: " + cmd);

        // 6 把训练结果返回
        return train(cmd);
    }

    /**
     * @Description: 把控制台打印的结果文件路径设置到Session中
     * @Param: HttpServletRequest request, Vector<String> trainResult
     * @Return: void
     */
    public Boolean setSession(HttpServletRequest request, Vector<String> trainResult) {
        System.out.println("=========TrainService：setSession 把训练的结果设置到session中===========");
        HttpSession session = request.getSession();
        // 1 对每一条命令过滤Warnings和不同的Result文件信息
        Vector<ProcessWarning> warningInfo = new Vector<>();
        // 🐎 plot flag
        boolean plotFlag = false;
        // 🐎 warning count
        int wCount = 0;
        for (String str : trainResult) {
            // 2 Warnings 最终需要在页面展示
            if (str.contains("[Warning]")) {
                wCount += 1;
                ProcessWarning pw = new ProcessWarning();
                pw.setWID(wCount);
                pw.setWarning(str);
                warningInfo.add(pw);
                System.out.println("----》" + str);
            }
            // 3 Errors 需要提示用户训练发生未知错误
            if (str.contains("[Error]")) {
                return false;
            }
            // 4 正常的Result文件
            if (str.contains("contig_map_ref.png")) {
                session.setAttribute("hifi_ref_url", str);
                System.out.println("----》hifi_ref_url: " + str);
            }
            // TODO genomeRef是选择了plot才有滴！
            if (str.contains("genome_map_ref.png")) {
                session.setAttribute("genome_ref_url", str);
                plotFlag = true;
            }
            if (str.contains("draftgenome.png")) {
                session.setAttribute("genome_png", str);
            }
            if (str.contains("mapinfo")) {
                session.setAttribute("mapinfo", str);
            }
            if (str.contains("stat")) {
                session.setAttribute("stat", str);
            }
            if (str.contains("fasta")) {
                session.setAttribute("fasta", str);
            }
            if (str.contains("agp")) {
                session.setAttribute("agp", str);
            }
        }
        // 🐎 循环结束 看看是否选择plot 如果没有plot，给前端处理下
        if (!plotFlag) {
            session.setAttribute("genome_ref_url", "NotPlot");
        }
        // 4 Warnings是一个String数组的形式设置为Session
        session.setAttribute("warnings", warningInfo);
        return true;
    }


    @Value("${bio.fillPath}")
    private String fillExePath;

    /**
     * @Description: module2-gapfill 的 训练函数
     * @Param: 两个文件Url+参数对象 组合为一个命令行语句
     * @Return: execResult Vector包含着命令行每行数据结果的队列
     */
    public Vector<String> trainGapFill(String fillGenomeUrl, ArrayList<GapContigs> fillContigsUrl, GapParam gapParam) {
        System.out.println("=========TrainService - trainGapFill 参数的处理+命令的拼接===========");
        // 1 获取两个必要的训练FASTA文件 和 训练语言
        String exe = exeMethod;

        // 2 获取前端的参数类中的各个值
        String minAlignLength = gapParam.getMinAlignLength().toString();
        String minAlignIdentity = gapParam.getMinAlignIdentity().toString();
        String flankLength = gapParam.getFlankLength().toString();
        String maxFillingLength = gapParam.getMaxFillingLength().toString();
        String prefix = gapParam.getPrefix();

        // 3 获取所有contigs文件url
        StringBuilder allFillContugsUrl = new StringBuilder();
        for (GapContigs gapContigs : fillContigsUrl) {
            String uuidName = gapContigs.getUuidName() + " ";
            allFillContugsUrl.append(uuidName);
        }
        System.out.println("------> all contig filename " + allFillContugsUrl);

        // 4 prefix传入到脚本中，是最终出来文件的前缀 脚本添加不了UUID 我来处理
        prefix = prefix + "_" + UUID.randomUUID().toString();
        System.out.println("------》new UUID prefix:" + prefix);

        // 5 拼接cmd指令
//        String cmd = exe + " " +
//                fillExePath + " " +
//                "-d=" + fillGenomeUrl + " " +
//                "-g=" + allFillContugsUrl + " " +
//                "-f=" + flankLength + " " +
//                "-l=" + minAlignLength + " " +
//                "-i=" + minAlignIdentity + " " +
//                "-m=" + maxFillingLength + " " +
//                "-p=" + prefix + " ";
        String cmd = exe + " " +
                fillExePath + " " +
                "-d " + fillGenomeUrl + " " +
                "-g " + allFillContugsUrl + " " +
                "-f " + flankLength + " " +
                "-l " + minAlignLength + " " +
                "-i " + minAlignIdentity + " " +
                "-m " + maxFillingLength + " " +
                "-p " + prefix + " ";
        System.out.println("------》调用cmd的语句：");
        System.out.println("------》cmd: " + cmd);

        // 6 把训练结果返回
        return train(cmd);
    }


    /**
     * @Description: fill的setSesssion
     * @Param: request Vector execResult
     * @Return: Boolean
     */
    public Boolean fillSetSession(HttpServletRequest request, Vector<String> trainResult) {
        System.out.println("=========TrainService：fillSetSession 把训练的结果设置到session中===========");
        HttpSession session = request.getSession();
        // 1 对每一条命令过滤Warnings和不同的Result文件信息
        Vector<ProcessWarning> warningInfo = new Vector<>();
        // 🐎 warning count
        int wCount = 0;
        for (String str : trainResult) {
            // 2 Warnings 最终需要在页面展示
            if (str.contains("[Warning]")) {
                wCount += 1;
                ProcessWarning pw = new ProcessWarning();
                pw.setWID(wCount);
                pw.setWarning(str);
                warningInfo.add(pw);
                System.out.println("----》" + str);
            }
            // 3 Errors 需要提示用户训练发生未知错误
            if (str.contains("[Error]")) {
                return false;
            }
            // 4 正常的Result文件
            if (str.contains("png")) {
                session.setAttribute("fillPng", str);
            }
            if (str.contains("stat")) {
                session.setAttribute("fillStat", str);
            }
            if (str.contains("fasta")) {
                session.setAttribute("fillFasta", str);
            }
            if (str.contains("detail")) {
                session.setAttribute("fillDetail", str);
            }
        }
        // 4 Warnings是一个String数组的形式设置为Session
        session.setAttribute("fillWarnings", warningInfo);
        return true;
    }


    @Value("${bio.teloPath}")
    private String teloExePath;


    /**
     * @Description: module 3 telo的训练函数
     * @Param: HttpRequest TeloParam
     * @Return: execResult
     */
    public Vector<String> trainTelo(String teloGenomeUrl, TeloParam teloParam) {
        System.out.println("=========TrainService - trainTelo 参数的处理+命令的拼接===========");
        // 1 获取两个必要的训练FASTA文件 和 训练语言
        String exe = exeMethod;

        // 2 获取前端的参数类中的各个值
        String clade = teloParam.getClade();
        String minRepeatTime = teloParam.getMinRepeatTime().toString();
        String prefix = teloParam.getPrefix();

        // 4 prefix传入到脚本中，是最终出来文件的前缀 脚本添加不了UUID 我来处理
        prefix = prefix + "_" + UUID.randomUUID().toString();
        System.out.println("------》new UUID prefix:" + prefix);

        //  5 拼接cmd指令
        String cmd = exe + " " +
                teloExePath + " " +
                "-i=" + teloGenomeUrl + " " +
                "-c=" + clade + " " +
                "-m=" + minRepeatTime + " " +
                "-p=" + prefix;

        System.out.println("------》调用cmd的语句：");
        System.out.println("------》cmd: " + cmd);

        // 6 把训练结果返回
        return train(cmd);
    }


    /**
     * @Description: teloBlast 的 setSession
     * @Param: HttpServletRequest Vector trainResult
     * @Return: boolean Boolean用于看命令行中是否出现[error]
     */
    public Boolean teloSetSession(HttpServletRequest request, Vector<String> trainResult) {
        System.out.println("=========TrainService：teloSetSession 把训练的结果设置到session中===========");
        HttpSession session = request.getSession();
        // 1 对每一条命令过滤Warnings和不同的Result文件信息
        Vector<ProcessWarning> warningInfo = new Vector<>();
        // 🐎 warning count
        int wCount = 0;
        for (String str : trainResult) {
            // 2 Warnings 最终需要在页面展示
            if (str.contains("[Warning]")) {
                wCount += 1;
                ProcessWarning pw = new ProcessWarning();
                pw.setWID(wCount);
                pw.setWarning(str);
                warningInfo.add(pw);
                System.out.println("----》" + str);
            }
            // 3 Errors 需要提示用户训练发生未知错误
            if (str.contains("[Error]")) {
                return false;
            }
            // 4 正常的Result文件
            if (str.contains("png")) {
                session.setAttribute("teloPng", str);
            }
            if (str.contains("info")) {
                session.setAttribute("infoUrl", str);
            }
        }
        // 4 Warnings是一个String数组的形式设置为Session
        session.setAttribute("teloWarnings", warningInfo);
        return true;
    }


    @Value("${bio.centroPath}")
    private String centroExePath;


    /**
     * @Description: centroBlast的训练
     * @Param: CentroGenomeUrl TEAnnotationRUrl CentroParam
     * @Return: execResult
     */
    public Vector<String> trainCentro(String centroGenomeUrl, String centroTEurl, CentroParam centroParam) {
        System.out.println("=========TrainService - trainCentro 参数的处理+命令的拼接===========");
        // 1 获取两个必要的训练FASTA文件 和 训练语言
        String exe = exeMethod;

        // 2 获取前端的参数类中的各个值
        String minPeriod = centroParam.getMinPeriod().toString();
        String maxPeriod = centroParam.getMaxPeriod().toString();
        String maxGap = centroParam.getMaxGap().toString();
        String minLength = centroParam.getMinLength().toString();
        String prefix = centroParam.getPrefix();

        // 4 prefix传入到脚本中，是最终出来文件的前缀 脚本添加不了UUID 我来处理
        prefix = prefix + "_" + UUID.randomUUID().toString();
        System.out.println("------》new UUID prefix:" + prefix);

        //  5 拼接cmd指令
        // 因为这里TEannotation是从Session中获取的，如果用户没有上传这个文件，那从Session得到的就是null，转化为了String类型
        // TODO 另外 如果用户上传了，又删除了，这里删除也要真的从Session中移除掉
        System.out.println("------》是否上传了TE annotation:" + centroTEurl);
        String cmd;
        if (centroTEurl == null) {
            cmd = exe + " " +
                    centroExePath + " " +
                    "-i=" + centroGenomeUrl + " " +
                    "-n=" + minPeriod + " " +
                    "-m=" + maxPeriod + " " +
                    "-g=" + maxGap + " " +
                    "-l=" + minLength + " " +
                    "-p=" + prefix;
        } else {
            cmd = exe + " " +
                    centroExePath + " " +
                    "-i=" + centroGenomeUrl + " " +
                    "--TE=" + centroTEurl + " " +
                    "-n=" + minPeriod + " " +
                    "-m=" + maxPeriod + " " +
                    "-g=" + maxGap + " " +
                    "-l=" + minLength + " " +
                    "-p=" + prefix;
        }
        System.out.println("------》调用cmd的语句：");
        System.out.println("------》cmd: " + cmd);
        // 6 把训练结果返回
        return train(cmd);
    }


    /**
    * @Description: centroBlast 的setSession
    * @Param: execResult request
    * @Return: Boolean
    */
    public Boolean centroSetSession(HttpServletRequest request, Vector<String> trainResult) {
        System.out.println("=========TrainService：centroSetSession 把训练的结果设置到session中===========");
        HttpSession session = request.getSession();
        // 1 对每一条命令过滤Warnings和不同的Result文件信息
        Vector<ProcessWarning> warningInfo = new Vector<>();
        // 🐎 warning count
        int wCount = 0;
        for (String str : trainResult) {
            // 2 Warnings 最终需要在页面展示
            if (str.contains("[Warning]")) {
                wCount += 1;
                ProcessWarning pw = new ProcessWarning();
                pw.setWID(wCount);
                pw.setWarning(str);
                warningInfo.add(pw);
                System.out.println("----》" + str);
            }
            // 3 Errors 需要提示用户训练发生未知错误
            if (str.contains("[Error]")) {
                return false;
            }
            // 4 正常的Result文件
            if (str.contains("png")) {
                session.setAttribute("centroPng", str);
            }
            if (str.contains("best.candidate")) {
                session.setAttribute("candidateUrl", str);
            }
            if (str.contains("TRgff3")) {
                session.setAttribute("gff3ZipUrl", str);
            }
            if (str.contains("TRfasta")) {
                session.setAttribute("fastaZipUrl", str);
            }
            if (str.contains("candidate.zip")) {
                session.setAttribute("candidateZipUrl", str);
            }
        }
        // 4 Warnings是一个String数组的形式设置为Session
        session.setAttribute("centroWarnings", warningInfo);
        return true;
    }




}




