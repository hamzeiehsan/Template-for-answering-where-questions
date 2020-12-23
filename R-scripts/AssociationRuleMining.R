####################################################
#Installing the packages
#install.packages("Matrix")
#install.packages("arules")
#install.packages("qlcMatrix")
#install.packages("data.table")
#install.packages("ggplot2")
#install.packages("ggpubr")
#install.packages("gridExtra")
#install.packages("stringr")
#install.packages("RColorBrewer")
####################################################
#set workspace to this folder
setwd("D:/Work/IJGIS/R-scripts")
####################################################
#importing the packages
library(arules)
library(qlcMatrix)
library(data.table)
library(ggplot2)
library(ggpubr)
library(gridExtra)
library(stringr)
library(RColorBrewer)
####################################################

#######################################
#######################################
###############TYPE##############
#######################################
#######################################

type_raw <- read.table(file = "../sequences/type-nf-all.txt", sep = ",")
transpose_type_raw <- t(type_raw)[1, ]
pw <- pwMatrix(transpose_type_raw, sep = " ")
tt <- ttMatrix(pw$rownames)
distr <- (tt$M*1) %*% (pw$M*1)
distr_ngCMat = as (distr, "ngCMatrix")
ttRows <- tt$rownames
td <- as.data.frame(ttRows)


setnames(td, "ttRows", "labels")
trans1 <- new("transactions", data = distr_ngCMat, itemInfo = td)

type_rules <- apriori(trans1, parameter = list(support = 100/trans1@data@Dim[2],
                                               confidence = 0.5, maxlen = 3, maxtime = 15))
type_rules_d <- inspect(head(sort(type_rules, by = "lift"), 100))
#plot(type_rules, jitter = 0)
write.csv(x = type_rules_d, file = "result/type_rules_qa.csv")


#######################################
#######################################
###############SCALE##############
#######################################
#######################################

scale_raw <- read.table("../sequences/scale-nf-all.txt", sep = ",")
transpose_scale_raw <- t(scale_raw)[1, ]
pw_scale <- pwMatrix(transpose_scale_raw, sep = " ")
tt_scale <- ttMatrix(pw_scale$rownames)
distr_scale <- (tt_scale$M*1) %*% (pw_scale$M*1)
distr_ngCMat_scale = as (distr_scale, "ngCMatrix")
ttRows_scale <- tt_scale$rownames
td_scale <- as.data.frame(ttRows_scale)

setnames(td_scale, "ttRows_scale", "labels")
trans_scale <- new("transactions", data = distr_ngCMat_scale, itemInfo = td_scale)

scale_rules <- apriori(trans_scale, parameter = list(support = 100/trans_scale@data@Dim[2],
                                                     confidence = 0.5, maxlen = 3, maxtime = 15))
inspect(head(sort(scale_rules, by = "support"), 100))

scale_rules_d <- inspect(head(sort(scale_rules, by = "support"), 100))
#plot(scale_rules, jitter = 0)
write.csv(x = scale_rules_d, file = "result/scale_rules_qa.csv")

#######################################
#######################################
###############PROMINENCE##############
#######################################
#######################################

imp_raw <- read.table("../sequences/prominence-nf-all.txt", sep = ",")
transpose_imp_raw <- t(imp_raw)[1, ]
pw_imp <- pwMatrix(transpose_imp_raw, sep = " ")
tt_imp <- ttMatrix(pw_imp$rownames)
distr_imp <- (tt_imp$M*1) %*% (pw_imp$M*1)
distr_ngCMat_imp = as (distr_imp, "ngCMatrix")
ttRows_imp <- tt_imp$rownames
td_imp <- as.data.frame(ttRows_imp)

setnames(td_imp, "ttRows_imp", "labels")
trans_imp <- new("transactions", data = distr_ngCMat_imp, itemInfo = td_imp)

imp_rules <- apriori(trans_imp, parameter = list(support = 100/trans_imp@data@Dim[2],
                                                 confidence = 0.5, maxlen = 3, maxtime = 15))
inspect(head(sort(imp_rules, by = "support"), 100))

imp_rules_d <- inspect(head(sort(imp_rules, by = "support"), 100))

write.csv(x = imp_rules_d, file = "result/prominence_rules_qa.csv")


#######################################################################################################
#######################################################################################################


##############################################FUNCTIONS################################################
fun.extract.ncomplex.ids = function(questions, n) {
  validIds = c()
  counter = 0
  for (i in 1:length(questions[,1])) {
    qVals = questions[i, 2:length(questions)]
    if (length(qVals[qVals!=""]) == n) {
      counter= counter + 1
      validIds[counter] = questions[i, 1]
    }
  }
  return (validIds)
}

fun.write.simple.complex = function(all, questions, fileAddressSWQ, fileAddressDWQ) {
  swq_ids <- fun.extract.ncomplex.ids(questions, 1)
  swq <- all[all$V1 %in% swq_ids, 2:length(all)]
  dwq <- all[!all$V1 %in% swq_ids, 2:length(all)]
  write.table(row.names = FALSE, file = fileAddressSWQ, x = swq, col.names = FALSE)
  write.table(row.names = FALSE, file = fileAddressDWQ, x = dwq, col.names = FALSE)
  return (TRUE)
}

