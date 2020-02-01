(ns topup.lambda
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [uswitch.lambada.core :refer [deflambdafn]]
            [topup.client :as c]))

(deflambdafn com.busqandote.topup.SendTopup
  [in out ctx]
  (let [in (json/read-str (io/reader in) :key-fn keyword)
        response (c/send-topup (get in :phone-number))]
    (with-open [w (io/writer out)]
      (json/write response w))))

(let [in (json/read-str "{\"phone-number\":1,\"b\":2}" :key-fn keyword)]
  (print (get in :phone-number)))

