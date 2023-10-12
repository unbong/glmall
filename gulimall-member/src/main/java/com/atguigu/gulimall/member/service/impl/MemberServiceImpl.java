package com.atguigu.gulimall.member.service.impl;

import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneCheckException;
import com.atguigu.gulimall.member.exception.UserNameCheckException;
import com.atguigu.gulimall.member.service.MemberLevelService;
import com.atguigu.gulimall.member.vo.LoginUserVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import com.atguigu.gulimall.member.vo.UserRegistVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {


    @Autowired
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void registUser(UserRegistVo vo) {

        // id unique

        checkUserName(vo);
        checkPhone(vo);

        MemberEntity member = new MemberEntity();
        member.setUsername(vo.getUserName());
        member.setMobile(vo.getPhone());
        // 加密保存

        BCryptPasswordEncoder encode = new BCryptPasswordEncoder();
        String encodePassword  = encode.encode(vo.getPassword());
        member.setPassword(encodePassword);

        MemberLevelEntity level = memberLevelService.getOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));

        member.setLevelId(level.getId());

        this.save(member);


    }

    @Override
    public MemberEntity loginSocial(SocialUser user) {

        MemberEntity member = baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("uid", user.getUid()));

        if(member == null)
        {
            member = new MemberEntity();
            member.setUid(user.getUid());
            MemberLevelEntity level = memberLevelService.getOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));
            member.setNickname(user.getName());
            member.setLevelId(level.getId());

            baseMapper.insert(member);

        }
        else{
            member.setAccessToken(user.getIdtoken());
            member.setUid(user.getUid());
            baseMapper.updateById(member);
        }

        return member;
    }

    @Override
    public MemberEntity login(LoginUserVo vo) {
        MemberEntity member =  baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", vo.getUsername()));
        if(member != null)
        {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (encoder.matches( vo.getPassword(), member.getPassword()) )
            {

                member.setPassword("");
                return member;
            }


        }

        return null;
    }

    private void checkPhone(UserRegistVo vo)  {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", vo.getUserName()));
        if (count > 0)
        {
            throw  new PhoneCheckException();
        }

    }

    private void checkUserName(UserRegistVo vo) {
        Integer count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", vo.getUserName()));
        if (count > 0)
        {
            throw  new UserNameCheckException();
        }

    }

}