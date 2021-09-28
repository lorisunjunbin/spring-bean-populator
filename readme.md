# Spring Bean Populator

Populate List<[Resource](./src/main/java/cn/lori/bean/populator/model/PopulatorResource.java)> to what ever spring bean.

## Usage

- Implement [loadItems](./src/main/java/cn/lori/bean/populator/service/PopulateService.java)

- Add Class annotation: [PopulatorCategory](./src/main/java/cn/lori/bean/populator/annotation/PopulatorCategory.java)

- Add Field annotation: [PopulatorItem](./src/main/java/cn/lori/bean/populator/annotation/PopulatorItem.java) - Optional

## Sample Demo

Refer to [Demo](./src/main/java/cn/lori/demo)

## Refresh on the fly

#### refresh specific bean

http://localhost:8080/rest/refresh/demoWhatEverSpringBean

#### refresh all

http://localhost:8080/rest/refresh/all




echo "# spring-bean-populator" >> README.md
git init
git add README.md
git commit -m "first commit"
git branch -M main
git remote add origin https://github.com/lorisunjunbin/spring-bean-populator.git
git push -u origin main
…or push an existing repository from the command line

git remote add origin https://github.com/lorisunjunbin/spring-bean-populator.git
git branch -M main
git push -u origin main