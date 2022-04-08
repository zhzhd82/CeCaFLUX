package cn.gyu.flux.service;

import cn.gyu.flux.db.bean.InterProcessResultBean;
import cn.gyu.flux.db.bean.ModelingCasesBean;
import cn.gyu.flux.db.bean.OptimizingResultBean;
import cn.gyu.flux.mapper.ModelingCaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ModelingService {

    @Autowired
    private ModelingCaseMapper modelingCaseMapper;

    public List<ModelingCasesBean> queryAllModelingCases() {
        return modelingCaseMapper.getModelingCases();
    }

    public ModelingCasesBean queryModelingCaseByTimestamp(long timestamp) {
        return modelingCaseMapper.findModelingCasesByTimestamp(timestamp);
    }

    public ModelingCasesBean queryPublicModelingCaseByTimestamp(long timestamp) {
        return modelingCaseMapper.findPublicModelingCasesByTimestamp(timestamp);
    }

    public OptimizingResultBean queryOptimizingResultByTimestamp(long timestamp) {
        return modelingCaseMapper.findOptimizingResultByTimestamp(timestamp);
    }

    public long addModelingCaseBean(ModelingCasesBean modelingCase) {
        return modelingCaseMapper.insertModelingCase(modelingCase);
    }

    public List<InterProcessResultBean> findInterProcessResultBeanByModelIdAndCount(int model_id, int count) {
        return modelingCaseMapper.findInterProcessResultByModelIdAndCount(model_id, count);
    }

    public List<InterProcessResultBean> findInterProcessResultBeanByModelIdAndSort(int model_id) {
        return modelingCaseMapper.findInterProcessResultByModelIdAndSort(model_id);
    }

}
