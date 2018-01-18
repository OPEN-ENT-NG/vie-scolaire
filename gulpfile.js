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

var gulp = require('gulp');
var ts = require('gulp-typescript');
var webpack = require('webpack-stream');
var bower = require('gulp-bower');
var merge = require('merge2');
var watch = require('gulp-watch');
var rev = require('gulp-rev');
var revReplace = require("gulp-rev-replace");
var clean = require('gulp-clean');
var sourcemaps = require('gulp-sourcemaps');
var typescript = require('typescript');
var glob = require('glob');
var colors = require('colors');

var paths = {
    infra: '../infra-front',
    toolkit: '../toolkit'
};

function compileTs(){
    var tsProject = ts.createProject('./tsconfig.json', {
        typescript: typescript
    });
    var tsResult = tsProject.src()
        .pipe(sourcemaps.init())
        .pipe(ts(tsProject));

    return tsResult.js
        .pipe(sourcemaps.write('.'))
        .pipe(gulp.dest('./src/main/resources/public/temp'));
}

function startWebpackEntcore(isLocal) {
    return gulp.src('./src/main/resources/public/module/entcore')
        .pipe(webpack(require('./webpack-entcore.config.js')))
        .pipe(gulp.dest('./src/main/resources/public/dist/entcore'))
        .pipe(sourcemaps.init({ loadMaps: true }))
        .pipe(rev())
        .pipe(sourcemaps.write('.'))
        .pipe(gulp.dest('./src/main/resources/public/dist/entcore'))
        .pipe(rev.manifest({ merge: true, path : './manifests/entcore.json' }))
        .pipe(gulp.dest('./'));
}

function startWebpack(isLocal) {
    gulp.src("./manifests/*").pipe(clean());
    var absc = gulp.src('./src/main/resources/public/modules/absences/')
        .pipe(webpack(require('./src/main/resources/public/modules/absences/webpack.config.absc.js')))
        .pipe(gulp.dest('./src/main/resources/public/dist/absences'))
        .pipe(sourcemaps.init({ loadMaps: true }))
        .pipe(rev())
        .pipe(sourcemaps.write('.'))
        .pipe(gulp.dest('./src/main/resources/public/dist/absences'))
        .pipe(rev.manifest({path : './manifests/absc.json' }))
        .pipe(gulp.dest('./'));

    var vsco = gulp.src('./src/main/resources/public/modules/viescolaire/')
        .pipe(webpack(require('./src/main/resources/public/modules/viescolaire/webpack.config.vsco.js')))
        .pipe(gulp.dest('./src/main/resources/public/dist/viescolaire'))
        .pipe(sourcemaps.init({ loadMaps: true }))
        .pipe(rev())
        .pipe(sourcemaps.write('.'))
        .pipe(gulp.dest('./src/main/resources/public/dist/viescolaire'))
        .pipe(rev.manifest({path : './manifests/vsco.json' }))
        .pipe(gulp.dest('./'));

    return merge([absc, vsco]);
}

function updateRefs() {
    var absc = gulp.src(glob.sync("./src/main/resources/view-src/absences/*.html"))
        .pipe(revReplace({manifest: gulp.src(["./manifests/absc.json", "./manifests/entcore.json"]) }))
        .pipe(gulp.dest("./src/main/resources/view/absences"));
    var vsco = gulp.src(glob.sync("./src/main/resources/view-src/viescolaire/*.html"))
        .pipe(revReplace({manifest: gulp.src(["./manifests/vscos.json", "./manifests/entcore.json"]) }))
        .pipe(gulp.dest("./src/main/resources/view/viescolaire"));
    return merge([absc, vsco]);
}

gulp.task('ts-local', function () { return compileTs() });
gulp.task('webpack-local', ['ts-local'], function(){ return startWebpack() });
gulp.task('webpack-entcore-local', ['webpack-local'], function(){ return startWebpackEntcore() });

gulp.task('ts', function () { return compileTs() });
gulp.task('webpack', ['ts'], function(){ return startWebpack() });
gulp.task('webpack-entcore', ['webpack'], function(){ return startWebpackEntcore() });

gulp.task('drop-temp', ['webpack-entcore'], function() {
    return gulp.src([
        './src/main/resources/public/**/*.map.map',
        './src/main/resources/public/temp',
        './src/main/resources/public/dist/entcore/ng-app.js',
        './src/main/resources/public/dist/entcore/ng-app.js.map',
        './src/main/resources/public/dist/application.js',
        './src/main/resources/public/dist/application.js.map'
    ], { read: false })
        .pipe(clean());
});

gulp.task('build', ['drop-temp'], function () {
    var refs = updateRefs();
    var copyBehaviours = gulp.src('./src/main/resources/public/dist/behaviours.js')
        .pipe(gulp.dest('./src/main/resources/public/js'));
    return merge[refs, copyBehaviours];
});

gulp.task('build-local', ['webpack-entcore-local'], function () {
    var refs = updateRefs();
    var copyBehaviours = gulp.src('./src/main/resources/public/dist/behaviours.js')
        .pipe(gulp.dest('./src/main/resources/public/js'));
    return merge[refs, copyBehaviours];
});

gulp.task('removeTemp', function () {
    return gulp.src(['./src/main/resources/public/temp'], { read: false })
        .pipe(clean());
});

gulp.task('updateRefs', function () {
    updateRefs();
});