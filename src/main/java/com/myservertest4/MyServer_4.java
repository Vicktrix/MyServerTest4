package com.myservertest4;

import com.sun.net.httpserver.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class MyServer_4 implements HttpHandler{

    public static void main(String[] args){
        HttpServer server=new MyServer_4().getServer(8500);
        System.out.println("Start server");
        server.start();
    }
    public HttpServer getServer(int port) {
            HttpServer server = null;
        try {
            server=HttpServer.create(new InetSocketAddress(port),0);
            HttpContext context=server.createContext("/");
            context.setHandler(this);
        } catch(IOException e) {
            System.out.println("Somes wrong in crate server");
        }
        return server;
    }
    
    @Override
    public void handle(HttpExchange exchange) {
        try {
            System.out.println("requestParam " +getRequestParams(exchange));
            System.out.println("requestBody "+getRequestBody(exchange));
            String sendBack = "This String we are sending back as responce, and something mo to back 8)";
//            setResponce(exchange,sendBack);
//            setResponceFile(exchange,"index.html");
            setResponce(exchange,getHtmlTemplate(getRequestParams(exchange)));
        } catch(IOException e) {
            System.out.println("Handler Error "+e);
        }
    }
    public String getRequestParams(HttpExchange exchange) {
        return exchange.getRequestURI().toString().split("\\?")[1];
    }
    public String getRequestBody(HttpExchange exchange) throws IOException {
        StringBuffer sbuf = new StringBuffer();
        for(byte b : exchange.getRequestBody().readAllBytes()) 
            sbuf.append((char)b);
        return sbuf.toString();
    }
    public void setResponce(HttpExchange exchange, String str) throws IOException {
        exchange.sendResponseHeaders(200, str.length());
        var outputStream = exchange.getResponseBody();
        outputStream.write(str.getBytes());
        outputStream.flush();
    }
    public String getHtmlTemplate(String str) {
        StringBuffer sbuf = new StringBuffer();
         return sbuf.append("<html>")
            .append("<head>")
                .append("<style>").append("h1 { color : red; }").append("</style>")
            .append("</head>")
            .append("<body>")
                .append("<h1>").append("Hello ").append(str).append("</h1>")
            .append("</body>")
            .append("</html>").toString();
    }
    public void setResponceFile(HttpExchange exchange, String src) throws IOException {
        String line;
        StringBuffer resp = new StringBuffer("");
        File myFile = new File(src);
        try(BufferedReader bufferedReader = 
            new BufferedReader(new InputStreamReader(new FileInputStream(myFile)))) {
            while ((line = bufferedReader.readLine()) != null) {
                resp.append(line);
            }
        } catch (IOException e) {
            System.out.println("Something wrong with open file "+e+" StackTrace.. \n\n");
        }
        exchange.sendResponseHeaders(200,resp.length());
        OutputStream outputStream=exchange.getResponseBody();
        outputStream.write(resp.toString().getBytes());
        outputStream.flush();
//        outputStream.close();
    }
}