fun.extract.rules = function(inputAddress, outputAddress, minconf, minsup, window) {
  raw <- read.table(inputAddress, sep = ",")
  transpose_raw <- t(raw)[1, ]
  pw <- pwMatrix(transpose_raw, sep = " ")
  tt <- ttMatrix(pw$rownames)
  distr <- (tt$M*1) %*% (pw$M*1)
  distr_ngCMat = as (distr, "ngCMatrix")
  ttRows <- tt$rownames
  td <- as.data.frame(ttRows)
  
  setnames(td, "ttRows", "labels")
  trans1 <- new("transactions", data = distr_ngCMat, itemInfo = td)
  
  rules <- apriori(trans1, parameter = list(support = 100/trans1@data@Dim[2],
                                            confidence = 0.5, maxlen = window, maxtime = 15))
  rules_d <- inspect(head(sort(rules, by = "support"), 100))
  
  write.csv(x = rules_d, file = outputAddress)
  return (rules)
}

################################ReadFiles##################################
prominence_questions <- read.table("../sequences/prominence-nf-Q.txt",
                                   header = FALSE, sep = " ", col.names = paste0("V",seq_len(5)),
                                   fill = TRUE)
prominence_all <- read.table("../sequences/prominence-nf-all.txt",
                             header = FALSE, sep = " ", col.names = paste0("V",seq_len(20)),
                             fill = TRUE)
prominence_answers <- read.table("../sequences/prominence-nf-A.txt",
                                 header = FALSE, sep = " ", col.names = paste0("V",seq_len(15)),
                                 fill = TRUE)

scale_questions <- read.table("../sequences/scale-nf-Q.txt",
                              header = FALSE, sep = " ", col.names = paste0("V",seq_len(4)),
                              fill = TRUE)
scale_all <- read.table("../sequences/scale-nf-all.txt",
                        header = FALSE, sep = " ", col.names = paste0("V",seq_len(20)),
                        fill = TRUE)
scale_answers <- read.table("../sequences/scale-nf-A.txt",
                            header = FALSE, sep = " ", col.names = paste0("V",seq_len(15)),
                            fill = TRUE)

type_questions <- read.table("../sequences/type-nf-Q.txt",
                             header = FALSE, sep = " ", col.names = paste0("V",seq_len(5)),
                             fill = TRUE)
type_all <- read.table("../sequences/type-nf-all.txt",
                       header = FALSE, sep = " ", col.names = paste0("V",seq_len(20)),
                       fill = TRUE)
type_answers <- read.table("../sequences/type-nf-A.txt",
                           header = FALSE, sep = " ", col.names = paste0("V",seq_len(15)),
                           fill = TRUE)


################################Differentiating SWQ and DWQ##################################
fun.write.simple.complex(all= prominence_all, questions = prominence_questions,
                         fileAddressSWQ = "../sequences/prominence-nf-all-SWQ.txt",
                         fileAddressDWQ = "../sequences/prominence-nf-all-DWQ.txt") 
fun.write.simple.complex(all= scale_all, questions = scale_questions,
                         fileAddressSWQ = "../sequences/scale-nf-all-SWQ.txt",
                         fileAddressDWQ = "../sequences/scale-nf-all-DWQ.txt") 
fun.write.simple.complex(all= type_all, questions = type_questions,
                         fileAddressSWQ = "../sequences/type-nf-all-SWQ.txt",
                         fileAddressDWQ = "../sequences/type-nf-all-DWQ.txt") 
fun.write.simple.complex(all= type_questions, questions = type_questions,
                         fileAddressSWQ = "../sequences/type-nf-Q-SWQ.txt",
                         fileAddressDWQ = "../sequences/type-nf-Q-DWQ.txt") 
fun.write.simple.complex(all= type_answers, questions = type_questions,
                         fileAddressSWQ = "../sequences/type-nf-A-SWQ.txt",
                         fileAddressDWQ = "../sequences/type-nf-A-DWQ.txt")

#############################################################################################

scale_rules_swq = fun.extract.rules (inputAddress = "../sequences/scale-nf-all-SWQ.txt",
                                     outputAddress = "result/scale_swq_rules_qa.csv",
                                     minsup = 0.0034, minconf = 0.5, window = 3)
scale_rules_dwq = fun.extract.rules (inputAddress = "../sequences/scale-nf-all-DWQ.txt",
                                     outputAddress = "result/scale_dwq_rules_qa.csv",
                                     minsup = 0.0034, minconf = 0.5, window = 3)
scale_rules_all = fun.extract.rules (inputAddress = "../sequences/scale-nf-all.txt",
                                     outputAddress = "result/scale_all_rules_qa.csv",
                                     minsup = 0.0034, minconf = 0.5, window = 3)

