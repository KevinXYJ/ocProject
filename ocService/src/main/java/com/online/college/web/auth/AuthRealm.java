package com.online.college.web.auth;

import com.online.college.common.storage.QiniuStorage;
import com.online.college.common.storage.ThumbModel;
import com.online.college.common.web.SessionContext;
import com.online.college.common.web.auth.SessionUser;
import com.online.college.core.auth.domain.AuthUser;
import com.online.college.core.auth.service.IAuthUserService;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * shiro实现用户登陆
 */
public class AuthRealm extends AuthorizingRealm {




    @Autowired
	private IAuthUserService authUserService;
	
	/**
	 * 实现用户登陆
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authToken) throws AuthenticationException {
        System.out.println("AuthRealm ===> doGetAuthenticationInfo");
        UsernamePasswordToken token = (UsernamePasswordToken) authToken;
		String username = token.getUsername();
		String password = String.valueOf(token.getPassword());
		AuthUser authUser = null;
		/**
		 * 业务代码-start
		 */
		try {
			AuthUser tmpAuthUser = new AuthUser();
			tmpAuthUser.setUsername(username);
			tmpAuthUser.setPassword(password);
			
			tmpAuthUser = authUserService.getByUsernameAndPassword(tmpAuthUser);
			if(null != tmpAuthUser){
				authUser = new AuthUser();
				authUser.setId(tmpAuthUser.getId());
				authUser.setRealname(tmpAuthUser.getRealname());
				authUser.setUsername(tmpAuthUser.getUsername());
				authUser.setStatus(tmpAuthUser.getStatus());
				if(!StringUtils.isBlank(tmpAuthUser.getHeader())){
					authUser.setHeader(QiniuStorage.getUrl(tmpAuthUser.getHeader(), ThumbModel.THUMB_256));//设置头像
				}
			}else{
				throw new AuthenticationException("## user password is not correct! ");
			}
		} catch (Exception e) {
			throw new AuthenticationException("## user password is not correct! ");
		}
		//业务代码-end
		// 创建授权用户
		SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(authUser, password, getName());
		return info;
	}
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        System.out.println("AuthRealm ===> doGetAuthorizationInfo");
		if (principals == null)
			throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
		// 获取当前登录用户
		SessionUser user = SessionContext.getAuthUser();
		if (user == null) {
			return null;
		}
		// 设置权限
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
		// 获取用户权限并设置 以供shiro框架 
		info.setStringPermissions(user.getPermissions());
		return info;
	}

}
