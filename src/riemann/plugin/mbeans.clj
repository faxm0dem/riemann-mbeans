(ns riemann.plugin.mbeans
  "A riemann plugin to collect internal java jmx counters"
  (:use [clojure.tools.logging :only (info error debug warn)]
        [riemann.common :only [localhost event]])
  (:require [riemann.service :as service]
            [riemann.core :as core]
            [riemann.config]
            [clojure.java.jmx :as jmx]))

(defn instrumentation-service
  "Returns a service which samples jmx every
  interval seconds, and sends events to the core."
  [opts]
  (let [interval (long (* 1000 (get opts :interval 10)))
        service-name (get opts :service-name "jmx.memory.heap.used")
        jmx-read (get opts :jmx-read ["java.lang:type=Memory" :HeapMemoryUsage])
        jmx-func (get opts :jmx-func #(:used %))
        enabled? (get opts :enabled? true)]
    (service/thread-service
      ::jmx-instrumentation [interval service-name enabled?]
      (fn measure [core]
        (Thread/sleep interval)

        (try
          ; Take events from core and instrumented services
          (let [base (event {:host (localhost)
                             ; Default TTL of 2 intervals, and convert ms to s.
                             :ttl  (long (/ interval 500))})
                ;events [{:service service-name :metric (:used (jmx/read "java.lang:type=Memory" :HeapMemoryUsage))}]]
                events [{:service service-name :metric (jmx-func (apply jmx/read jmx-read))}]]
            (if enabled?
              ; Stream each event through this core
              (doseq [event events]
                (core/stream! core (merge base event)))
              ; Ensure we consume all events, to avoid overflowing stats
              (dorun events)))

          (catch Exception e
            (warn e "jmx instrumentation service caught")))))))

(defn periodical
    "Periodically collect jmx stats."
    ([]
        (periodical 10))
    ([& args]
        (riemann.config/service! (apply instrumentation-service args))))

(defn instrumentation
  "adds a jmx instrumentation service to core"
  [& opts]
  (let [service (apply instrumentation-service opts)]
    (swap! riemann.config/next-core core/conj-service service :force)))

