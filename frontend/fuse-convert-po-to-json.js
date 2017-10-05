#!/usr/bin/env node
const fs = require('fs');
const converter = require('i18next-conv');
const {Sparky} = require('fuse-box');

const utf8 = 'utf-8';
const extractLangFromFileName = (file) => file.name.split('.', 1);

const convertPoToJson = async ({base}) => {

  return Sparky.src('i18n/locales/*.po', {base})
    .completed(async (files) => {
      const templatePot = fs.readFileSync('./src/i18n/locales/template.pot', utf8);
      const templatePotJson = JSON.parse(await converter.gettextToI18next('en', templatePot, {quiet: true}));
      files.map(async (file) => {
        const language = extractLangFromFileName(file);
        const content = file.read().contents.toString(utf8);
        const data = await converter.gettextToI18next(language, content, {quiet: true});
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

        fs.writeFileSync(base + '/i18n/locales/' + language + '.json', data, utf8)
      });
    }).exec()
};

module.exports = {convertPoToJson};