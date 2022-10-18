这是一个生物信息网站，主要功能是让用户上传训练数据、调整训练参数、回显训练结果、重新调参、最终下载结果文件。

| 类型 | 技术          |
| ---- | ------------- |
| 前端 | Vue+ElementUI |
| 后端 | SpringBoot              |

# 1 文件上传



1. 模型描述：对你的模型总设计/处理流程进行一个概况描述。
2. 进度提示：提示用户处于操作流程的哪个阶段/
3. 模型展示：使用走马灯展示你的模型，可以自己调整图片张数和内容。
4. 训练文件上传：前端js代码对文件的文件类型（后缀判断）和文件大小进行了校验，可根据自己的需要处理。

![image](https://user-images.githubusercontent.com/71913581/188100957-026c3f29-876d-4a5b-9b1f-545c20512642.png)


# 2 参数调整

1. 可使用参数文件上传的方式进行训练，同样在前端对文件类型和文件大小做了校验，根据自己需要调整
2. 可使用在线调整参数的方式进行训练，两个方式只能选择一个，参数根据自己的模型设置名称

![Pasted image 20220901134330](https://user-images.githubusercontent.com/81914075/187856167-b329dc18-2b33-4726-a71c-93cc6afaa99d.png)

# 3 结果展示

1. 展示训练结果
2. 根据训练结果，用户选择是继续调整参数再训练，还是结束训练，下载结果

![Pasted image 20220901135102](https://user-images.githubusercontent.com/81914075/187856180-a6b4f286-929a-42eb-9d2b-2f3de3a04e2f.png)

# 4 文件下载

![Pasted image 20220901140101](https://user-images.githubusercontent.com/81914075/187856196-c91e0105-2a52-44f7-bd28-373741c63ce9.png)

# 5 注意事项

1. 该项目是使用Session进行数据的传输，用户上传的文件转存到本地，生成一个UUID，该UUID保存在Session中，当用户再次调参时，根据该ID就能找到训练文件，不用再次上传，但是用户离开页面后就会失去数据。
2. 你的训练脚本生成的`训练结果（图片URL）`、`生成的结果文件（文件URL）`都需要一个`UUID`来保证结果的唯一性，否则可能被覆盖。
3. 需要在application.yml中配置对应的位置。

```yml
# 上传文件根目录  
bio:  
  # 用户文件上传的地址  
  uploadPath: http://127.0.0.1:8887/upload/  
  # 脚本生成结果的地址  
  genPath: http://127.0.0.1:8887/result/  
  # 使用文件训练的脚本地址  
  exePath: http://127.0.0.1:8887/exec/blastNew.py  
  # 使用参数训练的脚本地址  
  exeParamPath: http://127.0.0.1:8887/exec/blastNewParam.py  
  # 你的脚本执行方式  
  exeMethod: python
```

