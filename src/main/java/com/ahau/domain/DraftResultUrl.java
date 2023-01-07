package com.ahau.domain;

import lombok.Data;

import java.util.Vector;

/*
* 假设最后你的程序出来两个下载文件 这个随意的都可以改*/
@Data
public class DraftResultUrl {
    // 基本信息
    private String hifiRefUrl;
    private String genomeRefUrl;
    private String genomePng;
    private String mapInfoUrl;
    private String statUrl;
    private String fastaUrl;
    private String agpUrl;
    // 两个表格的JSON内容
    private Vector<DraftStat> draftStat;
    private Vector<DraftMapInfo> draftMapInfo;
    // cmd输出的Warnings
    private Vector<ProcessWarning> warnings;
}
