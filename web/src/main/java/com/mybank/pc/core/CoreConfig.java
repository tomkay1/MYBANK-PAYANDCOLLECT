package com.mybank.pc.core;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.core.JFinal;
import com.jfinal.handler.Handler;
import com.jfinal.json.FastJsonFactory;
import com.jfinal.kit.PathKit;
import com.jfinal.log.Log4jLogFactory;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.cron4j.Cron4jPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.template.Engine;
import com.mybank.pc.CMNCtr;
import com.mybank.pc.admin.LoginCtr;
import com.mybank.pc.admin.art.ArtCtr;
import com.mybank.pc.admin.param.ParamCtr;
import com.mybank.pc.admin.res.ResCtr;
import com.mybank.pc.admin.role.RoleCtr;
import com.mybank.pc.admin.taxonomy.TaxCtr;
import com.mybank.pc.admin.user.UserCtr;
import com.mybank.pc.collection.trade.CTradeCtr;
import com.mybank.pc.interceptors.AdminAAuthInterceptor;
import com.mybank.pc.interceptors.AdminIAuthInterceptor;
import com.mybank.pc.interceptors.ExceptionInterceptor;
import com.mybank.pc.kits.DateKit;
import com.mybank.pc.kits.ResKit;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by yuhaihui8913 on 2017/11/14.
 */
public class CoreConfig extends JFinalConfig{
    @Override
    public void configConstant(Constants constants) {
        constants.setDevMode(ResKit.getConfigBoolean("devMode"));
        //constants.setMainRenderFactory(new BeetlRenderFactory());
        constants.setError500View("/WEB-INF/template/common/500.html");
        constants.setError404View("/WEB-INF/template/common/404.html");
        constants.setError403View("/WEB-INF/template/common/403.html");
        constants.setError401View("/WEB-INF/template/common/401.html");
        constants.setEncoding("UTF-8");
        constants.setJsonFactory(new FastJsonFactory());
        constants.setLogFactory(new Log4jLogFactory());
        constants.setJsonDatePattern(DateKit.STR_DATEFORMATE);

    }

    @Override
    public void configRoute(Routes routes) {
        routes.add(new Routes() {
            @Override
            public void config() {
                addInterceptor(new AdminAAuthInterceptor());
                add("/ad00", ParamCtr.class);
                add("/ad01", UserCtr.class);
                add("/ad02", RoleCtr.class);
                add("/ad03", ResCtr.class);
                add("/ad04", ArtCtr.class);
                add("/ad05", TaxCtr.class);
                add("/ad06", LoginCtr.class);
            }
        });

        routes.add(new Routes() {
            @Override
            public void config() {
            	 add("/coll", CTradeCtr.class);
            }
        });

        routes.add(new Routes() {
            @Override
            public void config() {
                add("/cmn", CMNCtr.class);
            }
        });

    }

    @Override
    public void configEngine(Engine engine) {
        engine.addSharedObject("ctx", JFinal.me().getContextPath());
        engine.addSharedMethod( new StrUtil());
        engine.addSharedObject("cKit",new CollectionUtil());
        engine.setDevMode(ResKit.getConfigBoolean("devMode", true));
        //使用JF模板渲染通用页面
//        engine.addSharedFunction("/WEB-INF/template/www/css.html");
//        engine.addSharedFunction("/WEB-INF/template/www/js.html");
//        engine.addSharedFunction("/WEB-INF/template/admin/_layout.html");
        engine.addSharedFunction("/WEB-INF/template/www/_layout.html");
    }

    @Override
    public void configPlugin(Plugins plugins) {
        //开启druid数据库连接池
        DruidPlugin druidPlugin = createDruidPlugin();
        // StatFilter提供JDBC层的统计信息
        druidPlugin.addFilter(new StatFilter());
        // WallFilter的功能是防御SQL注入攻击
        WallFilter wallDefault = new WallFilter();
        wallDefault.setDbType("mysql");
        druidPlugin.addFilter(wallDefault);
        druidPlugin.setInitialSize(ResKit.getConfigInt("db.default.poolInitialSize"));
        druidPlugin.setMaxPoolPreparedStatementPerConnectionSize(ResKit.getConfigInt("db.default.poolMaxSize"));
        druidPlugin.setTimeBetweenConnectErrorMillis(ResKit.getConfigInt("db.default.connectionTimeoutMillis"));
        plugins.add(druidPlugin);
        //开启DB+record 映射关系插件
        ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
        _MappingKit.mapping(arp);
        arp.getEngine().setDevMode(true);
        arp.getEngine().addSharedMethod(new StrUtil());
        arp.setBaseSqlTemplatePath(PathKit.getRootClassPath()+"/sql");
        arp.addSqlTemplate("all.sql");
        arp.setShowSql(true);
        plugins.add(arp);
        //开启eheache缓存
        plugins.add(new EhCachePlugin());
        //计划任务插件
        Cron4jPlugin cron4jPlugin=new Cron4jPlugin("task.properties","cron4j");
        plugins.add(cron4jPlugin);

    }

    private DruidPlugin createDruidPlugin() {
        DruidPlugin druidDefault = new DruidPlugin(ResKit.getConfig("db.default.url"), ResKit.getConfig("db.default.user"),
                ResKit.getConfig("db.default.password"),ResKit.getConfig("db.default.driver"));
        return druidDefault;
    }

    @Override
    public void configInterceptor(Interceptors interceptors) {
        interceptors.add(new ExceptionInterceptor());
        interceptors.add(new AdminIAuthInterceptor());
    }

    @Override
    public void configHandler(Handlers handlers) {
        if(JFinal.me().getConstants().getDevMode()){
            handlers.add(new Handler() {
                @Override
                public void handle(String s, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, boolean[] booleans) {
                    if(s.equals("/static/swagger/index.html")||s.equals("/Api")){
                        return ;
                    }else{
                        next.handle(s,httpServletRequest,httpServletResponse,booleans);
                    }
                }
            });
        }
    }

    @Override
    public void afterJFinalStart() {
        super.afterJFinalStart();
        CoreData.loadAllCache();//省市区 Cache 初始化
    }
}
