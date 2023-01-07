package com.ahau;

import com.ahau.common.Code;
import com.ahau.domain.DraftMapInfo;
import com.ahau.domain.DraftStat;
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
                for(int i = 0;i<split.length;i++){
                    switch (i){
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
                for(int i=0;i<split.length;i++){
                    switch (i){
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
                            for(int j=i; j<split.length;j++){
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
        for(GapStat gapStat:gapStats){
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
                for(int i = 0;i<split.length;i++){
                    switch (i){
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


}
