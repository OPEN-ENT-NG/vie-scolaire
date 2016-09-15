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

/**
 * Created by ledunoiss on 12/09/2016.
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
var glob = require('glob');

var jsInfraPath = './bower_components/entcore';

function compileTs(){
    var tsProject = ts.createProject('./src/main/resources/public/modules/tsconfig.json');
    var tsResult = tsProject.src()
        .pipe(sourcemaps.init())
        .pipe(ts(tsProject));

    return tsResult.js
        .pipe(sourcemaps.write('.'))
        .pipe(gulp.dest('./src/main/resources/public/temp'));
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

    var eval = gulp.src('./src/main/resources/public/modules/evaluations/')
        .pipe(webpack(require('./src/main/resources/public/modules/evaluations/webpack.config.eval.js')))
        .pipe(gulp.dest('./src/main/resources/public/dist/evaluations'))
        .pipe(sourcemaps.init({ loadMaps: true }))
        .pipe(rev())
        .pipe(sourcemaps.write('.'))
        .pipe(gulp.dest('./src/main/resources/public/dist/evaluations'))
        .pipe(rev.manifest({path : './manifests/eval.json' }))
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

    var entcore = gulp.src('./src/main/resources/public/modules/entcore')
        .pipe(webpack(require('./webpack-entcore.config.js')))
        .pipe(gulp.dest('./src/main/resources/public/dist/entcore'))
        .pipe(sourcemaps.init({ loadMaps: true }))
        .pipe(rev())
        .pipe(sourcemaps.write('.'))
        .pipe(gulp.dest('./src/main/resources/public/dist/entcore'))
        .pipe(rev.manifest({ path : './manifests/entcore.json' }))
        .pipe(gulp.dest('./'));

    return merge([entcore, absc, eval, vsco]);
}

function updateRefs() {
    var absc = gulp.src(glob.sync("./src/main/resources/view-src/absences/*.html"))
        .pipe(revReplace({manifest: gulp.src("./manifests/absc.json") }))
        .pipe(gulp.dest("./src/main/resources/view/absences"));
    var eval = gulp.src(glob.sync("./src/main/resources/view-src/evaluations/*.html"))
        .pipe(revReplace({manifest: gulp.src("./manifests/eval.json") }))
        .pipe(gulp.dest("./src/main/resources/view/evaluations"));
    var vsco =  gulp.src(glob.sync("./src/main/resources/view-src/viescolaire/*.html"))
        .pipe(revReplace({manifest: gulp.src("./manifests/vscos.json") }))
        .pipe(gulp.dest("./src/main/resources/view/viescolaire"));
    return merge([absc, eval, vsco]);
}

gulp.task('copy-local-libs', function(){
    var ts = gulp.src(jsInfraPath + '/src/ts/**/*.ts')
        .pipe(gulp.dest('./src/main/resources/public/modules/entcore'));
    var html = gulp.src(jsInfraPath + '/src/template/**/*.html')
        .pipe(gulp.dest('./src/main/resources/public/templates/entcore'));
    return merge([html, ts]);
});

gulp.task('drop-cache', function(){
    return gulp.src(['./bower_components', './src/main/resources/public/dist'], { read: false })
        .pipe(clean());
});

gulp.task('bower', ['drop-cache'], function(){
    return bower({ directory: './bower_components', cwd: '.', force: true });
});

gulp.task('update-libs', ['bower'], function(){
    var ts = gulp.src(jsInfraPath + '/src/ts/**/*.ts')
        .pipe(gulp.dest('./src/main/resources/public/modules/entcore'));
    var html = gulp.src(jsInfraPath + '/src/template/**/*.html')
        .pipe(gulp.dest('./src/main/resources/public/templates/entcore'));
    return merge([html, ts]);
});

gulp.task('ts-local', ['copy-local-libs'], function () {
    gulp.src('./src/main/resources/public/dist')
        .pipe(clean());
    gulp.src('./src/main/resources/public/temp')
        .pipe(clean());
    gulp.src('./src/main/resources/view')
        .pipe(clean());
    return compileTs()
});
gulp.task('webpack-local', ['ts-local'], function(){ return startWebpack() });

gulp.task('ts', ['update-libs'], function () {
    gulp.src('./src/main/resources/public/dist')
        .pipe(clean());
    gulp.src('./src/main/resources/public/temp')
        .pipe(clean());
    gulp.src('./src/main/resources/view')
        .pipe(clean());
    return compileTs()
});
gulp.task('webpack', ['ts'], function(){ return startWebpack() });

gulp.task('build', ['webpack'], function () {
    var refs = updateRefs();
    var copyBehaviours = gulp.src('./src/main/resources/public/temp/behaviours.js')
        .pipe(gulp.dest('./src/main/resources/public/js'));
    return merge[refs, copyBehaviours];
});

gulp.task('build-local', ['webpack-local'], function () {
    var refs = updateRefs();
    var copyBehaviours = gulp.src('./src/main/resources/public/temp/behaviours.js')
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