prom_rules_swq = fun.extract.rules (inputAddress = "../sequences/prominence-nf-all-SWQ.txt",
                                    outputAddress = "result/prominence_swq_rules_qa.csv",
                                    minsup = 0.0034, minconf = 0.5, window = 3)
prom_rules_dwq = fun.extract.rules (inputAddress = "../sequences/prominence-nf-all-DWQ.txt",
                                    outputAddress = "result/prominence_dwq_rules_qa.csv",
                                    minsup = 0.0034, minconf = 0.5, window = 3)

type_rules_swq = fun.extract.rules (inputAddress = "../sequences/type-nf-all-SWQ.txt",
                                    outputAddress = "result/type_swq_rules_qa.csv",
                                    minsup = 0.0034, minconf = 0.5, window = 3)
type_rules_dwq = fun.extract.rules (inputAddress = "../sequences/type-nf-all-DWQ.txt",
                                    outputAddress = "result/type_dwq_rules_qa.csv",
                                    minsup = 0.0034, minconf = 0.5, window = 3)


t_p = ggplot(type_rules@quality, aes(x=support, y=confidence)) +
  geom_point(aes(colour= lift)) +
  scale_color_gradient(low = "#ffffff", high = "#000000",  limits=c(0.6, 2.2)) +
  theme_bw() + theme(legend.position = "top", legend.box = "horizontal") +
  coord_cartesian(xlim =c(0.0, 1.0), ylim = c(0.5, 1.0))
ts_p = ggplot(type_rules_swq@quality, aes(x=support, y=confidence)) +
  geom_point(aes(colour= lift)) +
  scale_color_gradient(low = "#ffffff", high = "#000000",  limits=c(0.6, 2.2)) +
  theme_bw() + theme(legend.position = "none") +
  coord_cartesian(xlim =c(0.0, 1.0), ylim = c(0.5, 1.0))
td_p = ggplot(type_rules_dwq@quality, aes(x=support, y=confidence)) +
  geom_point(aes(colour= lift)) +
  scale_color_gradient(low = "#ffffff", high = "#000000",  limits=c(0.6, 2.2)) +
  theme_bw() + theme(legend.position = "none") +
  coord_cartesian(xlim =c(0.0, 1.0), ylim = c(0.5, 1.0))

s_p = ggplot(scale_rules@quality, aes(x=support, y=confidence)) +
  geom_point(aes(colour= lift)) +
  scale_color_gradient(low = "#ffffff", high = "#000000",  limits=c(0.6, 2.2)) +
  theme_bw() + theme(legend.position = "none") +
  coord_cartesian(xlim =c(0.0, 1.0), ylim = c(0.5, 1.0))
ss_p = ggplot(scale_rules_swq@quality, aes(x=support, y=confidence)) +
  geom_point(aes(colour= lift)) +
  scale_color_gradient(low = "#ffffff", high = "#000000",  limits=c(0.6, 2.2)) +
  theme_bw() + theme(legend.position = "none") +
  coord_cartesian(xlim =c(0.0, 1.0), ylim = c(0.5, 1.0))
sd_p = ggplot(scale_rules_dwq@quality, aes(x=support, y=confidence)) +
  geom_point(aes(colour= lift)) +
  scale_color_gradient(low = "#ffffff", high = "#000000",  limits=c(0.6, 2.2)) +
  theme_bw() +theme(legend.position = "none") +
  coord_cartesian(xlim =c(0.0, 1.0), ylim = c(0.5, 1.0))

p_p = ggplot(imp_rules@quality, aes(x=support, y=confidence)) +
  geom_point(aes(colour= lift)) +
  scale_color_gradient(low = "#ffffff", high = "#000000",  limits=c(0.6, 2.2)) +
  theme_bw() + theme(legend.position = "none") +
  coord_cartesian(xlim =c(0.0, 1.0), ylim = c(0.5, 1.0))
ps_p = ggplot(prom_rules_swq@quality, aes(x=support, y=confidence)) +
  geom_point(aes(colour= lift)) +
  scale_color_gradient(low = "#ffffff", high = "#000000",  limits=c(0.6, 2.2)) +
  theme_bw() + theme(legend.position = "none") +
  coord_cartesian(xlim =c(0.0, 1.0), ylim = c(0.5, 1.0))
pd_p = ggplot(prom_rules_dwq@quality, aes(x=support, y=confidence)) +
  geom_point(aes(colour= lift)) +
  scale_color_gradient(low = "#ffffff", high = "#000000",  limits=c(0.6, 2.2)) +
  theme_bw() + theme(legend.position = "none") +
  coord_cartesian(xlim =c(0.0, 1.0), ylim = c(0.5, 1.0))

grid.arrange(t_p, ts_p, td_p, s_p, ss_p, sd_p, p_p, ps_p, pd_p, ncol=3, nrow = 3, layout_matrix = rbind(c(1,2,3), c(4,5,6), c(7,8,9)))
