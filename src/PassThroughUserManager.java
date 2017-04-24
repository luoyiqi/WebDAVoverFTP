import org.apache.commons.codec.digest.DigestUtils;
import org.apache.ftpserver.command.impl.SYST;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.usermanager.AnonymousAuthentication;
import org.apache.ftpserver.usermanager.PasswordEncryptor;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.AbstractUserManager;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;

import java.util.Arrays;

/**
 * Created by Koen on 23-4-2017.
 */
public class PassThroughUserManager extends AbstractUserManager {
    private BaseUser user;

    public PassThroughUserManager(String adminName, PasswordEncryptor passwordEncryptor) {
        super(adminName, passwordEncryptor);

        user = new BaseUser();
        user.setAuthorities(Arrays.asList(new Authority[] {new ConcurrentLoginPermission(10, 10)}));
        user.setEnabled(true);
    }

    @Override
    public User getUserByName(String username) throws FtpException {
        user.setHomeDirectory(Main.BASE_PATH + DigestUtils.md5Hex(username).toUpperCase());
        user.setName(username);
        //System.out.println(DigestUtils.md5Hex(username).toUpperCase());
        return user;
    }

    @Override
    public String[] getAllUserNames() throws FtpException {
        return new String[] {""};
    }

    @Override
    public void delete(String username) throws FtpException {
        //no opt
    }

    @Override
    public void save(User user) throws FtpException {
        //no opt
        System.out.println("save");
    }

    @Override
    public boolean doesExist(String username) throws FtpException {
        return true;
    }

    @Override
    public User authenticate(Authentication authentication) throws AuthenticationFailedException {
        //if(UsernamePasswordAuthentication.class.isAssignableFrom(authentication.getClass())) {
            UsernamePasswordAuthentication upAuth = (UsernamePasswordAuthentication) authentication;

            user.setHomeDirectory(Main.BASE_PATH + DigestUtils.md5Hex(upAuth.getUsername()).toUpperCase());
            user.setName(upAuth.getUsername());
            user.setPassword(upAuth.getPassword());

            return user;
        //}
        //return null;
    }
}
