tags=read.csv('tags.csv')
cooccur=t(as.matrix(tags)) %*% as.matrix(tags)
t1=data.frame(which(cooccur>=200, arr.ind=TRUE))
tagNames=names(tags)
indTagName=c()
indTagRatio=c()
for(tag in unique(t1[t1$row==t1$col,'col'])){
    total=0
    tagName=tagNames[tag]
    subset=tags[tags[,tagName]==1,]
    for(i in unique(t1[t1$row==t1$col,'col'])){
        if(i!=tag){
            otherTagName=tagNames[i]
            subset=subset[subset[,otherTagName]==0,]
        }
    }
    total=nrow(subset)
    ratio=total/cooccur[tag,tag]
    indTagName=c(indTagName, tagName)
    indTagRatio=c(indTagRatio, ratio)
}
indTags=data.frame(indTagName, indTagRatio)

