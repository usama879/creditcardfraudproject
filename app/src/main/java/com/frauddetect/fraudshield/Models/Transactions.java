package com.frauddetect.fraudshield.Models;

public class Transactions {

    public String tid;
    public String amt;
    public String age;
    public String unix_time;
    public String city_pop;
    public String cc_num;
    public String lat;
    public String lng;
    public String category_shopping_pos;
    public String category_misc_pos;
    public String category_misc_net;
    public String category_shopping_net;
    public String category_food_dining;
    public String category_kids_pets;
    public String category_home;
    public String category_personal_care;
    public String amount;
    public String city;
    public String population;
    public String date;
    public String time;
    public String user_id;
    public String status;

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getUnix_time() {
        return unix_time;
    }

    public void setUnix_time(String unix_time) {
        this.unix_time = unix_time;
    }

    public String getCity_pop() {
        return city_pop;
    }

    public void setCity_pop(String city_pop) {
        this.city_pop = city_pop;
    }

    public String getCc_num() {
        return cc_num;
    }

    public void setCc_num(String cc_num) {
        this.cc_num = cc_num;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getCategory_shopping_pos() {
        return category_shopping_pos;
    }

    public void setCategory_shopping_pos(String category_shopping_pos) {
        this.category_shopping_pos = category_shopping_pos;
    }

    public String getCategory_misc_pos() {
        return category_misc_pos;
    }

    public void setCategory_misc_pos(String category_misc_pos) {
        this.category_misc_pos = category_misc_pos;
    }

    public String getCategory_misc_net() {
        return category_misc_net;
    }

    public void setCategory_misc_net(String category_misc_net) {
        this.category_misc_net = category_misc_net;
    }

    public String getCategory_shopping_net() {
        return category_shopping_net;
    }

    public void setCategory_shopping_net(String category_shopping_net) {
        this.category_shopping_net = category_shopping_net;
    }

    public String getCategory_food_dining() {
        return category_food_dining;
    }

    public void setCategory_food_dining(String category_food_dining) {
        this.category_food_dining = category_food_dining;
    }

    public String getCategory_kids_pets() {
        return category_kids_pets;
    }

    public void setCategory_kids_pets(String category_kids_pets) {
        this.category_kids_pets = category_kids_pets;
    }

    public String getCategory_home() {
        return category_home;
    }

    public void setCategory_home(String category_home) {
        this.category_home = category_home;
    }

    public String getCategory_personal_care() {
        return category_personal_care;
    }

    public void setCategory_personal_care(String category_personal_care) {
        this.category_personal_care = category_personal_care;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPopulation() {
        return population;
    }

    public void setPopulation(String population) {
        this.population = population;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Transactions(String tid, String amt, String age, String unix_time, String city_pop, String cc_num, String lat, String lng, String category_shopping_pos, String category_misc_pos, String category_misc_net, String category_shopping_net, String category_food_dining, String category_kids_pets, String category_home, String category_personal_care, String amount, String city, String population, String date, String time, String user_id, String status) {
        this.tid = tid;
        this.amt = amt;
        this.age = age;
        this.unix_time = unix_time;
        this.city_pop = city_pop;
        this.cc_num = cc_num;
        this.lat = lat;
        this.lng = lng;
        this.category_shopping_pos = category_shopping_pos;
        this.category_misc_pos = category_misc_pos;
        this.category_misc_net = category_misc_net;
        this.category_shopping_net = category_shopping_net;
        this.category_food_dining = category_food_dining;
        this.category_kids_pets = category_kids_pets;
        this.category_home = category_home;
        this.category_personal_care = category_personal_care;
        this.amount = amount;
        this.city = city;
        this.population = population;
        this.date = date;
        this.time = time;
        this.user_id = user_id;
        this.status = status;
    }

    public Transactions() {
    }
}
