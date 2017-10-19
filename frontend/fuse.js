const {
  FuseBox,
  SVGPlugin,
  CSSPlugin,
  PostCSSPlugin,
  QuantumPlugin,
  SassPlugin,
  TypeScriptHelpers,
  WebIndexPlugin,
  Sparky
} = require('fuse-box');

const {createPotFile} = require('./fuse-extract-translations');
const {convertPoToJson} = require('./fuse-convert-po-to-json');

const autoprefixer = require('autoprefixer');
const TypeHelper = require('fuse-box-typechecker').TypeHelper;
const {runCLI} = require('jest');

const distDir = 'dist';
const homeDir = 'src';
const appCss = 'css/app.css';
const fuseboxCacheDir = '.fusebox/cache';

let fuse, app, vendor, isProduction = false;

const runTests = () => {
  runCLI({bail: isProduction}, ['src']);
};

/**
 * Returns the number of errors
 */
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

const materialDesign = './node_modules/mdi/';
const materialDesignFonts = './fonts/**/*';
const assets = ['**/*.+(svg|png|jpg|jpeg|gif|json)', 'assets/fonts/**/*'];

Sparky.task('config', ['convert-po-to-json'], () => {
  fuse = new FuseBox({
    debug: false,
    homeDir: homeDir,
    sourceMaps: !isProduction,
    hash: isProduction,
    target: 'browser',
    output: `${distDir}/$name.js`,
    plugins: [
      TypeScriptHelpers(),
      SVGPlugin(),
      [
        SassPlugin({outputStyle: 'compressed',}),
        PostCSSPlugin([autoprefixer()]),
        CSSPlugin({
          group: `${appCss}`,
          outFile: `${distDir}/${appCss}`,
        }),
      ],
      WebIndexPlugin({template: `${homeDir}/index.html`}),
      isProduction && QuantumPlugin({
        removeExportsInterop: false,
        uglify: true,
      }),
    ]
  });

  vendor = fuse.bundle('vendor').instructions('~ index.ts');

  app = fuse.bundle('app').instructions('> index.tsx');
});

Sparky.task('remove-fusebox-cache', () => Sparky.src(fuseboxCacheDir).clean(fuseboxCacheDir));

Sparky.task('extract-translations', () => createPotFile({base: homeDir}));

Sparky.task('convert-po-to-json', ['extract-translations'], () => convertPoToJson({base: homeDir}));

Sparky.task('watch:assets', () => Sparky.watch(assets, {base: homeDir}).dest(distDir));

Sparky.task('copy:assets', () => Sparky.src(assets, {base: homeDir}).dest(distDir));

Sparky.task('copy:external-assets', () => Sparky.src([materialDesignFonts], {base: materialDesign}).dest(distDir));

Sparky.task('clean', ['remove-fusebox-cache'], () => Sparky.src(distDir).clean(distDir));

Sparky.task('set-production', () => isProduction = true);

Sparky.task('tests', runTests);

Sparky.task('run-type-checker', runTypeChecker);

Sparky.task('default', ['clean', 'config', 'watch:assets', 'copy:external-assets'], () => {
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
  'tests',
  'copy:assets',
  'copy:external-assets',
];

Sparky.task('dist', distTasks, () => {
  return fuse.run();
});

Sparky.task('dist-server', distTasks, () => {
  fuse.dev();
  return fuse.run();
});

