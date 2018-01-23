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
var webpack = require('webpack-stream');
var merge = require('merge2');
var watch = require('gulp-watch');
var rev = require('gulp-rev');
var revReplace = require("gulp-rev-replace");
var clean = require('gulp-clean');
var sourcemaps = require('gulp-sourcemaps');
var glob = require('glob');
var colors = require('colors');

var paths = {
    infra: '../infra-front',
    toolkit: '../toolkit'
};

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

gulp.task('drop-cache', function(){
    return gulp.src(['./src/main/resources/public/dist'], { read: false })
        .pipe(clean());
});

gulp.task('copy-files', ['drop-cache'], () => {
    var html = gulp.src('./node_modules/entcore/src/template/**/*.html')
        .pipe(gulp.dest('./src/main/resources/public/template/entcore'));
var bundle = gulp.src('./node_modules/entcore/bundle/*')
    .pipe(gulp.dest('./src/main/resources/public/dist/entcore'));

return merge(html, bundle);
})

gulp.task('webpack', ['copy-files'], function(){ return startWebpack() });

gulp.task('rev', ['webpack'], function () {
    updateRefs();
});

gulp.task('build', ['rev'], function () {
    var refs = updateRefs();
    var copyBehaviours = gulp.src('./src/main/resources/public/dist/behaviours.js')
        .pipe(gulp.dest('./src/main/resources/public/js'));
    return merge[refs, copyBehaviours];
});

gulp.task('removeTemp', function () {
    return gulp.src(['./src/main/resources/public/temp'], { read: false })
        .pipe(clean());
});

function getModName(fileContent){
    var getProp = function(prop){
        return fileContent.split(prop + '=')[1].split(/\r?\n/)[0];
    }
    return getProp('modowner') + '~' + getProp('modname') + '~' + getProp('version');
}

gulp.task('watch', () => {
    var springboard = argv.springboard;
if(!springboard){
    springboard = '../springboard-ent77/';
}
if(springboard[springboard.length - 1] !== '/'){
    springboard += '/';
}

gulp.watch('./src/main/resources/public/modules/**/*.ts', () => gulp.start('build'));

fs.readFile("./gradle.properties", "utf8", function(error, content){
    var modName = getModName(content);
    gulp.watch(['./src/main/resources/public/template/**/*.html', '!./src/main/resources/public/template/entcore/*.html'], () => {
        console.log('Copying resources to ' + springboard + 'mods/' + modName);
    gulp.src('./src/main/resources/**/*')
        .pipe(gulp.dest(springboard + 'mods/' + modName));
});

    gulp.watch('./src/main/resources/view/**/*.html', () => {
        console.log('Copying resources to ' + springboard + 'mods/' + modName);
    gulp.src('./src/main/resources/**/*')
        .pipe(gulp.dest(springboard + 'mods/' + modName));
});
});
});