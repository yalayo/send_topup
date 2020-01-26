(ns topup.client-test
  (:require [topup.client :as c]
            [clojure.spec.test.alpha :as stest]
            [org.httpkit.client :as client]))

(stest/instrument `c/invoke-service {:stub #{`c/invoke-service}})

(c/invoke-service nil {::query "test"})

(stest/summarize-results (stest/check `c/run-query))
