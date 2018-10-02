const {src, context, task} = require('fuse-box/sparky');
const {createPotFile} = require('./fuse-extract-translations');
const {convertPoToJson} = require('./fuse-convert-po-to-json');
const {gitVersion} = require('./fuse-git-version');
const TypeHelper = require('fuse-box-typechecker').TypeHelper;
const {runCLI: runTests} = require('jest');

const distDir = 'dist';
const buildDir = 'build';
const homeDir = 'src';
const fuseboxCacheDir = '.fusebox';
const parcelCacheDir = '.cache';

let isProduction = false;

context(class {});

task('version', async () => await gitVersion());

task('translations', async () => {
  await createPotFile({base: homeDir, outputDir: buildDir});
  await convertPoToJson({
    base: homeDir,
    templateFile: `${buildDir}/template.pot`,
    outputDir: `${distDir}/i18n/locales/`,
  });
});

task('clean', async () => {
  await src(parcelCacheDir).clean(parcelCacheDir).exec();
  await src(fuseboxCacheDir).clean(fuseboxCacheDir).exec();
  await src(buildDir).clean(buildDir).exec();
  await src(distDir).clean(distDir).exec();
});

task('type-checker', () => {
  try {
    TypeHelper({
      tsConfig: './tsconfig.json',
      basePath: './',
      tsLint: './tslint.json',
      name: 'EVO',
      throwOnGlobal: isProduction,
      throwOnSyntactic: isProduction,
      throwOnSemantic: isProduction,
      throwOnTsLint: isProduction,
    })
      .runSync();
  } catch (error) {
    console.error(error, error.stack);
    process.exit(1);
  }
});

task('test', async () => await runTests({bail: isProduction}, [homeDir]));

task('verify', ['test', 'type-checker']);

task('start', ['translations', 'verify']);

task('serve', ['translations', 'type-checker']);

task('set-is-prod', () => isProduction = true);

task('dist', ['set-is-prod', 'clean', 'start']);
