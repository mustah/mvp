#!/usr/bin/env node
const {readFileSync, writeFileSync} = require('fs');
const {gettextToI18next} = require('i18next-conv');
const {sync} = require('mkdirp');
const {src} = require('fuse-box/sparky');

const utf8 = 'utf-8';
const extractLangFromFileName = (file) => file.name.split('.', 1);

const convertPoToJson = async ({base, templateFile, outputDir}) =>
  await src('i18n/locales/*.po', {base}).completed(async (files) => {
      const templatePot = readFileSync(templateFile, utf8);
      const templatePotJson = JSON.parse(await gettextToI18next('en', templatePot, {quiet: true}));
      files.map(async (file) => {
        const language = extractLangFromFileName(file);
        const content = file.read().contents.toString(utf8);
        const data = await gettextToI18next(language, content, {quiet: true});
        const dataJson = JSON.parse(data);

        const poTemplateTranslationsKeys = Object.keys(templatePotJson);
        const poTranslationKeys = Object.keys(dataJson);

        const missingKeys = poTemplateTranslationsKeys.filter(key => poTranslationKeys.indexOf(key) === -1);

        // Check for missing translation keys
        if (missingKeys.length > 0) {
          console.error('Missing keys in file: ' + language + '.po', missingKeys);
          process.exit(1);
        }

        // Check for untranslated keys.
        poTranslationKeys.forEach(key => {
          if (dataJson[key].length === 0) {
            console.error('Empty translation in file: ' + language + '.po, ' + key);
            process.exit(1);
          }
        });

        sync(outputDir);
        writeFileSync(`${outputDir}/${language}.json`, data, utf8);
      });
    })
    .exec();

module.exports = {convertPoToJson};
