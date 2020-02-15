package cn.itrip.auth.service;

import cn.itrip.auth.exception.UserLoginFailedException;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.common.EmptyUtils;
import cn.itrip.common.MD5;
import cn.itrip.common.RedisAPI;
import cn.itrip.dao.user.ItripUserMapper;
import cn.itrip.dao.userlinkuser.ItripUserLinkUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("userService")
public class UserServiceImpl implements UserService {
    @Resource
    private ItripUserMapper itripUserMapper;
    @Resource
    private RedisAPI redisAPI;
    @Resource
    private MailService mailService;
    @Resource
    private SmsService smsService;
    @Resource
    private ItripUserLinkUserMapper itripUserLinkUserMapper;
    private int expire=5;//过期时间（分钟）
    /**
     * 创建邮箱用户
     * @param user
     * @throws Exception
     */
    @Override
    public void itriptxCreateUser(ItripUser user) throws Exception {
       //发送激活邮件j进行加密
        System.out.println("**"+user.getUserCode());
        String activationCode=MD5.getMd5(new Date().toLocaleString(),32);
        mailService.sendActivationMail(user.getUserCode(),activationCode);//进入激活邮箱传入邮箱账号和密码
        //保存用户信息
        itripUserMapper.insertItripUser(user);
    }
    /**
     * 创建手机账号
     */
    @Override
    public void itriptxCreateUserByPhone(ItripUser user) throws Exception {
        //发送短信验证码
        String code=String.valueOf(MD5.getRandomCode());
        System.out.println("**"+user.getUserCode());
        smsService.send(user.getUserCode(), "1", new String[]{code,String.valueOf(expire)});
        //缓存验证码
        String key="activation:"+user.getUserCode();
        redisAPI.set(key,expire*60,code);
        //保存用户信息
        itripUserMapper.insertItripUser(user);
    }


    public void updateUser(ItripUser user) throws Exception {
        itripUserMapper.updateItripUser(user);
    }

    public void deleteUser(Long userId) throws Exception {
        itripUserMapper.deleteItripUserById(userId);
    }
    /**
     * 根据用户名查找用户
     * @param username
     * @return
     * @throws Exception
     */
    public ItripUser findByUsername(String username) throws Exception {
        Map<String, Object> param=new HashMap();
        param.put("userCode", username);
        List<ItripUser> list= itripUserMapper.getItripUserListByMap(param);
        if(list.size()>0)
            return list.get(0);
        else
            return null;
    }

    /**
     * 激活邮箱用户
     */
    @Override
    public boolean activate(String email, String code) throws Exception {
        String key="activation:"+email;
        System.out.println("邮箱号"+email+redisAPI.exist(key)+"键"+code+"key"+redisAPI.get(key));
        if(redisAPI.exist(key))
            if(redisAPI.get(key).equals(code)){
                ItripUser user=this.findByUsername(email);
                if(EmptyUtils.isNotEmpty(user))
                {
                    System.out.println("用户邮箱验证已通过："+email);
                    user.setActivated(1);//激活用户
                    user.setUserType(0);//自注册用户
                    user.setFlatID(user.getId());
                    itripUserMapper.updateItripUser(user);
                    return true;
                }
            }
        return false;
    }

    /**
     * 短信验证手机号
     * @throws Exception
     */
    public boolean validatePhone(String phoneNumber,String code)throws  Exception{
        String key="activation:"+phoneNumber;
        System.out.println("手机号"+phoneNumber+redisAPI.exist(key)+"键"+code+"key"+redisAPI.get(key));
        if(redisAPI.exist(key))
            if(redisAPI.get(key).equals(code)){
                ItripUser user=this.findByUsername(phoneNumber);
                if(EmptyUtils.isNotEmpty(user))
                {
                    System.out.println("用户手机验证已通过："+phoneNumber);
                    user.setActivated(1);//激活用户
                    user.setUserType(0);//自注册用户
                    user.setFlatID(user.getId());
                    itripUserMapper.updateItripUser(user);
                    return true;
                }
            }
        return false;
    }
    /**
     * login()判断登录
     * generateToken生成token
     * save()保存token到redis缓存数据库中
     * @throws Exception
     */
    @Override
    public ItripUser login(String name, String password) throws Exception {
        ItripUser user=this.findByUsername(name);//查找数据库有没有账号
        if(null!=user && user.getUserPassword().equals(password)){
            if(user.getActivated()!=1){//判断数据库该字段是否等于1
                   throw new UserLoginFailedException("用户为激活");
            }
            return  user;
        }     else

            return null;
    }
}
