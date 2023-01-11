package com.ahau.controller;

import com.ahau.common.Code;
import com.ahau.common.Result;
import com.ahau.domain.DraftMapInfo;
import com.ahau.domain.DraftResultUrl;
import com.ahau.domain.DraftStat;
import com.ahau.domain.ProcessWarning;
import com.ahau.domain.centro.CentroCandidate;
import com.ahau.domain.centro.CentroResultUrl;
import com.ahau.domain.gapFill.GapContigs;
import com.ahau.domain.gapFill.GapDetail;
import com.ahau.domain.gapFill.GapResultUrl;
import com.ahau.domain.gapFill.GapStat;
import com.ahau.domain.telo.TeloInfo;
import com.ahau.domain.telo.TeloResultUrl;
import com.ahau.exception.BusinessException;
import com.ahau.exception.SystemException;
import com.ahau.service.impl.CommonService;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.Vector;

@RestController
@RequestMapping("/blast/common") // 对应前端的格式
public class CommonController {
    // 服务器中用户上传文件的目录
    @Value("${bio.uploadPath}")
    private String basedir;
    // 服务器中程序生成的地址目录
    @Value("${bio.genPath}")
    private String genPath;
    // 注入commonService
    @Autowired
    private CommonService commonService;

    /**
     * @Description: 通用上传文件 从本地上传到服务器
     * @Param: MultipartFile file
     * @Return: String fileName
     */
    public String upload(MultipartFile file) {
        // 1. 获取用户上传的原始文件名
        String originalFilename = file.getOriginalFilename();//abc.jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 2. 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
        String fileName = UUID.randomUUID().toString() + suffix;
        // 3. 创建一个目录对象
        Date date = new Date();
        // 根据当前日期创建目录
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = simpleDateFormat.format(date);
        String newDir = basedir + sDate;
        File dir = new File(newDir);
        // 4. 判断当前目录是否存在
        if (!dir.exists()) {
            //目录不存在，需要创建
            dir.mkdirs();
            System.out.println("建立当天目录dir：" + dir);
        }
        try {
            // 5. 将临时文件转存到指定位置
            String newFileName = newDir + '/' + fileName;
            System.out.println("上传文件地址：" + newFileName);
            file.transferTo(new File(newFileName));
            return newFileName;
        } catch (Exception e) {  // 上传出现问题直接抛出系统异常，不属于业务范围
            throw new SystemException("Upload failed, please check your network", Code.SYSTEM_ERR);
        }
    }

    /**
     * @Description: 上传训练文件  1. 上传到服务器，返回一个UUID 2. 把该UUID写进Session的属性中
     * @Param: request 前端发送的请求
     * @Param: file file实例
     * @Return: Result 回复前端的响应体
     */
    @PostMapping("/uploadGenome")
    public Result uploadTrain(HttpServletRequest request, MultipartFile file) {
        System.out.println("=============CommonController: uploadGenome==============");
        String fileName = upload(file);
        System.out.println("----> RefGenome_Url:" + fileName);
        // 把文件名存到Session
        HttpSession session = request.getSession();
        session.setAttribute("RefGenome_Url", fileName);
        return new Result(Code.UPLOAD_OK, "success");
    }

    /*
     * @Description:上传参数文件 1. 上传到服务器，返回一个UUID 2. 把该UUID写进Session的属性中（参数和训练属性名不同）
     * @Param: request 前端请求
     * @Param: file 参数文件
     * @Return:
     */
    @PostMapping("/uploadHiFi")
    public Result uploadParam(HttpServletRequest request, MultipartFile file) {
        System.out.println("=============CommonController: uploadHiFi==============");
        String fileName = upload(file);
        System.out.println("----> HiFi _Url:" + fileName);
        // 把文件名存到Session
        HttpSession session = request.getSession();
        session.setAttribute("HiFi_Url", fileName);
        return new Result(Code.UPLOAD_OK, "success");
    }

