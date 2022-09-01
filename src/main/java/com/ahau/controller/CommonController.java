package com.ahau.controller;

import com.ahau.common.Code;
import com.ahau.common.Result;
import com.ahau.domain.ResultUrl;
import com.ahau.exception.BusinessException;
import com.ahau.exception.SystemException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.UUID;

@RestController
@RequestMapping("/common") // 对应前端的格式
public class CommonController {
    // 服务器中用户上传文件的目录
    @Value("${bio.uploadPath}")
    private String basedir;
    // 服务器中程序生成的地址目录
    @Value("${bio.genPath}")
    private String genPath;

    /**
     * 上传文件 用户从本地上传到服务器
     * @param file
     * @return filename
     */
    public String upload(MultipartFile file){
            //1. 获取原始文件名
            String originalFilename = file.getOriginalFilename();//abc.jpg
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            //2. 使用UUID重新生成文件名，防止文件名称重复造成文件覆盖
            String fileName = UUID.randomUUID().toString() + suffix;
            //3. 创建一个目录对象
            File dir = new File(basedir);
            //4. 判断当前目录是否存在
            if(!dir.exists()){
                //目录不存在，需要创建
                dir.mkdirs();
            }
            try {
                //5. 将临时文件转存到指定位置
                file.transferTo(new File(basedir + fileName));
                return fileName;
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
    @PostMapping("/uploadTrain")
    public Result uploadTrain(HttpServletRequest request,MultipartFile file){
        String fileName = upload(file);
        // 把文件名存到Session
        HttpSession session = request.getSession();
        session.setAttribute("train_Url", fileName);
        return new Result(Code.UPLOAD_OK,"success");
    }

    /**
    * @Description: 上传参数文件 1. 上传到服务器，返回一个UUID 2. 把该UUID写进Session的属性中（参数和训练属性名不同）
    * @Param: request 前端请求
    * @Param: file 参数文件
    * @Return:
    */
    @PostMapping("/uploadParam")
    public Result uploadParam(HttpServletRequest request,MultipartFile file){
        String fileName = upload(file);
        // 把文件名存到Session
        HttpSession session = request.getSession();
        session.setAttribute("param_Url", fileName);
        return new Result(Code.UPLOAD_OK,"success");
    }

    /**
    * @Description: 数据回显 从Session中获取展示数据，返回给前端，结合mounted使用
    * @Param: request 前端mounted发送的异步请求
    * @Return: Result
    */
    @GetMapping("/display")
    public Result display(HttpServletRequest request){
        try{
            HttpSession session = request.getSession();
            // 1. 从Session获取数据 return给用户
            String imgUrl = (String) session.getAttribute("imgUrl");
            imgUrl = genPath + imgUrl;
            System.out.println("=============display: imgUrl==============");
            System.out.println(imgUrl);
            return new Result(Code.TRAIN_OK, "success", imgUrl);
        }catch (Exception e){
            throw new BusinessException("Fail to generate the result, please check the format of your file",Code.BUSINESS_ERR);
        }
    }

    /**
    * @Description: 在Session中写入生成的文件的URL
    * @Param: 前端发送的get请求
    * @Return: Request
    */
    @GetMapping("/giveLink")
    public Result mountLink(HttpServletRequest request){
        try{
            System.out.println(" ==== SendLink... 获取结果文件的url==== ");
            HttpSession session = request.getSession();
            String resultUrl_1 = (String) session.getAttribute("resultUrl_1");
            String resultUrl_2 = (String) session.getAttribute("resultUrl_2");
            // 存储session时是存储的文件名 这里要告诉前端的URL的位置 所以要加上根目录
            resultUrl_1 = genPath + resultUrl_1;
            System.out.println(resultUrl_1);
            resultUrl_2 = genPath + resultUrl_2;
            // 到了前端 我们要告诉哪一个是哪一个的文件
            ResultUrl resultUrl= new ResultUrl();
            resultUrl.setLink1(resultUrl_1);
            resultUrl.setLink2(resultUrl_2);
            return new Result(Code.TRAIN_OK,"Success",resultUrl);
        }catch (Exception e){
            throw new SystemException("Network connection failed", Code.SYSTEM_ERR);
        }

    }
}
