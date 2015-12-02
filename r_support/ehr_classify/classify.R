library('randomForest')

base_path <- "C:\\Users\\phd2\\PycharmProjects\\medical_nn\\"

x <- data.frame(read.table(paste(base_path,"m.txt",sep="")))
ys <- read.table(paste(base_path,"l.txt",sep=""))

for(label_i in 1:dim(y)[2]){
  y = ys[,label_i]
  Grid <-  expand.grid(
    n.trees = c(250),
    interaction.depth = c(22) ,
    shrinkage = 0.2)
  
  # Define the parameters for cross validation
  fitControl <- trainControl(method = "none", classProbs = TRUE)
  GBMmodel <- train(Cover_Type ~ .,
                    x = train,
                    method = "gbm",
                    trControl = fitControl,
                    verbose = TRUE,
                    tuneGrid = Grid,
                    ## Specify which metric to optimize
                    ## We will optimize AUC of ROC curve as it best encapsulates the predictive power of a model                 
                    metric = "ROC")
} 


print("Done")