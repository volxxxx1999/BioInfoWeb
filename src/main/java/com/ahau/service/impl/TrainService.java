package com.ahau.service.impl;


import com.ahau.common.Code;
import com.ahau.common.Result;
import com.ahau.domain.FilenamePair;
import com.ahau.domain.ProcessError;
import com.ahau.domain.assemble.DraftParam;
import com.ahau.domain.ProcessWarning;
import com.ahau.domain.centro.CentroParam;
import com.ahau.domain.combination.AGParam;
import com.ahau.domain.gapFill.GapParam;
import com.ahau.domain.telo.TeloParam;
import com.ahau.exception.BusinessException;
import com.ahau.exception.SystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

@Service
public class TrainService {

    // 2. 后端进行读写的地址（相对路径）
    @Value("${pathConfig.rwRootPath}")
    private String rwRootPath;
    // 4. 训练脚本的语言
    @Value("${pathConfig.exeMethod}")
    private String exeMethod;
    // 5. 服务器中 使用参数设置方式 训练脚本的位置
    @Value("${pathConfig.assemblePath}")
    private String assembleExePath;
    // 7. 对于taskDir来说，上传文件的目录
    @Value("${pathConfig.taskUploadPath}")
    private String taskUploadPath;
    // 对于taskDir来说，存储在线genome的目录
    @Value("${pathConfig.taskGenomePath}")
    private String taskGenomePath;

    // 导入邮件工具类
    @Autowired
    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

    //注入邮件工具类 外部类
    @Value("${spring.mail.username}")
    private String sendMailer;


    /**
     * @Description: 初始化taskID，建立对应的目录和catalogue
     * @Param: HttpServletRequest request
     * @Param: String catalogue
     * @Return: String taskID
     */
    public File initTask(HttpServletRequest request, String catalogue) {
        System.out.println("--->initTask......");
        // 1. 给该任务创建一个序列ID
        String taskID = catalogue + UUID.randomUUID().toString() + "/"; // taskID: Assemble/uuid/
        // 2. 设置Session
        HttpSession session = request.getSession();
        session.setAttribute("TaskID", taskID);
        System.out.println("------>新建TaskID Session属性:" + taskID);
        // 3. 新建目录
        String taskDir = rwRootPath + taskID; // taskDir: ../../bioRepository/user_dir/Assemble/uuid/
        File file = new File(taskDir);
        if (!file.exists()) {
            file.mkdirs();
            System.out.println("------>建立任务目录：" + file);
        }
        return file;
    }


