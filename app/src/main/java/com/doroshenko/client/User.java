package com.doroshenko.client;

public class User {
    private static String phone;
    private static String password;
    private static byte role;

    public static String getPhone() {
        return phone;
    }

    public static void setPhone(String phone) {
        User.phone = phone;
    }

    public static String getPassword() {
        return password;
    }

    public static void setPassword(String password) {
        User.password = password;
    }

    public static byte getRole() {
        return role;
    }

    public static void setRole(byte role) {
        User.role = role;
    }
}
