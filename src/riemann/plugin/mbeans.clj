(ns riemann.plugin.mbeans
  "A riemann plugin to collect internal java jmx counters"
  (:use [clojure.tools.logging :only (info error debug warn)]
        [riemann.common :only [localhost event]])
  (:require [riemann.service :as service]
            [riemann.core :as core]
            [riemann.config]
            [clojure.java.jmx :as jmx]))

(defn eat-bean
  "takes a map describing mbean. returns a map containing the corresponding metric in the :metric value and the name in the :service value"
  [{:keys [mbean property attribute service]}]
  (let [service (or service (str mbean property attribute))]
    (if attribute
      {:service service :metric (attribute (jmx/read mbean property))}
      {:service service :metric (jmx/read mbean property)})))

(defn eat-beans
  "takes a sequence of maps describing mbeans. returns a sequence of maps containing the corresponding metrics in the :metric value"
  [mbeans]
  (map eat-bean mbeans))

(defn instrumentation-service
  "Returns a service which samples jmx every
  interval seconds, and sends events to the core."
  ([] (instrumentation-service {}))
  (
  [opts]
  (let [interval (long (* 1000 (get opts :interval 10)))
        mbeans (get opts :mbeans [{:mbean "java.lang:type=Memory" :property :HeapMemoryUsage :attribute :used}
                                  {:mbean "java.lang:type=Memory" :property :HeapMemoryUsage :attribute :committed}
                                  {:mbean "java.lang:type=Memory" :property :HeapMemoryUsage :attribute :init}
                                  {:mbean "java.lang:type=Memory" :property :HeapMemoryUsage :attribute :max}])
        enabled? (get opts :enabled? true)]
    (service/thread-service
      ::jmx-instrumentation [interval mbeans enabled?]
      (fn measure [core]
        (Thread/sleep interval)

        (try
          ; Take events from core and instrumented services
          (let [base (event {:host (localhost)
                             ; Default TTL of 2 intervals, and convert ms to s.
                             :ttl  (long (/ interval 500))})
                events (eat-beans mbeans)]
            (if enabled?
              ; Stream each event through this core
              (doseq [event events]
                (core/stream! core (merge base event)))
              ; Ensure we consume all events, to avoid overflowing stats
              (dorun events)))

          (catch Exception e
            (warn e "jmx instrumentation service caught")))))))
   )

(defn instrumentation
  "adds a jmx instrumentation service to core"
  [& opts]
  (let [service (apply instrumentation-service opts)]
    (swap! riemann.config/next-core core/conj-service service :force)))

