package cn.itrip.auth.controller;

import cn.itrip.auth.service.TokenService;
import cn.itrip.auth.service.UserService;
import cn.itrip.beans.dto.Dto;
import cn.itrip.beans.pojo.ItripUser;
import cn.itrip.beans.vo.ItripTokenVO;
import cn.itrip.common.*;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

/**
 * 用户登录控制器
 * @author hduser
 *
 */
@Controller
@RequestMapping(value = "/api")
public class LoginController {
    @Resource
    private UserService userService;
    @Resource
    private TokenService tokenService;
    //用户登录，邮箱和手机登录
    @ApiOperation(value = "用户登录",httpMethod = "POST",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class,notes="根据用户名、密码进行统一认证")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType="form",required=true,value="用户名",name="name",defaultValue="yao.liu2015@bdqn.cn"),
            @ApiImplicitParam(paramType="form",required=true,value="密码",name="password",defaultValue="111111"),
    })//Swagger生成API文档 protocols = "HTTP"http协议 下面的ApiParams是Swagger注解
    //			@ApiParam(required = true, name = "name", value = "用户名",defaultValue="2324594295")
    //			@ApiParam(required = true, name = "password", value = "密码",defaultValue="123456jz")
    @RequestMapping(value="/dologin",method= RequestMethod.POST,produces= "application/json")
    @ResponseBody
    public Dto dologin(@RequestParam("name") String name, @RequestParam("password") String password, HttpServletRequest request){
        System.out.println("进来登录");
        try {
            ItripUser user = userService.login(name, MD5.getMd5(password+name, 32));//登录的用户和密码进行加密，在保存到token中
            if(EmptyUtils.isNotEmpty(user)){//输入的账号不为空则
               // 调用tokenservice的generateToken方法，将账号保存到token中
                String token=tokenService.generateToken(request.getHeader("user-agent"), user);
                tokenService.save(token, user);//将token保存到redis中
                ItripTokenVO vo=new ItripTokenVO(token, Calendar.getInstance().getTimeInMillis()
                +2*60*60*1000,Calendar.getInstance().getTimeInMillis());//token的过期时间和生成时间等会话时间
                return DtoUtil.returnDataSuccess(vo);//给前端返回数据对象vo对象
            }else{return DtoUtil.returnFail("用户名密码错误",ErrorCode.AUTH_ACTIVATE_FAILED);}
        }catch (Exception e){
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(),ErrorCode.AUTH_ILLEGAL_USERCODE);
        }

    }
    //注销 token
    @ApiOperation(value = "用户注销",httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class,notes="客户端需在header中发送token")
    @ApiImplicitParam(paramType="header",required=true,name="token",value="用户认证凭据",defaultValue="PC-yao.liu2015@bdqn.cn-8-20170516141821-d4f514")
    @RequestMapping(value="/logout",method=RequestMethod.GET,produces="application/json",headers="token")
    @ResponseBody
    public Dto logout(HttpServletRequest request){
        //验证token
        String token=request.getHeader("token");//拿到token信息
        //调用validate进行token判断
        if(tokenService.validate(request.getHeader("user-agent"), token)){
            try {
                //删除有效token到redis删除保存的token
                tokenService.delete(token);
                return DtoUtil.returnSuccess("注销成功");
            }catch (Exception e){
                e.printStackTrace();
                return DtoUtil.returnFail("注销失败", ErrorCode.AUTH_UNKNOWN);
            }
        }else{
            return DtoUtil.returnFail("token无效", ErrorCode.AUTH_TOKEN_INVALID);
        }

    }
}
