package server;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Directory {
    private Map<String, List<Integer>> directory;

    public Directory() {
        this.directory = new HashMap<>();
    }

    public void add(String key, Integer val) {
        List<Integer> vals = directory.get(key);
        if (vals == null) {
            vals = new ArrayList<>();
            directory.put(key, vals);
        }
        vals.add(val);
    }

    public boolean set(String key, ArrayList<Integer> new_vals) {
        List<Integer> vals = directory.get(key);
        if (vals == null) return false;
        else {
            directory.put(key, new_vals);
            return true;
        }
    }

    public String list() {
        String keys = "";
        Set<String> ks = directory.keySet();
        if (ks.size() == 0) {
            return "EMPTY-DIRECTORY";
        } else {
            boolean first = true;
            for (String k:ks) {
                if (!first) {
                    keys += "," + k;
                } else {
                    keys += k;
                    first = false;
                }
            }
        }
        return keys;
    }

    public boolean delete(String key) {
        if (directory.get(key) != null) {
            directory.remove(key);
            return true;
        }
        return false;
    }

    public String getValue(String key) {
        List<Integer> arr = directory.get(key);
        if (arr != null) {
            if (arr.size() > 0)  return "OK " + arr.get(0).toString();
            else return "EMPTY";
        } else {
            return "UNKNOWN KEY";
        }
    }


    public String getValues(String key) {
        List<Integer> arr = directory.get(key);
        if (arr != null) {
            if (arr.size() > 0) {
                String str = "";
                boolean first = true;
                for (Integer i:arr) {
                    if (first) {
                        str += i.toString();
                        first = false;
                    } else {
                        str += ","+i.toString();
                    }
                }
                return "OK " + str;
            }
            else return "EMPTY";
        } else {
            return "UNKNOWN KEY";
        }
    }

    public String min(String key) {
        List<Integer> vals = directory.get(key);
        if (vals == null) {
            return "UNKNOWN KEY";
        }
        Integer min = 0;
        boolean first = true;
        for (int i: vals) {
            if (first) {
                min = i;
                first = false;
            } else if (i < min) {
                min = i;
            }
        }
        return "OK " + min;
    }

    public String max(String key) {
        List<Integer> vals = directory.get(key);
        if (vals == null) {
            return "UNKNOWN KEY";
        }
        Integer max = 0;
        boolean first = true;
        for (int i: vals) {
            if (first) {
                max = i;
                first = false;
            } else if (i > max) {
                max = i;
            }
        }
        return "OK " + max;
    }

    public String sum(String key) {
        List<Integer> vals = directory.get(key);
        if (vals == null) {
            return "UNKOWN KEY";
        }
        Integer sum = 0;
        boolean first = true;
        for (int i: vals) {
            sum += i;
        }
        return "OK " + sum;
    }

    public void reset() {
        directory = new HashMap<>();
    }
}
