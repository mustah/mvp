#!/usr/bin/env node
const fs = require('fs');
const path = require('path');
const mockdata = require('./mockdata');
const buildMockDatabase = ({dist}) => {
  return new Promise((resolve, reject) => {
    const dbpath = path.resolve(dist, "db.json");
    console.log("Building mock database at " + dbpath + "!");
    fs.writeFile(dbpath,
        JSON.stringify(mockdata(), null, "\t"), (err) =>  {
          if (err) {
            throw err;
          }
          console.log("Wrote db.json!");
        });

    return resolve();
  })
}

module.exports = {buildMockDatabase};
