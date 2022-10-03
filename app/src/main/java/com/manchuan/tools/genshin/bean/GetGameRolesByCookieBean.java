package com.manchuan.tools.genshin.bean;

import java.util.List;

/*
* 通过Cookie获取原神游戏角色
*
* */

public class GetGameRolesByCookieBean {
    private List<ListBean> list;

    public List<ListBean> getList() {
        return this.list;
    }

    public void setList(List<ListBean> list2) {
        this.list = list2;
    }

    public static class ListBean {
        private String game_biz;
        private String game_uid;
        private boolean is_chosen;
        private boolean is_official;
        private int level;
        private String nickname;
        private String region;
        private String region_name;

        public String getGame_biz() {
            return this.game_biz;
        }

        public void setGame_biz(String str) {
            this.game_biz = str;
        }

        public String getRegion() {
            return this.region;
        }

        public void setRegion(String str) {
            this.region = str;
        }

        public String getGame_uid() {
            return this.game_uid;
        }

        public void setGame_uid(String str) {
            this.game_uid = str;
        }

        public String getNickname() {
            return this.nickname;
        }

        public void setNickname(String str) {
            this.nickname = str;
        }

        public int getLevel() {
            return this.level;
        }

        public void setLevel(int i) {
            this.level = i;
        }

        public boolean isIs_chosen() {
            return this.is_chosen;
        }

        public void setIs_chosen(boolean z) {
            this.is_chosen = z;
        }

        public String getRegion_name() {
            return this.region_name;
        }

        public void setRegion_name(String str) {
            this.region_name = str;
        }

        public boolean isIs_official() {
            return this.is_official;
        }

        public void setIs_official(boolean z) {
            this.is_official = z;
        }
    }
}
