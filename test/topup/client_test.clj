(ns topup.client-test
  (:require [topup.client :as c]
            [clojure.spec.test.alpha :as stest]
            [org.httpkit.client :as client]
            [clojure.spec.alpha :as s]))

(stest/instrument `c/consume-api {:stub #{`c/consume-api}})

(stest/summarize-results (stest/check `c/send-topup))
