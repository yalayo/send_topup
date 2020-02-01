(ns topup.client
  (:gen-class)
  (:require [clojure.spec.alpha :as s]
            [clojure.data.json :as json]
            [org.httpkit.client :as client]
            [clojure.spec.gen.alpha :as gen]))

(def phone-number-regex #"^[0-9]{10}$")

(def ten-digits (gen/fmap #(str %)
                              (gen/large-integer* {:min 1000000000 :max 9999999999})))


(s/def ::phone-number (s/with-gen (s/and string? #(re-matches phone-number-regex %))
               (constantly ten-digits)))

(s/def ::result (s/coll-of string? :gen-max 3))
(s/def ::error int?)
(s/def ::response (s/or :ok (s/keys :req [::result])
                        :err (s/keys :req [::error])))

(s/fdef consume-api
  :args (s/cat :phone-number ::phone-number)
  :ret ::response)

(defn consume-api [phone-number]
  @(client/post {:url "http://http-kit.org/"
               :method :get             ; :post :put :head or other
               :user-agent "User-Agent string"
               :oauth-token "your-token"
               :headers {"X-header" "value"
                         "X-Api-Version" "2"}
               :query-params {"q" "foo, bar"} ;"Nested" query parameters are also supported
               :form-params {"q" "foo, bar"} ; just like query-params, except sent in the body
                 :body (json/write-str {"key" "value"}) ; use this for content-type json
               :basic-auth ["user" "pass"]
               :keepalive 3000          ; Keep the TCP connection for 3000ms
               :timeout 1000      ; connection timeout and reading timeout 1000ms
               :filter (client/max-body-filter (* 1024 100)) ; reject if body is more than 100k
               :insecure? true ; Need to contact a server with an untrusted SSL cert?

               ;; File upload. :content can be a java.io.File, java.io.InputStream, String
               ;; It read the whole content before send them to server:
               ;; should be used when the file is small, say, a few megabytes
               :multipart [{:name "comment" :content "httpkit's project.clj"}
                           {:name "file" :content (clojure.java.io/file "project.clj") :filename "project.clj"}]

               :max-redirects 10 ; Max redirects to follow
                ;; whether follow 301/302 redirects automatically, default to true
                ;; :trace-redirects will contain the chain of the redirections followed.
               :follow-redirects false
               })
  )

(s/fdef send-topup
  :args (s/cat :phone-number ::phone-number)
  :ret (s/or :ok ::result :err ::error))

(defn send-topup [phone-number]
  (let [{::keys [result error]} (consume-api phone-number)]
    (or result error))) 