    /*
     * @Description: 数据回显 从Session中获取展示数据，返回给前端，结合mounted使用
     * @Param: request 前端mounted发送的异步请求
     * @Return: Result
     */
    @GetMapping("/draftDisplay")
    public Result draftDisplay(HttpServletRequest request) {
        try {
            System.out.println("=============CommonController: draftDisplay==============");
            HttpSession session = request.getSession();
            // 1. 从Session获取数据 return给用户
            String hifiRef = genPath + session.getAttribute("hifi_ref_url");
            System.out.println("----》HiFiRefUrl:" + hifiRef);
            String genomeRef = (String) session.getAttribute("genome_ref_url");
            if (!genomeRef.equals("NotPlot")) { // 如果plot，加上url前缀
                genomeRef = genPath + genomeRef;
            }
            String genomePng = genPath + session.getAttribute("genome_png");
            String fasta = genPath + session.getAttribute("fasta");
            String agp = genPath + session.getAttribute("agp");
            // 还有一个新增的Warnings别忘记了 这里要JSON转对象
            Vector<ProcessWarning> warnings = (Vector<ProcessWarning>) session.getAttribute("warnings");

            // 2. 读取stat和mapinfo的内容
            String mapinfo = (String) session.getAttribute("mapinfo");
            Vector<DraftMapInfo> draftMapInfos = commonService.draftReadMapInfo(mapinfo);
            System.out.println("----->draftMapInfo: " + draftMapInfos);
            mapinfo = genPath + mapinfo;
            String stat = (String) session.getAttribute("stat");
            Vector<DraftStat> draftStats = commonService.draftReadStat(stat);
            System.out.println("----->draftStats: " + draftStats);
            stat = genPath + stat;

            // 3 作为结果传递给前端
            DraftResultUrl resultUrl = new DraftResultUrl();
            resultUrl.setHifiRefUrl(hifiRef);
            resultUrl.setGenomeRefUrl(genomeRef);
            resultUrl.setGenomePng(genomePng);
            resultUrl.setMapInfoUrl(mapinfo);
            resultUrl.setStatUrl(stat);
            resultUrl.setDraftMapInfo(draftMapInfos);
            resultUrl.setDraftStat(draftStats);
            resultUrl.setFastaUrl(fasta);
            resultUrl.setAgpUrl(agp);
            resultUrl.setWarnings(warnings);

            System.out.println("---->resultUrl: " + resultUrl);
            return new Result(Code.TRAIN_OK, "success", resultUrl);
        } catch (Exception e) {
            throw new BusinessException("Fail to generate the result, please check the format of your file", Code.BUSINESS_ERR);
        }
    }


    /**
     * @Description: GapFill的genome file上传 1. 转存文件 2. 把文件名设置进Session中
     * @Param: request和file
     * @Return: Result
     */
    @PostMapping("/fillGenome")
    public Result uploadFillGenome(HttpServletRequest request, MultipartFile file) {
        System.out.println("=============CommonController: fillGenome==============");
        String fileName = upload(file);
        System.out.println("----> upload->fillGenome_Url:" + fileName);
        // 把文件名存到Session
        HttpSession session = request.getSession();
        session.setAttribute("fillGenome_Url", fileName);
        return new Result(Code.UPLOAD_OK, "success");
    }


    /**
     * @Description: GapFill的（多个）Contigs文件上传 1. 转存文件 2. 把文件名设置进Session中
     * （TODO 多个文件是怎么处理的？果不其然 multiple是调用了两次这个函数）
     * @Param: request和file
     * @Return: Result
     */
    @PostMapping("/fillContig")
    public Result uploadFillContigs(HttpServletRequest request, MultipartFile file) {
        System.out.println("=============CommonController: fillContig==============");
        String originalFilename = file.getOriginalFilename();
        String uuidFileName = upload(file);
        GapContigs contigs = new GapContigs();
        contigs.setOriginName(originalFilename);
        contigs.setUuidName(uuidFileName);
        System.out.println("----> upload->fillContig_Url: " + uuidFileName);
        // 1. 查看session中是否已经上传了contigs文件
        HttpSession session = request.getSession();
        ArrayList<GapContigs> fillContigUrl = (ArrayList<GapContigs>) session.getAttribute("fillContig_Url");
        // 2. 如果之前没有有其他上传的文件,需要初始化一个队列
        if (fillContigUrl == null) {
//            System.out.println("之前没有上传文件");
            ArrayList<GapContigs> gapContigs = new ArrayList<>();
            gapContigs.add(contigs);
            session.setAttribute("fillContig_Url", gapContigs);
        }
        // 3. 如果已经有上传的文件
        else {
            System.out.println(fillContigUrl);
            fillContigUrl.add(contigs);
            session.setAttribute("fillContig_Url", fillContigUrl);
        }
        return new Result(Code.UPLOAD_OK, "success");
    }


