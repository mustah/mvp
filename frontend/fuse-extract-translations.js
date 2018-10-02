#!/usr/bin/env node
const {defaultLanguage, defaultNamespace, i18nextConfig} = require('./src/i18n/i18nextConfig');
const {writeFileSync} = require('fs');
const Parser = require('i18next-scanner').Parser;
const {i18nextToPot} = require('i18next-conv');
const {sync} = require('mkdirp');
const {src} = require('fuse-box/sparky');

const parserOptions = Object.assign({
  lngs: [defaultLanguage],
  func: {
    list: ['translate', 'firstUpperTranslated'],
  },
}, i18nextConfig);
const parser = new Parser(parserOptions);

const readFileContent = file => file.read().contents.toString('utf-8');
const accumulateContent = (prev, curr) => prev + curr;

const createPotFile = async ({base, outputDir}) =>
  await src('**/*.+(ts|tsx)', {base})
    .completed(async (files) => {
      const content = files
        .map(readFileContent)
        .reduce(accumulateContent, '');

      parser.parseFuncFromString(content, parserOptions);
      parser.parseAttrFromString(content, parserOptions);

      const json = parser.get()[defaultLanguage][defaultNamespace];
      const data = await i18nextToPot(defaultLanguage, JSON.stringify(json), {quiet: true});

      sync(outputDir);
      writeFileSync(`${outputDir}/template.pot`, data, 'utf-8');
    })
    .exec();

module.exports = {createPotFile};
