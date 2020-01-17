package ru.rsatu.seryakova.POJO.infOfGroup;

import java.util.List;

public class Course {
    private Integer courseNumber;
    private List<Group> group;

    public Integer getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(Integer courseNumber) {
        this.courseNumber = courseNumber;
    }

    public List<Group> getGroup() {
        return group;
    }

    public void setGroup(List<Group> group) {
        this.group = group;
    }
}
