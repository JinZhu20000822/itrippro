package cn.itrip.auth.controller;

import cn.itrip.auth.service.MailService;
import cn.itrip.auth.service.SmsService;
import cn.itrip.auth.service.UserService;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.vo.userinfo.ItripUserVO;
import cn.itrip.common.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import cn.itrip.beans.dto.Dto;

import javax.annotation.Resource;
import java.util.regex.Pattern;

/**
 * 用户管理控制器
 * @author hduser
 *
 */
@Controller
@RequestMapping(value = "/api")
public class UserController {
    @Resource
    private UserService userService;

    //然后本人发现他的邮箱和手机都在一个数据库表 itrip_user 里面，数据库里面有二张表，



    // 注意我柱某人通过网上查找到了@RequestBody是作用在形参列表上，用于将前台发送过来固定格式的数据【xml 格式或者 json等】
    // 封装为对应的 JavaBean 对象，封装时使用到的一个对象是系统默认配置的 HttpMessageConverter进行解析，然后封装到形参上。
    //他的前台项目的js要跟我们写的json和路径要一样，返回的对象和数据要一样，因为我们改不了前台里面的js格式
    /**
     * 使用手机注册
     * @param userVO
     * @return
     */
    @ApiOperation(value="使用手机注册",httpMethod = "POST",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class,notes="使用手机注册 ")//Swagger生成API文档 protocols = "HTTP"http协议 下面的ApiParams是Swagger注解
    @RequestMapping(value="/registerbyphone",method= RequestMethod.POST,produces = "application/json")
    @ResponseBody
   public Dto registerByPhone(@ApiParam(name="userVO",value="用户实体",required=true) @RequestBody ItripUserVO userVO){
       try {
           if (!validPhone(userVO.getUserCode())) {//调用下面的手机验证方法，格式不对则返回给前台
               return DtoUtil.returnFail("请使用正确的手机号注册", ErrorCode.AUTH_ILLEGAL_USERCODE);
             }
           ItripUser user = new ItripUser();
           user.setUserCode(userVO.getUserCode());
           user.setUserPassword(userVO.getUserPassword());
           user.setUserType(0);
           user.setUserName(userVO.getUserName());
           if (null == userService.findByUsername(user.getUserCode())) {//后台UserService里面的方法进行账号是否已经存在
               user.setUserPassword(MD5.getMd5(user.getUserPassword()+userVO.getUserCode(),32));//md5进行加密处理，长度为32
               userService.itriptxCreateUserByPhone(user);//添加一个手机账号
               return DtoUtil.returnSuccess();
           }else{
               return DtoUtil.returnFail("用户已存在，注册失败", ErrorCode.AUTH_USER_ALREADY_EXISTS);
           }
       }catch (Exception e){
           e.printStackTrace();
           return DtoUtil.returnFail(e.getMessage(), ErrorCode.AUTH_UNKNOWN);
       }
    }
    // 注意@RequestBody是作用在形参列表上，用于将前台发送过来固定格式的数据【xml 格式或者 json等】
    // 封装为对应的 JavaBean 对象，封装时使用到的一个对象是系统默认配置的 HttpMessageConverter进行解析，然后封装到形参上。
        /**
         * 使用邮箱注册
         * @param userVO
         * @return
         */
        @ApiOperation(value="使用邮箱注册",httpMethod = "POST",protocols = "HTTP",produces = "application/json",
        response = Dto.class,notes="使用邮箱注册" )//Swagger生成API文档 protocols = "HTTP"http协议 下面的ApiParams是Swagger注解
        @RequestMapping(value="/doregister",method=RequestMethod.POST,produces = "application/json")
        @ResponseBody
        public  Dto doRegister(@ApiParam(name="userVO",value="用户实体",required=true) @RequestBody ItripUserVO userVO){
            if(!validEmail(userVO.getUserCode())) {//调用下面的邮箱验证方法，格式不对则返回给前台
                return DtoUtil.returnFail("请使用正确的邮箱地址注册", ErrorCode.AUTH_ILLEGAL_USERCODE);
            }else{
             try {
                ItripUser user=new ItripUser();
                user.setUserCode(userVO.getUserCode());
                user.setUserPassword(userVO.getUserPassword());
                user.setUserType(0);
                user.setUserName(userVO.getUserName());
                if(null==userService.findByUsername(user.getUserCode())){//后台UserService里面的方法进行账号是否已经存在
                    user.setUserPassword(MD5.getMd5(user.getUserPassword()+userVO.getUserCode(),32));//md5进行加密处理，长度为32
                    userService.itriptxCreateUser(user);//添加一个邮箱账号
                    return  DtoUtil.returnSuccess();
                }else{
                    return DtoUtil.returnFail("用户已存在，注册失败",ErrorCode.AUTH_USER_ALREADY_EXISTS);
                }
              } catch (Exception e) {
                  e.printStackTrace();
                  return DtoUtil.returnFail(e.getMessage(), ErrorCode.AUTH_UNKNOWN);
              }
            }

        }

