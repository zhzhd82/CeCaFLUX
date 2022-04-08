package cn.gyu.flux.service;

import cn.gyu.flux.db.bean.GenomeNetworkReactionBean;
import cn.gyu.flux.mapper.GenomeNetworkReactionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GenomeNetworkReactionService {

    @Autowired
    private GenomeNetworkReactionMapper genomeNetworkReactionMapper;

    public List<GenomeNetworkReactionBean> queryAllGenomeNetworkReactions() {
        return genomeNetworkReactionMapper.getAllGenomeNetworkReaction();
    }

    public List<GenomeNetworkReactionBean> queryGenomeNetworkReactions(int page, int pageSize) {
        int curIndex = (page - 1) * pageSize;
        return genomeNetworkReactionMapper.getGenomeNetworkReaction(curIndex, pageSize);
    }

    public int queryTotalCount() {
        return genomeNetworkReactionMapper.getTotalCount();
    }

    public List<GenomeNetworkReactionBean> queryGenomeNetworkReactionByRxnID(String rxn_id) {
        return genomeNetworkReactionMapper.getGenomeNetworkReactionByRxnID(rxn_id);
    }

    public List<GenomeNetworkReactionBean> queryGenomeNetworkReactionByRxnName(String rxnName) {
        return genomeNetworkReactionMapper.getGenomeNetworkReactionByRxnName(rxnName);
    }

    public List<GenomeNetworkReactionBean> queryGenomeNetworkReactionByCompound(String compound) {
        return genomeNetworkReactionMapper.getGenomeNetworkReactionByCompound(compound);
    }

    public List<GenomeNetworkReactionBean> queryGenomeNetworkReactionByRxnNameAndCompound(String rxnName, String compound) {
        return genomeNetworkReactionMapper.getGenomeNetworkReactionByRxnNameAndCompound(rxnName, compound);
    }

    public List<GenomeNetworkReactionBean> queryGenomeNetworkReactionByRxnIDNameAndCompound(String rxnID, String rxnName, String compound) {
        return genomeNetworkReactionMapper.getGenomeNetworkReactionByRxnIDNameAndCompound(rxnID, rxnName, compound);
    }

}
