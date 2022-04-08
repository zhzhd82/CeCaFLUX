package cn.gyu.flux.mapper;

import cn.gyu.flux.db.bean.InterProcessResultBean;
import cn.gyu.flux.db.bean.ModelingCasesBean;
import cn.gyu.flux.db.bean.OptimizingResultBean;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ModelingCaseMapper {

    @Select("select * from modeling_case where status=3 and is_public=2 order by timestamp desc")
    List<ModelingCasesBean> getModelingCases();

    @Select("select * from modeling_case where timestamp=#{timestamp}")
    ModelingCasesBean findModelingCasesByTimestamp(long timestamp);

    @Select("select * from modeling_case where timestamp=#{timestamp} and is_public=2")
    ModelingCasesBean findPublicModelingCasesByTimestamp(long timestamp);

    @Select("select * from optimizing_result where timestamp=#{timestamp}")
    OptimizingResultBean findOptimizingResultByTimestamp(long timestamp);

    @Insert("insert into modeling_case(email,name,timestamp,comp_formula,carbon_formula,fluxes_net,pool_size,equalities,inequalities,label_input,mass_spectrometry,sample_space,method,step,status, is_public) " +
            "values(#{email},#{name},#{timestamp},#{comp_formula},#{carbon_formula},#{fluxes_net},#{pool_size},#{equalities},#{inequalities},#{label_input},#{mass_spectrometry},#{sample_space},#{method},#{step},#{status}, #{is_public})")
    long insertModelingCase(ModelingCasesBean modelingCase);

    @Select("select * from inter_calculation_precess where model_id=#{model_id} and count > #{count}")
    List<InterProcessResultBean> findInterProcessResultByModelIdAndCount(@Param("model_id") int model_id, @Param("count") int count);

    @Select("select * from inter_calculation_precess where model_id=#{model_id} order by count asc")
    List<InterProcessResultBean> findInterProcessResultByModelIdAndSort(@Param("model_id") int model_id);

}
