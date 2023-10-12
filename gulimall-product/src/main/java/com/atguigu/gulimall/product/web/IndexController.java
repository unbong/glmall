package com.atguigu.gulimall.product.web;


import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import org.redisson.Redisson;
import org.redisson.RedissonWriteLock;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {


    // 1 处理映射亲贵  、 index。html
    // 、、试图解析请进行品串
    // 前缀 + 返回值 + 后缀
    // classpath 、template   + index  + html

    // TODO: 2021/8/18  查出一级分类 
    // TODO: 2021/8/18   Model .addAttribute("categoruy", 返回的对象)
    // TODO: 2021/8/18  在service 中查询  baseMapper，sekectList queryWrapper


    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping({"/", "index.html"})
    public String indexController(Model model )
    {
        List<CategoryEntity> categorys = categoryService.getCategoryLevel1();
        model.addAttribute("categorys", categorys);


        return "index";
    }

    @GetMapping("/hello")
    public String helloControllwe(Model model)
    {

        // 可重入锁
        RLock lock =  redissonClient.getLock("indexLock");
        //lock.lock(10, TimeUnit.SECONDS);
        lock.lock();
        System.out.println("start lock"+ lock.getName());
        try{

            Thread.sleep(30*1000);


        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
            System.out.println("end lock"+ lock.getName());
        }


        return "hello";
    }


    @GetMapping("/writelock")
    @ResponseBody
    public String writeLock()
    {
        // 可重入锁
        RReadWriteLock  lock =  redissonClient.getReadWriteLock("writelock");
        //lock.lock(10, TimeUnit.SECONDS);
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
         try{
            lock.writeLock().lock();
             System.out.println("get write lock" + Thread.currentThread().getId());

             Thread.sleep(30*1000);
            ops.set("write", "1");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
        System.out.println("end write lock"+ + Thread.currentThread().getId());


        return "1";
    }


    /**
     * 信号量锁
     *
     * @return
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore lock = redissonClient.getSemaphore("park");
        lock.acquire(3);

        return "ok";
    }



    @GetMapping("/go")
    @ResponseBody
    public String go()
    {
        RSemaphore lock = redissonClient.getSemaphore("park");
        lock.release();

        return "1";
    }



    @ResponseBody
    @GetMapping("/readlock")
    public  String readLock(){

        RReadWriteLock lock =  redissonClient.getReadWriteLock("writelock");
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String s = "";

        try{
            lock.readLock().lock();
            System.out.println("get read lock" + Thread.currentThread().getId());

            s = ops.get("write");
            Thread.sleep(30*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            lock.readLock().unlock();

        }
        System.out.println("end read lock"+ + Thread.currentThread().getId());
        return s;
    }


// 返回2 3
// ResponseBody
// return map
//
    @ResponseBody
    @GetMapping("index/json/catalog.json")
    public Map<String, List<Catalog2Vo>> catelog3Controllwe()
    {


        Map<String, List<Catalog2Vo>> res =  categoryService.getCatalogJson();

        return res;
    }






}

