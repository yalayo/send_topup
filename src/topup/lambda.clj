(ns topup.lambda
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [uswitch.lambada.core :refer [deflambdafn]]
            [topup.client :as c]))

(deflambdafn com.busqandote.topup.SendTopup
  [in out ctx]
  (let [in (json/read (io/reader in) :key-fn keyword)
        response (c/send-topup (get in :phone-number))]
    (with-open [w (io/writer out)]
      (json/write response w))))
