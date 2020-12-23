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
all_answers <- read.table("../sequences/prominence-nf-A-int.txt", 
                          header = FALSE, sep = " ", 
                          col.names = paste0("V",seq_len(13)), fill = TRUE)

differential_mat = matrix(0, nrow = 19, ncol = 1) #prominence

for (i in 1:length(all_questions$V1)) {
  id = all_questions[i, 1]
  
  num_vec = as.numeric(all_questions[i,2:4])
  num_vec = num_vec[!is.na(num_vec)]
  
  min_val = min(num_vec)

  ans_vec = all_answers[all_answers$V1 == id, 2:13]
  ans_vec = ans_vec[!is.na(ans_vec)]
  diff_ans = ans_vec - min_val
  
  for (j in 1:length(diff_ans)) {
    differential_mat[diff_ans[j] + 10, 1] = differential_mat[diff_ans[j] + 10, 1] + 1
  }
}


df_differrential = as.data.frame(differential_mat)
colnames(df_differrential) = c("frequency")
rownames(df_differrential) = as.character(-9:9)
df_differrential

write.csv(df_differrential, "result/differential_prominence.csv") #prominence

###########################Scale#########################

all_questions <- read.table("../sequences/scale-nf-Q-int.txt",
                            header = FALSE, sep = " ",
                            col.names = paste0("V",seq_len(4)), fill = TRUE)
all_answers <- read.table("../sequences/scale-nf-A-int.txt",
                          header = FALSE, sep = " ",
                          col.names = paste0("V",seq_len(13)), fill = TRUE)


differential_mat = matrix(0, nrow = 21, ncol = 1) #scale

for (i in 1:length(all_questions$V1)) {
  id = all_questions[i, 1]
  
  num_vec = as.numeric(all_questions[i,2:4])
  num_vec = num_vec[!is.na(num_vec)]
  
  min_val = min(num_vec)
  
  ans_vec = all_answers[all_answers$V1 == id, 2:13]
  ans_vec = ans_vec[!is.na(ans_vec)]
  diff_ans = ans_vec - min_val
  
  for (j in 1:length(diff_ans)) {
    differential_mat[diff_ans[j] + 11, 1] = differential_mat[diff_ans[j] + 11, 1] + 1 #scale
  }
}




df_differrential = as.data.frame(differential_mat)
colnames(df_differrential) = c("frequency")
rownames(df_differrential) = as.character(-10:10)
df_differrential
write.csv(df_differrential, "result/differential_scale.csv") #scale


