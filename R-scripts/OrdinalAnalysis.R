############################################################
############################################################
######################ORDINAL ANLAYSIS######################
############################################################
############################################################

####################################################
#Installing the packages
#install.packages("plyr")
####################################################
#set workspace to this folder
setwd("D:/Work/IJGIS/R-scripts")
####################################################
library(plyr)
###########################PREPROCESSING#########################
fun.to.int <- function(file_address, result_address) {
  raw <- read.table(file = file_address, sep = ",")
  processed = data.frame(V1 = c(raw))
  processed$woQ <- gsub("Q-", "", raw$V1)
  processed$woQA <- gsub("A-", "", processed$woQ)
  write.table(processed$woQA, file=result_address,
              quote = F, sep = " ", row.names = F, col.names = F)
}
fun.to.int("../sequences/prominence-nf-Q.txt", 
           "../sequences/prominence-nf-Q-int.txt")
fun.to.int("../sequences/prominence-nf-A.txt", 
           "../sequences/prominence-nf-A-int.txt")
fun.to.int("../sequences/scale-nf-Q.txt", 
           "../sequences/scale-nf-Q-int.txt")
fun.to.int("../sequences/scale-nf-A.txt", 
           "../sequences/scale-nf-A-int.txt")
###########################PROMINENNCE#########################

all_questions <- read.table("../sequences/prominence-nf-Q-int.txt",
                            header = FALSE, sep = " ",
                            col.names = paste0("V",seq_len(4)), fill = TRUE)
head(all_questions, 5)
all_answers <- read.table("../sequences/prominence-nf-A-int.txt",
                          header = FALSE, sep = " ",
                          col.names = paste0("V",seq_len(13)), fill = TRUE)
head(all_answers, 5)

point_matrix = matrix(0, nrow = 4, ncol = 3)
range_matrix = matrix(0, nrow = 4, ncol = 3)

fun.rel1 = function (qVal, aVal) {
  if (qVal == aVal)
    return (1)
  else if (qVal > aVal) 
    return (0)
  else
    return (2)
}

fun.relv1 = function (vals) {
  qVal = vals[1]
  aVal = vals[2]
  if (qVal == aVal)
    return (1)
  else if (qVal > aVal) 
    return (0)
  else
    return (2)
}


fun.rel2 = function (qVal_min, qVal_max, aVal) {
  if (qVal_min > aVal)
    return (0)
  else if (qVal_max < aVal) 
    return (2)
  else
    return (1)
}

fun.relv2 = function (vals) {
  qVal_min = vals[1]
  qVal_max = vals[2]
  aVal = vals[3]
  if (qVal_min > aVal)
    return (0)
  else if (qVal_max < aVal) 
    return (2)
  else
    return (1)
}

for (i in 1:length(all_questions$V1)) {
  id = all_questions[i, 1]
  
  num_vec = as.numeric(all_questions[i,2:4])
  num_vec = num_vec[!is.na(num_vec)]
  
  ans_vec = all_answers[all_answers$V1 == id, 2:13]
  ans_vec = ans_vec[!is.na(ans_vec)]
  
  if (length(num_vec) > 0 && length(ans_vec) > 0 ) {
  min_val = min(num_vec)
  max_val = max(num_vec)
  
  
  min_ans = min(ans_vec)
  max_ans = max(ans_vec)
  median_ans = median(ans_vec)
  
  #if (min_val == max_val) { #point-based
  if (length(num_vec) == 1) { #SWQ
    
    res = colwise(fun.relv1)(rbind(
      as.data.frame(
        matrix(data = min_val, nrow = 1, ncol = length(ans_vec))), c(ans_vec)))
    point_matrix[1,1] = point_matrix[1,1] + length(which(res == 0))
    point_matrix[1,2] = point_matrix[1,2] + length(which(res == 1))
    point_matrix[1,3] = point_matrix[1,3] + length(which(res == 2))
    
    min_rel = fun.rel1(min_val, min_ans)
    point_matrix[2, min_rel+1] = point_matrix[2, min_rel+1] + 1
    
    median_rel = fun.rel1(min_val, median_ans)
    point_matrix[3, median_rel+1] = point_matrix[3, median_rel+1] + 1
    
    max_rel = fun.rel1(min_val, max_ans)
    point_matrix[4, max_rel+1] = point_matrix[4, max_rel+1] + 1
    
  } else {#DWQ #range-based
    res = colwise(fun.relv2)(rbind(as.data.frame(
      matrix(data = min_val, nrow = 1, ncol = length(ans_vec))), 
      as.data.frame(matrix(data = max_val, nrow = 1, ncol = length(ans_vec))), c(ans_vec)))
    range_matrix[1,1] = range_matrix[1,1] + length(which(res == 0))
    range_matrix[1,2] = range_matrix[1,2] + length(which(res == 1))
    range_matrix[1,3] = range_matrix[1,3] + length(which(res == 2))
    
    min_rel = fun.rel2(min_val, max_val, min_ans)
    range_matrix[2, min_rel+1] = range_matrix[2, min_rel+1] + 1
    
    median_rel = fun.rel2(min_val, max_val, median_ans)
    range_matrix[3, median_rel+1] = range_matrix[3, median_rel+1] + 1
    
    max_rel = fun.rel2(min_val, max_val, max_ans)
    range_matrix[4, max_rel+1] = range_matrix[4, max_rel+1] + 1
  }
  }
}

