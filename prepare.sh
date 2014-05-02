git clone https://github.com/elbiczel/MSRwithSpark.git
cd MSRwithSpark

./sbt/sbt assembly
mkdir ~/jars
mv target/scala-2.10/loc-assembly-1.0.jar ~/jars/loc.jar
~/spark-ec2/copy-dir ~/jars

./repos.sh
mv volley ~/
mv spring-framework ~/
mv jenkins ~/
mv httpd ~/

~/spark-ec2/copy-dir ~/volley
~/spark-ec2/copy-dir ~/spring-framework
~/spark-ec2/copy-dir ~/jenkins
~/spark-ec2/copy-dir ~/httpd

cd ~/

source ./spark/conf/spark-env.sh
export ADD_JARS=/root/jars/loc.jar

./spark/bin/spark-shell

# val spark = new SparkImpl(sc, (commitsCount) => commitsCount / 2)
# val spark = new SparkImpl(sc, (commitsCount) => 60) // K * N nodes * 4 processors + CONSTANT
# val commitStats = spark("/root/volley")
# val commitStats = spark("/root/spring-framework")
# val commitStats = spark("/root/jenkins")
# val commitStats = spark("/root/httpd")

ssh -i ~/biczel_mbp.pem root@ec2-54-86-79-156.compute-1.amazonaws.com
time java -Xmx10G -jar /root/jars/loc.jar /root/volley
time java -Xmx10G -jar /root/jars/loc.jar -naive /root/volley

ssh -i ~/biczel_mbp.pem root@ec2-54-86-85-125.compute-1.amazonaws.com
time java -Xmx10G -jar /root/jars/loc.jar /root/spring-framework
time java -Xmx10G -jar /root/jars/loc.jar -naive /root/spring-framework

ssh -i ~/biczel_mbp.pem root@ec2-54-86-90-213.compute-1.amazonaws.com
time java -Xmx10G -jar /root/jars/loc.jar /root/jenkins
time java -Xmx10G -jar /root/jars/loc.jar -naive /root/jenkins

ssh -i ~/biczel_mbp.pem root@ec2-54-86-139-71.compute-1.amazonaws.com
time java -Xmx10G -jar /root/jars/loc.jar /root/httpd
time java -Xmx10G -jar /root/jars/loc.jar -naive /root/httpd

git clone https://github.com/torvalds/linux.git
~/spark-ec2/copy-dir ~/linux
# val commitStats = spark("/root/linux")
time java -Xmx10G -jar /root/jars/loc.jar /root/linux

