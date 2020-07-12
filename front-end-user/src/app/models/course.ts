export class Course {
  id: number;
  courseCode: string;
  name: string;
  imgUrl: string;

  constructor(courseCode: string, name: string) {
    this.courseCode = courseCode;
    this.name = name;
  }

}
