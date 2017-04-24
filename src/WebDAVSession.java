import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by Koen on 23-4-2017.
 */
public class WebDAVSession {
    private String url = null;
    private String username = null;
    private String password = null;
    private String cwd = null;

    public WebDAVSession(String url, String username, String password)
    {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl(){
        return this.url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public String getCwd() {
        return cwd;
    }

    public String getHashedUsername() {
        return DigestUtils.md5Hex(this.username + "@" + this.url).toUpperCase();
    }

    public String encode(String text) {
        try {
            return URLEncoder.encode(text.replaceAll(" ", "%20"), "UTF-8").replaceAll("%2F", "/").replaceAll("%2520", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return text;
        }
    }

    public void cwd(String argument)
    {
        if(argument.startsWith("/")){
            this.cwd = argument;
        } else {
            this.cwd += "/" + argument;
        }

        try {
            Sardine sardine = SardineFactory.begin(this.username, this.password);
            System.out.println("CWD: " + this.url + "/" + encode(this.cwd));
            List<DavResource> resources = sardine.list(this.url + "/" + encode(this.cwd) + "/", 1);
            for (DavResource res : resources) {
                File f = new File(Main.BASE_PATH + this.getHashedUsername() + res.getPath());

                f.getParentFile().mkdirs();
                if (res.isDirectory()) {
                    f.mkdir();
                } else {
                    f.createNewFile();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void get(String argument)
    {
        Sardine sardine = SardineFactory.begin(this.username, this.password);
        String requestFile = "/" + argument.replaceAll("^/+", "");
        try {
            InputStream inputStream = sardine.get(this.url + encode(this.cwd) + encode(requestFile));
            String outputPath = Main.BASE_PATH + this.getHashedUsername() + this.cwd + requestFile;
            System.out.println("GET: " + outputPath);
            File output = new File(outputPath);
            FileUtils.copyInputStreamToFile(inputStream, output);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
