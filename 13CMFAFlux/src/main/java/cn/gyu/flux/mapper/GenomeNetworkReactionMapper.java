package cn.gyu.flux.mapper;

import cn.gyu.flux.db.bean.GenomeNetworkReactionBean;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface GenomeNetworkReactionMapper {

    @Select("select * from genome_network_reaction")
    public List<GenomeNetworkReactionBean> getAllGenomeNetworkReaction();

    @Select("select * from genome_network_reaction limit #{curIndex}, #{size}")
    public List<GenomeNetworkReactionBean> getGenomeNetworkReaction(@Param("curIndex") int curIndex, @Param("size") int size);

    @Select("select count(*) from genome_network_reaction")
    public int getTotalCount();

    @Select("select * from genome_network_reaction where rxn_id like CONCAT('%',#{rxn_id},'%')")
    public List<GenomeNetworkReactionBean> getGenomeNetworkReactionByRxnID(String rxn_id);

    @Select("select * from genome_network_reaction where rxn_name like CONCAT('%',#{name},'%')")
    public List<GenomeNetworkReactionBean> getGenomeNetworkReactionByRxnName(String name);

    @Select("select * from genome_network_reaction where compound_formula like CONCAT('%',#{compound},'%')")
    public List<GenomeNetworkReactionBean> getGenomeNetworkReactionByCompound(String compound);

    @Select("select * from genome_network_reaction where compound_formula like CONCAT('%',#{compound},'%') and rxn_name like CONCAT('%',#{name},'%')")
    public List<GenomeNetworkReactionBean> getGenomeNetworkReactionByRxnNameAndCompound(@Param("name") String name, @Param("compound") String compound);

    @Select("select * from genome_network_reaction where rxn_id like CONCAT('%',#{rxn_id},'%') and compound_formula like CONCAT('%',#{compound},'%') and rxn_name like CONCAT('%',#{name},'%')")
    public List<GenomeNetworkReactionBean> getGenomeNetworkReactionByRxnIDNameAndCompound(@Param("rxn_id") String rxn_id, @Param("name") String name, @Param("compound") String compound);

}