    /**
     * @Description: 删除文件 TODO 这个还没有实现呢
     * @Param: MultipartFile HTTPRequest
     * @Return: Result
     */
    @PostMapping("/removeContig")
    public Result removeContig(HttpServletRequest request, @RequestBody String file) {
        System.out.println("aha??");
//        System.out.println(file.getOriginalFilename());
        System.out.println(file);
        // 1. 获取要删除的原始文件名
//        String originalFilename = file.getOriginalFilename();
        String originalFilename = file;
        // 2. 获取request中已经上传的session队列
        HttpSession session = request.getSession();
        ArrayList<GapContigs> fillContig_url = (ArrayList<GapContigs>) session.getAttribute("fillContig_Url");
        System.out.println(fillContig_url);
        int index = 0;
        for (GapContigs contigs : fillContig_url) {
            index += 1;
            if (contigs.getOriginName().equals(originalFilename)) {
                // 3. 根据原始文件名找到uuid文件名(这里是包括了整个路径地址的), 并删除
                File deleteFile = new File(contigs.getUuidName());
                if (deleteFile.delete()) {
                    System.out.println("Successfully deleted the file");
                }
                // 4. 从Session中也删除这个文件
                fillContig_url.remove(index);
                // 5. 把新的Url存储到Session中
                session.setAttribute("fillContig_Url", fillContig_url);
            }
        }
        return new Result(Code.REMOVE_OK, "success");
    }


    /**
    * @Description: fillDisplay
    * @Param: httpRequest
    * @Return: Result
    */
    @GetMapping("/fillDisplay")
    public Result fillDisplay(HttpServletRequest request){
        try {
            System.out.println("=============CommonController: fillDisplay==============");
            HttpSession session = request.getSession();
            // 1. 从Session获取数据 return给用户
            // png
            String genomePng = genPath + session.getAttribute("fillPng");
            // fasta
            String fasta = genPath + session.getAttribute("fillFasta");
            // warnings
            Vector<ProcessWarning> warnings = (Vector<ProcessWarning>) session.getAttribute("fillWarnings");
            // 2. stat和detail URL
            String stat = (String) session.getAttribute("fillStat");
            String detail = (String) session.getAttribute("fillDetail");
            // 3. stat和detail读取出来展示
            ArrayList<GapStat> gapStats = commonService.fillReadStat(stat);
            ArrayList<GapDetail> gapDetails = commonService.fillReadDetail(detail);
            // 4. 作为结果传递给前端
            GapResultUrl gapResultUrl = new GapResultUrl();
            gapResultUrl.setGenomePng(genomePng);
            gapResultUrl.setFastaUrl(fasta);
            stat = genPath + stat;
            gapResultUrl.setStatUrl(stat);
            detail = genPath + detail;
            gapResultUrl.setDetailUrl(detail);
            gapResultUrl.setGapStat(gapStats);
            gapResultUrl.setGapDetail(gapDetails);
            gapResultUrl.setWarnings(warnings);
            return new Result(Code.TRAIN_OK, "success", gapResultUrl);
        } catch (Exception e) {
            throw new BusinessException("Fail to generate the result, please check the format of your file", Code.BUSINESS_ERR);
        }
    }



    /**
     * @Description: 测试一下结合后端的下载
     * @Param: 参数就是request的参数
     * @Return: 返回url 先测试着
     */
    @GetMapping("/testDownload")
    public String testDownload(HttpServletRequest request, HttpServletResponse response) {
        // 1 从request获取到session中的URL
        HttpSession session = request.getSession();
        String agp = genPath + session.getAttribute("agp");
        // 2 给response设置响应头 attachment附件下载
        response.setHeader("content-disposition",
                "attachment; filename=" + agp);
        // 3 注：ResponseBody默认把return值作为响应体 看看会不会影响
        return agp;
    }


    /**
    * @Description: Telo module3的文件上传
    * @Param: MultiPartFile file HttpRequest
    * @Return: Result
    */
    @PostMapping("/teloGenome")
    public Result uploadTeloGenome(HttpServletRequest request, MultipartFile file){
        System.out.println("=============CommonController: TeloGenome==============");
        String fileName = upload(file);
        System.out.println("----> upload->TeloGenome_Url:" + fileName);
        // 把文件名存到Session
        HttpSession session = request.getSession();
        session.setAttribute("teloGenome_Url", fileName);
        return new Result(Code.UPLOAD_OK, "success");
    }


