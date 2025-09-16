package com.saltzman.aicodemother.controller;

import com.saltzman.aicodemother.common.BaseResponse;
import com.saltzman.aicodemother.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Saltzman
 * @Date: 2025/09/16/12:28
 * @Description:
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping("/health")
    public BaseResponse<String> healthCheck() {
        return ResultUtils.success("ok");
    }
}
