# ============================================
# 厨房一：前端配菜专家 (Frontend Builder)
# ============================================
# 我们用一个带有Node.js环境的镜像来当“配菜厨房”
FROM node:18-alpine AS frontend-builder

# 设置前端项目的工作目录
WORKDIR /app/frontend

# 先只复制 package.json 和 package-lock.json
# 这样可以利用缓存，如果依赖没变，就不用每次都重新 npm install 喵~
COPY tgDrive-front-main/package.json ./
COPY tgDrive-front-main/package-lock.json ./
RUN npm install

# 把前端的所有源代码都复制进来
COPY tgDrive-front-main/ ./

# 执行前端构建命令！制作出香喷喷的“配菜”(dist 文件夹)！
RUN npm run build


# ============================================
# 厨房二：后端主食专家 (Backend Builder)
# ============================================
# 和之前一样，用 Maven 厨房来做主食
FROM maven:3.8.5-openjdk-17 AS backend-builder

# 设置后端项目的工作目录
WORKDIR /app

# 先复制后端项目的 pom.xml 来利用缓存
COPY pom.xml .
RUN mvn dependency:go-offline

# 把后端的源代码复制进来
COPY src ./src

# 【魔法第一步：清空餐盘！】删除旧的静态文件
RUN rm -rf ./src/main/resources/static/*

# 【魔法第二步：摆上新配菜！】从“配菜厨房”里，把做好的 dist 文件夹里的所有东西，都复制到后端的 static 目录里！
COPY --from=frontend-builder /app/frontend/dist ./src/main/resources/static/

# 【魔法第三步：封装便当盒！】执行打包，这时候新的前端文件就会被一起打包进 JAR 里了！
RUN mvn clean package -DskipTests


# ============================================
# 厨房三：打包上架专家 (Final Stage)
# ============================================
# 最后，还是用那个超轻量级的 JRE 厨房
FROM eclipse-temurin:17-jre

# 设置工作目录
WORKDIR /app

# 从“主食厨房”里，把最终封装好的、带有前后端所有内容的便当盒(JAR包)拿过来！
COPY --from=backend-builder /app/target/app.jar app.jar

# 暴露端口
EXPOSE 8085

# 开饭啦！
CMD ["java", "-jar", "app.jar"]