    /**			 *
     * 合法E-mail地址：
     * 1. 必须包含一个并且只有一个符号“@”
     * 2. 第一个字符不得是“@”或者“.”
     * 3. 不允许出现“@.”或者.@
     * 4. 结尾不得是字符“@”或者“.”
     * 5. 允许“@”前的字符中出现“＋”
     * 6. 不允许“＋”在最前面，或者“＋@”
     */
    private boolean validEmail(String email){

        String regex="^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$"  ;
        return Pattern.compile(regex).matcher(email).find();//放回数据给手机注册进行判断
    }
    /**
     * 验证是否合法的手机号
     * @param phone
     * @return
     */
      private  boolean validPhone(String phone){
          String regex="^1[3578]{1}\\d{9}$";
          return Pattern.compile(regex).matcher(phone).find();//放回数据给邮箱注册进行判断

      }
    /**
     * 检查用户邮箱是否已注册
     * @param name
     * @return
     */
    @ApiOperation(value="用户名验证",httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class,notes="验证是否已存在该用户名")//Swagger生成API文档 protocols = "HTTP"http协议 下面的ApiParams是Swagger注解
    @RequestMapping(value="/ckusr",method=RequestMethod.GET,produces= "application/json")
    @ResponseBody
    public Dto checkUser(
            @ApiParam(name="name",value="被检查的用户名",defaultValue="jz232459@163.com")//进行打比方
            @RequestParam String name) {
        try {
            if (null == userService.findByUsername(name))//后台UserService里面的方法进行账号是否已经存在
            {
                return DtoUtil.returnSuccess("用户名可用");
            }
            else
            {
                return DtoUtil.returnFail("用户已存在，注册失败", ErrorCode.AUTH_USER_ALREADY_EXISTS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(), ErrorCode.AUTH_UNKNOWN);
        }
    }
    @ApiOperation(value="邮箱注册用户激活",httpMethod = "PUT",
        protocols = "HTTP", produces = "application/json",
        response = Dto.class,notes="邮箱激活")//Swagger生成API文档 protocols = "HTTP"http协议 下面的ApiParams是Swagger注解
    @RequestMapping(value="/activate",method=RequestMethod.PUT,produces= "application/json")
    @ResponseBody
    public Dto activate(
            @ApiParam(name="user",value="注册邮箱地址",defaultValue="2324594295@qq.com")
            @RequestParam String user,
            @ApiParam(name="code",value="激活码",defaultValue="018f9a8b2381839ee6f40ab2207c0cfe")//进行打比方
            @RequestParam String code){
         System.out.println("激活吧"+user);
        try {
            if(userService.activate(user, code))//通过UserService的该方法判断redis里面有没有存进去的key，有返回true无返回false，让后进行激活，数据库则更新
            {   System.out.println("激活");
                return DtoUtil.returnSuccess("激活成功");
            }else{
                System.out.println("激活失败");
                return DtoUtil.returnFail("激活失败", ErrorCode.AUTH_ACTIVATE_FAILED);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return DtoUtil.returnFail("激活失败", ErrorCode.AUTH_ACTIVATE_FAILED);
        }
    }

    @ApiOperation(value="手机注册用户短信验证",httpMethod = "PUT",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class,notes="手机注册短信验证")//Swagger生成API文档 protocols = "HTTP"http协议 下面的ApiParams是Swagger注解
    @RequestMapping(value="/validatephone",method=RequestMethod.PUT,produces= "application/json")
    @ResponseBody
    public  Dto validatePhone(
            @ApiParam(name="user",value="手机号码",defaultValue="18598933274")
            @RequestParam String user,
            @ApiParam(name="code",value="验证码",defaultValue="8888")//进行打比方
            @RequestParam String code){
        System.out.println("激活吧"+user);
        try {
            if(userService.validatePhone(user, code))//通过UserService的该方法判断redis里面有没有存进去的key，有返回true无返回false，让后进行激活，数据库则更新
            { System.out.println("激活");
                return DtoUtil.returnSuccess("验证成功");
            }else{
                System.out.println("激活失败");
                return DtoUtil.returnFail("激活失败", ErrorCode.AUTH_ACTIVATE_FAILED);
            }
        } catch (Exception e) {
            return DtoUtil.returnFail("验证失败", ErrorCode.AUTH_ACTIVATE_FAILED);
        }

    }


}
