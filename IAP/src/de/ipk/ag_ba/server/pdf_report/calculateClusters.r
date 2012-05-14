##################################
# Author: C. Klukas
# May 2012
##################################

library(pvclust)

mydata <- read.csv('report.clustering.csv', sep=';', row.names="UniID")

result <- pvclust(data.frame(mydata[2:length(mydata)]), nboot=100)

pdf("clusters.pdf")

plot(result)

pvrect(result, alpha=0.95)

dev.off()
