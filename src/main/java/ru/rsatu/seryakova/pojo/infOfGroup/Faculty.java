package ru.rsatu.seryakova.pojo.infOfGroup;

import ru.rsatu.seryakova.tables.InfOfGroups;

import java.util.ArrayList;
import java.util.List;

public class Faculty {
    private String facultyName;
    private List<Course> course;

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public List<Course> getCourse() {
        return course;
    }

    public void setCourse(List<Course> course) {
        this.course = course;
    }

    public List<Faculty> getFaculty(List<InfOfGroups> groupsInfo) {
        List<Faculty> faculties = new ArrayList<>();
        Faculty tempFaculty = new Faculty();
        List<Course> tempCourses = new ArrayList<Course>();
        Course tempCourse = new Course();
        List<Group> tempGroups = new ArrayList<Group>();
        String prevFaculty = ""; //предыдущий фак
        Integer prevCourse = -1;//предыдущий курс

        for (int i = 0; i < groupsInfo.size(); i++) {

            if (!groupsInfo.get(i).getFacultyName().equals(prevFaculty)){
                tempFaculty = new Faculty();
                tempFaculty.setFacultyName(groupsInfo.get(i).getFacultyName());
                prevFaculty = tempFaculty.getFacultyName();

                tempCourses = new ArrayList<Course>();
            }

            if (groupsInfo.get(i).getCourseNumber() != prevCourse){
                tempCourse = new Course();
                tempCourse.setCourseNumber(groupsInfo.get(i).getCourseNumber());
                prevCourse = tempCourse.getCourseNumber();

                tempGroups = new ArrayList<Group>();
            }

            tempGroups.add(new Group(groupsInfo.get(i).getSpecialityName(), groupsInfo.get(i).getGroupName()));

            if (groupsInfo.size() == i+1){

                tempCourse.setGroup(tempGroups);
                tempCourses.add(tempCourse);
                tempFaculty.setCourse(tempCourses);
                faculties.add(tempFaculty);

            } else {
                if (!groupsInfo.get(i + 1).getFacultyName().equals(prevFaculty)) {
                    tempCourse.setGroup(tempGroups);
                    tempCourses.add(tempCourse);
                    tempFaculty.setCourse(tempCourses);
                    faculties.add(tempFaculty);
                } else {
                    if (groupsInfo.get(i + 1).getCourseNumber() != prevCourse) {
                        tempCourse.setGroup(tempGroups);
                        tempCourses.add(tempCourse);
                    }
                }
            }
        }
        return faculties;
    }

}
