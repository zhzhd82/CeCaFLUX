package edu.gyu.mfa.dao;

import edu.gyu.mfa.db.bean.InterCalProcessResult;
import edu.gyu.mfa.db.bean.ModelingCase;
import edu.gyu.mfa.db.bean.OptimizingResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ModelingCaseMapper {

    @Select("select * from modeling_case where status=#{status}")
    public List<ModelingCase> selectModelingCaseByStatus(int status);

    @Update("update modeling_case set status=#{status} where id=#{id}")
    public void updateModelingCaseStatus(@Param("id") long id, @Param("status") int status);

    @Insert("insert into optimizing_result(model_id, name, timestamp, optimizing_flux, x_value, norm2, c_free_value, flux_confidence_interval, c_confidence_interval, goodness_of_fit) " +
            "values(#{model_id}, #{name}, #{timestamp}, #{optimizing_flux}, #{x_value}, #{norm2}, #{c_free_value}, #{flux_confidence_interval}, #{c_confidence_interval}, #{goodness_of_fit})")
    public void insertOptimizingResult(OptimizingResult optimizingResult);

    @Insert("insert into inter_calculation_precess(model_id, name, timestamp, flux_value, count, c_free_value, parent_mean_norm2, cross_mean_norm2, mutation_mean_norm2, parent_min_norm2, cross_min_norm2, mutation_min_norm2) " +
            "values(#{model_id}, #{name}, #{timestamp}, #{flux_value}, #{count}, #{c_free_value}, #{parent_mean_norm2}, #{cross_mean_norm2}, #{mutation_mean_norm2}, #{parent_min_norm2}, #{cross_min_norm2}, #{mutation_min_norm2})")
    public void insertInterCalProcessResult(InterCalProcessResult interCalProcessResult);

}
