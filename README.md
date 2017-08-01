# optimizer
android图片优化插件
下载:
jpg压缩 guetzli  https://github.com/google/guetzli/releases
png压缩 pngcrush  https://pmt.sourceforge.io/pngcrush/
webp转换	 https://storage.googleapis.com/downloads.webmproject.org/releases/webp/index.html

注意:
pngcrush没有找到直接的linux与mac的命令行,直接下载源码在源码目录下使用make就能够编译出来



pngcrush  -brute -rem alla -reduce -q in.png out.png
-brute	从136种方案中查找最合适的方法
-q   安静的，不输出详情
-reduce 排除没用的颜色并降低色深
-rem alla 移除无用的数据,保留透明度

guetzli --quality quality  in.jpg out.jpg
--quality 质量不得小于84

cwebp  -q quality in.png -o out.webp
-q 质量，0-100之间。google给出75是最佳质量
android 14以上原生支持webp;支持显示含透明度的WebP需要18


png格式: PNG文件标志+数据块+...+数据块
头
89 50 4E 47 0D 0A 1A 0A

数据块格式:
名称								字节数				说明
Length (长度)					  4					指定数据块中数据域的长度，其长度不超过(231－1)字节
Chunk Type Code (数据块类型码)	  4					数据块类型码由ASCII字母(A-Z和a-z)组成
Chunk Data (数据块数据)			  n					可变长度	存储按照Chunk Type Code指定的数据
CRC (循环冗余检测)				  4					存储用来检测是否有错误的循环冗余码


IHDR:文件头数据块格式
域的名称  						字节数				说明
Width							  4					图像宽度,以像素为单位
Height							  4					图像高度,以像素为单位
Bit depth						  1					图像深度:索引彩色图像：1，2，4或8;
															灰度图像：1，2，4，8或16;
															真彩色图像：8或16
ColorType						  1 				颜色类型：
															0：灰度图像, 1，2，4，8或16 
															2：真彩色图像，8或16 
															3：索引彩色图像，1，2，4或8 
															4：带α通道数据的灰度图像，8或16 
															6：带α通道数据的真彩色图像，8或16
Compression method				  1 				压缩方法(LZ77派生算法)
Filter method					  1 				滤波器方法
Interlace method				  1 				隔行扫描方法：
															0：非隔行扫描 
															1： Adam7(由Adam M. Costello开发的7遍隔行扫描方法)











