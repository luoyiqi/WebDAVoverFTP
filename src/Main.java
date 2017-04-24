import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.ClearTextPasswordEncryptor;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;

import java.io.*;
import java.util.*;

public class Main {

    public static final String BASE_PATH = "folders/";

    public static void main(String[] args) {
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(3033);
        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setAnonymousLoginEnabled(true);
        serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());
        serverFactory.addListener("default", listenerFactory.createListener());
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        userManagerFactory.setPasswordEncryptor(new ClearTextPasswordEncryptor());
        serverFactory.setUserManager(new PassThroughUserManager("admin", new ClearTextPasswordEncryptor()));
        Map<String, Ftplet> m = new HashMap<String, Ftplet>();
        m.put("miaFtplet", new Ftplet()
        {

            @Override
            public void init(FtpletContext ftpletContext) throws FtpException {
                //System.out.println("init");
                //System.out.println("Thread #" + Thread.currentThread().getId());
            }

            @Override
            public void destroy() {
                //System.out.println("destroy");
                //System.out.println("Thread #" + Thread.currentThread().getId());
            }

            @Override
            public FtpletResult beforeCommand(FtpSession session, FtpRequest request) throws FtpException, IOException
            {
                System.out.println("beforeCommand " + session.getUserArgument() + " : " + session.toString() + " | " + request.getArgument() + " : " + request.getCommand() + " : " + request.getRequestLine());
                System.out.println("Thread #" + Thread.currentThread().getId());

                String requestArgument = request.getRequestLine();
                if(requestArgument.contains(" ")){
                    requestArgument = requestArgument.split(" ",2)[1];
                }

                String command = request.getCommand();
                if (command.equals("PASS")) {
                    try {
                        String[] userConnection = session.getUserArgument().split("@", 2);
                        WebDAVSession webDAVSession = new WebDAVSession(userConnection[1], userConnection[0], requestArgument);
                        session.setAttribute("login", webDAVSession);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if (command.equals("CWD")) {
                    WebDAVSession webDAVSession = (WebDAVSession) session.getAttribute("login");
                    webDAVSession.cwd(requestArgument);
                } else if (command.equals("CDUP")) {
                    WebDAVSession webDAVSession = (WebDAVSession) session.getAttribute("login");
                    int index = webDAVSession.getCwd().lastIndexOf("/");
                    webDAVSession.cwd(webDAVSession.getCwd().substring(0, index));
                } else if (command.equals("RETR")) {
                    WebDAVSession webDAVSession = (WebDAVSession) session.getAttribute("login");
                    webDAVSession.get(requestArgument);
                }

                //do something
                return FtpletResult.DEFAULT;//...or return accordingly
            }

            @Override
            public FtpletResult afterCommand(FtpSession session, FtpRequest request, FtpReply reply) throws FtpException, IOException
            {
                //System.out.println("afterCommand " + session.getUserArgument() + " : " + session.toString() + " | " + request.getArgument() + " : " + request.getCommand() + " : " + request.getRequestLine() + " | " + reply.getMessage() + " : " + reply.toString());
                //System.out.println("Thread #" + Thread.currentThread().getId());

                //do something
                return FtpletResult.DEFAULT;//...or return accordingly
            }

            @Override
            public FtpletResult onConnect(FtpSession session) throws FtpException, IOException
            {
               // System.out.println("onConnect " + session.getUserArgument() + " : " + session.toString());
                //System.out.println("Thread #" + Thread.currentThread().getId());

                //do something
                return FtpletResult.DEFAULT;//...or return accordingly
            }

            @Override
            public FtpletResult onDisconnect(FtpSession session) throws FtpException, IOException
            {
                try {
                    WebDAVSession webDAVSession = (WebDAVSession) session.getAttribute("login");
                    FileUtils.deleteDirectory(new File(Main.BASE_PATH + webDAVSession.getHashedUsername()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                //System.out.println("onDisconnect " + session.getUserArgument() + " : " + session.toString());
                //System.out.println("Thread #" + Thread.currentThread().getId());

                //do something
                return FtpletResult.DEFAULT;//...or return accordingly
            }
        });
        serverFactory.setFtplets(m);
        //Map<String, Ftplet> mappa = serverFactory.getFtplets();
        //System.out.println(mappa.size());
        //System.out.println("Thread #" + Thread.currentThread().getId());
        //System.out.println(mappa.toString());
        FtpServer server = serverFactory.createServer();
        try
        {
            server.start();//Your FTP server starts listening for incoming FTP-connections, using the configuration options previously set
        }
        catch (FtpException ex)
        {
            //Deal with exception as you need
        }
    }
}
