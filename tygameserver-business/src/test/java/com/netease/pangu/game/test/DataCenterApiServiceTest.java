package com.netease.pangu.game.test;

import com.netease.pangu.game.meta.DataCenterSimpleRoleInfo;
import com.netease.pangu.game.service.DataCenterApiService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by huangc on 2017/2/5.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:tygameserver-business-service.xml")
public class DataCenterApiServiceTest {
    @Resource
    private DataCenterApiService dataCenterApiService;

    @Test
    public void getSimpleAvatarsInfoByUrs() {
        Map<String, List<DataCenterSimpleRoleInfo>> map = dataCenterApiService.getSimpleAvatarsInfoByUrs("ywwstcl@163.com");
        System.out.println(map);
    }


}
