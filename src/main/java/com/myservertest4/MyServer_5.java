package com.myservertest4;

import com.sun.net.httpserver.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class MyServer_5 implements HttpHandler{
    
    private String source = "C:\\Users\\vitck\\Documents\\NetBeansProjects\\JavaCoreNew\\2022\\SomeHTTP\\src\\main\\java\\com\\somehttp\\front";
    private Controller cont = new Controller(source);
    private MyHTTPController controller;
    public static void main(String[] args){
        HttpServer server=new MyServer_5().getServer(8500, "/server");
        System.out.println("Start server");
        server.start();
    }
    public HttpServer getServer(int port, String path) {
            HttpServer server = null;
        try {
            server=HttpServer.create(new InetSocketAddress(port),0);
            HttpContext context=server.createContext(path);
            context.setHandler(this);
        } catch(IOException e) {
            System.out.println("Somes wrong in crate server");
        }
//        MyHTTPController controller = new MyHTTPController(source,"/server");
        controller = new MyHTTPController(source,"/server");
        controller.printPath();
        return server;
    }
    
    @Override
    public void handle(HttpExchange exchange) {
        try {
//            System.out.println("requestParam " +getRequestParams(exchange));
//            System.out.println("requestBody "+getRequestBody(exchange));
            String sendBack = "This String we are sending back as responce, and something mo to back 8)";
//            setResponce(exchange,sendBack);
//            setResponceFile(exchange,"index.html");
            String src = exchange.getRequestURI().getPath();
            System.out.println("my src = "+src);
//            setResponceFile(exchange,cont.mapSource(src));
            responseFileBinary(exchange,cont.mapSource(src));
//            controller.mapSource(exchange);
//            setResponce(exchange,getHtmlTemplate(getRequestParams(exchange)));
        } catch(IOException e) {
            System.out.println("Handler Error "+e);
        }
    }
    public String getRequestParams(HttpExchange exchange) {
        return Optional.ofNullable(exchange.getRequestURI().getQuery())
                .orElseGet(()-> "query is empty");
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
        outputStream.close();
    }
    public void responseFileBinary(HttpExchange exchange, String src) throws IOException {
        File myFile = new File(src);
        System.out.println("Length of file "+myFile.length());
        byte[] bytes = new byte[(int) myFile.length()];
        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(myFile))) {
            bis.read(bytes,0,bytes.length);
        } catch(IOException e) {
            System.out.println("Wrong inside response file binary "+e);
        }
        System.out.println("Length of array bytes "+bytes.length);
        exchange.sendResponseHeaders(200,bytes.length);
        OutputStream outputStream=exchange.getResponseBody();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }
    
}

class MyHTTPController {
    private String source;
//            "C:\\Users\\vitck\\Documents\\NetBeansProjects\\JavaCoreNew\\2022\\SomeHTTP\\src\\main\\java\\com\\somehttp\\front";
    private String context;
    
//    public Set<Byte[]> bytes = new HashSet<>();
//    private List<byte[]> bytes;
//    public List<Path> pathNames = new ArrayList<>();
    private List<Path> pathNames;
    private HashMap<Path, byte[]> map = new HashMap<>();
    
    public MyHTTPController(String source,String context){
        this.source=source;
        this.context=context;
        getPathNames(source);
        getFileAsByte();
//        getFileAsByte(wrapException(p -> Files.readAllBytes(p)));
//        try (Stream<Path> find=Files.find(Paths.get(source),Integer.MAX_VALUE,(p, a) -> !a.isDirectory())) {
//            find.forEach(f -> pathNames.add(f.getFileName()));
//        } catch(IOException e) {
//            System.out.println("Wrong inside read file");
//        }
    }
    
    private void getPathNames(String source) {
        try (Stream<Path> find=Files.find(Paths.get(source),Integer.MAX_VALUE,(p, a) -> !a.isDirectory())) {
            pathNames = find.toList();
        } catch(IOException e) {
            System.out.println("Wrong inside getPathNames");
        }
    }
    private void getFileAsByte() {
        System.out.println("inside getFileAsByte()");
        pathNames.stream().forEach(wrapLambdaException);
    }
    private Consumer<Path> wrapLambdaException = p -> {
            try { 
                map.put(p.getFileName(),Files.readAllBytes(p));
            }catch(IOException e) {
                System.out.println("Wrong in wraper Exception");
            }
    }; 
    
//    private Consumer<Path> wrapException(Path cons) {
//        return (t) -> {
//            System.out.println("inside  wrapException(Path cons) ");
//            try {
//                byte[] b = Files.readAllBytes(cons);
////                byte[] b = Files.readAllBytes(t);
////                map.put(t.getFileName(),b);
//                System.out.println("b.length  -- "+b.length);
//                map.put(cons.getFileName(),b);
//            }catch(IOException e) {
//                System.out.println("Wrong in wraper Exception");
//            }
//        };
//    }
    
    public void printPath() {
        System.out.println("inside print, size of pathsNames = "+pathNames.size());
        pathNames.forEach(System.out::println);
        System.out.println("inside print, size of map = "+map.size());
        map.forEach((l,m) -> System.out.println("key = "+l+", val size = "+m.length));
//        map.
    }
     public void mapSource(HttpExchange exchange) throws IOException {
        String path=exchange.getRequestURI().getPath();
        byte[] response = null;
         System.out.println("Path is "+path);
        if(!path.contains(context)) {
            response = map.get("error.html");
            System.out.println(response.length);
        }
        path = path.replace(context,"");
        System.out.println("Path respaced is "+path);
        if(path.isEmpty() || path.startsWith("/index")) {
            response = map.get(path);
            System.out.println(response.length);
        }
        if(path.equals("/logo.css") || path.equals("/mycss.css")) {
            response = map.get("/css"+path);
            System.out.println(response.length);
        }
        if(path.startsWith("/img") && (path.endsWith(".png") || path.endsWith(".jpg") )) {
            response = map.get("img"+path);
            System.out.println(response.length);
        }
        try {
        exchange.sendResponseHeaders(200,response.length);
        OutputStream outputStream=exchange.getResponseBody();
        outputStream.write(response);
        outputStream.flush();
        outputStream.close();
        } catch(IOException e) {
            System.out.println("Somes wrong in controller NEW");
        }
     }
    
}

class Controller {
    private StringBuffer context;
    private String error = "/error.html";
    private String page = "/index.html";
    private String css = "/css";
    private String js = "/js";
    private String img = "/img";
    public Controller(String context) {
        this.context = new StringBuffer(context);
    }
    public String getContext(){
        return context.toString();
    }

    public String getPages(String str){
        return context.append(str).toString();
    }
    public String mapSource(String str) {
        String path = str.replace("/server","");
        StringBuffer source = new StringBuffer(context);
        System.out.println("path = "+path);
        if(path.isEmpty() || path.startsWith("/index")) {
            return source.append(page).toString();
        }
        if(path.equals("/logo.css") || path.equals("/mycss.css")) {
            return source.append(css).append(path).toString();
        }
        if(path.startsWith("/img") && (path.endsWith(".png") || path.endsWith(".jpg") )) {
            System.out.println("Inside img");
            return source.append(path).toString();
        } else {
            return source.append(error).toString();
        }
    }
}