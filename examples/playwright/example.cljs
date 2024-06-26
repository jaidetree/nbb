(ns example
  {:clj-kondo/config '{:lint-as {promesa.core/let clojure.core/let
                                 example/defp clojure.core/def}}}
  (:require
   ["playwright$default" :as pw]
   [clojure.string :as str]
   [clojure.test :as t :refer [async deftest is]]
   [nbb.core :refer [*file*]]
   [promesa.core :as p]))

(def browser-type pw/firefox) ;; or pw/chromium

(def continue (atom nil))
(defn pause []
  (js/Promise. (fn [resolve]
                 (reset! continue resolve))))

(def file *file*)
(defmacro print-error [err]
  (let [{:keys [line column]} (meta &form)]
    `(js/console.log (str file ":" ~line ":" ~column " - " (ex-message ~err)))))

(deftest browser-test
  (async
   done
   (p/let [browser (.launch browser-type #js {:headless false})]
     (-> (p/let [context (.newContext browser)
                 page (.newPage context)
                 _ (.goto page "https://clojure.org")
                 _ (-> (.screenshot page #js{:path "screenshot.png"})
                       (.catch #(js/console.log %)))
                 content (.content page)
                 ;; uncomment to save content to variable for inspection
                 ;; _ (def c content)
                 ;; uncomment to pause execution to inspect state in browser
                 ;; _ (pause)
                 ]
           (is (str/includes? content "clojure")))
         (p/catch
             (fn [err]
               (print-error err)
               (is false)))
         (p/finally
           (fn []
             (.close browser)
             (done)))))))

(t/run-tests 'example)

;;;; Scratch

(comment

  (browser-test)
  ;; evaluate to continue after pause
  (@continue)
  ;; inspect content captured during test
  ;; (subs c 0 10)

  (defmacro defp
    "Define var when promise is resolved"
    [binding expr]
    `(-> ~expr (.then (fn [val]
                        (def ~binding val)))))

  (defp browser (.launch browser-type #js {:headless false}))
  (defp context (.newContext browser))
  (defp page (.newPage context))
  (.goto page "https://clojure.org")
  (.goto page "https://github.com")
  (.close browser)

  )
