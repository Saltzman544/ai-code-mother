package com.saltzman.aicodemother.controller;

import com.mybatisflex.core.paginate.Page;
import com.saltzman.aicodemother.annotation.AuthCheck;
import com.saltzman.aicodemother.common.BaseResponse;
import com.saltzman.aicodemother.common.DeleteRequest;
import com.saltzman.aicodemother.common.ResultUtils;
import com.saltzman.aicodemother.constant.UserConstant;
import com.saltzman.aicodemother.exception.BusinessException;
import com.saltzman.aicodemother.exception.ErrorCode;
import com.saltzman.aicodemother.exception.ThrowUtils;
import com.saltzman.aicodemother.model.dto.user.*;
import com.saltzman.aicodemother.model.entity.User;
import com.saltzman.aicodemother.model.vo.LoginUserVO;
import com.saltzman.aicodemother.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.saltzman.aicodemother.service.UserService;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 用户 控制层。
 *
 * @author Saltzman
 * @since Wed Sep 17 11:34:17 CST 2025
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册。
     *
     * @param userRegisterRequest 用户注册请求
     * @return 注册结果
     */
    @PostMapping("register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录。
     *
     * @param userLoginRequest 用户登录请求
     * @return 登录结果
     */
    @PostMapping("login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest,
                                               HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 用户注销。
     *
     * @param request 登出请求
     * @return 登出结果
     */
    @PostMapping("logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户。
     *
     * @param request 请求
     * @return 当前登录用户(脱敏信息)
     */
    @GetMapping("get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 创建用户。
     */
    @PostMapping("add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 默认密码
        final String DEFAULT_USER_PASSWORD = "12345678";
        user.setUserPassword(userService.getEncryptPassword(DEFAULT_USER_PASSWORD));
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据id获取用户 （仅管理员）
     */
    @GetMapping("get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(Long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据id获取包装类
     */
    @GetMapping("get/vo")
    public BaseResponse<UserVO> getUserVOById(Long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean flag = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(flag);
    }

    /**
     * 更新用户
     */
    @PostMapping("update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(
                        userUpdateRequest == null || userUpdateRequest.getId() <= 0
                        , ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean flag = userService.updateById(user);
        ThrowUtils.throwIf(!flag, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     */
    @PostMapping("list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        int pageNum = userQueryRequest.getPageNum();
        int pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(Page.of(pageNum, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        // 数据脱敏
        Page<UserVO> userVOPage = new Page<>(pageNum, pageSize, userPage.getTotalRow());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }

//    /**
//     * 保存用户。
//     *
//     * @param user 用户
//     * @return {@code true} 保存成功，{@code false} 保存失败
//     */
//    @PostMapping("save")
//    public boolean save(@RequestBody User user) {
//        return userService.save(user);
//    }
//
//    /**
//     * 根据主键删除用户。
//     *
//     * @param id 主键
//     * @return {@code true} 删除成功，{@code false} 删除失败
//     */
//    @DeleteMapping("remove/{id}")
//    public boolean remove(@PathVariable Long id) {
//        return userService.removeById(id);
//    }
//
//    /**
//     * 根据主键更新用户。
//     *
//     * @param user 用户
//     * @return {@code true} 更新成功，{@code false} 更新失败
//     */
//    @PutMapping("update")
//    public boolean update(@RequestBody User user) {
//        return userService.updateById(user);
//    }
//
//    /**
//     * 查询所有用户。
//     *
//     * @return 所有数据
//     */
//    @GetMapping("list")
//    public List<User> list() {
//        return userService.list();
//    }
//
//    /**
//     * 根据主键获取用户。
//     *
//     * @param id 用户主键
//     * @return 用户详情
//     */
//    @GetMapping("getInfo/{id}")
//    public User getInfo(@PathVariable Long id) {
//        return userService.getById(id);
//    }
//
//    /**
//     * 分页查询用户。
//     *
//     * @param page 分页对象
//     * @return 分页对象
//     */
//    @GetMapping("page")
//    public Page<User> page(Page<User> page) {
//        return userService.page(page);
//    }

}
