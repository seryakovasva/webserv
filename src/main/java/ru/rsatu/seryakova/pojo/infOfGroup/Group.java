package ru.rsatu.seryakova.pojo.infOfGroup;

public class Group {
    private String specialityName;
    private String groupName;

    public Group(String specialityName, String groupName){
        this.specialityName = specialityName;
        this.groupName = groupName;
    }

    public String getSpecialityName() {
        return specialityName;
    }

    public void setSpecialityName(String specialityName) {
        this.specialityName = specialityName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

}
