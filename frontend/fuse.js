const {
  FuseBox,
  SVGPlugin,
  CSSPlugin,
  CSSResourcePlugin,
  JSONPlugin,
  PostCSSPlugin,
  QuantumPlugin,
  SassPlugin,
  TypeScriptHelpers,
  WebIndexPlugin,
  Sparky,
} = require('fuse-box');

const {createPotFile} = require('./fuse-extract-translations');
const {convertPoToJson} = require('./fuse-convert-po-to-json');

const autoprefixer = require('autoprefixer');
const TypeHelper = require('fuse-box-typechecker').TypeHelper;
const {runCLI} = require('jest');

const indexFile = 'index.tsx';
const distDir = 'dist';
const buildDir = 'build';
const homeDir = 'src';
const fuseboxCacheDir = '.fusebox/cache';

let fuse, app, isProduction = false;

const runTests = () => {
  runCLI({bail: isProduction}, ['src']);
};

const runTypeChecker = () => {
  try {
    TypeHelper({
      tsConfig: './tsconfig.json',
      basePath: './',
      tsLint: './tslint.json',
      name: `MVP`,
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
};

const assets = ['**/*.+(svg|png|jpg|jpeg|gif|json)', 'assets/fonts/**/*'];

Sparky.task('config', ['convert-po-to-json'], () => {
  fuse = new FuseBox({
    debug: !isProduction,
    homeDir: homeDir,
    sourceMaps: !isProduction,
    hash: isProduction,
    target: 'browser',
    output: `${distDir}/$name.js`,
    plugins: [
      TypeScriptHelpers(),
      SVGPlugin(),
      JSONPlugin(),
      [
        SassPlugin({outputStyle: isProduction && 'compressed'}),
        PostCSSPlugin([autoprefixer()]),
        CSSPlugin({
          group: 'css/app.css',
          outFile: `${distDir}/css/app.css`,
        }),
      ],
      [
        CSSResourcePlugin({
          dist: `${distDir}/css`,
        }),
        CSSPlugin(),
      ],
      WebIndexPlugin({template: `${homeDir}/index.html`}),
      isProduction && QuantumPlugin({
        removeExportsInterop: false,
        uglify: true,
        treeshake: true,
      }),
    ],
  });

  fuse.bundle('vendor').instructions(`~ ${indexFile}`);

  app = fuse.bundle('app').instructions(`!> ${indexFile}`);
});

Sparky.task('remove-fusebox-cache', () => Sparky.src(fuseboxCacheDir).clean(fuseboxCacheDir));

Sparky.task('extract-translations', () => createPotFile({base: homeDir, outputDir: buildDir}));

Sparky.task('convert-po-to-json', ['extract-translations'], () =>
  convertPoToJson({
    base: homeDir,
    templateFile: `${buildDir}/template.pot`,
    outputDir: `${distDir}/i18n/locales/`,
  }));

Sparky.task('watch:assets', () => Sparky.watch(assets, {base: homeDir}).dest(distDir));

Sparky.task('copy:assets', () => Sparky.src(assets, {base: homeDir}).dest(distDir));

Sparky.task('clean', ['remove-fusebox-cache'], () => Sparky.src(distDir).clean(distDir));

Sparky.task('set-production', () => isProduction = true);

Sparky.task('tests', runTests);

Sparky.task('run-type-checker', runTypeChecker);

Sparky.task('default', ['clean', 'config', 'watch:assets'], () => {
  fuse.dev();
  app.watch()
    .hmr()
    .completed(() => {
      runTypeChecker();
      runTests();
    });
  return fuse.run();
});

const distTasks = [
  'set-production',
  'clean',
  'config',
  'run-type-checker',
  'copy:assets',
];

Sparky.task('dist', distTasks, () => {
  return fuse.run();
});

Sparky.task('dist-server', distTasks, () => {
  fuse.dev();
  return fuse.run();
});
