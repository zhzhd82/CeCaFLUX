# -*- coding: utf-8 -*-

from cobra import Model, Reaction, Metabolite
from collections import namedtuple
import cobra
import argparse
import os
# import time


def optimizer(reaction_args):
    model = Model("optimizer_model")
    reactions = []
    metabolite_check_dict = {}
    for reaction_string in reaction_args.reactions_string.split(reaction_args.reaction_splitter):
        reaction_splits = reaction_string.split(reaction_args.reaction_name_splitter)
        reaction = Reaction(reaction_splits[0])
        reaction.name = reaction_splits[0]
        reaction.lower_bound = float(reaction_args.lower_bound)
        reaction.upper_bound = float(reaction_args.upper_bound)
        metabolite_splits = reaction_splits[1].split(reaction_args.equation_splitter)
        metabolites_dict = {}
        for reactant in metabolite_splits[0].split(reaction_args.metabolite_splitter):
            reactant_metabolite = get_or_create_metabolite(reactant, metabolite_check_dict)
            if reactant_metabolite in metabolites_dict:
                metabolites_dict[reactant_metabolite] = -2.0
            else:
                metabolites_dict[reactant_metabolite] = -1.0
        if "_ex" not in metabolite_splits[1]:
            for product in metabolite_splits[1].split(reaction_args.metabolite_splitter):
                product_metabolite = get_or_create_metabolite(product, metabolite_check_dict)
                if product_metabolite in metabolites_dict:
                    metabolites_dict[product_metabolite] = 2.0
                else:
                    metabolites_dict[product_metabolite] = 1.0
        reaction.add_metabolites(metabolites_dict)
        reactions.append(reaction)
    model.add_reactions(reactions)

    for constrain_string in reaction_args.constrains_string.split(reaction_args.cons_splitter):
        constrain_splits = constrain_string.split(reaction_args.cons_element_splitter)
        ub_value = None
        lb_value = None
        if constrain_splits[1] != "None":
            lb_value = float(constrain_splits[1])
        if constrain_splits[2] != "None":
            ub_value = float(constrain_splits[2])
        model.add_cons_vars(model.problem.Constraint(model.reactions.get_by_id(constrain_splits[0]).flux_expression,
                                                     lb=lb_value, ub=ub_value))

    if reaction_args.equalities_string != "None":
        for equality_string in reaction_args.equalities_string.split(reaction_args.cons_splitter):
            equality_splits = equality_string.split(reaction_args.cons_element_splitter)
            value = float(equality_splits[2])
            coefficient_splits = equality_splits[0].split(reaction_args.cons_element_coefficient_splitter)
            r_name1 = coefficient_splits[1]
            coefficient1 = float(coefficient_splits[0])
            coefficient_splits = equality_splits[1].split(reaction_args.cons_element_coefficient_splitter)
            r_name2 = coefficient_splits[1]
            coefficient2 = float(coefficient_splits[0])
            same_flux = model.problem.Constraint(
                coefficient1 * model.reactions.get_by_id(r_name1).flux_expression
                + coefficient2 * model.reactions.get_by_id(r_name2).flux_expression,
                lb=value, ub=value)
            model.add_cons_vars(same_flux)


    for reaction_string in reaction_args.reactions_string.split(reaction_args.reaction_splitter):
        reaction_splits = reaction_string.split(reaction_args.reaction_name_splitter)
        r_name = reaction_splits[0]
        if r_name.endswith("_ex"):
            continue

        model.objective = r_name
        solution = model.optimize("maximize")
        write_content_to_file(args.result_file, "########### optimizer value start ###########", "a")
        for reaction_name, value in solution.fluxes.items():
            write_content_to_file(args.result_file, reaction_name + " : " + str(value), "a")


def get_or_create_metabolite(metabolite_name, metabolite_check_dict):
    if "_ex" in metabolite_name:
        metabolite_name = metabolite_name[0:len(metabolite_name) - 3]
    if metabolite_name in metabolite_check_dict:
        return metabolite_check_dict[metabolite_name]
    metabolite = Metabolite(metabolite_name)
    metabolite.name = metabolite_name
    metabolite_check_dict[metabolite_name] = metabolite
    return metabolite


def df_iter(d, cols=None):
    if cols is None:
        v = d.values.tolist()
        cols = d.columns.values.tolist()
    else:
        j = [d.columns.get_loc(c) for c in cols]
        v = d.values[:, j].tolist()

    n = namedtuple('ReactionFluxTuple', cols)

    for line in iter(v):
        yield n(*line)


def write_content_to_file(file_name, content, mode):
    with open(file_name, mode) as file:
        file.write(content)
        file.write("\n")


def read_file(file_name):
    with open(file_name, 'r') as file:
        content = file.read()

    return content


if __name__ == "__main__":
    parser = argparse.ArgumentParser("reaction splitters parser")
    parser.add_argument("--reactions_content_file", help="reactions content string file path", type=str)
    parser.add_argument("--constrains_content_splitter", help="constrains string contents splitter", type=str)
    parser.add_argument("--constrains_content_file", help="constrains string file", type=str)
    parser.add_argument("--result_file", help="sample result file", type=str)
    parser.add_argument("--equalities_string", help="equality constrain string", type=str)
    parser.add_argument("--reaction_splitter", help="reactions string splitter", type=str)
    parser.add_argument("--reaction_name_splitter", help="reaction name and equation splitter", type=str)
    parser.add_argument("--equation_splitter", help="reaction reactants and products splitter", type=str)
    parser.add_argument("--metabolite_splitter", help="equation metabolites splitter", type=str)
    parser.add_argument("--cons_splitter", help="Constraints splitter", type=str)
    parser.add_argument("--cons_element_splitter", help="Constraint elements splitter", type=str)
    parser.add_argument("--cons_element_coefficient_splitter", help="Constraint element coefficient splitter", type=str)
    parser.add_argument("--lower_bound", help="lower bound value", type=str)
    parser.add_argument("--upper_bound", help="upper bound value", type=str)
    try:
        args = parser.parse_args()
        if os.path.exists(args.result_file):
            os.remove(args.result_file)
        args.reactions_string = read_file(args.reactions_content_file)
        constrains_contents = read_file(args.constrains_content_file)
        for constrains_content in constrains_contents.split(args.constrains_content_splitter):
            args.constrains_string = constrains_content
            optimizer(args)
            break

    except Exception as e:
        write_content_to_file(args.result_file, "Exception  occurs : " + str(e), "w")

