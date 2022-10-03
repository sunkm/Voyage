package com.manchuan.tools.activity.touch.widget;

import android.text.TextUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TouchUtils {

	public static int a(Object obj, int i) {
        try {
            return Integer.parseInt(obj.toString().trim());
        } catch (Exception e) {
            return i;
        }
    }

    public static Object a(Map map, String... strArr) {
        int i = 0;
        while (i < strArr.length - 1) {
            try {
                Map map2 = map == null ? null : (Map) map.get(strArr[i]);
                i++;
                map = map2;
            } catch (Exception e) {
                return null;
            }
        }
        return map.get(strArr[strArr.length - 1]);
    }

    public static <T> T a(T... tArr) {
        for (int i = 0; i < tArr.length; i++) {
            if (tArr[i] != null) {
                return tArr[i];
            }
        }
        return null;
    }

    public static String a(long j) {
        if (j <= 0) {
            return "0";
        }
        String[] strArr = {"", "K", "M", "G", "T"};
        int log10 = (int) (Math.log10((double) j) / Math.log10(1024.0d));
        return new DecimalFormat("#,##0.#").format(((double) j) / Math.pow(1024.0d, (double) log10)) + " " + strArr[log10];
    }

    public static String a(Object obj, String str) {
        String b = b(obj);
        return b == null ? str : b.trim();
    }

    public static String a(String str) {
        return str == null ? "" : str;
    }

    public static String a(String str, String str2) {
        if (!a(str).contains(str2)) {
            return str;
        }
        return null;
    }

    public static String a(String str, String str2, String str3) {
        return d(str) ? str2 : d(str2) ? str : str + str3 + str2;
    }

    public static <T> String a(Map<String, T> map) {
        return a(map, ":", "\n");
    }

    public static <T> String a(Map<String, T> map, String str, String str2) {
        if (map == null) {
            return "";
        }
        ArrayList<String> arrayList = new ArrayList<>();
        for (Map.Entry next : map.entrySet()) {
            arrayList.add(((String) next.getKey()) + str + a(b(next.getValue())));
        }
        return TextUtils.join(str2, arrayList);
    }

    public static String a(boolean z) {
        return z ? "✓" : "X";
    }

    public static List<Object> a(Object obj) {
        if (obj == null || !(obj instanceof List)) {
            return null;
        }
        return (List) obj;
    }

    public static <T> List<T> a(List<T> list, T t) {
        if (t != null) {
            list.add(t);
        }
        return list;
    }

    public static <K, V> Map<K, V> a(Map<K, V> map, K k, V v) {
        if (v != null) {
            map.put(k, v);
        }
        return map;
    }

    public static <T> Map<T, String> a(Map<T, String> map, T t, String str, String str2) {
        if (!(str == null || t == null)) {
            String str3 = map.get(t);
            map.put(t, (str3 == null ? "" : str3 + str2) + str);
        }
        return map;
    }

    public static Map<String, String> a(String... strArr) {
        HashMap<String, String> hashMap = new HashMap<>();
        for (int i = 0; i < strArr.length / 2; i++) {
            hashMap.put(strArr[i * 2], strArr[(i * 2) + 1]);
        }
        return hashMap;
    }

    public static <T> boolean a(T t, T... tArr) {
        for (T equals : tArr) {
            if (t.equals(equals)) {
                return true;
            }
        }
        return false;
    }

    public static boolean a(String str, String... strArr) {
        for (String contains : strArr) {
            if (str.contains(contains)) {
                return true;
            }
        }
        return false;
    }

    public static String b(Object obj) {
        return b(obj, (String) null);
    }

    public static String b(Object obj, String str) {
        try {
            return obj.toString();
        } catch (Exception e) {
            return str;
        }
    }

    public static String b(String str) {
        if (str == null || str.trim().length() == 0) {
            return null;
        }
        return str;
    }

    public static String b(String str, String str2) {
        if (str2.equals(str)) {
            return null;
        }
        return str;
    }

    public static String b(String str, String... strArr) {
        return a(c(str, strArr));
    }

    public static <T> List<T> b(T... tArr) {
        ArrayList<T> arrayList = new ArrayList<>();
        for (int i = 0; i < tArr.length; i++) {
            if (tArr[i] != null) {
                arrayList.add(tArr[i]);
            }
        }
        return arrayList;
    }

    public static Set<String> b(String... strArr) {
        HashSet<String> hashSet = new HashSet<>();
        for (String add : strArr) {
            hashSet.add(add);
        }
        return hashSet;
    }

    public static int c(Object obj) {
        return a(obj, 0);
    }

    public static String c(String str) {
        return a(str).replaceAll(".", "$0​");
    }

    public static String c(String str, String... strArr) {
        boolean z;
        StringBuilder sb = new StringBuilder();
        boolean z2 = true;
        int length = strArr.length;
        int i = 0;
        while (i < length) {
            String str2 = strArr[i];
            if (str2 != null) {
                if (!z2) {
                    str2 = str + str2;
                }
                sb.append(str2);
                z = false;
            } else {
                z = z2;
            }
            i++;
            z2 = z;
        }
        if (z2) {
            return null;
        }
        return sb.toString();
    }

    public static Matcher c(String str, String str2) {
        return Pattern.compile(str2).matcher(str);
    }

    public static String d(String str, String... strArr) {
        StringBuilder sb = new StringBuilder();
        int length = strArr.length;
        int i = 0;
        boolean z = true;
        while (i < length) {
            String str2 = strArr[i];
            if (str2 == null) {
                return null;
            }
            if (!z) {
                str2 = str + str2;
            }
            sb.append(str2);
            i++;
            z = false;
        }
        return sb.toString();
    }

    public static boolean d(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static boolean d(String str, String str2) {
        return c(str, str2).matches();
    }

    public static String e(String str, String... strArr) {
        StringBuilder sb = new StringBuilder();
        boolean z = true;
        HashSet<String> hashSet = new HashSet<>();
        for (String str2 : strArr) {
            if (str2 != null && !hashSet.contains(str2)) {
                sb.append(z ? str2 : str + str2);
                hashSet.add(str2);
                z = false;
            }
        }
        if (z) {
            return null;
        }
        return sb.toString();
    }

    public static boolean e(String str, String str2) {
        return c(str, str2).find();
    }

    public static String f(String str, String str2) {
        try {
            Matcher matcher = Pattern.compile(str2).matcher(str);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
        }
        return null;
    }

}
