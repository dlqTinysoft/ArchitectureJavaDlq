package com.ccnu.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Created by 董乐强 on 2018/11/17.
 * bio模式，服务端
 */
public class Server {


    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(7777);
        System.out.println("服务端启动...");
        while(true){
            //这个位置是阻塞模式，需要有客户端请求建立连接，才继续往下执行
            Socket socket = serverSocket.accept();//获取套接字
            InputStream is = socket.getInputStream();//获取管道输入流，从管道中读取数据
            OutputStream os = socket.getOutputStream();
            //必须给客户端响应
            os.write("dddd".getBytes("utf-8"));
            os.flush();
            byte[] b = new byte[1024];
            int len = 0 ;
            StringBuilder sb = new StringBuilder();
            //is.read(b) 也是阻塞模式，只要把数据读取完了，才能继续往下执行
            while((len = is.read(b))!=-1){
                String info = new String(b,0,len, "UTF-8");
                sb.append(info);
                //必须给客户端响应，要不客户端会一直等着给响应，一直不释放，所以下面这两行代码不能省，在哪个位置给响应都可以的
                //os.write(info.getBytes("utf-8"));
                //os.flush();
            }
            System.out.println(sb.toString());//读取接收的数据
        }
    }




}
