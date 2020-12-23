# Run all scripts sequentially:
options(warn=-1) # warning off
print("*************************************************")
print("Running association rule mining...")
source("AssociationRuleMining.R")

print("*************************************************")
print("Running Distribution analysis...")
source("importanceDistribution.R")
source("OrdinalAnalysis.R")
source("OrdinalRepresentation.R")

print("*************************************************")
print("Running Sequence Distribution Analysis...")
source("prominence-categorical-distribution.R")
source("scale-categorical-distribution.R")
source("type-categorical-distribution.R")


