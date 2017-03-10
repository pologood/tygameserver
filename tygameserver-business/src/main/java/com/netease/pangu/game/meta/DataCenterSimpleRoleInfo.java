package com.netease.pangu.game.meta;

import java.io.Serializable;
import java.math.BigInteger;

public class DataCenterSimpleRoleInfo implements Serializable {

    private static final long serialVersionUID = -3667736206287426197L;
    private long writeToDbTime;
    private long createTime;
    private int totalMallScore;
    private String phone;
    private int sex;
    private String playerName;
    private BigInteger gbId;
    private String lastLoginServerName;
    private String serverName;
    private int accountValid;
    private int mallScore;
    private int level;
    private int school;
    private String guildName;
    private int combatScore;

    public long getWriteToDbTime() {
        return writeToDbTime;
    }

    public void setWriteToDbTime(long writeToDbTime) {
        this.writeToDbTime = writeToDbTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getTotalMallScore() {
        return totalMallScore;
    }

    public void setTotalMallScore(int totalMallScore) {
        this.totalMallScore = totalMallScore;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public BigInteger getGbId() {
        return gbId;
    }

    public void setGbId(BigInteger gbId) {
        this.gbId = gbId;
    }

    public String getLastLoginServerName() {
        return lastLoginServerName;
    }

    public void setLastLoginServerName(String lastLoginServerName) {
        this.lastLoginServerName = lastLoginServerName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getAccountValid() {
        return accountValid;
    }

    public void setAccountValid(int accountValid) {
        this.accountValid = accountValid;
    }

    public int getMallScore() {
        return mallScore;
    }

    public void setMallScore(int mallScore) {
        this.mallScore = mallScore;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getSchool() {
        return school;
    }

    public void setSchool(int school) {
        this.school = school;
    }

    public String getGuildName() {
        return guildName;
    }

    public void setGuildName(String guildName) {
        this.guildName = guildName;
    }

    public int getCombatScore() {
        return combatScore;
    }

    public void setCombatScore(int combatScore) {
        this.combatScore = combatScore;
    }
}
