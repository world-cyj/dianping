package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {


    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1校验手机号(正则表达式来校验--有工具类)
        if(RegexUtils.isPhoneInvalid(phone)){
            //2如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }
        //3符合，生成验证码
        String code = RandomUtil.randomNumbers(6);
        //4保存验证码到session
        session.setAttribute("code",code);
        //5发送验证码
        log.info("发送验证码成功！验证码为：{}",code);
        //6返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1.校验手机号
        String phone = loginForm.getPhone();
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误");
        }
        //2.校验验证码

        String code = loginForm.getCode();
        Object cacheCode = session.getAttribute("code");
        if(cacheCode == null || !cacheCode.toString().equals(code)){

            //3.不一致，报错
            return Result.fail("验证码错误");
        }


        //4.一致，根据手机号查询用户(查数据库)(mybatisplus)
        //select * from tb_user where phone = ?
        User user = query().eq("phone", phone).one();

        //5.判断用户是否存在
        if (user == null){
            //6不存在，创建新用户并保存
            user = createUserWithPhone(phone);
            return Result.ok(user);

        }
        //7.存在，返回用户信息
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
//        return Result.ok(user);
        return Result.ok();

    }
    //创建新用户
    private User createUserWithPhone(String phone) {
        User user=new User();
        user.setPhone(phone);
        //默认随机用户名
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        //mybatisplus 自动填充
        save(user);
        return user;
    }

}