    /**
     * @Description: 发送邮件
     * @Param: HttpServletRequest
     * @Param: Boolean err (是否发生错误）
     * @Param: String catalogue 训练类型
     * @Return: Result
     */
    public Result sendEmail(HttpServletRequest request, Boolean err, String catalogue) {
        System.out.println("---> trainService: sendEmail");
        // 1. 获取taskID 和 email
        HttpSession session = request.getSession();
        String mail = (String) session.getAttribute("Email");
        System.out.println("------> 用户的邮箱：" + mail);
        System.out.println("------> 是否发生err: ");
        // 返回的Result
        Result result = new Result(Code.TRAIN_OK, "success", null);
        // 2. 判断是否发生了错误
        StringBuilder content;
        if (err) { // 没有发生错误
            System.out.println("没有错误");
            // 获取查询ID
            String taskID = (String) session.getAttribute("TaskID");
            taskID = taskID.replace("/", "_");
            taskID = taskID.substring(0, taskID.length() - 1);
            System.out.println("---》 任务序列号：" + taskID);
            // 邮件内容
            content = new StringBuilder("# Your task is completed. Your Query ID is: " + taskID + "\n" +
                    "# Please access your result at http://www.atcgn.com:8080/quarTeT/pages/home.html" + "\n" +
                    "# ----------------------------------------------" + "\n" +
                    "# If you have any comments or questions about this service," + "\n" +
                    "# please contact us at:  t2t_quartet@163.com ");
            // 返回的Result
        } else { // 发生了错误
            System.out.println("发生错误");
            // 获取Errors
            String paramType = catalogue + "Log";
            Vector<String> processLogs = (Vector<String>) session.getAttribute(paramType);
            content = new StringBuilder("# There are some errors occurred during the process:" + "\n");
            for (String log : processLogs) {
                content.append("# ").append(log).append("\n");
            }
            content.append("# You can have another try at http://www.atcgn.com:8080/quarTeT/pages/home.html" + "\n" +
                           "# or contact us with the error log for help at: t2t_quartet@163.com ");
            // 修改Result状态
            result.setCode(Code.TRAIN_ERR);
            result.setMessage("Error occurred!");
        }
        // 3. 发邮件
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // 谁发的
            message.setFrom(sendMailer);
            // 发给谁（这里可以转发多个邮箱 使用setCc）
            message.setTo(mail);
            // 主题
            message.setSubject("【quarTeT】 Your task results ");
            // 内容+发送时间 TasKID: Telo/uuid/ ---> Telo_uuid /改为_ 去掉后面的/
            message.setText(content.toString());
            message.setSentDate(new Date());
            // 发送
            javaMailSender.send(message);
            return result;
        }
        // 异常写在服务里 向上抛出
        catch (Exception e) {
            throw new SystemException("Send Failed! Bad Network Connection", Code.SEND_ERR);
        }
    }


    /**
     * @Description: 在 指定目录 调用脚本
     * @Param: String cmd 指令
     * @Param: File file 指定工作目录
     * @Return: Vector<String> execResult
     */
    public Vector<String> train(String cmd, File file) {
        System.out.println("--->TrainService\ttrain 通用调用进程执行命令......");
        // 1. 创建进程对象
        Process process;
        // 2. 存储命令行打印的读取结果
        Vector<String> execResult = new Vector<>();
        try {
            // 3. 使用Runtime.getRuntime()创建一个本地进程，可以使用指定工作目录执行 但是重载的函数中有exec(String cmds, String[] envp，File file)
            process = Runtime.getRuntime().exec(cmd, null, file);
            // 5. 定义脚本的输出
            String result = null;
            // 6. cmd返回流 BufferedInputStream：字节缓冲流， 需要提供process返回连接到子进程正常输出的输入流
            BufferedInputStream in = new BufferedInputStream(process.getInputStream());
            // 7. 字符流转换字节流 BufferedReader：从字符输入流中读取文本，缓冲字符； InputStreamReader:从字节流到字符流的桥梁
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
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
     * @Description: 设置Session Warnings
     * @Param: HttpServletRequest request
     * @Param: String paramType session的attribute名
     * @Param: Vector<String> trainResult 控制台输出结果
     * @Return: Boolean（是否出错）
     */

    public Boolean setSession(HttpServletRequest request, String paramType, Vector<String> trainResult) {
        System.out.println("--->TrainService\tsetSession\t通用指令setSession......");
        HttpSession session = request.getSession();
        // 1 存储Warning信息
        Vector<ProcessWarning> warningInfo = new Vector<>();
        // warning count
        int wCount = 0;
        for (String str : trainResult) {
            // 2 Warnings 最终需要在页面展示
            if (str.contains("[Warning]")) {
                wCount += 1;
                ProcessWarning pw = new ProcessWarning();
                pw.setWID(wCount);
                pw.setWarning(str);
                warningInfo.add(pw);
                System.out.println("warning------>\t" + str);
            }
            // 3 Errors 需要提示用户训练发生未知错误 Error也是有不同类型的
            if (str.contains("[Error]")) {
                // 3.1 session要设置一下Error
                System.out.println("error------>\t" + str);
                String errorParam = paramType + "Log";
                session.setAttribute(errorParam, trainResult);
                return false;
            }
        }
        // 4 Warnings是一个String数组的形式设置为Session
        String warningParam = paramType + "Warnings";
        session.setAttribute(warningParam, warningInfo);
        return true;

    }


    /**
     * @Description: 获取genome的路径
     * @Param: String assembleGenome
     * @Param: HttpServletRequest request
     * @Return: String
     */
    public String getGenome(String assembleGenome, HttpServletRequest request, String attrName) {
        System.out.println("--->TrainService\tgetAssembleGenome 获取基因组路径......");
        if (assembleGenome.isEmpty()) {
            HttpSession session = request.getSession();
            assembleGenome = taskUploadPath + session.getAttribute(attrName);
        } else {
            assembleGenome = taskGenomePath + assembleGenome + ".fasta";
        }
        return assembleGenome;
    }


    /**
     * @Description: 通用的assemble运行 拼接命令行 调用运行
     * @Param: HttpServletRequest request
     * @Param: String catalogue
     * @Param: String assembleGenome
     * @Return: execResult
     */
    public Vector<String> assembleTrain(HttpServletRequest request, DraftParam draftParam, String assembleGenome, File taskDir) {
        System.out.println("--->TrainService\tassembleTrain 命令的拼接......");
        // 1. 获取文件名 拼接出文件路径
        HttpSession session = request.getSession();
        String assembleHiFi = taskUploadPath + session.getAttribute("AssembleHiFi");
        System.out.println("------> 对于 运行根目录（taskDir）来说，用户上传的文件路径为：");
        System.out.println(assembleGenome);
        System.out.println(assembleHiFi);
        System.out.println("------> 用户输入的参数列表：");
        System.out.println(draftParam.toString());
        // 2 获取前端的参数类中的各个值
        String minAlign = draftParam.getMinAlign().toString();
        String minContig = draftParam.getMinContig().toString();
        String minIdentity = draftParam.getMinIdentity().toString();
        String prefix = draftParam.getPrefix();
        Boolean plot = draftParam.getPlot();
        // 3 plot是store_true属性 需要特殊处理下
        String plotArg;
        if (plot) {
            plotArg = "--plot";
        } else {
            plotArg = "";
        }
        // 4 拼接运行指令
        String cmd = exeMethod + " " +
                assembleExePath + " " +
                "-r=" + assembleGenome + " " +
                "-q=" + assembleHiFi + " " +
                "-c=" + minContig + " " +
                "-l=" + minAlign + " " +
                "-i=" + minIdentity + " " +
                "-p=" + prefix + " " +
                plotArg;
        // 5 输出拼接cmd的结果
        System.out.println("------>调用命令行的语句：" + cmd);
        // 6 把训练结果返回
        return train(cmd, taskDir);
    }

    /**
     * @Description: 把控制台打印的结果文件路径设置到Session中，Warnings
     * @Param: HttpServletRequest request
     * @Param: Vector<String> trainResult
     * @Return: void
     */
    public Boolean assembleSetSession(HttpServletRequest request, Vector<String> trainResult, String catalogue) {
        System.out.println("--->TrainService：setSession\t把训练的结果设置到session中......");
        return setSession(request, catalogue, trainResult);
    }


    /**
     * @Description: Assmble + GapFiller模块
     * @Param:
     * @Return:
     */
    public Result agTrain(HttpServletRequest request, AGParam agParam, String genome, File taskDir, String catalogue) {
        System.out.println("--->TrainService\tagTrain assemble->draftGenome->gap......");
        System.out.println("------>Step1: Begin to Run Assembly......");
        // 1.1 获取draftParam
        DraftParam draftParam = agParam.getDraftParam();
        // 1.2 调用assembleTrain
        Vector<String> assembleResult = assembleTrain(request, draftParam, genome, taskDir);
        // 1.3 获取assemble结果 区别1下
        String AG_A_attr = "AG_A";
        Boolean aErr = assembleSetSession(request, assembleResult, AG_A_attr);
//        Boolean aErr = assembleSetSession(request, assembleResult, catalogue);
        // 1.4 如果assemble模块发生错误，gap模块就不执行了 直接回显错误
        if (!aErr) {
            System.out.println("------>Assembly模块发生错误，提前结束运行！");
            // 1.5 若有错误 提前结束 并发送邮件
            return sendEmail(request, false, catalogue);
        }
        // 5. 若assmble模块没有发生错误，继续运行gap模块
        else {
            System.out.println("------>Step2: Begin to Run GapFiller......");
            // 2.1 从TaskDir获取draftGenome
            String draftGenome = "";
            // 2.2 遍历taskDir，得到draftGenome
            File projectPath = new File(rwRootPath + taskDir); // ../../bioRepository/user_dir/Assemble/uuid/
            File[] asssmbleFiles = projectPath.listFiles();
            // 2.3 若该目录为空 或 目录不存在
            if (!projectPath.exists() || asssmbleFiles == null) {
                System.out.println("------>错误：draft程序没有在taskDir生成需要的draftGenome");
                return null;
            } else {
                // 2.4 遍历dir
                for (File file : asssmbleFiles) {
                    String fileName = file.getName(); // 文件名
                    if (fileName.contains(".fasta")) {
                        // 2.5 获取draftGenome相对于本taskid的相对路径
                        draftGenome = "../../" + taskDir + fileName;  //  ../../Assemble/uuid/Quartet.draftgenome.fasta
                        break;
                    }
                }
            }
            // 2.5 获取gapParam
            GapParam gapParam = agParam.getGapParam();
            // 2.6 获取GapFill的结果
            Vector<String> gapResult = trainGapFill(request, gapParam, draftGenome, taskDir);
            String AG_G_attr = "AG_G";
            Boolean gErr = fillSetSession(request, gapResult, AG_G_attr);
//            Boolean gErr = fillSetSession(request, gapResult, catalogue);
            return sendEmail(request, gErr, catalogue);
        }
    }


    @Value("${pathConfig.fillPath}")
    private String fillExePath;

    /**
     * @Description: gapFill 的 训练函数
     * @Param: HttpServletRequest request
     * @Param: GapParam gapParam
     * @Param: String catalogue
     * @Return: execResult Vector包含着命令行每行数据结果的队列
     */
    public Vector<String> trainGapFill(HttpServletRequest request, GapParam gapParam, String gapFillGenome, File taskDir) {
        System.out.println("--->TrainService\ttrainGapFill 参数的处理+命令的拼接......");
        // 1. 获取文件名 拼接出文件路径
        HttpSession session = request.getSession();
        ArrayList<FilenamePair> filenamePairs = (ArrayList<FilenamePair>) session.getAttribute("GapFillContigs");
        System.out.println("------> 对于 运行根目录（taskDir）来说，用户上传的文件路径为：");
        System.out.println(gapFillGenome);
        System.out.println("------> 用户输入的参数列表：");
        System.out.println(gapParam.toString());
        // 2 获取前端的参数类中的各个值
        String minAlignLength = gapParam.getMinAlignLength().toString();
        String minAlignIdentity = gapParam.getMinAlignIdentity().toString();
        String flankLength = gapParam.getFlankLength().toString();
        String maxFillingLength = gapParam.getMaxFillingLength().toString();
        String prefix = gapParam.getPrefix();
        // 3 获取所有contigs文件url
        StringBuilder allFillContugsUrl = new StringBuilder();
        for (FilenamePair gapContigs : filenamePairs) {
            String uuidName = taskUploadPath + gapContigs.getUuidName() + " ";
            allFillContugsUrl.append(uuidName);
        }
        System.out.println("------> All contig filename " + allFillContugsUrl);
        // 4 拼接运行指令
        String cmd = exeMethod + " " +
                fillExePath + " " +
                "-d " + gapFillGenome + " " +
                "-g " + allFillContugsUrl + " " +
                "-f " + flankLength + " " +
                "-l " + minAlignLength + " " +
                "-i " + minAlignIdentity + " " +
                "-m " + maxFillingLength + " " +
                "-p " + prefix + " ";
        System.out.println("------》调用cmd的语句：" + cmd);
        // 5 把训练结果返回
        return train(cmd, taskDir);
    }


    /**
     * @Description: GapFill的setSession
     * @Param: HttpServletRequest request
     * @Param: Vector<String> trainResult
     * @Return: Boolean
     */
    public Boolean fillSetSession(HttpServletRequest request, Vector<String> trainResult, String catalogue) {
        System.out.println("--->TrainService：fillSetSession 把训练的结果设置到session中......");
        return setSession(request, catalogue, trainResult);
    }


    @Value("${pathConfig.teloPath}")
    private String teloExePath;

    /**
     * @Description: telo的训练函数
     * @Param: HttpServletRequest request
     * @Param: TeloParam teloParam
     * @Param: String catalogue
     * @Return: execResult
     */
    public Vector<String> trainTelo(HttpServletRequest request, TeloParam teloParam, String catalogue) {
        System.out.println("--->TrainService - trainTelo 参数的处理+命令的拼接......");
        // 0. 创建TaskID属性和目录,是该任务脚本的运行目录（运行目录是相对idea项目的路径）
        File taskDir = initTask(request, catalogue); // taskDir: ../../bioRepository/user_dir/upload/Telo/uuid/
        // 1. 获取文件名 拼接出文件路径
        HttpSession session = request.getSession();
        String teloGenome = taskUploadPath + session.getAttribute("TeloGenome"); // ../../upload/uuid_originalName
        System.out.println("------> 对于 运行根目录（taskDir）来说，用户上传的文件路径为：");
        System.out.println(teloGenome);
        System.out.println("------> 用户输入的参数列表：");
        System.out.println(teloParam.toString());
        // 2 获取前端的参数类中的各个值
        String clade = teloParam.getClade();
        String minRepeatTime = teloParam.getMinRepeatTime().toString();
        String prefix = teloParam.getPrefix();
        //  3 拼接cmd指令
        String cmd = exeMethod + " " +
                teloExePath + " " +
                "-i=" + teloGenome + " " +
                "-c=" + clade + " " +
                "-m=" + minRepeatTime + " " +
                "-p=" + prefix;
        System.out.println("------》调用cmd的语句：" + cmd);
        // 4 把训练结果返回
        return train(cmd, taskDir);
    }


    /**
     * @Description: teloBlast的setSession
     * @Param: HttpServletRequest
     * @Param: Vector<String> trainResult
     * @Return: boolean Boolean用于看命令行中是否出现[error]
     */
    public Boolean teloSetSession(HttpServletRequest request, Vector<String> trainResult, String catalogue) {
        System.out.println("--->TrainService：teloSetSession 把训练的结果设置到session中......");
        return setSession(request, catalogue, trainResult);
    }


    @Value("${pathConfig.centroPath}")
    private String centroExePath;


    /**
     * @Description: centroBlast的训练
     * @Param: HttpServletRequest request
     * @Param: CentroParam centroParam
     * @Param: String catalogue
     * @Return: execResult
     */
    public Vector<String> trainCentro(HttpServletRequest request, CentroParam centroParam, String catalogue) {
        System.out.println("--->TrainService - trainCentro 参数的处理+命令的拼接......");
        // 0. 创建TaskID属性和目录,是该任务脚本的运行目录（运行目录是相对idea项目的路径）
        File taskDir = initTask(request, catalogue); // taskDir: ../../bioRepository/user_dir/upload/Telo/uuid/
        // 1. 获取文件名 拼接出文件路径
        HttpSession session = request.getSession();
        String centroGenome = taskUploadPath + session.getAttribute("CentroGenome"); // ../../upload/uuid_originalName
        System.out.println("------> 对于 运行根目录（taskDir）来说，用户上传的文件路径为：");
        System.out.println(centroGenome);
        System.out.println("------> 用户输入的参数列表：");
        System.out.println(centroParam.toString());
        // 2 获取前端的参数类中的各个值
        String minPeriod = centroParam.getMinPeriod().toString();
        String maxPeriod = centroParam.getMaxPeriod().toString();
        String maxGap = centroParam.getMaxGap().toString();
        String minLength = centroParam.getMinLength().toString();
        String prefix = centroParam.getPrefix();
        //  5 拼接cmd指令
        // 因为这里TE annotation是从Session中获取的，如果用户没有上传这个文件，那从Session得到的就是null，转化为了String类型
        // TODO 另外 如果用户上传了，又删除了，这里删除也要真的从Session中移除掉
        String centroTE = (String) session.getAttribute("CentroTE");
        System.out.println("------》是否上传了TE annotation:" + centroTE);
        String cmd;
        if (centroTE == null) {
            cmd = exeMethod + " " +
                    centroExePath + " " +
                    "-i=" + centroGenome + " " +
                    "-n=" + minPeriod + " " +
                    "-m=" + maxPeriod + " " +
                    "-g=" + maxGap + " " +
                    "-l=" + minLength + " " +
                    "-p=" + prefix;
        } else {
            centroTE = taskUploadPath + centroTE;
            cmd = exeMethod + " " +
                    centroExePath + " " +
                    "-i=" + centroExePath + " " +
                    "--TE=" + centroTE + " " +
                    "-n=" + minPeriod + " " +
                    "-m=" + maxPeriod + " " +
                    "-g=" + maxGap + " " +
                    "-l=" + minLength + " " +
                    "-p=" + prefix;
        }
        System.out.println("------》调用cmd的语句：" + cmd);
        // 6 把训练结果返回
        return train(cmd, taskDir);
    }


    /**
     * @Description: centroBlast的setSession
     * @Param: HttpServletRequest request
     * @Param: Vector<String> trainResult
     * @Return: Boolean
     */
    public Boolean centroSetSession(HttpServletRequest request, Vector<String> trainResult, String catalogue) {
        System.out.println("--->TrainService：centroSetSession 把训练的结果设置到session中......");
        return setSession(request, catalogue, trainResult);
    }
}




