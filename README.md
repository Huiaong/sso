# SSO

SSO，单点登录Demo

## 运行前

-----

运行前请确认您的本地是否安装了以下：

| 应用            | 版本   |
| --------------- | ------ |
| JDK             | 1.8    |
| Redis           | 6.0    |
| Nginx | 1.16  |

## 运行

> 修改本地Hosts文件

增加：

```
127.0.0.1 www.app1.com
127.0.0.1 www.app2.com
127.0.0.1 www.sso.com
```

> docker 启动 nginx

```bash
docker pull nginx:1.16.1
cd ${your project home}
docker run -p 80:80 -d --name nginx \
	-v ${pwd}/nginx/conf/:/etc/nginx/ \
	-v ${pwd}/nginx/html/:/usr/share/nginx/html/ \
	nginx:latest
```

> 直接启动 nginx

修改项目中 `nginx/config/nginx.conf` 文件的 `root` 路径为您的本地路径

替换 `nginx.conf` 为项目中 `nginx/config/nginx.conf`

> 启动

运行 `SsoApplication.java` 的 `main` 方法 