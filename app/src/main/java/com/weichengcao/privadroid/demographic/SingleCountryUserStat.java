package com.weichengcao.privadroid.demographic;

public class SingleCountryUserStat {
    private String active;
    private String target;
    private String total;

    public SingleCountryUserStat(String active, String target, String total) {
        this.active = active;
        this.target = target;
        this.total = total;
    }

    public int getActive() {
        if (active == null || active.isEmpty()) {
            return -1;
        }
        return Integer.parseInt(active);
    }

    public int getTarget() {
        if (target == null || target.isEmpty()) {
            return -1;
        }
        return Integer.parseInt(target);
    }

    public int getTotalCount() {
        if (total == null || total.isEmpty()) {
            return -1;
        }
        return Integer.parseInt(total);
    }
}
