package com.ccnu.nio;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by 董乐强 on 2018/11/17.
 */
public class NioServer {

     private Selector selector; //通道管理器，多个用户共用，所以需要作为全局的

    /**
     * 初始化服务端ServerSocketChannel通道，并初始化选择器
     * 获得一个ServerSocket通道，并对该通道做一些初始化操作
     * @param port
     * @throws IOException
     */
    public void initServer(int port) throws IOException{
        //获取ServerSocket通道，相对于传统的ServerSocket
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        //设置通道为非阻塞
        serverChannel.configureBlocking(false);
        //将该通道对应的ServerSocket绑定到port端口
        serverChannel.socket().bind(new InetSocketAddress(port));
        //获取一个通道选择器
        this.selector = Selector.open();
        //将通道选择器和该通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件
        //注册事件后，当该事件到达时，selector.select()会返回，如果该事件没有到达
        //selector.select()会一直阻塞
        //意思是大门交给selector看着，给我监听是否有accpet事件
        /**
         * SelectionKey中定义的四种事件
         * OP_ACCEPT:接收连接继续事件，表示服务器监听到了客户连接，服务器可以接收这个连接
         * OP_CONNECT: 连接就绪事件，表示客户端与服务器的连接已经建立成功
         * OP_READ: 读就绪事件，表示通道已经有了可读数据，可以执行读操作（通道目前有数据了，可以进行读操作了）
         * OP_WRITE： 写就绪事件，表示已经可以向通道写数据了（通道目前可以用于写操作）
         */
        serverChannel.register(this.selector, SelectionKey.OP_ACCEPT);


    }

    /**
     * 采用轮询的方式监听selector上是否有需要处理的事件，如果有，则进行处理
     * @throws IOException
     */
    public void listenSelector() throws IOException{
        //轮询访问selector
        while(true){
            //当注册的事件到达时，方法返回：否则，该方法会一直阻塞
            //多路复用 Reactor
            this.selector.select();
            //无论是否有多写事件发生，selector每隔1s被唤醒一次
            //this.selector.select(1000);
            //this.selector.selectNow()
            Iterator<SelectionKey> iteratorKey = this.selector.selectedKeys().iterator();

            while(iteratorKey.hasNext()){
                SelectionKey selectionKey = iteratorKey.next();
                //删除已选的key，以防重复处理
                iteratorKey.remove();
                handler(selectionKey);
            }
        }
    }

    /**
     * 处理请求
     * @param selectionKey
     */
    private void handler(SelectionKey selectionKey) throws IOException {
        //处理客户端连接请求
        if(selectionKey.isAcceptable()){
            System.out.println("新的客户端连接...");
            ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
            //获取和客户端连接的通道
            //完成该操作意味着完成TCP三次握手，TCP物理链路正式建立
            SocketChannel channel = server.accept();
            //设置非阻塞模式
            channel.configureBlocking(false);
            //在和客户端连接成功后，为了可以接收客户端的信息，需要给通道设置读的权限
            channel.register(this.selector,SelectionKey.OP_READ);
        }
        //处理读事件
        else if(selectionKey.isReadable()){
            /*=======================读写数据都是通过ByteBuffer这缓冲区， 成块成块的读写，提供性能==========================*/
            //服务器可读取消息：得到事件发生的Socket通道
            SocketChannel channel = (SocketChannel) selectionKey.channel();

            //创建读取的缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            int readData = channel.read(buffer);

            if(readData>0){
                //先将缓冲区的数据转换为byte数组，在转换为成String
                String msg = new String(buffer.array(),"GBK").trim();
                System.out.println("服务端收到信息："+Thread.currentThread().getName()+"============="+msg);
                //回写数据
                ByteBuffer writeBackBuffer = ByteBuffer.wrap("receive data".getBytes("GBK"));
                channel.write(writeBackBuffer);//将消息回送给客户端
            }else{
                System.out.println("客户端关闭...");
                //SelectionKey对象会失效，这意味着Selector再也不会监控与它相关的事件
                selectionKey.cancel();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        NioServer nioServer = new NioServer();
        //初始化服务端
        nioServer.initServer(8899);

        nioServer.listenSelector();
    }
}
