#!/usr/bin/env node
const {defaultLanguage, defaultNamespace, i18nextConfig} = require('./src/i18n/i18nextConfig');
const fs = require('fs');
const Parser = require('i18next-scanner').Parser;
const converter = require('i18next-conv');
const mkdirp = require('mkdirp');
const {Sparky} = require('fuse-box');

const utf8 = 'utf-8';

const options = {
  lngs: [defaultLanguage],
  func: {
    list: ['translate', 'firstUpperTranslated'],
  },
};

const parserOptions = Object.assign(options, i18nextConfig);
const parser = new Parser(parserOptions);

const readFileContent = file => file.read().contents.toString(utf8);
const accumulateContent = (prev, curr) => prev + curr;

const createPotFile = async ({base, outputDir}) => {
  return Sparky.src('**/*.+(ts|tsx)', {base})
    .completed(async (files) => {
      const content = files
        .map(readFileContent)
        .reduce(accumulateContent, '');

      parser.parseFuncFromString(content, parserOptions);
      parser.parseAttrFromString(content, parserOptions);

      const json = parser.get()[defaultLanguage][defaultNamespace];
      const data = await converter.i18nextToPot(defaultLanguage, JSON.stringify(json), {quiet: true});

      mkdirp.sync(outputDir);
      fs.writeFileSync(`${outputDir}/template.pot`, data, utf8);
    })
    .exec();
};

module.exports = {createPotFile};
