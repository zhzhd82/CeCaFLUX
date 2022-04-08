package edu.gyu.mfa.service;

import edu.gyu.mfa.dao.ModelingCaseMapper;
import edu.gyu.mfa.db.bean.InterCalProcessResult;
import edu.gyu.mfa.db.bean.ModelingCase;
import edu.gyu.mfa.db.bean.OptimizingResult;
import edu.gyu.mfa.util.MybatisUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.List;

public class ModelingService {

    public static ModelingCase queryModelingCaseByStatus(int status) {
        SqlSession openSession = null;
        ModelingCase modelingCase = null;
        try{
            SqlSessionFactory sqlSessionFactory = MybatisUtil.getSqlSessionFactory();
            openSession = sqlSessionFactory.openSession(true);
            ModelingCaseMapper modelingCaseMapper = openSession.getMapper(ModelingCaseMapper.class);
            List<ModelingCase> modelingCaseList = modelingCaseMapper.selectModelingCaseByStatus(status);
            if(modelingCaseList != null && modelingCaseList.size() > 0) {
                modelingCase = modelingCaseList.get(0);
            }
        }catch (Exception e) {
            System.err.println(e);
        }finally {
            if(openSession != null) {
                openSession.close();
            }
        }
        return modelingCase;
    }

    public static void updateModelingCaseStatus(ModelingCase modelingCase, int status) {
        SqlSession openSession = null;
        try{
            SqlSessionFactory sqlSessionFactory = MybatisUtil.getSqlSessionFactory();
            openSession = sqlSessionFactory.openSession(true);
            ModelingCaseMapper modelingCaseMapper = openSession.getMapper(ModelingCaseMapper.class);
            modelingCaseMapper.updateModelingCaseStatus(modelingCase.getId(), status);
        }catch (Exception e) {
            System.err.println(e);
        }finally {
            if(openSession != null) {
                openSession.close();
            }
        }
    }

    public static void insertOptimizingResult(OptimizingResult optimizingResult) {
        SqlSession openSession = null;
        try{
            SqlSessionFactory sqlSessionFactory = MybatisUtil.getSqlSessionFactory();
            openSession = sqlSessionFactory.openSession(true);
            ModelingCaseMapper modelingCaseMapper = openSession.getMapper(ModelingCaseMapper.class);
            modelingCaseMapper.insertOptimizingResult(optimizingResult);
        }catch (Exception e) {
            System.err.println(e);
        }finally {
            if(openSession != null) {
                openSession.close();
            }
        }
    }

    public static void insertInterCalProcessResult(InterCalProcessResult interCalProcessResult) {
        SqlSession openSession = null;
        try{
            SqlSessionFactory sqlSessionFactory = MybatisUtil.getSqlSessionFactory();
            openSession = sqlSessionFactory.openSession(true);
            ModelingCaseMapper modelingCaseMapper = openSession.getMapper(ModelingCaseMapper.class);
            modelingCaseMapper.insertInterCalProcessResult(interCalProcessResult);
        }catch (Exception e) {
            System.err.println(e);
        }finally {
            if(openSession != null) {
                openSession.close();
            }
        }
    }

}
