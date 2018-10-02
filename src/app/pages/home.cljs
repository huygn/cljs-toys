(ns app.pages.home
  (:require [rum.core :as rum]))

(rum/defc home-component []
  [:div#home.pt-8
   [:h1.mb-4 "Home"]
   [:nav
    [:a.block {:href "/todo"} "Todo"]
    [:a.block {:href "/form"} "Form"]]])