df_range = as.data.frame(range_matrix)
colnames(df_range) = c("lower than", "between-equal", "greater than")
rownames(df_range) = c("each_value", "min_value", "median_value", "max_value")
df_range # prominence relation between detailed where questions and their answers 

df_point = as.data.frame(point_matrix)
colnames(df_point) = c("lower than", "equal", "greater than")
rownames(df_point) = c("each_value", "min_value", "median_value", "max_value")
df_point # prominence relation between simple where questions and their answers 

write.csv(df_range, "result/dwq_prominence.csv")
write.csv(df_point, "result/swq_prominence.csv")

###########################SCALE#########################

all_questions <- read.table("../sequences/scale-nf-Q-int.txt",
                            header = FALSE, sep = " ",
                            col.names = paste0("V",seq_len(4)), fill = TRUE)
head(all_questions, 5)
all_answers <- read.table("../sequences/scale-nf-A-int.txt",
                          header = FALSE, sep = " ",
                          col.names = paste0("V",seq_len(13)), fill = TRUE)
head(all_answers, 5)

point_matrix = matrix(0, nrow = 4, ncol = 3)
range_matrix = matrix(0, nrow = 4, ncol = 3)


for (i in 1:length(all_questions$V1)) {
  id = all_questions[i, 1]
  
  num_vec = as.numeric(all_questions[i,2:4])
  num_vec = num_vec[!is.na(num_vec)]
  
  ans_vec = all_answers[all_answers$V1 == id, 2:13]
  ans_vec = ans_vec[!is.na(ans_vec)]
  
  if (length(num_vec) > 0 && length(ans_vec) > 0) {
  
  min_val = min(num_vec)
  max_val = max(num_vec)
  
  
  
  min_ans = min(ans_vec)
  max_ans = max(ans_vec)
  median_ans = median(ans_vec)
  
  #if (min_val == max_val) {#point-based
  if (length(num_vec) == 1) {#SWQ
    res = colwise(fun.relv1)(rbind(
      as.data.frame(
        matrix(data = min_val, nrow = 1, ncol = length(ans_vec))), c(ans_vec)))
    point_matrix[1,1] = point_matrix[1,1] + length(which(res == 0))
    point_matrix[1,2] = point_matrix[1,2] + length(which(res == 1))
    point_matrix[1,3] = point_matrix[1,3] + length(which(res == 2))
    
    min_rel = fun.rel1(min_val, min_ans)
    point_matrix[2, min_rel+1] = point_matrix[2, min_rel+1] + 1
    
    median_rel = fun.rel1(min_val, median_ans)
    point_matrix[3, median_rel+1] = point_matrix[3, median_rel+1] + 1
    
    max_rel = fun.rel1(min_val, max_ans)
    point_matrix[4, max_rel+1] = point_matrix[4, max_rel+1] + 1
    
  } else {#DWQ #range-based
    res = colwise(fun.relv2)(rbind(
      as.data.frame(
        matrix(data = min_val, nrow = 1, ncol = length(ans_vec))), 
      as.data.frame(matrix(data = max_val, nrow = 1, ncol = length(ans_vec))), c(ans_vec)))
    range_matrix[1,1] = range_matrix[1,1] + length(which(res == 0))
    range_matrix[1,2] = range_matrix[1,2] + length(which(res == 1))
    range_matrix[1,3] = range_matrix[1,3] + length(which(res == 2))
    
    min_rel = fun.rel2(min_val, max_val, min_ans)
    range_matrix[2, min_rel+1] = range_matrix[2, min_rel+1] + 1
    
    median_rel = fun.rel2(min_val, max_val, median_ans)
    range_matrix[3, median_rel+1] = range_matrix[3, median_rel+1] + 1
    
    max_rel = fun.rel2(min_val, max_val, max_ans)
    range_matrix[4, max_rel+1] = range_matrix[4, max_rel+1] + 1
  }
  }
}

df_range = as.data.frame(range_matrix)
colnames(df_range) = c("lower than", "between-equal", "greater than")
rownames(df_range) = c("each_value", "min_value", "median_value", "max_value")
df_range # scale relation between detailed where questions and their answers 

df_point = as.data.frame(point_matrix)
colnames(df_point) = c("lower than", "equal", "greater than")
rownames(df_point) = c("each_value", "min_value", "median_value", "max_value")
df_point # scale relation between simple where questions and their answers 

write.csv(df_range, "result/dwq_scale.csv")
write.csv(df_point, "result/swq_scale.csv")

