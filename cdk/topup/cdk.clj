(ns topup.cdk
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [stedi.cdk.alpha :as cdk]
            [uberdeps.api :as uberdeps]))

(cdk/import [[App Construct Duration Stack] :from "@aws-cdk/core"]
            [[Bucket] :from "@aws-cdk/aws-s3"]
            [[Code Function Runtime Tracing] :from "@aws-cdk/aws-lambda"])

(defn- clean
  []
  (let [f (io/file "classes")]
    (when (.exists f)
      (->> f
           (file-seq)
           (reverse)
           (map io/delete-file)
           (dorun)))))

(def code
  (let [jarpath "target/app.jar"
        deps    (edn/read-string (slurp "deps.edn"))]
    (with-out-str
      (clean)
      (io/make-parents "classes/.")
      (io/make-parents jarpath)
      (compile 'topup.client)
      (compile 'topup.lambda)
      (uberdeps/package deps jarpath {:aliases [:classes]}))
    (Code/fromAsset jarpath)))

(def app (App))

(def stack (Stack app "send-topup-lambda"))

(def bucket (Bucket stack "send-topup-lambda-bucket"))

(def my-fn
  (Function stack
            "send-topup-fn"
            {:code        code        ;; Calling a static method
             :handler     "com.busqandote.topup.SendTopup"
             :runtime     (:JAVA_8 Runtime)               ;; Getting a static property
             :environment {"BUCKET" (:bucketName bucket)} ;; Getting an instance property
             :memorySize 512
             :timeout (Duration/seconds 30)
             }))

;; We can grant the function write access to the bucket using an
;; instance method

(Bucket/grantWrite bucket my-fn)

