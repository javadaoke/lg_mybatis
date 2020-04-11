package com.lagou.edu.servlet;

import com.lagou.edu.anno.AutowiredNote;
import com.lagou.edu.anno.ComponentNote;
import com.lagou.edu.service.TransferService;

@ComponentNote
public class TestServiceInject {
    @AutowiredNote
    private TransferService transferService;

    public void test(){

    }
}
