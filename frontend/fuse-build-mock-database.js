#!/usr/bin/env node
const fs = require('fs');
const path = require('path');

const mockdata = require('./mockdata');
const buildMockDatabase = ({dist, doGeocoding}) => {
  return new Promise((resolve, reject) => {
    const dbpath = path.resolve(dist, "db.json");
    fs.writeFile(dbpath,
        JSON.stringify(mockdata(doGeocoding), null, '\t'), (err) =>  {
          if (err) {
            throw err;
          }
        });
    return resolve();
  });
}

module.exports = {buildMockDatabase};
