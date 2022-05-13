package com.gf.output.service.impl;

import com.gf.output.pojo.Test;
import com.gf.output.mapper.TestMapper;
import com.gf.output.service.ITestService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author lb
 * @since 2022-05-13
 */
@Service
public class TestServiceImpl extends ServiceImpl<TestMapper, Test> implements ITestService {

}
