package com.lagou.edu.service.impl;

import com.lagou.edu.dao.AccountDao;
import com.lagou.edu.pojo.Account;
import com.lagou.edu.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author 应癫
 */

/**
 * 不实现接口，使用cglib动态代理生成service代理对象
 */
@Service("transferService")
@Transactional
public class TransferServiceNoImpl {


    @Autowired
    @Qualifier("accountDao")
    private AccountDao accountDao;

    public void transfer(String fromCardNo, String toCardNo, int money) throws Exception {

            Account from = accountDao.queryAccountByCardNo(fromCardNo);
            Account to = accountDao.queryAccountByCardNo(toCardNo);

            from.setMoney(from.getMoney()-money);
            to.setMoney(to.getMoney()+money);

            accountDao.updateAccountByCardNo(to);
            //int c = 1/0;
            accountDao.updateAccountByCardNo(from);
            System.out.println("使用cglib代理成功！");

    }
}
