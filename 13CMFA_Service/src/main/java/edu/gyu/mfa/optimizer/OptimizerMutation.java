package edu.gyu.mfa.optimizer;

import Jama.Matrix;
import edu.gyu.mfa.calculator.ReactionFluxCalculator;
import edu.gyu.mfa.db.bean.InterCalProcessResult;
import edu.gyu.mfa.db.bean.ModelingCase;
import edu.gyu.mfa.db.bean.OptimizingResult;
import edu.gyu.mfa.ftbl.*;
import edu.gyu.mfa.info.*;
import edu.gyu.mfa.matrix.MatrixTool;
import edu.gyu.mfa.matrix.VectorTool;
import edu.gyu.mfa.reaction.*;
import edu.gyu.mfa.service.ModelingService;
import edu.gyu.mfa.util.SampleUtil;
import edu.gyu.mfa.util.Util;
import edu.gyu.mfa.x.EMUX;
import edu.gyu.mfa.x.XTool;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OptimizerMutation extends AbsReactionSizeEMU {

    private ReactionFluxCalculator reactionFluxCalculator;
    private double[] timePoints;
    private List<Reaction> reactions;
    private FTBLFile ftblFile;
    private Map<String, List<MassSpectrometry>> emuKeyMassSpectrometryMap;
    private Map<String, Map<Double, double[]>> emuKeyMMap;
    private final double[] lb;
    private final double[] ub;
    private double[] B;
    private ReactionSizeEMU reactionSizeEMU;
    private CAndCFree cAndCFree;
    private List<EMUReaction> emuReactions;
    private double[][] M_Deviation;
    private int cores;
    private Matrix SigmaXMat;
    private Map<String, Map<Double, MassSpectrometryTimePointValue>> weightKeyTimePointValueMap;
    private ModelingCase modelingCase;

    public OptimizerMutation(ReactionSizeEMU reactionSizeEMU,
                             ReactionFluxCalculator reactionFluxCalculator,
                             CAndCFree cAndCFree,
                             List<EMUReaction> emuReactions,
                             List<Reaction> reactions,
                             FTBLFile ftblFile,
                             ModelingCase modelingCase) {
        super(reactionSizeEMU);
        this.reactionSizeEMU = reactionSizeEMU;
        this.reactionFluxCalculator = reactionFluxCalculator;
        this.reactions = reactions;
        this.ftblFile = ftblFile;
        this.cAndCFree = cAndCFree;
        this.modelingCase = modelingCase;
        this.emuReactions = emuReactions;
        emuKeyMassSpectrometryMap = computeEMUKeyMassSpectrometry(ftblFile);
        lb = VectorTool.computeLBVector();
        ub = VectorTool.computeUBVector();
        cores = Runtime.getRuntime().availableProcessors();
        cores = cores - Argument.cpu_cores_keep;
        if (cores < 0) {
            cores = 1;
        }

        weightKeyTimePointValueMap = ftblFile.getWeightKeyTimePointValueMap();
        List<Double> timePointList = new ArrayList<>();
        for(String weightKey : weightKeyTimePointValueMap.keySet()) {
            Map<Double, MassSpectrometryTimePointValue> map = weightKeyTimePointValueMap.get(weightKey);
            timePointList.addAll(map.keySet());
            break;
        }
        Collections.sort(timePointList);
        timePoints = VectorTool.convertListToVector(timePointList);
    }

    private void calculateMAndDeviation() {
        int dimen = 0;
        for(String weightKey : weightKeyTimePointValueMap.keySet()) {
            dimen += weightKeyTimePointValueMap.get(weightKey).size();
        }
        M_Deviation = new double[dimen][dimen];
        int deviationIndex = 0;
        emuKeyMMap = new HashMap<>();
        for (String emuKey : emuKeyMassSpectrometryMap.keySet()) {
            List<MassSpectrometry> massSpectrometryList = emuKeyMassSpectrometryMap.get(emuKey);
            Map<Double, double[]> timePointM = new HashMap<>();
            emuKeyMMap.put(emuKey, timePointM);
            for (double timePoint : timePoints) {
                double[] Mk = new double[massSpectrometryList.size()];
                for (int index = 0; index < massSpectrometryList.size(); index++) {
                    MassSpectrometry massSpectrometry = massSpectrometryList.get(index);
                    MassSpectrometryTimePointValue timePointValue = weightKeyTimePointValueMap.get(massSpectrometry.getWeightKey()).get(timePoint);
                    Mk[index] = timePointValue.getValue();
                    M_Deviation[deviationIndex][deviationIndex] = timePointValue.getDeviation() * timePointValue.getDeviation();
                    deviationIndex++;
                }
                timePointM.put(timePoint, Mk);
            }
        }

        SigmaXMat = new Matrix(M_Deviation);
    }

    public boolean optimize(double[] B) {
        boolean result = true;
        OptimizingResult optimizingResult = new OptimizingResult();
        optimizingResult.setModel_id(modelingCase.getId());
        optimizingResult.setName(modelingCase.getName());
        optimizingResult.setTimestamp(modelingCase.getTimestamp());
        try {
            calculateMAndDeviation();
            this.B = B;
            int N = modelingCase.getSample_space();
            double alpha = 0.8;
            int k1;
            int k2;

            int p = Count.freeCount + Count.cFreeCount;

            int n = 0;
            for (String emuKey : emuKeyMassSpectrometryMap.keySet()) {
                int mCount = emuKeyMassSpectrometryMap.get(emuKey).size();
                String compName = emuKey.split(Constant.NAME_SPLITTER)[0];
                int carbonCount = compNameCarbonMap.get(compName).split(Constant.CARBON_SPLITTER).length;
                if (mCount == carbonCount + 1) {
                    n += carbonCount * timePoints.length;
                } else {
                    n += mCount * timePoints.length;
                }
            }
            ChiSquaredDistribution distribution = new ChiSquaredDistribution(n - p, 0.00001);

            List<Norm2ValueFluxX> parentNorm2ValueFluxXList = ReactionTool.sampleReactionFluxWithOptimizerAndConvertToNorm2ValueFluxX(reactions, ftblFile);
            int optimizerFluxCount = parentNorm2ValueFluxXList.size();
            if(optimizerFluxCount >= N) {
                parentNorm2ValueFluxXList = parentNorm2ValueFluxXList.subList(0, N);
            } else {
                parentNorm2ValueFluxXList.addAll(ReactionTool.sampleReactionFluxAndConvertToNorm2ValueFluxX(reactions, ftblFile, N - optimizerFluxCount));
            }
            List<Norm2ValueCalculatingFactor> parentCalculatingFactorList = new ArrayList<>();
            for (Norm2ValueFluxX parentNorm2ValueFluxX : parentNorm2ValueFluxXList) {
                double[] cFreeValues = VectorTool.generateRandomVector(Count.cFreeCount, Constant.C_FREE_MIN_VALUE, Constant.C_FREE_MAX_VALUE);
                parentNorm2ValueFluxX.setcFreeValues(cFreeValues);
                parentNorm2ValueFluxX.generateFluxXFromVAndCFreeValues();
                parentCalculatingFactorList.add(generateNorm2ValueCalculatingFactor(parentNorm2ValueFluxX.getFluxX()));
            }

            calculateNorm2ValueInThread(parentNorm2ValueFluxXList, parentCalculatingFactorList, n);

            int count = 0;
            double goodness_of_fit;
            while (true) {
                count++;
                k1 = count;
                if (k1 >= Count.freeCount - 2) {
                    k1 = Count.freeCount - 2;
                }
                if(k1 < 0) {
                    k1 = 0;
                }

                k2 = count;
                if (k2 >= Count.cFreeCount - 2) {
                    k2 = Count.cFreeCount - 2;
                }
                if(k2 < 0) {
                    k2 = 0;
                }

                List<Norm2ValueFluxX> crossChildNorm2ValueFluxXList = calculateCrossChildNorm2ValueFluxX(parentNorm2ValueFluxXList, alpha);
                List<Norm2ValueCalculatingFactor> crossCalculatingFactorList = new ArrayList<>();
                for (Norm2ValueFluxX crossNorm2ValueFluxX : crossChildNorm2ValueFluxXList) {
                    crossCalculatingFactorList.add(generateNorm2ValueCalculatingFactor(crossNorm2ValueFluxX.getFluxX()));
                }

                List<Norm2ValueFluxX> mutantChildNorm2ValueFluxXList = calculateMutantChildNorm2ValueFluxX(parentNorm2ValueFluxXList, k1, k2);
                List<Norm2ValueCalculatingFactor> mutantCalculatingFactorList = new ArrayList<>();
                for (Norm2ValueFluxX mutantNorm2ValueFluxX : mutantChildNorm2ValueFluxXList) {
                    mutantCalculatingFactorList.add(generateNorm2ValueCalculatingFactor(mutantNorm2ValueFluxX.getFluxX()));
                }

                List<Norm2ValueFluxX> crossMutantNorm2ValueFluxXList = new ArrayList<>();
                crossMutantNorm2ValueFluxXList.addAll(crossChildNorm2ValueFluxXList);
                crossMutantNorm2ValueFluxXList.addAll(mutantChildNorm2ValueFluxXList);
                List<Norm2ValueCalculatingFactor> crossMutantCalculatingFactorList = new ArrayList<>();
                crossMutantCalculatingFactorList.addAll(crossCalculatingFactorList);
                crossMutantCalculatingFactorList.addAll(mutantCalculatingFactorList);
                calculateNorm2ValueInThread(crossMutantNorm2ValueFluxXList, crossMutantCalculatingFactorList, n);

                InterCalProcessResult interCalProcessResult = new InterCalProcessResult();
                interCalProcessResult.setCount(count);
                interCalProcessResult.setModel_id(modelingCase.getId());
                interCalProcessResult.setTimestamp(modelingCase.getTimestamp());
                interCalProcessResult.setName(modelingCase.getName());

                Collections.sort(parentNorm2ValueFluxXList);
                Collections.sort(crossChildNorm2ValueFluxXList);
                Collections.sort(mutantChildNorm2ValueFluxXList);

                interCalProcessResult.setParent_mean_norm2(VectorTool.computeMeanValue(parentNorm2ValueFluxXList));
                interCalProcessResult.setParent_min_norm2(parentNorm2ValueFluxXList.get(0).getNorm2_value());

                interCalProcessResult.setCross_mean_norm2(VectorTool.computeMeanValue(crossChildNorm2ValueFluxXList));
                interCalProcessResult.setCross_min_norm2(crossChildNorm2ValueFluxXList.get(0).getNorm2_value());

                interCalProcessResult.setMutation_mean_norm2(VectorTool.computeMeanValue(mutantChildNorm2ValueFluxXList));
                interCalProcessResult.setMutation_min_norm2(mutantChildNorm2ValueFluxXList.get(0).getNorm2_value());

                List<Norm2ValueFluxX> tmpList = new ArrayList<>();
                tmpList.addAll(parentNorm2ValueFluxXList);
                tmpList.addAll(crossChildNorm2ValueFluxXList);
                tmpList.addAll(mutantChildNorm2ValueFluxXList);

                Collections.sort(tmpList);
                parentNorm2ValueFluxXList = tmpList.subList(0, N);

                double norm2_minimal = parentNorm2ValueFluxXList.get(0).getNorm2_value();
                goodness_of_fit = distribution.cumulativeProbability(norm2_minimal);

                double[] inter_vFreeValues = parentNorm2ValueFluxXList.get(0).getvFreeValues();
                reactionFluxCalculator.calculateReactionFlux(reactions, B, inter_vFreeValues);
                interCalProcessResult.setFlux_value(ReactionTool.convertReactionFluxToString(reactions));
                double[] inter_cFreeValues = parentNorm2ValueFluxXList.get(0).getcFreeValues();
                interCalProcessResult.setC_free_value(getCFreeValuesString(inter_cFreeValues));
                ModelingService.insertInterCalProcessResult(interCalProcessResult);
                if (norm2_minimal < 0.1 || count >= 300) {
                    break;
                }
            }

            double final_norm2_value = parentNorm2ValueFluxXList.get(0).getNorm2_value();
            double[] F = parentNorm2ValueFluxXList.get(0).getF();
            double[] cFreeValues = parentNorm2ValueFluxXList.get(0).getcFreeValues();
            double[] vFreeValues = parentNorm2ValueFluxXList.get(0).getvFreeValues();

            optimizingResult.setGoodness_of_fit(goodness_of_fit);
            optimizingResult.setNorm2(Util.formatNumber(final_norm2_value));

            StringBuffer sb = new StringBuffer();
            List<String> C_FreeList = cAndCFree.getC_FreeList();
            for(int index = 0; index < C_FreeList.size(); index++) {
                sb.append(C_FreeList.get(index) + Constant.INNER_SPLITTER + cFreeValues[index]);
                if(index < C_FreeList.size() - 1) {
                    sb.append(Constant.OUTER_SPLITTER);
                }
            }
            optimizingResult.setC_free_value(sb.toString());

            reactionFluxCalculator.calculateReactionFlux(reactions, B, vFreeValues);
            double[] vConsValues = new double[Count.consCount];
            for (Reaction reaction : reactions) {
                if (reaction.getFlux().isConsed()) {
                    vConsValues[reaction.getFlux().getIndex()] = reaction.getFlux().getValue();
                }
            }

            optimizingResult.setOptimizing_flux(ReactionTool.convertReactionFluxToString(reactions));
            optimizingResult.setX_value(computeXByFAndConvertToString(F));
            ModelingService.updateModelingCaseStatus(modelingCase, 5);
            calculateStatistics(parentNorm2ValueFluxXList.get(0), optimizingResult, n, vConsValues);

            ModelingService.insertOptimizingResult(optimizingResult);
        }catch (Exception e) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));
            String exception = baos.toString();
            System.out.println(exception);
            result = false;
        }
        return result;
    }

    private String computeXByFAndConvertToString(double[] F) {
        StringBuffer sb = new StringBuffer();
        int fIndex = 0;
        for (String emuKey : emuKeyMassSpectrometryMap.keySet()) {
            List<MassSpectrometry> massSpectrometryList = emuKeyMassSpectrometryMap.get(emuKey);
            for (double timePoint : timePoints) {
                double[] Mk = emuKeyMMap.get(emuKey).get(timePoint);
                for (int index = 0; index < massSpectrometryList.size(); index++) {
                    MassSpectrometry massSpectrometry = massSpectrometryList.get(index);
                    double x_value = F[fIndex] + Mk[index];
                    fIndex++;
                    sb.append(massSpectrometry.getMeta_name() + Constant.INNER_SPLITTER +
                            massSpectrometry.getFragment() + Constant.INNER_SPLITTER +
                            massSpectrometry.getWeight() + Constant.INNER_SPLITTER +
                            timePoint + Constant.INNER_SPLITTER + x_value + Constant.OUTER_SPLITTER);
                }
            }
        }
        Util.removeStringBufferEndTag(sb, Constant.OUTER_SPLITTER);
        return sb.toString();
    }

    private void calculateStatistics(Norm2ValueFluxX norm2ValueFluxX, OptimizingResult optimizingResult, final int n, double[] vConsValues) {
        double norm2_opt = norm2ValueFluxX.getNorm2_value();
        final double[] Wz = norm2ValueFluxX.getFluxX();
        double[] Wz_upper_bound = new double[Wz.length];
        double[] Wz_lower_bound = new double[Wz.length];

        List<BoundInfo> boundInfoList = new ArrayList<>();
        for(int i = 0; i < Wz.length; i++) {
            double[] Ni = VectorTool.generateZeroVector(Wz.length);
            Ni[i] = 1;
            BoundInfo boundInfo = new BoundInfo();
            boundInfo.setIndex(i);
            boundInfo.setNi(Ni);
            boundInfo.setNorm2_opt(norm2_opt);
            boundInfo.setUpperBound(true);
            boundInfoList.add(boundInfo);
            EMUX emux = new EMUX(reactionSizeEMU, emuReactions, reactionFluxCalculator);
            CAndCFree localCAndCFree = new CAndCFree(ftblFile.getPoolSizeMap(), reactionSizeEMU.getAllInterReactionEMUsMap());
            boundInfo.setEmux(emux);
            boundInfo.setcAndCFree(localCAndCFree);
        }

        for(int i = 0; i < Wz.length; i++) {
            double[] Ni = VectorTool.generateZeroVector(Wz.length);
            Ni[i] = -1;
            BoundInfo boundInfo = new BoundInfo();
            boundInfo.setIndex(i);
            boundInfo.setNi(Ni);
            boundInfo.setNorm2_opt(norm2_opt);
            boundInfo.setUpperBound(false);
            boundInfoList.add(boundInfo);
            EMUX emux = new EMUX(reactionSizeEMU, emuReactions, reactionFluxCalculator);
            CAndCFree localCAndCFree = new CAndCFree(ftblFile.getPoolSizeMap(), reactionSizeEMU.getAllInterReactionEMUsMap());
            boundInfo.setEmux(emux);
            boundInfo.setcAndCFree(localCAndCFree);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(8);
        for(final BoundInfo boundInfo : boundInfoList) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    calculateBound(boundInfo, Wz, n);
                }
            });
        }
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) {
                break;
            }
        }

        for(BoundInfo boundInfo : boundInfoList) {
            if(boundInfo.isUpperBound()) {
                Wz_upper_bound[boundInfo.getIndex()] = boundInfo.getBound();
            } else {
                Wz_lower_bound[boundInfo.getIndex()] = boundInfo.getBound();
            }
        }

        double[] vFree_lower_bound = VectorTool.getSubVetor(Wz_lower_bound, 0, Count.freeCount);
        double[] vFree_upper_bound = VectorTool.getSubVetor(Wz_upper_bound, 0, Count.freeCount);
        double[] vFree_deviation = VectorTool.computeDeviation(vFree_upper_bound, vFree_lower_bound);

        double[] vCons_deviation = reactionFluxCalculator.calculateConsDeviationByFree(vFree_deviation);
        double[] vCons_lower_bound = new double[reactions.size() - Count.freeCount];
        double[] vCons_upper_bound = new double[reactions.size() - Count.freeCount];
        for(int index = 0; index < vCons_deviation.length; index++) {
            vCons_lower_bound[index] = vConsValues[index] - vCons_deviation[index];
            if(vCons_lower_bound[index] < 0) {
                vCons_lower_bound[index] = 0;
            }
            vCons_upper_bound[index] = vConsValues[index] + vCons_deviation[index];
        }

        double[] cFree_upper_bound = VectorTool.getSubVetor(Wz_upper_bound, Count.freeCount, Wz_upper_bound.length);
        double[] cFree_lower_bound = VectorTool.getSubVetor(Wz_lower_bound, Count.freeCount, Wz_lower_bound.length);

        cFree_upper_bound = VectorTool.formatVector(cFree_upper_bound);
        cFree_lower_bound = VectorTool.formatVector(cFree_lower_bound);
        List<String> C_FreeList = cAndCFree.getC_FreeList();
        StringBuffer sb = new StringBuffer();
        for(int index = 0; index < C_FreeList.size(); index++) {
            sb.append(C_FreeList.get(index) + Constant.INNER_SPLITTER
                    + "[" + cFree_lower_bound[index] + " , " + cFree_upper_bound[index] + "]");
            if(index < C_FreeList.size() - 1) {
                sb.append(Constant.OUTER_SPLITTER);
            }
        }
        optimizingResult.setC_confidence_interval(sb.toString());

        vFree_lower_bound = VectorTool.formatVector(vFree_lower_bound);
        vFree_upper_bound = VectorTool.formatVector(vFree_upper_bound);
        vCons_lower_bound = VectorTool.formatVector(vCons_lower_bound);
        vCons_upper_bound = VectorTool.formatVector(vCons_upper_bound);

        sb = new StringBuffer();
        for(int rIndex = 0; rIndex < reactions.size(); rIndex++) {
            Reaction reaction = reactions.get(rIndex);
            sb.append(reaction.getName() + Constant.INNER_SPLITTER);
            int index = reaction.getFlux().getIndex();
            if(reaction.getFlux().isConsed()) {
                sb.append("[" + vCons_lower_bound[index] + " , " + vCons_upper_bound[index] + "]");
            } else {
                sb.append("[" + vFree_lower_bound[index] + " , " + vFree_upper_bound[index] + "]");
            }
            if(rIndex < reactions.size() - 1) {
                sb.append(Constant.OUTER_SPLITTER);
            }
        }
        optimizingResult.setFlux_confidence_interval(sb.toString());
    }

    private void calculateBound(BoundInfo boundInfo, double[] Wz, int n) {
        double chi_distribution_value = 3.84;
        int index = boundInfo.getIndex();
        double[] Ni = boundInfo.getNi();
        double norm2_opt = boundInfo.getNorm2_opt();
        double[] Vk = VectorTool.deepCopyVector(Wz);
        double[] dk = VectorTool.appendVector(new double[]{0}, Vk);
        double hk = 0.01;
        FAndS fAndS = calculateStatisticsXFS(timePoints, Vk, boundInfo.getEmux(), boundInfo.getcAndCFree());
        double norm2k_value;
        int k = 0;
        while (true) {
            k++;
            double[][] H = new Matrix(fAndS.getS()).times(SigmaXMat.inverse()).times(new Matrix(fAndS.getS()).transpose()).getArray();
            double[] J = MatrixTool.getMatrixCol(
                    new Matrix(fAndS.getS()).
                            times(SigmaXMat.inverse()).
                            times(new Matrix(MatrixTool.convertVectorToColMatrix(fAndS.getF()))).getArray(),
                    0);
            
            double[][] Aerror = generateAerror(H, Ni);
            double[] berror = generateBerror(J, hk);
            double[] delta_d = MatrixTool.getMatrixCol(
                    MatrixTool.calculateInverseMatrix(Aerror).
                            times(new Matrix(MatrixTool.convertVectorToColMatrix(berror))).getArray(),
                    0);
            double[] delta_v = VectorTool.getSubVetor(delta_d, 0, delta_d.length - 1);
            dk = VectorTool.addVector(dk, delta_d);
            Vk = VectorTool.getSubVetor(dk, 0, Vk.length);
            if(Vk[index] >= ub[index] || Vk[index] <= lb[index]) {
                boundInfo.setBound(Vk[index]);
                break;
            }
            fAndS = calculateStatisticsXFS(timePoints, Vk, boundInfo.getEmux(), boundInfo.getcAndCFree());
            norm2k_value = computeNorm2(fAndS.getF(), n);
            if(norm2k_value - norm2_opt > chi_distribution_value || k > 3000) {
                boundInfo.setBound(Vk[index]);
                break;
            }
            double a1 = 2 * VectorTool.vectorTimesVector(delta_v, J);
            Matrix delta_v_H = new Matrix(MatrixTool.convertVectorToRowMatrix(delta_v)).times(new Matrix(H));
            double a2 = VectorTool.vectorTimesVector(
                    MatrixTool.getMatrixRow(delta_v_H.getArray(), 0),
                    delta_v);
            double a3 = Math.abs(norm2k_value - norm2_opt - (a1 * hk + a2 * Math.pow(hk, 2))) / Math.pow(hk, 3);
            hk = Math.pow(1e-4 / a3, 1.0 / 3);
        }
    }

    private double[][] generateAerror(double[][] H, double[] Ni) {
        double[][] Aerror = new double[Ni.length + 1][Ni.length + 1];
        for(int row = 0; row < Ni.length + 1; row++) {
            for(int col = 0; col < Ni.length + 1; col++) {
                if(row < Ni.length && col < Ni.length) {
                    Aerror[row][col] = 2 * H[row][col];
                } else if(row < Ni.length && col == Ni.length) {
                    Aerror[row][col] = Ni[row];
                } else if(col < Ni.length && row == Ni.length) {
                    Aerror[row][col] = Ni[col];
                }
            }
        }
        return Aerror;
    }

    private double[] generateBerror(double[] J, double hk) {
        double[] berror = new double[J.length + 1];
        for(int index = 0; index < J.length; index++) {
            berror[index] = -2 * J[index];
        }
        berror[J.length] = hk;
        return berror;
    }

    private List<Norm2ValueFluxX> calculateMutantChildNorm2ValueFluxX(List<Norm2ValueFluxX> parentNorm2ValueFluxXList, int k1, int k2) {
        List<Reaction> freeReactionList = new ArrayList<>();
        for (Reaction reaction : reactions) {
            if (!reaction.getFlux().isConsed()) {
                freeReactionList.add(reaction);
            }
        }

        List<Norm2ValueFluxX> mutantNorm2ValueFluxXList = new ArrayList<>();
        List<List<AbsConsNameValue>> constraisList = new ArrayList<>();
        List<List<Integer>> parentRandomFixedVIndexList = new ArrayList<>();
        for (final Norm2ValueFluxX parentNorm2ValueFluxX : parentNorm2ValueFluxXList) {
            double[] vFreeValues = parentNorm2ValueFluxX.getvFreeValues();
            double[] cFreeValues = parentNorm2ValueFluxX.getcFreeValues();
            List<Integer> randomFixedVIndexList = VectorTool.sampleArrayIndexRandom(vFreeValues, k1);
            parentRandomFixedVIndexList.add(randomFixedVIndexList);
            List<AbsConsNameValue> consNameValueList = new ArrayList<>();
            for (Integer vFreeIndex : randomFixedVIndexList) {
                consNameValueList.add(new FluxNet(freeReactionList.get(vFreeIndex).getName(), vFreeValues[vFreeIndex]));
            }
            constraisList.add(consNameValueList);

            List<Integer> randomFixedCIndexList = VectorTool.sampleArrayIndexRandom(cFreeValues, k2);
            double[] mutantCFreeValues = VectorTool.generateRandomVector(Count.cFreeCount, Constant.C_FREE_MIN_VALUE, Constant.C_FREE_MAX_VALUE);
            for (Integer cFreeIndex : randomFixedCIndexList) {
                mutantCFreeValues[cFreeIndex] = cFreeValues[cFreeIndex];
            }
            Norm2ValueFluxX mutantNorm2ValueFluxX = new Norm2ValueFluxX();
            mutantNorm2ValueFluxX.setcFreeValues(mutantCFreeValues);
            mutantNorm2ValueFluxXList.add(mutantNorm2ValueFluxX);
        }
        Argument.sample_count = 1;
        List<List<String>> constrainsReactionFluxesList = SampleUtil.sampleReactionFluxWithFile(reactions, ftblFile, Argument.python, constraisList);
        for (int index = 0; index < constraisList.size(); index++) {
            Norm2ValueFluxX mutantNorm2ValueFluxX = mutantNorm2ValueFluxXList.get(index);
            String reactionFluxString = constrainsReactionFluxesList.get(index).get(0);
            double[] mutantVFreeValues = ReactionTool.parseVFreeValuesFromReactionFluxString(reactionFluxString, reactions);
            List<Integer> randomFixedVIndexList = parentRandomFixedVIndexList.get(index);
            double[] vFreeValues = parentNorm2ValueFluxXList.get(index).getvFreeValues();
            for (Integer vFreeIndex : randomFixedVIndexList) {
                mutantVFreeValues[vFreeIndex] = vFreeValues[vFreeIndex];
            }
            mutantNorm2ValueFluxX.setvFreeValues(mutantVFreeValues);
            mutantNorm2ValueFluxX.generateFluxXFromVAndCFreeValues();
        }
        return mutantNorm2ValueFluxXList;
    }

    private List<Norm2ValueFluxX> calculateCrossChildNorm2ValueFluxX(List<Norm2ValueFluxX> parentNorm2ValueFluxXList, double alpha) {
        List<Norm2ValueFluxX> result = new ArrayList<>();
        for (int index = 0; index < parentNorm2ValueFluxXList.size(); index = index + 2) {
            Norm2ValueFluxX parentNorm2ValueFluxX1 = parentNorm2ValueFluxXList.get(index);
            Norm2ValueFluxX parentNorm2ValueFluxX2 = parentNorm2ValueFluxXList.get(index + 1);
            Norm2ValueFluxX crossNorm2ValueFluxX1 = new Norm2ValueFluxX();
            Norm2ValueFluxX crossNorm2ValueFluxX2 = new Norm2ValueFluxX();
            double[] Wz1 = parentNorm2ValueFluxX1.getFluxX();
            double[] Wz2 = parentNorm2ValueFluxX2.getFluxX();
            double[] Wzi = VectorTool.addVector(VectorTool.vectorTimesNumber(Wz1, alpha),
                    VectorTool.vectorTimesNumber(Wz2, 1 - alpha));
            double[] Wzj = VectorTool.addVector(VectorTool.vectorTimesNumber(Wz1, 1 - alpha),
                    VectorTool.vectorTimesNumber(Wz2, alpha));

            crossNorm2ValueFluxX1.setFluxX(Wzi);
            crossNorm2ValueFluxX2.setFluxX(Wzj);
            result.add(crossNorm2ValueFluxX1);
            result.add(crossNorm2ValueFluxX2);
        }
        return result;
    }

    private Norm2ValueCalculatingFactor generateNorm2ValueCalculatingFactor(double[] Wz) {
        double[] vFreeValues = VectorTool.getSubVetor(Wz, 0, Count.freeCount);
        double[] cFreeValues = VectorTool.getSubVetor(Wz, Count.freeCount, Wz.length);
        reactionFluxCalculator.calculateReactionFlux(reactions, B, vFreeValues);
        EMUX emux = new EMUX(reactionSizeEMU, emuReactions, reactionFluxCalculator);
        cAndCFree.initCAndCFree(cFreeValues, allInterReactionEMUsMap, emux.getSbCalculator());
        Map<Integer, Double> C_PosValueMap = cAndCFree.getCloneC_PosValueMap();
        return new Norm2ValueCalculatingFactor(emux, C_PosValueMap);
    }

    private void calculateNorm2ValueInThread(List<Norm2ValueFluxX> norm2ValueFluxXList,
                                             List<Norm2ValueCalculatingFactor> calculatingFactorList,
                                             final int n) {
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        for (int index = 0; index < norm2ValueFluxXList.size(); index++) {
            final Norm2ValueFluxX norm2ValueFluxX = norm2ValueFluxXList.get(index);
            final Norm2ValueCalculatingFactor calculatingFactor = calculatingFactorList.get(index);
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    Map<String, Map<Double, double[]>> sizeEMUTimePointX = calculateSizeEMUTimePointX(timePoints, calculatingFactor);
                    double[] F = computeF(sizeEMUTimePointX);
                    double norm2_value = computeNorm2(F, n);
                    norm2ValueFluxX.setNorm2_value(norm2_value);
                    norm2ValueFluxX.setF(F);
                }
            });
        }
        executorService.shutdown();
        while (true) {
            if (executorService.isTerminated()) {
                break;
            }
        }

    }

    private FAndS calculateStatisticsXFS(double[] timePoints, double[] Vk, EMUX emux, CAndCFree localCAndCFree) {
        double[] vFreeValues = VectorTool.getSubVetor(Vk, 0, Count.freeCount);
        double[] cFreeValues = VectorTool.getSubVetor(Vk, Count.freeCount, Vk.length);
        Map<String, Double> reactionNameValueMap = reactionFluxCalculator.calculateReactionFluxMap(reactions, B, vFreeValues);
        emux.updateReactionCoefficientMatrixElement(reactionNameValueMap);
        localCAndCFree.initCAndCFree(cFreeValues, allInterReactionEMUsMap, emux.getSbCalculator());

        double[] sizeEMUXn = null;
        double[] F = null;
        double[][] S = null;
        double T0 = 0;
        for (int index = 0; index < timePoints.length; index++) {
            double Tn = timePoints[index];
            if (sizeEMUXn == null) {
                sizeEMUXn = emux.calculateX(localCAndCFree.getCloneC_PosValueMap(), localCAndCFree.getCloneCLCJ_PosValueMap(), T0, Tn, null);
            } else {
                sizeEMUXn = emux.calculateX(localCAndCFree.getCloneC_PosValueMap(), localCAndCFree.getCloneCLCJ_PosValueMap(), T0, Tn, sizeEMUXn);
            }

            Map<String, double[]> emuKeyXMap = XTool.parseSizeEMUKeyXWithDxdw(interReactionSizeEMUsMap, sizeEMUXn);

            for (String emuKey : emuKeyMassSpectrometryMap.keySet()) {
                List<MassSpectrometry> massSpectrometryList = emuKeyMassSpectrometryMap.get(emuKey);
                double[] Mk = emuKeyMMap.get(emuKey).get(Tn);
                double[] Fk = new double[Mk.length];
                double[] Xk = emuKeyXMap.get(emuKey);
                double[][] Sk = new double[Count.freeCount + Count.cFreeCount][Mk.length];
                for (int mIndex = 0; mIndex < massSpectrometryList.size(); mIndex++) {
                    MassSpectrometry massSpectrometry = massSpectrometryList.get(mIndex);
                    Fk[mIndex] = Xk[massSpectrometry.getWeight()] - Mk[mIndex];
                    int size = massSpectrometry.getSize();
                    for(int vFreeIndex = 0; vFreeIndex < Count.freeCount; vFreeIndex++) {
                        int vBase = (vFreeIndex + 1) * (size + 1);
                        Sk[vFreeIndex][mIndex] = Xk[vBase + massSpectrometry.getWeight()];
                    }
                    for(int cFreeIndex = 0; cFreeIndex < Count.cFreeCount; cFreeIndex++) {
                        int cBase = (Count.freeCount + 1 + cFreeIndex) * (size + 1);
                        Sk[cFreeIndex + Count.freeCount][mIndex] = Xk[cBase + massSpectrometry.getWeight()];
                    }
                }
                F = VectorTool.appendVector(Fk, F);
                S = MatrixTool.concatenateMatrix(Sk, S);
            }
            T0 = Tn;
        }

        return new FAndS(F, S);
    }

    private Map<String, Map<Double, double[]>> calculateSizeEMUTimePointX(double[] timePoints, Norm2ValueCalculatingFactor calculatingFactor) {
        Map<String, Map<Double, double[]>> emuKeyTimePointX = new HashMap<>();
        double[] sizeEMUXn = null;
        EMUX emux = calculatingFactor.getEmux();
        double T0 = 0;
        for (int index = 0; index < timePoints.length; index++) {
            double Tn = timePoints[index];
            if (sizeEMUXn == null) {
                sizeEMUXn = emux.calculateX(calculatingFactor.getC_PosValueMap(), calculatingFactor.getCLCJ_PosValueMap(), T0, Tn, null);
            } else {
                sizeEMUXn = emux.calculateX(calculatingFactor.getC_PosValueMap(), calculatingFactor.getCLCJ_PosValueMap(), T0, Tn, sizeEMUXn);
            }
            if(sizeEMUXn == null) {
                return null;
            }
            Map<String, double[]> emuKeyXMap = XTool.parseSizeEMUKeyX(interReactionSizeEMUsMap, sizeEMUXn);
            for (String emuKey : emuKeyXMap.keySet()) {
                Map<Double, double[]> timePointX = emuKeyTimePointX.get(emuKey);
                if (timePointX == null) {
                    timePointX = new HashMap<>();
                    emuKeyTimePointX.put(emuKey, timePointX);
                }
                timePointX.put(Tn, emuKeyXMap.get(emuKey));
            }
            T0 = Tn;
        }
        return emuKeyTimePointX;
    }

    private Map<String, List<MassSpectrometry>> computeEMUKeyMassSpectrometry(FTBLFile ftblFile) {
        Map<String, List<MassSpectrometry>> result = new HashMap<>();
        String emuKey = "";
        List<MassSpectrometry> list = null;
        for (MassSpectrometry massSpectrometry : ftblFile.getMassSpectrometryList()) {
            String key = massSpectrometry.getEMUKey();
            if (!emuKey.equals(key)) {
                emuKey = key;
                list = new ArrayList<>();
                result.put(emuKey, list);
            }
            list.add(massSpectrometry);
        }
        return result;
    }

    private double computeNorm2(double[] F, int n) {
        Matrix FtSigmaX = new Matrix(MatrixTool.convertVectorToRowMatrix(F)).times(SigmaXMat.inverse());
        double[] FtSigmaX_Vector = MatrixTool.getMatrixRow(FtSigmaX.getArray(), 0);
        double norm2_value = VectorTool.vectorTimesVector(FtSigmaX_Vector, F);
        norm2_value = norm2_value / n;
        return norm2_value;
    }

    private double[] computeF(Map<String, Map<Double, double[]>> sizeEMUTimePointX) {
        double[] F = null;

        for (String emuKey : emuKeyMassSpectrometryMap.keySet()) {
            List<MassSpectrometry> massSpectrometryList = emuKeyMassSpectrometryMap.get(emuKey);
            for (double timePoint : timePoints) {
                double[] timePointX = sizeEMUTimePointX.get(emuKey).get(timePoint);
                double[] Xk = new double[massSpectrometryList.size()];
                double[] Mk = emuKeyMMap.get(emuKey).get(timePoint);
                for (int index = 0; index < massSpectrometryList.size(); index++) {
                    MassSpectrometry massSpectrometry = massSpectrometryList.get(index);
                    Xk[index] = timePointX[massSpectrometry.getWeight()];
                }
                double[] Fk = VectorTool.subVector(Xk, Mk);
                F = VectorTool.appendVector(Fk, F);
            }
        }

        return F;
    }

    private String getCFreeValuesString(double[] cFreeValues) {
        StringBuffer sb = new StringBuffer();
        List<String> C_FreeList = cAndCFree.getC_FreeList();
        for(int index = 0; index < C_FreeList.size(); index++) {
            sb.append(C_FreeList.get(index) + Constant.INNER_SPLITTER + cFreeValues[index]);
            if(index < C_FreeList.size() - 1) {
                sb.append(Constant.OUTER_SPLITTER);
            }
        }
        return sb.toString();
    }

}
