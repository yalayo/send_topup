(ns topup.client
  (:gen-class)
  (:require [clojure.spec.alpha :as s]
            [clojure.data.json :as json]
            [org.httpkit.client :as client]))

(s/def ::query string?)
(s/def ::request (s/keys :req [::query]))
(s/def ::result (s/coll-of string? :gen-max 3))
(s/def ::error int?)
(s/def ::response (s/or :ok (s/keys :req [::result])
                        :err (s/keys :req [::error])))

(s/fdef invoke-service
  :args (s/cat :service any? :request ::request)
  :ret ::response)

(defn invoke-service [service request]
  )

(s/fdef run-query
  :args (s/cat :service any? :query string?)
  :ret (s/or :ok ::result :err ::error))

(defn run-query [service query]
  (let [{::keys [result error]} (invoke-service service {::query query})]
    (or result error)))
