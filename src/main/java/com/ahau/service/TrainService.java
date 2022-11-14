package com.ahau.service;


import com.ahau.common.Code;
import com.ahau.domain.BlastParam;
import com.ahau.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

@Service
public class TrainService {

    //  服务器中 使用文件上传方式 训练脚本的位置
    @Value("${bio.exePath}")
    private String exePath;
    // 服务器中 使用参数设置方式 训练脚本的位置
    @Value("${bio.exeParamPath}")
    private String exeParamPath;
    // 服务器中 用户上传文件的目录
    @Value("${bio.uploadPath}")
    private String uploadPath;
    // 你训练脚本的语言（Python？Java？Perl？R？自己yml配置）
    @Value("${bio.exeMethod}")
    private String exeMethod;

    /**
     * 普通方法：调用进程执行命令 返回cmd结果Vector
     *
     * @param cmd
     * @return
     */
    public Vector<String> train(String cmd) {
        // 2. 创建进程对象
        Process process;
        // 9. 存储读取结果
        Vector<String> execResult = new Vector<>();
        try {
            System.out.println("==========Service：调用外部训练脚本==========");
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
        System.out.println("==========Service：训练生成文件位置和可视化数据==========");
        for (int i = 0; i < execResult.size(); i++) {
            System.out.println(execResult.get(i));
        }
        return execResult;

    }

    /**
     * @Description: 接受两个文件的地址，传入本地python脚本并执行，返回结果和可展示数据
     * @Param: String trainUrl 用户上传的训练文件名 UUID 唯一
     * @Param: String paramUrl 用户上传的参数文件名 UUID 唯一
     * @Return: ExecResult 对返回结果的封装
     */
/*    public Vector<String> trainFile(String RefGenomeUrl, String HiFiUrl) {
        RefGenomeUrl = uploadPath + RefGenomeUrl;
        HiFiUrl = uploadPath + HiFiUrl;
        // 【约定规则】1. 使用sys args传递参数 2. 使用UUID命名文件并输出 3. 返回值都输出
        // 1. 定义命令行语句 exePath-你的可执行程序存档的地址 trainUrl，paramUrl-前端用户上传进行转存的文件
        String exe = exeMethod;
        String[] cmd = new String[]{exe, exePath, RefGenomeUrl, HiFiUrl};
        System.out.println("cmd: "+ Arrays.toString(cmd));

        Vector<String> result = train(cmd);
        return  result;
    }*/

    /**
     * 参数训练的方式 接受一个封装的BlastParam 这里我们把作为
     */
    public Vector<String> trainParam(String RefGenomeUrl, String HiFiUrl, BlastParam param) {
        RefGenomeUrl = uploadPath + RefGenomeUrl;
        HiFiUrl = uploadPath + HiFiUrl;
        String exe = exeMethod;

        String length = param.getMinLength().toString();
        String identity = param.getMinIdentity().toString();
        String prefix = param.getPrefix();
        String threads = param.getThreads().toString();
        String aligner = param.getAligner();
        String plot = param.getPlot().toString();
        String overwrite = param.getOverwrite().toString();
        String miniMap = param.getMiniMapOption();
        String nucmer = param.getNucmerOption();
        String delta = param.getDeltaFilterOption();

        /*一些命令设置
         * 1. plot和overwrite设置了store_true（话说去掉这个参数不就行了x
         * 2. --minimapoption接收 -x ams5，需要使用=“ ”包裹参数
         * 3. --nucmer --delta也一样*/
        String cmd = exe + " " +
                exeParamPath + " " +
                "-r=" + RefGenomeUrl +" " +
                "-q=" + HiFiUrl +" " +
                "-l=" + length +" " +
                "-i=" + identity +" " +
                "-p=" + prefix +" " +
                "-t=" + threads +" " +
                "-a=" + aligner +" " +
                "--plot=" + plot +" " +
                "--overwrite=" + "\"" + overwrite + "\" " +
                "--minimapoption=" + "\"" + miniMap + "\" " +
                "--nucmeroption=" + "\"" + nucmer + "\" " +
                "--deltafilteroption=" + "\"" + delta + "\" ";
      /*  String[] cmd = new String[]{
                exe, exeParamPath,
                "-r", RefGenomeUrl,
                "-q", HiFiUrl,
                "-l", length,
                "-i", identity,
                "-p", prefix,
                "-t", threads,
                "-a", aligner,
                "--plot", plot,
                "--overwrite", overwrite,
                "--minimapoption=", miniMap,
                "--nucmeroption=", nucmer,
                "--deltafilteroption=", delta
                };*/

        System.out.println("⭐~ trainService 调用cmd的语句：");
        System.out.println("cmd: " + cmd);
        Vector<String> result = train(cmd);
        return result;
    }

    /**
     * common方法
     * 写Session 属性
     * request 和 Vector<String> Result
     */

    public void setSession(HttpServletRequest request, Vector<String> trainResult) {
        HttpSession session = request.getSession();

        // 3. 把返回结果写入Session 包括结果Url、可视化结果
        session.setAttribute("resultUrl_1", trainResult.get(0));
        session.setAttribute("resultUrl_2", trainResult.get(1));
        session.setAttribute("imgUrl", trainResult.get(2));

    }
}




