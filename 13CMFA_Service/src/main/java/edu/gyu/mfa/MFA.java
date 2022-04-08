package edu.gyu.mfa;

import edu.gyu.mfa.calculator.ReactionFluxCalculator;
import edu.gyu.mfa.db.bean.ModelingCase;
import edu.gyu.mfa.emu.EMUTool;
import edu.gyu.mfa.ftbl.FTBLFile;
import edu.gyu.mfa.graph.SizeEMUGraph;
import edu.gyu.mfa.info.Argument;
import edu.gyu.mfa.info.CAndCFree;
import edu.gyu.mfa.matrix.FluxEquationMatrix;
import edu.gyu.mfa.matrix.MatrixConsFreeInfo;
import edu.gyu.mfa.matrix.MatrixTool;
import edu.gyu.mfa.optimizer.OptimizerMutation;
import edu.gyu.mfa.reaction.*;
import edu.gyu.mfa.rungekutta.RungeKuttaParameter;
import edu.gyu.mfa.service.ModelingService;
import edu.gyu.mfa.util.Util;

import java.util.List;

public class MFA {

    private List<Reaction> reactions;
    private List<EMUReaction> emuReactions;
    private FTBLFile ftblFile;
    private FluxEquationMatrix fluxEquationMatrix;
    private ReactionSizeEMU reactionSizeEMU;
    private ReactionFluxCalculator reactionFluxCalculator;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: args: \n" +
                    "flux_sampling_file=The flux sampling program file\n" +
                    "python=python|python3\n" +
                    "| tolerance_addition_factor=The RungeKutta adaptive mode tolerance_addition_factor, default is 1e-10\n" +
                    "| tolerance_scaling_factor=The RungeKutta adaptive mode tolerance_scaling_factor, default is 1e-8\n" +
                    "| sample_lower_bound=The sampling lower bound value\n" +
                    "| sample_upper_bound=The sampling upper bound value\n" +
                    "| cpu_cores_keep=The cores of cpu to keep\n");
            return;
        }
        Util.parseArgs(args);
        new MFA().start();
    }

    private void start() {
        while (true) {
            ModelingCase modelingCase = null;
            try {
                modelingCase = ModelingService.queryModelingCaseByStatus(1);
                if(modelingCase != null) {
                    ModelingService.updateModelingCaseStatus(modelingCase, 2);
                    calculateModelCase(modelingCase);
                    ModelingService.updateModelingCaseStatus(modelingCase, 3);
                }
                Thread.sleep(5 * 1000);
            }catch (Exception e) {
                if(modelingCase != null) {
                    ModelingService.updateModelingCaseStatus(modelingCase, 1);
                }
                try{
                    Thread.sleep(10 * 1000);
                }catch (Exception ex) {
                }
            }
        }
    }

    private void calculateModelCase(ModelingCase modelingCase) {
        String step = modelingCase.getStep();
        if(!Util.isBlank(step)) {
            Argument.step = Double.parseDouble(step);
        }
        if(modelingCase.getMethod().equals("fixed")) {
            Argument.isAdaptive = false;
        } else {
            Argument.isAdaptive = true;
        }
        initRungeKuttaParameter();
        parseNetowrkFromModelCase(modelingCase);
        if(!initAndCheckNetwork()) {
            ModelingService.updateModelingCaseStatus(modelingCase, 4);
            return;
        }
        initReactionFluxCalculator();
        initEMU();
        if(accelerateEMUAlgorithm(modelingCase)) {
            System.out.println("----------------------- Success ----------------------");
        } else {
            System.out.println("----------------------- Error ----------------------");
            ModelingService.updateModelingCaseStatus(modelingCase, 4);
        }

    }

    private void parseNetowrkFromModelCase(ModelingCase modelingCase) {
        ftblFile = new FTBLFile(modelingCase);
        reactions = ReactionParser.parseNetwork(modelingCase);
        for(Reaction reaction : reactions) {
            reaction.generateCompoundIsotopomers();
        }
        ReactionTool.computeInitIsotopomerValue(ftblFile, reactions);
    }

    private boolean initAndCheckNetwork() {
        fluxEquationMatrix = new FluxEquationMatrix(ftblFile);
        fluxEquationMatrix.computeStoichiometricMatrix(reactions, false);
        for (Reaction reaction : reactions) {
            reaction.parseType(fluxEquationMatrix);
            reaction.computeAtomTransferMatrix();
        }
        fluxEquationMatrix.computeStoichiometricMatrix(reactions, true);
        fluxEquationMatrix.computeSaAndSi();
        if (!fluxEquationMatrix.checkByRank()) {
            System.err.println("The ranks of Sa and Si are not equal, please check FTBL network!");
            return false;
        }
        return true;
    }

    private void initReactionFluxCalculator() {
        MatrixConsFreeInfo consFreeInfo = MatrixTool.computeMatrixConsFree(null, fluxEquationMatrix.getSa());
        reactionFluxCalculator = new ReactionFluxCalculator(consFreeInfo);
        reactionFluxCalculator.calculateReactionConsFreeInfo(reactions);
    }

    private void initEMU() {
        emuReactions = ReactionTool.generateEMUReactions(reactions);
        EMUTool.computeInputReactionInitValue(reactions, emuReactions);
        reactionSizeEMU = new ReactionSizeEMU();
        reactionSizeEMU.computeCompoundNameCarbon(reactions);
        reactionSizeEMU.generateSizeEMUs(emuReactions);
        reactionSizeEMU.computeInputReactionEMUPosition();
        reactionSizeEMU.computeInterReactionEMUPosition();
    }

    private boolean accelerateEMUAlgorithm(ModelingCase modelingCase) {
        SizeEMUGraph sizeEMUGraph = new SizeEMUGraph(reactionSizeEMU);
        sizeEMUGraph.filterSizeEMU(emuReactions, ftblFile);
        EMUTool.computeInterReactionInitValue(reactionSizeEMU.getAllInterReactionEMUsMap());
        ReactionTool.computeEMUReactionFluxFromCompoundReaction(reactions, emuReactions);
        return optimize(modelingCase);
    }

    private boolean optimize(ModelingCase modelingCase) {
        CAndCFree cAndCFree = new CAndCFree(ftblFile.getPoolSizeMap(), reactionSizeEMU.getAllInterReactionEMUsMap());
        OptimizerMutation optimizer = new OptimizerMutation(reactionSizeEMU, reactionFluxCalculator, cAndCFree, emuReactions, reactions, ftblFile, modelingCase);
        return optimizer.optimize(fluxEquationMatrix.getB());
    }

    private void initRungeKuttaParameter() {
        RungeKuttaParameter.Step = Argument.step;
        RungeKuttaParameter.tolerance_scaling_factor = Argument.tolerance_scaling_factor;
        RungeKuttaParameter.tolerance_addition_factor = Argument.tolerance_addition_factor;
    }
}
