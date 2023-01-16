package com.ahau;

import com.ahau.common.Code;
import com.ahau.domain.DraftMapInfo;
import com.ahau.domain.DraftStat;
import com.ahau.domain.centro.CentroCandidate;
import com.ahau.domain.centro.CentroSubCan;
import com.ahau.domain.gapFill.GapDetail;
import com.ahau.domain.gapFill.GapStat;
import com.ahau.domain.telo.TeloInfo;
import com.ahau.exception.BusinessException;
import com.ahau.exception.UserAlreadyExistsException;
import com.ahau.exception.UserNotExistsException;
import com.ahau.exception.WrongPasswordException;
import com.ahau.service.UserService;
import com.ahau.utils.JwtUtil;
import com.auth0.jwt.interfaces.Claim;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
class YuanyiDemoApplicationTests {

    @Autowired
    UserService userService;

    @Test
    void contextLoads() {
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = simpleDateFormat.format(date);

        System.out.println(sDate);

    }

    @Test
    void UserServiceRegisterTest() {
        try {
            boolean ans = userService.register("123456@qq.com", "123456");
            log.info("注册结果：" + ans);
        } catch (UserAlreadyExistsException e) {
            log.info("注册失败，用户已存在");
        }
    }

    @Test
    void UserLoginAndTokenTest() {
        String email = "123456@qq.com";
        String password = "123456";
        try {
            String token = userService.login(email, password);
            if (token != null && !"".equals(token)) {
                log.info("登录成功，获取到的 token: " + token);
                Map<String, Claim> ans = JwtUtil.verify(token);
                if (ans != null) log.info("验证成功");
                else log.info("验证失败");
                Thread.sleep(11 * 1000L); // 等待 Token 过期
                ans = JwtUtil.verify(token);
                if (ans != null) log.info("验证成功");
                else log.info("验证失败");
            } else {
                log.info("登录失败");
            }
        } catch (UserNotExistsException e) {
            log.info("用户不存在，请先注册");
        } catch (WrongPasswordException e) {
            log.info("密码错误");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 读取stat
    @Test
    void draftReadMapInfo() throws IOException {
        Vector<DraftMapInfo> draftMapInfos = new Vector<>();
        BufferedReader reader;
        /*E:/2022_Winter/test/tester_final/tester/server/testdir/Quartet.contig.mapinfo*/
        /*E:/2022_Winter/test/tester_final/tester/server/testdir/Quartet.genome.filled.stat*/


        try {
            reader = new BufferedReader(new FileReader(
                    "E:/2022_Winter/test/tester_final/tester/server/testdir/Quartet.genome.filled.stat"));
            String line = reader.readLine();
            while (line != null) {
                System.out.println("========" + line + "========");
                String[] tempLine;
                if (!line.contains("#")) {
                    // TODO 这也不是个办法 万一有缺失的值怎么办？字符串是最下等的处理方法了
                    // 万一一个单元格里面有多个数据怎么办？
                    tempLine = line.split("\t");
                    DraftMapInfo dmi = new DraftMapInfo();
                    dmi.setContigID(tempLine[0]);
                    dmi.setContigLength(tempLine[1]);
                    dmi.setTargetID(tempLine[2]);
                    for (String item : tempLine) {
                        System.out.println("--->" + item);
                    }
                }
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void splitTest() {
        String s = "Chr08_Qd\t26490480\t1\t89384-89483";
        String substring = s.substring(s.indexOf("\t"));
        System.out.println(substring);
    }

    @Test
    void draftReadStat() throws IOException {
        Vector<DraftStat> draftStats = new Vector<>();
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(
                "E:/2022_Winter/test/tester_final/tester/server/testdir/Quartet.draftgenome.stat"));
        String line = reader.readLine();
        while (line != null) {
            System.out.println("========" + line + "========");
//                String[] tempLine;
            if (!line.contains("#")) {
                DraftStat draftStat = new DraftStat();
                // Chr01_Qd\t21163354\t0\t
                // AssemblyID
                int aID = line.indexOf("\t");
                System.out.println("----》" + line.substring(0, aID));
                draftStat.setAssemblyID(line.substring(0, aID));
                // Length
                int len = line.indexOf("\t", aID + 1);
                System.out.println("----》" + line.substring(aID + 1, len));
                draftStat.setLength(line.substring(aID + 1, len));
                // GapCount
                int gCount = line.indexOf("\t", len + 1);
                System.out.println("----》" + line.substring(len + 1, gCount));
                draftStat.setGapCount(line.substring(len + 1, gCount));
                // 然后这后面就是没有的or1个or多个
                if (gCount + 1 == line.length()) {
                    // 说明没有是空的
                    System.out.println("----》 -");
                    draftStat.setGapLocus("-");
                } else {
                    // 说明有一个或多个，通通都是
                    draftStat.setGapLocus(line.substring(gCount + 1));
                    System.out.println("----》" + line.substring(gCount + 1));
                }
                draftStats.add(draftStat);
            }
            line = reader.readLine();
        }
        reader.close();
    }

    @Test
    void fillReadDetail() throws IOException {
        String detailUrl = "QuartetQuartet.genome.filled.detail";
        String transPath = "E:/bioResp/result/genResult/";
        ArrayList<GapDetail> gapDetails = new ArrayList<>();
        BufferedReader reader;
        detailUrl = transPath + detailUrl;
        reader = new BufferedReader(new FileReader(detailUrl));
        String line = reader.readLine();
        while (line != null) {
            // 1. 过滤掉头部的信息
            if (!line.contains("#")) {
                GapDetail gapDetail = new GapDetail();
                String[] split = line.split("\t");
                for (int i = 0; i < split.length; i++) {
                    switch (i) {
                        case 0:
                            gapDetail.setSID(split[i]);
                            break;
                        case 1:
                            gapDetail.setGIdentify(split[i]);
                            break;
                        case 2:
                            gapDetail.setStatus(split[i]);
                            break;
                        case 3:
                            gapDetail.setCTigID(split[i]);
                            break;
                        case 4:
                            gapDetail.setCRange(split[i]);
                            break;
                        case 5:
                            gapDetail.setCLen(split[i]);
                            break;
                        case 6:
                            gapDetail.setCStrand(split[i]);
                            break;
                        case 7:
                            gapDetail.setCScore(split[i]);
                            break;
                    }
                }
                gapDetails.add(gapDetail);
            }
            line = reader.readLine();
        }
        reader.close();
        for (GapDetail gapDetail : gapDetails) {
            System.out.println(gapDetail);
        }
    }

    @Test
    void fillReadStat() throws IOException {
        String statUrl = "QuartetQuartet.genome.filled.stat";
        String transPath = "E:/bioResp/result/genResult/";
        ArrayList<GapStat> gapStats = new ArrayList<>();
        BufferedReader reader;
        statUrl = transPath + statUrl;
        reader = new BufferedReader(new FileReader(statUrl));
        String line = reader.readLine();
        while (line != null) {
            // 1. 过滤掉头部的信息
            if (!line.contains("#")) {
                GapStat gapStat = new GapStat();
                String[] split = line.split("\t");
                for (int i = 0; i < split.length; i++) {
                    switch (i) {
                        case 0:
                            gapStat.setCID(split[i]);
                            break;
                        case 1:
                            gapStat.setLen(split[i]);
                            break;
                        case 2:
                            gapStat.setGCount(split[i]);
                            break;
                        case 3:
                            // 可能有多个
                            StringBuilder gLocus = new StringBuilder();
                            for (int j = i; j < split.length; j++) {
                                gLocus.append(split[j]).append("\t");
                            }
                            gapStat.setGLocus(gLocus.toString());
                            break;
                    }
                }
                gapStats.add(gapStat);
            }
            line = reader.readLine();
        }
        reader.close();
        for (GapStat gapStat : gapStats) {
            System.out.println(gapStat);
        }
    }

    @Test
    void teloReadInfo() throws IOException {
        String infoUrl = "Quartet.telo.info";
        String transPath = "E:/bioResp/result/epResult/Telo/";
        ArrayList<TeloInfo> teloInfos = new ArrayList<>();
        BufferedReader reader;
        infoUrl = transPath + infoUrl;
        reader = new BufferedReader(new FileReader(infoUrl));
        String line = reader.readLine();
        while (line != null) {
            // 1. 过滤掉头部的信息
            if (!line.contains("#")) {
                TeloInfo teloInfo = new TeloInfo();
                String[] split = line.split("\t");
                for (int i = 0; i < split.length; i++) {
                    switch (i) {
                        case 0:
                            teloInfo.setChrID(split[i]);
                            break;
                        case 1:
                            teloInfo.setLeftNum(split[i]);
                            break;
                        case 2:
                            teloInfo.setLeftStart(split[i]);
                            break;
                        case 3:
                            teloInfo.setLeftEnd(split[i]);
                            break;
                        case 4:
                            teloInfo.setRightNum(split[i]);
                            break;
                        case 5:
                            teloInfo.setRightStart(split[i]);
                            break;
                        case 6:
                            teloInfo.setRightEnd(split[i]);
                            break;
                    }
                }
                teloInfos.add(teloInfo);
            }
            line = reader.readLine();
        }
        reader.close();
        for (TeloInfo teloInfo : teloInfos) {
            System.out.println(teloInfo);
        }
    }

    @Test
    void centroReader() throws IOException {
        String candidateUrl = "simu.candidate";
        String transPath = "E:/bioResp/result/epResult/Centro/";
        ArrayList<CentroCandidate> centroCandidates = new ArrayList<>();
        BufferedReader reader;
        candidateUrl = transPath + candidateUrl;
        System.out.println("-----> candidateUrl : " + candidateUrl);
        reader = new BufferedReader(new FileReader(candidateUrl));
        String line = reader.readLine();
        // 如果没有读到结尾
        while (line != null) {
            // 过滤掉头部的信息
            if (!line.startsWith("#")) {
                // 如果该行是主行
                if (!line.startsWith("\t")) {
                    // 创建主行对象 设置各个属性
                    CentroCandidate centroCandidate = new CentroCandidate();
                    System.out.println("mainLine---》" + line);
                    String[] split = line.split("\t");
                    // 设置主属性
                    for (int i = 0; i < split.length; i++) {
                        switch (i) {
                            case 0:
                                centroCandidate.setChr(split[i]);
                                break;
                            case 1:
                                centroCandidate.setStart(split[i]);
                                break;
                            case 2:
                                centroCandidate.setEnd(split[i]);
                                break;
                            case 3:
                                centroCandidate.setLength(split[i]);
                                break;
                            case 4:
                                centroCandidate.setTRLength(split[i]);
                                break;
                            case 5:
                                centroCandidate.setTRCoverage(split[i]);
                                break;
                            case 6:
                                centroCandidate.setTELength(split[i]);
                                break;
                            case 7:
                                centroCandidate.setTECoverage(split[i]);
                                break;
                            case 8:
                                centroCandidate.setGeneLength(split[i]);
                                break;
                            case 9:
                                centroCandidate.setGeneCoverage(split[i]);
                                break;
                            case 10:
                                centroCandidate.setRegionScore(split[i]);
                                break;
                        }
                    }
                    // 构建一个子行数组Vector，存放子行对象
                    Vector<CentroSubCan> centroSubCans = new Vector<>();
                    // 这里开始往下继续读
                    line = reader.readLine();
                    while ((line != null) && line.startsWith("\t")) {
                        System.out.println("SubLine---》" + line);
                        // 对于每一行，创建子行对象
                        CentroSubCan centroSubCan = new CentroSubCan();
                        String[] splitSub = line.split("\t");
                        // 设置子属性
                        for (int j = 0; j < splitSub.length; j++) {
                            switch (j) {
                                case 1:
                                    centroSubCan.setSubTR(splitSub[j]);
                                    break;
                                case 2:
                                    centroSubCan.setPeriod(splitSub[j]);
                                    break;
                                case 3:
                                    centroSubCan.setSubTRLength(splitSub[j]);
                                    break;
                                case 4:
                                    centroSubCan.setSubTRCoverage(splitSub[j]);
                                    break;
                                case 5:
                                    centroSubCan.setPattern(splitSub[j]);
                                    break;
                            }
                        }
                        // 把对象加到数组中
                        centroSubCans.add(centroSubCan);
                        // 主要是这个往下读的动作已经做了
                        line = reader.readLine();
                    }
                    // 循环结束，下一行是主行 这里的line有两种可能，一种是读完了，一种是主行 但是没有利用
                    // 把子行数组作为subInfo设置给主行对象
                    centroCandidate.setSubInfo(centroSubCans);
                    // 主行对象设置完毕，存放到主行数组中
                    centroCandidates.add(centroCandidate);
                }
                // 这里结束 line已经是在下一个主行了
                System.out.println("PastLine--->"+line);
            }
            line = reader.readLine();
        }
        reader.close();
        // 输出一下看看是否读的正确
//        for (CentroCandidate centroCandidate : centroCandidates) {
//            System.out.println(centroCandidate);
//        }
    }

    @Test
    void centroReader2() throws IOException {
        String candidateUrl = "simu.candidate";
        String transPath = "E:/bioResp/result/epResult/Centro/";
        ArrayList<CentroCandidate> centroCandidates = new ArrayList<>();
        BufferedReader reader;
        candidateUrl = transPath + candidateUrl;
        System.out.println("-----> candidateUrl : " + candidateUrl);
        reader = new BufferedReader(new FileReader(candidateUrl));
        String line = reader.readLine();
        // 如果没有读到结尾
        while (line != null) {
            if(line.startsWith("#")){
                line = reader.readLine();
            }else if(!line.startsWith("\t")){
                // 创建主行对象 设置各个属性
                CentroCandidate centroCandidate = new CentroCandidate();
                System.out.println("mainLine---》" + line);
                String[] split = line.split("\t");
                // 设置主属性
                for (int i = 0; i < split.length; i++) {
                    switch (i) {
                        case 0:
                            centroCandidate.setChr(split[i]);
                            break;
                        case 1:
                            centroCandidate.setStart(split[i]);
                            break;
                        case 2:
                            centroCandidate.setEnd(split[i]);
                            break;
                        case 3:
                            centroCandidate.setLength(split[i]);
                            break;
                        case 4:
                            centroCandidate.setTRLength(split[i]);
                            break;
                        case 5:
                            centroCandidate.setTRCoverage(split[i]);
                            break;
                        case 6:
                            centroCandidate.setTELength(split[i]);
                            break;
                        case 7:
                            centroCandidate.setTECoverage(split[i]);
                            break;
                        case 8:
                            centroCandidate.setGeneLength(split[i]);
                            break;
                        case 9:
                            centroCandidate.setGeneCoverage(split[i]);
                            break;
                        case 10:
                            centroCandidate.setRegionScore(split[i]);
                            break;
                    }
                }
                // 构建一个子行数组Vector，存放子行对象
                Vector<CentroSubCan> centroSubCans = new Vector<>();
                // 这里开始往下继续读
                line = reader.readLine();
                while ((line != null) && line.startsWith("\t")) {
                    System.out.println("SubLine---》" + line);
                    // 对于每一行，创建子行对象
                    CentroSubCan centroSubCan = new CentroSubCan();
                    String[] splitSub = line.split("\t");
                    // 设置子属性
                    for (int j = 0; j < splitSub.length; j++) {
                        switch (j) {
                            case 1:
                                centroSubCan.setSubTR(splitSub[j]);
                                break;
                            case 2:
                                centroSubCan.setPeriod(splitSub[j]);
                                break;
                            case 3:
                                centroSubCan.setSubTRLength(splitSub[j]);
                                break;
                            case 4:
                                centroSubCan.setSubTRCoverage(splitSub[j]);
                                break;
                            case 5:
                                centroSubCan.setPattern(splitSub[j]);
                                break;
                        }
                    }
                    // 把对象加到数组中
                    centroSubCans.add(centroSubCan);
                    // 主要是这个往下读的动作已经做了
                    line = reader.readLine();
                }
                // 循环结束，下一行是主行 这里的line有两种可能，一种是读完了，一种是主行 但是没有利用
                // 把子行数组作为subInfo设置给主行对象
                centroCandidate.setSubInfo(centroSubCans);
                // 主行对象设置完毕，存放到主行数组中
                centroCandidates.add(centroCandidate);
            }
        }
        reader.close();
        // 输出一下看看是否读的正确
//        for (CentroCandidate centroCandidate : centroCandidates) {
//            System.out.println(centroCandidate);
//        }
    }


}
