(defproject riemann-mbeans "0.2.0-SNAPSHOT"
  :description "riemann plugin to collect internal jvm statistics"
  :url "http://github.com/ccin2p3/riemann-mbeans"
  :license {:name "CeCILL-C"
            :url "http://www.cecill.info/licences/Licence_CeCILL-C_V1-en.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [riemann             "0.2.10"]
                 [org.clojure/java.jmx "0.3.1"]])
