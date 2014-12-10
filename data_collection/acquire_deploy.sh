wget http://cs109-team-proj.s3-website-us-west-2.amazonaws.com/progs/collector.zip -O collector.zip; 
unzip collector.zip; 
rm collector.zip;
cd collector;
java -jar DataCollector.jar "AWS 1"&