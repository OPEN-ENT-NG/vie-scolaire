/*
 * Copyright (c) Région Hauts-de-France, Département 77, CGI, 2016.
 *
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */

var gulp = require("gulp");
var webpack = require("webpack-stream");
var merge = require("merge2");
var watch = require("gulp-watch");
var rev = require("gulp-rev");
const replace = require("gulp-replace");
var clean = require("gulp-clean");
var sourcemaps = require("gulp-sourcemaps");
var glob = require("glob");
var colors = require("colors");
const mergeStream = require("merge-stream");

var paths = {
  infra: "./bower_components/entcore",
  toolkit: "../toolkit",
};

function startWebpack(isLocal) {
  gulp.src("./manifests/*").pipe(clean());

  var vsco = gulp
    .src("./src/main/resources/public/modules/viescolaire/")
    .pipe(
      webpack(
        require("./src/main/resources/public/modules/viescolaire/webpack.config.vsco.js")
      )
    )
    .pipe(gulp.dest("./src/main/resources/public/dist/"))
    .pipe(sourcemaps.init({ loadMaps: true }))
    .pipe(rev())
    .pipe(sourcemaps.write("."))
    .pipe(gulp.dest("./src/main/resources/public/dist/"))
    .pipe(rev.manifest({ path: "./manifests/vsco.json" }))
    .pipe(gulp.dest("./"));

  return vsco;
}

function updateRefs() {
  const notifyFiles = gulp
    .src("./src/main/resources/view-src/notify/**/*.html")
    .pipe(replace("@@VERSION", Date.now()))
    .pipe(gulp.dest("./src/main/resources/view/notify"));

  const otherFiles = gulp
    .src([
      "./src/main/resources/view-src/**/*.html",
      "!./src/main/resources/view-src/notify/**/*.html",
    ])
    .pipe(replace("@@VERSION", Date.now()))
    .pipe(gulp.dest("./src/main/resources/view/viescolaire"));

  return mergeStream(notifyFiles, otherFiles);
}

gulp.task("drop-cache", function () {
  return gulp
    .src(["./src/main/resources/public/dist"], { read: false })
    .pipe(clean());
});

gulp.task("copy-mdi-font", ["drop-cache"], function () {
  return gulp
    .src("./node_modules/@mdi/font/fonts/*")
    .pipe(gulp.dest("./src/main/resources/public/font/material-design/fonts"));
});

gulp.task("webpack", ["copy-mdi-font"], function () {
  return startWebpack();
});

gulp.task("copyBehaviours", ["webpack"], function () {
  return gulp
    .src("./src/main/resources/public/dist/behaviours.js")
    .pipe(gulp.dest("./src/main/resources/public/js"));
});
gulp.task("build", ["copyBehaviours"], function () {
  var refs = updateRefs();
  var copyBehaviours = gulp
    .src("./src/main/resources/public/dist/behaviours.js")
    .pipe(gulp.dest("./src/main/resources/public/js"));
  return merge[(refs, copyBehaviours)];
});

gulp.task("removeTemp", function () {
  return gulp
    .src(["./src/main/resources/public/temp"], { read: false })
    .pipe(clean());
});

function getModName(fileContent) {
  var getProp = function (prop) {
    return fileContent.split(prop + "=")[1].split(/\r?\n/)[0];
  };
  return (
    getProp("modowner") + "~" + getProp("modname") + "~" + getProp("version")
  );
}

gulp.task("watch", () => {
  var springboard = argv.springboard;
  if (!springboard) {
    springboard = "../springboard-ent77/";
  }
  if (springboard[springboard.length - 1] !== "/") {
    springboard += "/";
  }

  gulp.watch("./src/main/resources/public/modules/**/*.ts", () =>
    gulp.start("build")
  );

  fs.readFile("./gradle.properties", "utf8", function (error, content) {
    var modName = getModName(content);
    gulp.watch(
      [
        "./src/main/resources/public/template/**/*.html",
        "!./src/main/resources/public/template/entcore/*.html",
      ],
      () => {
        console.log("Copying resources to " + springboard + "mods/" + modName);
        gulp
          .src("./src/main/resources/**/*")
          .pipe(gulp.dest(springboard + "mods/" + modName));
      }
    );

    gulp.watch("./src/main/resources/view/**/*.html", () => {
      console.log("Copying resources to " + springboard + "mods/" + modName);
      gulp
        .src("./src/main/resources/**/*")
        .pipe(gulp.dest(springboard + "mods/" + modName));
    });
  });
});
