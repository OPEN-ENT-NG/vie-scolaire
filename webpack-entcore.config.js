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

var webpack = require('webpack');
var path = require('path');

module.exports = {
    entry: {
        'ng-app': './src/main/resources/public/temp/entcore/ng-app.js'
    },
    output: {
        filename: '[name].js',
        library: 'ng_app',
        path: __dirname + 'dest'
    },
    resolve: {
        modulesDirectories: ['bower_components', 'node_modules'],
        root: path.resolve('.'),
        alias: {
            'jquery': path.resolve('./bower_components/jquery/dist/jquery.min.js'),
            'lodash': path.resolve('./bower_components/lodash/dist/lodash.min.js'),
            'underscore': path.resolve('./bower_components/underscore/underscore-min.js'),
            'moment': path.resolve('./bower_components/moment/min/moment-with-locales.min.js'),
            'humane-js': path.resolve('./bower_components/humane-js/humane.min.js'),
            'angular': path.resolve('./bower_components/angular/angular.min.js'),
            'angular-route': path.resolve('./bower_components/angular-route/angular-route.min.js'),
            'angular-sanitize': path.resolve('./bower_components/angular-sanitize/angular-sanitize.min.js'),
            'entcore-toolkit': path.resolve('./node_modules/toolkit/index.js'),
            'toolkit': path.resolve('./node_modules/toolkit/index.js'),
            'entcore-toolkit': path.resolve('./node_modules/toolkit/index.js'),
            'axios': path.resolve('./node_modules/axios/dist/axios.js')
        },
        extensions: ['', '.js']
    },
    devtool: "source-map",
    module: {
        preLoaders: [
            {
                test: /\.js$/,
                loader: 'source-map-loader'
            }
        ]
    }
}