    /**
    * @Description: Telo module3的display
    * @Param: HttpServletRequest
    * @Return: Result
    */
    @GetMapping("/teloDisplay")
    public Result teloDisplay(HttpServletRequest request){
        try {
            System.out.println("=============CommonController: teloDisplay==============");
            HttpSession session = request.getSession();
            // 1. 从Session获取数据 return给用户
            String teloPng = genPath + session.getAttribute("teloPng");
            // warnings
            Vector<ProcessWarning> warnings = (Vector<ProcessWarning>) session.getAttribute("fillWarnings");
            // 2. info URL
            String infoUrl =  (String) session.getAttribute("infoUrl");
            // 3. info读取出来展示
            ArrayList<TeloInfo> teloInfos = commonService.teloReadInfo(infoUrl);
            // 4. 作为结果传递给前端
            TeloResultUrl teloResultUrl = new TeloResultUrl();
            teloResultUrl.setGenomePng(teloPng);
            infoUrl = genPath + infoUrl;
            teloResultUrl.setInfoUrl(infoUrl);
            teloResultUrl.setTeloInfo(teloInfos);
            teloResultUrl.setWarnings(warnings);
            System.out.println(teloResultUrl);
            return new Result(Code.TRAIN_OK, "success", teloResultUrl);
        } catch (Exception e) {
            throw new BusinessException("Fail to generate the result, please check the format of your file", Code.BUSINESS_ERR);
        }
    }


    /**
    * @Description: Centro Module4的Genome上传
    * @Param: request MultiPartFile
    * @Return: Result
    */
    @PostMapping("/centroGenome")
    public Result uploadCentroGenome(HttpServletRequest request, MultipartFile file){
        System.out.println("=============CommonController: CentroGenome==============");
        String fileName = upload(file);
        System.out.println("----> upload->CentroGenome_Url:" + fileName);
        // 把文件名存到Session
        HttpSession session = request.getSession();
        session.setAttribute("centroGenome_Url", fileName);
        return new Result(Code.UPLOAD_OK, "success");
    }


    /**
    * @Description: Centro Module的TE annnotation上传
    * @Param: request MultiPartFile
    * @Return: Result
    */
        @PostMapping("/centroTE")
    public Result uploadTEGenome(HttpServletRequest request, MultipartFile file){
        System.out.println("=============CommonController: CentroTE==============");
        String fileName = upload(file);
        System.out.println("----> upload->CentroTE_Url:" + fileName);
        // 把文件名存到Session
        HttpSession session = request.getSession();
        session.setAttribute("centroTE_Url", fileName);
        return new Result(Code.UPLOAD_OK, "success");
    }


    /**
    * @Description: Centro Module的display
    * @Param: request
    * @Return: ResultUrl
    */
    @GetMapping("/centroDisplay")
    public Result centroDisplay(HttpServletRequest request){
        try {
            System.out.println("=============CommonController: centroDisplay==============");
            HttpSession session = request.getSession();
            // 1. 从Session获取数据 return给用户
            String centroPng = genPath + session.getAttribute("centroPng");
            String gff3ZipUrl = genPath + session.getAttribute("gff3ZipUrl");
            String fastaZipUrl = genPath + session.getAttribute("fastaZipUrl");
            String candidateZipUrl = genPath + session.getAttribute("candidateZipUrl");
            // warnings
            Vector<ProcessWarning> warnings = (Vector<ProcessWarning>) session.getAttribute("fillWarnings");
            // 2. candidate URL
            String candidateUrl =  (String) session.getAttribute("candidateUrl");
            // 3. 表格读取出来展示
            ArrayList<CentroCandidate> centroCandidates = commonService.centroReadCandidate(candidateUrl);
            // 4. 作为结果传递给前端
            CentroResultUrl centroResultUrl = new CentroResultUrl();
            centroResultUrl.setGenomePng(centroPng);
            centroResultUrl.setCandidateZipUrl(candidateZipUrl);
            centroResultUrl.setFastaZipUrl(fastaZipUrl);
            centroResultUrl.setGff3ZipUrl(gff3ZipUrl);
            // 表格数据
            candidateUrl = genPath + candidateUrl;
            centroResultUrl.setCandidateUrl(candidateUrl);
            centroResultUrl.setCandidate(centroCandidates);
            System.out.println(centroResultUrl);
            return new Result(Code.TRAIN_OK, "success", centroResultUrl);
        } catch (Exception e) {
            throw new BusinessException("Fail to generate the result, please check the format of your file", Code.BUSINESS_ERR);
        }
    }





}
