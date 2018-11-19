package com.ccnu.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by 董乐强 on 2018/11/17.
 */
public class ServerDemo {

    public static void main(String[] args) throws IOException {
        //服务端，监听个端口
        ServerSocket serverSocket = new ServerSocket(9898);

        while(true){
            //与客户端建立连接，并获取套接字socket
            final Socket socket = serverSocket.accept();

            System.out.println("新的客户端与服务端建立连接了");

            //开启线程来为客户端服务，每个客户端对应一个线程
            new Thread(new Runnable() {
                public void run() {
                    try {
                        InputStream is = socket.getInputStream();
                        OutputStream os = socket.getOutputStream();
                        //os.write("hello postman".getBytes("utf-8"));
                        //os.flush();
                        byte [] b = new byte[1024];
                        int len = 0 ;
                        StringBuilder sb = new StringBuilder();
                        while((len = is.read(b))!=-1){
                            String info = new String(b,0,len,"utf-8");
                            sb.append(info);
                        }
                        System.out.println(Thread.currentThread().getName()+": "+sb.toString());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }

    }
}
